package com.playstore.PlayStoreScrapper.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.playstore.PlayStoreScrapper.application.model.PlayStoreItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class RedisStorageService {

    private static final String PLAYSTORE_ITEMS_KEY = "playstore_items";

    private static final Long MAXIMUM_ENTRIES_SIZE = 10000L;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

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

}
