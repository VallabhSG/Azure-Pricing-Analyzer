/**
 * Represents the result of finding the best option, containing the option name and its price.
 */
public class BestOptionResult {
    private String optionName;
    private double price;
    private String pricingType;

    public BestOptionResult(String optionName, double price, String pricingType) {
        this.optionName = optionName;
        this.price = price;
        this.pricingType = pricingType;
    }

    public String getPricingType() {
		return pricingType;
	}

	public void setPricingType(String pricingType) {
		this.pricingType = pricingType;
	}

	public String getOptionName() {
        return optionName;
    }

    public void setOptionName(String optionName) {
        this.optionName = optionName;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return String.format("%s (Price: $%.4f/hr)", optionName, price);
    }
}