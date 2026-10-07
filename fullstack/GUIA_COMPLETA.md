# GUIA COMPLETA - MINI ECOMMERCE FULLSTACK

Guia oficial del proyecto, desde la creacion en Spring Initializr hasta la ejecucion completa.

---

## 1) Objetivo

Construir y ejecutar una aplicacion fullstack con:

- Backend: Spring Boot + JPA + MySQL
- Frontend: Angular
- Modulo: CRUD de productos

---

## 2) Requisitos previos

Debes tener instalado:

- Java 21
- Maven 3.9+
- MySQL Server + MySQL Workbench
- Node.js 20+ y npm
- Angular CLI

Verificacion:

```powershell
java -version
mvn -v
mysql --version
node -v
npm -v
ng version
```

---

## 3) Diligenciamiento completo en Spring Initializr

Abre:

- `https://start.spring.io/index.html`

En la pantalla principal llena **cada campo** asi:

### 3.1 Bloque Project

- `Project`: **Maven**
- `Language`: **Java**
- `Spring Boot`: **4.0.3** (o la estable mas cercana)

### 3.2 Bloque Project Metadata

- `Group`: `com.fullstack`
- `Artifact`: `fullstack`
- `Name`: `fullstack`
- `Description`: `Mini ecommerce backend`
- `Package name`: `com.fullstack.fullstack`
- `Packaging`: `Jar`
- `Java`: `21`

### 3.3 Bloque Dependencies

Haz clic en **Add Dependencies** y agrega:

- Spring Web
- Spring Data JPA
- MySQL Driver
- Spring Security
- Lombok

### 3.4 Generar proyecto

1. Haz clic en **GENERATE**.
2. Descarga el archivo `.zip`.
3. Extrae el contenido en la carpeta `backend/`.

---

## 4) Estructura del proyecto

```text
fullstack/
|- backend/   (proyecto generado en Spring Initializr)
|- frontend/  (proyecto Angular)
|- GUIA_COMPLETA.md
```

---

## 5) Configurar MySQL

En MySQL Workbench ejecuta:

```sql
CREATE DATABASE IF NOT EXISTS fullstack_ecommerce
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
```

Verifica:

```sql
SHOW DATABASES;
```

---

## 6) Configurar backend

### 6.1 `application.properties`

Archivo: `backend/src/main/resources/application.properties`

```properties
spring.application.name=fullstack

# Datasource
spring.datasource.url=jdbc:mysql://localhost:3306/fullstack_ecommerce
spring.datasource.username=root
spring.datasource.password=admin
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA / Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
```

> Cambia `username/password` segun tu entorno.

### 6.2 `pom.xml`

Verifica en `backend/pom.xml`:

```xml
<java.version>21</java.version>
```

---

## 7) Crear capas backend (resumen)

Dentro de `backend/src/main/java/com/fullstack/fullstack/`:

- `model/entity/Product.java`
- `dto/ProductDto.java`
- `repository/ProductRepository.java`
- `mapper/ProductMapper.java`
- `service/ProductService.java`
- `service/impl/ProductServiceImpl.java`
- `controller/ProductController.java`
- `config/SecurityConfig.java`
- `exception/ResourceNotFoundException.java`
- `exception/GlobalExceptionHandler.java`

Endpoints esperados:

- `GET /api/products`
- `GET /api/products/{id}`
- `POST /api/products`
- `PUT /api/products/{id}`
- `DELETE /api/products/{id}`

---

## 8) Crear frontend Angular

Desde la raiz `fullstack/`:

```powershell
ng new frontend
```

Opciones recomendadas:

- Stylesheet: `CSS`
- SSR/SSG: `No`
- Routing: opcional (`Yes` o `No`)

---

## 9) Configurar frontend (resumen)

- Modelo: `frontend/src/app/modules/products/models/product.model.ts`
- Servicio HTTP: `frontend/src/app/modules/products/services/product.service.ts`
- Componente principal:
  - `frontend/src/app/modules/products/components/product-list/product-list.component.ts`
  - `frontend/src/app/modules/products/components/product-list/product-list.component.html`

