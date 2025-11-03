package app.controllers;

import app.daos.TripDAO;
import app.dtos.PackingListDTO;
import app.dtos.TripDTO;
import app.entities.Trip;
import app.enums.Category;
import app.exceptions.ApiException;
import app.service.PackingService;
import app.service.TripConverters;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import jakarta.persistence.PersistenceException;
import org.hibernate.dialect.function.DB2SubstringFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static app.utils.ResponseUtil.disableCache;

public class TripController {
    LocalDateTime timeStamp = LocalDateTime.now();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    String formattedTime = timeStamp.format(formatter);

    private static final Logger logger = LoggerFactory.getLogger("production");
    private static final Logger debugLogProd = LoggerFactory.getLogger("debug");
    private final TripDAO tripDAO;

    public TripController(TripDAO tripDAO) {
        this.tripDAO = tripDAO;
    }

      public void getTrips(Context ctx) {

       Category category;
          try {
            disableCache(ctx);

            String request = ctx.queryParam("category");

            if(request != null && !request.isEmpty()) {
                try {
                    category = Category.valueOf(request.toUpperCase());
                } catch (IllegalArgumentException iae) {
                    ctx.status(HttpStatus.BAD_REQUEST).json(Map.of("status", HttpStatus.BAD_REQUEST.getCode(),
                            "msg", "Invalid category. Valid categories are: beach, city, forest, lake, sea, snow"));
                    return;
                }
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
                    ctx.status(HttpStatus.NOT_FOUND).json(Map.of("status", HttpStatus.NOT_FOUND.getCode(), "msg", "No trips in database"));
                    logger.warn("No trips in database");
                } else {
                    ctx.status(200).json(tripDTOs);
                }
            }
        }
          catch (ApiException ae){
              int code = ae.getStatusCode();
              ctx.status(code).json(Map.of("status", code,
                      "msg","Database problems, try agian later"));
              debugLogProd.debug(formattedTime, "Error with database trying to trying to get all", ae);
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
        Category category;
        try {
            disableCache(ctx);
            id = Integer.parseInt(ctx.pathParam("id"));
            if (id > 0) {
                TripDTO tripDTO = TripConverters.convertToTripDTO(tripDAO.getTripById(id));
                category = tripDTO.getCategory();
                String request =category.name().toLowerCase();
                PackingListDTO packingList = PackingService.getTripPackingList(request);
                tripDTO.setPackingList(packingList);

                ctx.status(200).json(tripDTO);
            }
            else {
                ctx.status(HttpStatus.BAD_REQUEST).json(Map.of("status",HttpStatus.BAD_REQUEST.getCode(),"msg", "You need to type at id above 0"));
            }
        }
        catch (NumberFormatException ne) {
            ctx.status(HttpStatus.BAD_REQUEST.getCode()).json(Map.of("status", HttpStatus.BAD_REQUEST.getCode(), "msg",
                    "Invalid id format: " + ctx.pathParam("id")));
        }
        catch (ApiException ae) {
            int code = ae.getStatusCode();
           String msg = "";
           String debugMsg = "";
    if(code == 404){ msg = "Trip with id " + id + " not found in database";}
    else {
        msg = "Problems getting packing items externally, try again later";
        debugLogProd.error(formattedTime, debugMsg, ae);
    }
        ctx.status(code).json(
                    Map.of("status", HttpStatus.forStatus(code).getCode(),
                    "msg", msg ));
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
        catch (ApiException ae){
            int code = ae.getStatusCode();
            ctx.status(code).json(Map.of("status", code,
                    "msg","Database problems, try agian later"));
            debugLogProd.debug(formattedTime, "Error with database trying to trying to create trip", ae);
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
               ctx.status(HttpStatus.NOT_FOUND).json(Map.of("status", HttpStatus.NOT_FOUND.getCode(), "msg", "guide not found"));
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
   catch (ApiException ae){
       int code = ae.getStatusCode();
       ctx.status(code).json(Map.of("status", code,
               "msg","Database problems, try agian later"));
       debugLogProd.debug(formattedTime, " Database error trying to update trip ", ae);
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
                ctx.status(HttpStatus.OK).json(Map.of("status",HttpStatus.OK.getCode(),"msg", "trip with id: " + id + " was deleted"));
            }
            else {
                ctx.status(HttpStatus.BAD_REQUEST).json(Map.of("status",HttpStatus.BAD_REQUEST.getCode(),"msg", "You need to type at id above 0"));
            }
        }
        catch (NumberFormatException ne) {
            ctx.json(Map.of("status", HttpStatus.BAD_REQUEST.getCode(), "msg",
                    "Invalid id format:" + ctx.pathParam("id")));
        }
        catch (ApiException ae){
            int code = ae.getStatusCode();
            ctx.status(code).json(Map.of("status", code,
                    "msg","Database problems, try agian later"));
            debugLogProd.debug(formattedTime, " Database error trying to delete trip ", ae);
        }
        catch(Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json(Map.of("status", HttpStatus.INTERNAL_SERVER_ERROR.getCode(),
                    "msg", "There was an unexpected server error with the server"));
            debugLogProd.debug(formattedTime + "; Unexpected server while trying to delete Trip with Id: " + id, e);
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
        catch (ApiException ae){
            int code = ae.getStatusCode();
            ctx.status(code).json(Map.of("status", code,
                    "msg","Database problems, try agian later"));
            debugLogProd.debug(formattedTime, " Database error trying to get all trips in: totalPriceTripsByGuide", ae);
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

    public void getPackingWeight(Context ctx) {
        PackingService packingService = new PackingService();
        int id = 0;
        Category category;
        try {
            disableCache(ctx);
            id = Integer.parseInt(ctx.pathParam("id"));
            if (id > 0) {
                TripDTO tripDTO = TripConverters.convertToTripDTO(tripDAO.getTripById(id));
                category = tripDTO.getCategory();
                PackingListDTO packingList = packingService.getTripPackingList(category.name().toLowerCase());
                Integer total = PackingService.calcPackingTotalWeight(packingList);
                tripDTO.setPackingList(packingList);
                ctx.status(200).json(Map.of("status", HttpStatus.OK.getCode(),"msg","Weight in grams: " + total));
            }
            else {
                ctx.status(HttpStatus.BAD_REQUEST).json(Map.of("status",HttpStatus.BAD_REQUEST.getCode(),"msg", "You need to type at id above 0"));
            }
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
        catch (NumberFormatException nfe){
            ctx.status(HttpStatus.BAD_REQUEST).json(Map.of("status", HttpStatus.BAD_REQUEST.getCode(), "msg",
                    "You need to type Id format correct, like: 1"));
        }
        catch (ApiException ae) {
            int code = ae.getStatusCode();
            String msg = "";
            String debugMsg = "";
            if(code == 404){ msg = "Trip with id " + id + " not found in database";
                debugMsg = "Not a error. Trip not found in db";}
            else {msg = "problems getting trip with Id: " + id + "try again later";
                debugMsg = "Problems with database trying to get trip with Id: " + id;}
            ctx.status(code).json(
                    Map.of("status", HttpStatus.forStatus(code).getCode(),
                            "msg", msg ));
            debugLogProd.error(formattedTime, debugMsg, ae);
        }
        catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR.getCode()).json(Map.of("status", HttpStatus.INTERNAL_SERVER_ERROR.getCode(),
                    "msg", "Unexpected error updating trip" + ctx.pathParam("id")));
            debugLogProd.debug(formattedTime + "; Unexpected error deliver packaging weight", e);
        }
    }

    public void linkGuideToTrip(Context ctx) {
        Integer tripId = 0;
        Integer guideId = 0;

        try {
            disableCache(ctx);
            tripId = Integer.parseInt(ctx.pathParam("tripId"));
            guideId = Integer.parseInt(ctx.pathParam("guideId"));

            if (tripId <= 0) {
                ctx.status(HttpStatus.BAD_REQUEST).json(Map.of("status", HttpStatus.BAD_REQUEST.getCode(),
                        "msg", "TripId must be greater than 0"));
                return;
            }
            if (guideId <= 0) {
                ctx.status(HttpStatus.BAD_REQUEST).json(Map.of("status", HttpStatus.BAD_REQUEST.getCode(),
                        "msg", "GuideId must be greater than 0"
                ));
                return;
            }
            tripDAO.addGuideToTrip(tripId, guideId);
            ctx.status(HttpStatus.OK).json(Map.of("status", HttpStatus.OK.getCode(), "msg", "Guide have been added"));
        }
        catch (NumberFormatException ne) {
            ctx.json(Map.of("status", HttpStatus.BAD_REQUEST.getCode(), "msg",
                    "Invalid id format:" + ctx.pathParam("id")));
        }
        catch (ApiException ae) {
            int code = ae.getStatusCode();
            if (code == 404) {
                ctx.status(code).json(Map.of("status", code, "msg", ae.getMessage()));
            } else {
                ctx.status(code).json(Map.of("status", code, "msg", "problems with database, try again later"));
                debugLogProd.error(formattedTime, "DB error in linkGuideToTrip", ae);
            }
        }
        catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json(Map.of("status",
                    HttpStatus.INTERNAL_SERVER_ERROR.getCode(), "msg",
                    "There was an unexpected error with the server, try again later"));
            debugLogProd.debug(formattedTime, "unexpected error with the server trying to add guide to trip: ", e);
        }
    }
}