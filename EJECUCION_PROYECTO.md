# Ejecución rápida

1. MySQL 8 encendido (usuario `root`; la clave por defecto del proyecto es `admin`, o define `DB_PASSWORD`).
2. Backend: `cd backend && ./mvnw spring-boot:run` (Windows: `mvnw.cmd spring-boot:run`). Debe terminar con "Started FullstackApplication" en el puerto 8080.
3. Frontend: `cd frontend && npm install && npm start`, y abre http://localhost:4200.
4. Entra con `docente@gestor.com / Docente123*`, abre "Calificar" → Ingeniería de Software → Grupo 1 → Parcial 1 y selecciona a Juan Pérez (nota 4.60).

Problemas frecuentes: `JAVA_HOME` no definido → instala Java 21; "Access denied" de MySQL → revisa la clave; el login falla con "No se pudo conectar" → el backend no está encendido.

## Novedades (código/correo, entregas y correos)
- Estudiantes ahora tienen **código** y **correo**. Los 3 de ejemplo se completan solos; los que creaste antes quedan sin código/correo hasta que los edites (Estudiantes → Editar).
- Entra como `juan.perez@gestor.com / Estudiante123*` → Mis notas → Ingeniería de Software → sube un archivo en "Mi entrega". Luego como docente: Calificar → actividad → elige a Juan → "Entregas del estudiante" → Descargar.
- Para enviar notas por correo configura Brevo como se explica en el README (`BREVO_API_KEY` y `MAIL_FROM`); si no, el botón avisa que falta configurar.
- Los archivos subidos se guardan en `backend/uploads` (no se suben a git).