URL backend en servicio:

```ts
private readonly apiUrl = 'http://localhost:8080/api/products';
```

---

## 10) Ejecutar todo

### Terminal 1 (backend)

```powershell
cd "C:\Users\johan\Documents\SALLE\2026-1\CONSTRUCCION DE SOFTWARE\fullstack\backend"
mvn clean compile
mvn spring-boot:run
```

### Terminal 2 (frontend)

```powershell
cd "C:\Users\johan\Documents\SALLE\2026-1\CONSTRUCCION DE SOFTWARE\fullstack\frontend"
npm install
ng serve
```

Abrir:

- Frontend: `http://localhost:4200`
- Backend API: `http://localhost:8080/api/products`

---

## 11) Prueba funcional

1. Crear producto en el formulario del frontend.
2. Verificar que aparezca en la tabla.
3. Validar en DB:

```sql
USE fullstack_ecommerce;
SELECT * FROM products;
```

---

## 12) Solucion de errores comunes

### Error `release version 25 not supported`

Dejar en `pom.xml`:

```xml
<java.version>21</java.version>
```

### Error `MySQL8Dialect` no encontrado

Usar:

```properties
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
```

### Frontend: `No se pudo crear el producto`

Revisar:

1. Backend activo en `:8080`.
2. MySQL activo.
3. Credenciales correctas.
4. CORS permitido en `SecurityConfig`.

---

## 13) Checklist final

- [ ] Proyecto diligenciado en Spring Initializr
- [ ] Backend generado y abierto en `backend/`
- [ ] Base de datos creada
- [ ] Backend ejecutando en `:8080`
- [ ] Frontend ejecutando en `:4200`
- [ ] CRUD funcionando

---

Fin.

# GUIA COMPLETA - MINI ECOMMERCE FULLSTACK (DESDE SPRING INITIALIZR)

Esta guia esta pensada para rehacer el proyecto completo desde cero, empezando por la plantilla creada en:

- `https://start.spring.io/index.html`

Incluye:

1. Creacion de plantilla backend en Spring Initializr
2. Configuracion de MySQL
3. Implementacion backend (CRUD productos)
4. Creacion de frontend Angular
5. Conexion frontend-backend
6. Ejecucion y pruebas
7. Solucion de errores comunes

---

## 1) Stack del proyecto

- Backend: Spring Boot `4.0.3+` (Maven, Java 21)
- Frontend: Angular `19`
- Base de datos: MySQL `8.x`
- API: REST `/api/products`

---

## 2) Requisitos previos

Debes tener instalado:

- Java JDK 21
- Maven 3.9+
- MySQL Server + MySQL Workbench
- Node.js 20+ y npm
- Angular CLI (`npm i -g @angular/cli`)
- Visual Studio Code (opcional, recomendado)

Verificacion rapida:

```powershell
java -version
mvn -v
mysql --version
node -v
npm -v
ng version
```

---

## 3) Crear backend desde Spring Initializr

Abre `https://start.spring.io/index.html` y configura:

### 3.1 Opciones generales

- Project: `Maven`
- Language: `Java`
- Spring Boot: `4.0.3` (o la estable disponible)

### 3.2 Metadata

- Group: `com.fullstack`
- Artifact: `fullstack`
- Name: `fullstack`
- Description: `Mini ecommerce backend`
- Package name: `com.fullstack.fullstack`
- Packaging: `Jar`
- Java: `21`

### 3.3 Dependencias

Agrega estas dependencias:

- Spring Web
- Spring Data JPA
- MySQL Driver
- Spring Security
- Lombok

Pulsa `GENERATE` y descarga el zip.

---

## 4) Estructura de carpetas del repositorio

Crea una carpeta raiz llamada `fullstack` y deja:

```text
fullstack/
|- backend/   (proyecto descargado de Spring Initializr)
|- frontend/  (se crea con Angular CLI)
```

Extrae el zip de Spring Initializr dentro de `backend`.

---

