package app.dtos;

public class GuideTotalPriceTripsDTO {

    int guideId;
    double totalPrice;

    public GuideTotalPriceTripsDTO(int guideId, double totalPrice) {
        this.guideId = guideId;
        this.totalPrice = totalPrice;
    }
}
