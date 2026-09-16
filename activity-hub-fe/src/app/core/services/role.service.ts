import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../models/api-response.model';
import { RoleResponse } from '../models/user.model';

export interface RoleCreationRequest {
  name: string;
  description: string;
}

@Injectable({
  providedIn: 'root',
})
export class RoleService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = environment.apiUrl;

  getRoles(): Observable<RoleResponse[]> {
    return this.http
      .get<ApiResponse<RoleResponse[]>>(`${this.baseUrl}/roles`)
      .pipe(map((res) => res.result));
  }

  createRole(request: RoleCreationRequest): Observable<RoleResponse> {
    return this.http
      .post<ApiResponse<RoleResponse>>(`${this.baseUrl}/roles`, request)
      .pipe(map((res) => res.result));
  }

  deleteRole(roleName: string): Observable<void> {
    return this.http
      .delete<ApiResponse<void>>(`${this.baseUrl}/roles/${roleName}`)
      .pipe(map((res) => res.result));
  }
}

