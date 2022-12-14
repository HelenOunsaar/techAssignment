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
import java.nio.file.Paths;
import java.util.Scanner;

public class Main {
    private static final String BASE_PATH = "./jobs/";
    private static final URI MAIN_URI = URI.create("https://cv.ee/api/v1/vacancies-service/search?");

    public static void main(String[] args) throws JSONException, IOException, InterruptedException {

        makeDirectory();
        String keywords = keywordInput();
        int totalResults = getTotalResults(makeRequest(0, keywords));
        saveJobsToFile(makeRequest(totalResults, keywords));
    }

    private static void makeDirectory() {

        try{
            Path path = Paths.get(BASE_PATH);
            Files.createDirectories(path);
        } catch (IOException e) {
            System.err.println("Failed to create directory" + e.getMessage());
        }
    }

    private static String makeRequest (int limit, String keywords) throws IOException, InterruptedException {

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(MAIN_URI + String.format("categories[]=INFORMATION_TECHNOLOGY&limit=%s&offset=0&keywords[]=%s", limit, keywords)))
                .build();

        return client.send(request, HttpResponse.BodyHandlers.ofString()).body();
    }

    private static String keywordInput() {
        Scanner scannerKeyword = new Scanner(System.in); //System.in is a standard input stream
        System.out.print("Please enter a key: ");
        return scannerKeyword.nextLine();
    }

    public static int getTotalResults(String responseBody) {

        JSONObject responseObject = new JSONObject(responseBody);
        int totalResults = responseObject.getInt("total");
        System.out.println("Result: " + totalResults + " jobs found");
        return totalResults;
    }


    public static void saveJobsToFile(String responseBody) {
        JSONObject responseObject = new JSONObject(responseBody);

        //getting jobs applications array
        JSONArray array = responseObject.getJSONArray("vacancies");
        for (int i = 0; i < array.length(); i++) {

            String positionContentError = "";
            String jobLink = "https://cv.ee/et/vacancy/" + array.getJSONObject(i).getInt("id");

            String jobPosition = array.getJSONObject(i).getString("positionTitle");
            String positionContent = "";
            try {
                positionContent = array.getJSONObject(i).getString("positionContent");
            } catch (Exception ex) {
                positionContentError = "PositionContent not found!";
            }

            String expirationDate = array.getJSONObject(i).getString("expirationDate");
            String employerName = array.getJSONObject(i).getString("employerName");

            System.out.println("Jobs-" + (i+1) + ": " + jobLink + " " + positionContentError);
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
