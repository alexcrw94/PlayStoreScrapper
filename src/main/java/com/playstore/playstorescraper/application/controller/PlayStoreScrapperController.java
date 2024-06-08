package com.playstore.playstorescraper.application.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.playstore.playstorescraper.application.service.KafkaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/url-scrapper")
public class PlayStoreScrapperController {

    @Autowired
    private KafkaService kafkaService;

    @GetMapping("/publish")
    public String publish() throws JsonProcessingException {
        kafkaService.publishUnpublishedUrls();
        return "All urls published";
    }
}
