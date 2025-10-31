package app.daos;

import app.entities.Guide;
import app.exceptions.ApiException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.TypedQuery;
import org.hibernate.exception.ConstraintViolationException;

import java.util.List;

public class GuideDAO {
    private static GuideDAO instance;
    private static EntityManagerFactory emf;

    private GuideDAO() {}

    public static GuideDAO getInstance(EntityManagerFactory _emf) {

        if (instance == null) {
            instance = new GuideDAO();
            emf = _emf;
        }
        return instance;
    }

    public Guide createGuide(Guide guide) {

        try(EntityManager em = emf.createEntityManager()) {

            em.getTransaction().begin();
            em.persist(guide);
            em.getTransaction().commit();
        }
        catch (ConstraintViolationException cve){
            throw new ApiException(400, "Guide already exists");
        }
        catch (PersistenceException pe) {

            throw new ApiException(500,"Persistence error");
        }
        catch (Exception e) {
            throw new ApiException(500, "unknown error");
        }
        return guide;
    }

    public List<Guide> getAllGuides() {
        try (EntityManager em = emf.createEntityManager()) {
            TypedQuery<Guide> query = em.createQuery(("SELECT g FROM guide g"), Guide.class);
            return query.getResultList();
        }
        catch (PersistenceException pe) {
            throw new ApiException(500, "Persistence error");
    }
        catch (Exception e) {
            throw new ApiException(500, "unknown error");
        }
    }

    public Guide getGuideById(int id) {

        try (EntityManager em = emf.createEntityManager()) {
   Guide guide =  em.find(Guide.class, id);
            if(guide == null) {
                throw new ApiException(400, "Guide with" + id + "Does not exist");
            }
            return guide;
        }
        catch (Exception e) {
            throw new ApiException(500, "unexpected error");
        }
    }

    public Guide updateGuide(Integer integer, Guide guide) {

        try(EntityManager em = emf.createEntityManager()) {
           Guide target = em.find(Guide.class, integer);
           if(target == null) {
               throw new ApiException(400, "Guide with" + guide.getId() + "Does not exist");
           }
           if(guide.getName() != null) {
               target.setName(guide.getName());
           }
           if(guide.getEmail() != null) {
               target.setEmail(guide.getEmail());
           }
           if (guide.getPhoneNumber() != 0){
               target.setPhoneNumber(guide.getPhoneNumber());
           }
           if(guide.getTrips() != null) {
               target.setTrips(guide.getTrips());
           }
           em.getTransaction().begin();
           em.merge(target);
           em.getTransaction().commit();
        }
        catch (PersistenceException pe) {
            throw new ApiException(500, "Persistence error");
        }
        catch (Exception e) {
            throw new ApiException(500, "unknown error");
        }
        return guide;
    }

    public void deleteGuide(Integer integer) {
        try(EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();

        Guide delete = em.find(Guide.class, integer);
            if(delete == null) {
                throw new ApiException(400, "Guide with" + integer + "Does not exist");
            }
            em.remove(delete);
            em.getTransaction().commit();
        }
        catch (PersistenceException pe) {
            throw new ApiException(500, "Persistence error");
        }
        catch (Exception e) {
            throw new ApiException(500, "unknown error");
        }
    }
}
