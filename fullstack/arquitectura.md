# Arquitectura de la aplicación - Mini Ecommerce Fullstack

Este documento describe la arquitectura completa de la app para guiarte al desarrollarla.

---

## 1. Vista general (alto nivel)

La aplicación tiene **tres partes** que se comunican:

```
┌─────────────────────────────────────────────────────────────────────────┐
│                         NAVEGADOR (Usuario)                              │
└─────────────────────────────────────────────────────────────────────────┘
                    │                                    │
                    │ HTTP (puerto 4200)                 │ HTTP (puerto 8080)
                    ▼                                    ▼
┌───────────────────────────────┐     ┌─────────────────────────────────────┐
│      FRONTEND (Angular)       │     │        BACKEND (Spring Boot)        │
│  - Componentes, servicios     │────▶│  - API REST, lógica, persistencia   │
│  - UI y formularios           │     │  - Controller → Service → Repository│
└───────────────────────────────┘     └─────────────────────────────────────┘
                                                          │
                                                          │ JDBC / JPA
                                                          ▼
                                               ┌─────────────────────┐
                                               │   MySQL (base de    │
                                               │   datos)            │
                                               │   fullstack_ecommerce│
                                               └─────────────────────┘
```

- **Frontend**: lo que ve el usuario (formulario, tabla). Hace peticiones HTTP al backend.
- **Backend**: recibe peticiones, aplica reglas y guarda/lee en la base de datos.
- **Base de datos**: almacena los productos (tabla `products`).

---

## 2. Arquitectura del BACKEND (Spring Boot)

El backend sigue una **arquitectura en capas**. Cada capa tiene una responsabilidad clara.

### 2.1 Diagrama de capas (flujo de una petición)

```
                    ┌─────────────────────────────────────┐
                    │         ProductController            │  ← Capa de presentación (REST)
                    │  GET/POST/PUT/DELETE /api/products   │
                    └─────────────────────────────────────┘
                                        │
                                        │ usa
                                        ▼
                    ┌─────────────────────────────────────┐
                    │         ProductService              │  ← Capa de lógica de negocio
                    │  (interfaz) ProductServiceImpl     │
                    └─────────────────────────────────────┘
                                        │
                          ┌─────────────┴─────────────┐
                          │                           │
                          ▼                           ▼
                    ┌───────────┐              ┌──────────────┐
                    │  Mapper   │              │  Repository  │  ← Capa de acceso a datos
                    │ Product ↔ │              │  Product     │
                    │ ProductDto│              │  (JPA)       │
                    └───────────┘              └──────────────┘
                                                         │
                                                         ▼
                                               ┌──────────────────┐
                                               │  Entidad Product │  ← Modelo de persistencia
                                               │  (tabla products)│
                                               └──────────────────┘
```

### 2.2 Responsabilidad de cada capa

| Capa | Carpeta / Clase | Responsabilidad |
|------|-----------------|-----------------|
| **Controller** | `controller/ProductController.java` | Recibe HTTP, valida entrada básica, llama al servicio y devuelve JSON. No contiene lógica de negocio. |
| **Service** | `service/ProductService.java` + `service/impl/ProductServiceImpl.java` | Lógica de negocio: validaciones, orquestar repository y mapper, lanzar excepciones (ej. ResourceNotFoundException). |
| **Repository** | `repository/ProductRepository.java` | Acceso a la base de datos. Métodos como findAll(), findById(), save(), deleteById(). |
| **Entity** | `model/entity/Product.java` | Representa la tabla `products` en MySQL. Anotaciones JPA. |
| **DTO** | `dto/ProductDto.java` | Objeto plano para la API (entrada/salida). No expone la entidad directamente. |
| **Mapper** | `mapper/ProductMapper.java` | Convierte Entity ↔ DTO (toDto, toEntity, updateEntity). |
| **Config** | `config/SecurityConfig.java` | Seguridad y CORS: permitir origen del frontend, métodos HTTP, etc. |
| **Exception** | `exception/` | `ResourceNotFoundException` + `GlobalExceptionHandler`: convertir excepciones en respuestas HTTP (ej. 404). |

### 2.3 Estructura de carpetas del backend

```
backend/
├── src/main/java/com/fullstack/fullstack/
│   ├── FullstackApplication.java          # Punto de entrada
│   ├── config/
│   │   └── SecurityConfig.java            # CORS y seguridad
│   ├── controller/
│   │   └── ProductController.java         # REST /api/products
│   ├── dto/
│   │   └── ProductDto.java
│   ├── exception/
│   │   ├── ResourceNotFoundException.java
│   │   └── GlobalExceptionHandler.java
│   ├── mapper/
│   │   └── ProductMapper.java
│   ├── model/entity/
│   │   └── Product.java
│   ├── repository/
│   │   └── ProductRepository.java
│   └── service/
│       ├── ProductService.java            # Interfaz
│       └── impl/
│           └── ProductServiceImpl.java
└── src/main/resources/
    └── application.properties             # BD, JPA, dialect
```

---

## 3. Arquitectura del FRONTEND (Angular)

El frontend está organizado por **módulos funcionales** y dentro de ellos: **modelos**, **servicios** y **componentes**.

### 3.1 Diagrama de capas (frontend)

