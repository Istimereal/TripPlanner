package app.service;

import app.dtos.ItemForPackagingDTO;
import app.dtos.PackingListDTO;
import app.enums.Category;
import app.exceptions.ApiException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class PackingService {

    public static PackingListDTO getPackingList(Category category){
        PackingListDTO results =  null;
        String uri = "https://packingapi.cphbusinessapps.dk/packinglist/" + category;

        ObjectMapper mapper = new ObjectMapper();
        HttpClient client = HttpClient.newHttpClient();

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(uri))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                Object jSonGet = response.body();

         return mapper.readValue(jSonGet.toString(), PackingListDTO.class);

            } else if (response.statusCode() != 200) {
                throw new ApiException(response.statusCode(), "Unexpected response from Packing service");
            }
        }
        catch (InterruptedException ie) {
            throw new ApiException(504, "Not Found");
        }
        catch (JsonParseException jpe){

            throw new ApiException(404, "could not read/parse Packing response in correct format");
        }
        catch (JsonProcessingException jme){
            throw new ApiException(500, "could not map Packing response in correct format");
        }
       catch(URISyntaxException use){
            throw new ApiException(400, "URI error");
        }
        catch (IOException ioe)
            {
            throw new ApiException(502, "I/O Error");
            }
        catch (Exception e) {
            throw new ApiException(500, "Unexpected error from using packing service");
        }
        return results;
        }

        public static Integer calcPackingTotalWeight(PackingListDTO packingListDTO){
Integer result;
       return result = packingListDTO.getItems().stream()
               .mapToInt(ItemForPackagingDTO::getWeightInGrams)
               .sum();
        }

}
