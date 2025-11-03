package app.security;

import app.exceptions.EntityNotFoundException;
import app.exceptions.ValidationException;
import app.config.HibernateConfig;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import dk.bugelhartmann.UserDTO;

import app.security.Role;
import org.mindrot.jbcrypt.BCrypt;

import java.util.stream.Collectors;

public class SecurityDAO implements ISecurityDAO{
    EntityManagerFactory emf;

    public SecurityDAO(EntityManagerFactory emf){
        this.emf = emf;
    }

    @Override
    public User getVerifiedUser(String username, String password) throws ValidationException {
        username = username.toLowerCase();


        try(EntityManager em = emf.createEntityManager()){
            User foundUser = em.find(User.class, username);
            System.out.println("🔹 Bruger fundet BEFORE IF: " + foundUser.getUsername());
            System.out.println("🔹 Hashed fra DB: " + foundUser.getPassword());
            System.out.println("🔹 Indtastet password: " + password);
            System.out.println("🔹 BCrypt check: BEFORE IF " + BCrypt.checkpw(password, foundUser.getPassword()));

            if(foundUser != null && foundUser.verifyPassword(password)) //foundUser.verifyPassword(password) == true
            {System.out.println("🔹 Bruger fundet: " + foundUser.getUsername());
                System.out.println("🔹 Hashed fra DB: " + foundUser.getPassword());
                System.out.println("🔹 Indtastet password: " + password);
                System.out.println("🔹 BCrypt check: " + BCrypt.checkpw(password, foundUser.getPassword()));

                return foundUser;
            }
        }
        return null;
    }

    @Override
    public User createUser(String username, String password) {
        try(EntityManager em = emf.createEntityManager()){
            username = username.toLowerCase();
            User user = new User(username, password);
            em.getTransaction().begin();
            em.persist(user);
            em.getTransaction().commit();
            return user;
        }
    }

    @Override
    public Role createRole(String roleName) {
        roleName = roleName.toUpperCase();
        Role role = new Role(roleName);
        try (EntityManager em = emf.createEntityManager()) {

            em.getTransaction().begin();
            em.persist(role);
            em.getTransaction().commit();
            return role;
        }
    }
    @Override
    public User addUserRole(String username, String rolename) {
        username = username.toLowerCase();
        rolename = rolename.toUpperCase();
        try (EntityManager em = emf.createEntityManager()) {
            User foundUser = em.find(User.class, username);

            Role foundRole = em.find(Role.class, rolename);
            if (foundUser == null || foundRole == null) {
                throw new IllegalArgumentException("User and role not found");
            }
            foundUser.addRole(foundRole);
            em.getTransaction().begin();
            em.merge(foundUser);
            em.getTransaction().commit();
            return foundUser;
        }
    }
}
