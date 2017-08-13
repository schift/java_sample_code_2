package ai.pickaxe.instagram.service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Properties;
import java.util.Scanner;

import org.jinstagram.Instagram;
import org.jinstagram.auth.InstagramAuthService;
import org.jinstagram.auth.model.Token;
import org.jinstagram.auth.model.Verifier;
import org.jinstagram.auth.oauth.InstagramService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.pickaxe.instagram.utils.Constants;

public class OAuth2Service {
    private static final Logger LOG = LoggerFactory.getLogger(OAuth2Service.class);

    private String apiKey;
    private String apiSecret;
    private String callbackURL;
    private String accessToken;
    private String accessTokenSecret;
    private Properties conf;

    public OAuth2Service(Properties conf) {
        this.conf = conf;
        apiKey = conf.getProperty(Constants.API_KEY_PROP_NAME, "");
        apiSecret = conf.getProperty(Constants.API_SECRETS_PROP_NAME, "");
        callbackURL = conf.getProperty(Constants.CALLBACK_URL_PROP_NAME, "");
        accessToken = conf.getProperty(Constants.ACCESS_TOKEN_PROP_NAME, "");
        accessTokenSecret = conf.getProperty(Constants.ACCESS_TOKEN_SECRET_PROP_NAME, "");
    }

    public void updateAccessToken(String accessToken, String accessTokenSecret) {
        this.accessToken = accessToken;
        this.accessTokenSecret = accessTokenSecret;
    }

    public Instagram authorize() throws IOException, URISyntaxException, InterruptedException {
        Instagram instagram = null;
        try {
            Token accTokenObj = new Token(accessToken, accessTokenSecret);
            instagram = new Instagram(accTokenObj);
            instagram.getCurrentUserInfo(); // for access token test
            LOG.info("App successfully authorized!");
            return instagram;
        } catch (Exception e) {
            LOG.error("{}", e.getMessage());
            LOG.error("You need to authorize the app!");
        }

        InstagramService service = new InstagramAuthService()
                .apiKey(apiKey)
                .apiSecret(apiSecret)
                .callback(callbackURL)
                .scope("basic+public_content+follower_list+comments+relationships+likes")
                .build();
        String authorizationUrl = service.getAuthorizationUrl();
        LOG.info("To authorize go to {}", authorizationUrl);

        System.out.println("Paste authorization code: ");
        String authorizationCode = "";
        try (Scanner in = new Scanner(System.in)) {
            authorizationCode = in.next();
        }
        Verifier verifier = new Verifier(authorizationCode);
        Token accessToken = service.getAccessToken(verifier);
        instagram = new Instagram(accessToken);
        instagram.getCurrentUserInfo(); // for access token test
        LOG.info("App successfully authorized!");

        conf.setProperty(Constants.ACCESS_TOKEN_PROP_NAME, accessToken.getToken());
        conf.setProperty(Constants.ACCESS_TOKEN_SECRET_PROP_NAME, accessToken.getSecret());
        LOG.info("Please update token in your {} file as following:", Constants.DEFAULT_AGENT_CONF_FILE);
        LOG.info("{}={}", Constants.ACCESS_TOKEN_PROP_NAME, accessToken.getToken());
        LOG.info("{}={}", Constants.ACCESS_TOKEN_SECRET_PROP_NAME, accessToken.getSecret());
        return instagram;
    }

}
