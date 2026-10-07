# Orden de programación de archivos

Este documento indica **en qué orden crear o programar** cada archivo del proyecto para respetar las dependencias (un archivo no puede depender de otro que aún no existe).

---

## 1. Backend (Spring Boot)

El orden sigue la capa de abajo hacia arriba: primero lo que no depende de nada, luego lo que lo usa.

### Fase 1 – Base del proyecto (ya viene de Spring Initializr)

| Orden | Archivo | Motivo |
|-------|---------|--------|
| 1 | `pom.xml` / proyecto desde start.spring.io | Ya viene generado. Solo revisar `java.version` (21). |
| 2 | `application.properties` | Configuración de MySQL y JPA. Sin esto no arranca la BD. |
| 3 | `FullstackApplication.java` | Clase principal. Ya viene generada. |

### Fase 2 – Modelo y persistencia

| Orden | Archivo | Motivo |
|-------|---------|--------|
| 4 | `model/entity/Product.java` | Entidad JPA. No depende de ningún otro archivo nuestro. |
| 5 | `repository/ProductRepository.java` | Depende de `Product`. Proporciona acceso a la tabla `products`. |

### Fase 3 – DTO y mapper

| Orden | Archivo | Motivo |
|-------|---------|--------|
| 6 | `dto/ProductDto.java` | Objeto plano para la API. No depende de entidad ni repositorio. |
| 7 | `mapper/ProductMapper.java` | Convierte Product ↔ ProductDto. Depende de `Product` y `ProductDto`. |

### Fase 4 – Excepciones

| Orden | Archivo | Motivo |
|-------|---------|--------|
| 8 | `exception/ResourceNotFoundException.java` | Excepción personalizada. No depende de nadie. |
| 9 | `exception/GlobalExceptionHandler.java` | Maneja la excepción anterior. Depende de `ResourceNotFoundException`. |

### Fase 5 – Lógica de negocio (servicio)

| Orden | Archivo | Motivo |
|-------|---------|--------|
| 10 | `service/ProductService.java` | Interfaz del servicio. Solo declara métodos con `ProductDto`. |
| 11 | `service/impl/ProductServiceImpl.java` | Implementación. Depende de: Repository, Mapper, ProductDto, ResourceNotFoundException. |

### Fase 6 – API REST y seguridad

| Orden | Archivo | Motivo |
|-------|---------|--------|
| 12 | `controller/ProductController.java` | Expone los endpoints. Depende de `ProductService` y `ProductDto`. |
| 13 | `config/SecurityConfig.java` | CORS y seguridad. Puede ir antes o después del controller; se suele dejar después para que la API esté ya definida. |

**Resumen orden backend:**  
1 → 2 → 3 → **4 Product** → **5 Repository** → **6 DTO** → **7 Mapper** → **8 ResourceNotFoundException** → **9 GlobalExceptionHandler** → **10 ProductService** → **11 ProductServiceImpl** → **12 ProductController** → **13 SecurityConfig**.

---

## 2. Frontend (Angular)

Primero lo que no depende de la API (modelo, estructura), luego el servicio HTTP y por último la vista.

### Fase 1 – Configuración y modelo

| Orden | Archivo | Motivo |
|-------|---------|--------|
| 1 | Proyecto `ng new frontend` | Estructura base. |
| 2 | `app.config.ts` | Añadir `provideHttpClient()` para poder usar el servicio HTTP. |
| 3 | `app.routes.ts` | Rutas (puede quedar vacío). |
| 4 | `modules/products/models/product.model.ts` | Interfaz `Product`. No depende de servicios ni componentes. |

### Fase 2 – Servicio HTTP

| Orden | Archivo | Motivo |
|-------|---------|--------|
| 5 | `modules/products/services/product.service.ts` | Depende del modelo `Product` y de `HttpClient` (ya provisto en config). |

### Fase 3 – Componente de lista y formulario

| Orden | Archivo | Motivo |
|-------|---------|--------|
| 6 | `modules/products/components/product-list/product-list.component.ts` | Lógica del componente. Depende del modelo y del `ProductService`. |
| 7 | `modules/products/components/product-list/product-list.component.html` | Vista (formulario + tabla). Depende del .ts del mismo componente. |
| 8 | `modules/products/components/product-list/product-list.component.css` | Estilos del componente. |

### Fase 4 – Integración en la app

| Orden | Archivo | Motivo |
|-------|---------|--------|
| 9 | `app.component.ts` | Importar `ProductListComponent`. |
| 10 | `app.component.html` | Usar `<app-product-list></app-product-list>`. |

**Resumen orden frontend:**  
1 (proyecto) → 2 **app.config** → 3 **app.routes** → 4 **product.model** → 5 **product.service** → 6 **product-list.component.ts** → 7 **product-list.component.html** → 8 **product-list.component.css** → 9 **app.component.ts** → 10 **app.component.html**.

---

## 3. Orden global (Backend + Frontend)

1. Crear proyecto backend (Spring Initializr) y configurar `application.properties`.
2. Backend: **Product** → **ProductRepository** → **ProductDto** → **ProductMapper** → **ResourceNotFoundException** → **GlobalExceptionHandler** → **ProductService** → **ProductServiceImpl** → **ProductController** → **SecurityConfig**.
3. Crear proyecto frontend (`ng new`) y configurar `app.config.ts` (HttpClient).
4. Frontend: **product.model** → **product.service** → **product-list** (ts, html, css) → **app.component** (ts, html).

Si sigues este orden, en cada paso tendrás ya creadas todas las dependencias que usa el archivo que estás programando.
