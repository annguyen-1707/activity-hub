package com.softdreams.activityhub.controller;

import java.text.ParseException;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nimbusds.jose.JOSEException;
import com.softdreams.activityhub.anotation.ActivityLog;
import com.softdreams.activityhub.dto.request.*;
import com.softdreams.activityhub.dto.response.AuthenticationResponse;
import com.softdreams.activityhub.dto.response.IntrospectResponse;
import com.softdreams.activityhub.dto.response.AuthenticationResponse;
import com.softdreams.activityhub.enums.EventType;
import com.softdreams.activityhub.enums.TargetType;
import com.softdreams.activityhub.exception.AppException;
import com.softdreams.activityhub.exception.ErrorCode;
import com.softdreams.activityhub.service.AuthenticationService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationController {
    AuthenticationService authenticationService;

    private static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";

    private ResponseCookie createRefreshTokenCookie(String refreshToken, long maxAge) {
        return ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, refreshToken != null ? refreshToken : "")
                .httpOnly(true)
                .secure(false) // Đặt true khi chạy HTTPS Production
                .path("/")
                .maxAge(maxAge)
                .sameSite("Lax")
                .build();
    }

    @PostMapping("/token")
    @ActivityLog(eventType = EventType.LOGIN, targetType = TargetType.USER, targetId = "#request.username")
    public ResponseEntity<ApiResponse<AuthenticationResponse>> authenticate(@RequestBody AuthenticationRequest request) {
        AuthenticationResponse authenticationResponse = authenticationService.authenticate(request);

        ResponseCookie cookie = createRefreshTokenCookie(
                authenticationResponse.getRefreshToken(),
                authenticationService.getRefreshableDuration()
        );

        AuthenticationResponse response = AuthenticationResponse.builder()
                .accessToken(authenticationResponse.getAccessToken())
                .refreshToken(authenticationResponse.getRefreshToken())
                .authenticated(true)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(ApiResponse.<AuthenticationResponse>builder().result(response).build());
    }

    @PostMapping("/introspect")
    public ApiResponse<IntrospectResponse> authenticate(@RequestBody IntrospectRequest request)
            throws ParseException, JOSEException {
        var result = authenticationService.introspect(request);
        return ApiResponse.<IntrospectResponse>builder().result(result).build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthenticationResponse>> refresh(
            @CookieValue(name = REFRESH_TOKEN_COOKIE_NAME, required = false) String refreshTokenFromCookie,
            @RequestBody(required = false) RefreshRequest request)
            throws ParseException, JOSEException {

        // Ưu tiên đọc từ HttpOnly Cookie, dự phòng đọc từ body
        String token = (refreshTokenFromCookie != null && !refreshTokenFromCookie.isBlank())
                ? refreshTokenFromCookie
                : (request != null ? request.getRefreshToken() : null);

        if (token == null || token.isBlank()) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        AuthenticationResponse authenticationResponse = authenticationService.refreshToken(token);

        // Xoay vòng refresh token: đặt cookie mới cho trình duyệt
        ResponseCookie newCookie = createRefreshTokenCookie(
                authenticationResponse.getRefreshToken(),
                authenticationService.getRefreshableDuration()
        );

        AuthenticationResponse response = AuthenticationResponse.builder()
                .accessToken(authenticationResponse.getAccessToken())
                .refreshToken(authenticationResponse.getRefreshToken())
                .authenticated(true)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, newCookie.toString())
                .body(ApiResponse.<AuthenticationResponse>builder().result(response).build());
    }

    @PostMapping("/logout")
    @ActivityLog(
            eventType = EventType.LOGOUT,
            targetType = TargetType.USER,
            targetId =
                    "#jwtSubject(#refreshTokenFromCookie != null ? #refreshTokenFromCookie : (#request != null ? #request.token : null))")
    public ResponseEntity<ApiResponse<Void>> logout(
            @CookieValue(name = REFRESH_TOKEN_COOKIE_NAME, required = false) String refreshTokenFromCookie,
            @RequestBody(required = false) LogoutRequest request) throws ParseException, JOSEException {

        String accessToken = (request != null) ? request.getToken() : null;
        authenticationService.logout(accessToken, refreshTokenFromCookie);

        // Xóa sạch cookie trên trình duyệt
        ResponseCookie clearCookie = createRefreshTokenCookie("", 0);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, clearCookie.toString())
                .body(ApiResponse.<Void>builder().build());
    }
}
