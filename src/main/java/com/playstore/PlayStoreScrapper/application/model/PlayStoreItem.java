package com.playstore.PlayStoreScrapper.application.model;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
public class PlayStoreItem {
    private String url;

    @Builder(builderMethodName = "hiddenBuilder") // Custom builder method name
    public PlayStoreItem(String url) {
        this.url = url;
    }
}
