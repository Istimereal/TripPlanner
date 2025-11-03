package app.entities;

import app.enums.Category;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;


@Setter
@Getter
@EqualsAndHashCode
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "trip")
public class Trip {

    @Id
    @Column(name = "trip_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "name", nullable = false)
    String name;

    @Column(name =  "start_time", nullable = false)
  //  @JsonFormat(pattern = "yyyy-MM-ddHH:mm")
    private LocalDateTime startTime;

    @Column(name =  "end_time", nullable = false)
  //  @JsonFormat(pattern = "yyyy-MM-ddHH:mm")
    private LocalDateTime endTime;

    @Column(name =  "location_cordinates", nullable = false)
    private String locationCordinates;

    @Column( name = "price", nullable = false)
    private double price;

    @Column(name = "category", nullable = false)
    private Category category;

    @ManyToOne
    @JoinColumn(name = "guide_id")
    @JsonBackReference
    private Guide guide;

}
