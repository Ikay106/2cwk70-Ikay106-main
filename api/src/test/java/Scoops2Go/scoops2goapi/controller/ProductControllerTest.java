package Scoops2Go.scoops2goapi.controller;

import Scoops2Go.scoops2goapi.dto.ProductDTO;
import Scoops2Go.scoops2goapi.exception.ResourceNotFoundException;
import Scoops2Go.scoops2goapi.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;


    @Test
    void getAllProducts_validRequest_returns200() throws Exception {
        mockMvc.perform(get("/api/product"))
                .andExpect(status().isOk());
    }


    @Test
    void getProductById_validId_returns200() throws Exception {
        long id = 1L;

        ProductDTO dto = new ProductDTO(
                id,
                "Vanilla",
                new BigDecimal("2.50"),
                "Classic vanilla scoop",
                List.of("Milk", "Sugar", "Vanilla"),
                "FLAVOR"
        );

        when(productService.getProductById(id)).thenReturn(dto);

        mockMvc.perform(get("/api/product/" + id))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON));
    }


    @Test
    void getProductById_invalidId_returns404() throws Exception {
        long invalidId = 99999L;

        when(productService.getProductById(invalidId))
                .thenThrow(new ResourceNotFoundException("Product not found"));

        mockMvc.perform(get("/api/product/" + invalidId))
                .andExpect(status().isNotFound());
    }


    @Test
    void getProductById_invalidFormat_returns500() throws Exception {
        mockMvc.perform(get("/api/product/abc"))
                .andExpect(status().isInternalServerError());
    }

}
