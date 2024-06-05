package stock.controller;

import org.junit.Test;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import stock.model.StockModel;
import stock.view.BasicMenuOptions;
import stock.view.StockView;

import static org.junit.Assert.assertEquals;
import static stock.controller.Interactions.inputs;
import static stock.controller.Interactions.modelLog;
import static stock.controller.Interactions.prints;

public class BasicStockControllerTest {

  protected final class MockModel implements StockModel {
    private StringBuilder log;
    private boolean throwIOException;

    public MockModel(StringBuilder log, boolean throwIOException){
      this.log = log;
      this.throwIOException = throwIOException;
    }

    // Dummy data 100
    @Override
    public double getGainOverTime(LocalDate startDate, LocalDate endDate, String ticker) throws IOException {
      if (throwIOException) {
        log.append("getGainOverTimeIOException\n");
        throw new IOException("gainIOExceptionMessage");
      }
      return 100;
    }

    // Dummy data 200
    @Override
    public double getMovingDayAverage(LocalDate endDate, int days, String ticker) throws IOException {
      if (throwIOException) {
        log.append("getMovingDayAverageIOException\n");
        throw new IOException("averageIOExceptionMessage");
      }
      return 200;
    }

    // Dummy data dates
    @Override
    public List<LocalDate> getCrossover(LocalDate endDate, int days, String ticker) throws IOException {
      if (throwIOException) {
        log.append("getCrossoverIOException\n");
        throw new IOException("crossoverIOExceptionMessage");
      }
      log.append("getCrossover").append(endDate).append(days).append(ticker).append("\n");
      return List.of(LocalDate.of(1,1,1), LocalDate.of(2,2,2));
    }

    @Override
    public void createNewPortfolio(String name) {
      log.append("createNewPortfolio").append(name).append("\n");
    }

    @Override
    public void deletePortfolio(String name) {
      log.append("deletePortfolio").append(name).append("\n");
    }

    @Override
    public void renamePortfolio(String oldName, String newName) {
      log.append("renamePortfolio").append(oldName).append(newName).append("\n");
    }

    // Dummy data
    @Override
    public Map<String, Integer> getPortfolioContents(String name) {
      return Map.of("AAPL", 5, "AMZN", 10, "NFLX", 15);
    }

    // Dummy data
    @Override
    public List<String> getPortfolios() {
      return List.of("S&P500", "NASDAQ");
    }

    // Dummy data 400
    @Override
    public double getPortfolioValue(String name, LocalDate date) throws IOException {
      if (throwIOException) {
        log.append("getPortfolioValueIOException\n");
        throw new IOException("portfolioValueIOExceptionMessage");
      }
      return 400;
    }

    @Override
    public void addStockToPortfolio(String name, String ticker, int shares) {
      log.append("addStockToPortfolio").append(name).append(ticker).append(shares).append("\n");
    }

    @Override
    public void removeStockFromPortfolio(String name, String ticker) {
      log.append("removeStockFromPortfolio").append(name).append(ticker).append("\n");
    }

    // List of valid stocks is hardcoded.
    @Override
    public boolean stockExists(String ticker) throws IOException {
      return List.of("AAPL", "NFLX", "AMZN", "TSLA").contains(ticker);
    }
  }

  protected final class MockView implements StockView {
    private StringBuilder log;

    public MockView(StringBuilder log){
      this.log = log;
    }

    @Override
    public void printWelcomeScreen() {
      log.append("printWelcomeScreen\n");
    }

    @Override
    public void printMainMenu() {
      log.append("printMainMenu\n");
    }

    @Override
    public void printMessage(String message) {
      log.append("printMessage").append(message).append("\n");
    }

    @Override
    public void printViewPortfolios(List<String> portfolios) {
      log.append("printViewPortfolios");
      for (var port: portfolios) {
        log.append(port);
      }
      log.append("\n");
    }

    @Override
    public void printManagePortfolio(Map<String, Integer> stocks, String name) {
      log.append("printManagePortfolios").append(name);
      List<String> list = new ArrayList<>();
      for (var s: stocks.entrySet()) {
        list.add(s.getKey());
        list.add(Integer.toString(s.getValue()));
      }
      Collections.sort(list);
      Collections.reverse(list);
      list.forEach(it -> log.append(it));
      log.append("\n");
    }

