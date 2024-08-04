/**
 * Represents pricing tier information for a product or service.
 * Contains the minimum units required for the tier, the retail price,
 * and the unit of measure.
 * This class is used to store and manage tier-based pricing details.
 */
public class TierInfo {
    private double tierMinimumUnits;
    private double retailPrice;
    private String unitOfMeasure;

    public TierInfo(double tierMinimumUnits, double retailPrice, String unitOfMeasure) {
        this.tierMinimumUnits = tierMinimumUnits;
        this.retailPrice = retailPrice;
        this.unitOfMeasure = unitOfMeasure;
    }

    public double getTierMinimumUnits() {
        return tierMinimumUnits;
    }

    public double getRetailPrice() {
        return retailPrice;
    }

    public String getUnitOfMeasure() {
        return unitOfMeasure;
    }
}
