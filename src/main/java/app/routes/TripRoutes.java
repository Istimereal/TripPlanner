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

            get("/", tripController::getTrips, Role.ADMIN, Role.USER);
            get("/{id}", tripController::getTripById, Role.ADMIN, Role.USER);
            post("/", tripController::createTrip, Role.ADMIN);
            put("/{id}", tripController::updateTrip, Role.ADMIN);
            delete("/{id}", tripController::deleteTrip, Role.ADMIN);
            get("/guides/totalprice", tripController::totalPriceTripsByGuide, Role.ADMIN);
            get("/{id}/packing/weight", tripController::getPackingWeight, Role.ADMIN, Role.USER);
            put("/{tripId}/guides/{guideId}", tripController::linkGuideToTrip, Role.ADMIN);
            // POST /guides
             post("/guides", guideController::createGuide, Role.ADMIN);
        };
    }

    /*
    public EndpointGroup getTripRoutes() {

        return  () -> {

            get(ctx -> tripController.getAllTrips(ctx), Role.ADMIN, Role.USER);
            get(ctx -> tripController.getTripById(ctx), Role.ADMIN, Role.USER);
            post(ctx -> tripController.createTrip(ctx), Role.ADMIN);
            patch(ctx -> tripController.updateTrip(ctx), Role.ADMIN);
            delete(ctx -> tripController.deleteTrip(ctx), Role.ADMIN);
            get(ctx -> tripController.getTripsByCategory(ctx), Role.ADMIN, Role.USER);

        };
    }  */
}
