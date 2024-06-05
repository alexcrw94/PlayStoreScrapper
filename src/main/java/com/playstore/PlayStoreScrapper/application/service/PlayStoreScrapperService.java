package com.playstore.PlayStoreScrapper.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.playstore.PlayStoreScrapper.application.model.PlayStoreItem;
import com.playstore.PlayStoreScrapper.application.model.PublishStatus;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PlayStoreScrapperService {

    private static final String URL = "https://play.google.com/store/apps";
    private static final Random random = new Random();
    @Autowired
    private RedisStorageService redisStorageService;

    @PostConstruct
    public void scrapePlayStore() {
        // Setup ChromeDriver
        WebDriver driver = setupWebDriver();

        try {
            driver.get(URL);
            processUrls(driver);
        } catch (Exception e) {
            log.error("Error during scraping: " + e.getMessage(), e);
        } finally {
            driver.quit();
        }
    }

    private WebDriver setupWebDriver() {
        System.setProperty("webdriver.chrome.driver", "C:\\Users\\jandr\\Documents\\chromedriver.exe");
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        return new ChromeDriver(options);
    }

    private void processUrls(WebDriver driver) throws Exception {
        while (!redisStorageService.isCacheFull()) {
            String link = findUrl(driver);
            if (link == null) {
                log.error("No child link to follow up with. Getting back to Home playstore.");
                driver.get(URL);
            } else {
                PlayStoreItem playStoreItem = PlayStoreItem.builder().url(link).publishStatus(PublishStatus.UNPUBLISHED)
                        .lastAttemptTimestamp(LocalDateTime.now()).attemptCount( 0 ).build();
                redisStorageService.savePlayStoreItem(playStoreItem);
            }
        }
        log.error("Cache is full!");
        throw new Exception("Cache is full!");
    }


    private String findUrl(WebDriver driver) throws Exception {
        List<WebElement> linkMatchingCriteria = driver.findElements(By.tagName("a")).stream().filter(link -> {
            String url = link.getAttribute("href");
            if (url == null || !url.contains("details?id=") || url.equals(driver.getCurrentUrl())) {
                return false;
            }
            try {
                return redisStorageService.getPlayStoreItem(url) == null;
            } catch (Exception e) {
                log.error("Error checking Redis for URL: " + url, e);
                return false;
            }
        }).toList();

        if (linkMatchingCriteria.isEmpty()) {
            log.error("No suitable link found in the current site {}" , driver.getCurrentUrl());
            return null;
        }

        int randomIndex = random.nextInt(linkMatchingCriteria.size());
        String randomUrl = linkMatchingCriteria.get(randomIndex).getAttribute("href");
        driver.get(randomUrl);
        return randomUrl;
    }
}
