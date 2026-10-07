## Tutorial paso a paso – Frontend Angular para el mini ecommerce

> **Nota (actualización posterior):** este tutorial describe la primera versión del frontend (crear y listar con `ngModel`). El código actual del proyecto ya incluye editar/eliminar, formulario reactivo (`ReactiveFormsModule`), plantillas con `@if`/`@for` y la URL de la API en `src/environments/`. No se usa `app.module.ts`: la app es standalone (`bootstrapApplication` + `app.config.ts`). Revisa `GUIA_COMPLETA.md`, sección "Cambios posteriores", y el código fuente como referencia vigente.

Este tutorial está pensado para **alguien que jamás ha programado** y que va a usar **Visual Studio Code**.  
La idea es que, leyendo solo este archivo, puedas crear **todo el frontend** que habla con el backend que ya tienes.

Al final tendrás:

- Una aplicación Angular que se conecta a `http://localhost:8080/api/products`.
- Una pantalla donde:
  - Puedes **ver** la lista de productos.
  - Puedes **crear** nuevos productos.

---

## 1. Requisitos previos

Antes de empezar necesitas:

- **Node.js** (versión 18 o superior).
  - Para comprobar si lo tienes, abre una terminal y escribe:

    ```bash
    node -v
    ```

    Si ves algo como `v18.x.x` o superior, perfecto.

- **npm** (gestor de paquetes de Node).

  ```bash
  npm -v
  ```

- **Angular CLI** (herramienta para crear proyectos Angular).

  Si no la tienes, instálala con:

  ```bash
  npm install -g @angular/cli
  ```

- **Visual Studio Code** (VS Code).
- Tu **backend** Spring Boot funcionando en `http://localhost:8080`  
  (puedes seguir el `TUTORIAL_BACKEND.md` para eso).

---

## 2. Crear el proyecto Angular con Angular CLI

1. Abre **VS Code** en la carpeta principal del proyecto fullstack  
   (donde tienes las carpetas `backend/` y `frontend/`).
2. Abre la **Terminal integrada**:
   - Menú **View → Terminal** (o atajo `Ctrl + ñ` / `Ctrl + '` según teclado).
3. Asegúrate de estar en la carpeta raíz del proyecto (donde ves `backend` y `frontend`):

   ```bash
   cd RUTA/HASTA/tu/fullstack
   ```

4. Ejecuta este comando para crear la app Angular (si ya existe la carpeta `frontend`, Angular la llenará con los archivos del proyecto):

   ```bash
   ng new frontend
   ```

5. El asistente de Angular CLI te hará algunas preguntas:
   - **? Which stylesheet format would you like to use?**
     - Puedes elegir **CSS** (lo más sencillo).
   - Si pregunta por routing, puedes responder **Yes** (sí) o **No**.  
     Para este tutorial, da igual, pero puedes responder **Yes**.

6. Angular creará una carpeta `frontend/` con muchos archivos:

   - `package.json`
   - `angular.json`
   - `src/` (aquí vive el código)
   - etc.

> A partir de aquí, **todo lo que hagamos del frontend será dentro de la carpeta `frontend/`.**

---

## 3. Ejecutar la app Angular por primera vez

Vamos a comprobar que todo funciona antes de escribir código.

1. En la terminal de VS Code, entra en la carpeta `frontend`:

   ```bash
   cd frontend
   ```

2. Ejecuta:

   ```bash
   npm install
   ```

   Esto descargará todas las dependencias (solo la primera vez puede tardar).

3. Cuando termine, arranca el servidor de desarrollo:

   ```bash
   ng serve
   ```

4. Espera a que aparezca un mensaje parecido a:

   ```
   ✔ Compiled successfully
   ✔ Local:   http://localhost:4200/
   ```

5. Abre tu navegador y entra a `http://localhost:4200/`.  
   Verás la página inicial de Angular.

Déjalo corriendo mientras desarrollas. Cada vez que cambies código, Angular recargará la página automáticamente.

---

## 4. Organizar la estructura de carpetas (core, shared, modules/products)

Vamos a organizar nuestro código para que se vea parecido al diagrama del profesor.

En **VS Code**, dentro de `frontend/src/app`:

1. Haz clic derecho sobre la carpeta `app` → **New Folder**:
   - Crea la carpeta `core`.
2. Otra vez clic derecho en `app` → **New Folder**:
   - Crea la carpeta `shared`.
3. Clic derecho en `app` → **New Folder**:
   - Crea la carpeta `modules`.
4. Dentro de `modules`, crea una carpeta `users`:
   - Clic derecho en `modules` → **New Folder** → `users`.
5. Dentro de `users`, crea las siguientes carpetas:
   - `components`
   - `services`
   - `models`

Al final, dentro de `src/app` deberías tener:

- `core/`
- `shared/`
- `modules/products/components/`
- `modules/products/services/`
- `modules/products/models/`

---

## 5. Crear el modelo de producto en el frontend

El frontend necesita saber cómo se ve un **Producto** que viene del backend.

En `src/app/modules/products/models`:

