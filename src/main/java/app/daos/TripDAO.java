package app.daos;

import app.entities.Trip;
import app.exceptions.ApiException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.TypedQuery;

import java.util.List;

public class TripDAO {

    private static  TripDAO instance;
    private static EntityManagerFactory emf;

    public TripDAO(){}

    public static TripDAO getInstance(EntityManagerFactory _emf) {

        if(instance==null){
            instance= new TripDAO();
        emf = _emf; }
        return instance;
    }

    public Trip createTrip(Trip trip)
    {
        try(EntityManager em=emf.createEntityManager()){
            em.getTransaction().begin();
            em.persist(trip);
            em.getTransaction().commit();
        }
        catch (PersistenceException pe){
            throw new ApiException(400,"Persistence error");
        }
        catch (Exception ex){
            throw new ApiException(400, "unexpected error");
        }
        return trip;
    }

    public Trip getTrip(int id) {
        try(EntityManager em=emf.createEntityManager()){
            Trip found =em.find(Trip.class, id);
         if(found==null)
             throw new ApiException(400,"Trip not found");
         return found;
        }
        catch (Exception ex){
            throw new ApiException(400, "unexpected error");
        }
    }

    public List<Trip> getAllTrips(){
        try(EntityManager em=emf.createEntityManager()){

            TypedQuery<Trip> query = em.createNamedQuery("Trip.findAll",Trip.class);
          return query.getResultList();
        }
        catch (PersistenceException pe){
            throw new ApiException(400,"Persistence error");
        }
        catch (Exception ex){
            throw new ApiException(400, "unexpected error");
        }
    }

    public Trip updateTrip(Integer integer, Trip trip){

        try(EntityManager em=emf.createEntityManager()){

            Trip target = em.find(Trip.class, integer);
            if(target==null){
               throw new ApiException(400,"Trip with id "+integer+" not found");
        }
            if(trip.getName() !=null){
                target.setName(trip.getName());
            }
            if(trip.getStartTime() !=null){
                target.setStartTime(trip.getStartTime());
            }
            if(trip.getEndTime() !=null){
                target.setEndTime(trip.getEndTime());
            }
            if(trip.getLocationCordinates()!=null){
                target.setLocationCordinates(trip.getLocationCordinates());
            }
            if (trip.getPrice() != 0){
                target.setPrice(trip.getPrice());
            }
            if(trip.getCategory()!=null){
                target.setCategory(trip.getCategory());
            }
            if(trip.getGuide()!=null){
                target.setGuide(trip.getGuide());
            }
            em.getTransaction().begin();
            em.merge(target);
            em.getTransaction().commit();
            return target;
        }
        catch (PersistenceException pe){
            throw new ApiException(400,"Persistence error");
        }
        catch (Exception ex){
            throw new ApiException(400, "unexpected error");
        }
    }

    public void deleteTrip(Integer integer){
        try(EntityManager em=emf.createEntityManager()){
            Trip target = em.find(Trip.class, integer);
            if(target==null){
               throw new ApiException(400,"Trip with id "+integer+" not found");
            }
            em.getTransaction().begin();
            em.remove(target);
            em.getTransaction().commit();
        }
        catch (PersistenceException pe){
            throw new ApiException(400,"Persistence error");
        }
        catch (Exception ex){
            throw new ApiException(400, "unexpected error");
        }
    }
}
