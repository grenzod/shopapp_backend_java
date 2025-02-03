package com.project.shopapp.services.impl;

import com.project.shopapp.models.Token;
import com.project.shopapp.models.User;
import com.project.shopapp.repositories.TokenRepository;
import com.project.shopapp.services.ITokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class TokenService implements ITokenService {
    private final TokenRepository tokenRepository;
    private final int MaxToken = 3;
    @Value("${jwt.expiration}")
    private int expiration;

    @Override
    public void addToken(User user, String token, boolean isMobile){
        List<Token> userTokens = tokenRepository.findByUser(user);
        int countToken = userTokens.size();

        if(countToken > MaxToken){
            boolean hasNonMobileToken = !userTokens.stream().allMatch(Token::isMobile);
            Token tokenToDelete;
            if(hasNonMobileToken) {
                tokenToDelete = userTokens.stream()
                        .filter(userToken -> !userToken.isMobile())
                        .findFirst()
                        .orElse(userTokens.get(0));
            }
            else {
                tokenToDelete = userTokens.get(0);
            }
            tokenRepository.delete(tokenToDelete);
        }

        long expirationInSecond = expiration;
        LocalDateTime expirationDateTime = LocalDateTime.now().plusSeconds(expirationInSecond);
        Token newToken = Token.builder()
                .user(user)
                .token(token)
                .revoked(false)
                .expired(false)
                .tokenType("Bearer")
                .expirationDate(expirationDateTime)
                .isMobile(isMobile)
                .build();
        tokenRepository.save(newToken);
    }

    @Override
    public Token deleteToken(String token) {
        return tokenRepository.deleteByToken(token);
    }
}