## 5) Crear base de datos en MySQL

En MySQL Workbench, ejecuta:

```sql
CREATE DATABASE IF NOT EXISTS fullstack_ecommerce
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
```

Comprobacion:

```sql
SHOW DATABASES;
```

---

## 6) Configurar `application.properties`

Archivo: `backend/src/main/resources/application.properties`

```properties
spring.application.name=fullstack

# === Datasource ===
spring.datasource.url=jdbc:mysql://localhost:3306/fullstack_ecommerce
spring.datasource.username=root
spring.datasource.password=admin
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# === JPA / Hibernate ===
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
```

> Ajusta `username/password` con tus credenciales reales.

---

## 7) Ajustar `pom.xml` a Java 21

En `backend/pom.xml` verifica:

```xml
<java.version>21</java.version>
```

Esto evita el error `release version 25 not supported` cuando Maven usa JDK 21.

---

## 8) Estructura backend (paquetes)

Dentro de `backend/src/main/java/com/fullstack/fullstack/` crea:

```text
config/
controller/
dto/
exception/
mapper/
model/entity/
repository/
service/
service/impl/
```

---

## 9) Crear clases backend

### 9.1 Entidad `Product`

Ruta: `model/entity/Product.java`

Campos:

- `id: Long`
- `name: String`
- `description: String`
- `price: BigDecimal`
- `stock: Integer`

Anotaciones:

- `@Entity`
- `@Table(name = "products")`
- Lombok (`@Data`, `@Builder`, etc.)

### 9.2 DTO `ProductDto`

Ruta: `dto/ProductDto.java`  
Mismos campos del `Product`.

### 9.3 Repositorio

Ruta: `repository/ProductRepository.java`

Hereda de:

- `JpaRepository<Product, Long>`

### 9.4 Mapper

Ruta: `mapper/ProductMapper.java`  
Metodos:

- `toDto(Product)`
- `toEntity(ProductDto)`
- `updateEntity(Product, ProductDto)`

### 9.5 Excepciones

En `exception/`:

- `ResourceNotFoundException`
- `GlobalExceptionHandler`

### 9.6 Servicio

En `service/`:

- `ProductService` (interfaz CRUD)

En `service/impl/`:

- `ProductServiceImpl` (implementacion con `ProductRepository`)

### 9.7 Controlador REST

Ruta: `controller/ProductController.java`

Base path:

- `/api/products`

Endpoints:

- `GET /api/products`
- `GET /api/products/{id}`
- `POST /api/products`
- `PUT /api/products/{id}`
- `DELETE /api/products/{id}`

---

## 10) Configurar seguridad y CORS

Crea `config/SecurityConfig.java` para permitir peticiones del frontend (`localhost:4200`):

- Deshabilitar CSRF en desarrollo
- Permitir `/api/**`
- CORS con metodos `GET, POST, PUT, DELETE, OPTIONS`

Origen permitido:

- `http://localhost:4200`

---

## 11) Probar backend solo

Desde `backend`:

```powershell
mvn clean compile
mvn spring-boot:run
```

Probar endpoint:

- `http://localhost:8080/api/products`

Debe responder `[]` o lista JSON.

---

## 12) Crear frontend Angular

Desde la carpeta raiz `fullstack/`:

```powershell
ng new frontend
```

Durante el asistente:

- Stylesheet: `CSS`
- SSR/SSG: `No`
- Routing: puede ser `Yes` o `No` (ambos sirven)

---

## 13) Estructura frontend

En `frontend/src/app/` usa:

```text
modules/products/
|- components/product-list/
|- models/
|- services/
```

Archivos clave:

- `models/product.model.ts`
- `services/product.service.ts`
- `components/product-list/*`

---

## 14) Implementar frontend

### 14.1 Modelo

`product.model.ts`:

- `id?: number`
- `name: string`
- `description: string`
- `price: number`
- `stock: number`

### 14.2 Habilitar HttpClient

En `app.config.ts` agrega:

- `provideHttpClient()`

### 14.3 Servicio HTTP

