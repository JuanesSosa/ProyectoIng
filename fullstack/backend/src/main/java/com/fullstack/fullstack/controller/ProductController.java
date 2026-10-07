package com.fullstack.fullstack.controller;

import com.fullstack.fullstack.dto.ProductDto;
import com.fullstack.fullstack.service.ProductService;
import jakarta.validation.Valid;
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

/**
 * Controlador REST para el recurso Producto.
 * Todas las rutas tienen el prefijo /api/products.
 * Recibe peticiones HTTP del frontend y delega la lógica en ProductService.
 */
@RestController  // Indica que esta clase es un controlador REST; el retorno de los métodos se serializa a JSON.
@RequestMapping("/api/products")  // Prefijo común para todas las rutas de este controlador (base path).
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /** GET /api/products - Devuelve la lista de todos los productos. */
    @GetMapping  // Mapea peticiones HTTP GET a este método (ruta final: /api/products).
    public List<ProductDto> getAll() {
        return productService.findAll();
    }

    /** GET /api/products/{id} - Devuelve un producto por su id (404 si no existe). */
    @GetMapping("/{id}")  // GET /api/products/1 → el "1" se captura en el parámetro id.
    public ProductDto getById(@PathVariable Long id) {  // @PathVariable: extrae {id} de la URL y lo inyecta aquí.
        return productService.findById(id);
    }

    /**
     * POST /api/products - Crea un nuevo producto.
     * Devuelve 201 Created con la cabecera Location y el cuerpo con el producto creado (incluye id).
     */
    @PostMapping  // Mapea peticiones HTTP POST a este método (ruta: /api/products).
    public ResponseEntity<ProductDto> create(@Valid @RequestBody ProductDto productDto) {  // @RequestBody: el cuerpo JSON se deserializa a ProductDto.
        ProductDto created = productService.create(productDto);
        return ResponseEntity
                .created(URI.create("/api/products/" + created.getId()))
                .body(created);
    }

    /** PUT /api/products/{id} - Actualiza un producto existente. */
    @PutMapping("/{id}")  // Mapea HTTP PUT /api/products/1 a este método.
    public ProductDto update(@PathVariable Long id, @Valid @RequestBody ProductDto productDto) {  // id desde la URL; cuerpo JSON → productDto.
        return productService.update(id, productDto);
    }

    /** DELETE /api/products/{id} - Elimina un producto. Devuelve 204 No Content. */
    @DeleteMapping("/{id}")  // Mapea HTTP DELETE /api/products/1 a este método.
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
