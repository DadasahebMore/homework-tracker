package com.university.homework.config;

import java.io.IOException;

import com.university.homework.exception.SearchException;
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

      try(RestHighLevelClient client = RestClients.create(
              ClientConfiguration.builder()
                      .connectedTo("elasticsearch:9200")
                      .withConnectTimeout(5000)
                      .withSocketTimeout(60000)
                      .build()).rest()){

    return client;
    } catch (IOException e) {
          throw new SearchException("",e);
      }
  }
}
