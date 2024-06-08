package com.playstore.playstorescraper.application.service;

import com.playstore.playstorescraper.application.model.AppCategory;
import com.playstore.playstorescraper.application.model.PlayStoreItem;
import com.playstore.playstorescraper.application.model.PublishStatus;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Service
@Slf4j
public class PlayStoreScrapperService {

    private static final String HOME_URL = "https://play.google.com/store/apps";
    private static final String CATEGORY_URL = "https://play.google.com/store/apps/category/%s";
    private static final List<AppCategory> CATEGORIES = Arrays.asList(
            AppCategory.AUTO_AND_VEHICLES,
            AppCategory.ART_AND_DESIGN,
            AppCategory.BEAUTY,
            AppCategory.BOOKS_AND_REFERENCE,
            AppCategory.BUSINESS,
            AppCategory.COMICS,
            AppCategory.COMMUNICATIONS,
            AppCategory.DATING,
            AppCategory.EDUCATION,
            AppCategory.ENTERTAINMENT,
            AppCategory.EVENTS,
            AppCategory.FINANCE,
            AppCategory.FOOD_AND_DRINK,
            AppCategory.HEALTH_AND_FITNESS,
            AppCategory.HOUSE_AND_HOME,
            AppCategory.LIBRARIES_AND_DEMO,
            AppCategory.LIFESTYLE,
            AppCategory.MAPS_AND_NAVIGATION,
            AppCategory.MEDICAL,
            AppCategory.MUSIC_AND_AUDIO,
            AppCategory.NEWS_AND_MAGAZINES,
            AppCategory.PARENTING,
            AppCategory.PERSONALIZATION,
            AppCategory.PHOTOGRAPHY,
            AppCategory.PRODUCTIVITY,
            AppCategory.SHOPPING,
            AppCategory.SOCIAL,
            AppCategory.SPORTS,
            AppCategory.TOOLS,
            AppCategory.TRAVEL_AND_LOCAL,
            AppCategory.VIDEO_PLAYERS,
            AppCategory.WEATHER
    );

    private static final Random random = new Random();
    @Autowired
    private RedisStorageService redisStorageService;

    @Async
    public void scrapePlayStore() {
        // Setup ChromeDriver
        WebDriver driver = setupWebDriver();

        try {
            driver.get(HOME_URL);
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
        int baseUrlIndex = 0;
        while (!redisStorageService.isCacheFull()) {
            String link = findUrl(driver);
            if (link == null) {
                // Log the current category before incrementing the index
                String categoryUrl = String.format(CATEGORY_URL, CATEGORIES.get(baseUrlIndex));
                log.error("No child link to follow up with. Browsing a new category {}.", categoryUrl);
                driver.get(categoryUrl);

                // Increment the index after using it
                baseUrlIndex = (baseUrlIndex + 1) % CATEGORIES.size();
            } else {
                PlayStoreItem playStoreItem = PlayStoreItem.builder().url(cleanUrl(link)).publishStatus(PublishStatus.UNPUBLISHED)
                        .lastAttemptTimestamp(LocalDateTime.now()).attemptCount( 0 ).build();
                redisStorageService.savePlayStoreItem(playStoreItem);
            }
        }
        log.error("Cache is full!");
        throw new Exception("Cache is full!");
    }

    private String cleanUrl( String link ) {
        return link.replace("https://play.google.com/store/apps/details?id=", "");
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
