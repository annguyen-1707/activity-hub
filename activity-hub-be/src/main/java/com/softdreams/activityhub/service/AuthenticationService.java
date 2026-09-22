package com.softdreams.activityhub.service;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.StringJoiner;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.softdreams.activityhub.dto.request.AuthenticationRequest;
import com.softdreams.activityhub.dto.request.IntrospectRequest;
import com.softdreams.activityhub.dto.response.AuthenticationResponse;
import com.softdreams.activityhub.dto.response.IntrospectResponse;
import com.softdreams.activityhub.entity.InvalidatedToken;
import com.softdreams.activityhub.entity.User;
import com.softdreams.activityhub.exception.AppException;
import com.softdreams.activityhub.exception.ErrorCode;
import com.softdreams.activityhub.repository.InvalidatedTokenRepository;
import com.softdreams.activityhub.repository.UserRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationService {
    UserRepository userRepository;
    InvalidatedTokenRepository invalidatedTokenRepository;

    @NonFinal
    @Value("${jwt.signerKey}")
    protected String SIGNER_KEY;

    @NonFinal
    @Value("${jwt.valid-duration}")
    protected long VALID_DURATION;

    @NonFinal
    @Value("${jwt.refreshable-duration}")
    protected long REFRESHABLE_DURATION;

    @NonFinal
    @Value("${jwt.refreshable-duration-long}")
    protected long REFRESHABLE_DURATION_LONG;

    public long getRefreshableDuration(Boolean rememberMe) {
        if (rememberMe == null || !rememberMe)
            return REFRESHABLE_DURATION;
        return REFRESHABLE_DURATION_LONG;
    }

    public IntrospectResponse introspect(IntrospectRequest request) throws JOSEException, ParseException {
        var token = request.getToken();
        boolean isValid = true;

        try {
            verifyToken(token, false);
        } catch (AppException e) {
            isValid = false;
        }

        return IntrospectResponse.builder().valid(isValid).build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        var user = userRepository
                .findByUsername(request.getUsername())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        boolean authenticated = passwordEncoder.matches(request.getPassword(), user.getPassword());

        if (!authenticated) throw new AppException(ErrorCode.UNAUTHENTICATED);

        boolean rememberMe = Boolean.TRUE.equals(request.getRememberMe());
        var accessToken = generateAccessToken(user);
        var refreshToken = generateRefreshToken(user);
        var refreshToken = generateRefreshToken(user, rememberMe);

        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .authenticated(true)
                .rememberMe(rememberMe)
                .build();
    }

    public void logout(String accessToken, String refreshToken) {
        if (accessToken != null && !accessToken.isBlank()) {
            invalidateTokenString(accessToken, false);
        }
        if (refreshToken != null && !refreshToken.isBlank()) {
            invalidateTokenString(refreshToken, true);
        }
    }

    private void invalidateTokenString(String token, boolean isRefresh) {
        try {
            var signToken = verifyToken(token, isRefresh);
            String jit = signToken.getJWTClaimsSet().getJWTID();
            Date expiryTime = signToken.getJWTClaimsSet().getExpirationTime();

            InvalidatedToken invalidatedToken =
                    InvalidatedToken.builder().id(jit).expiryTime(expiryTime).build();

            invalidatedTokenRepository.save(invalidatedToken);
        } catch (Exception exception) {
            log.error(String.valueOf(exception));
        }
    }

    public AuthenticationResponse refreshToken(String token) throws ParseException, JOSEException {
        var signedJWT = verifyToken(token, true);

        var jit = signedJWT.getJWTClaimsSet().getJWTID();
        var expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        // Thu hồi refresh token cũ (Refresh Token Rotation)
        InvalidatedToken invalidatedToken =
                InvalidatedToken.builder().id(jit).expiryTime(expiryTime).build();

        invalidatedTokenRepository.save(invalidatedToken);

        var username = signedJWT.getJWTClaimsSet().getSubject();

        var user =
                userRepository.findByUsername(username).orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));

        Object rememberMeClaim = signedJWT.getJWTClaimsSet().getClaim("remember_me");
        boolean rememberMe = Boolean.TRUE.equals(rememberMeClaim);

        var newAccessToken = generateAccessToken(user);
        var newRefreshToken = generateRefreshToken(user);
        var newRefreshToken = generateRefreshToken(user, rememberMe);

        return AuthenticationResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .authenticated(true)
                .rememberMe(rememberMe)
                .build();
    }

    private String generateAccessToken(User user) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getUsername())
                .issuer("devteria.com")
                .issueTime(new Date())
                .expirationTime(new Date(
                        Instant.now().plus(VALID_DURATION, ChronoUnit.SECONDS).toEpochMilli()))
                .jwtID(UUID.randomUUID().toString())
                .claim("scope", buildScope(user))
                .claim("token_type", "ACCESS")
                .build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject());
        JWSObject jwsObject = new JWSObject(header, payload);

        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            log.error("Cannot create access token", e);
            throw new RuntimeException(e);
        }
    }

    private String generateRefreshToken(User user) {
    private String generateRefreshToken(User user, boolean rememberMe) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);
        long duration = rememberMe ? REFRESHABLE_DURATION_LONG : REFRESHABLE_DURATION;

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getUsername())
                .issuer("devteria.com")
                .issueTime(new Date())
                .expirationTime(new Date(Instant.now()
                        .plus(REFRESHABLE_DURATION, ChronoUnit.SECONDS)
                        .plus(duration, ChronoUnit.SECONDS)
                        .toEpochMilli()))
                .jwtID(UUID.randomUUID().toString())
                .claim("token_type", "REFRESH")
                .claim("remember_me", rememberMe)
                .build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject());
        JWSObject jwsObject = new JWSObject(header, payload);

        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            log.error("Cannot create refresh token", e);
            throw new RuntimeException(e);
        }
    }

    private SignedJWT verifyToken(String token, boolean isRefresh) throws JOSEException, ParseException {
        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());

        SignedJWT signedJWT = SignedJWT.parse(token);

        Date tokenExpiry = signedJWT.getJWTClaimsSet().getExpirationTime();
        Date expiryTime = (isRefresh)
                ? (tokenExpiry != null
                && tokenExpiry
                .toInstant()
                .isAfter(signedJWT
                        .getJWTClaimsSet()
                        .getIssueTime()
                        .toInstant()
                        .plus(VALID_DURATION + 60, ChronoUnit.SECONDS))
                   ? tokenExpiry
                   : new Date(signedJWT
                .getJWTClaimsSet()
                .getIssueTime()
                .toInstant()
                .plus(REFRESHABLE_DURATION, ChronoUnit.SECONDS)
                .plus(
                    Boolean.TRUE.equals(signedJWT.getJWTClaimsSet().getClaim("remember_me"))
                        ? REFRESHABLE_DURATION_LONG
                        : REFRESHABLE_DURATION,
                    ChronoUnit.SECONDS)
                .toEpochMilli()))
                : tokenExpiry;

        var verified = signedJWT.verify(verifier);

        if (!(verified && expiryTime != null && expiryTime.after(new Date()))) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        if (invalidatedTokenRepository.existsById(signedJWT.getJWTClaimsSet().getJWTID())) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        return signedJWT;
    }

    private String buildScope(User user) {
        StringJoiner stringJoiner = new StringJoiner(" ");

        if (!CollectionUtils.isEmpty(user.getRoles()))
            user.getRoles().forEach(role -> {
                stringJoiner.add("ROLE_" + role.getName());
            });
        return stringJoiner.toString();
    }
}
