## Tutorial completo paso a paso – Backend mini ecommerce

> **Nota (actualización posterior):** el código actual del backend añade validación (`@Valid` + Bean Validation en `ProductDto`), `price` como `BigDecimal`, manejo de errores 400/500 en `GlobalExceptionHandler`, configuración por variables de entorno y tests con H2 / MockMvc. Revisa `GUIA_COMPLETA.md`, sección "Cambios posteriores", y el código fuente como referencia vigente.

Este documento está pensado para **alguien que jamás ha programado**.  
Vamos a ir **muy despacio**, para que puedas crear todo el backend tú solo.

La idea es que al final puedas:

- Crear un proyecto backend con Spring Boot desde cero en  
  la página [Spring Initializr](https://start.spring.io/index.html).
- Conectarlo a una base de datos MySQL.
- Tener un mini backend de ecommerce que guarda **productos** en la base de datos.
- Probarlo con Postman / Thunder Client sin depender todavía del frontend.

Lee cada paso con calma y no te saltes ninguno.

---

## 1. ¿Qué es el backend? (explicación muy sencilla)

- **Frontend**: lo que ves en la pantalla (página web, botones, formularios, etc.).
- **Backend**: el programa que está “detrás”, en el servidor.
  - Recibe peticiones a URLs como `http://localhost:8080/api/products`.
  - Habla con la **base de datos** (MySQL) para guardar y leer datos.
  - Devuelve datos (generalmente en formato **JSON**) al frontend.

En este tutorial:

- Usaremos **Spring Boot** (framework de Java) para el backend.
- Usaremos **MySQL** para la base de datos.

---

## 2. Requisitos antes de empezar

Antes de hacer nada, asegúrate de tener instalado:

- **Java JDK** (**versión 25**, que es la que vamos a usar).
- **MySQL** y **MySQL Workbench**.
- **Visual Studio Code (VS Code)** con:
  - Extensión **Extension Pack for Java** (o al menos las extensiones básicas de Java).
  - Extensión **Thunder Client** (la instalaremos más adelante).

Si algo de esto no lo tienes, pídele a tu profesor o a un compañero que te ayude a instalarlo.

---

## 3. Crear el proyecto en Spring Initializr

1. Abre tu navegador y entra a:  
   [https://start.spring.io/index.html](https://start.spring.io/index.html)

2. Rellena los datos principales (pueden variar un poco, pero esta es una buena configuración):

   - **Project**: Maven Project  
   - **Language**: Java  
   - **Spring Boot**: elige una versión estable (por ejemplo 3.x o 4.x, la que te sugiera la página).  
   - **Project Metadata**:
     - **Group**: `com.fullstack`
     - **Artifact**: `fullstack`
     - **Name**: `fullstack`
     - **Description**: `Mini ecommerce backend`
     - **Package name**: se rellenará solo, algo como `com.fullstack.fullstack`
   - **Packaging**: Jar  
   - **Java**: **25** (elige Java 25 en el desplegable).

3. En la parte de **Dependencies** (dependencias), añade:

   - `Spring Web` (o `Spring Web MVC`)
   - `Spring Data JPA`
   - `MySQL Driver`
   - Opcional: `Lombok` (para reducir código repetitivo)

4. Haz clic en **GENERATE**.

5. Se descargará un archivo `.zip` (por ejemplo `fullstack.zip`).

6. Descomprime (extrae) ese `.zip` en una carpeta donde quieras trabajar, por ejemplo:

   - `C:\Users\TU_USUARIO\Documents\fullstack`

   Dentro verás una estructura parecida a:

   - `pom.xml`
   - `src/main/java/...`
   - `src/main/resources/...`

Este es tu proyecto backend base.

---

## 4. Abrir el proyecto en Visual Studio Code

En este tutorial vamos a trabajar **solo con VS Code**.

1. Abre **Visual Studio Code**.
2. Ve a **File → Open Folder…** y selecciona la carpeta del proyecto que descomprimiste (la que contiene el archivo `pom.xml`).
3. Si VS Code te pregunta si quieres **confiar en los autores** del proyecto, acepta.
4. Si no tienes las extensiones de Java instaladas, VS Code mostrará un mensaje sugiriendo instalarlas:
   - Acepta y deja que instale lo que recomienda (Java, Maven, etc.).
5. Espera a que termine de descargar dependencias de Maven (puede tardar un poco la primera vez).

Cuando todo esté listo, en el explorador de archivos deberías ver algo como:

- `src/main/java/com/fullstack/fullstack/FullstackApplication.java`
- `src/main/resources/application.properties`

---

## 5. Crear la base de datos en MySQL Workbench

Ahora vamos a crear la base de datos donde se guardarán los productos.

1. Abre **MySQL Workbench**.
2. Conéctate a tu servidor MySQL (haciendo doble clic sobre tu conexión).
3. Abre una pestaña nueva de consulta SQL.
4. Escribe y ejecuta (botón del rayo) este script:

   ```sql
   CREATE DATABASE IF NOT EXISTS fullstack_ecommerce
       CHARACTER SET utf8mb4
       COLLATE utf8mb4_unicode_ci;

   USE fullstack_ecommerce;
   ```

Con esto creas una base de datos vacía llamada `fullstack_ecommerce`.  
No hace falta crear la tabla `products` a mano, Spring la generará a partir del código.

---

## 6. Configurar la conexión a la base de datos (application.properties)

En tu proyecto, ve al archivo:

- `src/main/resources/application.properties`

Y deja su contenido así (ajusta el usuario y contraseña a los tuyos reales):

```properties
spring.application.name=fullstack

spring.datasource.url=jdbc:mysql://localhost:3306/fullstack_ecommerce
spring.datasource.username=root
spring.datasource.password=admin
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect
```

Explicación sencilla:

- `spring.datasource.url`: dirección de la base de datos (`localhost` = tu computadora).
- `spring.datasource.username`: usuario de MySQL (por ejemplo `root`).
- `spring.datasource.password`: contraseña de MySQL (por ejemplo `admin`).
- `ddl-auto=update`: crea/actualiza las tablas según las clases Java.
- `show-sql=true`: mostrará en la consola las consultas SQL que se ejecutan.

Guarda el archivo.

---

## 7. Crear la estructura de carpetas por capas (versión muy simple)

Vamos a crear **carpetas** dentro de `src/main/java/com/fullstack/fullstack` para ordenar el código.

Carpetas que necesitamos:

- `controller`
- `service`
- `service/impl`
- `repository`
- `model`
- `model/entity`
- `dto`
- `mapper`
- `exception`
- `config` (la podemos dejar vacía por ahora)

Pasos (muy sencillos) en VS Code:

1. En el panel de archivos, abre la ruta `src/main/java/com/fullstack/fullstack`.
2. Haz **clic derecho** sobre la carpeta `fullstack`.
3. Elige **New Folder** (Nueva carpeta).
4. Escribe el nombre de la carpeta que quieras crear (por ejemplo `controller`) y pulsa **Enter**.
5. Repite lo mismo para crear el resto de carpetas de la lista (service, repository, dto, etc.).

Más adelante, cuando el tutorial lo indique, **dentro de cada carpeta** harás:

1. Clic derecho sobre la carpeta (por ejemplo `controller`).
2. Opción **New File**.
3. Escribes el nombre del archivo que te diga el tutorial (por ejemplo `ProductController.java`) y pegas el código que se muestra.

No te preocupes por entender todo ahora. Por el momento, solo asegúrate de que las carpetas existen como en la imagen del profesor.

---

## 8. Crear la entidad Product (lo que se guarda en la BD)

La entidad representa una tabla de la base de datos.

1. En el paquete `com.fullstack.fullstack.model.entity`, crea una nueva clase Java:
   - Nombre: `Product`

2. Dentro de `Product.java`, escribe este contenido:

```java
package com.fullstack.fullstack.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String description;

    private BigDecimal price;

    private Integer stock;
}
```

Ideas clave (sin entrar en detalles técnicos):

- `@Entity` y `@Table` dicen a JPA que esta clase representa una tabla llamada `products`.
- `@Id` y `@GeneratedValue` indican que `id` es la clave primaria y se genera automáticamente.
- Los campos `name`, `description`, `price`, `stock` serán columnas de la tabla.

Guarda el archivo.

---

## 9. Crear el repositorio ProductRepository (acceso a la base de datos)

El repositorio es una interfaz que nos da métodos para guardar, buscar y borrar productos sin escribir SQL a mano.

1. En el paquete `com.fullstack.fullstack.repository`, crea una interfaz:
   - Nombre: `ProductRepository`

2. Escribe este contenido:

```java
package com.fullstack.fullstack.repository;

import com.fullstack.fullstack.model.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
}
```

Con esto, ya tienes métodos como:

- `findAll()`
- `findById(id)`
- `save(product)`
- `deleteById(id)`

sin tener que implementarlos tú.

---

## 10. Crear el DTO ProductDto (lo que viaja por la API)

El DTO es el objeto que se envía y recibe por la API.  
Normalmente se parece a la entidad, pero nos permite separar la capa de base de datos de lo que mostramos fuera.

1. En `com.fullstack.fullstack.dto`, crea una clase:
   - Nombre: `ProductDto`

2. Escribe:

```java
package com.fullstack.fullstack.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDto {

    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stock;
}
```

---

## 11. Crear el mapper ProductMapper (traduce entre entidad y DTO)

Este mapper convierte de `Product` a `ProductDto` y al revés.

1. En `com.fullstack.fullstack.mapper`, crea una clase:
   - Nombre: `ProductMapper`

2. Escribe:

```java
package com.fullstack.fullstack.mapper;

import com.fullstack.fullstack.dto.ProductDto;
import com.fullstack.fullstack.model.entity.Product;

public final class ProductMapper {

    private ProductMapper() {
        // Clase de utilidad: no se instancia
    }

    public static ProductDto toDto(Product entity) {
        if (entity == null) {
            return null;
        }
        return ProductDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .price(entity.getPrice())
                .stock(entity.getStock())
                .build();
    }

    public static Product toEntity(ProductDto dto) {
        if (dto == null) {
            return null;
        }
        return Product.builder()
                .id(dto.getId())
                .name(dto.getName())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .stock(dto.getStock())
                .build();
    }

    public static void updateEntity(Product entity, ProductDto dto) {
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setPrice(dto.getPrice());
        entity.setStock(dto.getStock());
    }
}
```

No necesitas entender cada línea; piensa en esto como un “traductor” entre dos tipos de objetos.

---

## 12. Crear una excepción simple para recursos no encontrados

Cuando se pida un producto que no existe, queremos devolver un error 404 claro.

1. En `com.fullstack.fullstack.exception`, crea una clase:
   - Nombre: `ResourceNotFoundException`

2. Escribe:

```java
package com.fullstack.fullstack.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
```

También crearemos un manejador global:

1. En el mismo paquete `exception`, crea otra clase:
   - Nombre: `GlobalExceptionHandler`

2. Contenido:

```java
package com.fullstack.fullstack.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(ResourceNotFoundException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", HttpStatus.NOT_FOUND.value());
        body.put("error", "Not Found");
        body.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }
}
```

---

## 13. Crear la interfaz ProductService (lógica de negocio)

El servicio define qué operaciones hacemos con los productos.

1. En `com.fullstack.fullstack.service`, crea una interfaz:
   - Nombre: `ProductService`

2. Escribe:

```java
package com.fullstack.fullstack.service;

import com.fullstack.fullstack.dto.ProductDto;

import java.util.List;

public interface ProductService {

    List<ProductDto> findAll();

    ProductDto findById(Long id);

    ProductDto create(ProductDto productDto);

    ProductDto update(Long id, ProductDto productDto);

    void delete(Long id);
}
```

---

## 14. Implementar el servicio ProductServiceImpl

1. En `com.fullstack.fullstack.service.impl`, crea una clase:
   - Nombre: `ProductServiceImpl`

2. Escribe:

```java
package com.fullstack.fullstack.service.impl;

import com.fullstack.fullstack.dto.ProductDto;
import com.fullstack.fullstack.exception.ResourceNotFoundException;
import com.fullstack.fullstack.mapper.ProductMapper;
import com.fullstack.fullstack.model.entity.Product;
import com.fullstack.fullstack.repository.ProductRepository;
import com.fullstack.fullstack.service.ProductService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDto> findAll() {
        return productRepository.findAll()
                .stream()
                .map(ProductMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDto findById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id " + id));
        return ProductMapper.toDto(product);
    }

    @Override
    public ProductDto create(ProductDto productDto) {
        Product product = ProductMapper.toEntity(productDto);
        product.setId(null); // nuevo producto
        Product saved = productRepository.save(product);
        return ProductMapper.toDto(saved);
    }

    @Override
    public ProductDto update(Long id, ProductDto productDto) {
        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id " + id));
        ProductMapper.updateEntity(existing, productDto);
        Product saved = productRepository.save(existing);
        return ProductMapper.toDto(saved);
    }

    @Override
    public void delete(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product not found with id " + id);
        }
        productRepository.deleteById(id);
    }
}
```

Piensa en este servicio como el “cerebro” que decide qué hacer con los productos.

---

## 15. Crear el controlador ProductController (la puerta de entrada)

El controlador define las URLs de la API y conecta las peticiones con el servicio.

1. En `com.fullstack.fullstack.controller`, crea una clase:
   - Nombre: `ProductController`

2. Escribe:

```java
package com.fullstack.fullstack.controller;

import com.fullstack.fullstack.dto.ProductDto;
import com.fullstack.fullstack.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public List<ProductDto> getAll() {
        return productService.findAll();
    }

    @GetMapping("/{id}")
    public ProductDto getById(@PathVariable Long id) {
        return productService.findById(id);
    }

    @PostMapping
    public ResponseEntity<ProductDto> create(@RequestBody ProductDto productDto) {
        ProductDto created = productService.create(productDto);
        return ResponseEntity
                .created(URI.create("/api/products/" + created.getId()))
                .body(created);
    }

    @PutMapping("/{id}")
    public ProductDto update(@PathVariable Long id, @RequestBody ProductDto productDto) {
        return productService.update(id, productDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
```

Ahora ya tienes las rutas:

- `GET /api/products` – lista todos los productos.
- `GET /api/products/{id}` – obtiene un producto por id.
- `POST /api/products` – crea un producto.
- `PUT /api/products/{id}` – actualiza un producto existente.
- `DELETE /api/products/{id}` – borra un producto.

---

## 16. Arrancar el backend

### Opción A: desde VS Code (recomendado)

1. En VS Code, abre el archivo `FullstackApplication.java` (está en `src/main/java/com/fullstack/fullstack`).
2. Arriba del método `main` o en la esquina superior derecha del editor deberías ver un botón de **Run** (o un triángulo ▶).
3. Haz clic en ese botón para ejecutar la aplicación.
4. En la parte inferior se abrirá un panel de **terminal de Java** donde verás los mensajes de arranque.
5. Espera hasta que aparezca algo parecido a:
   - `Started FullstackApplication`  
   y no haya errores en rojo.

### Opción B: desde la terminal integrada (Maven)

1. En VS Code, abre la **Terminal integrada**:
   - Menú **View → Terminal** (o atajo `Ctrl + ñ` / `Ctrl + '` según teclado).
2. Asegúrate de estar en la carpeta donde está el `pom.xml`:

   ```bash
   cd ruta/hasta/tu/proyecto/fullstack
   ```

   (o si creaste una estructura `fullstack/backend`, entra en `backend`).

3. Ejecuta (en Windows):

   ```bash
   mvnw.cmd spring-boot:run
   ```

   o (en otros sistemas):

   ```bash
   ./mvnw spring-boot:run
   ```

4. Espera el mensaje de que la aplicación se ha iniciado.

Si aparece un error, léelo con calma; casi siempre indica si hay problema de conexión con la base de datos (usuario/contraseña mal, base no creada, etc.).

---

## 17. Probar la API con Postman o Thunder Client

Con el backend arrancado, vamos a probar cada operación.

### 17.1. Crear un producto (POST)

1. Abre Postman o Thunder Client.
2. Crea una nueva petición:
   - Método: **POST**
   - URL: `http://localhost:8080/api/products`
3. Ve a la pestaña **Body**:
   - Marca **raw**.
   - Selecciona **JSON**.
4. Escribe este JSON:

   ```json
   {
     "name": "Camiseta roja",
     "description": "Talla M",
     "price": 15.99,
     "stock": 5
   }
   ```

5. Pulsa **Send**.

Resultados esperados:

- Respuesta con un JSON similar, pero con un campo `id` (por ejemplo `1`).
- En la consola del backend verás SQL ejecutándose.
- En MySQL Workbench, si haces:

  ```sql
  USE fullstack_ecommerce;
  SELECT * FROM products;
  ```

  deberías ver tu producto.

### 17.2. Listar productos (GET)

1. Nueva petición.
2. Método: **GET**
3. URL: `http://localhost:8080/api/products`
4. Pulsa **Send**.

Verás algo como:

```json
[
  {
    "id": 1,
    "name": "Camiseta roja",
    "description": "Talla M",
    "price": 15.99,
    "stock": 5
  }
]
```

### 17.3. Ver un producto concreto (GET /{id})

1. Método: **GET**
2. URL: `http://localhost:8080/api/products/1`
3. Pulsa **Send**.

Te devolverá solo el producto con id 1.

### 17.4. Actualizar un producto (PUT)

1. Método: **PUT**
2. URL: `http://localhost:8080/api/products/1`
3. Body → **raw** → **JSON**:

   ```json
   {
     "name": "Camiseta roja actualizada",
     "description": "Talla L",
     "price": 17.99,
     "stock": 8
   }
   ```

4. Pulsa **Send**.

Verás el producto ya actualizado en la respuesta y también en la base de datos.

### 17.5. Borrar un producto (DELETE)

1. Método: **DELETE**
2. URL: `http://localhost:8080/api/products/1`
3. Pulsa **Send**.

La respuesta será un **código 204 No Content** (sin cuerpo).  
Si haces de nuevo el `SELECT` en MySQL, verás que ya no está.

---

## 18. Cómo probarlo usando Thunder Client (en VS Code)

Si usas **VS Code**, una forma muy cómoda de probar el backend es con la extensión **Thunder Client**.

### 18.1. Instalar Thunder Client

1. Abre **VS Code**.
2. Ve a la pestaña de extensiones (icono de cuadritos en la barra lateral izquierda).
3. En la barra de búsqueda escribe: `Thunder Client`.
4. Busca la extensión llamada **Thunder Client** (autor: Ranga Vadhineni).
5. Pulsa el botón **Install**.

Cuando termine de instalarse, verás un icono de un rayo en la barra lateral izquierda.

### 18.2. Crear una colección para tu backend

1. Haz clic en el icono de **Thunder Client** (rayo).
2. Pulsa el botón **New Collection**.
3. Ponle un nombre, por ejemplo: `Mini Ecommerce Backend`.
4. Dentro de esa colección crearemos las diferentes peticiones (POST, GET, PUT, DELETE).

### 18.3. Crear la petición POST /api/products

1. En Thunder Client, dentro de tu colección, haz clic en **New Request**.
2. Ponle un nombre, por ejemplo: `Crear producto`.
3. Configura:
   - Método: en el desplegable, elige **POST**.
   - URL: `http://localhost:8080/api/products`
4. Ve a la pestaña **Body** (en Thunder Client).
5. Marca la opción **JSON**.
6. En el cuadro de texto pega este JSON:

   ```json
   {
     "name": "Camiseta roja",
     "description": "Talla M",
     "price": 15.99,
     "stock": 5
   }
   ```

7. Pulsa el botón **Send**.

En la parte derecha verás la **respuesta** del backend.  
Deberías ver un JSON similar, pero con un `id` añadido:

```json
{
  "id": 1,
  "name": "Camiseta roja",
  "description": "Talla M",
  "price": 15.99,
  "stock": 5
}
```

### 18.4. Crear la petición GET /api/products

1. Crea otra request en la misma colección.
2. Nombre: `Listar productos`.
3. Método: **GET**.
4. URL: `http://localhost:8080/api/products`
5. No necesitas Body para GET.
6. Pulsa **Send**.

Verás un arreglo de productos en el panel de respuesta.

### 18.5. Crear la petición GET /api/products/{id}

1. Nueva request.
2. Nombre: `Ver producto por id`.
3. Método: **GET**.
4. URL: `http://localhost:8080/api/products/1` (o el id que obtuviste).
5. Pulsa **Send**.

Deberías ver solo ese producto.

### 18.6. Crear la petición PUT /api/products/{id}

1. Nueva request.
2. Nombre: `Actualizar producto`.
3. Método: **PUT**.
4. URL: `http://localhost:8080/api/products/1`.
5. Pestaña **Body** → **JSON**.
6. Escribe, por ejemplo:

   ```json
   {
     "name": "Camiseta roja actualizada",
     "description": "Talla L",
     "price": 17.99,
     "stock": 8
   }
   ```

7. Pulsa **Send**.

Verás en la respuesta el producto modificado.  
Si haces GET otra vez, verás los nuevos valores.

### 18.7. Crear la petición DELETE /api/products/{id}

1. Nueva request.
2. Nombre: `Borrar producto`.
3. Método: **DELETE**.
4. URL: `http://localhost:8080/api/products/1`.
5. Pulsa **Send**.

La respuesta será un código `204` sin cuerpo.  
Si vuelves a hacer `GET /api/products/1`, verás el error de “no encontrado”.

Con esta colección de Thunder Client tendrás **todas las operaciones CRUD** listas para ejecutar con un clic.

---

## 19. Manejo de errores (producto no encontrado)

Si intentas acceder a un producto que no existe:

- Por ejemplo: `GET http://localhost:8080/api/products/9999`

El backend lanzará la excepción `ResourceNotFoundException` y el manejador la convertirá en una respuesta JSON:

```json
{
  "timestamp": "2026-02-04T10:00:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Product not found with id 9999"
}
```

Eso te indica claramente que ese producto no existe.

---

## 20. Resumen final (qué debes haber logrado)

Si seguiste todos los pasos:

- Creaste un proyecto Spring Boot desde [Spring Initializr](https://start.spring.io/index.html).
- Configuraste la conexión a MySQL (`fullstack_ecommerce`).
- Implementaste:
  - Una entidad `Product`.
  - Un repositorio `ProductRepository`.
  - Un DTO `ProductDto`.
  - Un mapper `ProductMapper`.
  - Un servicio `ProductService` + `ProductServiceImpl`.
  - Un controlador `ProductController`.
  - Manejo básico de errores.
- Probaste la API completa (crear, listar, ver, actualizar, borrar) con Postman/Thunder y verificaste los datos en MySQL Workbench.

Si alguna parte no funciona, revisa el paso correspondiente con calma.  
La idea es que, leyendo **solo este archivo .md**, puedas reconstruir todo el backend sin necesidad de experiencia previa en programación.

