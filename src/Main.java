import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.io.BufferedWriter;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

public class Main {
    private static final String BASE_PATH = "/Users/helen/IdeaProjects/TechAssignment/src/jobs/";
    private static final URI MAIN_URI = URI.create("https://cv.ee/api/v1/vacancies-service/search?limit=2000&offset=0&categories[]=INFORMATION_TECHNOLOGY&keywords[]=");

    public static void main(String[] args) throws JSONException {

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(MAIN_URI + keywordInput())).build();
        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(Main::parse)
                .join();
    }

    private static String keywordInput() {
        Scanner scannerKeyword = new Scanner(System.in); //System.in is a standard input stream
        System.out.print("Please enter a key: ");
        return scannerKeyword.nextLine();
    }

    public static void parse(String responseBody) {
        // using json jar library
        // getting total job applications
        JSONObject responseObject = new JSONObject(responseBody);
        int totalResults = responseObject.getInt("total");
        System.out.println("Result: " + totalResults + " jobs found");

        //getting jobs applications array
        JSONArray array = responseObject.getJSONArray("vacancies");
        for (int i = 0; i < array.length(); i++) {

            String jobLink = "https://cv.ee/et/vacancy/" + array.getJSONObject(i).getInt("id");
            System.out.println(jobLink);

            String jobPosition = array.getJSONObject(i).getString("positionTitle");
            String positionContent = "";
            try {
                positionContent = array.getJSONObject(i).getString("positionContent");
            } catch (Exception ex) {
                System.out.println("PositionContent not found!");
            }

            String expirationDate = array.getJSONObject(i).getString("expirationDate");
            String employerName = array.getJSONObject(i).getString("employerName");

            writingFiles("Job-" + (i+1), jobPosition, positionContent, expirationDate, employerName);
        }
        System.out.println("More info is saved to the files.");
    }

    public static void writingFiles(String jobId, String text, String positionContent, String expirationDate, String employerName) {
            Path path = Path.of(BASE_PATH + jobId + ".txt");
            try(BufferedWriter writer = Files.newBufferedWriter(path)) {
                writer.write(text);
                writer.newLine();
                writer.write(positionContent);
                writer.newLine();
                writer.write(expirationDate);
                writer.newLine();
                writer.write(employerName);

            } catch (IOException ioe) {
                ioe.printStackTrace();
           }
    }
}
