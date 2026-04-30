package com.university.homework.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.config.ElasticsearchConfigurationSupport;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

/**
 * Elasticsearch configuration
 */
@Configuration
@EnableElasticsearchRepositories(
        basePackages = "com.university.homework.repository"
)
@EnableConfigurationProperties(ElasticsearchConfig.ElasticsearchProperties.class)
public class ElasticsearchConfig extends ElasticsearchConfigurationSupport {

    @Getter
    @Setter
    @ConfigurationProperties(prefix = "elasticsearch")
    public static class ElasticsearchProperties {
        private String host = "localhost";
        private int port = 9200;
        private String scheme = "http";
        private int socketTimeout = 60000;
        private int connectTimeout = 5000;
        private String indexPrefix = "homework";
        private int numberOfShards = 5;
        private int numberOfReplicas = 1;
        private long refreshInterval = 1000; // 1 second
    }
}