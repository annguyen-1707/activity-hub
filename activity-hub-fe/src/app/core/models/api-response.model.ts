export interface ApiResponse<T> {
  code?: number;
  message?: string;
  result: T;
}

export type APIResponse<T> = ApiResponse<T>;

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}
