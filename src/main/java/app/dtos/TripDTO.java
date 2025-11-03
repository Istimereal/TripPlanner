package app.dtos;

import app.entities.Guide;
import app.entities.Trip;
import app.enums.Category;
import com.fasterxml.jackson.annotation.JsonFormat;
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
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime startTime;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime endTime;
    private String locationCordinates;
    private double price;
    private Category category;
    private Integer guideId;
    private  Guide guide;

    private PackingListDTO packingList;


    public TripDTO(Trip trip) {
        this.id = trip.getId();
        this.name = trip.getName();
        this.startTime = trip.getStartTime();
        this.endTime = trip.getEndTime();
        this.locationCordinates = trip.getLocationCordinates();
        this.price = trip.getPrice();
        this.category = trip.getCategory();
        if(trip.getGuide() != null){
            this.guide = trip.getGuide();
            this.guideId = trip.getGuide().getId();
        }
    }
}
