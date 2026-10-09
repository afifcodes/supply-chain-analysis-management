package com.example.supplychain.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.supplychain.dto.ProductRequest;
import com.example.supplychain.exception.BusinessRuleException;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ProductServiceTest {
    @Autowired
    ProductService productService;

    @Test
    void duplicateSkuIsRejected() {
        ProductRequest first = new ProductRequest("DUP-1", "First", null, null, new BigDecimal("10.00"), 5, 2, null);
        ProductRequest second = new ProductRequest("dup-1", "Second", null, null, new BigDecimal("11.00"), 5, 2, null);

        productService.create(first);

        assertThatThrownBy(() -> productService.create(second))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("SKU already exists");
    }
}