1. Clic derecho en la carpeta `models` → **New File**.
2. Nombre del archivo: `product.model.ts`.
3. Contenido:

```typescript
export interface Product {
  id?: number;
  name: string;
  description: string;
  price: number;
  stock: number;
}
```

- Esta interfaz representa los datos que esperamos del backend.
- `id` es opcional (`?`) porque cuando creamos un producto nuevo todavía no tiene id.

---

## 6. Crear el servicio para llamar al backend (ProductService)

El servicio se encargará de hacer las peticiones HTTP al backend (`http://localhost:8080/api/products`).

### 6.1. Activar HttpClientModule en app.module.ts

1. Abre el archivo `src/app/app.module.ts`.
2. Asegúrate de:
   - Importar `HttpClientModule`.
   - Incluirlo en el array `imports`.

Ejemplo (no borres lo que ya tengas, solo añade lo que falte):

```typescript
import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { HttpClientModule } from '@angular/common/http';
import { AppComponent } from './app.component';

@NgModule({
  declarations: [
    AppComponent
    // aquí irán más componentes luego
  ],
  imports: [
    BrowserModule,
    HttpClientModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
```

Guarda el archivo.

### 6.2. Crear ProductService

1. En `src/app/modules/products/services`, crea un archivo:
   - `product.service.ts`

2. Contenido:

```typescript
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Product } from '../models/product.model';

@Injectable({
  providedIn: 'root'
})
export class ProductService {

  private readonly apiUrl = 'http://localhost:8080/api/products';

  constructor(private http: HttpClient) {}

  getAll(): Observable<Product[]> {
    return this.http.get<Product[]>(this.apiUrl);
  }

  create(product: Product): Observable<Product> {
    return this.http.post<Product>(this.apiUrl, product);
  }

  update(id: number, product: Product): Observable<Product> {
    return this.http.put<Product>(`${this.apiUrl}/${id}`, product);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
```

Este servicio sabe cómo hablar con el backend usando HTTP.

---

## 7. Crear un componente para listar y crear productos

Vamos a hacer un componente sencillo que:

- Muestre una **tabla** con los productos.
- Tenga un **formulario** para crear un nuevo producto.

### 7.1. Crear el componente product-list

1. En la terminal, dentro de `frontend` (carpeta del proyecto Angular), ejecuta:

   ```bash
   ng generate component modules/products/components/product-list
   ```

   Esto creará varios archivos, incluyendo:

   - `product-list.component.ts`
   - `product-list.component.html`
   - `product-list.component.css`

### 7.2. Editar product-list.component.ts

Abre `src/app/modules/products/components/product-list/product-list.component.ts` y reemplaza su contenido por:

```typescript
import { Component, OnInit } from '@angular/core';
import { Product } from '../../models/product.model';
import { ProductService } from '../../services/product.service';

@Component({
  selector: 'app-product-list',
  templateUrl: './product-list.component.html',
  styleUrls: ['./product-list.component.css']
})
export class ProductListComponent implements OnInit {

  products: Product[] = [];

  // Modelo del formulario
  newProduct: Product = {
    name: '',
    description: '',
    price: 0,
    stock: 0
  };

  isLoading = false;
  errorMessage = '';

  constructor(private productService: ProductService) {}

  ngOnInit(): void {
    this.loadProducts();
  }

  loadProducts(): void {
    this.isLoading = true;
    this.errorMessage = '';

    this.productService.getAll().subscribe({
      next: (data) => {
        this.products = data;
        this.isLoading = false;
      },
      error: () => {
        this.errorMessage = 'No se pudieron cargar los productos. Revisa que el backend esté encendido.';
        this.isLoading = false;
      }
    });
  }

  createProduct(): void {
    // Validación muy simple
    if (!this.newProduct.name || this.newProduct.price <= 0 || this.newProduct.stock < 0) {
      this.errorMessage = 'Por favor completa correctamente el formulario.';
      return;
    }

    this.errorMessage = '';

    this.productService.create(this.newProduct).subscribe({
      next: () => {
        // Limpiamos el formulario
        this.newProduct = {
          name: '',
          description: '',
          price: 0,
          stock: 0
        };
        // Recargamos la lista
        this.loadProducts();
      },
      error: () => {
        this.errorMessage = 'No se pudo crear el producto.';
      }
    });
  }
}
```

---

## 8. Diseñar la vista HTML del componente

Abre `src/app/modules/products/components/product-list/product-list.component.html` y reemplaza su contenido por:

