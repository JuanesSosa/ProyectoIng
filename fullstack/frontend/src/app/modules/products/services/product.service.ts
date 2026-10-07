import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Product } from '../models/product.model';
import { environment } from '../../../../environments/environment';

/**
 * Servicio que se encarga de todas las peticiones HTTP al backend de productos.
 * Centraliza la URL base y los métodos GET, POST, PUT, DELETE.
 */
@Injectable({
  providedIn: 'root'  // El servicio está disponible en toda la app (inyección en cualquier componente); "root" = singleton a nivel de aplicación.
})
export class ProductService {

  /** URL base del API de productos en el backend (Spring Boot). Se configura en src/environments. */
  private readonly apiUrl = `${environment.apiUrl}/products`;

  constructor(private http: HttpClient) {}

  /** Obtiene la lista de todos los productos (GET /api/products). */
  getAll(): Observable<Product[]> {
    return this.http.get<Product[]>(this.apiUrl);
  }

  /** Crea un nuevo producto (POST /api/products). El backend devuelve el producto con su id. */
  create(product: Product): Observable<Product> {
    return this.http.post<Product>(this.apiUrl, product);
  }

  /** Actualiza un producto existente (PUT /api/products/{id}). */
  update(id: number, product: Product): Observable<Product> {
    return this.http.put<Product>(`${this.apiUrl}/${id}`, product);
  }

  /** Elimina un producto (DELETE /api/products/{id}). */
  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
