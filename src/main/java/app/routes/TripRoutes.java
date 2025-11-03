package app.routes;

import app.controllers.GuideController;
import app.controllers.TripController;
import app.dtos.TripDTO;
import app.enums.Category;
import app.security.SecurityController.Role;
import app.service.TripConverters;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import jakarta.persistence.PersistenceException;

import java.util.List;
import java.util.Map;

import static io.javalin.apibuilder.ApiBuilder.*;

public class TripRoutes {


    private final TripController tripController;
    private final GuideController guideController;

    public TripRoutes(TripController tripController, GuideController guideController) {
        this.tripController = tripController;
        this.guideController = guideController;
    }

    public EndpointGroup getTripRoutes() {
        return () -> {

            // Mere specifikke/statiske STIER først
            get("/guides/totalprice", tripController::totalPriceTripsByGuide, Role.ADMIN);

            // Understier med id (tilføj regex på id)
            get("/{id}/packing/weight", tripController::getPackingWeight, Role.ADMIN, Role.USER);
            put("/{tripId}/guides/{guideId}", tripController::linkGuideToTrip, Role.ADMIN);


            get("/", tripController::getTrips, Role.ADMIN, Role.USER);  // ?category=lake gives all trips by a category
            post("/", tripController::createTrip, Role.ADMIN);
            put("/{id}", tripController::updateTrip);
            delete("/{id}", tripController::deleteTrip, Role.ADMIN);


            get("/{id}", tripController::getTripById, Role.ADMIN, Role.USER);


            post("/guides", guideController::createGuide, Role.ADMIN);

           /*
            put("/{tripId}/guides/{guideId}", tripController::linkGuideToTrip, Role.ADMIN);
            get("/{id}/packing/weight", tripController::getPackingWeight, Role.ADMIN, Role.USER);
            put("/{id}", tripController::updateTrip, Role.ADMIN);
            get("/", tripController::getTrips, Role.ADMIN, Role.USER);
            post("/", tripController::createTrip, Role.ADMIN);
            delete("/{id}", tripController::deleteTrip, Role.ADMIN);
            get("/{id:\\\\d+}", tripController::getTripById, Role.ADMIN, Role.USER);
            get("/guides/totalprice", tripController::totalPriceTripsByGuide, Role.ADMIN);


            // POST /guides
             post("/guides", guideController::createGuide, Role.ADMIN);  */
        };
    }

}
