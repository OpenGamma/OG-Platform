/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.tool;

import java.util.Arrays;

import javax.time.calendar.LocalDate;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.bbg.BloombergIdentifierProvider;
import com.opengamma.bbg.loader.BloombergHistoricalLoader;
import com.opengamma.component.tool.AbstractTool;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.util.generate.scripts.Scriptable;
import com.opengamma.util.time.DateUtils;

/**
 * Tool to load time-series information from Bloomberg.
 * <p>
 * This loads missing historical time-series data from Bloomberg.
 */
@Scriptable
public class BloombergTimeSeriesTool extends AbstractTool<ToolContext> {

  /** Command line option. */
  private static final String FIELDS_OPTION = "fields";
  /** Command line option. */
  private static final String DATAPROVIDERS_OPTION = "dataproviders";
  /** Command line option. */
  private static final String POSITION_MASTER_OPTION = "pm";
  /** Command line option. */
  private static final String RELOAD_OPTION = "reload";
  /** Command line option. */
  private static final String UPDATE_OPTION = "update";
  /** Command line option. */
  private static final String START_OPTION = "start";
  /** Command line option. */
  private static final String END_OPTION = "end";
  /** Command line option. */
  private static final String UNIQUE_ID_OPTION = "unique";
  /** Command line option. */
  private static final String CSV_OPTION = "csv";

  /**
   * Main method to run the tool.
   * 
   * <pre>
   * usage: java com.opengamma.bbg.loader.BloombergTimeSeriesTool [options]... [files]...
   *  -e,--end (yyyymmdd)                            End date
   *  -f,--fields (PX_LAST,VOLUME,LAST_VOLATILITY)   List of bloomberg fields
   *  -h,--help                                      Print this message
   *  -p,--dataproviders (CMPL,CMPT)                 List of data providers
   *  -pm                                            Load missing data from position master
   *  -r,--reload                                    Reload historical data
   *  -s,--start (yyyymmdd)                          Start date
   *  -u,--update                                    Update historical data in database
   *  -unique                                        BLOOMBERG UNIQUE IDS in files otherwise treat as BLOOMBERG_TICKERS
   *  -csv                                           Files are in CSV format (provider,field,id-scheme,id-value)
   * </pre>
   * 
   * @param args the command line arguments
   */
  public static void main(String[] args) {   // CSIGNORE
    boolean success = new BloombergTimeSeriesTool().initAndRun(args, ToolContext.class);
    System.exit(success ? 0 : 1);
  }

  //-------------------------------------------------------------------------
  @Override
  protected void doRun() throws Exception {
    BloombergHistoricalLoader loader = new BloombergHistoricalLoader(
        getToolContext().getHistoricalTimeSeriesMaster(),
        getToolContext().getHistoricalTimeSeriesProvider(),
        new BloombergIdentifierProvider(((BloombergToolContext) getToolContext()).getBloombergReferenceDataProvider()));
    configureOptions(getCommandLine(), loader);
    loader.setUpdateDb(true);
    loader.setReload(getCommandLine().hasOption("reload"));
    loader.run();
  }