    @Override
    public void printXDayCrossovers(String ticker, LocalDate date, int days, List<LocalDate> dates) {
      log.append("printXDayCrossovers").append(ticker).append(date).append(days);
      for (var d: dates) {
        log.append(d);
      }
      log.append("\n");
    }

    @Override
    public void printStockGain(String ticker, LocalDate startDate, LocalDate endDate, double gain) {
      log.append("printStockGain").append(ticker).append(startDate).append(endDate).append(gain).append("\n");
    }

    @Override
    public void printStockAverage(String ticker, LocalDate endDate, int days, double average) {
      log.append("printStockAverage").append(ticker).append(endDate).append(days).append(average).append("\n");
    }
  }

  private void runTest(boolean throwException, Interaction... interactions) {
    StringBuilder expectedViewLog = new StringBuilder();
    StringBuilder expectedModelLog = new StringBuilder();
    StringBuilder fakeInput = new StringBuilder();

    for (var inter: interactions) {
      inter.apply(fakeInput, expectedViewLog, expectedModelLog);
    }

    Reader in = new StringReader(fakeInput.toString());
    StringBuilder viewLog = new StringBuilder();
    StringBuilder modelLog = new StringBuilder();
    StockView view = new MockView(viewLog);
    StockModel model = new MockModel(modelLog, throwException);
    StockController controller = new BasicStockController(view, model, in);
    controller.run();

    assertEquals(expectedViewLog.toString(), viewLog.toString());
    assertEquals(expectedModelLog.toString(), modelLog.toString());
  }

  String tickerPrompt = "printMessagePlease enter the ticker of the stock that you would like to know about:";
  String invalidDate = "printMessageInvalid date: Please enter a valid date.";
  String invalidDateFormat = "printMessageIncorrect format: Please enter the date in the format MM/DD/YYYY.";
  String invalidInputInteger = "printMessageInvalid input: not an integer, please try again.";
  String invalidInputDateOrder = "printMessageInvalid input: The end date must be after the start date.";
  String startDatePrompt = "printMessagePlease enter the starting date (inclusive) in the format MM/DD/YYYY:";
  String endDatePrompt = "printMessagePlease enter the ending date (inclusive) in the format MM/DD/YYYY:";
  String tickerIncorrect = "printMessageThat stock does not exist! Please try again.";

  @Test
  public void controllerExitsCorrectly() {
    runTest(
            false,
            prints("printMainMenu"),
            inputs("EXIT")
    );
  }

  @Test
  public void controllerHandlesInvalidMenuInputs() {
    String invalidInputMessage = "printMessageInvalid input. Please enter a valid choice (a number from 1 through "
            + BasicMenuOptions.mainMenu().size() + ") or " + BasicMenuOptions.exitKeyword()
            + " to exit the application.";
    runTest(
            false,
            prints("printMainMenu"),
            inputs("0"),
            prints(invalidInputMessage, "printMainMenu"),
            inputs("500000"),
            prints(invalidInputMessage, "printMainMenu"),
            inputs("a"),
            prints(invalidInputMessage, "printMainMenu"),
            inputs("-1"),
            prints(invalidInputMessage, "printMainMenu"),
            inputs("1+1"),
            prints(invalidInputMessage, "printMainMenu"),
            inputs("-10000"),
            prints(invalidInputMessage, "printMainMenu"),
            inputs("a"),
            prints(invalidInputMessage, "printMainMenu"),
            inputs("&(*&91j"),
            prints(invalidInputMessage, "printMainMenu"),
            inputs("EXIT")
    );
  }

  @Test
  public void controllerHandlesInvalidTicker() {
    runTest(
            false,
            prints("printMainMenu"),
            inputs("1"),

            prints(tickerPrompt),
            inputs("1093"),

            prints(tickerIncorrect),
            inputs("auiqhroiq"),

            prints(tickerIncorrect),
            inputs("APPLE"),

            prints(tickerIncorrect),
            inputs("GOOGLE"),

            prints(tickerIncorrect),
            inputs("AAPL"),

            prints(startDatePrompt),
            inputs("4/20/2005"),

            prints(endDatePrompt),
            inputs("4/20/2024"),

            prints("printStockGainAAPL2005-04-202024-04-20100.0"),
            prints("printMainMenu"),
            inputs("EXIT")
    );
  }

