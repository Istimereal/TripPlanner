package app.routes;

import app.controllers.TripController;
import app.security.SecurityController.Role;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.*;

public class TripRoutes {


    private final TripController tripController;

    public TripRoutes(TripController tripController) {
        this.tripController = tripController;
    }


    public EndpointGroup getTripRoutes() {
        return () -> {

            get("/", tripController::getTrips, Role.ADMIN, Role.USER);

            get("/{id}", tripController::getTripById, Role.ADMIN, Role.USER);

            post("/", tripController::createTrip, Role.ADMIN);

            put("/{id}", tripController::updateTrip, Role.ADMIN);

            delete("/{id}", tripController::deleteTrip, Role.ADMIN);

            get("/guides/totalprice", tripController::totalPriceTripsByGuide, Role.ADMIN);


      //      put("/{tripId}/guides/{guideId}", tripController::linkGuide, Role.ADMIN);


          //  get("/guides/totalprice", tripController::getTotalPrice, Role.ADMIN, Role.USER);
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
