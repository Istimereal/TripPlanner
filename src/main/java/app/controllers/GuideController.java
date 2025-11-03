package app.controllers;

import app.daos.GuideDAO;
import app.dtos.GuideDTO;
import app.entities.Guide;
import app.exceptions.ApiException;
import app.service.GuideConverters;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.io.JsonEOFException;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import jakarta.persistence.PersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UncheckedIOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import static app.utils.ResponseUtil.disableCache;

public class GuideController {

    LocalDateTime timeStamp = LocalDateTime.now();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    String formattedTime = timeStamp.format(formatter);

    private static final Logger logger = LoggerFactory.getLogger("production");
    private static final Logger debugLogProd = LoggerFactory.getLogger("debug");
    private final GuideDAO guideDAO;

    public GuideController(GuideDAO guideDAO) {
        this.guideDAO = guideDAO;
    }
    
    public void createGuide(Context ctx) {
        try {
            GuideDTO newGuide = ctx.bodyAsClass(GuideDTO.class);
            if(newGuide == null){
                throw new ApiException(400, "Invalid post, see documentation for correct form");
            }

            GuideDTO createdGuide = GuideConverters.convertToGuideDTO(guideDAO.createGuide(GuideConverters.convertToGuide(newGuide)));
            ctx.status(HttpStatus.CREATED).json(createdGuide);
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
            debugLogProd.debug(formattedTime, "Error with database trying to create guide", ae);
        }
        catch(Exception e) {
            if (
                    e.getCause() instanceof com.fasterxml.jackson.core.JacksonException) {
                ctx.status(HttpStatus.BAD_REQUEST).json(Map.of(
                        "status", HttpStatus.BAD_REQUEST.getCode(),
                        "msg", "Invalid post, see documentation for correct form"
                ));
            } else {
                ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json(Map.of(
                        "status", HttpStatus.INTERNAL_SERVER_ERROR.getCode(),
                        "msg", "There was an unexpected problem with the server"
                ));
                debugLogProd.error(formattedTime, "Unexpected server problem while creating a Guide", e);
            }}
    }

public void geAllGuides(Context ctx){
    try {
        disableCache(ctx);
        List<GuideDTO> guideDTOs = GuideConverters.convertToGuideDTO(guideDAO.getAllGuides());
        if(guideDTOs.isEmpty()) {
            ctx.status(HttpStatus.NOT_FOUND).json(Map.of("status",HttpStatus.NOT_FOUND.getCode(),
                    "msg", "No Guides in database"));
            logger.warn("No Guides in database");
        }
        else {
            ctx.status(200).json(guideDTOs);
        }
    }
    catch (ApiException ae){
        int code = ae.getStatusCode();
        ctx.status(code).json(Map.of("status", code,
                "msg","Database problems, try agian later"));
        debugLogProd.debug(formattedTime, "Error with database trying to get all guides", ae);

    }
    catch (Exception e) {
        ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json(Map.of("status",
                HttpStatus.INTERNAL_SERVER_ERROR.getCode(), "msg",
                "There was an unexpected error with the server, try again later"));
        debugLogProd.debug(formattedTime, "unexpected error with the server from createGuide ", e);
    }
}

public  void getGuideById(Context ctx){
    int id = 0;
    try {
        disableCache(ctx);
        id = Integer.parseInt(ctx.pathParam("id"));
        if (id > 0) {
            GuideDTO GuideDTO = GuideConverters.convertToGuideDTO(guideDAO.getGuideById(id));
            ctx.status(200).json(GuideDTO);
        }
        else {
            ctx.status(HttpStatus.BAD_REQUEST).json(Map.of("status",HttpStatus.BAD_REQUEST.getCode(),
                    "msg", "You need to type at id above 0"));
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
        debugLogProd.debug(formattedTime, "Error with database trying to find Guide by Id: " + id + " ", ae);
    }
    catch (Exception e) {
        ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json(Map.of("status",
                HttpStatus.INTERNAL_SERVER_ERROR.getCode(), "msg",
                "There was an unexpected error with the server, try again later"));
        debugLogProd.debug(formattedTime, "unexpected error with the server trying to find Guide by ID: ", e);
    }
}

public void updateGuide(Context ctx){

    int id = 0;
    try {
        id = Integer.parseInt(ctx.pathParam("id"));
        if (id > 0) {
            Guide guide = guideDAO.getGuideById(id);

            if (guide == null) {
                ctx.status(HttpStatus.NOT_FOUND).json(Map.of("status", HttpStatus.NOT_FOUND.getCode(),
                        "msg", "Guide not found"));
                return;
            }
        }
        GuideDTO guideUpdate = ctx.bodyAsClass(GuideDTO.class);
        if(guideUpdate.getName() != null && guideUpdate.getName().isEmpty()) {
            throw new BadRequestResponse("Name cannot be empty, exclude or put desired name");
        }
        if(guideUpdate.getEmail() != null && guideUpdate.getEmail().isEmpty()) {
            throw new BadRequestResponse("Email cannot be empty, exclude or put desired adress");
        }
        Guide forUpdate = GuideConverters.convertToGuide(guideUpdate);
        Guide updateResult =  guideDAO.updateGuide(id, forUpdate);
        GuideDTO updated = GuideConverters.convertToGuideDTO(updateResult);
        ctx.status(HttpStatus.OK).json(updated);
    }
    catch(BadRequestResponse bre) {
        String message;

        if (bre.getMessage() == null || bre.getMessage().isBlank()) {
            // A: JSON-formatfejl (bodyAsClass fejlede)
            message = "Guide with id: " + ctx.pathParam("id") +
                    " was not in valid JSON format. See API documentation for correct structure.";
        } else { message = bre.getMessage(); }
        ctx.status(HttpStatus.BAD_REQUEST).json(Map.of("status", HttpStatus.BAD_REQUEST.getCode(), "msg", message));
    }
    catch (ApiException ae){
        int code = ae.getStatusCode();
        ctx.status(code).json(Map.of("status", code,
                "msg","Database problems, try agian later"));
        debugLogProd.debug(formattedTime, "Error with database trying to trying to update Guide: " + id + " ", ae);
    }
    catch (Exception e) {
        ctx.json(Map.of("status", HttpStatus.INTERNAL_SERVER_ERROR.getCode(), "msg", "Unexpected error updating Guide" + ctx.pathParam("id")));
        debugLogProd.debug(formattedTime + "; Unexpected error trying to update Guide:" + id + "OperationState: ", e);
    }    
}

public void deleteGuide(Context ctx){
   Integer id = 0;
    try {
        disableCache(ctx);
        id = Integer.parseInt(ctx.pathParam("id"));
        if (id > 0) {
            guideDAO.deleteGuide(id);
            ctx.json(Map.of("status", HttpStatus.OK.getCode(), "msg", "Guide deleted"));
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
        debugLogProd.debug(formattedTime, "Error with database trying to trying to delete Guide: " + id + " ", ae);
    }
    catch(Exception e) {
        ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json(Map.of("status", HttpStatus.INTERNAL_SERVER_ERROR.getCode(),
                "msg", "There was an unexpected server error with the server"));
        debugLogProd.debug(formattedTime + "; Unexpected server while trying to delete Guide with Id: " + id, e);
    }
}
}

