/**
  /**
     * Main method to run the Azure Pricing Calculator
     * Prompts user for meter ID and optionally region
     * Calculates and displays pricing information VSG
     */   
import java.util.Scanner;

public class MainClass {

	 public static void main(String[] args) {
	        Scanner scanner = new Scanner(System.in);

	        System.out.println("Enter meter ID: ");
	        String meterId = scanner.nextLine();

	        /*System.out.println("Enter region (optional, press Enter to skip): ");
	        String userRegion = scanner.nextLine();*/
	        String userRegion = "";

	        try {
	            AzurePricingCalculator calculator = new AzurePricingCalculator(meterId, userRegion);
	            calculator.calculatePricing(false);
	            PricingResult result = calculator.getPricingResult(false);
	            System.out.println(result);
	        } catch (Exception e) {
	            e.printStackTrace();
	        } finally {
	            scanner.close();
	        }
	    }
}
