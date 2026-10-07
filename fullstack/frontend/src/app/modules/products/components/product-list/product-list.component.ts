import { Component, OnInit } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { Product } from '../../models/product.model';
import { ProductService } from '../../services/product.service';

/**
 * Componente CRUD de productos: lista, crea, edita y elimina.
 * Usa un formulario reactivo; el mismo formulario sirve para crear y para editar (según editingId).
 */
@Component({
  selector: 'app-product-list',
  standalone: true,
  imports: [ReactiveFormsModule, CurrencyPipe],
  templateUrl: './product-list.component.html',
  styleUrls: ['./product-list.component.css']
})
export class ProductListComponent implements OnInit {

  products: Product[] = [];

  /** Formulario reactivo con las mismas reglas que el backend. */
  form: FormGroup;

  /** id del producto que se está editando; null = modo creación. */
  editingId: number | null = null;

  isLoading = false;

  /** Error al cargar la lista. */
  loadError = '';

  /** Error al guardar o eliminar (separado del de carga para no mezclarlos). */
  actionError = '';

  constructor(private productService: ProductService, private fb: FormBuilder) {
    this.form = this.fb.group({
      name: ['', [Validators.required, Validators.maxLength(255)]],
      description: ['', [Validators.maxLength(255)]],
      price: [null as number | null, [Validators.required, Validators.min(0.01)]],
      stock: [null as number | null, [Validators.required, Validators.min(0), Validators.pattern(/^\d+$/)]]
    });
  }

  ngOnInit(): void {
    this.loadProducts();
  }

  loadProducts(): void {
    this.isLoading = true;
    this.loadError = '';

    this.productService.getAll().subscribe({
      next: (data) => {
        this.products = data;
        this.isLoading = false;
      },
      error: () => {
        this.loadError = 'No se pudieron cargar los productos. Revisa que el backend esté encendido.';
        this.isLoading = false;
      }
    });
  }

  /** true si el control fue tocado y es inválido (para mostrar su mensaje). */
  showError(field: string): boolean {
    const control = this.form.get(field);
    return !!control && control.invalid && (control.touched || control.dirty);
  }

  /** Crea o actualiza según el modo actual. */
  save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.actionError = '';

    const value = this.form.value;
    const payload: Product = {
      name: String(value.name).trim(),
      description: value.description ?? '',
      price: Number(value.price),
      stock: Number(value.stock)
    };

    const request$ = this.editingId === null
      ? this.productService.create(payload)
      : this.productService.update(this.editingId, payload);

    request$.subscribe({
      next: () => {
        this.resetForm();
        this.loadProducts();
      },
      error: (err: HttpErrorResponse) => {
        this.actionError = this.backendMessage(err, 'No se pudo guardar el producto.');
      }
    });
  }

  /** Carga un producto en el formulario para editarlo. */
  edit(product: Product): void {
    this.editingId = product.id ?? null;
    this.form.reset({
      name: product.name,
      description: product.description,
      price: product.price,
      stock: product.stock
    });
    this.actionError = '';
  }

  cancelEdit(): void {
    this.resetForm();
  }

  delete(product: Product): void {
    if (product.id === undefined) {
      return;
    }
    if (!confirm(`¿Eliminar "${product.name}"?`)) {
      return;
    }
    this.actionError = '';

    this.productService.delete(product.id).subscribe({
      next: () => {
        if (this.editingId === product.id) {
          this.resetForm();
        }
        this.loadProducts();
      },
      error: (err: HttpErrorResponse) => {
        this.actionError = this.backendMessage(err, 'No se pudo eliminar el producto.');
      }
    });
  }

  private resetForm(): void {
    this.form.reset({ name: '', description: '', price: null, stock: null });
    this.editingId = null;
  }

  /** Extrae el mensaje de error que devuelve el backend (400 con "errors" por campo, 404, etc.). */
  private backendMessage(err: HttpErrorResponse, fallback: string): string {
    const body = err.error;
    if (body?.errors) {
      return Object.values(body.errors).join(' ');
    }
    return body?.message ?? fallback;
  }
}
