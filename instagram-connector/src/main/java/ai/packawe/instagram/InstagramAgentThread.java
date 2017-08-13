package ai.pickaxe.instagram;

import static ai.pickaxe.instagram.utils.Constants.ACCESS_TOKEN_PROP_NAME;
import static ai.pickaxe.instagram.utils.Constants.ACCESS_TOKEN_SECRET_PROP_NAME;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Properties;

import org.jinstagram.Instagram;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.pickaxe.instagram.service.InstagramService;
import ai.pickaxe.instagram.service.KafkaService;
import ai.pickaxe.instagram.service.OAuth2Service;
import ai.pickaxe.instagram.utils.AgentUtils;
import ai.pickaxe.instagram.utils.Constants;
import ai.pickaxe.instagram.utils.FetchResult;

public class InstagramAgentThread extends Thread {
    private static final Logger LOG = LoggerFactory.getLogger(InstagramAgentThread.class);

    private KafkaService kafkaService;
    private OAuth2Service authService;
    private InstagramService instagramService;
    private Properties agentConf;
    private Properties kafkaConf;
    private File oplogFile;
    private int fetchinterval;

    public InstagramAgentThread(Properties agentConf, Properties kafkaConf) {
        this.agentConf = agentConf;
        this.kafkaConf = kafkaConf;
    }

    public void initialize() {
        oplogFile = new File(agentConf.getProperty(Constants.OPLOG_FILE_PROP_NAME, "logs/oplog.log"));
        try {
            fetchinterval = Integer.parseInt(agentConf.getProperty(Constants.FETCH_INTERVAL_PROP_NAME, "1"));
        } catch (NumberFormatException e) {
            LOG.warn("Invalid fetch.interval property. Set to 1 hour");
            fetchinterval = 1;
        }
        kafkaService = new KafkaService(kafkaConf);
        kafkaService.start();
        LOG.info("Kafka service started");
        authService = new OAuth2Service(agentConf);
        LOG.info("OAuth2 service started");
        instagramService = new InstagramService(agentConf);
        LOG.info("InstagramService started");
    }

    @Override
    public void run() {
        DateTime nextExecution = DateTime.now(DateTimeZone.UTC);
        while (true) {
            process();
            nextExecution = nextExecution.plusHours(fetchinterval);
            waitForNextExecution(nextExecution);
        }
    }

    private void waitForNextExecution(DateTime nextExecution) {
        long pause = nextExecution.getMillis() - DateTime.now(DateTimeZone.UTC).getMillis();
        if (pause > 1000) {
            LOG.info("Waiting " + pause + "ms for next execution");
            AgentUtils.sleep(pause);
        }
    }

    public void process() {
        LOG.info("Trying to read oplog file");
        Properties oplog = readOplog();
        if (oplog.containsKey(ACCESS_TOKEN_PROP_NAME) && oplog.containsKey(ACCESS_TOKEN_SECRET_PROP_NAME)) {
            authService.updateAccessToken(oplog.getProperty(ACCESS_TOKEN_PROP_NAME), oplog.getProperty(ACCESS_TOKEN_SECRET_PROP_NAME));
        }

        LOG.info("Fetching feeds...");
        for (int attempt = 0; attempt < Constants.MAX_ATTEMPTS; ++attempt) {
            try {
                LOG.info("Processing feeds...");
                Instagram instagram = authService.authorize();
                FetchResult result = instagramService.search(instagram);
                if (result != null) {
                    LOG.info("Fetched " + result.usersCounts.size() + " users counts");
                    LOG.info("Fetched " + result.usersFeeds.size() + " users feed objects");
                    LOG.info("Fetched " + result.tags.size() + " tags counts");

                    kafkaService.send(result.usersCounts);
                    kafkaService.send(result.usersFeeds);
                    kafkaService.send(result.tags);

                    int total = result.usersCounts.size() + result.usersFeeds.size() + result.tags.size();
                    LOG.info(total + " documents sent to Kafka");

                    oplog.setProperty(ACCESS_TOKEN_PROP_NAME, agentConf.getProperty(ACCESS_TOKEN_PROP_NAME));
                    oplog.setProperty(ACCESS_TOKEN_SECRET_PROP_NAME, agentConf.getProperty(ACCESS_TOKEN_SECRET_PROP_NAME));
                    saveOplog(oplog);
                    return;
                } else {
                    LOG.error("Unable to process feeds");
                }
            } catch (IOException | URISyntaxException | InterruptedException e1) {
                LOG.error("Processing feeed failed", e1);
            }
            AgentUtils.sleep(Constants.RETRY_PAUSE_IN_MILISEC);
        }
    }

    public void close() {
        kafkaService.close();
    }

    private void saveOplog(Properties oplog) {
        if (oplog != null) {
            try {
                oplog.store(new FileOutputStream(oplogFile), null);
                LOG.info("Oplog successfully saved");
            } catch (IOException e) {
                LOG.error("Unable to save oplog file", e);
            }
        } else {
            LOG.warn("Empty oplog cannot be saved");
        }
    }

    private Properties readOplog() {
        Properties oplog = new Properties();
        try {
            oplog.load(new FileInputStream(oplogFile));
            LOG.info("Oplog successfully loaded");
        } catch (FileNotFoundException e) {
            LOG.warn("Oplog file not found");
        } catch (IOException e) {
            LOG.error("Unable to read oplog file", e);
        }
        return oplog;
    }

}
