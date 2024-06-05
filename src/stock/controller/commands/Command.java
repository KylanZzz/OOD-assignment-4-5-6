package stock.controller.commands;

import java.io.IOException;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.InputMismatchException;
import java.util.Scanner;
import stock.view.StockView;
import stock.model.StockModel;

public abstract class Command {
  protected final StockView view;
  protected final StockModel model;
  protected final Scanner scanner;

  public Command(StockView view, StockModel model, Scanner scanner) {
    this.view = view;
    this.model = model;
    this.scanner = scanner;
  }

  public abstract void apply();

  // Some helper functions below

  protected final int getPositiveFromUser(int max) {
    int choice = -1;
    while (choice == -1) {
      try {
        choice = scanner.nextInt();
        if (choice > max || choice < 1) {
          throw new IllegalArgumentException();
        }
        scanner.nextLine();
      } catch (InputMismatchException e) {
        view.printMessage("Invalid input: not an integer, please try again.");
        scanner.nextLine();
      } catch (IllegalArgumentException e) {
        view.printMessage("Invalid input: not a valid number. Please enter a number from 1 to " +
                max);
        choice = -1;
        scanner.nextLine();
      }
    }

    return choice;
  }

  protected final String getTickerFromUser() {
    while (true) {
      String ticker = scanner.nextLine().toUpperCase();
      try {
        if (model.stockExists(ticker)) {
          return ticker;
        } else {
          view.printMessage("That stock does not exist! Please try again.");
        }
      } catch (IOException e) {
        view.printMessage("Error while fetching data: " + e.getMessage());
      }
    }
  }

  protected final LocalDate getDateFromUser() {
    LocalDate date = null;
    while (date == null) {
      try {
        String input = scanner.nextLine();
        String[] split = input.split("/");

        if (split.length != 3) {
          throw new IllegalArgumentException();
        }

        int month = Integer.parseInt(split[0]);
        int day = Integer.parseInt(split[1]);
        int year = Integer.parseInt(split[2]);

        date = LocalDate.of(year, month, day);
      } catch (NumberFormatException e) {
        view.printMessage("Invalid input: not an integer, please try again.");
      } catch (DateTimeException e) {
        view.printMessage("Invalid date: Please enter a valid date.");
      } catch (Exception e) {
        view.printMessage("Incorrect format: Please enter the date in the format MM/DD/YYYY.");
      }
    }

    return date;
  }


}
