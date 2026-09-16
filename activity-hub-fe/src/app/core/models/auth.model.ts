export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  token?: string;
  accessToken?: string;
  refreshToken?: string;
  authenticated?: boolean;
}

export interface RefreshRequest {
  refreshToken?: string;
  token?: string;
}

export type AuthenticationResponse = LoginResponse;
