package genesis.crypto.wallet.performance;

import org.apache.http.client.utils.URIBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

// Represents an asset in the crypto wallet
class Asset {
    private String symbol;
    private double quantity;
    private double price;
    private double performance;

    public Asset(String symbol, double quantity, double price) {
        this.symbol = symbol;
        this.quantity = quantity;
        this.price = price;
    }

    public String getSymbol() {
        return symbol;
    }

    public double getQuantity() {
        return quantity;
    }

    public double getPrice() {
        return price;
    }

    public double getPerformance() {
        return performance;
    }

    public void setPerformance(double performance) {
        this.performance = performance;
    }
}

public class CryptoWalletPerformance {

    private static final String COINCAP_API_BASE_URL = "https://api.coincap.io/v2";
    private static final int MAX_THREADS = 3;
    private static final String INTERVAL = "d1";
    private static final long START_TIME = 1617753600000L;
    private static final long END_TIME = 1617753601000L;

    public static void main(String[] args) {

        // Step 1: Read assets from the wallet CSV file
        List<Asset> assets = readAssetsFromWalletCSV();

        // Step 2: Calculate the total value of the assets
        double totalValue = calculateTotalValueOfTheAssets(assets);

        // Step 3: Find the best performing asset
        Asset bestAsset = findBestPerformingAsset(assets);

        // Step 4: Find the worst performing asset
        Asset worstAsset = findWorstPerformingAsset(assets);

        // Generate the output string
        String output = String.format("total=%.2f,best_asset=%s,best_performance=%.2f,worst_asset=%s,worst_performance=%.2f",
                totalValue, bestAsset.getSymbol(), bestAsset.getPerformance(), worstAsset.getSymbol(), worstAsset.getPerformance());

        System.out.println(output);

    }

    private static List<Asset> readAssetsFromWalletCSV() {
        List<Asset> assets = new ArrayList<>();

        // Step 1: Read the wallet CSV file
        URL file = CryptoWalletPerformance.class.getResource("/crypto-wallet.csv");
        try {
            InputStreamReader inputStreamReader = new InputStreamReader(file.openStream());
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            String currentLine;
            boolean skipFirstLine = true;
            while ((currentLine = bufferedReader.readLine()) != null) {
                if (skipFirstLine) {
                    skipFirstLine = false;
                    continue;
                }
                String[] values = currentLine.split(",");
                if (values.length == 3) {
                    // Create Asset objects from the CSV data
                    String symbol = values[0].trim();
                    double quantity = Double.parseDouble(values[1].trim());
                    double price = Double.parseDouble(values[2].trim());
                    assets.add(new Asset(symbol, quantity, price));
                }
            }
            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return assets;
    }

    private static double calculateTotalValueOfTheAssets(List<Asset> assets) {
        ExecutorService executor = Executors.newFixedThreadPool(MAX_THREADS);
        List<Callable<Double>> tasks = new ArrayList<>();

        long currentTime = System.currentTimeMillis();
        System.out.println("Now is " + new Date(currentTime));

        // Step 2: Calculate the value of each asset
        for (Asset asset : assets) {
            tasks.add(() -> {
                // Step 2.1: Get the asset ID from the CoinCap API
                String assetId = getAssetId(asset.getSymbol());

                // Step 2.2: Get the latest price for the asset
                double latestPrice = getLatestPrice(assetId);

                // Step 2.3: Calculate the asset's performance and update the Asset object
                double performance = (latestPrice) / asset.getPrice();
                asset.setPerformance(performance);

                // Step 2.4: Calculate the value of the asset based on the quantity and latest price
                return asset.getQuantity() * latestPrice;
            });
            long requestTime = System.currentTimeMillis() + 1000;
            System.out.println("Submitted request " + asset.getSymbol() + " at " + new Date(requestTime));
            try {
                Thread.sleep(requestTime - currentTime);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        List<Future<Double>> results = null;
        try {
            // Execute the tasks and get the results
            results = executor.invokeAll(tasks);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        executor.shutdown();

        double totalValue = 0.0;
        for (Future<Double> result : results) {
            try {
                // Step 2.5: Accumulate the total value of all assets
                totalValue += result.get();
            } catch (Exception e) {
                System.out.println("Failed to fetch latest price for an asset.");
                e.printStackTrace();
            }
        }
        return totalValue;
    }

    private static String getAssetId(String symbol) {
        try {
            // Step 2.1: Build the URL for the CoinCap API to get the asset ID
            URIBuilder uriBuilder = new URIBuilder(COINCAP_API_BASE_URL + "/assets");
            uriBuilder.addParameter("search", symbol);
            URL url = uriBuilder.build().toURL();

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                StringBuilder response = new StringBuilder();

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                reader.close();

                // Parse the JSON response and extract the asset ID based on the symbol
                JsonParser jsonParser = new JsonParser();
                JsonObject jsonObject = jsonParser.parse(response.toString()).getAsJsonObject();
                JsonArray data = jsonObject.getAsJsonArray("data");

                for (JsonElement element : data) {
                    JsonObject assetObject = element.getAsJsonObject();
                    String assetSymbol = assetObject.get("symbol").getAsString();

                    if (assetSymbol.equalsIgnoreCase(symbol)) {
                        return assetObject.get("id").getAsString();
                    }
                }
            }
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }

    static double getLatestPrice(String assetId) {
        try {
            // Step 2.2: Build the URL for the CoinCap API to get the latest price
            URIBuilder uriBuilder = new URIBuilder(COINCAP_API_BASE_URL + "/assets/" + assetId + "/history");
            uriBuilder.addParameter("interval", INTERVAL);
            uriBuilder.addParameter("start", String.valueOf(START_TIME));
            uriBuilder.addParameter("end", String.valueOf(END_TIME));
            URL url = uriBuilder.build().toURL();

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                StringBuilder response = new StringBuilder();

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                reader.close();

                // Parse the JSON response and extract the latest price
                JsonParser jsonParser = new JsonParser();
                JsonObject jsonObject = jsonParser.parse(response.toString()).getAsJsonObject();
                JsonArray data = jsonObject.getAsJsonArray("data");

                if (data.size() > 0) {
                    JsonObject latestData = data.get(data.size() - 1).getAsJsonObject();
                    return latestData.get("priceUsd").getAsDouble();
                }
            }

        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    private static Asset findBestPerformingAsset(List<Asset> assets) {
        Asset bestAsset = null;
        double bestPerformance = Double.MIN_VALUE;

        // Step 3: Find the asset with the highest performance
        for (Asset asset : assets) {
            double performance = asset.getPerformance();
            if (performance > bestPerformance) {
                bestPerformance = performance;
                bestAsset = asset;
            }
        }

        return bestAsset;
    }

    private static Asset findWorstPerformingAsset(List<Asset> assets) {
        Asset worstAsset = null;
        double worstPerformance = Double.MAX_VALUE;

        // Step 4: Find the asset with the lowest performance
        for (Asset asset : assets) {
            double performance = asset.getPerformance();
            if (performance < worstPerformance) {
                worstPerformance = performance;
                worstAsset = asset;
            }
        }

        return worstAsset;
    }
}
