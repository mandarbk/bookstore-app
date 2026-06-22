package com.bookstore.api.cartmanager.repository;

import org.springframework.data.repository.CrudRepository;

import com.bookstore.api.domain.Cart;

public interface CartRepository extends CrudRepository<Cart, String> {


}