  @Test
  public void controllerHandlesIncorrectDate() {
    runTest(
            false,
            prints("printMainMenu"),
            inputs("1"),

            prints(tickerPrompt),
            inputs("AAPL"),

            prints(startDatePrompt),
            inputs("a&(*aLm.]10-"),

            prints(invalidDateFormat),
            inputs("a/20/2024"),

            prints(invalidInputInteger),
            inputs("20/2024"),

            prints(invalidDateFormat),
            inputs("20/2024"),

            prints(invalidDateFormat),
            inputs("202024/111/"),

            prints(invalidDateFormat),
            inputs("/11/24/2011"),

            prints(invalidDateFormat),
            inputs("-1/11/2011"),

            prints(invalidDate),
            inputs("1/32/2012"),

            prints(invalidDate),
            inputs("13/1/2011"),

            prints(invalidDate),
            inputs("4/20/2013"),

            prints(endDatePrompt),
            inputs("4/19/2013"),

            prints(invalidInputDateOrder),
            prints("printMainMenu"),
            inputs("1"),

            prints(tickerPrompt),
            inputs("AAPL"),

            prints(startDatePrompt),
            inputs("4/20/2013"),

            prints(endDatePrompt),
            inputs("4/20/2013"),

            prints(invalidInputDateOrder),
            prints("printMainMenu"),
            inputs("1"),

            prints(tickerPrompt),
            inputs("AAPL"),

            prints(startDatePrompt),
            inputs("4/20/2013"),

            prints(endDatePrompt),
            inputs("4/21/2013"),

            prints("printStockGainAAPL2013-04-202013-04-21100.0"),
            prints("printMainMenu"),
            inputs("EXIT")
    );
  }

  @Test
  public void movingAverageWorks() {
    runTest(
            false,
            prints("printMainMenu"),
            inputs("2"),

            prints(tickerPrompt),
            inputs("NFLX"),

            prints("printMessagePlease enter the ending date in the format MM/DD/YYYY:"),
            inputs("04/20/2013"),

            prints("printMessagePlease enter the number of days."),
            inputs("100"),

            prints("printStockAverageNFLX2013-04-20100200.0"),
            prints("printMainMenu"),
            inputs("EXIT")
    );
  }

  @Test
  public void crossoversWorks() {
    runTest(
            false,
            prints("printMainMenu"),
            inputs("3"),

            prints(tickerPrompt),
            inputs("AAPL"),

            prints("printMessagePlease enter the ending date in the format MM/DD/YYYY:"),
            inputs("04/20/2013"),

            prints("printMessagePlease enter the number of days."),
            inputs("13"),
            modelLog("getCrossover2013-04-2013AAPL"),

            prints("printXDayCrossoversAAPL" +
                    "2013-04-2013" +
                    "0001-01-01" +
                    "0002-02-02"
            ),
            prints("printMainMenu"),
            inputs("EXIT")
    );
  }

  @Test
  public void createOnePortfolioWorks() {
    runTest(
            false,
            prints("printMainMenu"),
            inputs("4"),

            prints("printViewPortfoliosS&P500NASDAQ"),
            inputs("1"),

            prints("printMessageWhat is the name of the portfolio you would like to create?"),
            inputs("ADOBE"),
            modelLog("createNewPortfolioADOBE"),

            prints("printMessageSuccessfully created portfolio ADOBE."),
            prints("printViewPortfoliosS&P500NASDAQ"),
            inputs("EXIT"),

            prints("printMainMenu"),
            inputs("EXIT")
    );
  }

