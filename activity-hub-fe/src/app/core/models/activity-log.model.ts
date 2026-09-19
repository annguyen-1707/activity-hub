export type ActivityEventType =
  | 'CREATED'
  | 'UPDATED'
  | 'DELETED'
  | 'APPROVED'
  | 'REJECTED'
  | 'CANCEL'
  | 'DONE'
  | 'LOGIN'
  | 'LOGOUT';

export type ActivityTargetType = 'USER' | 'ORDER' | 'ROLE';

export interface ActivityLog {
  id: string;
  eventId: string;
  userId?: string;
  username?: string;
  fullName?: string;
  eventType: ActivityEventType;
  eventTypeLabel: string;
  targetType: ActivityTargetType;
  targetTypeLabel: string;
  targetId?: string;
  ipAddress?: string;
  createdAt: string;
}
