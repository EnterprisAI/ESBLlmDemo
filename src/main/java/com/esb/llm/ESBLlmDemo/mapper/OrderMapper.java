package com.esb.llm.ESBLlmDemo.mapper;

import com.esb.llm.ESBLlmDemo.model.Order;
import com.esb.llm.ESBLlmDemo.model.TargetOrder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(source = "orderId", target = "id")
    @Mapping(source = "customerName", target = "customerInfo")
    @Mapping(source = "customerEmail", target = "customerContact")
    @Mapping(source = "orderDate", target = "purchaseDate")
    @Mapping(source = "orderStatus", target = "status")
    @Mapping(source = "orderItems", target = "items")
    @Mapping(target = "deliveryAddress", expression = "java(order.getShippingDetails().getShippingAddress() + \", \" + order.getShippingDetails().getShippingCity() + \", \" + order.getShippingDetails().getShippingState() + \" \" + order.getShippingDetails().getShippingZipCode())")
    @Mapping(target = "paymentDetails", expression = "java(order.getPaymentInfo().getPaymentMethod() + \" - Card ending in \" + order.getPaymentInfo().getCardLastFour())")
    @Mapping(target = "totalAmount", expression = "java(order.getOrderItems().stream().mapToDouble(item -> item.getTotalPrice()).sum() + order.getShippingDetails().getShippingCost())")
    TargetOrder orderToTargetOrder(Order order);

    @Mapping(source = "itemId", target = "productId")
    @Mapping(source = "productName", target = "productName")
    @Mapping(source = "quantity", target = "qty")
    @Mapping(source = "unitPrice", target = "price")
    @Mapping(source = "totalPrice", target = "subtotal")
    TargetOrder.TargetOrderItem orderItemToTargetOrderItem(Order.OrderItem item);
} 