  @Test
  public void createDuplicatePortfoliosDoesntWork() {
    runTest(
            false,
            prints("printMainMenu"),
            inputs("4"),

            prints("printViewPortfoliosS&P500NASDAQ"),
            inputs("1"),

            prints("printMessageWhat is the name of the portfolio you would like to create?"),
            inputs("ADOBE"),
            modelLog("createNewPortfolioADOBE"),

            prints("printMessageSuccessfully created portfolio ADOBE."),
            prints("printViewPortfoliosS&P500NASDAQ"),
            inputs("1"),

            prints("printMessageWhat is the name of the portfolio you would like to create?"),
            inputs("S&P500"),

            prints("printMessageA portfolio with that name already exists!"),
            prints("printViewPortfoliosS&P500NASDAQ"),
            inputs("1"),

            prints("printMessageWhat is the name of the portfolio you would like to create?"),
            inputs("AAAAAA"),
            modelLog("createNewPortfolioAAAAAA"),

            prints("printMessageSuccessfully created portfolio AAAAAA."),
            prints("printViewPortfoliosS&P500NASDAQ"),
            inputs("EXIT"),

            prints("printMainMenu"),
            inputs("EXIT")
    );
  }

  @Test
  public void deleteProfileWorks() {
    runTest(
            false,
            prints("printMainMenu"),
            inputs("4"),

            prints("printViewPortfoliosS&P500NASDAQ"),
            inputs("2"),

            prints("printMessageWhat portfolio would you like to delete?"),
            inputs("NASDAQ"),
            modelLog("deletePortfolioNASDAQ"),

            prints("printMessageSuccessfully deleted portfolio NASDAQ."),
            prints("printViewPortfoliosS&P500NASDAQ"),
            inputs("2"),

            prints("printMessageWhat portfolio would you like to delete?"),
            inputs("S&P50"),

            prints("printMessageA portfolio with that name does not exist!"),
            prints("printViewPortfoliosS&P500NASDAQ"),
            inputs("2"),

            prints("printMessageWhat portfolio would you like to delete?"),
            inputs("AAPL"),

            prints("printMessageA portfolio with that name does not exist!"),
            prints("printViewPortfoliosS&P500NASDAQ"),
            inputs("EXIT"),

            prints("printMainMenu"),
            inputs("EXIT")
    );
  }

  @Test
  public void renameProfileWorks() {
    runTest(
            false,
            prints("printMainMenu"),
            inputs("4"),

            prints("printViewPortfoliosS&P500NASDAQ"),
            inputs("3"),

            prints("printMessageWhat portfolio would you like to rename? (Please enter the name)."),
            inputs("NASDAQ"),

            prints("printMessageWhat would you like to rename this portfolio to?"),
            inputs("AAAAAA"),
            modelLog("renamePortfolioNASDAQAAAAAA"),

            prints("printMessageSuccessfully renamed portfolio NASDAQ to AAAAAA."),
            prints("printViewPortfoliosS&P500NASDAQ"),
            inputs("EXIT"),

            prints("printMainMenu"),
            inputs("EXIT")
    );
  }

  @Test
  public void controllerHandlesGainIOException() {
    runTest(
            true,
            prints("printMainMenu"),
            inputs("1"),

            prints(tickerPrompt),
            inputs("AAPL"),

            prints(startDatePrompt),
            inputs("01/01/2020"),

            prints(endDatePrompt),
            inputs("01/01/2021"),

            modelLog("getGainOverTimeIOException"),
            prints("printMessageError while fetching data: gainIOExceptionMessage"),
            prints("printMainMenu"),
            inputs("EXIT")
    );
  }

  @Test
  public void controllerHandlesCrossoverIOException() {
    runTest(
            true,
            prints("printMainMenu"),
            inputs("3"),

            prints(tickerPrompt),
            inputs("AAPL"),

            prints("printMessagePlease enter the ending date in the format MM/DD/YYYY:"),
            inputs("01/01/2021"),

            prints("printMessagePlease enter the number of days."),
            inputs("50"),

            modelLog("getCrossoverIOException"),
            prints("printMessageError while fetching data: crossoverIOExceptionMessage"),
            prints("printMainMenu"),
            inputs("EXIT")
    );
  }

