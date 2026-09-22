export interface LoginRequest {
  username: string;
  password: string;
  rememberMe?: boolean;
}

export interface LoginResponse {
  token?: string;
  accessToken?: string;
  refreshToken?: string;
  authenticated?: boolean;
  rememberMe?: boolean;
}

export interface RefreshRequest {
  refreshToken?: string;
  token?: string;
}

export type AuthenticationResponse = LoginResponse;
