package com.project.shopapp.services.impl;

import com.project.shopapp.DTO.ProductDTO;
import com.project.shopapp.DTO.ProductImageDTO;
import com.project.shopapp.exceptions.DataNotFoundException;
import com.project.shopapp.models.Category;
import com.project.shopapp.models.Product;
import com.project.shopapp.models.ProductImage;
import com.project.shopapp.repositories.CategoryRepository;
import com.project.shopapp.repositories.ProductImageRepository;
import com.project.shopapp.repositories.ProductRepository;
import com.project.shopapp.responses.ProductResponse;
import com.project.shopapp.services.IProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductService implements IProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductImageRepository productImageRepository;

    @Override
    public Product createProduct(ProductDTO productDTO) throws DataNotFoundException {
        Category category = categoryRepository
                .findById(productDTO.getCategoryId())
                .orElseThrow(() -> new DataNotFoundException("Category not found with " + productDTO.getCategoryId()));

        Product product = Product.builder()
                .name(productDTO.getName())
                .price(productDTO.getPrice())
                .thumbnail(productDTO.getThumbnail())
                .description(productDTO.getDescription())
                .category(category)
                .build();
        return productRepository.save(product);
    }

    @Override
    public Product getProduct(Long id) throws DataNotFoundException {
//        return productRepository.findById(id).orElseThrow(() -> new DataNotFoundException("Cannot found this product !"));
        Optional<Product> product = productRepository.getDetailProduct(id);
        if(product.isPresent()){
            return product.get();
        }
        throw new DataNotFoundException("Product not found with " + id);
    }

    @Override
    public List<Product> findProductByIds(List<Long> ids) {
        return productRepository.findAllById(ids);
    }

    @Override
    public Page<ProductResponse> getAllProducts(String keyword, Long categoryId, PageRequest pageRequest) {
        Page<Product> productsPage = productRepository.searchProducts(keyword, categoryId, pageRequest);
        return productsPage.map(ProductResponse::fromProduct);
    }

    @Override
    public Product updateProduct(Long id, ProductDTO productDTO) throws Exception{
        Product product = getProduct(id);
        if(product != null){
            Category category = categoryRepository
                    .findById(productDTO.getCategoryId())
                    .orElseThrow(() -> new DataNotFoundException("Category not found with " + productDTO.getCategoryId()));

            product.setName(productDTO.getName());
            product.setCategory(category);
            product.setPrice(productDTO.getPrice());
            product.setThumbnail(productDTO.getThumbnail());
            product.setDescription(productDTO.getDescription());
            return productRepository.save(product);
        }
        return null;
    }

    @Override
    public void deleteProduct(Long id) {
        Optional<Product> optionalProduct = productRepository.findById(id);
        optionalProduct.ifPresent(productRepository::delete);
    }

    @Override
    public boolean existsByName(String name) {
        return productRepository.existsByName(name);
    }

    @Override
    public ProductImage createProductImage(Long productId, ProductImageDTO productImageDTO) throws Exception{
        Product product = productRepository
                .findById(productId)
                .orElseThrow(() -> new DataNotFoundException("Cannot found this product!"));

        ProductImage productImage = ProductImage.builder()
                .product(product)
                .imageUrl(productImageDTO.getImageUrl())
                .build();

        int size = productImageRepository.findByProductId(productId).size();
        if(size >= ProductImage.MAXIMUM_IMAGES_PER_PRODUCT) {
            throw new Exception(
                    "Number of images 's product has been '"
                            +ProductImage.MAXIMUM_IMAGES_PER_PRODUCT);
        }
        return productImageRepository.save(productImage);
    }
}