  @Test
  public void controllerHandlesAverageIOException() {
    runTest(
            true,
            prints("printMainMenu"),
            inputs("2"),

            prints(tickerPrompt),
            inputs("AAPL"),

            prints("printMessagePlease enter the ending date in the format MM/DD/YYYY:"),
            inputs("01/01/2021"),

            prints("printMessagePlease enter the number of days."),
            inputs("50"),

            modelLog("getMovingDayAverageIOException"),
            prints("printMessageError while fetching data: averageIOExceptionMessage"),
            prints("printMainMenu"),
            inputs("EXIT")
    );
  }

  @Test
  public void renameNonExistentPortfolio() {
    runTest(
            false,
            prints("printMainMenu"),
            inputs("4"),

            prints("printViewPortfoliosS&P500NASDAQ"),
            inputs("3"),

            prints("printMessageWhat portfolio would you like to rename? (Please enter the name)."),
            inputs("S&N500"),

            prints("printMessageA portfolio with that name does not exist!"),
            prints("printViewPortfoliosS&P500NASDAQ"),
            inputs("EXIT"),

            prints("printMainMenu"),
            inputs("EXIT")
    );
  }

  @Test
  public void renamePortfolioToExistingName() {
    runTest(
            false,
            prints("printMainMenu"),
            inputs("4"),

            prints("printViewPortfoliosS&P500NASDAQ"),
            inputs("3"),

            prints("printMessageWhat portfolio would you like to rename? (Please enter the name)."),
            inputs("S&P500"),

            prints("printMessageWhat would you like to rename this portfolio to?"),
            inputs("NASDAQ"),

            prints("printMessageA portfolio with that name already exists!"),
            prints("printViewPortfoliosS&P500NASDAQ"),
            inputs("EXIT"),

            prints("printMainMenu"),
            inputs("EXIT")
    );
  }

  @Test
  public void viewPortfoliosMenuHandlesInvalidInput() {
    runTest(
            false,
            prints("printMainMenu"),
            inputs("4"),

            prints("printViewPortfoliosS&P500NASDAQ"),
            inputs("0"),

            prints("printMessageInvalid input. Please enter a valid choice (a number from 1 through 3) or EXIT to go back."),
            prints("printViewPortfoliosS&P500NASDAQ"),
            inputs("10"),

            prints("printMessageInvalid input. Please enter a valid choice (a number from 1 through 3) or EXIT to go back."),
            prints("printViewPortfoliosS&P500NASDAQ"),
            inputs("a"),

            prints("printMessageInvalid input. Please enter a valid choice (a number from 1 through 3) or EXIT to go back."),
            prints("printViewPortfoliosS&P500NASDAQ"),
            inputs("-1"),

            prints("printMessageInvalid input. Please enter a valid choice (a number from 1 through 3) or EXIT to go back."),
            prints("printViewPortfoliosS&P500NASDAQ"),
            inputs("EXIT"),

            prints("printMainMenu"),
            inputs("EXIT")
    );
  }

  @Test
  public void addStockWorksForMultipleStocks() {
    runTest(
            false,
            prints("printMainMenu"),
            inputs("4"),

            prints("printViewPortfoliosS&P500NASDAQ"),
            inputs("4"),

            prints("printManagePortfoliosS&P500NFLXAMZNAAPL51510"),
            inputs("2"),

            prints("printMessagePlease enter the ticker of the stock that you would like to add to portfolio S&P500:"),
            inputs("AAPL"),

            prints("printMessagePlease enter the number of shares you would like to purchase (you cannot buy fractional number of stocks): "),
            inputs("50"),
            modelLog("addStockToPortfolioS&P500AAPL50"),

            prints("printMessageSuccessfully purchased 50 number of AAPL stocks in the S&P500 portfolio."),
            prints("printManagePortfoliosS&P500NFLXAMZNAAPL51510"),
            inputs("2"),

            prints("printMessagePlease enter the ticker of the stock that you would like to add to portfolio S&P500:"),
            inputs("NFLX"),

            prints("printMessagePlease enter the number of shares you would like to purchase (you cannot buy fractional number of stocks): "),
            inputs("100"),
            modelLog("addStockToPortfolioS&P500NFLX100"),

            prints("printMessageSuccessfully purchased 100 number of NFLX stocks in the S&P500 portfolio."),
            prints("printManagePortfoliosS&P500NFLXAMZNAAPL51510"),
            inputs("EXIT"),

            prints("printViewPortfoliosS&P500NASDAQ"),
            inputs("EXIT"),

            prints("printMainMenu"),
            inputs("EXIT")
    );
  }

