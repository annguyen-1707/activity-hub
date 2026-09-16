import { Injectable, signal } from '@angular/core';
import { Observable, of } from 'rxjs';
import { ActivityEventType, ActivityLog, ActivityTargetType } from '../models/activity-log.model';

const STORAGE_KEY = 'activity_hub_logs';

const INITIAL_LOGS: ActivityLog[] = [
  {
    id: 'log-101',
    eventId: 'EVT-20260916-001',
    username: 'admin',
    fullName: 'Quản Trị Viên',
    eventType: 'USER_LOGIN',
    targetType: 'AUTH',
    targetId: 'admin',
    description: 'Đăng nhập vào hệ thống thành công qua tài khoản quản trị.',
    ipAddress: '192.168.1.105',
    createdAt: '2026-09-16T08:30:15',
    details: { browser: 'Chrome 132', os: 'macOS 15.2' },
  },
  {
    id: 'log-102',
    eventId: 'EVT-20260916-002',
    username: 'admin',
    fullName: 'Quản Trị Viên',
    eventType: 'ORDER_CREATED',
    targetType: 'ORDER',
    targetId: 'ORD-9842',
    description: 'Tạo đơn hàng mới gồm 2 sản phẩm: iPhone 16 Pro Max, Tai nghe Sony WH-1000XM5.',
    ipAddress: '192.168.1.105',
    createdAt: '2026-09-16T09:12:40',
    details: { totalAmount: 42980000, paymentMethod: 'BANK', itemCount: 2 },
  },
  {
    id: 'log-103',
    eventId: 'EVT-20260916-003',
    username: 'admin',
    fullName: 'Quản Trị Viên',
    eventType: 'ROLE_CREATED',
    targetType: 'ROLE',
    targetId: 'SALE_MANAGER',
    description: 'Tạo vai trò mới: SALE_MANAGER (Trưởng phòng kinh doanh).',
    ipAddress: '192.168.1.105',
    createdAt: '2026-09-16T09:45:00',
    details: { roleName: 'SALE_MANAGER', description: 'Trưởng phòng kinh doanh' },
  },
  {
    id: 'log-104',
    eventId: 'EVT-20260916-004',
    username: 'admin',
    fullName: 'Quản Trị Viên',
    eventType: 'USER_CREATED',
    targetType: 'USER',
    targetId: 'testuser',
    description: 'Tạo tài khoản người dùng mới: testuser (Nguyễn Văn An).',
    ipAddress: '192.168.1.105',
    createdAt: '2026-09-16T10:05:22',
    details: { username: 'testuser', roles: ['USER'] },
  },
  {
    id: 'log-105',
    eventId: 'EVT-20260916-005',
    username: 'testuser',
    fullName: 'Nguyễn Văn An',
    eventType: 'USER_LOGIN',
    targetType: 'AUTH',
    targetId: 'testuser',
    description: 'Người dùng đăng nhập lần đầu vào hệ thống.',
    ipAddress: '192.168.1.120',
    createdAt: '2026-09-16T10:15:33',
    details: { browser: 'Safari 18', os: 'macOS 15.2' },
  },
  {
    id: 'log-106',
    eventId: 'EVT-20260916-006',
    username: 'testuser',
    fullName: 'Nguyễn Văn An',
    eventType: 'ORDER_CREATED',
    targetType: 'ORDER',
    targetId: 'ORD-9843',
    description: 'Khách hàng đặt mua Chuột Logitech MX Master 3S và Bàn phím cơ Keychron Q1 Pro.',
    ipAddress: '192.168.1.120',
    createdAt: '2026-09-16T10:30:10',
    details: { totalAmount: 6780000, paymentMethod: 'CASH', itemCount: 2 },
  },
];

@Injectable({
  providedIn: 'root',
})
export class ActivityLogService {
  private readonly logsSignal = signal<ActivityLog[]>(this.loadLogsFromStorage());

  private loadLogsFromStorage(): ActivityLog[] {
    try {
      const stored = localStorage.getItem(STORAGE_KEY);
      if (stored) {
        return JSON.parse(stored);
      }
    } catch {
      // Ignore parsing errors
    }
    localStorage.setItem(STORAGE_KEY, JSON.stringify(INITIAL_LOGS));
    return [...INITIAL_LOGS];
  }

  private saveLogsToStorage(logs: ActivityLog[]): void {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(logs));
    this.logsSignal.set(logs);
  }

  getLogs(): Observable<ActivityLog[]> {
    return of(this.logsSignal());
  }

  recordLog(params: {
    username: string;
    fullName?: string;
    eventType: ActivityEventType;
    targetType: ActivityTargetType;
    targetId?: string | number;
    description: string;
    details?: Record<string, any>;
  }): void {
    const current = this.logsSignal();
    const newLog: ActivityLog = {
      id: 'log-' + Date.now(),
      eventId: 'EVT-' + new Date().toISOString().slice(0, 10).replace(/-/g, '') + '-' + Math.floor(100 + Math.random() * 900),
      username: params.username || 'unknown',
      fullName: params.fullName || params.username || 'Người dùng',
      eventType: params.eventType,
      targetType: params.targetType,
      targetId: params.targetId,
      description: params.description,
      ipAddress: '127.0.0.1',
      createdAt: new Date().toISOString(),
      details: params.details,
    };

    const updated = [newLog, ...current];
    this.saveLogsToStorage(updated);
  }

  clearLogs(): void {
    this.saveLogsToStorage([...INITIAL_LOGS]);
  }
}

