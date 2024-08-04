import java.net.URLEncoder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

public class AzurePricingCalculator {
	private String productName;
    private ConsumptionAndSavingsPlanPricing consumptionAndSavingsPlanPricing;
    private ReservationPricing reservationPricing;
    private PricingResult pricingResult;
    private String meterId;
    private String userRegion;
    /**
     * Constructor for AzurePricingCalculator
     * Initializes the calculator with a meter ID and user region
     * Sets up pricing components and result object
     */

    public AzurePricingCalculator(String meterId, String userRegion) {
        this.meterId = meterId;
        this.userRegion = userRegion;
        this.pricingResult = new PricingResult();
        this.consumptionAndSavingsPlanPricing = new ConsumptionAndSavingsPlanPricing();
        this.reservationPricing = new ReservationPricing();
    }
    /**
     * Calculates pricing for Azure services
     * Fetches prices for consumption, reservation, and savings plans
     * Calculates the best pricing options
     * @param b Boolean flag to determine if user input for region is required
     * @throws Exception if there's an error in calculation or API calls
     */

    public void calculatePricing(boolean b) throws Exception {
        String filter = buildFilter();
        if (b && filter == null) {
            System.out.println("Multiple regions found. Please enter a region:");
            Scanner scanner = new Scanner(System.in);
            userRegion = scanner.nextLine();
            filter = buildFilter();
        }
        
        consumptionAndSavingsPlanPricing.fetchPrices(filter,productName);
        List<ConsumptionPriceResult> consumptionPrices = consumptionAndSavingsPlanPricing.getConsumptionPrices();
        List<ReservationPriceResult> reservationPrices = reservationPricing.fetchPrices(filter);
        List<SavingsPlanResult> savingsPlans = consumptionAndSavingsPlanPricing.getSavingsPlans();

        pricingResult.setConsumptionPrices(consumptionPrices);
        pricingResult.setReservationPrices(reservationPrices);
        pricingResult.setSavingsPlans(savingsPlans);
        PricingCalculator calculator = new PricingCalculator();
        calculator.calculateBestOptions(pricingResult);

    }
    public class PricingCalculator {
    	/**
         * Calculates the best pricing options based on consumption, reservation, and savings plan prices
         * Determines the overall best option, best option without consumption, and term-wise best options
         * @param pricingResult The object containing all pricing information
         */
        public void calculateBestOptions(PricingResult pricingResult) {
            double lowestPrice = Double.MAX_VALUE;
            double lowestPriceWithoutConsumption = Double.MAX_VALUE;
            BestOptionResult bestOption = null;
            BestOptionResult bestOptionWithoutConsumption = null;
            Map<String, BestOptionResult> termWiseBestOptions = new HashMap<>();

            for (ConsumptionPriceResult price : pricingResult.getConsumptionPrices()) {
                double hourlyRate = price.getHourlyRate();
                if (hourlyRate < lowestPrice) {
                    lowestPrice = hourlyRate;
                    bestOption = new BestOptionResult(price.getPricingType(), hourlyRate,Constants.Consumption);
                }
            }

            for (ReservationPriceResult price : pricingResult.getReservationPrices()) {
                double hourlyRate = price.getHourlyRate();
                if (hourlyRate < lowestPrice) {
                    lowestPrice = hourlyRate;
                    bestOption = new BestOptionResult(price.getTerm() + " "+Constants.Reservation, hourlyRate,Constants.Reservation);
                }
                if (hourlyRate < lowestPriceWithoutConsumption) {
                    lowestPriceWithoutConsumption = hourlyRate;
                    bestOptionWithoutConsumption = new BestOptionResult(price.getTerm() + " "+Constants.Reservation, hourlyRate,Constants.Reservation);
                }
                String term = price.getTerm().split(" ")[0] + " Year";
                termWiseBestOptions.put(term, new BestOptionResult(term + " "+Constants.Reservation, hourlyRate,Constants.Reservation));
            }

            for (SavingsPlanResult plan : pricingResult.getSavingsPlans()) {
                double hourlyRate = plan.getHourlyRate();
                if (hourlyRate < lowestPrice) {
                    lowestPrice = hourlyRate;
                    bestOption = new BestOptionResult(plan.getTermYears() + " Years "+Constants.Savings_Plan, hourlyRate,Constants.Savings_Plan);
                }
                if (hourlyRate < lowestPriceWithoutConsumption) {
                    lowestPriceWithoutConsumption = hourlyRate;
                    bestOptionWithoutConsumption = new BestOptionResult(plan.getTermYears() + " Years "+Constants.Savings_Plan, hourlyRate, Constants.Savings_Plan);
                }
                String term = plan.getTermYears() + " Year";
                BestOptionResult currentBest = termWiseBestOptions.get(term);
                if (currentBest == null || hourlyRate < currentBest.getPrice()) {
                    termWiseBestOptions.put(term, new BestOptionResult(term + " "+Constants.Savings_Plan, hourlyRate,Constants.Savings_Plan));
                }
            }

            pricingResult.setBestOption(bestOption);
            pricingResult.setBestOptionWithoutConsumption(bestOptionWithoutConsumption);
            pricingResult.setTermWiseBestOptions(termWiseBestOptions);
        }
    }
    
