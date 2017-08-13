package ai.pickaxe.instagram.service;

import static ai.pickaxe.instagram.utils.Constants.KAFKA_BROKER_LIST_PROP_NAME;
import static ai.pickaxe.instagram.utils.Constants.KAFKA_MAX_MESSAGE_BYTES;
import static ai.pickaxe.instagram.utils.Constants.KAFKA_TOPIC_PROP_NAME;
import static org.apache.commons.lang.StringUtils.isBlank;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

public class KafkaService {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaService.class);

    private Producer<String, String> producer;
    private final Properties config;
    private final String topicName;

    public KafkaService(Properties config) {
        this.config = config;
        this.topicName = config.getProperty(KAFKA_TOPIC_PROP_NAME);
        if (isBlank(topicName)) {
            throw new IllegalArgumentException("Missing kafka topic name");
        }
    }

    public void start() {
        producer = new KafkaProducer<String, String>(config);
        LOG.info("Start Kafka Producer for " + topicName + " topic with brokers: " + config.getProperty(KAFKA_BROKER_LIST_PROP_NAME));
    }

    public void send(List<Map<String, String>> rows) {
        for (Map<String, String> r : rows) {
            String message = new Gson().toJson(r).toString();
            send(topicName, UUID.randomUUID().toString(), message);
        }
    }

    public void close() {
        producer.close();
    }

    private boolean send(String topicName, String key, String message) {
        if (message.getBytes().length < KAFKA_MAX_MESSAGE_BYTES) {
            ProducerRecord<String, String> data = new ProducerRecord<String, String>(topicName, key, message);
            producer.send(data);
            return true;
        } else {
            LOG.error("Unable to send a message to Kafka. The message is too long - " + message.getBytes().length + " bytes");
        }
        return false;
    }

}
