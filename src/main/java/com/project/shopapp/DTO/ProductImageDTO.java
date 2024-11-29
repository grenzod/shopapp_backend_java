package com.project.shopapp.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import lombok.*;

@Data//toString
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductImageDTO {
    @Min(value = 1, message = "Id must be > 0")
    @JsonProperty("product_id")
    private Long productId;

    @JsonProperty("image_url")
    private String imageUrl;
}
