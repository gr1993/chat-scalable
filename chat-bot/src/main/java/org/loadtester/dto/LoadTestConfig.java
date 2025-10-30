package org.loadtester.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LoadTestConfig {
    private String userIdPrefix;
    private int userCount;
    private int rampUpTimeSeconds;
    private int chatDurationSeconds;
    private int messageLength;
    private String restApiBaseUrl;
    private String webSocketEndpoint;
}

