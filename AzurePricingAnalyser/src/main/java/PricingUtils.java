/**
 * Utility class for pricing-related calculations.
 * Provides methods to:
 * - Extract years from a term string
 * - Calculate hourly rates based on different units of measure
 * Handles exceptions and provides default values when necessary.
 */
import java.util.logging.Level;
import java.util.logging.Logger;

public class PricingUtils {
    public static int extractYearsFromTerm(String term) {
        String[] parts = term.split(" ");
        if (parts.length > 0) {
            try {
                return Integer.parseInt(parts[0]);
            } catch (NumberFormatException e) {
                Logger.getLogger(PricingUtils.class.getName()).log(Level.WARNING, "Failed to parse term: " + term, e);
            }
        }
        return 1; // Default to 1 year if parsing fails
    }

    public static double calculateHourlyRate(double price, String unitOfMeasure) {
        switch (unitOfMeasure) {
            case "1 Hour":
                return price;
            case "1 Day":
                return price / 24.0;
            case "1 Month":
                return price / Constants.MONTHLY;
            case "1 Year":
                return price / Constants.YEARLY;
            default:
                return price;
        }
    }
}