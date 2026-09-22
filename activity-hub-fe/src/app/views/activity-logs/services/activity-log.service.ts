import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../../environments/environment';
import { ApiResponse, PageResponse } from '../../../core/models/api-response.model';
import { ActivityEventType, ActivityLog, ActivityTargetType, TargetTypeItem } from '../../../core/models/activity-log.model';

@Injectable({
  providedIn: 'root',
})
export class ActivityLogService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = environment.apiUrl;

  getTargetTypes(): Observable<TargetTypeItem[]> {
    return this.http
      .get<ApiResponse<TargetTypeItem[]>>(`${this.baseUrl}/activity-logs/targets`)
      .pipe(map((res) => res.result));
  }

  getLogs(
    page: number = 0,
    size: number = 10,
    keyword: string = '',
    eventType: ActivityEventType | 'ALL' = 'ALL',
    targetType: ActivityTargetType | 'ALL' = 'ALL',
    fromDate?: string,
    toDate?: string
  ): Observable<PageResponse<ActivityLog>> {
    let params = new HttpParams().set('page', page.toString()).set('size', size.toString());

    if (keyword && keyword.trim()) {
      params = params.set('keyword', keyword.trim());
    }
    if (eventType && eventType !== 'ALL') {
      params = params.set('eventType', eventType);
    }
    if (targetType && targetType !== 'ALL') {
      params = params.set('targetType', targetType);
    }
    if (fromDate && fromDate.trim()) {
      const formattedFrom = fromDate.length === 10 ? `${fromDate}T00:00:00` : fromDate;
      params = params.set('fromDate', formattedFrom);
    }
    if (toDate && toDate.trim()) {
      const formattedTo = toDate.length === 10 ? `${toDate}T23:59:59` : toDate;
      params = params.set('toDate', formattedTo);
    }

    return this.http
      .get<ApiResponse<PageResponse<ActivityLog>>>(`${this.baseUrl}/activity-logs`, { params })
      .pipe(map((res) => res.result));
  }

  exportPdf(
    keyword: string = '',
    eventType: ActivityEventType | 'ALL' = 'ALL',
    targetType: ActivityTargetType | 'ALL' = 'ALL',
    fromDate?: string,
    toDate?: string
  ): Observable<Blob> {
    let params = new HttpParams();

    if (keyword && keyword.trim()) {
      params = params.set('keyword', keyword.trim());
    }
    if (eventType && eventType !== 'ALL') {
      params = params.set('eventType', eventType);
    }
    if (targetType && targetType !== 'ALL') {
      params = params.set('targetType', targetType);
    }
    if (fromDate && fromDate.trim()) {
      const formattedFrom = fromDate.length === 10 ? `${fromDate}T00:00:00` : fromDate;
      params = params.set('fromDate', formattedFrom);
    }
    if (toDate && toDate.trim()) {
      const formattedTo = toDate.length === 10 ? `${toDate}T23:59:59` : toDate;
      params = params.set('toDate', formattedTo);
    }

    return this.http.get(`${this.baseUrl}/activity-logs/export/pdf`, {
      params,
      responseType: 'blob',
    });
  }
}
