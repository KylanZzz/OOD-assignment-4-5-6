package stock.view;

import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;
import java.time.LocalDate;
import java.util.List;

/**
 * Provides a basic text-based user interface for displaying stock-related information,
 * including menus, portfolios, and stock metrics. This class interacts with users through
 * an Appendable object which allows for flexible output destinations.
 */
public class BasicStockView extends AbstractBasicStockView implements StockView {

  /**
   * Constructs a BasicStockView with a specified Appendable object to enable output.
   *
   * @param out The Appendable object to which all output will be directed.
   */
  public BasicStockView(Appendable out) {
    super(out);
    List<LocalDate> dateList = new ArrayList<>();
  }



  /**
   * Displays the welcome screen with a welcome message.
   */
  @Override
  public void printWelcomeScreen() {
    println("Welcome to OOD Assignment 4 for Stocks part 1!\n");
  }

  /**
   * Displays the main menu to the user.
   */
  @Override
  public void printMainMenu() {
    printOptionsPrompt();
    printMenu(BasicMenuOptions.mainMenu());
  }

  /**
   * Displays the portfolios view menu, allowing users to select from existing portfolios,
   * or view/edit them.
   *
   * @param portfolios A list of existing portfolio names.
   */
  @Override
  public void printViewPortfolios(List<String> portfolios) {
    var updatedPortfolioOptions = new ArrayList<>(BasicMenuOptions.viewPortfolios());
    portfolios.forEach(it -> updatedPortfolioOptions.add("View/Edit: " + it));

    printOptionsPrompt();
    printMenu(updatedPortfolioOptions);
  }

  /**
   * Displays the contents of a specific portfolio, including all stocks and their quantities.
   *
   * @param stocks A map containing stock tickers and their respective quantities.
   * @param name The name of the portfolio.
   */
  @Override
  public void printManagePortfolio(Map<String, Integer> stocks, String name) {
    println(String.format("Here are all the stocks in the %s portfolio:\n", name));
    var list = stocks.keySet().stream().sorted()
            .map(it -> String.format("%-30s %d", it, stocks.get(it)))
            .collect(Collectors.toList());
    list.add(0, String.format("%-30s %s", "Stock", "Shares"));
    printList(list);
    println("");

    printOptionsPrompt();
    printMenu(BasicMenuOptions.managePortfolio());
  }


  /**
   * Displays the dates of X-day crossovers for a specific stock starting from a given date.
   *
   * @param ticker The stock ticker.
   * @param date The starting date for calculating crossovers.
   * @param days The number of days to consider for the crossover.
   * @param dates A list of dates where crossovers occur.
   */
  @Override
  public void printXDayCrossovers(String ticker, LocalDate date, int days, List<LocalDate> dates) {
    println(String.format("Here are all the %d-day crossovers for the %s stock starting at %s:\n",
            days, ticker, date.toString()));
    printList(dates.stream().map(LocalDate::toString).collect(Collectors.toList()));
    println("");
  }

  /**
   * Displays the gain for a specific stock over a given period.
   *
   * @param ticker The stock ticker.
   * @param startDate The start date of the period.
   * @param endDate The end date of the period.
   * @param gain The calculated gain over the period.
   */
  @Override
  public void printStockGain(String ticker, LocalDate startDate, LocalDate endDate, double gain) {
    println(String.format("The gain for stock %s from %s to %s was $%.2f.\n",
            ticker, startDate, endDate, gain));
  }

  /**
   * Displays the average price of a stock over a specified number of days ending on a given date.
   *
   * @param ticker The stock ticker.
   * @param endDate The end date for the average calculation.
   * @param days The number of days over which the average is calculated.
   * @param average The calculated average price.
   */
  @Override
  public void printStockAverage(String ticker, LocalDate endDate, int days, double average) {
    println(String.format("The average for stock %s on %s for %d days was $%.2f.\n",
            ticker, endDate, days, average));
  }

  /**
   * Prints a generic message to the output.
   *
   * @param message The message to be printed.
   */
  @Override
  public void printMessage(String message) {
    println(message);
  }

}
