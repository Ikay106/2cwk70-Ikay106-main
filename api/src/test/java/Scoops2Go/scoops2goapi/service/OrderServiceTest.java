package Scoops2Go.scoops2goapi.service;

import Scoops2Go.scoops2goapi.dto.ProductDTO;
import Scoops2Go.scoops2goapi.exception.InvalidPromotionException;
import Scoops2Go.scoops2goapi.exception.InvalidTreatException;
import Scoops2Go.scoops2goapi.infrastructure.OrderRepository;
import Scoops2Go.scoops2goapi.infrastructure.PaymentGateway;
import Scoops2Go.scoops2goapi.infrastructure.ProductRepository;
import Scoops2Go.scoops2goapi.model.Order;
import Scoops2Go.scoops2goapi.model.Product;
import Scoops2Go.scoops2goapi.model.Treat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderServiceTest {

    private OrderRepository orderRepository;
    private ProductRepository productRepository;
    private PaymentGateway paymentGateway;

    private OrderService sut;

    @BeforeEach
    void setUp() {
        orderRepository = mock(OrderRepository.class);
        productRepository = mock(ProductRepository.class);
        paymentGateway = mock(PaymentGateway.class);

        sut = new OrderService(orderRepository, productRepository, paymentGateway);
    }

    @ParameterizedTest(name = "date={0} -> surcharge={1}")
    @CsvSource({
            "2025-05-31T10:00:00, 0.00",
            "2025-06-01T10:00:00, 3.00",
            "2025-09-06T10:00:00, 3.00",
            "2025-09-07T10:00:00, 0.00"
    })
    @DisplayName("calcSurcharge applies 3.00 only from June 1 (inclusive) to Sep 7 (exclusive)")
    void calcSurcharge_boundaries(String time, String expected) {
        // Arrange
        LocalDateTime dt = LocalDateTime.parse(time);
        BigDecimal expectedBd = new BigDecimal(expected);

        // Act
        BigDecimal actual = sut.calcSurcharge(dt);

        // Assert
        assertEquals(0, expectedBd.compareTo(actual),
                "Surcharge should match boundary rules");
    }

    @Test
    @DisplayName("calcSurcharge null datetime returns 0.00")
    void calcSurcharge_null_returnsZero() {
        assertEquals(0, new BigDecimal("0.00").compareTo(sut.calcSurcharge(null)));
    }


    @Test
    @DisplayName("calcEstDeliveryMinutes(0,0) returns base 20")
    void calcEstDeliveryMinutes_zeroZero_returns20() {
        assertEquals(20, sut.calcEstDeliveryMinutes(0, 0));
    }

    @Test
    @DisplayName("calcEstDeliveryMinutes uses ceiling correctly")
    void calcEstDeliveryMinutes_ceilingExample() {
        // 20 + (2*1) + (0.4*1) = 22.4 -> ceil 23
        assertEquals(23, sut.calcEstDeliveryMinutes(1, 1));
    }

    @Test
    @DisplayName("calcEstDeliveryMinutes ignores negative inputs via Math.max(0, x)")
    void calcEstDeliveryMinutes_negativeInputs_treatedAsZero() {
        assertEquals(20, sut.calcEstDeliveryMinutes(-5, -999));
    }


    @Test
    @DisplayName("validateBasketSize returns true when basketSize < 10")
    void validateBasketSize_under10_true() {
        assertTrue(sut.validateBasketSize(0));
        assertTrue(sut.validateBasketSize(9));
    }

    @Test
    @DisplayName("validateBasketSize returns false when basketSize >= 10")
    void validateBasketSize_10OrMore_false() {
        assertFalse(sut.validateBasketSize(10));
        assertFalse(sut.validateBasketSize(50));
    }


    @Test
    @DisplayName("calcEstimatedDeliveryTime adds estDeliveryMinutes to orderTime")
    void calcEstimatedDeliveryTime_addsMinutes() {
        LocalDateTime start = LocalDateTime.of(2025, 1, 1, 10, 0);
        LocalDateTime expected = LocalDateTime.of(2025, 1, 1, 10, 30);

        assertEquals(expected, sut.calcEstimatedDeliveryTime(start, 30));
    }


    @Test
    @DisplayName("validateTreatProducts empty list throws InvalidTreatException")
    void validateTreatProducts_empty_throws() {
        assertThrows(InvalidTreatException.class, () -> sut.validateTreatProducts(List.of()));
    }

    @Test
    @DisplayName("validateTreatProducts null list throws InvalidTreatException")
    void validateTreatProducts_null_throws() {
        assertThrows(InvalidTreatException.class, () -> sut.validateTreatProducts(null));
    }

    @Test
    @DisplayName("validateTreatProducts no cone throws InvalidTreatException")
    void validateTreatProducts_noCone_throws() {
        List<ProductDTO> products = List.of(
                dto(1L, "Vanilla", "FLAVOR")
        );
        assertThrows(InvalidTreatException.class, () -> sut.validateTreatProducts(products));
    }

    @Test
    @DisplayName("validateTreatProducts more than 1 cone throws InvalidTreatException")
    void validateTreatProducts_twoCones_throws() {
        List<ProductDTO> products = List.of(
                dto(1L, "Cone", "CONE"),
                dto(2L, "Cone2", "CONE"),
                dto(3L, "Vanilla", "FLAVOR")
        );
        assertThrows(InvalidTreatException.class, () -> sut.validateTreatProducts(products));
    }

    @Test
    @DisplayName("validateTreatProducts no flavour throws InvalidTreatException")
    void validateTreatProducts_noFlavour_throws() {
        List<ProductDTO> products = List.of(
                dto(1L, "Cone", "CONE"),
                dto(2L, "Sprinkles", "TOPPING")
        );
        assertThrows(InvalidTreatException.class, () -> sut.validateTreatProducts(products));
    }

    @Test
    @DisplayName("validateTreatProducts more than 3 flavours throws InvalidTreatException")
    void validateTreatProducts_fourFlavours_throws() {
        List<ProductDTO> products = List.of(
                dto(1L, "Cone", "CONE"),
                dto(2L, "F1", "FLAVOR"),
                dto(3L, "F2", "FLAVOR"),
                dto(4L, "F3", "FLAVOR"),
                dto(5L, "F4", "FLAVOR")
        );
        assertThrows(InvalidTreatException.class, () -> sut.validateTreatProducts(products));
    }

    @Test
    @DisplayName("validateTreatProducts more than 5 toppings throws InvalidTreatException")
    void validateTreatProducts_sixToppings_throws() {
        List<ProductDTO> products = new ArrayList<>();
        products.add(dto(1L, "Cone", "CONE"));
        products.add(dto(2L, "Vanilla", "FLAVOR"));
        for (int i = 0; i < 6; i++) products.add(dto(10L + i, "T" + i, "TOPPING"));

        assertThrows(InvalidTreatException.class, () -> sut.validateTreatProducts(products));
    }

    @Test
    @DisplayName("validateTreatProducts valid treat does not throw")
    void validateTreatProducts_valid_doesNotThrow() {
        List<ProductDTO> products = List.of(
                dto(1L, "Cone", "CONE"),
                dto(2L, "Vanilla", "FLAVOR"),
                dto(3L, "Sprinkles", "TOPPING")
        );

        assertDoesNotThrow(() -> sut.validateTreatProducts(products));
    }


    @Test
    @DisplayName("calcSubtotal sums product prices across all treats")
    void calcSubtotal_sumsPrices() {
        // Arrange
        Product p1 = mock(Product.class);
        Product p2 = mock(Product.class);
        when(p1.getPrice()).thenReturn(new BigDecimal("2.00"));
        when(p2.getPrice()).thenReturn(new BigDecimal("1.50"));

        Treat t = new TestTreat();
        t.setProducts(List.of(p1, p2));

        // Act
        BigDecimal total = sut.calcSubtotal(List.of(t));

        // Assert
        assertEquals(0, new BigDecimal("3.50").compareTo(total));
    }


    @Test
    @DisplayName("applyPromotion unknown promo throws InvalidPromotionException")
    void applyPromotion_unknown_throws() {
        Order order = new TestOrder();
        order.setOrderTotal(new BigDecimal("50.00"));

        assertThrows(InvalidPromotionException.class, () -> sut.applyPromotion(order, "NOT_REAL"));
    }

    @Test
    @DisplayName("luckyForSome applies 13% discount when total >= 13.00")
    void luckyForSome_valid_appliesDiscount() {
        Order order = new TestOrder();
        order.setOrderTotal(new BigDecimal("13.00"));

        sut.luckyForSome(order);

        assertEquals(0, new BigDecimal("11.31").compareTo(order.getOrderTotal()));
    }

    @Test
    @DisplayName("megaMelt100 subtracts 20 when total >= 100")
    void megaMelt100_valid_subtracts20() {
        Order order = new TestOrder();
        order.setOrderTotal(new BigDecimal("120.00"));

        sut.megaMelt100(order);

        assertEquals(0, new BigDecimal("100.00").compareTo(order.getOrderTotal()));
    }

    @Test
    @DisplayName("frozen40 applies 40% off only when >=4 treats AND total >= 39.99")
    void frozen40_valid_applies40PercentOff() {
        Order order = new TestOrder();
        order.setOrderTotal(new BigDecimal("40.00"));
        order.setTreats(List.of(new TestTreat(), new TestTreat(), new TestTreat(), new TestTreat()));

        sut.frozen40(order);

        // 40.00 * 0.60 = 24.00
        assertEquals(0, new BigDecimal("24.00").compareTo(order.getOrderTotal()));
    }

    @Test
    @DisplayName("tripleTreat3 subtracts 3.00 when >=3 treats")
    void tripleTreat3_valid_subtracts3() {
        Order order = new TestOrder();
        order.setOrderTotal(new BigDecimal("10.00"));
        order.setTreats(List.of(new TestTreat(), new TestTreat(), new TestTreat()));

        sut.tripleTreat3(order);

        assertEquals(0, new BigDecimal("7.00").compareTo(order.getOrderTotal()));
    }

    @Test
    @DisplayName("scoopThereItIs subtracts 1.00 always (never below 0)")
    void scoopThereItIs_valid_subtracts1() {
        Order order = new TestOrder();
        order.setOrderTotal(new BigDecimal("0.50"));

        sut.scoopThereItIs(order);

        assertEquals(0, new BigDecimal("0.00").compareTo(order.getOrderTotal()));
    }


    private ProductDTO dto(Long id, String name, String type) {
        return new ProductDTO(id, name, BigDecimal.ZERO, "", List.of(), type);
    }

    static class TestOrder extends Order {
        public TestOrder() { super(); }
    }

    static class TestTreat extends Treat {
        public TestTreat() { super(); }
    }
}
