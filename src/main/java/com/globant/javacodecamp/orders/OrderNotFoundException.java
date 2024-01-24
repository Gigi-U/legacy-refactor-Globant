package com.globant.javacodecamp.orders;

public class OrderNotFoundException extends RuntimeException{
    public OrderNotFoundException(long orderId){
        super("Order with id %d not found".formatted(orderId));
    }
}


// after creating the exception im going to OrderService to thow it