```
                    ┌─────────────────────────────────────┐
                    │         app.component.html          │  ← Raíz: solo incluye el listado
                    │    <app-product-list></app-product-list>  │
                    └─────────────────────────────────────┘
                                        │
                                        ▼
                    ┌─────────────────────────────────────┐
                    │     ProductListComponent            │  ← Componente de vista
                    │  (.ts = lógica, .html = plantilla)  │     (formulario + tabla)
                    └─────────────────────────────────────┘
                                        │
                                        │ usa
                                        ▼
                    ┌─────────────────────────────────────┐
                    │       ProductService                │  ← Servicio HTTP
                    │  getAll(), create(), update(), delete()  │     (llamadas al backend)
                    └─────────────────────────────────────┘
                                        │
                                        │ usa
                                        ▼
                    ┌─────────────────────────────────────┐
                    │       Product (model / interface)   │  ← Modelo de datos
                    │  id?, name, description, price, stock   │
                    └─────────────────────────────────────┘
```

### 3.2 Responsabilidad de cada parte

| Parte | Archivo(s) | Responsabilidad |
|-------|------------|-----------------|
| **Modelo** | `models/product.model.ts` | Interfaz TypeScript que define la forma de un producto (coincide con el JSON del backend). |
| **Servicio** | `services/product.service.ts` | Hace las peticiones HTTP (GET, POST, PUT, DELETE) a `http://localhost:8080/api/products`. |
| **Componente** | `product-list.component.ts` + `.html` + `.css` | Lógica de la pantalla (cargar lista, crear producto), plantilla (formulario + tabla) y estilos. |
| **App raíz** | `app.component.ts` + `.html` | Monta el único contenido visible: el componente de lista de productos. |
| **Config** | `app.config.ts` | Provee HttpClient, Router, etc. |

### 3.3 Estructura de carpetas del frontend

```
frontend/src/app/
├── app.component.ts
├── app.component.html
├── app.config.ts
├── app.routes.ts
└── modules/
    └── users/
        ├── models/
        │   └── product.model.ts
        ├── services/
        │   └── product.service.ts
        └── components/
            └── product-list/
                ├── product-list.component.ts
                ├── product-list.component.html
                └── product-list.component.css
```

---

## 4. Flujo de datos (ejemplo: crear un producto)

Secuencia desde que el usuario hace clic en "Guardar producto" hasta que el dato queda en la base de datos:

```
1. Usuario llena el formulario y hace clic en "Guardar producto".

2. Frontend (ProductListComponent)
   - createProduct() valida nombre, precio, stock.
   - productService.create(this.newProduct) envía POST a http://localhost:8080/api/products
     con body JSON: { name, description, price, stock }.

3. Backend (ProductController)
   - Recibe POST /api/products y el body (mapeado a ProductDto).
   - Llama a productService.create(productDto).

4. Backend (ProductServiceImpl)
   - ProductMapper.toEntity(dto) → Product (sin id).
   - product.setId(null).
   - productRepository.save(product) → INSERT en MySQL.
   - ProductMapper.toDto(saved) → ProductDto con id.
   - Devuelve ese DTO al controller.

5. Backend (ProductController)
   - Responde 201 Created con cabecera Location y cuerpo JSON (producto con id).

6. Frontend (ProductListComponent)
   - subscribe next: limpia el formulario y llama loadProducts() (GET /api/products).
   - La tabla se actualiza con la lista que devuelve el backend.

7. Base de datos
   - La tabla products tiene la nueva fila (id, name, description, price, stock).
```

---

## 5. API REST (contrato backend – frontend)

El frontend y el backend se coordinan mediante esta API:

| Método | Ruta | Descripción | Body (si aplica) |
|--------|------|-------------|------------------|
| GET | `/api/products` | Lista todos los productos | — |
| GET | `/api/products/{id}` | Obtiene un producto por id | — |
| POST | `/api/products` | Crea un producto | `{ name, description, price, stock }` |
| PUT | `/api/products/{id}` | Actualiza un producto | `{ name, description, price, stock }` |
| DELETE | `/api/products/{id}` | Elimina un producto | — |

Base URL del backend: `http://localhost:8080`.

---

## 6. Tecnologías por capa

| Capa | Tecnología |
|------|------------|
| Frontend UI | Angular 19, TypeScript, HTML, CSS |
| Frontend HTTP | HttpClient (Angular) |
| Backend API | Spring Boot 4, Spring Web MVC |
| Backend lógica | Spring (servicios, inyección de dependencias) |
| Persistencia | Spring Data JPA, Hibernate |
| Base de datos | MySQL 8 |
| Seguridad / CORS | Spring Security |
| Build backend | Maven |
| Build frontend | Angular CLI (npm) |

---

## 7. Cómo usar esta arquitectura para desarrollar

1. **Backend**: sigue el orden del documento `ORDEN_DE_PROGRAMACION.md` (entidad → repository → dto → mapper → excepciones → service → controller → config).
2. **Frontend**: modelo → servicio → componente (ts, html, css) → app raíz.
3. **Probar**: primero backend solo (por ejemplo con `curl` o Postman a `http://localhost:8080/api/products`), luego frontend contra ese backend.
4. **CORS**: si el frontend no puede llamar al backend, revisa `SecurityConfig` y que el origen sea `http://localhost:4200`.

Con esta arquitectura tienes una guía clara de dónde va cada archivo y qué responsabilidad tiene cada capa.