```html
<div class="container">
  <h1>Mini Ecommerce - Productos</h1>

  <section class="form-section">
    <h2>Crear nuevo producto</h2>

    <div class="form-grid">
      <label>
        Nombre:
        <input type="text" [(ngModel)]="newProduct.name" />
      </label>

      <label>
        Descripción:
        <input type="text" [(ngModel)]="newProduct.description" />
      </label>

      <label>
        Precio:
        <input type="number" [(ngModel)]="newProduct.price" />
      </label>

      <label>
        Stock:
        <input type="number" [(ngModel)]="newProduct.stock" />
      </label>
    </div>

    <button (click)="createProduct()">Guardar producto</button>
  </section>

  <section class="list-section">
    <h2>Lista de productos</h2>

    <p *ngIf="isLoading">Cargando productos...</p>
    <p *ngIf="errorMessage" class="error">{{ errorMessage }}</p>

    <table *ngIf="products.length > 0">
      <thead>
        <tr>
          <th>ID</th>
          <th>Nombre</th>
          <th>Descripción</th>
          <th>Precio</th>
          <th>Stock</th>
        </tr>
      </thead>
      <tbody>
        <tr *ngFor="let product of products">
          <td>{{ product.id }}</td>
          <td>{{ product.name }}</td>
          <td>{{ product.description }}</td>
          <td>{{ product.price | currency:'USD' }}</td>
          <td>{{ product.stock }}</td>
        </tr>
      </tbody>
    </table>

    <p *ngIf="!isLoading && products.length === 0">
      No hay productos aún. Crea el primero usando el formulario.
    </p>
  </section>
</div>
```

### 8.1. Estilos básicos

Abre `product-list.component.css` y añade:

```css
.container {
  max-width: 900px;
  margin: 0 auto;
  padding: 1.5rem;
  font-family: system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
}

h1 {
  text-align: center;
  margin-bottom: 2rem;
}

.form-section,
.list-section {
  background: #ffffff;
  border-radius: 8px;
  padding: 1rem 1.5rem;
  margin-bottom: 1.5rem;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1rem;
  margin-bottom: 1rem;
}

label {
  display: flex;
  flex-direction: column;
  font-size: 0.9rem;
}

input {
  margin-top: 0.25rem;
  padding: 0.4rem 0.6rem;
  border-radius: 4px;
  border: 1px solid #ccc;
}

button {
  background-color: #1976d2;
  color: white;
  border: none;
  padding: 0.6rem 1.2rem;
  border-radius: 4px;
  cursor: pointer;
}

button:hover {
  background-color: #115293;
}

table {
  width: 100%;
  border-collapse: collapse;
  margin-top: 1rem;
}

th,
td {
  border: 1px solid #ddd;
  padding: 0.5rem;
  text-align: left;
}

th {
  background-color: #f4f4f4;
}

.error {
  color: #d32f2f;
}
```

---

## 9. Activar el uso de formularios (FormsModule)

Para que `[(ngModel)]` funcione en el HTML necesitamos importar el `FormsModule`.

1. Abre de nuevo `src/app/app.module.ts`.
2. Añade la importación y el módulo en `imports`:

```typescript
import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { HttpClientModule } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { AppComponent } from './app.component';
import { ProductListComponent } from './modules/products/components/product-list/product-list.component';

@NgModule({
  declarations: [
    AppComponent,
    ProductListComponent
  ],
  imports: [
    BrowserModule,
    HttpClientModule,
    FormsModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
```

Guarda el archivo.

---

## 10. Mostrar el componente en la página principal

Abre `src/app/app.component.html` y deja su contenido muy sencillo:

```html
<app-product-list></app-product-list>
```

Así la página principal de Angular mostrará directamente nuestra pantalla de productos.

---

## 11. Probar el frontend junto con el backend

1. Asegúrate de que el **backend** Spring Boot está encendido en `http://localhost:8080`.
2. Asegúrate de que el **frontend Angular** está corriendo con:

   ```bash
   ng serve
   ```

   y que la consola indica que está escuchando en `http://localhost:4200/`.

3. Abre el navegador y entra a:

   - `http://localhost:4200/`

4. Deberías ver:
   - El título “Mini Ecommerce - Productos”.
   - El formulario para crear un producto.
   - Una tabla (vacía al principio).

5. Rellena el formulario con un producto de prueba y pulsa **“Guardar producto”**.

6. Si todo está bien:
   - El producto aparecerá en la tabla.
   - Si miras en MySQL Workbench con:

     ```sql
     USE fullstack_ecommerce;
     SELECT * FROM products;
     ```

     verás el nuevo producto creado desde el frontend.

Si ves errores de red en la consola del navegador (por ejemplo `CORS`), se pueden solucionar añadiendo configuración CORS en el backend; coméntaselo a tu profesor o revisa la documentación de Spring Boot para `@CrossOrigin`.

---

## 12. Resumen final del frontend

Siguiendo este tutorial habrás conseguido:

- Crear una aplicación Angular usando **Angular CLI**.
- Organizar las carpetas en `core`, `shared` y `modules/products`.
- Definir un modelo `Product` en el frontend.
- Crear un servicio `ProductService` que llama al backend Spring Boot.
- Crear un componente `ProductListComponent` con:
  - Formulario para crear productos.
  - Tabla que muestra la lista de productos.
- Hacer que el frontend y el backend trabajen juntos usando HTTP.

Con esto ya tienes una base sólida para seguir ampliando tu mini ecommerce:

- Añadir botones para **editar** y **borrar** productos desde la tabla.
- Crear más pantallas (por ejemplo, gestión de usuarios, pedidos, etc.).
- Mejorar el diseño con alguna librería de estilos (Angular Material, Bootstrap, Tailwind, etc.).

