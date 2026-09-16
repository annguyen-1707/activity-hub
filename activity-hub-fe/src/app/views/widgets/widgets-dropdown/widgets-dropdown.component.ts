import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import {
  CardBodyComponent,
  CardComponent,
  ColComponent,
  RowComponent,
} from '@coreui/angular';
import { IconDirective } from '@coreui/icons-angular';

@Component({
  selector: 'app-widgets-dropdown',
  templateUrl: './widgets-dropdown.component.html',
  standalone: true,
  imports: [
    RowComponent,
    ColComponent,
    CardComponent,
    CardBodyComponent,
    IconDirective,
    RouterLink,
  ],
})
export class WidgetsDropdownComponent {}

