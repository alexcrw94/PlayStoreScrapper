package com.playstore.PlayStoreScrapper.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.playstore.PlayStoreScrapper.application.model.PlayStoreItem;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class PlayStoreScrapperService {

    @Autowired
    private RedisStorageService redisStorageService;

    @PostConstruct
    public void scrapePlayStore() {
        // Setup ChromeDriver
        System.setProperty("webdriver.chrome.driver", "C:\\Users\\jandr\\Documents\\chromedriver.exe");
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        WebDriver driver = new ChromeDriver(options);

        try {
            driver.get("https://play.google.com/store/apps");

            String link = findUrl(driver);

            // Navigate to the link
            processUrl(link, driver);

        } catch (Exception e) {
            //
        } finally {
            // Close the driver
            driver.quit();
        }
    }

    private void processUrl(String link, WebDriver driver) throws Exception {
        if (link != null && !redisStorageService.isCacheFull() ) {
            // Its not going to be in the cache because we already check in the method findUrl
            PlayStoreItem playStoreItem =  PlayStoreItem.builder()
                    .url(link)
                    .build();
            redisStorageService.savePlayStoreItem( playStoreItem );
            processUrl( findUrl( driver ), driver );
        } else {
            log.error( "Either the next link is empty or the cache is full." );
            throw new Exception("No suitable link found for in the Play Store.");
        }
    }


    private String findUrl(WebDriver driver) throws JsonProcessingException {
        List<WebElement> links = driver.findElements(By.tagName("a"));
        for (WebElement link : links) {
            String href = link.getAttribute("href");
            if (href != null && href.contains("details?id=") && !href.equals( driver.getCurrentUrl() ) &&
                    redisStorageService.getPlayStoreItem(href) == null ) {
                driver.get( href );
                return href;
            }
        }
        return null;
    }
}
