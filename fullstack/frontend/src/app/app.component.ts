import { Component } from '@angular/core';
import { ProductListComponent } from './modules/products/components/product-list/product-list.component';

/**
 * Componente raíz de la aplicación.
 * Solo incluye el componente de lista de productos (app-product-list).
 */
@Component({
  selector: 'app-root',  // Etiqueta principal; en index.html se usa <app-root></app-root> para montar la app.
  standalone: true,
  imports: [ProductListComponent],  // Importa el componente de lista para poder usarlo en la plantilla.
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent {
}
