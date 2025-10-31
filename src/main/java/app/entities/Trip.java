package app.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Locale;

@Setter
@Getter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Trip {

    @Id
    @Column(name = "trip_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "name", nullable = false)
    String name;

    @Column(name =  "startTime", nullable = false)
    @JsonFormat(pattern = "yyyy-mm-dd hh:mm")
    private LocalDateTime startTime;

    @Column(name =  "endTime", nullable = false)
    @JsonFormat(pattern = "yyyy-mm-dd hh:mm")
    private LocalDateTime endTime;

    @Column(name =  "locationCordinates", nullable = false)
    private String locationCordinates;

    @Column( name = "price", nullable = false)
    private double price;

    @Column(name = "category", nullable = false)
    private Locale.Category category;

    @ManyToOne
    @JoinColumn(name = "guide_id")
    private Guide guide;

}
