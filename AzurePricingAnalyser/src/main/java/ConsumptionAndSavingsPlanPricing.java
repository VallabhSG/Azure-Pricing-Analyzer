import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

public class ConsumptionAndSavingsPlanPricing {
	/**
     * Fetches consumption prices and savings plan from the Azure Pricing API.
     * This function makes API calls to retrieve pricing data, handles pagination,
     * and processes the response to return a list of ConsumptionPriceResult objects.
     * 
     * @param filter The filter string to apply to the API request
     * @return A list of ConsumptionPriceResult objects containing the fetched prices
     * @throws Exception If there's an error during the API call or data processing
     */
    List<ConsumptionPriceResult> consumptionPrices = new ArrayList<>();
    List<SavingsPlanResult> savingsPlans = new ArrayList<>();

    
    public List<ConsumptionPriceResult> getConsumptionPrices() {
		return consumptionPrices;
	}
	public void setConsumptionPrices(List<ConsumptionPriceResult> prices) {
		this.consumptionPrices = prices;
	}
	public List<SavingsPlanResult> getSavingsPlans() {
		return savingsPlans;
	}
	public void setSavingsPlans(List<SavingsPlanResult> plans) {
		this.savingsPlans = plans;
	}
	public void fetchPrices(String filter, String vmProductName) throws Exception {
        String endpoint = Constants.AZURE_PRICING_API + Constants.API_VERSION + Constants.API_FILTER + URLEncoder.encode(filter + " and " + Constants.TYPE_CONSUMPTION_FILTER, "UTF-8");

        while (endpoint != null) {
            String response = ApiClient.getApiResponse(endpoint);
            JSONObject jsonObject = new JSONObject(response);
            JSONArray items = jsonObject.getJSONArray(Constants.ITEMS);

            List<JSONObject> itemList = new ArrayList<>();
            for (int i = 0; i < items.length(); i++) {
                itemList.add(items.getJSONObject(i));
                if (items.getJSONObject(i).has(Constants.SAVINGS_PLAN)) {
                    JSONArray savingsPlans = items.getJSONObject(i).getJSONArray(Constants.SAVINGS_PLAN);
                    for (int j = 0; j < savingsPlans.length(); j++) {
                       this.savingsPlans.add(parseSavingsPlan(savingsPlans.getJSONObject(j)));
                    }
                }
            }

            this.consumptionPrices.addAll(parseConsumptionPrices(itemList, vmProductName));

            endpoint = jsonObject.optString(Constants.NEXT_PAGE_LINK, null);
        }
    }
    /**
     * Parses the consumption prices from the API response.
     * This function processes the JSON objects returned by the API and converts them
     * into ConsumptionPriceResult objects. It handles different pricing structures
     * for various services, including special handling for Virtual Machines.
     * 
     * @param items A list of JSONObjects containing the pricing data
     * @return A list of ConsumptionPriceResult objects
     */

    private List<ConsumptionPriceResult> parseConsumptionPrices(List<JSONObject> items, String productName) {
        List<ConsumptionPriceResult> prices = new ArrayList<>();
        String serviceName = items.get(0).getString(Constants.SERVICE_NAME);
        
        if (serviceName.equals(Constants.VIRTUAL_MACHINES)) {
            Map<String, List<JSONObject>> vmPrices = new HashMap<>();
            for (JSONObject item : items) {
                String key = getVMPricingKey(item, productName);
                vmPrices.computeIfAbsent(key, k -> new ArrayList<>()).add(item);
            }
            for (Map.Entry<String, List<JSONObject>> entry : vmPrices.entrySet()) {
                PriceInfo priceInfo = parsePriceInfo(entry.getValue());
                prices.add(new ConsumptionPriceResult(entry.getKey(), priceInfo));
            }
        } else {
        	if (allItemsHaveZeroTierMinimumUnits(items)) {
                for (JSONObject item : items) {
                    String skuName = item.getString(Constants.SKU_NAME);
                    double retailPrice = item.getDouble(Constants.RETAIL_PRICE);
                    String unitOfMeasure = item.getString(Constants.UNIT_OF_MEASURE);
                    double hourlyRate = convertToHourlyRate(retailPrice, unitOfMeasure);
                    PriceInfo priceInfo = new PriceInfo(retailPrice, unitOfMeasure, hourlyRate, null); // Set tiers to null
                    prices.add(new ConsumptionPriceResult(skuName, priceInfo));
                }
            } else {
                String pricingType = items.get(0).getString(Constants.SKU_NAME);
                PriceInfo priceInfo = parsePriceInfo(items);
                prices.add(new ConsumptionPriceResult(pricingType, priceInfo));
            }
        }
        
        return prices;
    }
    
