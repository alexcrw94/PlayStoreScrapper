package com.playstore.playstorescraper.application.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
public class PlayStoreItem {
    private String url;
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private PublishStatus publishStatus;
    private LocalDateTime lastAttemptTimestamp;
    private int attemptCount;

    @Builder(builderMethodName = "hiddenBuilder") // Custom builder method name
    public PlayStoreItem(String url, PublishStatus publishStatus, LocalDateTime lastAttemptTimestamp, int attemptCount) {
        this.url = url;
        this.publishStatus = publishStatus;
        this.lastAttemptTimestamp = lastAttemptTimestamp;
        this.attemptCount = attemptCount;
    }
}
