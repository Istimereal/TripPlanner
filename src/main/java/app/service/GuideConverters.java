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
        Guide.GuideBuilder builder = Guide.builder();
        if (guideDTO.getName() != null) {
            builder.name(guideDTO.getName());
        }
        if (guideDTO.getEmail() != null) {
             builder.name(guideDTO.getName());
        }
              if(guideDTO.getEmail() != null){
                  builder.email(guideDTO.getEmail());
              }
             if(guideDTO.getPhoneNumber() > 0) {
                builder.phoneNumber(guideDTO.getPhoneNumber());
             }
             if(guideDTO.getExperienceInYears() != 0) {
                builder.experienceInYears(guideDTO.getExperienceInYears());
             }
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
