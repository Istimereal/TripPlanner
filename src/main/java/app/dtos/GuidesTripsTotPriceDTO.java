package app.dtos;


import lombok.*;

@Setter
@Getter
@Builder
@NoArgsConstructor
public class GuidesTripsTotPriceDTO {

    private Integer guideId;

    private double totalPrice;

    public GuidesTripsTotPriceDTO(Integer guideId, double totalPrice) {
        this.guideId = guideId;
        this.totalPrice = totalPrice;
    }
}
