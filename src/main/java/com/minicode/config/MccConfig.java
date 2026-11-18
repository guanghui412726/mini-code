package com.minicode.config;

public record MccConfig(
     String model,
     int matSteps,
     int contextLimit,
     String baseUrl,
     String apiKey
) {

    public static final String DEFAULT_MODEL = "deepseek-flash";

    public static final Integer DEFAULT_MAX_STEPS = 40;
}
