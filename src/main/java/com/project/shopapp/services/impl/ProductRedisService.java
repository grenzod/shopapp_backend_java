package com.project.shopapp.services.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.shopapp.responses.ProductResponse;
import com.project.shopapp.services.IProductRedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ProductRedisService implements IProductRedisService {
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    private String getKeyFrom(String keyword, Long categoryId, PageRequest pageRequest) {
        int pageNumber = pageRequest.getPageNumber();
        int pageSize = pageRequest.getPageSize();
        Sort sort = pageRequest.getSort();
        String sortDirection = Objects.requireNonNull(sort.getOrderFor("id"))
                .getDirection() == Sort.Direction.ASC ? "asc" : "desc";

        return String.format("%s-%d-%s-%d-%d-%s", keyword, categoryId, "id", pageNumber, pageSize, sortDirection);
    }

    @Override
    public void clear() {
        Objects.requireNonNull(redisTemplate.getConnectionFactory()).getConnection().flushAll();
    }

    @Override
    public List<ProductResponse> getAllProducts(String keyword, Long categoryId, PageRequest pageRequest) throws JsonProcessingException {
        String key = this.getKeyFrom(keyword, categoryId, pageRequest);
        String json = redisTemplate.opsForValue().get(key);
        return json != null ? objectMapper.readValue(json, new TypeReference<List<ProductResponse>>() {}) : null;
    }

    @Override
    public void saveAllProducts(List<ProductResponse> products, String keyword, Long categoryId, PageRequest pageRequest) throws JsonProcessingException {
        String key = this.getKeyFrom(keyword, categoryId, pageRequest);
        String json = objectMapper.writeValueAsString(products);
        redisTemplate.opsForValue().set(key, json);
    }
}
