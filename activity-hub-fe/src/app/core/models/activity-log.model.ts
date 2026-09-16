export type ActivityEventType =
  | 'USER_LOGIN'
  | 'USER_LOGOUT'
  | 'ORDER_CREATED'
  | 'ORDER_UPDATED'
  | 'ORDER_DELETED'
  | 'USER_CREATED'
  | 'USER_UPDATED'
  | 'USER_DELETED'
  | 'ROLE_CREATED'
  | 'ROLE_DELETED';

export type ActivityTargetType = 'ORDER' | 'USER' | 'ROLE' | 'AUTH';

export interface ActivityLog {
  id: string;
  eventId: string;
  username: string;
  fullName?: string;
  eventType: ActivityEventType;
  targetType: ActivityTargetType;
  targetId?: string | number;
  description: string;
  ipAddress?: string;
  createdAt: string;
  details?: Record<string, any>;
}

