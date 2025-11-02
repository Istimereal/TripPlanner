package app.routes;

import app.controllers.GuideController;
import app.controllers.TripController;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.*;

public class Routes {

    private final GuideRoutes guideRoutes;
    private final TripRoutes tripRoutes;

    public Routes(GuideRoutes  guideRoutes, TripRoutes tripRoutes) {
        this.guideRoutes = guideRoutes;
        this.tripRoutes = tripRoutes;
    }

    public EndpointGroup getEndpoints() {

        return () -> {
            path("/guides", guideRoutes.getGuideRoutes());
            path("/trips", tripRoutes.getTripRoutes());
        };
    }
}
