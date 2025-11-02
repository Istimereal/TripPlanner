package app;

import app.config.ApplicationConfig;
import app.config.HibernateConfig;
import app.service.Populator;
import jakarta.persistence.EntityManagerFactory;

public class Main {

    public static void main(String[] args) {

       // EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory("tripplanner");
        EntityManagerFactory emfTest = HibernateConfig.getEntityManagerFactoryForTest();

        Populator populator = new Populator(emfTest);
      //  populator.createAdminAndRolesForProdDB();
       populator.createUsersAndRolesTest();
       populator.poppulateDBTest();

        ApplicationConfig.startServer(7080, emfTest);
    }
}
