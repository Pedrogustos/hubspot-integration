package com.example.hubspot.integration.controller;

import com.example.hubspot.integration.model.Contact;
import com.example.hubspot.integration.model.Token;
import com.example.hubspot.integration.service.AuthorizationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

@RestController
@RequestMapping("/oauth")
public class AuthorizationController {

    private final AuthorizationService authorizationService;

    public AuthorizationController(AuthorizationService authorizationService) {
        this.authorizationService = authorizationService;
    }

    @GetMapping("/authorize")
    public ResponseEntity<String> getAuthorizationUrl() {
        try {
            return ResponseEntity.ok(authorizationService.generateAuthorizationUrl());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Couldn't generate the authorization URL.");
        }
    }

    @GetMapping("/callback")
    public ResponseEntity<String> callback(@RequestParam String code) {
        System.out.println("Code received: " + code);

        try {
            Token tokenResponse = authorizationService.exchangeCodeForAccessToken(code);

            System.out.println("Token: ");
            System.out.println("Access Token: " + tokenResponse.getAccessToken());
            System.out.println("Refresh Token: " + tokenResponse.getRefreshToken());
            System.out.println("Expires In: " + tokenResponse.getExpiresIn());
            System.out.println("Token Type: " + tokenResponse.getTokenType());

            return ResponseEntity.ok("Token received, check the URL");
        } catch (Exception ex) {
            System.err.println("Error processing callback: " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error processing callback: " + ex.getMessage());
        }
    }

    @PostMapping("/create")
    public ResponseEntity<String> createContact(@RequestBody Contact contact,
                                                @RequestHeader("Authorization") String bearerToken) {
        String token = bearerToken.replace("Bearer ", "");
        try {
            validateContactRequest(contact);
            authorizationService.createContact(contact, token);
            return ResponseEntity.ok("Contact sent for creation");
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (HttpClientErrorException.BadRequest ex) {
            return ResponseEntity.badRequest().body("Error: Check the submitted fields.");
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating contact: " + ex.getMessage());
        }
    }

    private void validateContactRequest(Contact contact) {
        if (contact.getFirstName().isBlank() || contact.getLastName().isBlank() || contact.getEmail().isBlank()) {
            throw new IllegalArgumentException("Error: All fields (firstName, lastName, email) are required.");
        }
        if (!contact.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new IllegalArgumentException("Error: Invalid email address.");
        }
    }

    @GetMapping("/contacts")
    public ResponseEntity<String> getContacts(@RequestHeader("Authorization") String bearerToken) {
        try {
            String token = bearerToken.replace("Bearer ", "");
            String contactsJson = authorizationService.getContacts(token);
            return ResponseEntity.ok(contactsJson);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error listing contacts: " + ex.getMessage());
        }
    }
}
