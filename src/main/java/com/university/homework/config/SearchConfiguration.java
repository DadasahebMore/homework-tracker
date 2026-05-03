package com.university.homework.config;

import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.erhlc.RestClients;

/** Configuration for Elasticsearch client */
@Configuration
public class SearchConfiguration {

  /** Configure Elasticsearch REST client */
  @Bean
  @ConditionalOnMissingBean
  public RestHighLevelClient elasticsearchClient() {
    final ClientConfiguration clientConfiguration =
        ClientConfiguration.builder()
            .connectedTo("elasticsearch:9200")
            .withConnectTimeout(5000)
            .withSocketTimeout(60000)
            .build();

    return RestClients.create(clientConfiguration).rest();
  }
}
