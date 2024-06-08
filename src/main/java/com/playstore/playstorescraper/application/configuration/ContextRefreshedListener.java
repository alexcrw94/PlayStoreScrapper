package com.playstore.playstorescraper.application.configuration;

import com.playstore.playstorescraper.application.service.PlayStoreScrapperService;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component
public class ContextRefreshedListener implements ApplicationListener<ContextRefreshedEvent> {

    private final PlayStoreScrapperService playStoreScrapperService;

    public ContextRefreshedListener(PlayStoreScrapperService playStoreScrapperService) {
        this.playStoreScrapperService = playStoreScrapperService;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        playStoreScrapperService.scrapePlayStore();
    }
}