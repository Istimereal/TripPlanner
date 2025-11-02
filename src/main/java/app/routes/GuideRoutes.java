package app.routes;

import app.controllers.GuideController;
import io.javalin.apibuilder.EndpointGroup;
import static io.javalin.apibuilder.ApiBuilder.*;
import app.security.SecurityController.Role;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GuideRoutes {

private final GuideController guideController;

public GuideRoutes(GuideController guideController) {
    this.guideController = guideController;
}


    public EndpointGroup getGuideRoutes() {
        return () -> {
            // GET /guides
            get("/", guideController::geAlltGuides, Role.ADMIN, Role.USER);

            // GET /guides/{id}
            get("/{id}", guideController::getGuideById, Role.ADMIN, Role.USER);

            // POST /guides
            post("/", guideController::createGuide, Role.ADMIN);

            // PUT /guides/{id}
            put("/{id}", guideController::updateGuide, Role.ADMIN);

            // DELETE /guides/{id}
            delete("/{id}", guideController::deleteGuide, Role.ADMIN);
        };
    }


/*
public EndpointGroup getGuideRoutes() {

    return ()->{
        get(ctx -> guideController.geAlltGuides(ctx), Role.ADMIN, Role.USER );
        get(ctx -> guideController.getGuideById(ctx), Role.ADMIN);
        post(ctx -> guideController.createGuide(ctx), Role.ADMIN);
        post(ctx -> guideController.updateGuide(ctx), Role.ADMIN);
        delete(ctx -> guideController.deleteGuide(ctx), Role.ADMIN);
    };
}  */
}
