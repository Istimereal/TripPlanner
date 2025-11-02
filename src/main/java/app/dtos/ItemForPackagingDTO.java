package app.dtos;

import app.enums.Category;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;

@Getter
@Setter
public class ItemForPackagingDTO {

    String name;
    Integer weightInGrams;
    Integer quantity;
    String description;
    String category;
    ZonedDateTime createdAt;
    ZonedDateTime updatedAt;
List<BuyingOption> buyingOptions;


@Getter
@Setter
    public static class BuyingOption{
        String shopName;
        String shopUrl;
        double price;
    }

}
