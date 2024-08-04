/**
 * This class handles fetching and parsing reservation pricing information from Azure.
 * It includes methods to:
 * - Fetch prices using a filter and paginate through results
 * - Parse individual reservation price items
 * - Extract the number of years from a reservation term
 * The class uses the Azure Pricing API and returns a list of ReservationPriceResult objects.
 */
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class ReservationPricing {
    public List<ReservationPriceResult> fetchPrices(String filter) throws Exception {
        List<ReservationPriceResult> prices = new ArrayList<>();
        String endpoint = Constants.AZURE_PRICING_API + Constants.API_FILTER + URLEncoder.encode(filter + " and " + Constants.TYPE_RESERVATION_FILTER, "UTF-8");

        while (endpoint != null) {
            String response = ApiClient.getApiResponse(endpoint);
            JSONObject jsonObject = new JSONObject(response);
            JSONArray items = jsonObject.getJSONArray(Constants.ITEMS);

            for (int i = 0; i < items.length(); i++) {
                JSONObject item = items.getJSONObject(i);
                prices.add(parseReservationPrice(item));
            }

            endpoint = jsonObject.optString(Constants.NEXT_PAGE_LINK, null);
        }

        return prices;
    }

    private ReservationPriceResult parseReservationPrice(JSONObject item) {
        String term = item.getString(Constants.RESERVATION_TERM);
        double unitPrice = item.getDouble(Constants.RETAIL_PRICE);
        int years = extractYearsFromTerm(term);
        double hours = years * Constants.YEARLY;
        double hourlyRate = unitPrice/ hours;
        double upfrontCost = unitPrice;

        return new ReservationPriceResult(term, hourlyRate, upfrontCost);
    }

    private int extractYearsFromTerm(String term) {
        String[] parts = term.split(" ");
        if (parts.length > 0) {
            try {
                return Integer.parseInt(parts[0]);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return 1; // Default to 1 year if parsing fails
    }
}