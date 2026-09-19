export type StockTransactionType = 'IMPORT' | 'SALE' | 'CANCEL' | 'ADJUSTMENT';

export const STOCK_TRANSACTION_TYPE_LABELS: Record<StockTransactionType, string> = {
  IMPORT: 'Nhập hàng',
  SALE: 'Bán hàng',
  CANCEL: 'Hủy đơn',
  ADJUSTMENT: 'Điều chỉnh tồn kho',
};

export const STOCK_TRANSACTION_TYPE_COLORS: Record<StockTransactionType, string> = {
  IMPORT: 'success',
  SALE: 'warning',
  CANCEL: 'danger',
  ADJUSTMENT: 'info',
};

export interface StockTransactionLineResponse {
  id?: string;
  productId: string;
  productName?: string;
  quantity: number;
}

export interface StockTransactionResponse {
  id: string;
  type: StockTransactionType;
  typeLabel?: string;
  referenceId?: string;
  note?: string;
  createdByUsername?: string;
  createdAt: string;
  lines: StockTransactionLineResponse[];
}

export interface StockTransactionLineRequest {
  productId: string;
  quantity: number;
}

export interface StockTransactionRequest {
  type: 'IMPORT' | 'ADJUSTMENT';
  note?: string;
  lines: StockTransactionLineRequest[];
}

