import java.util.List;
/**
 * PriceInfo class
 * 
 * This class represents pricing information for a product or service.
 * It includes details such as original price, unit of measure, hourly rate,
 * monthly and yearly prices, upfront cost, and pricing tiers.
 * The class is designed to handle both consumption-based pricing (with no upfront cost)
 * and reservation-based pricing (where an upfront cost can be set).
 */
public class PriceInfo {
    private double originalPrice;
    private String unitOfMeasure;
    private double hourlyRate;
    private double monthlyPrice;
    private double yearlyPrice;
    private double upfrontCost;
    private List<TierInfo> tiers;

    public PriceInfo(double originalPrice, String unitOfMeasure, double hourlyRate, List<TierInfo> tiers) {
        this.originalPrice = originalPrice;
        this.unitOfMeasure = unitOfMeasure;
        this.hourlyRate = hourlyRate;
        this.monthlyPrice = hourlyRate * Constants.MONTHLY;
        this.yearlyPrice = hourlyRate * Constants.YEARLY;
        this.upfrontCost = 0; // Set to 0 for consumption pricing
        this.tiers = tiers;
    }

    // Add getters for all fields
    public double getOriginalPrice() { return originalPrice; }
    public String getUnitOfMeasure() { return unitOfMeasure; }
    public double getHourlyRate() { return hourlyRate; }
    public double getMonthlyPrice() { return monthlyPrice; }
    public double getYearlyPrice() { return yearlyPrice; }
    public double getUpfrontCost() { return upfrontCost; }
    public List<TierInfo> getTiers() { return tiers; }

    // Add a setter for upfrontCost (to be used for reservation pricing)
    public void setUpfrontCost(double upfrontCost) { this.upfrontCost = upfrontCost; }
    public boolean hasTiers() {
        return tiers != null && !tiers.isEmpty();
    }
}