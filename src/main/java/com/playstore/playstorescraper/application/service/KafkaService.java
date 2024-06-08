package com.playstore.playstorescraper.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.playstore.playstorescraper.application.model.PlayStoreItem;
import com.playstore.playstorescraper.application.model.PublishStatus;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class KafkaService {

    @Autowired
    RedisStorageService redisStorageService;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String TOPIC = "playstore-items";

    public void publishUnpublishedUrls() throws JsonProcessingException {
        List<PlayStoreItem> unpublishedItems = redisStorageService.getUnpublishedItems();
        for (PlayStoreItem item : unpublishedItems) {
            try {
                String itemJson = objectMapper.writeValueAsString(item);
                // Send JSON message to Kafka
                kafkaTemplate.send(new ProducerRecord<>(TOPIC, item.getUrl(), itemJson));
                item.setPublishStatus(PublishStatus.PUBLISHED);
            } catch (Exception e) {
                item.setAttemptCount(item.getAttemptCount() + 1);
                item.setLastAttemptTimestamp(LocalDateTime.now());
            } finally {
                redisStorageService.savePlayStoreItem(item);
            }
        }
    }
}
