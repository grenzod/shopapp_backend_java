package com.project.shopapp.services;

import io.jsonwebtoken.io.IOException;

import java.util.Map;

public interface IAuthorService {
    String generateAuthor(String type);
    Map<String, Object> authenticateAndFetchProfile(String code, String loginType) throws Exception;
}
