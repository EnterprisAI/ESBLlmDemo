package com.esb.llm.ESBLlmDemo.service;

import com.esb.llm.ESBLlmDemo.mapper.ProductMapper;
import com.esb.llm.ESBLlmDemo.model.Product;
import com.esb.llm.ESBLlmDemo.model.TargetProduct;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ProductMapperTest {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testProductMapping() throws Exception {
        // Create source product
        Product.ProductCategory category = new Product.ProductCategory("CAT001", "Electronics", "Electronic devices and accessories");
        Product.ProductSpecification spec1 = new Product.ProductSpecification("Color", "Black", "");
        Product.ProductSpecification spec2 = new Product.ProductSpecification("Weight", "500", "grams");
        List<Product.ProductSpecification> specs = Arrays.asList(spec1, spec2);
        Product.ProductInventory inventory = new Product.ProductInventory(25, "Warehouse A", "TechCorp Inc.");
        
        Product sourceProduct = new Product("PROD001", "Laptop", 999.99, category, specs, inventory);

        // Map to target
        TargetProduct targetProduct = productMapper.productToTargetProduct(sourceProduct);

        // Verify mapping
        assertNotNull(targetProduct);
        assertEquals("PROD001", targetProduct.getId());
        assertEquals("Laptop", targetProduct.getName());
        assertEquals(999.99, targetProduct.getPrice());
        assertEquals("Electronics", targetProduct.getCategory());
        assertEquals(25, targetProduct.getAvailableStock());
        assertEquals("Warehouse A", targetProduct.getLocation());
        assertTrue(targetProduct.isAvailable());
        assertEquals(2, targetProduct.getSpecs().size());

        // Convert to JSON for verification
        String sourceJson = objectMapper.writeValueAsString(sourceProduct);
        String targetJson = objectMapper.writeValueAsString(targetProduct);

        System.out.println("=== Product Mapping Test ===");
        System.out.println("Source JSON:");
        System.out.println(sourceJson);
        System.out.println("\nTarget JSON:");
        System.out.println(targetJson);
        System.out.println("=== Test Complete ===\n");
    }
} 