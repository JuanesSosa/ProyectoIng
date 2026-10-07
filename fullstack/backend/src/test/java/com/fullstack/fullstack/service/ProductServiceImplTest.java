package com.fullstack.fullstack.service;

import com.fullstack.fullstack.dto.ProductDto;
import com.fullstack.fullstack.exception.ResourceNotFoundException;
import com.fullstack.fullstack.model.entity.Product;
import com.fullstack.fullstack.repository.ProductRepository;
import com.fullstack.fullstack.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Pruebas unitarias del servicio de productos (el repositorio se simula con Mockito, sin base de datos). */
@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository repository;

    @InjectMocks
    private ProductServiceImpl service;

    private Product product(Long id, String name) {
        return Product.builder().id(id).name(name).description("desc")
                .price(new BigDecimal("10.50")).stock(5).build();
    }

    @Test
    void findAllDevuelveLosProductosComoDto() {
        when(repository.findAll()).thenReturn(List.of(product(1L, "A"), product(2L, "B")));

        List<ProductDto> result = service.findAll();

        assertEquals(2, result.size());
        assertEquals("A", result.get(0).getName());
    }

    @Test
    void findByIdLanzaNotFoundSiNoExiste() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.findById(99L));
    }

    @Test
    void createIgnoraElIdRecibido() {
        ProductDto dto = ProductDto.builder().id(7L).name("Nuevo")
                .price(new BigDecimal("1.00")).stock(1).build();
        when(repository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        service.create(dto);

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(repository).save(captor.capture());
        assertNull(captor.getValue().getId());
    }

    @Test
    void updateActualizaLosCampos() {
        Product existing = product(1L, "Viejo");
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));
        ProductDto dto = ProductDto.builder().name("Nuevo").description("x")
                .price(new BigDecimal("3.00")).stock(9).build();

        ProductDto result = service.update(1L, dto);

        assertEquals("Nuevo", result.getName());
        assertEquals(9, result.getStock());
    }

    @Test
    void deleteLanzaNotFoundSiNoExisteYNoBorra() {
        when(repository.existsById(5L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> service.delete(5L));
        verify(repository, never()).deleteById(any());
    }
}
