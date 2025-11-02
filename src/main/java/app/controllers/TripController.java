package app.controllers;

import app.daos.GuideDAO;
import app.daos.TripDAO;
import app.dtos.GuideDTO;
import app.dtos.GuidesTripsTotPriceDTO;
import app.dtos.TripDTO;
import app.entities.Trip;
import app.enums.Category;
import app.exceptions.ApiException;
import app.service.GuideConverters;
import app.service.TripConverters;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import jakarta.persistence.PersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import app.enums.Category;

import javax.swing.text.Keymap;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static app.utils.ResponseUtil.disableCache;
import static java.util.stream.Collectors.toList;

public class TripController {
    LocalDateTime timeStamp = LocalDateTime.now();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    String formattedTime = timeStamp.format(formatter);

    private static final Logger logger = LoggerFactory.getLogger("pruduction");
    private static final Logger debugLogProd = LoggerFactory.getLogger("debug");
    private final TripDAO tripDAO;


    public TripController(TripDAO tripDAO) {
        this.tripDAO = tripDAO;
    }

      public void getTrips(Context ctx) {
        try {
            Category category;
            disableCache(ctx);

            String request = ctx.queryParam("category");

            if(request != null && !request.isEmpty()) {

                category = Category.valueOf(request.toUpperCase());
                List<TripDTO> allTrips = TripConverters.convertToTripDTOList(tripDAO.getAllTrips());
                List<TripDTO> sortedTrips = allTrips.stream()
                        .filter( trip -> trip.getCategory().equals(category))
                        .toList();

                ctx.status(HttpStatus.OK).json(sortedTrips);
            }
            else
            {
                List<TripDTO> tripDTOs = TripConverters.convertToTripDTOList(tripDAO.getAllTrips());
                if (tripDTOs.isEmpty()) {
                    ctx.status(HttpStatus.NOT_FOUND).json(Map.of("status", HttpStatus.NOT_FOUND.getCode(), "message", "No trips in database"));
                    logger.warn("No trips in database");
                } else {
                    ctx.status(200).json(tripDTOs);
                }
            }
        }
        catch (PersistenceException pe) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json(Map.of("status",HttpStatus.INTERNAL_SERVER_ERROR.getCode(),
                    "message", "Database problems, try agian later"));
            debugLogProd.error(formattedTime + " Database  persistence error", pe);
        }
        catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json(Map.of("status",
                    HttpStatus.INTERNAL_SERVER_ERROR.getCode(), "msg",
                    "There was an unexpected error with the server, try again later"));
            debugLogProd.debug(formattedTime, "unexpected error with the server ", e);
        }
    }

    public void getTripById(Context ctx) {
        int id = 0;
        try {
            disableCache(ctx);
            id = Integer.parseInt(ctx.pathParam("id"));
            if (id > 0) {
                TripDTO tripDTO = TripConverters.convertToTripDTO(tripDAO.getTripById(id));
                ctx.status(200).json(tripDTO);
            }
            else {
                ctx.status(HttpStatus.BAD_REQUEST).json(Map.of("status",HttpStatus.BAD_REQUEST.getCode(),"message", "You need to type at id above 0"));
            }
        }
        catch (NumberFormatException ne) {
            ctx.json(Map.of("status", HttpStatus.BAD_REQUEST.getCode(), "msg",
                    "Invalid id format:" + ctx.pathParam("id")));
        }
        catch (PersistenceException pe) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json(Map.of("status", HttpStatus.INTERNAL_SERVER_ERROR.getCode(),
                    "msg","Database problems, try agian later"));
            debugLogProd.debug(formattedTime, "Error with database trying to find Trip by Id: " + id + " ", pe);
        }
        catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json(Map.of("status",
                    HttpStatus.INTERNAL_SERVER_ERROR.getCode(), "msg",
                    "There was an unexpected error with the server, try again later"));
            debugLogProd.debug(formattedTime, "unexpected error with the server trying to find trip by ID: ", e);
        }
    }

    public void createTrip(Context ctx) {
        try {
            TripDTO newTrip = ctx.bodyAsClass(TripDTO.class);

            TripDTO createdTrip = TripConverters.convertToTripDTO(tripDAO.createTrip(TripConverters.convertToTrip(newTrip)));
       ctx.status(HttpStatus.CREATED).json(createdTrip);
        }
        catch(BadRequestResponse br) {
            ctx.status(HttpStatus.BAD_REQUEST).
                    json(Map.of("status", HttpStatus.BAD_REQUEST.getCode(),
                            "msg", "Invalid post, see documentation for correct form"));
        }
        catch (PersistenceException pe){
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json(Map.of("status", HttpStatus.INTERNAL_SERVER_ERROR.getCode(),
                    "msg","Database problems, try agian later"));
            debugLogProd.error(formattedTime, "Database problems while creation a trip", pe);
        }
        catch(Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json(Map.of("status",HttpStatus.INTERNAL_SERVER_ERROR.getCode(),
                    "msg", "There was an unexpected problem with the server"));
            debugLogProd.error(formattedTime, "Unexpected server problem while creating a trip", e);
        }
    }

    public void updateTrip(Context ctx) {
   int id = 0;
   Trip trip = null;
   try {
       id = Integer.parseInt(ctx.pathParam("id"));
       if (id > 0) {
           trip = tripDAO.getTripById(id);

           if (trip == null) {
               ctx.status(HttpStatus.NOT_FOUND).json(Map.of("status", HttpStatus.NOT_FOUND.getCode(), "message", "guide not found"));
               return;
           }
       }
       TripDTO tripUpdateDTO = ctx.bodyAsClass(TripDTO.class);
       if(tripUpdateDTO.getName() != null && tripUpdateDTO.getName().isEmpty()) {
           throw new BadRequestResponse("Name cannot be empty, exclude or put desired name");
       }
      if(tripUpdateDTO.getLocationCordinates() != null && tripUpdateDTO.getLocationCordinates().isEmpty()) {
          throw new BadRequestResponse("LocationCordinates cannot be empty, exclude or put desired cordinates");
      }
      Trip forUpdate = TripConverters.convertToTrip(tripUpdateDTO);
     Trip updateResult =  tripDAO.updateTrip(id, forUpdate);
      TripDTO updated = TripConverters.convertToTripDTO(updateResult);
      ctx.status(HttpStatus.OK).json(updated);
   }
   catch(BadRequestResponse bre) {

       String message;

       if (bre.getMessage() == null || bre.getMessage().isBlank()) {

           message = "trip with id: " + ctx.pathParam("id") +
                   " was not in valid JSON format. See API documentation for correct structure.";
       } else {
           message = bre.getMessage();
       }
       ctx.status(HttpStatus.BAD_REQUEST).json(Map.of("status", HttpStatus.BAD_REQUEST.getCode(), "msg", message));
   }
   catch (PersistenceException pe) {
       ctx.json(Map.of("status", HttpStatus.INTERNAL_SERVER_ERROR.getCode(), "msg", "Database problems, try again later"));
       debugLogProd.debug(formattedTime + "; Database error trying to update trip", pe);
   }
   catch (Exception e) {
       ctx.json(Map.of("status", HttpStatus.INTERNAL_SERVER_ERROR.getCode(), "msg", "Unexpected error updating trip" + ctx.pathParam("id")));
       debugLogProd.debug(formattedTime + "; Unexpected error trying to update trip:" + id + "OperationState: ", e);
   }
    }

    public void deleteTrip(Context ctx) {
        int id = 0;
        try {
            disableCache(ctx);
            id = Integer.parseInt(ctx.pathParam("id"));
            if (id > 0) {
                tripDAO.deleteTrip(id);
            }
            else {
                ctx.status(HttpStatus.BAD_REQUEST).json(Map.of("status",HttpStatus.BAD_REQUEST.getCode(),"message", "You need to type at id above 0"));
            }
        }
        catch (NumberFormatException ne) {
            ctx.json(Map.of("status", HttpStatus.BAD_REQUEST.getCode(), "msg",
                    "Invalid id format:" + ctx.pathParam("id")));
        }
        catch (ApiException ex){
            ctx.status(HttpStatus.NOT_FOUND).json(Map.of("status",  HttpStatus.NOT_FOUND.getCode(),
                    "message", "Trip with id: " + id + " Was not found"));
        }
        catch (PersistenceException pe) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json(Map.of("status", HttpStatus.INTERNAL_SERVER_ERROR.getCode(),
                    "msg", "There was a problem with the database"));
            debugLogProd.debug(formattedTime + "; Database problems while trying to delete Trip with Id: " + id, pe);
        }
        catch(Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json(Map.of("status", HttpStatus.INTERNAL_SERVER_ERROR.getCode(),
                    "msg", "There was an unexpected server error with the server"));
            debugLogProd.debug(formattedTime + "; Unexpected server while trying to delete Trip with Id: " + id, e);
        }
    }

    public void getTripsByCategory(Context ctx) {
        Category category;
        try {
           String request = ctx.queryParam("category");
           if(request != null && !request.isEmpty()) {

    category = Category.valueOf(request.toUpperCase());
               List<TripDTO> allTrips = TripConverters.convertToTripDTOList(tripDAO.getAllTrips());
          List<TripDTO> sortedTrips = allTrips.stream()
                       .filter( trip -> trip.getCategory().equals(category))
                       .toList();

               ctx.status(HttpStatus.OK).json(sortedTrips);
               }
        else{
            throw new BadRequestResponse("Selected category ned to be BEACH, CITY, FOREST, LAKE, SEA or SNOW");
        }
        }
        catch (IllegalArgumentException iae) {
            ctx.status(HttpStatus.BAD_REQUEST).json(Map.of("status", HttpStatus.BAD_REQUEST.getCode(),
                    "msg", "Selected category ned to be BEACH, CITY, FOREST, LAKE, SEA or SNOW"));
        }
        catch (PersistenceException pe) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json(Map.of("status", HttpStatus.INTERNAL_SERVER_ERROR.getCode(),
                    "msg",  "Database problems, try again later"));
            debugLogProd.error(formattedTime, "Database problems while getting all trips", pe);
        }
        catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json(Map.of("status", HttpStatus.INTERNAL_SERVER_ERROR.getCode(),
                    "msg", "There was an unexpected server error with the server"));
            debugLogProd.debug(formattedTime, " Unexpected server while trying to get all trips", e);
        }
    }

    public void totalPriceTripsByGuide(Context ctx) {
        try{
            disableCache(ctx);

            List<TripDTO> allTrips = TripConverters.convertToTripDTOList(tripDAO.getAllTrips());

               Map<Integer, Double> totalPrices = allTrips.stream()
               .collect(Collectors.groupingBy(TripDTO::getGuideId, Collectors.summingDouble(TripDTO::getPrice)));

               ctx.status(HttpStatus.OK).json(totalPrices);

        }
        catch (PersistenceException pe){

            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json(Map.of("status", HttpStatus.INTERNAL_SERVER_ERROR.getCode(),
                    "msg",  "Database problems, try again later"));
            debugLogProd.error(formattedTime, "Database problems while calculating price for trips for each guide", pe);
        }
        catch (NullPointerException npe) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json(Map.of("status", HttpStatus.INTERNAL_SERVER_ERROR.getCode(),
                    "msg",  "Database problems, try again later"));
            debugLogProd.error(formattedTime, "Database problems while calculating price for trips on guide, no list of Trips", npe);
        }
        catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json(Map.of("status", HttpStatus.INTERNAL_SERVER_ERROR.getCode(),
                    "msg",  "Database problems, try again later"));
            debugLogProd.error(formattedTime, "Unexpected error while calculating price for trips on guide, no list of Trips", e);
        }

    }
}