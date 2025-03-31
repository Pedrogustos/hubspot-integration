package com.example.hubspot.integration.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class Token {

    @JsonProperty
    private String accessToken;

    @JsonProperty
    private String refreshToken;

    @JsonProperty
    private int expiresIn;

    @JsonProperty
    private String tokenType;
}