En `product.service.ts`:

- `apiUrl = 'http://localhost:8080/api/products'`
- Metodos `getAll`, `create`, `update`, `delete`

### 14.4 Componente `product-list`

Formulario para crear producto + tabla para listar productos.

### 14.5 Mostrar componente en app raiz

En `app.component.html`:

```html
<app-product-list></app-product-list>
```

---

## 15) Ejecutar todo el proyecto

Abre dos terminales.

### Terminal A - Backend

```powershell
cd "C:\Users\johan\Documents\SALLE\2026-1\CONSTRUCCION DE SOFTWARE\fullstack\backend"
mvn spring-boot:run
```

### Terminal B - Frontend

```powershell
cd "C:\Users\johan\Documents\SALLE\2026-1\CONSTRUCCION DE SOFTWARE\fullstack\frontend"
npm install
ng serve
```

Abrir en navegador:

- Frontend: `http://localhost:4200`
- Backend API: `http://localhost:8080/api/products`

---

## 16) Prueba funcional completa

1. En el frontend, llena:
   - Nombre
   - Descripcion
   - Precio
   - Stock
2. Clic en `Guardar producto`.
3. Verifica que aparece en la tabla.
4. En MySQL Workbench:

```sql
USE fullstack_ecommerce;
SELECT * FROM products;
```

Si aparece en DB, el flujo esta correcto:

Frontend -> Backend -> MySQL.

---

## 17) Errores comunes (y solucion)

### 17.1 `release version 25 not supported`

Causa: Maven corre con Java 21 y `pom.xml` estaba en 25.

Solucion:

```xml
<java.version>21</java.version>
```

### 17.2 `Unable to load class org.hibernate.dialect.MySQL8Dialect`

Causa: en Hibernate actual ese dialecto ya no aplica igual.

Solucion:

```properties
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
```

### 17.3 Frontend muestra "No se pudo crear el producto"

Revisar:

1. Backend activo en `:8080`.
2. MySQL activo.
3. Credenciales correctas en `application.properties`.
4. CORS permitido en `SecurityConfig`.
5. Revisar consola backend al hacer POST.

### 17.4 Puertos ocupados

```powershell
netstat -ano | findstr :8080
netstat -ano | findstr :4200
taskkill /PID <PID> /F
```

---

## 18) Checklist final

- [ ] Plantilla backend creada en `start.spring.io`
- [ ] Base de datos `fullstack_ecommerce` creada
- [ ] Backend levanta sin errores
- [ ] Frontend levanta en `:4200`
- [ ] Se puede crear producto desde UI
- [ ] Producto se guarda en MySQL

---

Fin.

# GUIA COMPLETA - MINI ECOMMERCE FULLSTACK

Esta guia explica, de principio a fin, como preparar, ejecutar, probar y solucionar problemas del proyecto `fullstack` con:

- Backend: Spring Boot + JPA + MySQL
- Frontend: Angular
- Entidad principal: `Product`

---

## 1) Arquitectura del proyecto

Estructura general:

```text
fullstack/
|- backend/   (API REST con Spring Boot)
|- frontend/  (UI con Angular)
|- GUIA_COMPLETA.md
```

Flujo funcional:

1. El frontend envia solicitudes HTTP a `http://localhost:8080/api/products`.
2. El backend recibe la solicitud y usa JPA/Hibernate para leer o guardar en MySQL.
3. El backend responde JSON y el frontend actualiza la vista.

---

## 2) Requisitos previos

Instala y verifica lo siguiente:

- Java JDK 21
- Maven 3.9.x (o usar `mvnw` del proyecto)
- MySQL Server 8.x
- Node.js 20+ y npm
- Angular CLI 19 (`npm i -g @angular/cli`)

Comandos de verificacion:

```powershell
java -version
mvn -v
node -v
npm -v
ng version
mysql --version
```

> Importante: en este proyecto Maven compila con Java 21. Si Maven usa otro JDK, revisa `JAVA_HOME`.

---

## 3) Base de datos MySQL

Conecta a MySQL y crea la base:

