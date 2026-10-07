package com.fullstack.fullstack.controller;

import com.fullstack.fullstack.dto.ProductDto;
import com.fullstack.fullstack.exception.GlobalExceptionHandler;
import com.fullstack.fullstack.exception.ResourceNotFoundException;
import com.fullstack.fullstack.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas del controller con MockMvc en modo "standalone": sin levantar Spring ni la base de datos.
 * Verifican códigos HTTP, validación (@Valid) y el formato de errores del GlobalExceptionHandler.
 */
@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    @Mock
    private ProductService service;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(new ProductController(service))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private ProductDto dto(Long id) {
        return ProductDto.builder().id(id).name("Teclado").description("Mecánico")
                .price(new BigDecimal("49.90")).stock(10).build();
    }

    @Test
    void getAllDevuelve200ConLaLista() throws Exception {
        when(service.findAll()).thenReturn(List.of(dto(1L)));

        mvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Teclado"));
    }

    @Test
    void getByIdInexistenteDevuelve404() throws Exception {
        when(service.findById(9L)).thenThrow(new ResourceNotFoundException("Product not found with id 9"));

        mvc.perform(get("/api/products/9"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Product not found with id 9"));
    }

    @Test
    void postValidoDevuelve201YLocation() throws Exception {
        when(service.create(any(ProductDto.class))).thenReturn(dto(3L));

        mvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Teclado\",\"description\":\"Mecánico\",\"price\":49.90,\"stock\":10}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/products/3"));
    }

    @Test
    void postInvalidoDevuelve400ConErroresPorCampoYNoLlamaAlServicio() throws Exception {
        mvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"price\":-5,\"stock\":-1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists())
                .andExpect(jsonPath("$.errors.price").exists())
                .andExpect(jsonPath("$.errors.stock").exists());

        verify(service, never()).create(any());
    }

    @Test
    void postConJsonMalFormadoDevuelve400() throws Exception {
        mvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{esto no es json"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteDevuelve204() throws Exception {
        mvc.perform(delete("/api/products/1"))
                .andExpect(status().isNoContent());

        verify(service).delete(1L);
    }
}
