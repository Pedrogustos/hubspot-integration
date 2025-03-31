package com.example.hubspot.integration.controller;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/webhook")
public class WebhookController {

    @PostMapping("/contact")
    @Operation(
            summary = "Receive contact creation events",
            description = "Endpoint that receives contact creation events sent by HubSpot."
    )
    public ResponseEntity<String> receiveWebhook(@RequestBody String payload) {
        try {
            System.out.println("Payload received:\n" + payload);
            return ResponseEntity.ok("Successfully received");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing webhook: " + e.getMessage());
        }
    }
}
