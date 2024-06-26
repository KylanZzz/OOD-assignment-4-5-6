package stock.model;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * The PortfolioStockModel interface provides methods for managing stock portfolios and
 * performing various financial calculations such as calculating gains, moving averages,
 * and detecting crossovers.
 * It also includes methods for portfolio management, such as creating,
 * deleting, and renaming portfolios, as well as adding and removing stocks from portfolios.
 */
public interface PortfolioStockModel extends StockModel {

  @Override
  default void addStockToPortfolio(String name, String ticker, int shares) {
    throw new UnsupportedOperationException("This method is not supported. "
            + "Please use addStockToPortfolio with date parameter.");
  }

  /**
   * Add the stock to the portfolio with date.
   *
   * @param name   the name of the portfolio.
   * @param ticker the name of the stock.
   * @param shares the quantity of the stock.
   * @param date   the date to buy the stock.
   * @throws IOException              if there is a data fetching error.
   * @throws IllegalArgumentException if shares is <= 0, ticker is invalid, date is invalid
   *                                  (this means that there is no earlier date where the stock
   *                                  has a recorded price), or if
   *                                  portfolio doesn't exist.
   */
  void addStockToPortfolio(String name, String ticker, int shares, LocalDate date) throws
          IOException, IllegalArgumentException;

  @Override
  default void removeStockFromPortfolio(String name, String ticker) {
    throw new UnsupportedOperationException(
            "This method is not supported. " + "Please use sellStockFromPortfolio instead.");
  }

  @Override
  default Map<String, Integer> getPortfolioContents(String name) {
    throw new UnsupportedOperationException("This method is no longer supported. Please use "
            + "getPortfolioContentsDecimal instead");
  }

  /**
   * Get the stocks and the corresponding shares in a portfolio.
   *
   * @param name        the name of the portfolio.
   * @param date        the date to get the state of the portfolio at.
   * @return            a map where the keys are the tickers of all the stocks and values are the
   *                    number of shares of that stock in the portfolio.
   * @throws IllegalArgumentException if the name of the ticker symbol does not exist.
   */
  Map<String, Double> getPortfolioContentsDecimal(String name, LocalDate date) throws
          IllegalArgumentException;

  /**
   * sell the stock from the portfolio using given date.
   *
   * @param name   the name of the portfolio.
   * @param ticker the name of the stock.
   * @param shares the quantity of the stock.
   * @param date   the date to sell the stock.
   * @throws IOException              if there is a data fetching error.
   * @throws IllegalArgumentException if shares is <= 0, ticker is invalid, date is invalid (this
   *                                  means that there is no earlier date where the stock has a
   *                                  recorded price), or if portfolio doesn't exist.
   */
  void sellStockFromPortfolio(String name, String ticker, int shares, LocalDate date) throws
          IOException, IllegalArgumentException;

  //
  //  /**
  //   * Get the value of a portfolio on a specific date. This should now return 0 if the requested
  //   * date was before the date of the first purchase in the portfolio.
  //   *
  //   * @param name the name of the portfolio.
  //   * @param date the date to get value at.
  //   * @return the value of the portfolio.
  //   * @throws IOException              if an I/O error occurs during data fetching.
  //   * @throws IllegalArgumentException if the name of the portfolio doesn't exist.
  //   */
  //  @Override
  //  double getPortfolioValue(String name, LocalDate date) throws IOException,
  //          IllegalArgumentException;

  /**
   * To get the distribution of certain portfolio by a date.
   *
   * @param name the name of the portfolio.
   * @param date the date that the user want to fetch.
   * @return A map of the stocks in the portfolio to their respective value.
   * @throws IOException              if a data fetching error occurs.
   * @throws IllegalArgumentException if the name of the portfolio doesn't exist.
   */
  Map<String, Double> getPortfolioDistribution(String name, LocalDate date) throws IOException,
          IllegalArgumentException;

  /**
   * Get all the disk saves of a portfolio. These files are loaded from each portfolios
   * respective folder in res/portfolios.
   *
   * @param name the name of the portfolio.
   * @return the filenames of the saved instances in sorted order of date (earliest to latest)
   * @throws IllegalArgumentException if the name of the portfolio doesn't exist.
   * @throws IOException              if an error occurs during file reading.
   */
  List<String> getPortfolioSaves(String name) throws IllegalArgumentException, IOException;

  /**
   * Load a previous save of a portfolio from disk.
   *
   * @param fileSaveName the name of the portfolio save file that you would like to load.
   * @throws IOException              if an I/O error occurs.
   * @throws IllegalArgumentException if the name of the portfolio doesn't exist, or the name of
   *                                  the file is not in the res/portfolios/${portfolio name}
   *                                  directory.
   */
  void loadPortfolioSave(String fileSaveName) throws IOException,
          IllegalArgumentException;

  /**
   * Creates a new save of a portfolio to disk.
   *
   * @param name the name of the portfolio.
   * @throws IOException              if a I/O error occurs
   * @throws IllegalArgumentException if the name of the portfolio doesn't exist.
   */
  void createNewPortfolioSave(String name) throws IOException, IllegalArgumentException;

  /**
   * Rebalance the portfolio from a given period. When a portfolio is rebalanced, shares of each
   * stock are purchased/sold so that the portfolio has even proportions (in terms of value) of
   * each stock.
   *
   * @param name        the name of the portfolio.
   * @param date        the date to rebalance. Only transactions that occurred before this date
   *                    will be
   *                    considered for rebalancing
   * @param proportions map of all the stocks to their relative proportions. Proportions should
   *                    be in decimal form, so 50% would be 0.5. All proportions must add up to 1
   *                    .0. All the stocks in the map must also exist in the portfolio at the
   *                    specified date.
   * @throws IOException              if a data fetching error occurs.
   * @throws IllegalArgumentException if the name of the stock doesn't exist, if the end date
   *                                  comes before the start date, or if any of the stocks in the
   *                                  portfolio didn't exist before the start date.
   */
  void rebalancePortfolio(String name, LocalDate date, Map<String, Double> proportions) throws
          IOException, IllegalArgumentException;

  /**
   * Get the performance of a value, which is a list of the value of the portfolio across a
   * timespan.
   *
   * @param name      the name of the portfolio.
   * @param startDate the starting date of the stock.
   * @param endDate   the ending date of the stock.
   * @return                  a map of the value of the portfolio at each date from startDate
   *                          to endDate, with one-day intervals in between.
   * @throws IOException              if a data fetching error occurs
   * @throws IllegalArgumentException if the start date is not before the end date or if the name
   *                                  of the ticker is invalid
   */
  Map<LocalDate, Double> getPortfolioPerformance(String name, LocalDate startDate,
                                                 LocalDate endDate) throws IllegalArgumentException,
          IOException;

}

