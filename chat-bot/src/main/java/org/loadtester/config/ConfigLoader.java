package org.loadtester.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.loadtester.dto.LoadTestConfig;

import java.io.InputStream;

public class ConfigLoader {

    public static LoadTestConfig load(String resourcePath) {
        try {
            ObjectMapper mapper = new ObjectMapper();

            InputStream is = ConfigLoader.class.getClassLoader().getResourceAsStream(resourcePath);
            if (is == null) {
                throw new IllegalArgumentException("Resource not found: " + resourcePath);
            }

            return mapper.readValue(is, LoadTestConfig.class);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
