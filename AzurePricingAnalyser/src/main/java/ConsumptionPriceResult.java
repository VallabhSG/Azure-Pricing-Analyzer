/**
 * ConsumptionPriceResult Class
 * 
 * This class represents the result of a consumption price calculation for a service or resource.
 * It encapsulates information about the pricing type and detailed price information.
 *
 * The class provides:
 * - Storage for pricing type and price information
 * - A constructor to initialize the object with necessary data
 * - Getter methods to access the pricing type, hourly rate, and full price information
 * - A custom toString method for easy string representation of the object
 */
public class ConsumptionPriceResult implements PricingOption {
    private String pricingType;
    private PriceInfo priceInfo;

    public ConsumptionPriceResult(String pricingType, PriceInfo priceInfo) {
        this.pricingType = pricingType;
        this.priceInfo = priceInfo;
    }

    @Override
    public String getPricingType() {
        return pricingType;
    }

    @Override
    public double getHourlyRate() {
        return priceInfo.getHourlyRate();
    }

    @Override
    public double getUpfrontCost() {
        return 0; // Consumption pricing typically has no upfront cost
    }

    @Override
    public PriceInfo getPriceInfo() {
        return priceInfo;
    }
    @Override
    public String toString() {
        return String.format("%s: $%.4f/hr", pricingType, getHourlyRate());
    }
}