    /**
     * Checks if all items in the list have zero tier minimum units.
     * This helper function is used to determine if the pricing structure
     * is flat-rate or tiered.
     * 
     * @param items A list of JSONObjects containing the pricing data
     * @return true if all items have zero tier minimum units, false otherwise
     */
    
    private boolean allItemsHaveZeroTierMinimumUnits(List<JSONObject> items) {
        return items.stream().allMatch(item -> item.getDouble("tierMinimumUnits") == 0);
    }
    /**
     * Generates a key for Virtual Machine pricing.
     * This function creates a unique key for VM pricing based on the OS type
     * and pricing model (e.g., Spot, Low Priority, or Normal).
     * 
     * @param item A JSONObject containing the VM pricing data
     * @return A string key representing the VM pricing category
     */

    private static String getVMPricingKey(JSONObject item, String vmProductName) {
        String serviceName = item.getString(Constants.SERVICE_NAME);
        String productName = item.getString(Constants.PRODUCT_NAME);
        String skuName = item.getString(Constants.SKU_NAME);
        String osType = null;
        vmProductName = vmProductName.replaceAll(Constants.WINDOWS+"\\s*", "").replaceAll("\\s+", " ").trim(); //replaceInOriginalCode

        if (productName.contains(vmProductName)) {
            if ((productName.contains(Constants.VIRTUAL_MACHINES) || serviceName.equals(Constants.VIRTUAL_MACHINES)) && productName.contains(Constants.WINDOWS)) {
                osType = Constants.WINDOWS;
            } else if((productName.contains(Constants.VIRTUAL_MACHINES) || serviceName.equals(Constants.VIRTUAL_MACHINES)) || 
                       (((productName.contains(Constants.VIRTUAL_MACHINES)) || (serviceName.equals(Constants.VIRTUAL_MACHINES))) && productName.contains(Constants.LINUX))) {
                osType = Constants.LINUX;
            } else {
            	osType = productName;
            }
        } else {
            osType = productName;
        }

        if (skuName.contains(Constants.SPOT)) {
            return osType + " " + Constants.SPOT;
        } else if (skuName.contains(Constants.LOW_PRIORITY)) {
            return osType + " " + Constants.LOW_PRIORITY;
        } else {
            return osType + " " + Constants.NORMAL;
        }
    }

    /**
     * Parses the price information from a list of JSON objects.
     * This function processes the pricing tiers, calculates the hourly rate,
     * and creates a PriceInfo object with the parsed data.
     * 
     * @param items A list of JSONObjects containing the pricing data
     * @return A PriceInfo object with the parsed pricing information
     */
 

    private PriceInfo parsePriceInfo(List<JSONObject> items) {
        List<TierInfo> tiers = new ArrayList<>();
        String unitOfMeasure = items.get(0).getString(Constants.UNIT_OF_MEASURE);
        
        for (JSONObject item : items) {
            double tierMinimumUnits = item.getDouble("tierMinimumUnits");
            double retailPrice = item.getDouble(Constants.RETAIL_PRICE);
            tiers.add(new TierInfo(tierMinimumUnits, retailPrice, unitOfMeasure));
        }
        
        // Sort tiers by minimum units
        tiers.sort(Comparator.comparingDouble(TierInfo::getTierMinimumUnits));
        
        double originalPrice = tiers.get(0).getRetailPrice();
        double hourlyRate = convertToHourlyRate(originalPrice, unitOfMeasure);
        
        if (tiers.size() == 1) {
            return new PriceInfo(originalPrice, unitOfMeasure, hourlyRate, null);
        } else {
            return new PriceInfo(originalPrice, unitOfMeasure, hourlyRate, tiers);
        }
    }
    /**
     * Converts a given unit price to an hourly rate.
     * This function handles different units of measure (e.g., hour, day, month, year)
     * and normalizes the price to an hourly rate.
     * 
     * @param unitPrice The original price
     * @param unitOfMeasure The unit of measure for the original price
     * @return The converted hourly rate
     */

    private double convertToHourlyRate(double unitPrice, String unitOfMeasure) {
        switch (unitOfMeasure) {
            case "1 Hour":
                return unitPrice;
            case "1 Day":
                return unitPrice / 24.0;
            case "1 Month":
                return unitPrice / (30.4167 * 24);
            case "1 Year":
                return unitPrice / (365 * 24);
            default:
                return unitPrice;
        }
    }
  

    private SavingsPlanResult parseSavingsPlan(JSONObject planJson) {
        String term = planJson.getString(Constants.TERM);
        int years = extractYearsFromTerm(term);
        double hourlyRate = planJson.getDouble(Constants.RETAIL_PRICE);
        return new SavingsPlanResult(years, hourlyRate);
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
