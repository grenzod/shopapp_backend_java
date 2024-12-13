package com.project.shopapp.services.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.project.shopapp.services.IAuthorService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@Service
public class AuthorService implements IAuthorService {
    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String googleClientSecret;

    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String googleRedirectUri;

    @Value("${spring.security.oauth2.client.provider.google.user-info-uri}")
    private String googleUserInfoUri;

    @Value("${spring.security.oauth2.client.registration.facebook.client-id}")
    private String faceClientId;
    @Value("${spring.security.oauth2.client.registration.facebook.client-secret}")
    private String faceClientSecret;

    @Value("${spring.security.oauth2.client.registration.facebook.redirect-uri}")
    private String faceRedirectUri;

    @Value("${spring.security.oauth2.client.provider.facebook.user-info-uri}")
    private String faceUserInfoUri;

    @Value("${spring.security.oauth2.client.provider.facebook.token-uri}")
    private String faceTokenUri;

    @Override
    public String generateAuthor(String type) {
        if (type.equalsIgnoreCase("google")) {
            return "https://accounts.google.com/o/oauth2/v2/auth" +
                    "?client_id=" + googleClientId +
                    "&redirect_uri=" + googleRedirectUri +
                    "&response_type=code" +
                    "&scope=openid%20profile%20email" +
                    "&access_type=offline";
        }
        if (type.equalsIgnoreCase("facebook")) {
            return "https://www.facebook.com/v21.0/dialog/oauth" +
                    "?client_id=" + faceClientId +
                    "&redirect_uri=" + faceRedirectUri +
                    "&response_type=code" +
                    "&scope=email,public_profile";
        }

        throw new IllegalArgumentException("Unsupported type: " + type);
    }


    @Override
    public Map<String, Object> authenticateAndFetchProfile(String code, String loginType) throws Exception {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
        String accessToken;

        switch (loginType.toLowerCase()) {
            case "google":
                accessToken = new GoogleAuthorizationCodeTokenRequest(
                        new NetHttpTransport(), new GsonFactory(),
                        googleClientId,
                        googleClientSecret,
                        code,
                        googleRedirectUri
                ).execute().getAccessToken();

                restTemplate.getInterceptors().add((req, body, executionContext) -> {
                    req.getHeaders().set("Authorization", "Bearer " + accessToken);
                    return executionContext.execute(req, body);
                });

                return new ObjectMapper().readValue(
                        restTemplate.getForEntity(googleUserInfoUri, String.class).getBody(),
                        new TypeReference<>() {}
                );

            case "facebook":
                String urlGetAccessToken = UriComponentsBuilder
                        .fromUriString(faceTokenUri)
                        .queryParam("client_id", faceClientId)
                        .queryParam("redirect_uri", faceRedirectUri)
                        .queryParam("client_secret", faceClientSecret)
                        .queryParam("code", code)
                        .toUriString();

                ResponseEntity<String> responseEntity = restTemplate.getForEntity(urlGetAccessToken, String.class);
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonNode = objectMapper.readTree(responseEntity.getBody());
                accessToken = jsonNode.get("access_token").asText();

                String userInfoUri = faceUserInfoUri + "&access_token=" + accessToken;
                return objectMapper.readValue(
                        restTemplate.getForEntity(userInfoUri, String.class).getBody(),
                        new TypeReference<>() {}
                );

            default:
                System.out.println("Unsupported login type: " + loginType);
                return null;
        }
    }

}
