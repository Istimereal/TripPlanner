package app.service;

import app.dtos.ItemForPackagingDTO;
import app.dtos.PackingListDTO;
import app.enums.Category;
import app.exceptions.ApiException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.testcontainers.shaded.org.bouncycastle.crypto.params.CramerShoupPublicKeyParameters;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class PackingService {

       static ApiService apiService = new ApiService();

        public static PackingListDTO getTripPackingList (String category){
            ConvertJsonToPackingListDTO convertJsonToPackingListDTO = new ConvertJsonToPackingListDTO();
         //   PackingListDTO result = new PackingListDTO();
          //  PackingListDTO results = null;
            String url = "https://packingapi.cphbusinessapps.dk/packinglist/" + category;

                String response = apiService.fetchFromApi(url);

            System.out.println("FETCH RESULT:\n" + response);

            PackingListDTO results = convertJsonToPackingListDTO.packingApiToPackingListDTO(response);

            return results;
        }

        public static Integer calcPackingTotalWeight(PackingListDTO packingListDTO){
       Integer result;

       return result = packingListDTO.getItems().stream()
               .mapToInt(ItemForPackagingDTO::getWeightInGrams)
               .sum();
        }

}
