package ai.packawe.instagram.utils;

public class Constants {

    public final static String APP_NAME = "Instagram Agent";
    public final static String USER_AGENT = "Mozilla/5.0";
    public final static int MAX_ATTEMPTS = 10;

    public final static String DEFAULT_AGENT_CONF_FILE = "agent.properties";
    public final static String DEFAULT_KAFKA_PRODUCER_CONF_FILE = "kafka-producer.properties";

    public final static long RETRY_PAUSE_IN_MILISEC = 5000; // 5sec
    public final static long RETRY_PAUSE_LIMIT_IN_MILISEC = 50000; // 50sec
    public final static String OPLOG_FILE_PROP_NAME = "oplog.file";
    public final static String INITIAL_SKIP_HOURS_PROP_NAME = "skip.hours";
    public final static String KAFKA_TOPIC_PROP_NAME = "kafka.topic";
    public final static String KAFKA_BROKER_LIST_PROP_NAME = "bootstrap.servers";
    public final static String FETCH_INTERVAL_PROP_NAME = "fetch.interval";
    public final static String USERS_PROP_NAME = "users";
    public final static String TAGS_PROP_NAME = "tags";
    public final static String API_KEY_PROP_NAME = "api.key";
    public final static String API_SECRETS_PROP_NAME = "api.secrets";
    public final static String CALLBACK_URL_PROP_NAME = "callback.url";
    public final static String ACCESS_TOKEN_PROP_NAME = "access.token";
    public final static String ACCESS_TOKEN_SECRET_PROP_NAME = "access.token.secret";

    public final static String GENERATE_KAFKA_INDEXING_SERVICE_MODE_ARG_NAME = "--generate-kafka-indexing";
    public final static String DAEMON_MODE_ARG_NAME = "--daemon";
    public final static String AGENT_CONF_FILE_ARG_NAME = "--agent-conf=";
    public final static String KAFKA_PRODUCER_CONF_FILE_ARG_NAME = "--kafka-producer-conf=";
    public final static String OPLOG_FILE_ARG_NAME = "--oplog-file=";
    public final static String FETCH_INTERVAL_ARG_NAME = "--fetch-interval=";
    public final static String KAFKA_TOPIC_ARG_NAME = "--kafka-topic=";
    public final static String KAFKA_BROKER_LIST_ARG_NAME = "--kafka-broker-list=";
    public final static String USERS_ARG_NAME = "--users=";
    public final static String TAGS_ARG_NAME = "--tags=";
    public final static String API_KEY_ARG_NAME = "--api-key=";
    public final static String API_SECRETS_ARG_NAME = "--api-secrets=";
    public final static String CALLBACK_URL_ARG_NAME = "--callback-url=";
    public final static String ACCESS_TOKEN_ARG_NAME = "--access-token=";
    public final static String ACCESS_TOKEN_SECRET_ARG_NAME = "--access-token-secret=";

    public final static long KAFKA_MAX_MESSAGE_BYTES = 800000;

    public static final String STATUS_INPROGRESS = "in-progress";
    public static final String STATUS_RETRYING = "retrying";
    public static final String STATUS_FAILED = "failed";

    public static final int MAX_METRICS_PER_REQUEST = 10;
    public static final int MAX_DIMENSIONS_PER_REQUEST = 9;
}