    /**
     * Builds the filter string for API calls based on the meter ID and service type
     * Handles different filter construction for Virtual Machines and other services
     * @return A string containing the filter for API calls
     * @throws Exception if no data is found for the given meter ID or if there's an error in API calls
     */

    private String buildFilter() throws Exception {
        String initialFilter = String.format(Constants.METERID_FILTER, meterId);
        String initialEndpoint = Constants.AZURE_PRICING_API + Constants.API_VERSION + Constants.API_FILTER + URLEncoder.encode(initialFilter, "UTF-8");
        String initialResponse = ApiClient.getApiResponse(initialEndpoint);
        JSONArray items = new JSONObject(initialResponse).getJSONArray(Constants.ITEMS);

        if (items.length() == 0) {
            throw new Exception("No data found for the given meter ID.");
        }

        JSONObject initialData = items.getJSONObject(0);
        String serviceName = initialData.getString(Constants.SERVICE_NAME);
        pricingResult.setService(serviceName);
        String currency = initialData.getString(Constants.CURRENCY);
        pricingResult.setCurrency(currency);
        productName = initialData.getString(Constants.PRODUCT_NAME);

        String filter;
        if (serviceName.equals(Constants.VIRTUAL_MACHINES)) {
            filter = buildVirtualMachinesFilter(initialData);
        } else {
            filter = buildOtherServicesFilter(initialData, initialFilter);
        }

        // Check for multiple regions
        if (hasMultipleRegions(filter)) {
            System.out.println("Multiple regions found. Please enter a specific region:");
            Scanner scanner = new Scanner(System.in);
            String userInputRegion = scanner.nextLine().trim();
            
            // Apply appropriate filter based on user input
            if (userInputRegion.contains(" ")) {
                filter += " and " + String.format(Constants.REGION_FILTER, userInputRegion);
            } else {
                filter += " and " + String.format(Constants.ARM_REGION_FILTER, userInputRegion);
            }
            pricingResult.setRegion(userInputRegion);
            
            // Verify that the filter returns results
            if (!hasResults(filter)) {
                System.out.println("No results found for the specified region. Reverting to original filter.");
                filter = initialFilter;
            }
        }

        return filter;
    }
    
    /**
     * Checks if the given filter returns any results from the Azure Pricing API
     * @param filter The filter string to be used in the API call
     * @return true if results are found, false otherwise
     * @throws Exception if there's an error in the API call
     */
    private boolean hasResults(String filter) throws Exception {
        String endpoint = Constants.AZURE_PRICING_API + Constants.API_VERSION + Constants.API_FILTER + URLEncoder.encode(filter, "UTF-8");
        String response = ApiClient.getApiResponse(endpoint);
        JSONArray items = new JSONObject(response).getJSONArray(Constants.ITEMS);
        return items.length() > 0;
    }
    
    /**
     * Checks if the given filter returns results from multiple regions
     * @param filter The filter string to be used in the API call
     * @return true if multiple regions are found, false otherwise
     * @throws Exception if there's an error in the API call
     */

