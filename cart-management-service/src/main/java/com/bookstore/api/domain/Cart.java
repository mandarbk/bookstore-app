package com.bookstore.api.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@RedisHash(value = "Cart", timeToLive = 43200)  // Cart remains valid for 12 hours.
public class Cart implements Serializable {

    @Id
    private String userId; // Maps to the JWT 'sub' claim
    private List<LineItem> items = new ArrayList<>();
}