  private static void configureOptions(CommandLine line, BloombergHistoricalLoader dataLoader) {
    //get files from command line if any
    String[] files = line.getArgs();
    dataLoader.setFiles(Arrays.asList(files));
    if (line.hasOption(UPDATE_OPTION)) {
      dataLoader.setUpdateDb(true);
    }
    if (line.hasOption(POSITION_MASTER_OPTION)) {
      dataLoader.setLoadPositionMaster(true);
    }
    if (line.hasOption(RELOAD_OPTION)) {
      dataLoader.setReload(true);
    }
    if (line.hasOption(CSV_OPTION)) {
      dataLoader.setCsv(true);
    }
    if (line.hasOption(DATAPROVIDERS_OPTION)) {
      if (dataLoader.isCsv()) {
        throw new OpenGammaRuntimeException("Cannot specify data providers with CSV input files, since providers are part of the CSV file");
      }
      
      String[] dataProviders = splitByComma(line.getOptionValue(DATAPROVIDERS_OPTION));
      dataLoader.setDataProviders(Arrays.asList(dataProviders));
    }
    if (line.hasOption(START_OPTION)) {
      String startOption = line.getOptionValue(START_OPTION);
      try {
        LocalDate startDate = DateUtils.toLocalDate(startOption);
        dataLoader.setStartDate(startDate);
      } catch (Exception ex) {
        throw new OpenGammaRuntimeException("Unable to parse start date " + startOption, ex);
      }
    }
    if (line.hasOption(END_OPTION)) {
      String endOption = line.getOptionValue(END_OPTION);
      try {
        LocalDate endDate = DateUtils.toLocalDate(endOption);
        dataLoader.setEndDate(endDate);
      } catch (Exception ex) {
        throw new OpenGammaRuntimeException("Unable to parse end date " + endOption, ex);
      }
    }
    String[] fields = null;
    if (line.hasOption(FIELDS_OPTION)) {
      if (dataLoader.isCsv()) {
        throw new OpenGammaRuntimeException("Cannot specify fields with CSV input files, since fields are part of the CSV file");
      }
      
      fields = splitByComma(line.getOptionValue(FIELDS_OPTION));
      dataLoader.setDataFields(Arrays.asList(fields));
    }
    
    if (line.hasOption(UNIQUE_ID_OPTION)) {
      dataLoader.setBbgUniqueId(true);
    }
    
    //check we have right options and input files
    if (files != null && files.length > 0 && !dataLoader.isCsv() && (fields == null || fields.length == 0)) {
      throw new OpenGammaRuntimeException("DataFields must be specified");
    }
  }

  private static String[] splitByComma(String word) {
    return word.split(",\\s*");
  }

  //-------------------------------------------------------------------------
  @Override
  protected Options createOptions(boolean mandatoryConfigResource) {
    Options options = super.createOptions(mandatoryConfigResource);
    options.addOption(createDataProviderOption());
    options.addOption(createFieldsOption());
    options.addOption(createReloadOption());
    options.addOption(createLoadPositionMasterOption());
    options.addOption(createUpdateOption());
    options.addOption(createStartOption());
    options.addOption(createEndOption());
    options.addOption(createUniqueOption());
    options.addOption(createCsvOption());
    return options;
  }

  private static Option createDataProviderOption() {
    OptionBuilder.withLongOpt(DATAPROVIDERS_OPTION);
    OptionBuilder.withDescription("List of data providers");
    OptionBuilder.hasArg();
    OptionBuilder.withArgName("CMPL,CMPT");
    return OptionBuilder.create("p");
  }

  private static Option createFieldsOption() {
    OptionBuilder.withLongOpt(FIELDS_OPTION);
    OptionBuilder.withDescription("List of bloomberg fields");
    OptionBuilder.hasArg();
    OptionBuilder.withArgName("PX_LAST,VOLUME,LAST_VOLATILITY");
    return OptionBuilder.create("f");
  }

  private static Option createReloadOption() {
    return new Option("r", RELOAD_OPTION, false, "Reload historical data");
  }

  private static Option createLoadPositionMasterOption() {
    return new Option(POSITION_MASTER_OPTION, false, "Load missing data from position master");
  }

  private static Option createUpdateOption() {
    return new Option("u", UPDATE_OPTION, false, "Update historical data in database");
  }

  private static Option createStartOption() {
    OptionBuilder.withLongOpt(START_OPTION);
    OptionBuilder.withDescription("Start date");
    OptionBuilder.hasArg();
    OptionBuilder.withArgName("yyyymmdd");
    return OptionBuilder.create("s");
  }

  private static Option createEndOption() {
    OptionBuilder.withLongOpt(END_OPTION);
    OptionBuilder.withDescription("End date");
    OptionBuilder.hasArg();
    OptionBuilder.withArgName("yyyymmdd");
    return OptionBuilder.create("e");
  }

  private static Option createUniqueOption() {
    return new Option(UNIQUE_ID_OPTION, false, "BLOOMBERG UNIQUE IDS in files otherwise treat as BLOOMBERG_TICKERS");
  }

  private static Option createCsvOption() {
    OptionBuilder.withLongOpt(CSV_OPTION);
    OptionBuilder.withDescription("CSV input files");
    return OptionBuilder.create("csv");
  }

}
