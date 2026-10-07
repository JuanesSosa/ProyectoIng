# EJECUCION DEL PROYECTO - PASO A PASO

Esta guia explica como levantar y probar todo el proyecto `fullstack` (backend + frontend) desde cero.

---

## 1) Antes de empezar

Verifica que tengas instalado:

- Java 21
- Maven
- MySQL Server (activo)
- Node.js + npm
- Angular CLI

Comandos de verificacion:

```powershell
java -version
mvn -v
mysql --version
node -v
npm -v
ng version
```

---

## 2) Iniciar MySQL y preparar base de datos

1. Asegurate de que el servicio de MySQL este corriendo.
2. Abre MySQL Workbench.
3. Ejecuta este script SQL:

```sql
CREATE DATABASE IF NOT EXISTS fullstack_ecommerce
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
```

4. Verifica que exista:

```sql
SHOW DATABASES;
```

---

## 3) Revisar configuracion del backend

Archivo: `backend/src/main/resources/application.properties`

Debes tener algo como:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/fullstack_ecommerce
spring.datasource.username=root
spring.datasource.password=admin
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
```

> Cambia `spring.datasource.password` si tu clave de MySQL es otra.

---

## 4) Ejecutar backend (Spring Boot)

Abre una terminal en la raiz del proyecto y ejecuta:

```powershell
cd "C:\Users\johan\Documents\SALLE\2026-1\CONSTRUCCION DE SOFTWARE\fullstack\backend"
mvn clean compile
mvn spring-boot:run
```

Cuando inicie bien, deberias ver que levanta en puerto `8080`.

Prueba rapida en navegador:

- `http://localhost:8080/api/products`

Si todo va bien, deberias ver `[]` o una lista JSON de productos.

---

## 5) Ejecutar frontend (Angular)

Con el backend corriendo, abre una **segunda terminal**:

```powershell
cd "C:\Users\johan\Documents\SALLE\2026-1\CONSTRUCCION DE SOFTWARE\fullstack\frontend"
npm install
ng serve
```

Luego abre:

- `http://localhost:4200`

---

## 6) Probar el flujo completo

1. En la pagina de Angular, llena el formulario:
   - Nombre
   - Descripcion
   - Precio
   - Stock
2. Clic en **Guardar producto**.
3. Verifica que aparezca en la lista.

---

## 7) Verificar en base de datos

En MySQL Workbench, ejecuta:

```sql
USE fullstack_ecommerce;
SELECT * FROM products;
```

Si ves el registro, el flujo completo funciona:

Frontend -> Backend -> MySQL.

---

## 8) Comandos rapidos de apagado

Para detener servidores:

- En cada terminal presiona `Ctrl + C`.

---

## 9) Errores comunes y solucion

### Error `release version 25 not supported`

En `backend/pom.xml` usa:

```xml
<java.version>21</java.version>
```

### Error `MySQL8Dialect` no encontrado

En `application.properties` usa:

```properties
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
```

### El frontend muestra "No se pudo crear el producto"

Revisa:

1. Backend realmente corriendo en `:8080`.
2. MySQL activo.
3. Credenciales correctas en `application.properties`.
4. Consola del backend para ver el error exacto.

---

## 10) Resumen ultra rapido

1. Inicia MySQL.
2. Levanta backend (`mvn spring-boot:run`).
3. Levanta frontend (`ng serve`).
4. Abre `http://localhost:4200`.
5. Crea producto y verifica lista.

Fin.

