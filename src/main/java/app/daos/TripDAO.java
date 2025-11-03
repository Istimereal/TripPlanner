package app.daos;

import app.dtos.GuideDTO;
import app.entities.Guide;
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

    public Trip getTripById(int id) {
        try(EntityManager em=emf.createEntityManager()){
            Trip found =em.find(Trip.class, id);
         if(found == null)
             throw new ApiException(404,"Trip not found");
         return found;
        }
        catch (Exception ex){
            throw new ApiException(500, "unexpected error");
        }
    }

    public List<Trip> getAllTrips(){
        try(EntityManager em=emf.createEntityManager()){

            TypedQuery<Trip> query = em.createQuery(("SELECT t FROM Trip t"),Trip.class);
          return query.getResultList();
        }
        catch (PersistenceException pe){
            throw new ApiException(500,"Persistence error");
        }
        catch (Exception ex){
            throw new ApiException(500, "unexpected error");
        }
    }

    public Trip createTrip(Trip trip) {
        EntityManager em = emf.createEntityManager();
        try{
            em.getTransaction().begin();
            em.persist(trip);
            em.getTransaction().commit();
        }
        catch (PersistenceException pe){
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();}
            throw new ApiException(500,"Persistence error");
        }
        catch (Exception ex){
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();}
            throw new ApiException(500, "unexpected error");
        }
        finally{
            em.close();
        }
        return trip;
    }

    public Trip updateTrip(Integer integer, Trip trip){
        EntityManager em=emf.createEntityManager();

        try{
            em.getTransaction().begin();
            Trip target = em.find(Trip.class, integer);
            if(target == null){
               throw new ApiException(404,"Trip with id "+integer+" not found");
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

            em.merge(target);
            em.getTransaction().commit();
            return target;
        }
        catch (PersistenceException pe){
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();}
            throw new ApiException(500,"Persistence error");
        }
        catch (Exception ex){
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();}
            throw new ApiException(500, "unexpected error");
        }
        finally {
            em.close();
        }
    }

    public void deleteTrip(Integer integer){
        EntityManager em=emf.createEntityManager();
        try{
            Trip target = em.find(Trip.class, integer);
            if(target==null){
               throw new ApiException(404,"Trip with id "+integer+" not found");
            }
            em.getTransaction().begin();
            em.remove(target);
            em.getTransaction().commit();
        }
        catch (PersistenceException pe){
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();}
            throw new ApiException(500,"Persistence error");
        }
        catch (Exception ex){
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();}
            throw new ApiException(500, "unexpected error");
        }
        finally {
            em.close();
        }
    }

public void addGuideToTrip(Integer tripId, Integer guideId ){
    EntityManager em=emf.createEntityManager();
      try{
          em.getTransaction().begin();
    Guide guide = em.find(Guide.class, guideId);
          if(guide == null){
              throw new ApiException(404,"Guide is not found: " + guideId);
          }
          Trip trip = em.find(Trip.class, tripId);
          if(trip == null){
              throw new ApiException(404,"Trip is not found:  " + tripId);
          }
        trip.setGuide(guide);
          em.merge(trip);
          em.getTransaction().commit();
      }
      catch (PersistenceException pe){
          if (em.getTransaction().isActive()){
              em.getTransaction().rollback();}
          throw new ApiException(500,"Persistence error adding Guide to trip:");
      }
      catch (Exception ex){
          if (em.getTransaction().isActive()){
              em.getTransaction().rollback();}
          throw new ApiException(500, "unexpected error adding guide to trip:");
      }
      finally {
          em.close();
      }
}

}
