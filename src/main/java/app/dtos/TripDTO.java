package app.dtos;

import app.entities.Guide;
import app.entities.Trip;
import app.enums.Category;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Builder
@ToString
public class TripDTO {

    private int id;
    private String name;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String locationCordinates;
    private double price;
    private Category category;
    private Guide guide;

    public TripDTO(Trip trip) {
        this.id = trip.getId();
        this.name = trip.getName();
        this.startTime = trip.getStartTime();
        this.endTime = trip.getEndTime();
        this.locationCordinates = trip.getLocationCordinates();
        this.price = trip.getPrice();
        this.category = trip.getCategory();
        this.guide = trip.getGuide();
    }
}
