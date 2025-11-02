package app.dtos;

import app.entities.Guide;
import lombok.*;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GuideDTO {

    private int id;
    private String name;
    private String email;
    private int phoneNumber;
    private int experienceInYears;

    public GuideDTO(Guide guide) {
        this.id = guide.getId();
        this.name = guide.getName();
        this.email = guide.getEmail();
        this.phoneNumber = guide.getPhoneNumber();
        this.experienceInYears = guide.getExperienceInYears();
    }
}
