package app.service;

import app.dtos.GuideDTO;
import app.entities.Guide;

import java.util.List;

public class GuideConverters {

    public static GuideDTO convertToGuideDTO(Guide guide){

        GuideDTO.GuideDTOBuilder builder = GuideDTO.builder()
                .name(guide.getName())
                .email(guide.getEmail())
                .phoneNumber(guide.getPhoneNumber())
                .experienceInYears(guide.getExperienceInYears());
        if(guide.getId() > 0){
            builder.id(guide.getId());
        }
        return builder.build();
    }

    public static Guide convertToGuide(GuideDTO guideDTO){
        Guide.GuideBuilder builder = Guide.builder()
                .name(guideDTO.getName())
                .email(guideDTO.getEmail())
                .phoneNumber(guideDTO.getPhoneNumber())
                .experienceInYears(guideDTO.getExperienceInYears());
        if(guideDTO.getId() > 0){
            builder.id(guideDTO.getId());
        }
        return builder.build();
    }

    public static List<GuideDTO> convertToGuideDTO(List<Guide> guides){
        return guides.stream()
                .map(GuideConverters::convertToGuideDTO)
                .toList();
    }
}
