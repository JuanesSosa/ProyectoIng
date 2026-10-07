# Mini Ecommerce – Fullstack

Aplicación web de gestión de productos (CRUD) con arquitectura cliente-servidor.

| Capa | Tecnología |
|------|------------|
| Frontend | Angular 19 (standalone), formularios reactivos |
| Backend | Spring Boot 4, Java 21, Spring Data JPA, Bean Validation |
| Base de datos | MySQL (tests con H2 en memoria) |

## Estructura

```
fullstack/
├── backend/    API REST (Spring Boot) → http://localhost:8080/api/products
├── frontend/   Interfaz (Angular)     → http://localhost:4200
└── *.md        Guías y documentación del proyecto
```

## Requisitos

- Java 21
- Node.js 20+ y npm
- MySQL 8+ en ejecución

## Puesta en marcha

### 1. Base de datos

```sql
CREATE DATABASE fullstack_ecommerce;
```

Las tablas se crean solas al arrancar el backend (`spring.jpa.hibernate.ddl-auto=update`).

### 2. Backend

Por defecto usa `root` / `admin` en `localhost:3306`. Para cambiarlo, define variables de entorno:

| Variable | Significado | Valor por defecto |
|----------|-------------|-------------------|
| `DB_URL` | URL JDBC de MySQL | `jdbc:mysql://localhost:3306/fullstack_ecommerce` |
| `DB_USERNAME` | Usuario de la BD | `root` |
| `DB_PASSWORD` | Contraseña de la BD | `admin` |
| `CORS_ALLOWED_ORIGIN` | Origen permitido del frontend | `http://localhost:4200` |

```bash
cd backend
./mvnw spring-boot:run      # en Windows: mvnw.cmd spring-boot:run
```

### 3. Frontend

```bash
cd frontend
npm install
npm start                   # abre http://localhost:4200
```

## Endpoints

| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | `/api/products` | Lista todos los productos |
| GET | `/api/products/{id}` | Obtiene un producto |
| POST | `/api/products` | Crea un producto (201) |
| PUT | `/api/products/{id}` | Actualiza un producto |
| DELETE | `/api/products/{id}` | Elimina un producto (204) |

Reglas de validación: `name` obligatorio, `price` mayor que 0, `stock` entero ≥ 0. Si fallan, la API responde 400 con los errores por campo.

## Tests

```bash
cd backend  && ./mvnw test      # usa H2, no necesita MySQL
cd frontend && npm test
```

## Documentación

- `GUIA_COMPLETA.md` – visión general y cambios posteriores
- `arquitectura.md` – diseño del sistema
- `EJECUCION_PROYECTO.md` – cómo ejecutarlo
- `backend/TUTORIAL_BACKEND.md` y `frontend/TUTORIAL_FRONTEND.md` – tutoriales paso a paso
