package com.softdreams.activityhub.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RefreshRequest {
    /**
     * Refresh Token dùng để cấp mới Access Token
     */
    String refreshToken;

    /**
     * Trường cũ (hỗ trợ tương thích ngược nếu client gửi "token")
     */
    String token;

    public String getRefreshToken() {
        if (refreshToken != null && !refreshToken.isBlank()) {
            return refreshToken;
        }
        return token;
    }
}