  @Test
  public void addStockToPortfolioWorksWithInvalidTickers() {
    runTest(
            false,
            prints("printMainMenu"),
            inputs("4"),

            prints("printViewPortfoliosS&P500NASDAQ"),
            inputs("4"),

            prints("printManagePortfoliosS&P500NFLXAMZNAAPL51510"),
            inputs("2"),

            prints("printMessagePlease enter the ticker of the stock that you would like to add to portfolio S&P500:"),
            inputs("UYHGBN"),

            prints("printMessageThat stock does not exist! Please try again."),
            inputs("$%^YTG"),

            prints("printMessageThat stock does not exist! Please try again."),
            inputs("7654rfgy"),

            prints("printMessageThat stock does not exist! Please try again."),
            inputs("5rtgfd"),

            prints("printMessageThat stock does not exist! Please try again."),
            inputs("4ewsdfg"),

            prints("printMessageThat stock does not exist! Please try again."),
            inputs("WSDF"),

            prints("printMessageThat stock does not exist! Please try again."),
            inputs("AAPL"),

            prints("printMessagePlease enter the number of shares you would like to purchase (you cannot buy fractional number of stocks): "),
            inputs("50"),
            modelLog("addStockToPortfolioS&P500AAPL50"),

            prints("printMessageSuccessfully purchased 50 number of AAPL stocks in the S&P500 portfolio."),
            prints("printManagePortfoliosS&P500NFLXAMZNAAPL51510"),
            inputs("EXIT"),

            prints("printViewPortfoliosS&P500NASDAQ"),
            inputs("EXIT"),

            prints("printMainMenu"),
            inputs("EXIT")
    );
  }

  @Test
  public void addStockHandlesInvalidShareInputs() {
    runTest(
            false,
            prints("printMainMenu"),
            inputs("4"),

            prints("printViewPortfoliosS&P500NASDAQ"),
            inputs("4"),

            prints("printManagePortfoliosS&P500NFLXAMZNAAPL51510"),
            inputs("2"),

            prints("printMessagePlease enter the ticker of the stock that you would like to add to portfolio S&P500:"),
            inputs("AAPL"),

            prints("printMessagePlease enter the number of shares you would like to purchase (you cannot buy fractional number of stocks): "),
            inputs("abcd"),
            prints("printMessageInvalid input: not an integer, please try again."),
            prints("printManagePortfoliosS&P500NFLXAMZNAAPL51510"),
            inputs("2"),

            prints("printMessagePlease enter the ticker of the stock that you would like to add to portfolio S&P500:"),
            inputs("AAPL"),

            prints("printMessagePlease enter the number of shares you would like to purchase (you cannot buy fractional number of stocks): "),
            inputs("$%^&"),
            prints("printMessageInvalid input: not an integer, please try again."),
            prints("printManagePortfoliosS&P500NFLXAMZNAAPL51510"),
            inputs("2"),

            prints("printMessagePlease enter the ticker of the stock that you would like to add to portfolio S&P500:"),
            inputs("AAPL"),

            prints("printMessagePlease enter the number of shares you would like to purchase (you cannot buy fractional number of stocks): "),
            inputs("10.5"),
            prints("printMessageInvalid input: not an integer, please try again."),
            prints("printManagePortfoliosS&P500NFLXAMZNAAPL51510"),
            inputs("2"),

            prints("printMessagePlease enter the ticker of the stock that you would like to add to portfolio S&P500:"),
            inputs("AAPL"),

            prints("printMessagePlease enter the number of shares you would like to purchase (you cannot buy fractional number of stocks): "),
            inputs("-10"),
            prints("printMessageCannot purchase negative number of stocks."),
            prints("printManagePortfoliosS&P500NFLXAMZNAAPL51510"),
            inputs("2"),

            prints("printMessagePlease enter the ticker of the stock that you would like to add to portfolio S&P500:"),
            inputs("AAPL"),

            prints("printMessagePlease enter the number of shares you would like to purchase (you cannot buy fractional number of stocks): "),
            inputs("0"),
            prints("printMessageCannot purchase 0 shares of a stock."),
            prints("printManagePortfoliosS&P500NFLXAMZNAAPL51510"),
            inputs("2"),

            prints("printMessagePlease enter the ticker of the stock that you would like to add to portfolio S&P500:"),
            inputs("AAPL"),

            prints("printMessagePlease enter the number of shares you would like to purchase (you cannot buy fractional number of stocks): "),
            inputs("50"),
            modelLog("addStockToPortfolioS&P500AAPL50"),

            prints("printMessageSuccessfully purchased 50 number of AAPL stocks in the S&P500 portfolio."),
            prints("printManagePortfoliosS&P500NFLXAMZNAAPL51510"),
            inputs("EXIT"),

            prints("printViewPortfoliosS&P500NASDAQ"),
            inputs("EXIT"),

            prints("printMainMenu"),
            inputs("EXIT")
    );
  }

