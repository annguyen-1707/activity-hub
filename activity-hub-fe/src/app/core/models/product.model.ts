export enum CategoryEnum {
  PHONE = 'PHONE',
  LAPTOP = 'LAPTOP',
  AUDIO = 'AUDIO',
  ACCESSORY = 'ACCESSORY',
  MONITOR = 'MONITOR',
  WATCH = 'WATCH',
}

export const CATEGORY_LABELS: Record<CategoryEnum, string> = {
  [CategoryEnum.PHONE]: 'Điện thoại',
  [CategoryEnum.LAPTOP]: 'Laptop',
  [CategoryEnum.AUDIO]: 'Âm thanh',
  [CategoryEnum.ACCESSORY]: 'Phụ kiện',
  [CategoryEnum.MONITOR]: 'Màn hình',
  [CategoryEnum.WATCH]: 'Đồng hồ',
};

export interface ProductResponse {
  id: string;
  name: string;
  price: number;
  categoryId?: string;
  categoryCode?: string;
  categoryName?: string;
  categoryLabel?: string;
  category?: string;
  rate: number;
  quantity: number;
  description?: string;
  image?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface ProductRequest {
  name: string;
  price: number;
  categoryId: string;
  rate?: number;
  description?: string;
  image?: string;
}
