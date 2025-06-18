package com.esb.llm.ESBLlmDemo.mapper;

import com.esb.llm.ESBLlmDemo.model.Product;
import com.esb.llm.ESBLlmDemo.model.TargetProduct;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(source = "productId", target = "id")
    @Mapping(source = "productName", target = "name")
    @Mapping(source = "productPrice", target = "price")
    @Mapping(source = "productCategory.categoryName", target = "category")
    @Mapping(source = "productSpecifications", target = "specs")
    @Mapping(source = "productInventory.stockLevel", target = "availableStock")
    @Mapping(source = "productInventory.warehouseLocation", target = "location")
    @Mapping(target = "available", expression = "java(product.getProductInventory().getStockLevel() > 0)")
    TargetProduct productToTargetProduct(Product product);

    @Mapping(source = "specKey", target = "key")
    @Mapping(source = "specValue", target = "value")
    @Mapping(source = "specUnit", target = "unit")
    TargetProduct.TargetProductSpec productSpecificationToTargetProductSpec(Product.ProductSpecification spec);
} 