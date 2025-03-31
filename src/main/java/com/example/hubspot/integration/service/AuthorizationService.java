package com.example.hubspot.integration.service;

import com.example.hubspot.integration.model.Contact;
import com.example.hubspot.integration.model.Token;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Map;

@Service
public class AuthorizationService {

    private final RestTemplate restTemplate;

    @Value("${hubspot.oauth.client.id}")
    private String clientId;

    @Value("${hubspot.oauth.client.secret}")
    private String clientSecret;

    @Value("${hubspot.oauth.redirect.uri}")
    private String redirectUri;

    @Value("${hubspot.oauth.scope}")
    private String scope;

    public AuthorizationService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String generateAuthorizationUrl() {
        if (clientId == null || redirectUri == null || scope == null) {
            throw new IllegalStateException("OAuth parameters are not properly configured.");
        }
        return String.format("https://app.hubspot.com/oauth/authorize?client_id=%s&redirect_uri=%s&scope=%s&response_type=code",
                clientId, redirectUri, scope);
    }

    public Token exchangeCodeForAccessToken(String code) {
        if (code == null || code.isEmpty()) {
            throw new IllegalArgumentException("Authorization code cannot be empty.");
        }

        String tokenUrl = "https://api.hubapi.com/oauth/v1/token";
        System.out.println("Requesting token with code: " + code);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("redirect_uri", redirectUri);  // Make sure redirectUri is correct
        body.add("code", code);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, new HttpHeaders());

        try {
            System.out.println("Sending POST request to " + tokenUrl);

            ResponseEntity<Token> response = restTemplate.postForEntity(tokenUrl, request, Token.class);

            System.out.println("API Response: " + response.getBody());

            return response.getBody();
        } catch (HttpClientErrorException e) {
            System.err.println("Error exchanging code for token: " + e.getResponseBodyAsString());
            throw new RuntimeException("Error exchanging code for token: " + e.getResponseBodyAsString(), e);
        }
    }

    public void createContact(Contact contact, String accessToken) {
        if (accessToken == null || accessToken.isEmpty()) {
            throw new IllegalArgumentException("Access Token cannot be null or empty.");
        }

        String url = "https://api.hubapi.com/crm/v3/objects/contacts";
        Map<String, Object> requestBody = Map.of("properties", Map.of(
                "email", contact.getEmail(),
                "firstname", contact.getFirstName(),
                "lastname", contact.getLastName()));

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            restTemplate.postForEntity(url, entity, String.class);
        } catch (HttpClientErrorException.Unauthorized ex) {
            throw new RuntimeException("Missing or invalid token.");
        } catch (HttpClientErrorException e) {
            throw new RuntimeException("Error creating contact: " + e.getResponseBodyAsString(), e);
        }
    }

    public String getContacts(String accessToken) {
        if (accessToken == null || accessToken.isEmpty()) {
            throw new IllegalArgumentException("Access Token cannot be null or empty.");
        }

        String url = "https://api.hubapi.com/crm/v3/objects/contacts?properties=firstname,lastname,email&limit=100";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            return response.getBody();
        } catch (HttpClientErrorException e) {
            throw new RuntimeException("Error listing contacts: " + e.getResponseBodyAsString(), e);
        }
    }
}