```sql
CREATE DATABASE IF NOT EXISTS fullstack_ecommerce
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
```

No necesitas crear la tabla manualmente; Hibernate la crea con `ddl-auto=update`.

---

## 4) Configuracion actual del backend

Archivo: `backend/src/main/resources/application.properties`

```properties
spring.application.name=fullstack

# === Datasource (ajusta usuario y password) ===
spring.datasource.url=jdbc:mysql://localhost:3306/fullstack_ecommerce
spring.datasource.username=root
spring.datasource.password=admin
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# === JPA / Hibernate ===
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
```

Si tu usuario/password de MySQL son distintos, cambia solo estas lineas:

- `spring.datasource.username`
- `spring.datasource.password`

---

## 5) Modelo funcional del backend

Paquetes principales en `backend/src/main/java/com/fullstack/fullstack/`:

- `model/entity/Product.java`
- `repository/ProductRepository.java`
- `dto/ProductDto.java`
- `mapper/ProductMapper.java`
- `service/ProductService.java`
- `service/impl/ProductServiceImpl.java`
- `controller/ProductController.java`
- `config/SecurityConfig.java`
- `exception/*`

### Endpoints disponibles

Base URL: `http://localhost:8080/api/products`

- `GET /api/products` -> listar
- `GET /api/products/{id}` -> buscar por ID
- `POST /api/products` -> crear
- `PUT /api/products/{id}` -> actualizar
- `DELETE /api/products/{id}` -> eliminar

Payload ejemplo para crear:

```json
{
  "name": "Camiseta",
  "description": "Algodon",
  "price": 200,
  "stock": 10
}
```

---

## 6) Seguridad y CORS

El backend permite peticiones desde Angular en `http://localhost:4200` y habilita `/api/**`.

Archivo: `backend/src/main/java/com/fullstack/fullstack/config/SecurityConfig.java`

Puntos clave:

- `csrf` deshabilitado (correcto para pruebas REST locales)
- CORS habilitado con origen `http://localhost:4200`
- Metodos permitidos: `GET, POST, PUT, DELETE, OPTIONS`

---

## 7) Configuracion actual del frontend

El consumo de API esta en:

- `frontend/src/app/modules/products/services/product.service.ts`

URL configurada:

```ts
private readonly apiUrl = 'http://localhost:8080/api/products';
```

Componente principal de UI:

- `frontend/src/app/modules/products/components/product-list/product-list.component.ts`
- `frontend/src/app/modules/products/components/product-list/product-list.component.html`

---

## 8) Ejecucion paso a paso

Abre dos terminales.

### Terminal 1 - Backend

```powershell
cd "C:\Users\johan\Documents\SALLE\2026-1\CONSTRUCCION DE SOFTWARE\fullstack\backend"
mvn spring-boot:run
```

Debe iniciar en `http://localhost:8080`.

### Terminal 2 - Frontend

```powershell
cd "C:\Users\johan\Documents\SALLE\2026-1\CONSTRUCCION DE SOFTWARE\fullstack\frontend"
npm install
ng serve
```

Abre:

- `http://localhost:4200`

---

## 9) Prueba funcional completa

1. En la pantalla "Mini Ecommerce - Productos", completa el formulario.
2. Clic en **Guardar producto**.
3. Debe aparecer en "Lista de productos".
4. Verifica en MySQL:

```sql
USE fullstack_ecommerce;
SELECT * FROM products;
```

---

## 10) Pruebas rapidas de API (sin frontend)