  @Test
  public void removeStockFromPortfolio() {
    runTest(
            false,
            prints("printMainMenu"),
            inputs("4"),

            prints("printViewPortfoliosS&P500NASDAQ"),
            inputs("4"),

            prints("printManagePortfoliosS&P500NFLXAMZNAAPL51510"),
            inputs("3"),

            prints("printMessagePlease enter the ticker of the stock that you would like to remove from portfolio S&P500."),
            inputs("AAPL"),
            modelLog("removeStockFromPortfolioS&P500AAPL"),

            prints("printMessageSuccessfully removed stock AAPL from portfolio S&P500."),
            prints("printManagePortfoliosS&P500NFLXAMZNAAPL51510"),
            inputs("EXIT"),

            prints("printViewPortfoliosS&P500NASDAQ"),
            inputs("EXIT"),

            prints("printMainMenu"),
            inputs("EXIT")
    );
  }

  @Test
  public void removeStockHandlesInvalidInputs() {
    runTest(
            false,
            prints("printMainMenu"),
            inputs("4"),

            prints("printViewPortfoliosS&P500NASDAQ"),
            inputs("4"),

            prints("printManagePortfoliosS&P500NFLXAMZNAAPL51510"),
            inputs("3"),

            prints("printMessagePlease enter the ticker of the stock that you would like to remove from portfolio S&P500."),
            inputs("XYZ123"),
            prints("printMessageThat stock does not exist! Please try again."),
            inputs("3"),

            prints("printMessageThat stock does not exist! Please try again."),
            inputs("GOOGL"),
            prints("printMessageThat stock does not exist! Please try again."),
            inputs("3"),

            prints("printMessageThat stock does not exist! Please try again."),
            inputs("NFLX100"),
            prints("printMessageThat stock does not exist! Please try again."),
            inputs("3"),

            prints("printMessageThat stock does not exist! Please try again."),
            inputs("aaaa"),
            prints("printMessageThat stock does not exist! Please try again."),
            inputs("3"),

            prints("printMessageThat stock does not exist! Please try again."),
            inputs("TSLA"),

            prints("printMessageThat stock is not in the portfolio."),
            prints("printManagePortfoliosS&P500NFLXAMZNAAPL51510"),
            inputs("EXIT"),

            prints("printViewPortfoliosS&P500NASDAQ"),
            inputs("EXIT"),

            prints("printMainMenu"),
            inputs("EXIT")
    );
  }

