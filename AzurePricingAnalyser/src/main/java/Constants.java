
public class Constants {
    public static final String AZURE_PRICING_API = "https://prices.azure.com/api/retail/prices?";
    public static final String API_VERSION = "api-version=2023-01-01-preview";
    public static final String API_FILTER = "&$filter=";
    public static final String SERVICE_FILTER = "serviceName eq '%s'";
    public static final String PRODUCT_NAME_FILTER = "productName eq '%s'";
    public static final String REGION_FILTER = "location eq '%s'";
    public static final String INSTANCE_TYPE_FILTER = "armSkuName eq '%s'";
    public static final String METERID_FILTER = "meterId eq '%s'";
    public static final String ARM_REGION_FILTER = "armRegionName eq '%s'";
    public static final String TYPE_CONSUMPTION_FILTER = "type eq 'Consumption'";
    public static final String TYPE_RESERVATION_FILTER = "type eq 'Reservation'";

    public static final String CURRENCY = "currencyCode";
    public static final String SERVICE_NAME = "serviceName";
    public static final String SKU_NAME = "skuName";
    public static final String ARM_SKU_NAME = "armSkuName";
    public static final String LOCATION = "location";
    public static final String PRODUCT_NAME = "productName";
    public static final String TYPE = "type";
    public static final String RETAIL_PRICE = "retailPrice";
    public static final String SAVINGS_PLAN = "savingsPlan";
    public static final String TERM = "term";
    public static final String ITEMS = "Items";
    
    public static final String Savings_Plan = "Savings Plan";
    public static final String Consumption = "Consumption";
    public static final String Reservation = "Reservation";

    public static final String WINDOWS = "Windows";
    public static final String LINUX = "Linux";
    public static final String SPOT = "Spot";
    public static final String LOW_PRIORITY = "Low Priority";
    public static final String NORMAL = "Normal";

    public static final String VIRTUAL_MACHINES = "Virtual Machines";
    public static final String RESERVATION_TERM = "reservationTerm";

    public static final String UNIT_OF_MEASURE = "unitOfMeasure";
    public static final String NEXT_PAGE_LINK = "NextPageLink";

    public static final double MONTHLY = 730.001; // Average number of hours in a month
    public static final double YEARLY = 8760; // Number of hours in a year
}