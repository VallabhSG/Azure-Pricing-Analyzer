import java.util.ArrayList;

public class ReservationPriceResult implements PricingOption {
    private String term;
    private PriceInfo priceInfo;

    public ReservationPriceResult(String term, double hourlyRate, double upfrontCost) {
        this.term = term;
        this.priceInfo = new PriceInfo(upfrontCost, "1 Hour", hourlyRate, new ArrayList<>());
        this.priceInfo.setUpfrontCost(upfrontCost);
    }

    public String getTerm() { return term; }
   	@Override
    public String getPricingType() {
        return Constants.Reservation + " " + term;
    }

    @Override
    public double getHourlyRate() {
        return priceInfo.getHourlyRate();
    }

    @Override
    public double getUpfrontCost() {
        return priceInfo.getUpfrontCost();
    }

	@Override
	public PriceInfo getPriceInfo() {
		return priceInfo;
	}
}
