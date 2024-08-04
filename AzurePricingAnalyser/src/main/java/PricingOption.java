public interface PricingOption {
    String getPricingType();
    double getHourlyRate();
    double getUpfrontCost();
    PriceInfo getPriceInfo();
}