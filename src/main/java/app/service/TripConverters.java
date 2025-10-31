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
                .category(trip.getCategory())
                .guide(trip.getGuide());
        if (trip.getId() > 0) {
            builder.id(trip.getId());
        }
        return builder.build();
    }

    public static Trip convertToTrip(TripDTO tripDTO) {
        Trip.TripBuilder builder = Trip.builder()
                .name(tripDTO.getName())
                .startTime(tripDTO.getStartTime())
                .endTime(tripDTO.getEndTime())
                .locationCordinates(tripDTO.getLocationCordinates())
                .price(tripDTO.getPrice())
                .category(tripDTO.getCategory())
                .guide(tripDTO.getGuide());
        if (tripDTO.getId() > 0) {
            builder.id(tripDTO.getId());
        }
        return builder.build();
    }

    public static List<TripDTO> convertToTripDTOList(List<Trip> trips) {

        return trips.stream()
                .map(TripConverters::convertToTripDTO)
                .toList();
    }
}