### Listar productos

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/products" -Method Get
```

### Crear producto

```powershell
$body = @{
  name = "Camiseta"
  description = "Algodon"
  price = 200
  stock = 10
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/products" `
  -Method Post `
  -ContentType "application/json" `
  -Body $body
```

---

## 11) Problemas comunes y soluciones

### Error: `release version 25 not supported`

Causa:
- Maven usa JDK 21, pero el `pom.xml` estaba en Java 25.

Solucion:
- En `backend/pom.xml` dejar:

```xml
<java.version>21</java.version>
```

---

### Error: `Unable to load class org.hibernate.dialect.MySQL8Dialect`

Causa:
- Hibernate 7 (Spring Boot 4) ya no usa `MySQL8Dialect` con ese nombre.

Solucion:
- En `application.properties` usar:

```properties
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
```

---

### Mensaje en frontend: `No se pudo crear el producto`

Checklist:

1. Verifica backend levantado en `:8080`.
2. Verifica MySQL activo.
3. Revisa credenciales en `application.properties`.
4. Revisa error exacto en consola del backend al hacer click en "Guardar producto".
5. Prueba `POST` con `Invoke-RestMethod`.

---

### Error CORS en navegador

Verifica en `SecurityConfig`:

- Origen permitido `http://localhost:4200`
- Metodo `POST` incluido
- Backend reiniciado despues de cambios

---

### Puerto ocupado

#### Puerto 8080

```powershell
netstat -ano | findstr :8080
taskkill /PID <PID> /F
```

#### Puerto 4200

```powershell
netstat -ano | findstr :4200
taskkill /PID <PID> /F
```

---

## 12) Comandos utiles del dia a dia

### Backend

```powershell
cd backend
mvn clean compile
mvn spring-boot:run
```

### Frontend

```powershell
cd frontend
npm install
ng serve
ng build
```

---

## 13) Estado esperado al finalizar

Todo esta correcto cuando:

- Backend inicia sin excepciones de Hibernate.
- Frontend abre en `http://localhost:4200`.
- Puedes crear productos desde el formulario.
- Los productos aparecen en la tabla del frontend.
- Los registros existen en MySQL (`products`).

---

## 14) Recomendaciones para siguiente iteracion

1. Agregar validaciones con Bean Validation (`@NotBlank`, `@Positive`, etc.).
2. Devolver errores estandarizados (400/422) para inputs invalidos.
3. Añadir botones de editar/eliminar en la UI.
4. Agregar pruebas unitarias e integracion para `ProductService` y `ProductController`.
5. Externalizar credenciales (`.env` o variables de entorno).

---

## 15) Resumen rapido

Si quieres ejecutar todo en menos de 2 minutos:

1. Inicia MySQL.
2. Corre backend con `mvn spring-boot:run`.
3. Corre frontend con `ng serve`.
4. Abre `http://localhost:4200`.
5. Crea un producto y verifica en la lista.

Fin.


---

## Cambios posteriores (versión actual del código)

**Backend**

- **Validación:** `ProductDto` usa `@NotBlank`, `@DecimalMin("0.01")`, `@NotNull` y `@PositiveOrZero`; el controller recibe `@Valid @RequestBody`. Requiere `spring-boot-starter-validation`.
- **Errores:** `GlobalExceptionHandler` responde 404 (no existe), 400 (validación, con `errors` por campo, o JSON mal formado) y 500 (error inesperado, detalle solo en el log).
- **Precio:** `BigDecimal` con `precision = 10, scale = 2` en lugar de `Double`.
- **Configuración:** `application.properties` lee `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` y `CORS_ALLOWED_ORIGIN` de variables de entorno (con valores por defecto para desarrollo local).
- **Tests:** `src/test/resources/application.properties` usa H2 en memoria (el `contextLoads` no necesita MySQL); `ProductServiceImplTest` (Mockito) y `ProductControllerTest` (MockMvc standalone).

**Frontend**

- **CRUD completo:** botones Editar y Eliminar; el mismo formulario crea o edita (`editingId`).
- **Formulario reactivo** con validaciones (`required`, `min`, `pattern`) y mensajes por campo.
- **Plantilla** con `@if` / `@for` (sin `CommonModule`).
- **API configurable:** `src/environments/environment.ts` y `environment.development.ts` (reemplazo en `angular.json`).
- **Carpeta** `modules/products` (antes `modules/users`) y sin `app.module.ts` (la app es standalone).
- **Tests:** `app.component.spec.ts` corregido y `product-list.component.spec.ts` con `HttpTestingController`.
