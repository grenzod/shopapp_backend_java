package com.project.shopapp.models;

import com.project.shopapp.services.IProductRedisService;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@AllArgsConstructor
public class ProductListener {
    private final IProductRedisService iProductRedisService;
    private static final Logger logger = LoggerFactory.getLogger(ProductListener.class);

    @PrePersist
    public void prePersist(Product product) {
        logger.info("Pre persist product: {}", product);
    }

    @PostUpdate
    public void postPersist(Product product) {
        logger.info("postPersist");
        iProductRedisService.clear();
    }

    @PreUpdate
    public void preUpdate(Product product) {
        logger.info("Pre update product: {}", product);
    }

    @PostRemove
    public void postRemove(Product product) {
        logger.info("postRemove");
        iProductRedisService.clear();
    }
}
