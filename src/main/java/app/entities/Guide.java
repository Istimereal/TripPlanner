package app.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@EqualsAndHashCode
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "guide")
public class Guide {


    @Id
    @Column(name = "guide_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "phone_number")
    private int phoneNumber;

    @Column(name = "experience_in_years", nullable = false)
    private Integer experienceInYears;

    @OneToMany(mappedBy = "guide", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE}, fetch = FetchType.LAZY)
    private List<Trip> trips = new ArrayList<Trip>();

    public void addTrip(Trip trip) {
        if ( trip != null) {
            this.trips.add(trip);
            trip.setGuide(this);
        }
    }

}
