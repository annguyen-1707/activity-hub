import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../../environments/environment';
import { ApiResponse, PageResponse } from '../../../core/models/api-response.model';
import { ActivityEventType, ActivityLog, ActivityTargetType } from '../../../core/models/activity-log.model';

@Injectable({
  providedIn: 'root',
})
export class ActivityLogService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = environment.apiUrl;

  getLogs(
    page: number = 0,
    size: number = 10,
    keyword: string = '',
    eventType: ActivityEventType | 'ALL' = 'ALL',
    targetType: ActivityTargetType | 'ALL' = 'ALL'
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

    return this.http
      .get<ApiResponse<PageResponse<ActivityLog>>>(`${this.baseUrl}/activity-logs`, { params })
      .pipe(map((res) => res.result));
  }
}
