# Crypto Wallet Performance
This Java program calculates the performance of assets in a crypto wallet and provides information about the total value, best performing asset, and worst performing asset. It utilizes the CoinCap API to fetch asset data and prices.

## Prerequisites
Before running the program, make sure you have the following dependencies installed:

- Java Development Kit (JDK) version 11 or higher
- Apache HttpClient library
- Google Gson library
- Maven

## Getting Started
1. Clone the repository or download the source code files:
```bash 
git clone <repository_url>
```
2. Navigate to the project directory: 
```bash 
cd crypto-wallet-performance
```
3. Build the project using Maven:
```bash
mvn clean install
```
4. Run the program:
```bash
mvn exec:java -Dexec.mainClass="genesis.crypto.wallet.performance.CryptoWalletPerformance"
```

## Program Workflow
- Read assets from the crypto-wallet.csv file.
- Calculate the total value of the assets by fetching the latest prices from the CoinCap API.
- Find the best performing asset based on the calculated performance.
- Find the worst performing asset based on the calculated performance.
- Print the output containing the total value, best performing asset, best performance, worst performing asset, and worst performance.

The output is printed in the following format:
```bash
total=<total_value>,best_asset=<best_asset_symbol>,best_performance=<best_performance>,worst_asset=<worst_asset_symbol>,worst_performance=<worst_performance>
```

## Asset Class
The Asset class represents an asset in the crypto wallet and provides methods to retrieve and update its properties.

### Properties
- symbol: The symbol of the asset (e.g., BTC, ETH).
- quantity: The quantity of the asset in the wallet.
- price: The price of the asset.
- performance: The performance of the asset.

### Methods
- getSymbol(): Returns the symbol of the asset.
- getQuantity(): Returns the quantity of the asset.
- getPrice(): Returns the price of the asset.
- getPerformance(): Returns the performance of the asset.
- setPerformance(double performance): Sets the performance of the asset.

## Main Class
The CryptoWalletPerformance class contains the main logic to calculate the performance of the crypto wallet.

### Constants
- COINCAP_API_BASE_URL: The base URL of the CoinCap API.
- MAX_THREADS: The maximum number of threads for concurrent API requests.
- INTERVAL: The time interval for price data retrieval from the CoinCap API.
- START_TIME and END_TIME: The start and end times for price data retrieval (in milliseconds).

### Methods
- main(String[] args): The entry point of the application. It reads the assets from the wallet CSV file, calculates the total value of the assets, finds the best and worst performing assets, and outputs the results.
- readAssetsFromWalletCSV(): Reads the assets from the wallet CSV file and returns a list of Asset objects.
- calculateTotalValueOfTheAssets(List<Asset> assets): Calculates the total value of the assets by retrieving the latest price data for each asset.
- getAssetId(String symbol): Retrieves the asset ID for a given symbol from the CoinCap API.
- getLatestPrice(String assetId): Retrieves the latest price for an asset from the CoinCap API.
- findBestPerformingAsset(List<Asset> assets): Finds the asset with the highest performance.
- findWorstPerformingAsset(List<Asset> assets): Finds the asset with the lowest performance.

## Tests
The package includes JUnit tests to ensure the correctness of the program's functionality. You can run the tests using the following command:
```bash
mvn test
```

The tests validate the main method's output against the expected result. The CryptoWalletPerformanceTest class contains the following test case:
```java
@Test
public void main_ShouldPrintCorrectOutput() {
    // Redirect standard output to a ByteArrayOutputStream
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outputStream));

    // Call the main method
    CryptoWalletPerformance.main(null);

    // Capture the output
    String output = outputStream.toString().trim();

    // Verify if the output matches the expected result
    String expectedOutput = "total=16984.62,best_asset=BTC,best_performance=1.51,worst_asset=ETH,worst_performance=1.01";
    assertTrue(output.contains(expectedOutput));
}
```
The test verifies that the program's output matches the expected result.

## Future Improvement: Implementing as a Spring Boot REST API

For further enhancements and scalability, this solution can be refactored to utilize the Spring Boot framework and design it as a REST API. By doing so, the crypto wallet performance functionality can be exposed as a service that can be accessed by other applications or clients.
Here a some to implement this solution using Spring Boot:

- Add the necessary Spring Boot dependencies to the project's pom.xml file.
- Create a new Spring Boot application class, for example, CryptoWalletPerformanceApplication, and annotate it with @SpringBootApplication.
- Define a controller class, such as CryptoWalletPerformanceController, and annotate it with @RestController.
- Inside the controller, define an endpoint, e.g., /performance, and annotate it with @GetMapping.
- Move the logic from the main method in the CryptoWalletPerformance class to a new method within the controller, which will handle the calculation and return the result.
- Replace the System.out.println statements with appropriate response structures, such as DTOs or JSON objects, to return the calculated performance data.
- Modify the program's entry point to start the Spring Boot application, either by running the main method in the CryptoWalletPerformanceApplication class or by using the spring-boot:run Maven command.

### Benefits of using Spring Boot
By adopting the Spring Boot framework and designing the solution as a REST API, there are several advantages:
- `Modularity and maintainability:` Spring Boot provides a modular and organized approach to application development, making it easier to manage and maintain the codebase.
- `Dependency Injection and IoC:` Spring Boot's dependency injection mechanism simplifies component wiring and promotes loose coupling between different layers of the application.
- `Testing and debugging:` Spring Boot offers robust testing capabilities, making it easier to write unit tests and perform integration testing on the REST API endpoints.
- `Integration with external systems:` With Spring Boot's extensive ecosystem and support for various libraries, it's easy to integrate the solution with external databases, caching systems, or other microservices.
- `Scalability:` Spring Boot's built-in support for containerization and deployment automation allows you to scale the application horizontally as needed.
- `Security:` Spring Security can be easily integrated into the REST API to provide authentication and authorization features to protect the crypto wallet performance data.
