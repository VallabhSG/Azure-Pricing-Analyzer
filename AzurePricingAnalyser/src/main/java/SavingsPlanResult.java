import java.util.ArrayList;

public class SavingsPlanResult implements PricingOption{
    private int termYears;
    private double hourlyRate;
    private double upfrontCost;

    public SavingsPlanResult(int termYears, double hourlyRate) {
        this.termYears = termYears;
        this.hourlyRate = hourlyRate;
        this.upfrontCost = hourlyRate * Constants.YEARLY * termYears;
    }

    public int getTermYears() { return termYears; }
    @Override
    public String getPricingType() {
        return Constants.Savings_Plan + " " + termYears + " Years";
    }

    @Override
    public double getHourlyRate() {
        return hourlyRate;
    }

    @Override
    public double getUpfrontCost() {
        return upfrontCost;
    }

    @Override
    public PriceInfo getPriceInfo() {
        return new PriceInfo(upfrontCost, "1 Hour", hourlyRate, new ArrayList<>());
    }

    @Override
    public String toString() {
        return String.format("%d Years: $%.4f/hr, $%.4f/month, $%.4f/year (Upfront: $%.2f)",
            termYears, hourlyRate, hourlyRate * Constants.MONTHLY, hourlyRate * Constants.YEARLY, upfrontCost);
    }
}
