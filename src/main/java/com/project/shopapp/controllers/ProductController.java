package com.project.shopapp.controllers;

import com.github.javafaker.Faker;
import com.project.shopapp.DTO.ProductDTO;
import com.project.shopapp.DTO.ProductImageDTO;
import com.project.shopapp.exceptions.DataNotFoundException;
import com.project.shopapp.models.Product;
import com.project.shopapp.models.ProductImage;
import com.project.shopapp.responses.ProductListResponse;
import com.project.shopapp.responses.ProductResponse;
import com.project.shopapp.services.impl.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/products")
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<ProductListResponse> getAllProducts(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit
    ) {
        PageRequest pageRequest = PageRequest.of(
                page,
                limit,
                Sort.by("id").ascending()
//                Sort.by("createdAt").descending()
        );

        Page<ProductResponse> productPages = productService.getAllProducts(keyword, categoryId, pageRequest);
        int totalPages = productPages.getTotalPages();
        List<ProductResponse> products = productPages.getContent();

        return ResponseEntity.ok(ProductListResponse.builder()
                .products(products)
                .total(totalPages)
                .build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(@PathVariable("id") Long productId) throws DataNotFoundException {
        Product product = productService.getProduct(productId);
        return ResponseEntity.ok().body(ProductResponse.fromProduct(product));
    }

    @GetMapping("/images/{imageName}")
    public ResponseEntity<?> getProductImage(@PathVariable("imageName") String imageName) {
        try {
            Path path = Paths.get("uploads/" + imageName);
            UrlResource resource = new UrlResource(path.toUri());

            if(resource.exists()){
                return ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_JPEG)
                        .body(resource);
            }
//                return ResponseEntity.notFound().build();
            else{
                return ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_JPEG)
                        .body(new UrlResource(Paths.get("uploads/error-404.webp").toUri()));
            }
        }
        catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/by-ids")
    public ResponseEntity<?> getProductsByIds(@RequestParam("ids") String ids) {
        try {
            List<Long> productIds = Arrays.stream(ids.split(","))
                    .map(Long::parseLong)
                    .collect(Collectors.toList());
            List<Product> list = productService.findProductByIds(productIds);
            return ResponseEntity.ok(list);
        }
        catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping(value = "")
    public ResponseEntity<?> addProduct(@Valid @RequestBody ProductDTO productDTO,
                                         BindingResult result) {
        try {
            if(result.hasErrors()) {
                List<String> errorMessages = result.getFieldErrors()
                        .stream()
                        .map(FieldError::getDefaultMessage)
                        .toList();
                return ResponseEntity.badRequest().body(errorMessages);
            }

            Product product = productService.createProduct(productDTO);
            return ResponseEntity.ok(product);
        }
        catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping(value = "uploads/{id}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    private ResponseEntity<?> uploadImages(@PathVariable("id") Long productId,
            @ModelAttribute("file") List<MultipartFile> files){

        try {
            Product product = productService.getProduct(productId);
            files = files == null ? new ArrayList<>() : files;
            if(files.size() > ProductImage.MAXIMUM_IMAGES_PER_PRODUCT) {
                return ResponseEntity.badRequest().body("You can only upload maximum 5 images");
            }
            List<ProductImage> productImages = new ArrayList<>();
            for(MultipartFile file : files) {
                if(file.getSize() == 0){
                    throw new Exception("File is empty");
                }
                if(file.getSize() > 10 * 1024 * 1024) {
                    return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                            .body("File is too large ! Maximum size is 10 MB");
                }
                String contentType = file.getContentType();
                if(contentType == null || !contentType.startsWith("image/")) {
                    return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                            .body("File must be an image !");
                }
                String fileName = storeFile(file);
                ProductImage productImage = productService.createProductImage(product.getId(), ProductImageDTO.builder()
                        .imageUrl(fileName)
                        .build());
                productImages.add(productImage);
            }
            return ResponseEntity.ok().body(productImages);
        }
        catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    private String storeFile(MultipartFile file) throws IOException {
        if(!isImageFile(file) || file.getOriginalFilename() == null){
            throw new IOException("Invalid image format");
        }
        String filename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));

        //Them UUID vao truoc ten file de dam bao ten file la duy nhat, tranh truong hop bi trung ten anh
        String uniquefile = UUID.randomUUID().toString() + "_" + filename;
        Path path = Paths.get("uploads");

        if(!Files.exists(path)){
            Files.createDirectories(path);
        }

        // Duong dan day du den File
        Path destination = Paths.get(path.toString(), uniquefile);
        Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
        return uniquefile;
    }

    private boolean isImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && contentType.startsWith("image/");
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable Long id,
                                                @RequestBody ProductDTO productDTO) {
        try {
            Product product = productService.updateProduct(id, productDTO);
            return ResponseEntity.ok().body(product);
        }
        catch (Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable Long id) {
        try {
            productService.deleteProduct(id);
            return ResponseEntity.ok(String.format("Product with id = %d deleted successfully", id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/generateFakeProducts")
    public ResponseEntity<String> generateFakeProducts() throws DataNotFoundException {
        Faker faker = new Faker();
        for(int i = 1; i <= 5_000; i++){
            String productName = faker.commerce().productName();
            if(productService.existsByName(productName)) {
                continue;
            }
            ProductDTO productDTO = ProductDTO.builder()
                    .name(productName)
                    .price((float)faker.number().numberBetween(10, 90_000_000))
                    .description(faker.lorem().sentence())
                    .thumbnail(null)
                    .categoryId((long)faker.number().numberBetween(2, 4))
                    .build();
            productService.createProduct(productDTO);
        }
        return ResponseEntity.ok("Fake product successfully");
    }
}
