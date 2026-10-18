# INSTALACIONES - PROYECTO FULLSTACK

Guia completa de instalaciones para dejar listo el entorno de desarrollo de este proyecto en **Windows**.

---

## 1) Herramientas a instalar

1. Java JDK 21  
2. Maven 3.9+  
3. MySQL Server 8.x  
4. MySQL Workbench  
5. Node.js LTS (20+)  
6. Angular CLI (19)  
7. Visual Studio Code  
8. (Opcional) Git

---

## 2) Java JDK 21

### Instalar

1. Ve a `https://adoptium.net/`.
2. Descarga **Temurin JDK 21 (x64 Windows)**.
3. Ejecuta el instalador y deja marcada la opcion de agregar Java al `PATH`.

### Verificar

```powershell
java -version
javac -version
```

Debes ver version **21.x**.

---

## 3) Maven 3.9+

> Si usaras `mvnw` (wrapper) no es obligatorio instalar Maven global, pero recomendado.

### Instalar

1. Descarga Maven desde `https://maven.apache.org/download.cgi`.
2. Extrae en una ruta simple, ejemplo: `C:\apache\maven\apache-maven-3.9.9`.
3. Configura variables del sistema:
   - `MAVEN_HOME = C:\apache\maven\apache-maven-3.9.9`
   - Agrega `%MAVEN_HOME%\bin` al `Path`.
4. Cierra y abre terminal.

### Verificar

```powershell
mvn -v
```

Debes confirmar:
- Maven 3.9.x
- Java version 21 (muy importante)

---

## 4) MySQL Server 8.x

### Instalar

1. Ve a `https://dev.mysql.com/downloads/installer/`.
2. Descarga **MySQL Installer for Windows**.
3. Elige tipo **Developer Default**.
4. Durante configuracion:
   - Puerto: `3306`
   - Usuario: `root`
   - Define contraseña (ejemplo: `admin`)
5. Finaliza instalacion.

### Verificar servicio

1. Abre `services.msc`.
2. Busca `MySQL80`.
3. Debe estar en estado **Running**.

### Verificar por terminal

```powershell
mysql --version
```

---

## 5) MySQL Workbench

Si instalaste `Developer Default`, ya queda instalado.

### Verificar

1. Abre **MySQL Workbench**.
2. Crea o abre conexion local:
   - Host: `localhost`
   - Port: `3306`
   - User: `root`
3. Ingresa la contraseña y conecta.

---

## 6) Crear base de datos del proyecto

En MySQL Workbench, abre una pestaña SQL y ejecuta:

```sql
CREATE DATABASE IF NOT EXISTS gestor_notas
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
```

Verifica:

```sql
SHOW DATABASES;
```

Debe aparecer `gestor_notas`.

---

## 7) Node.js LTS (20+)

### Instalar

1. Ve a `https://nodejs.org/`.
2. Descarga e instala la version **LTS**.
3. Deja marcada la opcion de agregar al `PATH`.

### Verificar

```powershell
node -v
npm -v
```

---

## 8) Angular CLI 19

### Instalar globalmente

```powershell
npm install -g @angular/cli
```

### Verificar

```powershell
ng version
```

Debes ver Angular CLI instalado.

---

## 9) Visual Studio Code

### Instalar

1. Ve a `https://code.visualstudio.com/`.
2. Descarga para Windows.
3. En instalador, marca:
   - Add to PATH
   - Open with Code (opcional)

### Extensiones recomendadas

- Extension Pack for Java
- Angular Language Service
- ESLint
- Prettier

---

## 10) (Opcional) Git

### Instalar

1. Descarga desde `https://git-scm.com/download/win`.
2. Instala con opciones por defecto.

### Verificar

```powershell
git --version
```

---

## 11) Configuracion de proyecto backend

Archivo: `backend/src/main/resources/application.properties`

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/gestor_notas
spring.datasource.username=root
spring.datasource.password=admin
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
```

Si tu contraseña no es `admin`, cambiala.

---

## 12) Verificacion final de instalaciones

Ejecuta todo esto:

```powershell
java -version
mvn -v
mysql --version
node -v
npm -v
ng version
code --version
```

Si todo responde con version, ya quedaste listo.

---

## 13) Primer arranque del proyecto

### Backend

```powershell
cd "C:\Users\johan\Documents\SALLE\2026-1\CONSTRUCCION DE SOFTWARE\fullstack\backend"
mvn clean compile
mvn spring-boot:run
```

### Frontend

```powershell
cd "C:\Users\johan\Documents\SALLE\2026-1\CONSTRUCCION DE SOFTWARE\fullstack\frontend"
npm install
ng serve
```

---

## 14) URLs esperadas

- Frontend: `http://localhost:4200`
- Backend API: `http://localhost:8080/api/auth/login (POST)`

---

## 15) Errores comunes rapidos

### `release version 25 not supported`

- En `backend/pom.xml`, usar:

```xml
<java.version>21</java.version>
```

### `MySQL8Dialect` no encontrado

- Usar en `application.properties`:

```properties
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
```

### Puerto ocupado

```powershell
netstat -ano | findstr :8080
netstat -ano | findstr :4200
taskkill /PID <PID> /F
```

---

Fin.

