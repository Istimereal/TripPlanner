package app.config;

import app.controllers.E1Controller;
import app.controllers.E2Controller;
import app.daos.D1DAO;
import app.daos.E2DAO;
import app.exceptions.ApiException;
import app.routes.E1Routes;
import app.routes.E2Routes;
import app.routes.Routes;
import app.routes.SecurityRoutes;
import app.security.SecurityController;
import app.security.SecurityDAO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.javalin.Javalin;
import jakarta.persistence.EntityManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class ApplicationConfig {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationConfig.class);
    private static final ObjectMapper jsonMapper = new ObjectMapper();

    public static Javalin startServer(int port, EntityManagerFactory emf) {

        // Init security + routes

        D1DAO D1DAO = D1DAO.getInstance(emf);
        E2DAO E2DAO = E2DAO.getInstance(emf);

        E1Controller E1Controller = new E1Controller(D1DAO);
        E2Controller E2Controller = new E2Controller(E2DAO, emf);

        SecurityDAO securityDAO = new SecurityDAO(emf);
        SecurityController securityController = new SecurityController(securityDAO);
        SecurityRoutes securityRoutes = new SecurityRoutes(securityController);

        E1Routes E1Routes = new E1Routes(E1Controller);
        E2Routes E2Routes = new E2Routes(E2Controller);

        Routes routes = new Routes(E1Routes, E2Routes);

        Javalin app = Javalin.create(config -> {
            config.showJavalinBanner = false;
            config.bundledPlugins.enableRouteOverview("/routes");
            config.router.contextPath = "/api/v1";

            // 👇 BEGGE linjer her skal være der
            config.router.apiBuilder(routes.getEndpoints());
            config.router.apiBuilder(securityRoutes.getSecurityRoutes());
        });


        // Åbne endpoints (ingen auth)
        app.get("/", ctx -> ctx.json(Map.of("status", "API is running ✅")));

        // Security filters (kører før matched routes)
        app.beforeMatched(securityController::authenticate);
        app.beforeMatched(securityController::authorize);

        // CORS + exception handling
        setCORS(app);
        setGeneralExceptionHandling(app);

        // Dev logging
        if (System.getenv("DEPLOYED") == null) {
            beforeFilter(app);
        }

        app.start(port);
        return app;
    }

    private static void setCORS(Javalin app) {
        app.before(ApplicationConfig::setCorsHeaders);
        app.options("/*", ApplicationConfig::setCorsHeaders);
    }

    private static void setCorsHeaders(io.javalin.http.Context ctx) {
        ctx.header("Access-Control-Allow-Origin", "*");
        ctx.header("Access-Control-Allow-Methods", "GET, POST, PUT, PATCH, DELETE, OPTIONS");
        ctx.header("Access-Control-Allow-Headers", "Content-Type, Authorization");
        ctx.header("Access-Control-Allow-Credentials", "true");
    }
    //ændringer
    private static void setGeneralExceptionHandling(Javalin app) {
        app.exception(Exception.class, (e, ctx) -> {
            int statusCode = (e instanceof ApiException apiEx) ? apiEx.getStatusCode() : 500;
            String message = (e instanceof ApiException) ? e.getMessage() : "Internal server error";

            logger.error("An exception occurred", e);

            ObjectNode on = jsonMapper.createObjectNode()
                    .put("status", statusCode)
                    .put("msg", message);

            ctx.json(on);
            ctx.status(statusCode);
        });
    }
    private static void beforeFilter(Javalin app) {
        app.before(ctx -> {
            // Debug-request headers, valgfrit
            ctx.req().getHeaderNames().asIterator().forEachRemaining(System.out::println);
        });
    }
}
