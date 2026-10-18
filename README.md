# Gestor de Notas Académicas

Aplicación web para registrar y consultar calificaciones. Spring Boot 4 (Java 21) + MySQL + Angular 19.

## Roles
| Rol | Puede |
|---|---|
| ADMIN | Gestionar usuarios, estudiantes y asignaturas; consultar notas de cualquiera |
| DOCENTE | Ver estudiantes y asignaturas; **registrar y editar notas**; consultar notas |
| ESTUDIANTE | Consultar **solo sus** notas y promedios |

## Reglas de negocio
- Notas de 0.0 a 5.0 (máx. 2 decimales); se muestran en rojo si el promedio es menor que 3.0.
- Cada evaluación puede tener un peso (%). Pesos en todas las evaluaciones o en ninguna; suman máximo 100.
- Promedio: con pesos, ponderado Σ(nota·peso)/Σpeso; sin pesos, promedio simple. Redondeo a 2 decimales.
- Promedio general = promedio simple de los promedios por asignatura.
- Documento de estudiante y código de asignatura únicos. Usuarios y asignaturas se **desactivan**, no se borran.
- Un estudiante con notas o usuario vinculado no se puede eliminar.

## Cómo ejecutarlo
Requisitos: Java 21, Node 20+, MySQL 8 (ver `instalaciones.md`).

```bash
# Backend (puerto 8080). Crea la base gestor_notas sola.
cd backend
./mvnw spring-boot:run        # Windows: mvnw.cmd spring-boot:run

# Frontend (puerto 4200)
cd frontend
npm install
npm start
```
Abre http://localhost:4200.

Usuarios de prueba (solo desarrollo; se crean si la base está vacía):
`admin@gestor.com / Admin123*`, `docente@gestor.com / Docente123*`, `juan.perez@gestor.com / Estudiante123*`.

### Variables de entorno (backend)
`DB_URL`, `DB_USERNAME` (root), `DB_PASSWORD` (admin), `JWT_SECRET` (**cámbiala**, mín. 32 caracteres), `JWT_EXPIRATION_MINUTES` (120), `CORS_ALLOWED_ORIGIN` (http://localhost:4200), `SEED_ENABLED` (true; usa `false` en producción).

## Pruebas
```bash
cd backend && ./mvnw test                 # usa H2 en memoria, no necesita MySQL
cd frontend && npm run test:ci            # Karma + Chrome headless (define CHROME_BIN si hace falta)
```

## API
| Método y ruta | Quién |
|---|---|
| POST /api/auth/login | público |
| GET/POST/PUT/DELETE /api/usuarios | ADMIN |
| GET /api/estudiantes?q= · /api/asignaturas?soloActivas= | ADMIN, DOCENTE |
| POST/PUT/DELETE /api/estudiantes, /api/asignaturas | ADMIN |
| PUT /api/notas/registro (RF-07/RF-08: crea y edita) | DOCENTE |
| GET /api/notas, /api/notas/resumen, /api/notas/periodos | autenticado (el estudiante solo ve lo suyo) |
| GET /api/panel | ADMIN, DOCENTE |

## Estructura
```
backend/src/main/java/com/fullstack/fullstack/
  config/ controller/ dto/ exception/ model/entity/ repository/ security/ service/
frontend/src/app/
  core/ (modelos, auth, guards, interceptor, promedio) · services/ · layout/ · pages/
```
Los reportes (futura ampliación en el documento de Avance 1) no están implementados.
