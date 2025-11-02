package app.dtos;

import app.enums.Category;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class ItemForPackagingDTO {

    String name;
    Integer weightInGrams;
    Integer quantity;
    String description;
    Category category;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
List<BuyingOption> buyingOptions;

    public static class BuyingOption{
        String shopName;
        String shopUrl;
        double price;
    }
}
