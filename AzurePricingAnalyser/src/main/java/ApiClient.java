import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ApiClient {
    // Cache to store API responses, using ConcurrentHashMap for thread-safety
    private static final ConcurrentHashMap<String, String> cache = new ConcurrentHashMap<>();
    
    // Thread pool for executing asynchronous API calls
    private static final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    // Public method to get API response, using cache if available
    public static String getApiResponse(String endpoint) throws Exception {
        return cache.computeIfAbsent(endpoint, ApiClient::fetchApiResponse);
    }

    // Private method to fetch API response if not in cache
    private static String fetchApiResponse(String endpoint) {
        try {
            // Create URL object and open connection
            URL url = new URL(endpoint);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            // Check if the response code is successful (200)
            if (conn.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
            }

            // Read the response into a StringBuilder
            StringBuilder sb = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
            }

            // Return the response as a string
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error fetching API response", e);
        }
    }

    // Public method to get API response asynchronously
    public static Future<String> getApiResponseAsync(String endpoint) {
        return executor.submit(() -> getApiResponse(endpoint));
    }

    // Method to shutdown the executor service
    public static void shutdown() {
        executor.shutdown();
    }
}