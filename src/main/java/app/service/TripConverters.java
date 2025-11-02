package app.service;

import app.dtos.TripDTO;
import app.entities.Trip;

import java.util.List;

public class TripConverters {

    public static TripDTO convertToTripDTO(Trip trip) {
        TripDTO.TripDTOBuilder builder = TripDTO.builder()
                .name(trip.getName())
                .startTime(trip.getStartTime())
                .endTime(trip.getEndTime())
                .locationCordinates(trip.getLocationCordinates())
                .price(trip.getPrice())
                .category(trip.getCategory());
        if (trip.getGuide() != null) {
          Integer guideId = trip.getGuide().getId();
          builder.guideId(guideId);
        }
        if (trip.getId() > 0) {
            builder.id(trip.getId());
        }
        return builder.build();
    }

    public static Trip convertToTrip(TripDTO tripDTO) {
        Trip.TripBuilder builder = Trip.builder();
        if (tripDTO.getId() > 0) {
            builder.id(tripDTO.getId());
        }
        if (tripDTO.getName() != null) {
                builder.name(tripDTO.getName());
        }
        if (tripDTO.getStartTime() != null) {
            builder.startTime(tripDTO.getStartTime());
        }
        if (tripDTO.getEndTime() != null) {
            builder.endTime(tripDTO.getEndTime());
        }
        if (tripDTO.getLocationCordinates() != null) {
            builder.locationCordinates(tripDTO.getLocationCordinates());
        }
        if(tripDTO.getPrice() != 0){
            builder.price(tripDTO.getPrice());
        }
        if (tripDTO.getCategory() != null) {
            builder.category(tripDTO.getCategory());
        }
        return builder.build();
    }

    public static List<TripDTO> convertToTripDTOList(List<Trip> trips) {

        return trips.stream()
                .map(TripConverters::convertToTripDTO)
                .toList();
    }
}
