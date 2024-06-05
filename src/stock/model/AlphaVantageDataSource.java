package stock.model;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.net.HttpURLConnection;


public class AlphaVantageDataSource extends CSVDataSource {
  private static final String API_KEY = "W0M1JOKC82EZEQA8";
  private static final String BASE_URL = "https://www.alphavantage.co/query?";

  //  3FKL0E8WUDB1EOMS

  private Set<String> tickerList = new HashSet<>();
  String folderName = "res/APIData";

  private boolean checkInitialization = false;

  // throw IOException
  public AlphaVantageDataSource() {
    super();
  }

  private void init() throws IOException {
    File folder = new File(folderName);
    deleteFolder(folder);
    folder.mkdirs();
    generateTickerList(new File("res/stocksData"));
  }

  private void generateTickerList(File directory) throws IOException {
    File[] files = directory.listFiles((dir, name) -> name.endsWith(".csv"));  // Filter for CSV files
    if (files != null) {
      for (File file : files) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
          String line = reader.readLine();
          while ((line = reader.readLine()) != null) {
            String[] parts = line.split(",");
            if (parts.length > 0) {
              String symbol = parts[0].trim();
              tickerList.add(symbol);
            }
          }
        }
      }
    } else {
      throw new IOException("Error: List of tickers was unsuccessfully loaded at " +
              directory.getPath() + " and the CSV file is formatted properly.");
    }
  }

  public Set<String> getTickerList() {
    return tickerList;
  }

  private void generateStockCSV(File folder, String ticker) throws IOException {
//    String function = "TIME_SERIES_DAILY_ADJUSTED";
//    String urlString = BASE_URL + "function=" + function + "&symbol=" + ticker + "&apikey=" + API_KEY;
//    try {
//      URL url = new URL(urlString);
//      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//      connection.setRequestMethod("GET");
//      connection.connect();
//
//      int responseCode = connection.getResponseCode();
//      if (responseCode == 200) {
//        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
//        String line;
//        StringBuilder response = new StringBuilder();
//        while ((line = reader.readLine()) != null) {
//          response.append(line);
//        }
//        reader.close();
//
//      } else {
//        System.out.println("Failed to get response from server.");
//      }
//    } catch (Exception e) {
//      e.printStackTrace();
//    }

    URL url;
    try {
      // Constructing the URL
      url = new URL("https://www.alphavantage.co/query?function=TIME_SERIES_DAILY_ADJUSTED"
              + "&outputsize=full"
              + "&symbol=" + ticker
              + "&apikey=" + API_KEY
              + "&datatype=csv");

      // Opening a stream from the URL
      try (InputStream in = url.openStream();
        BufferedInputStream bis = new BufferedInputStream(in);
        FileOutputStream fos = new FileOutputStream(new File(folder, ticker + ".csv"))) {

        byte[] dataBuffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = bis.read(dataBuffer, 0, 1024)) != -1) {
          fos.write(dataBuffer, 0, bytesRead);
        }
      }
    } catch (MalformedURLException e) {
      throw new IOException("The URL is malformed, please check the API endpoint and parameters: " + e.getMessage(), e);
    } catch (IOException e) {
      throw new IOException("Failed to download or write data for " + ticker + ": " + e.getMessage(), e);
    } catch (Exception e) {
      throw new IOException("An unexpected error occurred while accessing the API for " + ticker + ": " + e.getMessage(), e);
    }
  }

  @Override
  public boolean stockExistsAtDate(LocalDate date, String ticker) throws IOException {
    if (!checkInitialization) {
      init();
      checkInitialization = true;
    }

    // Invalid ticker
    if (!stockInDataSource(ticker)) {
      throw new IllegalArgumentException("Invalid ticker: Stock is not in data source.");
    }

    // Stock has not been read yet
    if (!stocks.containsKey(ticker)) {
      generateStockCSV(new File(folderName), ticker);
      loadAllStockData("res/APIData");
    }

    return stocks.get(ticker).containsKey(date);
  }

  @Override
  public double getClosingPrice(LocalDate date, String ticker) throws IOException {
    if (!checkInitialization) {
      init();
      checkInitialization = true;
    }
    if (!stockExistsAtDate(date, ticker)) {
      return 0;
    } else {
      return stocks.get(ticker).get(date);
    }
  }

  @Override
  public boolean stockInDataSource(String ticker) throws IOException {
    if (!checkInitialization) {
      init();
      checkInitialization = true;
    }
    return tickerList.contains(ticker);
  }

  private static void deleteFolder(File folder) {
    File[] files = folder.listFiles();
    if (files != null) {
      for (File f : files) {
        if (f.isDirectory()) {
          deleteFolder(f);
        } else {
          f.delete();
        }
      }
    }
    folder.delete();
  }
}