  @Test
  public void managePortfolioMenuHandlesInvalidInput() {
    runTest(
            false,
            prints("printMainMenu"),
            inputs("4"),

            prints("printViewPortfoliosS&P500NASDAQ"),
            inputs("4"),

            prints("printManagePortfoliosS&P500NFLXAMZNAAPL51510"),
            inputs("invalid"),

            prints("printMessageInvalid input. Please enter a valid choice (a number from 1 through 3) or EXIT to go back."),
            prints("printManagePortfoliosS&P500NFLXAMZNAAPL51510"),
            inputs("10"),

            prints("printMessageInvalid input. Please enter a valid choice (a number from 1 through 3) or EXIT to go back."),
            prints("printManagePortfoliosS&P500NFLXAMZNAAPL51510"),
            inputs("0"),

            prints("printMessageInvalid input. Please enter a valid choice (a number from 1 through 3) or EXIT to go back."),
            prints("printManagePortfoliosS&P500NFLXAMZNAAPL51510"),
            inputs("1+1"),

            prints("printMessageInvalid input. Please enter a valid choice (a number from 1 through 3) or EXIT to go back."),
            prints("printManagePortfoliosS&P500NFLXAMZNAAPL51510"),
            inputs("-5"),

            prints("printMessageInvalid input. Please enter a valid choice (a number from 1 through 3) or EXIT to go back."),
            prints("printManagePortfoliosS&P500NFLXAMZNAAPL51510"),
            inputs("2.5"),

            prints("printMessageInvalid input. Please enter a valid choice (a number from 1 through 3) or EXIT to go back."),
            prints("printManagePortfoliosS&P500NFLXAMZNAAPL51510"),
            inputs("EXIT"),

            prints("printViewPortfoliosS&P500NASDAQ"),
            inputs("EXIT"),

            prints("printMainMenu"),
            inputs("EXIT")
    );
  }

  @Test
  public void controllerHandlesGetPortfolioValueIOException() {
    runTest(
            true,
            prints("printMainMenu"),
            inputs("4"),

            prints("printViewPortfoliosS&P500NASDAQ"),
            inputs("4"),

            prints("printManagePortfoliosS&P500NFLXAMZNAAPL51510"),
            inputs("1"),

            prints("printMessageWhat date would you like to know the value of portfolio S&P500 at? Please enter the date in the format MM/DD/YYYY."),
            inputs("01/01/2021"),

            modelLog("getPortfolioValueIOException"),
            prints("printMessageError occurred while fetching data: portfolioValueIOExceptionMessage"),
            prints("printManagePortfoliosS&P500NFLXAMZNAAPL51510"),
            inputs("EXIT"),

            prints("printViewPortfoliosS&P500NASDAQ"),
            inputs("EXIT"),

            prints("printMainMenu"),
            inputs("EXIT")
    );
  }

  @Test
  public void calculatePortfolioValue() {
    runTest(
            false,
            prints("printMainMenu"),
            inputs("4"),

            prints("printViewPortfoliosS&P500NASDAQ"),
            inputs("4"),

            prints("printManagePortfoliosS&P500NFLXAMZNAAPL51510"),
            inputs("1"),

            prints("printMessageWhat date would you like to know the value of portfolio S&P500 at? Please enter the date in the format MM/DD/YYYY."),
            inputs("01/01/2021"),

            prints("printMessageThe value of the portfolio S&P500 at 2021-01-01 is 400.00."),
            prints("printManagePortfoliosS&P500NFLXAMZNAAPL51510"),
            inputs("EXIT"),

            prints("printViewPortfoliosS&P500NASDAQ"),
            inputs("EXIT"),

            prints("printMainMenu"),
            inputs("EXIT")
    );
  }

  @Test
  public void xDayCrossoverHandlesInvalidDaysInput() {
    runTest(
            false,
            prints("printMainMenu"),
            inputs("3"),

            prints(tickerPrompt),
            inputs("AAPL"),

            prints("printMessagePlease enter the ending date in the format MM/DD/YYYY:"),
            inputs("01/01/2021"),

            prints("printMessagePlease enter the number of days."),
            inputs("abcd"),
            prints("printMessageInvalid input: not an integer, please try again."),
            inputs("$%^&"),
            prints("printMessageInvalid input: not an integer, please try again."),
            inputs("10.5"),
            prints("printMessageInvalid input: not an integer, please try again."),
            inputs("-10"),
            prints("printMessageInvalid input: not a valid number. Please enter a number from 1 to 2147483647"),
            inputs("100"),
            modelLog("getCrossover2021-01-01100AAPL"),
            prints("printXDayCrossoversAAPL2021-01-011000001-01-010002-02-02"),

            prints("printMainMenu"),
            inputs("EXIT")
    );
  }

}