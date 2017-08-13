
package ai.pickaxe.instagram.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AgentUtils {

    private static final Logger LOG = LoggerFactory.getLogger(AgentUtils.class);

    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            LOG.warn("Sleep interrupted", e);
        }
    }

    public static Properties loadPropertiesFromClasspath(String filename) throws IOException {
        Properties properties = new Properties();
        properties.load(AgentUtils.class.getClassLoader().getResourceAsStream(filename));
        return properties;
    }

    public static Properties loadPropertiesFromFile(String filename) throws IOException {
        Properties properties = new Properties();
        properties.load(new FileInputStream(filename));
        return properties;
    }
}