    private boolean hasMultipleRegions(String filter) throws Exception {
        String endpoint = Constants.AZURE_PRICING_API + Constants.API_VERSION + Constants.API_FILTER + URLEncoder.encode(filter, "UTF-8");
        String response = ApiClient.getApiResponse(endpoint);
        JSONArray items = new JSONObject(response).getJSONArray(Constants.ITEMS);

        Set<String> regions = new HashSet<>();
        for (int i = 0; i < items.length(); i++) {
            JSONObject item = items.getJSONObject(i);
            String location = item.optString(Constants.LOCATION, "");
            if (!location.isEmpty()) {
                regions.add(location);
            }
        }

        return regions.size() > 1;
    }
    /**
     * Builds a filter string specifically for Virtual Machines services
     * @param initialData JSONObject containing initial data about the service
     * @return A string containing the filter for Virtual Machines
     */

    private String buildVirtualMachinesFilter(JSONObject initialData) {
        String location = initialData.getString(Constants.LOCATION);
        String armSkuName = initialData.getString(Constants.ARM_SKU_NAME);
        pricingResult.setRegion(location);
        pricingResult.setInstanceType(armSkuName);

        return String.format("%s and %s and %s",
                String.format(Constants.SERVICE_FILTER, Constants.VIRTUAL_MACHINES),
                userRegion.isEmpty() ? String.format(Constants.REGION_FILTER, location) : String.format(Constants.ARM_REGION_FILTER, userRegion),
                String.format(Constants.INSTANCE_TYPE_FILTER, armSkuName));
    }
    
    /**
     * Builds a filter string for services other than Virtual Machines
     * @param initialData JSONObject containing initial data about the service
     * @param initialFilter The initial filter string based on meter ID
     * @return A string containing the filter for the specific service
     * @throws Exception if there's an error in API calls
     */

    private String buildOtherServicesFilter(JSONObject initialData, String initialFilter) throws Exception {
        String serviceName = initialData.getString(Constants.SERVICE_NAME);
        String armSkuName = initialData.optString(Constants.ARM_SKU_NAME, "");
        String location = initialData.optString(Constants.LOCATION, "");
        String productName = initialData.optString(Constants.PRODUCT_NAME, "");
        pricingResult.setRegion(location);
        pricingResult.setInstanceType(armSkuName);

        StringBuilder filterBuilder = new StringBuilder();
        filterBuilder.append(String.format(Constants.SERVICE_FILTER, serviceName));

        if (!armSkuName.isEmpty()) {
            filterBuilder.append(" and ").append(String.format(Constants.INSTANCE_TYPE_FILTER, armSkuName));
        } else if (!productName.isEmpty()) {
            filterBuilder.append(" and ").append(String.format(Constants.PRODUCT_NAME_FILTER, productName));
        }

        if (!location.isEmpty()) {
            filterBuilder.append(" and ").append(String.format(Constants.REGION_FILTER, location));
        }

        if (!userRegion.isEmpty()) {
            filterBuilder.append(" and ").append(String.format(Constants.ARM_REGION_FILTER, userRegion));
        }

        String detailedFilter = filterBuilder.toString();

        // Check if SavingsPlan or Reservation exists for this service
        if (hasSavingsPlanOrReservation(detailedFilter)) {
            return detailedFilter;
        } else {
            return initialFilter;
        }
    }
    /**
     * Checks if Savings Plan or Reservation pricing exists for a given service
     * @param filter The filter string to be used in the API call
     * @return true if Savings Plan or Reservation pricing exists, false otherwise
     * @throws Exception if there's an error in the API call
     */

    private boolean hasSavingsPlanOrReservation(String filter) throws Exception {
        String endpoint = Constants.AZURE_PRICING_API + Constants.API_VERSION + Constants.API_FILTER + URLEncoder.encode(filter, "UTF-8");
        String response = ApiClient.getApiResponse(endpoint);
        JSONArray items = new JSONObject(response).getJSONArray(Constants.ITEMS);

        for (int i = 0; i < items.length(); i++) {
            JSONObject item = items.getJSONObject(i);
            if (item.has(Constants.SAVINGS_PLAN) || Constants.Reservation.equals(item.optString(Constants.TYPE))) {
                return true;
            }
        }
        return false;
    }
    /**
     * Retrieves the calculated pricing result
     * @param i Boolean flag (unused in the current implementation)
     * @return PricingResult object containing all calculated pricing information
     */

    public PricingResult getPricingResult(Boolean i) {
        return this.pricingResult;
    }
    
    
}