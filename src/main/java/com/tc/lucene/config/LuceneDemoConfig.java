package com.tc.lucene.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * @author AnthubTC
 * @version 1.0
 * @className LuceneDemoConfig
 * @description
 * @date 2024/1/15 14:15
 **/
@Data
@Configuration
@ConfigurationProperties(prefix = "com.tc.lucene")
public class LuceneDemoConfig {
    private String indexDbPath;

    public String getDemoIndexDbPath(String demo) {
        return indexDbPath + File.separator + demo + File.separator;
    }
}
