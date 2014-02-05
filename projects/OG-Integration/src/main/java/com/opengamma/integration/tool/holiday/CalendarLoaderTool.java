/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.holiday;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeParseException;

import au.com.bytecode.opencsv.CSVParser;
import au.com.bytecode.opencsv.CSVReader;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.component.tool.AbstractTool;
import com.opengamma.core.holiday.Holiday;
import com.opengamma.core.holiday.HolidayType;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.id.ExternalId;
import com.opengamma.master.holiday.HolidayDocument;
import com.opengamma.master.holiday.HolidayMaster;
import com.opengamma.master.holiday.ManageableHoliday;
import com.opengamma.scripts.Scriptable;
import com.opengamma.util.ArgumentChecker;

/**
 * Tool to load a calendar from a file.
 */
@Scriptable
public class CalendarLoaderTool extends AbstractTool<ToolContext> {

  /** The logger */
  private static final Logger s_logger = LoggerFactory.getLogger(CalendarLoaderTool.class);
  /** Determines whether this tool persists the calendar to the holiday master */
  private static final String DO_NOT_PERSIST = "do-not-persist";

  //-------------------------------------------------------------------------
  /**
   * Main method to run the tool.
   * 
   * @param args  the standard tool arguments, not null
   */
  public static void main(final String[] args) {  // CSIGNORE
    ArgumentChecker.isTrue(args.length > 3, "At least three arguments required: data file name, scheme name and calendar name");
    new CalendarLoaderTool().invokeAndTerminate(args);
  }

  //-------------------------------------------------------------------------
  @Override
  protected void doRun() {
    final CommandLine commandLine = getCommandLine();
    final boolean persist = !commandLine.hasOption(DO_NOT_PERSIST);
    final ToolContext toolContext = getToolContext();
    final HolidayMaster holidayMaster = toolContext.getHolidayMaster();  
    final String[] args = getCommandLine().getArgs();    
    final Holiday holiday = createManageableHoliday(args[0], args[1], args[2]);
    if (persist) {
      final HolidayDocument holidayDocument = new HolidayDocument(holiday);    
      holidayMaster.add(holidayDocument);
    }
    toolContext.close();
  }
  
  /**
   * Parses a csv file of dates and returns a {@link Holiday}
   * @param filePath The file path
   * @param schemeName The name of the scheme
   * @param calendarName The calendar name
   * @return The holiday
   * @throws DateTimeParseException If there is an entry that cannot be parsed by {@link LocalDate#parse}
   */
  private static Holiday createManageableHoliday(final String filePath, final String schemeName, final String calendarName) {
    final ManageableHoliday holiday = new ManageableHoliday();
    holiday.setType(HolidayType.CUSTOM);
    holiday.setCustomExternalId(ExternalId.of(schemeName, calendarName));
    CSVReader reader = null;
    try {
      reader = new CSVReader(new BufferedReader(new FileReader(filePath)), CSVParser.DEFAULT_SEPARATOR, CSVParser.DEFAULT_QUOTE_CHARACTER, 
          CSVParser.DEFAULT_ESCAPE_CHARACTER);
      String[] currentLine;
      while ((currentLine = reader.readNext()) != null) {
        for (String currentElement : currentLine) {
          if (!currentElement.isEmpty()) {
            //TODO ultimately, want to attempt different formats
            holiday.getHolidayDates().add(LocalDate.parse(currentElement.trim()));
          }
        }
      }
    } catch (IOException e) {
      throw new OpenGammaRuntimeException("Unable to read data field " + filePath, e);
    } finally {
      if (reader != null) {
        try {
          reader.close();
        } catch (IOException e) {
          s_logger.error("Failed to close reader ", e);
        }
      }
    }
    return holiday;
  }
  
  @Override
  protected Options createOptions(boolean mandatoryConfig) {
    Options options = super.createOptions(mandatoryConfig);
    options.addOption(createDoNotPersistOption());
    options.addOption(createVerboseOption());
    return options;
  }
  
  /**
   * Creates an option to turn persistence on and off.
   * @return The option
   */
  @SuppressWarnings("static-access")
  private static Option createDoNotPersistOption() {
    return OptionBuilder.isRequired(false)
        .hasArg(false)
        .withDescription("Calendar file will be parsed but not persisted")
        .withLongOpt(DO_NOT_PERSIST)
        .create("d");
  }
  
  /**
   * Creates an option to produce verbose output.
   * @return The option
   */
  @SuppressWarnings("static-access")
  private static Option createVerboseOption() {
    return OptionBuilder.isRequired(false)
                        .hasArg(false)
                        .withDescription("Verbose output")
                        .withLongOpt("verbose")
                        .create("v");
  }
  
  @Override
  protected void usage(Options options) {
    HelpFormatter formatter = new HelpFormatter();
    formatter.setWidth(120);
    formatter.printHelp("calendar-loader-tool.sh [file...]", options, true);
  }
  
}
