package stock.controller;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Scanner;

import stock.controller.commands.CalculateAverage;
import stock.controller.commands.CalculateCrossover;
import stock.controller.commands.CalculateGain;
import stock.controller.commands.Command;
import stock.controller.commands.ViewPortfolios;
import stock.model.AlphaVantageDataSource;
import stock.model.BasicStockModel;
import stock.model.StockModel;
import stock.view.BasicMenuOptions;
import stock.view.BasicStockView;
import stock.view.StockView;
import java.util.Map;

/**
 * A simple implementation of a stock controller. It allows the user to analyze
 * stocks such as finding the x-day average, gain/loss over a period of time,
 * as well as creating portfolios and analyzing their value through text-based
 * input.
 */
public class BasicStockController implements StockController {

  private final StockView view;
  private final StockModel model;
  private final Scanner scanner;
  private final Map<String, Command> commands;

  /**
   * Constructs a BasicStockController with the given view, model, and input
   * source.
   * @param view component of the MVC which displays information to the user.
   * @param model component of MVC which performs operations on data.
   * @param in the source of input for the application.
   */
  public BasicStockController(StockView view, StockModel model, Readable in) {
    scanner = new Scanner(in);
    this.view = view;
    this.model = model;
    commands = new HashMap<>();
    initializeCommands();
  }

  protected void initializeCommands() {
    commands.put("1", new CalculateGain(view, model, scanner));
    commands.put("2", new CalculateAverage(view, model, scanner));
    commands.put("3", new CalculateCrossover(view, model, scanner));
    commands.put("4", new ViewPortfolios(view, model, scanner));
  }

  /**
   * Starts the stock application. Displays the main menu, reads user input,
   * and executes the commands until the user exits the application.
   */
  @Override
  public void run() {
    String choice = "";

    while (!choice.equals(BasicMenuOptions.exitKeyword())) {
      view.printMainMenu();
      choice = scanner.nextLine();

      if (commands.containsKey(choice)) {
        commands.get(choice).apply();
      } else if (!choice.equals(BasicMenuOptions.exitKeyword())){
        view.printMessage("Invalid input. Please enter a valid choice (a number from 1 through "
                + BasicMenuOptions.mainMenu().size() + ") or " + BasicMenuOptions.exitKeyword()
        + " to exit the application.");
      }
    }
  }

  public static void main(String[] args) {

    StockView view = new BasicStockView(System.out);
//    StockModel model = new BasicStockModel(new CSVDataSource("res/CSVData"));
    StockModel model = new BasicStockModel(new AlphaVantageDataSource());
    StockController controller = new BasicStockController(view, model, new InputStreamReader(System.in));
    controller.run();
  }
}
