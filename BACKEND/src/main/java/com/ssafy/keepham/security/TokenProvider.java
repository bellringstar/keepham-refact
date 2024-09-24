package com.ssafy.keepham.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.keepham.common.error.TokenErrorCode;
import com.ssafy.keepham.common.exception.ApiException;
import com.ssafy.keepham.domain.user.entity.UserRefreshToken;
import com.ssafy.keepham.domain.user.repository.UserRefreshTokenRepository;
import com.ssafy.keepham.domain.user.repository.UserRepository;
import com.ssafy.keepham.domain.user.service.UserService;
import io.jsonwebtoken.*;

import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;
import javax.crypto.spec.SecretKeySpec;

import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/*
TODO: 회원의 리프레시 토큰을 관리할 엔티티
       리프레시 토큰을 생성하는 메소드
       로그인 API 응답에 리프레시 토큰 추가
       리프레시 토큰을 통해 액세스 토큰을 갱신
       리프레시 토큰 검증 및 새로운 액세스 토큰 발급

 */
@Service
@Getter
@Slf4j
@PropertySource("classpath:security.yaml")
public class TokenProvider {

        private final UserRefreshTokenRepository userRefreshTokenRepository;
        private final String secretKey;
        private final long expirationMinutes;
        private final long refreshExpirationHours;
        private final String issuer;
        private final long reissueLimit;
        private final ObjectMapper objectMapper = new ObjectMapper();
        private final UserRepository userRepository;
        private final RedisTemplate<String, String> redisTemplate;

        public TokenProvider(
                UserRefreshTokenRepository userRefreshTokenRepository,
                @Value("${secret-key}") String secretKey,
                @Value("${expiration-minutes}") long expirationMinutes,
                @Value("${refresh-expiration-hours}") long refreshExpirationHours,
                @Value("${issuer}") String issuer,
                UserRepository userRepository,
                RedisTemplate<String, String> redisTemplate){
                this.userRefreshTokenRepository = userRefreshTokenRepository;
                this.secretKey = secretKey;
                this.expirationMinutes = expirationMinutes;
                this.refreshExpirationHours = refreshExpirationHours;
                this.issuer = issuer;
                this.redisTemplate = redisTemplate;
                this.reissueLimit = refreshExpirationHours * 60 / expirationMinutes;
                this.userRepository = userRepository;
        }
        public String createAccessToken(String userSpecification){
                return Jwts.builder()
                    .signWith(new SecretKeySpec(secretKey.getBytes(), SignatureAlgorithm.HS512.getJcaName()))
                    .setSubject(userSpecification)
                    .setIssuer(issuer)
                    .setIssuedAt(Timestamp.valueOf(LocalDateTime.now()))
                    .setExpiration(Date.from(Instant.now().plus(expirationMinutes, ChronoUnit.HOURS)))
                    .compact();
        }
        public String validateTokenAndGetSubject(String token){
                return validateAndParserToken(token)
                        .getBody()
                        .getSubject();
        }
        @Transactional
        public String recreateAccessToken(String oldAccessToken) throws JsonProcessingException {
                String subject = decodeJwtPayloadSubject(oldAccessToken);
                Optional<UserRefreshTokenRepository> userRefreshTokenOptional = userRefreshTokenRepository.findByUserIdAndReissueCountLessThan(UUID.fromString(subject.split(":")[0]), reissueLimit);

                if (userRefreshTokenOptional.isPresent()) {
                        UserRefreshToken userRefreshToken = (UserRefreshToken) userRefreshTokenOptional.get();
                        userRefreshToken.increaseReissueCount();
                } else {
                        throw new ExpiredJwtException(null, null, "Refresh Token expire");
                }
                return createAccessToken(subject);
        }
        public String createRefreshToken(){
                Date now = new Date();
                long refreshExpirationMillis = refreshExpirationHours * 60 * 60 * 1000;
                Date expireDate = new Date(now.getTime() + refreshExpirationHours);

                String refreshToken = Jwts.builder()
                        .signWith(new SecretKeySpec(secretKey.getBytes(),SignatureAlgorithm.HS512.getJcaName()))
                        .setIssuer(issuer)
                        .setIssuedAt(Timestamp.valueOf(LocalDateTime.now()))
                        .setExpiration(Date.from(Instant.now().plus(refreshExpirationHours, ChronoUnit.HOURS)))
                        .compact();
                //TODO: 키값 조회 값 수정
                redisTemplate.opsForValue().set(String.valueOf(Jwts.builder().setIssuer(issuer)),refreshToken,refreshExpirationMillis, TimeUnit.MILLISECONDS);
                return refreshToken;
        }
        @Transactional
        public void validateRefreshToken(String refreshToken, String oldAccessToken) throws  JsonProcessingException{
                validateAndParserToken(refreshToken);
                String userId = decodeJwtPayloadSubject(oldAccessToken).split(":")[0];
                userRefreshTokenRepository.findByUserIdAndReissueCountLessThan(UUID.fromString(userId),reissueLimit)
                        .orElseThrow(() -> new ExpiredJwtException(null,null,"Refresh Token expire"));
        }
        private Jws<Claims> validateAndParserToken(String auth){
                var key = Keys.hmacShaKeyFor(secretKey.getBytes());
                var parser = Jwts.parserBuilder()
                        .setSigningKey(secretKey.getBytes())
                        .build();

                try {
                        var result = parser.parseClaimsJws(auth);
                        return result;
                } catch (Exception e){
                        if(e instanceof SignatureException) {
                                throw new ApiException(TokenErrorCode.INVALID_TOKEN);
                        } else if (e instanceof ExpiredJwtException) {
                                throw new ApiException(TokenErrorCode.EXPIRED_TOKEN);
                        } else {
                                throw new ApiException(TokenErrorCode.TOKEN_EXCEPTION);
                        }
                }
        }
        private String decodeJwtPayloadSubject(String oldAccessToken) throws JsonProcessingException {
                return objectMapper.readValue(
                        new String(Base64.getDecoder().decode(oldAccessToken.split("\\.")[1]), StandardCharsets.UTF_8),
                        Map.class
                ).get("sub").toString();
        }
}
