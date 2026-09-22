import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../models/api-response.model';
import { DashboardOverviewResponse } from '../models/dashboard.model';

@Injectable({
  providedIn: 'root',
})
export class DashboardService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/dashboard`;

  getOverview(days: number = 7): Observable<DashboardOverviewResponse> {
    const params = new HttpParams().set('days', days.toString());
    return this.http
      .get<ApiResponse<DashboardOverviewResponse>>(`${this.baseUrl}/overview`, {
        params,
        withCredentials: true,
      })
      .pipe(map((res) => res.result));
  }
}

