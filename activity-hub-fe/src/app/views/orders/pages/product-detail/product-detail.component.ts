import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import {
  ButtonDirective,
  CardBodyComponent,
  CardComponent,
  ColComponent,
  RowComponent,
  SpinnerComponent,
} from '@coreui/angular';
import { IconDirective } from '@coreui/icons-angular';
import { ProductService } from '../../../../core/services/product.service';
import { OrderService } from '../../services/order.service';
import { ProductResponse } from '../../../../core/models/product.model';
import { Product } from '../../../../core/models/order.model';
import { ProductReviewListComponent } from '../../../../shared/components/product-review-list/product-review-list.component';

@Component({
  selector: 'app-sales-product-detail',
  standalone: true,
  templateUrl: './product-detail.component.html',
  styleUrls: ['./product-detail.component.scss'],
  imports: [
    CommonModule,
    RouterLink,
    RowComponent,
    ColComponent,
    CardComponent,
    CardBodyComponent,
    ButtonDirective,
    SpinnerComponent,
    IconDirective,
    ProductReviewListComponent,
  ],
})
export class ProductDetailComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly productService = inject(ProductService);
  private readonly orderService = inject(OrderService);

  readonly stars = [1, 2, 3, 4, 5];

  productId = signal<string>('');
  product = signal<ProductResponse | null>(null);
  loading = signal<boolean>(true);
  errorMessage = signal<string>('');

  // Cart Signals from OrderService
  readonly cart = this.orderService.cart;
  readonly totalCartQuantity = this.orderService.totalCartQuantity;
  readonly totalCartAmount = this.orderService.totalCartAmount;

  readonly inCartQuantity = computed(() => {
    const id = this.productId();
    const item = this.cart().find((i) => i.product.id === id);
    return item ? item.quantity : 0;
  });

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.productId.set(id);
      this.loadProduct(id);
    } else {
      this.errorMessage.set('Không tìm thấy mã sản phẩm.');
      this.loading.set(false);
    }
  }

  loadProduct(id: string): void {
    this.loading.set(true);
    this.errorMessage.set('');
    this.productService.getProductById(id).subscribe({
      next: (res) => {
        this.product.set(res);
        this.loading.set(false);
      },
      error: () => {
        this.errorMessage.set('Không thể tải thông tin sản phẩm hoặc sản phẩm không tồn tại.');
        this.loading.set(false);
      },
    });
  }

  toCartProduct(p: ProductResponse): Product {
    return {
      id: p.id,
      name: p.name,
      price: p.price,
      category: p.category?.name || '',
      image: p.image || '',
      description: p.description || '',
      stock: p.quantity,
      rate: p.rate,
      totalReviews: p.totalReviews,
    };
  }

  addToCart(): void {
    const p = this.product();
    if (!p || p.quantity <= 0) return;
    const cartProduct = this.toCartProduct(p);

    const currentCart = [...this.cart()];
    const existingIndex = currentCart.findIndex((i) => i.product.id === cartProduct.id);

    if (existingIndex > -1) {
      if (currentCart[existingIndex].quantity < cartProduct.stock) {
        currentCart[existingIndex].quantity += 1;
        currentCart[existingIndex].subtotal = currentCart[existingIndex].quantity * cartProduct.price;
      }
    } else {
      currentCart.push({
        product: cartProduct,
        quantity: 1,
        subtotal: cartProduct.price,
      });
    }

    this.orderService.saveCart(currentCart);
  }

  updateQuantity(delta: number): void {
    const p = this.product();
    if (!p) return;
    const cartProduct = this.toCartProduct(p);

    const currentCart = [...this.cart()];
    const existingIndex = currentCart.findIndex((i) => i.product.id === cartProduct.id);

    if (existingIndex > -1) {
      const newQuantity = currentCart[existingIndex].quantity + delta;

      if (newQuantity <= 0) {
        currentCart.splice(existingIndex, 1);
      } else if (newQuantity <= cartProduct.stock) {
        currentCart[existingIndex].quantity = newQuantity;
        currentCart[existingIndex].subtotal = newQuantity * cartProduct.price;
      }

      this.orderService.saveCart(currentCart);
    }
  }

  formatCurrency(value?: number): string {
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value || 0);
  }
}
