# Gestor de Notas Académicas

Aplicación web para registrar y consultar calificaciones. Spring Boot 4 (Java 21) + MySQL + Angular 19.

## Cómo funciona (flujo de calificación)
1. **Asignaturas → grupos:** cada asignatura tiene grupos por periodo (Grupo 1, Grupo 2...), con un docente a cargo y estudiantes matriculados.
2. **Grupo → actividades:** el docente define las actividades del grupo con su porcentaje (Parcial 1 30 %, Parcial 2 30 %, Talleres 30 %, Proyecto 10 %).
3. **Actividad → rúbrica:** cada actividad tiene criterios con porcentaje (Teoría 50 %, Práctica 30 %, Sustentación 20 %). Se elige un estudiante y se escribe la nota de cada criterio.
4. **Progreso:** el sistema calcula la nota de cada actividad y lleva el avance hacia la nota final:
   - *Promedio*: nota ponderada de lo evaluado hasta ahora.
   - *Acumulado*: puntos ya ganados sobre 5.0 (lo no calificado cuenta 0).
   - *Avance*: porcentaje de la asignatura ya evaluado.
   Cuando todo está calificado y los porcentajes suman 100, promedio y acumulado son la nota final.

## Roles
| Rol | Puede |
|---|---|
| ADMIN | Gestionar usuarios, estudiantes, asignaturas, grupos y matrículas; **consultar** actividades, rúbricas y notas |
| DOCENTE | Ver sus grupos; **definir actividades y rúbricas, calificar, descargar las entregas y enviar las notas por correo** (solo en sus grupos) |
| ESTUDIANTE | Ver sus asignaturas y su seguimiento (nota por actividad y criterio, acumulado, promedio, avance, nota que necesita para aprobar) y **subir su entrega** (archivos) por actividad |

## Reglas de negocio
- Notas de 0.0 a 5.0 (máx. 2 decimales); se muestran en rojo si son menores que 3.0.
- Los porcentajes de las actividades de un grupo suman como máximo 100; los de los criterios de una rúbrica también. Los nombres no se repiten.
- Cada estudiante tiene documento, **código** y **correo** (documento y código únicos; se ven junto al nombre en las listas). Los estudiantes antiguos sin código/correo se completan al editarlos.
- Entregas: hasta 5 archivos por estudiante y actividad, máx. 10 MB cada uno (pdf, doc, docx, ppt, pptx, xls, xlsx, txt, zip, png, jpg, jpeg). Solo el estudiante matriculado sube y borra; descargan el dueño, el docente del grupo y el administrador.
- Documento de estudiante y código de asignatura únicos; un estudiante no se repite en un grupo.
- Usuarios y asignaturas se **desactivan**, no se borran. No se elimina un estudiante matriculado, ni un grupo con estudiantes o actividades, ni una actividad o criterio con calificaciones.
- Solo el docente a cargo del grupo califica; un estudiante solo ve lo suyo.

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

Usuarios de prueba (solo desarrollo; se crean si la base no tiene usuarios; el ejemplo académico de Ingeniería de Software se carga si no hay grupos):
`admin@gestor.com / Admin123*`, `docente@gestor.com / Docente123*`, `juan.perez@gestor.com / Estudiante123*`.

### Variables de entorno (backend)
`DB_URL`, `DB_USERNAME` (root), `DB_PASSWORD` (admin), `JWT_SECRET` (**cámbiala**, mín. 32 caracteres), `JWT_EXPIRATION_MINUTES` (120), `CORS_ALLOWED_ORIGIN` (http://localhost:4200), `SEED_ENABLED` (true; usa `false` en producción), `UPLOADS_DIR` (carpeta de entregas, por defecto `uploads` junto al backend).

### Envío de notas por correo (Brevo)
El botón "Enviar notas por correo" (pantalla de actividades del grupo, solo docente; por estudiante o para todo el grupo) usa la API HTTPS de [Brevo](https://www.brevo.com) (así funciona también en Render gratis, que bloquea SMTP). Escribe al **correo registrado en cada estudiante**. Define estas variables antes de arrancar el backend; sin ellas el botón responde "El envío de correos no está configurado":
```
BREVO_API_KEY=tu-api-key-de-brevo
MAIL_FROM=remitente-verificado-en-brevo@dominio.com
MAIL_FROM_NAME=Gestor de Notas      (opcional)
```
En PowerShell: `$env:BREVO_API_KEY="..."` (y así cada una) y luego `mvnw.cmd spring-boot:run` en la misma ventana.

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
| GET /api/grupos | autenticado (cada rol ve los suyos) |
| POST/PUT/DELETE /api/grupos · POST/DELETE /api/grupos/{id}/estudiantes/{estudianteId} (matrícula) | ADMIN |
| GET /api/grupos/{id}/estudiantes · /progreso | ADMIN, DOCENTE del grupo |
| GET /api/grupos/{id}/actividades · /resumen?estudianteId= | admin, docente del grupo, estudiante matriculado (solo lo suyo) |
| POST /api/grupos/{id}/actividades · PUT/DELETE /api/actividades/{id} · PUT /api/actividades/{id}/rubrica | DOCENTE del grupo |
| GET /api/actividades/{id} · /estudiantes · /rubrica?estudianteId= | admin, docente del grupo (el estudiante solo su rúbrica) |
| PUT /api/actividades/{id}/estudiantes/{estudianteId}/calificaciones | DOCENTE del grupo |
| POST /api/actividades/{id}/entregas (multipart `archivo`) · DELETE /api/entregas/{id} | ESTUDIANTE matriculado (solo lo suyo) |
| GET /api/actividades/{id}/entregas?estudianteId= · GET /api/entregas/{id}/descargar | dueño, docente del grupo, admin |
| POST /api/grupos/{id}/correos?estudianteId= | DOCENTE del grupo (sin estudianteId = todo el grupo) |
| GET /api/panel | ADMIN, DOCENTE |

## Estructura
```
backend/src/main/java/com/fullstack/fullstack/
  config/ controller/ dto/ exception/ model/entity/ repository/ security/ service/
frontend/src/app/
  core/ (modelos, auth, guards, interceptor, promedio) · services/ · layout/ · pages/ (notas, mis-notas, grupos, ...)
```
Los reportes (futura ampliación en el documento de Avance 1) no están implementados. Si ya tenías la base `gestor_notas` de una versión anterior, la tabla `notas` queda sin uso (no se borra).
