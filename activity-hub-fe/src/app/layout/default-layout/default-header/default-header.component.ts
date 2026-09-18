import { NgTemplateOutlet } from '@angular/common';
import { Component, computed, inject, input } from '@angular/core';
import { Router, RouterLink } from '@angular/router';

import {
  AvatarComponent,
  BadgeComponent,
  BreadcrumbRouterComponent,
  ColorModeService,
  ContainerComponent,
  DropdownComponent,
  DropdownDividerDirective,
  DropdownItemDirective,
  DropdownMenuDirective,
  DropdownToggleDirective,
  HeaderComponent,
  HeaderNavComponent,
  HeaderTogglerDirective,
  SidebarToggleDirective,
} from '@coreui/angular';

import { IconDirective } from '@coreui/icons-angular';
import { AuthService } from '../../../core/services/auth.service';
import { OrderService } from '../../../views/orders/services/order.service';

@Component({
  selector: 'app-default-header',
  templateUrl: './default-header.component.html',
  standalone: true,
  imports: [
    AvatarComponent,
    BadgeComponent,
    BreadcrumbRouterComponent,
    ContainerComponent,
    DropdownComponent,
    DropdownDividerDirective,
    DropdownItemDirective,
    DropdownMenuDirective,
    DropdownToggleDirective,
    HeaderNavComponent,
    HeaderTogglerDirective,
    IconDirective,
    NgTemplateOutlet,
    RouterLink,
    SidebarToggleDirective,
  ],
})
export class DefaultHeaderComponent extends HeaderComponent {
  private readonly authService = inject(AuthService);
  private readonly orderService = inject(OrderService);
  private readonly router = inject(Router);

  readonly cartCount = this.orderService.totalCartQuantity;
  readonly cartAmount = this.orderService.totalCartAmount;
  readonly cartItems = this.orderService.cart;

  readonly #colorModeService = inject(ColorModeService);
  readonly colorMode = this.#colorModeService.colorMode;

  readonly currentUser = this.authService.getCurrentUser();

  readonly colorModes = [
    { name: 'light', text: 'Sáng', icon: 'cilSun' },
    { name: 'dark', text: 'Tối', icon: 'cilMoon' },
    { name: 'auto', text: 'Hệ thống', icon: 'cilContrast' },
  ];

  readonly icons = computed(() => {
    const currentMode = this.colorMode();
    return this.colorModes.find((mode) => mode.name === currentMode)?.icon ?? 'cilSun';
  });

  readonly sidebarId = input('sidebar1');

  constructor() {
    super();
  }

  getUserDisplayName(): string {
    const user = this.currentUser();
    if (user?.lastName && user?.firstName) {
      return `${user.lastName} ${user.firstName}`;
    }
    return user?.username || 'Tài khoản';
  }

  getUserRole(): string {
    const user = this.currentUser();
    if (user?.roles && user.roles.length > 0) {
      return user.roles[0].name;
    }
    return 'USER';
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/authentication/login']);
  }

  formatCurrency(value: number): string {
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value || 0);
  }
}
