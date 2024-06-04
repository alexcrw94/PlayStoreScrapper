package com.playstore.PlayStoreScrapper.application.controller;

import com.playstore.PlayStoreScrapper.application.service.PlayStoreScrapperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PlayStoreScrapperController {

    @Autowired
    private PlayStoreScrapperService playStoreScrapperService;

    @GetMapping("/scrape")
    public String scrapePlayStore() {
        playStoreScrapperService.scrapePlayStore();
        return "Scraping completed!";
    }
}
