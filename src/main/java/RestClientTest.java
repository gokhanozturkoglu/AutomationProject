import com.jayway.jsonpath.JsonPath;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import org.json.XML;

import java.util.ArrayList;
import java.util.List;


public class RestClientTest {


    @BeforeTest()
    public void test() {
        RestAssured.baseURI = "http://ergast.com/api/f1";
    }

    @Test(priority = 1)
    public void test001() {
        given().get("http://ergast.com/api/f1/current").then().statusCode(equalTo(200));
    }

    @Test(priority = 2)
    public void test002() {
        String response = given().contentType(ContentType.JSON).log().all()
                .get("/current").getBody().asString();
        JSONObject xmlJSONObj = XML.toJSONObject(response);

        String jsonPrettyPrintString = xmlJSONObj.toString(4);
        ArrayList<String> locality = JsonPath.read(jsonPrettyPrintString, "$.MRData.RaceTable.Race[*].Circuit.Location.Locality");
        Assert.assertTrue(!(locality.contains("Turkey")), "Bu sene yarışlarda Türkiye grand prix'i de yer alıyor.");
    }

    @Test(priority = 3)
    public void test003() {
        //servise pagination ve limit dışında query param gönderilmediğinden string verdim.
        String pilot = "alonso";
        String seasonsResponse = given()
                .when().get("/drivers/" + pilot + "/driverStandings/1/seasons.json").getBody().asString();
        List<String> seasons = JsonPath.read(seasonsResponse, "$.MRData.SeasonTable.Seasons[*].season");
        String firstSeason = seasons.get(0);

        String pilotResponse = given()
                .when()
                .get("/drivers/" + pilot + "/driverStandings/1.json")
                .getBody()
                .asString();
        List<String> roundResponse = JsonPath.read(pilotResponse, "$.MRData.StandingsTable.StandingsLists[?(@.season == " + firstSeason + ")].round");
        int roundCount = Integer.valueOf(roundResponse.get(0));

        List<String> result = new ArrayList<String>();
        for (int i = 1; i <= roundCount; i++) {
            String response = given()
                    .when()
                    .get("http://ergast.com/api/f1/" + firstSeason + "/" + i + "/drivers/"+pilot+"/pitstops.json")
                    .getBody()
                    .asString();
            String totalPitStop = JsonPath.read(response, "$.MRData.total");
            result.add(totalPitStop);
        }
        Assert.assertTrue(result.contains("0"), "pit stop found");
    }
}
