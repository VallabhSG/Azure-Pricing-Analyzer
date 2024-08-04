/**
 * PricingResult class represents pricing information for Azure services.
 * It includes details on consumption prices, reservation prices, and savings plans.
 * The class also stores the best pricing options overall and term-wise.
 * It provides a toString method for formatted output of all pricing information. VSG
 */
import java.util.List;
import java.util.Map;

public class PricingResult {
	private String currency;
	private String service;
    private String region;
    private String instanceType;
    private List<ConsumptionPriceResult> consumptionPrices;
    private List<ReservationPriceResult> reservationPrices;
    private List<SavingsPlanResult> savingsPlans;
    private BestOptionResult bestOption;
    private BestOptionResult bestOptionWithoutConsumption;
    private Map<String, BestOptionResult> termWiseBestOptions;

    // Getters and setters for all fields
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    
    public String getService() { return service; }
    public void setService(String service) { this.service = service; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public String getInstanceType() { return instanceType; }
    public void setInstanceType(String instanceType) { this.instanceType = instanceType; }

    public List<ConsumptionPriceResult> getConsumptionPrices() { return consumptionPrices; }
    public void setConsumptionPrices(List<ConsumptionPriceResult> consumptionPrices) { this.consumptionPrices = consumptionPrices; }

    public List<ReservationPriceResult> getReservationPrices() { return reservationPrices; }
    public void setReservationPrices(List<ReservationPriceResult> reservationPrices) { this.reservationPrices = reservationPrices; }

    public List<SavingsPlanResult> getSavingsPlans() { return savingsPlans; }
    public void setSavingsPlans(List<SavingsPlanResult> savingsPlans) { this.savingsPlans = savingsPlans; }

    public BestOptionResult getBestOption() { return bestOption; }
    public void setBestOption(BestOptionResult bestOption) { this.bestOption = bestOption; }

    public BestOptionResult getBestOptionWithoutConsumption() { return bestOptionWithoutConsumption; }
    public void setBestOptionWithoutConsumption(BestOptionResult bestOptionWithoutConsumption) { this.bestOptionWithoutConsumption = bestOptionWithoutConsumption; }

    public Map<String, BestOptionResult> getTermWiseBestOptions() { return termWiseBestOptions; }
    public void setTermWiseBestOptions(Map<String, BestOptionResult> termWiseBestOptions) { this.termWiseBestOptions = termWiseBestOptions; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Azure Pricing Result:\n");
        sb.append("Service: ").append(service).append("\n");
        sb.append("Region: ").append(region).append("\n");
        sb.append("Instance Type: ").append(instanceType).append("\n");
        sb.append("Currency: ").append(currency).append("\n\n");

        sb.append(outputConsumptionPrices());
        sb.append(outputReservationPrices());
        sb.append(outputSavingsPlans());

        sb.append("\nBest Option: ").append(bestOption != null ? bestOption : "null").append("\n");
        sb.append("Best Option Without Consumption: ").append(bestOptionWithoutConsumption != null ? bestOptionWithoutConsumption : "null").append("\n");
        
        sb.append("Term-wise Best Options:\n");
        if (termWiseBestOptions == null || termWiseBestOptions.isEmpty()) {
            sb.append("null\n");
        } else {
            for (Map.Entry<String, BestOptionResult> entry : termWiseBestOptions.entrySet()) {
                sb.append(String.format("\t%s: %s\n", entry.getKey(), entry.getValue()));
            }
        }

        return sb.toString();
    }
    
    private String outputConsumptionPrices() {
        StringBuilder sb = new StringBuilder();
        sb.append(Constants.Consumption + " Prices:\n");

        for (ConsumptionPriceResult price : consumptionPrices) {
            sb.append("\t").append(price.getPricingType()).append(": ");
            PriceInfo priceInfo = price.getPriceInfo();
            
            if (priceInfo.hasTiers()) {
                sb.append("Tiered pricing:\n");
                for (TierInfo tier : priceInfo.getTiers()) {
                    sb.append(String.format("\t\t%.0f+ %s - $%.4f per %s\n",
                        tier.getTierMinimumUnits(), 
                        priceInfo.getUnitOfMeasure(),
                        tier.getRetailPrice(),
                        priceInfo.getUnitOfMeasure()));
                }
            } else {
                if (priceInfo.getUnitOfMeasure().equals("1 Hour")||priceInfo.getUnitOfMeasure().equals("1 Month")||priceInfo.getUnitOfMeasure().equals("1 Year")) {
                    sb.append(String.format("$%.4f/hr, $%.4f/month, $%.4f/year\n",
                        priceInfo.getHourlyRate(), priceInfo.getMonthlyPrice(), priceInfo.getYearlyPrice()));
                } else {
                    sb.append(String.format("$%.4f per %s\n", 
                        priceInfo.getOriginalPrice(), priceInfo.getUnitOfMeasure()));
                }
            }
        }
        return sb.toString();
    }



    private String outputReservationPrices() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n"+Constants.Reservation+" Prices:\n");
        for (ReservationPriceResult price : reservationPrices) {
            sb.append("\t").append(price.getTerm()).append(": ");
            sb.append(String.format("$%.4f/hr, $%.4f/month, $%.4f/year", 
                price.getHourlyRate(), price.getHourlyRate() * Constants.MONTHLY, price.getHourlyRate() * Constants.YEARLY));
            sb.append(String.format(" (Upfront: $%.2f)", price.getUpfrontCost()));
            sb.append("\n");
        }
        return sb.toString();
    }

    private String outputSavingsPlans() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n"+Constants.Savings_Plan+":\n");
        for (SavingsPlanResult plan : savingsPlans) {
            sb.append("\t").append(plan.getTermYears()).append(" Year Savings Plan: ");
            sb.append(String.format("$%.4f/hr, $%.4f/month, $%.4f/year", 
                plan.getHourlyRate(), plan.getHourlyRate() * Constants.MONTHLY, plan.getHourlyRate() * Constants.YEARLY));
            sb.append(String.format(" (Upfront: $%.2f)", plan.getUpfrontCost()));
            sb.append("\n");
        }
        return sb.toString();
    }
}
