export interface Role {
  name: string;
  description: string;
  permissions?: any[];
}

export type RoleResponse = Role;

export interface User {
  id: string;
  username: string;
  firstName: string;
  lastName: string;
  dob: string;
  roles: Role[];
}

export type UserResponse = User;

export interface UserCreationRequest {
  username: string;
  password?: string;
  firstName: string;
  lastName: string;
  dob?: string;
}

export interface UserUpdateRequest {
  password?: string;
  firstName: string;
  lastName: string;
  dob?: string;
  roles?: string[];
}
