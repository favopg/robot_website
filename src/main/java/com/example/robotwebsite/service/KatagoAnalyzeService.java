package com.example.robotwebsite.service;

import com.example.robotwebsite.dto.KatagoAnalyzeRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class KatagoAnalyzeService {

    private static final Logger logger = LoggerFactory.getLogger(KatagoAnalyzeService.class);

    private final RestTemplate restTemplate;

    @Value("${katago.api.url:http://localhost:8000/api/analyze}")
    private String apiUrl;

    public KatagoAnalyzeService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String analyze(KatagoAnalyzeRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<KatagoAnalyzeRequest> entity = new HttpEntity<>(request, headers);

        logger.info("Calling KataGo API at {}: date={}", apiUrl, request.getDate());

        ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, entity, String.class);
        return response.getBody();
    }
}
