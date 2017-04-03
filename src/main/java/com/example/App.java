package com.example;

import java.util.Scanner;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * passthru
 */
public class App {
  private static final Logger logger = LoggerFactory.getLogger(App.class);
  private Options options;
  private CommandLine commandLine;
  private long listenPort;
  private String passthruAddress;
  private long passthruPort;
  private long delay = 0;
  private PassThruServer passthruServer;
  private StopClientsTimer stopClientsTimer;

  public static void main( String[] args ) {
    logger.info("passthru...");
    App app = new App();
    app.run(args);
    logger.info("...passthru");
  }

  private void buildOptions() {
    Option help = new Option("h", "help", false, "show help");
    Option listen = Option.builder("l").argName("listen port")
      .hasArg()
      .desc("listen on the server port")
      .longOpt("listen")
      .type(Number.class)
      .build();
    Option address = Option.builder("a").argName("passthru address")
      .hasArg()
      .desc("the passthru address")
      .longOpt("address")
      .build();
    Option port = Option.builder("p").argName("passthru port")
      .hasArg()
      .desc("the passthru port")
      .longOpt("port")
      .type(Number.class)
      .build();
    //Option delay = Option.builder("d").argName("delay milliseconds")
    //  .hasArg()
    //  .desc("delay in milliseconds")
    //  .longOpt("delay")
    //  .type(Number.class)
    //  .build();
    options = new Options();
    options.addOption(help);
    options.addOption(listen);
    options.addOption(address);
    options.addOption(port);
    //options.addOption(delay);
  }

  private void getOptions() throws ParseException {
    if (commandLine.hasOption("listen")) {
      listenPort = (long)commandLine.getParsedOptionValue("listen");
    } else {
      throw new ParseException("no listen option");
    }
    if (commandLine.hasOption("address")) {
      passthruAddress = commandLine.getOptionValue("address");
    } else {
      throw new ParseException("no address option");
    }
    if (commandLine.hasOption("port")) {
      passthruPort = (long)commandLine.getParsedOptionValue("port");
    } else {
      throw new ParseException("no port option");
    }
    //if (commandLine.hasOption("delay")) {
    //  delay = (long)commandLine.getParsedOptionValue("delay");
    //}
  }

  private void parseCommandLine(String[] args) throws ParseException {
    CommandLineParser parser = new DefaultParser();
    commandLine = parser.parse(options, args);
  }

  private void processCommands() {
    Scanner s = new Scanner(System.in);
    for (;;) {
      System.out.print("$ ");
      String line = s.nextLine();
      if ("help".equals(line)) {
        System.out.println("Commands:");
        System.out.println("  exit - exit application");
        System.out.println("  help - show help");
        System.out.println("  rpts - repeat stop start");
        System.out.println("  rptc - repeat stop cancel");
        System.out.println("  show - show settings");
        System.out.println("  stop - stop clients");
      } else if ("rpts".equals(line)) {
        stopClientsTimer.start();
      } else if ("rptc".equals(line)) {
        stopClientsTimer.stop();
      } else if ("stop".equals(line)) {
        passthruServer.stopClients();
      } else if ("show".equals(line)) {
        System.out.format("Server port:      %d\n", listenPort);
        System.out.format("Passthru address: %s\n", passthruAddress);
        System.out.format("Passthru port:    %d\n", passthruPort);
      } else if ("exit".equals(line)) {
        break;
      }
    }
    s.close();
  }

  private void run(String[] args) {
    try {
      buildOptions();
      parseCommandLine(args);
      if (commandLine.hasOption("help")) {
        usage();
        return;
      }
      getOptions();
      logger.debug("listening: {}", listenPort);
      logger.debug("passthru: {}:{}", passthruAddress, passthruPort);
      passthruServer = new PassThruServer(listenPort, passthruAddress, passthruPort, delay);
      passthruServer.start();
      stopClientsTimer = new StopClientsTimer(passthruServer);
      processCommands();
      passthruServer.stop();
    } catch (ParseException ex) {
      System.err.println("Parse error: " + ex.getMessage());
    }
  }

  private void usage() {
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp("passthru", options, true);
  }
}
