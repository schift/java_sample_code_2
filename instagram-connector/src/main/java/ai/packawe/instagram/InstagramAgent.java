package ai.pickaxe.instagram;

import static ai.pickaxe.instagram.utils.Constants.ACCESS_TOKEN_ARG_NAME;
import static ai.pickaxe.instagram.utils.Constants.ACCESS_TOKEN_PROP_NAME;
import static ai.pickaxe.instagram.utils.Constants.ACCESS_TOKEN_SECRET_ARG_NAME;
import static ai.pickaxe.instagram.utils.Constants.ACCESS_TOKEN_SECRET_PROP_NAME;
import static ai.pickaxe.instagram.utils.Constants.AGENT_CONF_FILE_ARG_NAME;
import static ai.pickaxe.instagram.utils.Constants.API_KEY_ARG_NAME;
import static ai.pickaxe.instagram.utils.Constants.API_KEY_PROP_NAME;
import static ai.pickaxe.instagram.utils.Constants.API_SECRETS_ARG_NAME;
import static ai.pickaxe.instagram.utils.Constants.API_SECRETS_PROP_NAME;
import static ai.pickaxe.instagram.utils.Constants.CALLBACK_URL_ARG_NAME;
import static ai.pickaxe.instagram.utils.Constants.CALLBACK_URL_PROP_NAME;
import static ai.pickaxe.instagram.utils.Constants.DAEMON_MODE_ARG_NAME;
import static ai.pickaxe.instagram.utils.Constants.FETCH_INTERVAL_ARG_NAME;
import static ai.pickaxe.instagram.utils.Constants.FETCH_INTERVAL_PROP_NAME;
import static ai.pickaxe.instagram.utils.Constants.KAFKA_BROKER_LIST_ARG_NAME;
import static ai.pickaxe.instagram.utils.Constants.KAFKA_BROKER_LIST_PROP_NAME;
import static ai.pickaxe.instagram.utils.Constants.KAFKA_PRODUCER_CONF_FILE_ARG_NAME;
import static ai.pickaxe.instagram.utils.Constants.KAFKA_TOPIC_ARG_NAME;
import static ai.pickaxe.instagram.utils.Constants.KAFKA_TOPIC_PROP_NAME;
import static ai.pickaxe.instagram.utils.Constants.OPLOG_FILE_ARG_NAME;
import static ai.pickaxe.instagram.utils.Constants.OPLOG_FILE_PROP_NAME;
import static ai.pickaxe.instagram.utils.Constants.TAGS_ARG_NAME;
import static ai.pickaxe.instagram.utils.Constants.TAGS_PROP_NAME;
import static ai.pickaxe.instagram.utils.Constants.USERS_ARG_NAME;
import static ai.pickaxe.instagram.utils.Constants.USERS_PROP_NAME;
import static org.apache.commons.lang.StringUtils.isNotBlank;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.pickaxe.instagram.utils.AgentUtils;
import ai.pickaxe.instagram.utils.Constants;

public class InstagramAgent {

    private static final Logger LOG = LoggerFactory.getLogger(InstagramAgent.class);

    public static void main(String[] args) throws Exception {
        boolean daemonMode = false;
        String agentConfFile = Constants.DEFAULT_AGENT_CONF_FILE;
        String kafkaProducerConfFile = Constants.DEFAULT_KAFKA_PRODUCER_CONF_FILE;

        Properties agentConf = AgentUtils.loadPropertiesFromClasspath(agentConfFile);
        Properties kafkaConf = AgentUtils.loadPropertiesFromClasspath(kafkaProducerConfFile);

        for (String arg : args) {
            if (DAEMON_MODE_ARG_NAME.equals(arg)) {
                daemonMode = true;
            }
            if (isNotBlank(arg) && arg.startsWith(AGENT_CONF_FILE_ARG_NAME)) {
                agentConfFile = arg.replace(AGENT_CONF_FILE_ARG_NAME, "");
                agentConf = AgentUtils.loadPropertiesFromFile(agentConfFile);
            }
            if (isNotBlank(arg) && arg.startsWith(KAFKA_PRODUCER_CONF_FILE_ARG_NAME)) {
                kafkaProducerConfFile = arg.replace(KAFKA_PRODUCER_CONF_FILE_ARG_NAME, "");
                kafkaConf = AgentUtils.loadPropertiesFromFile(kafkaProducerConfFile);
            }
        }

        for (String arg : args) {
            if (isNotBlank(arg) && arg.startsWith(FETCH_INTERVAL_ARG_NAME)) {
                agentConf.setProperty(FETCH_INTERVAL_PROP_NAME, arg.replace(FETCH_INTERVAL_ARG_NAME, ""));
            }

            if (isNotBlank(arg) && arg.startsWith(OPLOG_FILE_ARG_NAME)) {
                agentConf.setProperty(OPLOG_FILE_PROP_NAME, arg.replace(OPLOG_FILE_ARG_NAME, ""));
            }

            if (isNotBlank(arg) && arg.startsWith(KAFKA_BROKER_LIST_ARG_NAME)) {
                kafkaConf.setProperty(KAFKA_BROKER_LIST_PROP_NAME, arg.replace(KAFKA_BROKER_LIST_ARG_NAME, ""));
            }

            if (isNotBlank(arg) && arg.startsWith(KAFKA_TOPIC_ARG_NAME)) {
                kafkaConf.setProperty(KAFKA_TOPIC_PROP_NAME, arg.replace(KAFKA_TOPIC_ARG_NAME, ""));
            }

            if (isNotBlank(arg) && arg.startsWith(USERS_ARG_NAME)) {
                agentConf.setProperty(USERS_PROP_NAME, arg.replace(USERS_ARG_NAME, ""));
            }

            if (isNotBlank(arg) && arg.startsWith(TAGS_ARG_NAME)) {
                agentConf.setProperty(TAGS_PROP_NAME, arg.replace(TAGS_ARG_NAME, ""));
            }

            if (isNotBlank(arg) && arg.startsWith(API_KEY_ARG_NAME)) {
                agentConf.setProperty(API_KEY_PROP_NAME, arg.replace(API_KEY_ARG_NAME, ""));
            }

            if (isNotBlank(arg) && arg.startsWith(API_SECRETS_ARG_NAME)) {
                agentConf.setProperty(API_SECRETS_PROP_NAME, arg.replace(API_SECRETS_ARG_NAME, ""));
            }

            if (isNotBlank(arg) && arg.startsWith(CALLBACK_URL_ARG_NAME)) {
                agentConf.setProperty(CALLBACK_URL_PROP_NAME, arg.replace(CALLBACK_URL_ARG_NAME, ""));
            }

            if (isNotBlank(arg) && arg.startsWith(ACCESS_TOKEN_ARG_NAME)) {
                agentConf.setProperty(ACCESS_TOKEN_PROP_NAME, arg.replace(ACCESS_TOKEN_ARG_NAME, ""));
            }

            if (isNotBlank(arg) && arg.startsWith(ACCESS_TOKEN_SECRET_ARG_NAME)) {
                agentConf.setProperty(ACCESS_TOKEN_SECRET_PROP_NAME, arg.replace(ACCESS_TOKEN_SECRET_ARG_NAME, ""));
            }

        }

        InstagramAgentThread agentThread = new InstagramAgentThread(agentConf, kafkaConf);
        agentThread.initialize();
        LOG.info("Instagram Agent Initialized");

        if (daemonMode) {
            agentThread.start();
            agentThread.join();
        } else {
            agentThread.process();
        }
        agentThread.close();
    }

}
