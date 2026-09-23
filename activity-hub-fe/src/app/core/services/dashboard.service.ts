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

  getOverview(days: number = 7, fromDate?: string, toDate?: string): Observable<DashboardOverviewResponse> {
    let params = new HttpParams().set('days', days.toString());
    if (fromDate && fromDate.trim()) {
      const formattedFrom = fromDate.length === 10 ? `${fromDate}T00:00:00` : fromDate;
      params = params.set('fromDate', formattedFrom);
    }
    if (toDate && toDate.trim()) {
      const formattedTo = toDate.length === 10 ? `${toDate}T23:59:59` : toDate;
      params = params.set('toDate', formattedTo);
    }
    return this.http
      .get<ApiResponse<DashboardOverviewResponse>>(`${this.baseUrl}/overview`, {
        params,
        withCredentials: true,
      })
      .pipe(map((res) => res.result));
  }
}

