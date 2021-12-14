package kozakowski.monitorowaniejednostek.model.kozakowski;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class TrackService {
    RestTemplate restTemplate = new RestTemplate();


    public List<Point> getTracks() {

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Authorization", "Bearer eyJhbGciOiJSUzI1NiIsImtpZCI6IjBCM0I1NEUyRkQ5OUZCQkY5NzVERDMxNDBDREQ4OEI1QzA5RkFDRjMiLCJ0eXAiOiJhdCtqd3QiLCJ4NXQiOiJDenRVNHYyWi03LVhYZE1VRE4ySXRjQ2ZyUE0ifQ.eyJuYmYiOjE2Mzk1MjIzODIsImV4cCI6MTYzOTUyNTk4MiwiaXNzIjoiaHR0cHM6Ly9pZC5iYXJlbnRzd2F0Y2gubm8iLCJhdWQiOiJhcGkiLCJjbGllbnRfaWQiOiJhbmFuaWFzejEyMjhAZ21haWwuY29tOnMxOTA2OCIsInNjb3BlIjpbImFwaSJdfQ.Gtu1JEu2TExPGJaLHi63OepU0zTrIOEmzbfvd_8M5aQ_kGaLGVvna-XSmaaHEYx_uUoK8dUFvnd5jC7xYyYsRUBvB6vISIWa9z-iPhg1gGFQcls6woNmR9t10XikjKilroRweSshKOqGBFXA0yoW12zNXLg0Nf4t6FBPtS9GEyf3NAsWUdn9nhZk-tfmW2AqDYdCT_dI2PtDLxC8PvFAcxKrs_Q94bqX6JJTOfAiSRER9iYqlNquoILUes9Sly61u2scEHd38eIl8fwvH5fd60uUoZnq6xyrKuTxL_IZ_Ef8CZ9lVlN3dlkvh8ZqWREPmo6DowZZXelEghI4NCsgZg40CoLbLaPKGDycPKyuW4rndgMy5M6a2dYh6gUyIlhVYVxBru9I4Iz6BndRvt5Q_XI86FWwHmRCWuVj-z6lPor44J6WzZL8_WwabsngFuWmcL7hyxr-vhhGBhxVW7OiKADySHbYD_30W19lWhlHoqZ2E0f49RxdGdtET0ehW3sOfmx7Rg0R8Y_xSoAW8En940n1gsvYx9Nc0W-xFcfJPDJDQhtqgsqGprMiY4vVnW9uO792cGT5_2iTHTSrq_ptWChCOPQUylytpQflsQMmhR9ECXIEmvN25BSmiC_zy5NyOFVTEpcnuAaerG-HZhPtIXl2FOXFgpTVuoXZceA4azw");
        HttpEntity httpEntity = new HttpEntity(httpHeaders);

        ResponseEntity<Track[]> exchange = restTemplate.exchange("https://www.barentswatch.no/bwapi/v2/geodata/ais/openpositions?Xmin=10.09094&Xmax=10.67047&Ymin=63.3989&Ymax=63.58645",
                HttpMethod.GET,
                httpEntity,
                Track[].class);

        List<Point> collect = Stream.of(exchange.getBody()).map(
                track -> new Point(
                        track.getGeometry().getCoordinates().get(0),
                        track.getGeometry().getCoordinates().get(1),
                        track.getName(),
                        getDestination(track.getDestination(), track.getGeometry().getCoordinates()).getLongitude(),
                        getDestination(track.getDestination(), track.getGeometry().getCoordinates()).getLatitude()
                )
        ).collect(Collectors.toList());
        return collect;
    }

    public Datum getDestination(String destinationName, List<Double> coordinates) {
        try {
            String url = "http://api.positionstack.com/v1/forward?access_key=f9aae45e031a1e66eac64db90ffda427&query=" + destinationName;
            JsonNode data = restTemplate.getForObject(url, JsonNode.class).get("data").get(0);
            double latitude = data.get("latitude").asDouble();
            double longitude = data.get("longitude").asDouble();
            return new Datum(latitude, longitude);

        } catch (Exception ex) {
            return new Datum(coordinates.get(1), coordinates.get(0));
        }
    }
}
