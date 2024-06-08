package com.playstore.playstorescraper.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.playstore.playstorescraper.application.model.PlayStoreItem;
import com.playstore.playstorescraper.application.model.PublishStatus;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class RedisStorageService {

    private static final String PLAYSTORE_ITEMS_KEY = "playstore_items";

    private static final Long MAXIMUM_ENTRIES_SIZE = 10000L;
    private static final int MAX_ATTEMPTS = 5;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @PostConstruct
    public void init() {
        // Register the Java Time module to handle LocalDateTime serialization/deserialization
        objectMapper.registerModule(new JavaTimeModule());
    }

    public void savePlayStoreItem(PlayStoreItem item) throws JsonProcessingException {
        String json = objectMapper.writeValueAsString(item);
        log.info( "Saving url: {}", item.getUrl() );
        redisTemplate.opsForHash().put(PLAYSTORE_ITEMS_KEY, item.getUrl(), json);
    }

    public PlayStoreItem getPlayStoreItem(String url) throws JsonProcessingException {
        String json = (String) redisTemplate.opsForHash().get(PLAYSTORE_ITEMS_KEY, url);
        return json != null ? objectMapper.readValue(json, PlayStoreItem.class) : null;
    }

    public boolean isCacheFull() {
        return countEntries() >= MAXIMUM_ENTRIES_SIZE;
    }

    private long countEntries() {
        HashOperations<String, String, String> hashOps = redisTemplate.opsForHash();
        Long size = hashOps.size(PLAYSTORE_ITEMS_KEY);
        log.info( "Cache size: {} elements", size);
        return size;
    }

    public List<PlayStoreItem> getUnpublishedItems() {
        HashOperations<String, String, String> hashOps = redisTemplate.opsForHash();
        Map<String, String> allEntries = hashOps.entries(PLAYSTORE_ITEMS_KEY);
        List<PlayStoreItem> unpublishedItems = new ArrayList<>();

        allEntries.forEach((key, json) -> {
            try {
                PlayStoreItem item = objectMapper.readValue(json, PlayStoreItem.class);
                if (item.getPublishStatus() == PublishStatus.UNPUBLISHED && item.getAttemptCount() < MAX_ATTEMPTS) {
                    unpublishedItems.add(item);
                }
            } catch (JsonProcessingException e) {
                log.error("Error deserializing PlayStoreItem for key: {}", key, e);
            }
        });

        return unpublishedItems;
    }
}
