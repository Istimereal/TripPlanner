package app.service;

import app.config.HibernateConfig;
import app.entities.Guide;
import app.entities.Trip;
import app.enums.Category;
import app.security.Role;
import app.security.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static app.enums.Category.*;

public class Populator {

    private final EntityManagerFactory emf;
    public Populator(EntityManagerFactory emf){ this.emf = emf; }

    public void createAdminAndRolesForProdDB(){

            try (EntityManager em = emf.createEntityManager()) {
                em.getTransaction().begin();

                Role adminRole = new Role("Admin");
                Role userRole = new Role("User");
                Role anyRole = new Role("Anyone");
                em.persist(adminRole);
                em.persist(userRole);
                em.persist(anyRole);

                User admin = new User("admin", "tpas2025");
                adminRole.addUser(admin);
                em.persist(admin);

                em.getTransaction().commit();

                System.out.println("Production database have a Admin");
            }
    }

    public void createUsersAndRolesTest() {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();

            // Ryd relationstabel først (ellers FK-fejl)
            em.createNativeQuery("DELETE FROM role_user").executeUpdate();
            // Slet Users før Roles (ellers FK-fejl)
            em.createQuery("DELETE FROM Role").executeUpdate();
            em.createQuery("DELETE FROM User").executeUpdate();

            Role adminRole = new Role("Admin");
            Role userRole = new Role("User");
            Role anyRole = new Role("Anyone");
            em.persist(adminRole);
            em.persist(userRole);
            em.persist(anyRole);

            User admin = new User("admin", "admin123");
            User normalUser = new User("user1", "pass123");

            adminRole.addUser(admin);
            userRole.addUser(normalUser);

            em.persist(admin);
            em.persist(normalUser);

            em.getTransaction().commit();

            System.out.println("Users and roles created successfully!");
        }
    }

    public void poppulateDBTest(){
        try (EntityManager em = emf.createEntityManager()) {
em.getTransaction().begin();
            Guide g1 = Guide.builder()
                    .name("tim")
                    .email("tim@guide.dk")
                    .phoneNumber(12345678)
                    .experienceInYears(4)
                    .trips(new ArrayList<>())
                    .build();

            Guide g2 = Guide.builder()
                    .name("max")
                    .email("max@guide.dk")
                    .phoneNumber(34567812)
                    .experienceInYears(2)
                    .trips(new ArrayList<>())
                    .build();

            LocalDateTime start1 = LocalDateTime.of(2025, 11, 10, 8, 30);
            LocalDateTime start2 = LocalDateTime.of(2025, 11, 18, 8, 30);
            LocalDateTime end1 = LocalDateTime.of(2025, 11, 14, 8, 30);
            LocalDateTime end2 = LocalDateTime.of(2025, 1, 24, 8, 30);
            Category category1 = LAKE;
            Category cat2 = BEACH;

            Trip t1 = Trip.builder()
                    .name("ski")
                    .startTime(start1)
                    .endTime(end1)
                    .locationCordinates("10.103.23")
                    .price(4000)
                    .category(category1)
                    .build();
            Trip t2 = Trip.builder()
                    .name("tenerife")
                    .startTime(start1)
                    .endTime(end1)
                    .locationCordinates("10.103.25")
                    .price(7000)
                    .category(cat2)
                    .build();

            em.persist(t1);
            em.persist(t2);

            g1.addTrip(t1);
            g2.addTrip(t2);

            em.persist(g1);
            em.persist(g2);

            em.getTransaction().commit();
        }
        try (EntityManager em = emf.createEntityManager()) {
            List<Guide> guides = em.createQuery("SELECT g FROM Guide g", Guide.class).getResultList();
            System.out.println("Guides in DB: " + guides.size());
        }
    }
}

