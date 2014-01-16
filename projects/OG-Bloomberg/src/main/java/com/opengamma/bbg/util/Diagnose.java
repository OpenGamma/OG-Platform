/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * <p/>
 * Please see distribution for license.
 */
package com.opengamma.bbg.util;

import java.io.IOException;
import java.util.Iterator;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.jdeferred.Deferred;
import org.jdeferred.DeferredManager;
import org.jdeferred.DoneCallback;
import org.jdeferred.Promise;
import org.jdeferred.impl.DefaultDeferredManager;
import org.jdeferred.impl.DeferredObject;

import com.bloomberglp.blpapi.CorrelationID;
import com.bloomberglp.blpapi.Element;
import com.bloomberglp.blpapi.Event;
import com.bloomberglp.blpapi.EventHandler;
import com.bloomberglp.blpapi.Message;
import com.bloomberglp.blpapi.MessageIterator;
import com.bloomberglp.blpapi.Request;
import com.bloomberglp.blpapi.Service;
import com.bloomberglp.blpapi.Session;
import com.bloomberglp.blpapi.SessionOptions;
import com.bloomberglp.blpapi.Subscription;
import com.bloomberglp.blpapi.SubscriptionList;
import com.opengamma.scripts.Scriptable;

/** Diagnostic tool for bloomberg API server */
@Scriptable
public class Diagnose {

  private static final String USAGE = "java com.opengamma.bbg.util.Diagnose";

  private static CommandLine s_line;
  private static String s_host = "localhost";
  private static String s_port = "8194";
  private static final String TICKER = "BBHBEAT Index";
  private static final String LIVE_FIELD = "LAST_PRICE";
  private static final String REF_FIELD = "API_MACHINE";
  private static String s_timeout = "5000";
  private static boolean s_verbose;

  private static final Deferred<String, Void, Void> REF_OK = new DeferredObject<>();
  private static final Deferred<String, Void, Void> MKT_OK = new DeferredObject<>();

  public static void main(String[] args) throws Exception {

    final Options options = createOptions();
    final CommandLineParser parser = new PosixParser();

    try {
      s_line = parser.parse(options, args);
    } catch (final ParseException e) {
      usage(options);
      System.exit(1);
    }

    s_host = s_line.getOptionValue("h", s_host);
    s_port = s_line.getOptionValue("p", s_port);
    s_timeout = s_line.getOptionValue("t", s_timeout);
    s_verbose = s_line.hasOption("v");

    try {
      Integer.parseInt(s_port);
    } catch (NumberFormatException e) {
      System.err.println("port should be intiger value");
      usage(options);
    }

    try {
      Integer.parseInt(s_timeout);
    } catch (NumberFormatException e) {
      System.err.println("timeout should be intiger value");
      usage(options);
    }

    SessionOptions sessionOptions = new SessionOptions();
    sessionOptions.setServerHost(s_host);
    sessionOptions.setServerPort(Integer.parseInt(s_port));

    Session session = new Session(sessionOptions, new MyEventHandler());
    session.startAsync();

    DeferredManager dm = new DefaultDeferredManager();
    Promise completed = dm.when(MKT_OK.promise(), REF_OK.promise());
    completed.done(new DoneCallback() {
      @Override
      public void onDone(Object o) {
        // all good
        System.exit(0);
      }
    });

    completed.waitSafely(Integer.parseInt(s_timeout));
    if (!MKT_OK.isResolved()) {
      System.err.printf("Received no market data within timeout of %s ms\n", s_timeout);
    }
    if (!REF_OK.isResolved()) {
      System.err.printf("Received no market data within timeout of %s ms\n", s_timeout);
    }
    System.exit(1);
  }

  private static Options createOptions() {
    final Options options = new Options();
    Option option = new Option("h", "host", true, "bloomberg host address (default " + s_host + ")");
    options.addOption(option);
    option = new Option("p", "port", true, "bloomberg host port (default " + s_port + ")");
    options.addOption(option);
    option = new Option("t", "timeout", true, "timeout in milliseconds (default " + s_timeout + ")");
    options.addOption(option);
    option = new Option("v", "verbose", false, "use verbose output");
    options.addOption(option);
    return options;
  }

  private static void usage(final Options options) {
    final HelpFormatter formatter = new HelpFormatter();
    formatter.setWidth(120);
    formatter.printHelp(USAGE, options, true);
  }

  static class MyEventHandler implements EventHandler {

    public void handleEventVerbose(Event event) {
      Iterator<Message> messages = event.iterator();
      while (messages.hasNext()) {
        Message message = messages.next();
        System.out.println("\n");
        try {
          message.print(System.out);
        } catch (IOException e) {
          e.printStackTrace();
        }
        System.out.println("\n");
      }
    }

    public void handleEvent(Event event) {
      Iterator<Message> messages = event.iterator();
      while (messages.hasNext()) {
        Message message = messages.next();
        System.out.println(message.messageType());
      }
    }

    public void processEvent(Event event, Session session) {

      MessageIterator iter = event.messageIterator();
      while (iter.hasNext()) {
        Message message = iter.next();

        if (s_verbose) {
          handleEventVerbose(event);
        }

        if (message.messageType().equals("SessionStartupFailure")) {
          System.err.printf("Session startup  failed.");
          System.exit(1);
        }
        if (message.messageType().equals("SubscriptionFailure")) {
          System.err.printf("Subsription failed. Ticker:%s, Field:%s", TICKER, LIVE_FIELD);
          System.exit(1);
        }
        if (message.messageType().equals("SessionStarted")) {
          try {

            SubscriptionList subscriptions = new SubscriptionList();
            subscriptions.add(new Subscription(TICKER, LIVE_FIELD));
            session.subscribe(subscriptions);

            if (!session.openService("//blp/refdata")) {
              System.out.println("Could not open service //blp/refdata");
              System.exit(1);
            }

            Service refDataSvc = session.getService("//blp/refdata");
            Request request = refDataSvc.createRequest("ReferenceDataRequest");
            request.append("securities", TICKER);
            request.append("fields", REF_FIELD);
            session.sendRequest(request, new CorrelationID(20));

          } catch (Exception e) {
            System.err.println("Could not open //blp/mktdata for async. " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
          }
        } else if (message.messageType().equals("ReferenceDataResponse")) {
          Element securityDataArray = message.getElement("securityData");
          int numItems = securityDataArray.numValues();
          if (numItems > 0) {
            Element securityData = securityDataArray.getValueAsElement(0);
            if (securityData.hasElement("securityError")) {
              Element securityError = securityData.getElement("securityError");
              System.err.println("Error retrieving reference data: " + securityError);
              System.exit(1);
            } else {
              REF_OK.resolve("ok");
              Element fieldData = securityData.getElement("fieldData");
              // Element ref = fieldData.getElement(REF_FIELD);
              System.out.printf("Successfully received reference value for  Ticker:%s, Field:%s",
                                TICKER,
                                fieldData.getElement(REF_FIELD));

            }
          }
        } else if (message.messageType().equals("MarketDataEvents") && message.getElement(LIVE_FIELD) != null) {
          MKT_OK.resolve("ok");
          System.out.printf("Successfully received live value for  Ticker:%s, Field:%s",
                            TICKER, message.getElement(LIVE_FIELD));
        } else {
          handleEvent(event);
        }
      }
    }
  }

}
