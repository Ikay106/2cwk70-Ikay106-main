package Scoops2Go.scoops2goapi.controller;

import Scoops2Go.scoops2goapi.dto.CheckoutDTO;
import Scoops2Go.scoops2goapi.dto.OrderDTO;
import Scoops2Go.scoops2goapi.exception.InvalidBasketException;
import Scoops2Go.scoops2goapi.exception.ResourceNotFoundException;
import Scoops2Go.scoops2goapi.service.OrderService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
@Import(ApiExceptionHandler.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;



    @Test
    @DisplayName("getOrder validId returns 200")
    void getOrder_validId_returns200() throws Exception {
        when(orderService.getOrderById(1L)).thenReturn(Mockito.mock(OrderDTO.class));

        mockMvc.perform(get("/api/order/1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("getOrder notFound returns 404")
    void getOrder_notFound_returns404() throws Exception {
        when(orderService.getOrderById(99999L))
                .thenThrow(new ResourceNotFoundException("Order not found"));

        mockMvc.perform(get("/api/order/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("getOrder invalidIdFormat returns 500")
    void getOrder_invalidFormat_returns500() throws Exception {
        mockMvc.perform(get("/api/order/abc"))
                .andExpect(status().isInternalServerError());
    }




    @Test
    @DisplayName("createOrder validRequest returns 201")
    void createOrder_validRequest_returns201() throws Exception {
        when(orderService.createOrder(any())).thenReturn(Mockito.mock(OrderDTO.class));

        String validOrderJson = """
        {
          "orderId": 0,
          "orderTime": "2025-01-01T10:00:00",
          "promotion": null,
          "basketItems": [
            {
              "products": [
                { "productId": 1, "productName": "Waffle Cone", "price": 0, "description": "", "ingredients": [], "type": "CONE" },
                { "productId": 4, "productName": "Vanilla",     "price": 0, "description": "", "ingredients": [], "type": "FLAVOR" }
              ]
            }
          ]
        }
        """;

        mockMvc.perform(post("/api/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validOrderJson))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("createOrder emptyBasket returns 400")
    void createOrder_emptyBasket_returns400() throws Exception {
        when(orderService.createOrder(any()))
                .thenThrow(new InvalidBasketException("Invalid basket size: 0", 0));

        String emptyOrderJson = """
        {
          "orderId": 0,
          "orderTime": "2025-01-01T10:00:00",
          "basketItems": []
        }
        """;

        mockMvc.perform(post("/api/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(emptyOrderJson))
                .andExpect(status().isBadRequest());
    }


    @Test
    @DisplayName("updateOrder validRequest returns 200")
    void updateOrder_validRequest_returns200() throws Exception {
        when(orderService.updateOrder(any())).thenReturn(Mockito.mock(OrderDTO.class));

        String updateJson = """
        {
          "orderId": 1,
          "orderTime": "2025-01-01T10:00:00",
          "promotion": null,
          "basketItems": [
            {
              "products": [
                { "productId": 1,  "productName": "Waffle Cone", "price": 0, "description": "", "ingredients": [], "type": "CONE" },
                { "productId": 4,  "productName": "Vanilla",     "price": 0, "description": "", "ingredients": [], "type": "FLAVOR" },
                { "productId": 12, "productName": "Sprinkles",   "price": 0, "description": "", "ingredients": [], "type": "TOPPING" }
              ]
            }
          ]
        }
        """;

        mockMvc.perform(put("/api/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("updateOrder invalidBasket returns 400")
    void updateOrder_invalidBasket_returns400() throws Exception {
        when(orderService.updateOrder(any()))
                .thenThrow(new InvalidBasketException("Invalid basket size: 0", 0));

        String badUpdateJson = """
        {
          "orderId": 1,
          "orderTime": "2025-01-01T10:00:00",
          "basketItems": []
        }
        """;

        mockMvc.perform(put("/api/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(badUpdateJson))
                .andExpect(status().isBadRequest());
    }


    @Test
    @DisplayName("checkoutOrder validId returns 200")
    void checkoutOrder_validId_returns200() throws Exception {
        when(orderService.checkoutOrder(1L)).thenReturn(Mockito.mock(CheckoutDTO.class));

        mockMvc.perform(post("/api/order/1/checkout"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("checkoutOrder notFound returns 404")
    void checkoutOrder_notFound_returns404() throws Exception {
        when(orderService.checkoutOrder(99999L))
                .thenThrow(new ResourceNotFoundException("Order not found"));

        mockMvc.perform(post("/api/order/99999/checkout"))
                .andExpect(status().isNotFound());
    }


    @Test
    @DisplayName("deleteOrder validId returns 204")
    void deleteOrder_validId_returns204() throws Exception {
        doNothing().when(orderService).deleteOrder(1L);

        mockMvc.perform(delete("/api/order/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("deleteOrder notFound returns 404")
    void deleteOrder_notFound_returns404() throws Exception {
        doThrow(new ResourceNotFoundException("Order not found"))
                .when(orderService).deleteOrder(99999L);

        mockMvc.perform(delete("/api/order/99999"))
                .andExpect(status().isNotFound());
    }
}
