package Scoops2Go.scoops2goapi.service;

import Scoops2Go.scoops2goapi.dto.ProductDTO;
import Scoops2Go.scoops2goapi.exception.ResourceNotFoundException;
import Scoops2Go.scoops2goapi.infrastructure.ProductRepository;
import Scoops2Go.scoops2goapi.model.Product;
import Scoops2Go.scoops2goapi.model.ProductType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductServiceTest {

    private ProductRepository productRepository;
    private ProductService sut;

    @BeforeEach
    void setUp() {
        productRepository = mock(ProductRepository.class);
        sut = new ProductService(productRepository);
    }


    @Test
    @DisplayName("getProductById existing id returns ProductDTO")
    void getProductById_existing_returnsDto() {
        // Arrange
        Product product = mock(Product.class);

        // Stub everything ProductMapper.toDto() needs
        when(product.getProductType()).thenReturn(ProductType.CONE);
        when(product.getName()).thenReturn("Waffle Cone");
        when(product.getPrice()).thenReturn(new BigDecimal("2.00"));
        when(product.getDescription()).thenReturn("");
        when(product.getIngredients()).thenReturn(List.of());

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        // Act
        ProductDTO result = sut.getProductById(1L);

        // Assert
        assertNotNull(result);
        assertEquals("Waffle Cone", result.productName());
        assertEquals("CONE", result.type());

        verify(productRepository).findById(1L);
    }

    @Test
    @DisplayName("getProductById missing id throws ResourceNotFoundException")
    void getProductById_missing_throwsException() {
        // Arrange
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(ResourceNotFoundException.class,
                () -> sut.getProductById(999L));

        verify(productRepository).findById(999L);
    }


    @Test
    @DisplayName("getProducts returns mapped ProductDTO list")
    void getProducts_returnsDtoList() {
        // Arrange
        Product p1 = mock(Product.class);
        when(p1.getProductType()).thenReturn(ProductType.CONE);
        when(p1.getName()).thenReturn("Waffle Cone");
        when(p1.getPrice()).thenReturn(new BigDecimal("2.00"));
        when(p1.getDescription()).thenReturn("");
        when(p1.getIngredients()).thenReturn(List.of());

        Product p2 = mock(Product.class);
        when(p2.getProductType()).thenReturn(ProductType.FLAVOR);
        when(p2.getName()).thenReturn("Vanilla");
        when(p2.getPrice()).thenReturn(new BigDecimal("1.00"));
        when(p2.getDescription()).thenReturn("");
        when(p2.getIngredients()).thenReturn(List.of());

        when(productRepository.findAll()).thenReturn(List.of(p1, p2));

        // Act
        List<ProductDTO> result = sut.getProducts();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("CONE", result.get(0).type());
        assertEquals("FLAVOR", result.get(1).type());

        verify(productRepository).findAll();
    }

    @Test
    @DisplayName("getProducts empty repository returns empty list")
    void getProducts_empty_returnsEmptyList() {
        // Arrange
        when(productRepository.findAll()).thenReturn(List.of());

        // Act
        List<ProductDTO> result = sut.getProducts();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(productRepository).findAll();
    }
}
