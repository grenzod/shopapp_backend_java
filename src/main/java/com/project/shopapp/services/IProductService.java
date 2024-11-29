package com.project.shopapp.services;

import com.project.shopapp.DTO.ProductDTO;
import com.project.shopapp.DTO.ProductImageDTO;
import com.project.shopapp.exceptions.DataNotFoundException;
import com.project.shopapp.models.Product;
import com.project.shopapp.models.ProductImage;
import com.project.shopapp.responses.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

public interface IProductService{
    Product createProduct(ProductDTO productDTO) throws DataNotFoundException;
    Product getProduct(Long id) throws DataNotFoundException;
    List<Product> findProductByIds(List<Long> ids);
    Page<ProductResponse> getAllProducts(String keyword, Long categoryId, PageRequest pageRequest);
    Product updateProduct(Long id, ProductDTO productDTO) throws Exception;
    void deleteProduct(Long id);
    boolean existsByName(String name);
    ProductImage createProductImage(Long productId, ProductImageDTO productImageDTO) throws Exception;
}
