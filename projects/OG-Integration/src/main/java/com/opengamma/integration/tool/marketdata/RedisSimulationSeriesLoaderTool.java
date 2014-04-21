/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.marketdata;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Map;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.threeten.bp.LocalDate;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.component.tool.AbstractTool;
import com.opengamma.core.historicaltimeseries.impl.RedisSimulationSeriesSource;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
import com.opengamma.integration.copier.sheet.SheetFormat;
import com.opengamma.integration.copier.timeseries.reader.SingleSheetMultiTimeSeriesReader;
import com.opengamma.integration.copier.timeseries.reader.TimeSeriesReader;
import com.opengamma.integration.copier.timeseries.writer.TimeSeriesWriter;
import com.opengamma.scripts.Scriptable;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleEntryIterator;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;

/**
 * The timeseries loader tool
 */
@Scriptable
public class RedisSimulationSeriesLoaderTool extends AbstractTool<ToolContext> {

  /** File name option flag */
  public static final String FILE_NAME_OPT = "f";
  /** Time series data source option flag*/
  public static final String TIME_SERIES_DATASOURCE_OPT = "s";
  /** Time series data provider option flag*/
  public static final String TIME_SERIES_DATAPROVIDER_OPT = "p";
  /** Time series data field option flag*/
  public static final String TIME_SERIES_DATAFIELD_OPT = "d";
  /** Time series observation time option flag*/
  public static final String TIME_SERIES_OBSERVATIONTIME_OPT = "o";
  /** Time series ID scheme option flag*/
  public static final String TIME_SERIES_IDSCHEME_OPT = "i";
  /** Time series date format option flag*/
  public static final String TIME_SERIES_DATEFORMAT_OPT = "t";
  /** Write option flag */
  public static final String WRITE_OPT = "w";

  //-------------------------------------------------------------------------
  /**
   * Main method to run the tool.
   * 
   * @param args  the standard tool arguments, not null
   */
  public static void main(String[] args) { //CSIGNORE
    new RedisSimulationSeriesLoaderTool().invokeAndTerminate(args);
  }

  //-------------------------------------------------------------------------
  /**
   * Loads the test portfolio into the position master.
   */
  @Override 
  protected void doRun() {
    if (!(getToolContext().getHistoricalTimeSeriesSource() instanceof RedisSimulationSeriesSource)) {
      throw new OpenGammaRuntimeException("HistoricalTimeSeriesSource from conrtext is not a RedisSimulationSeriesSource, got " +
          getToolContext().getHistoricalTimeSeriesSource() +
          ": note this tool must be run with a toolcontext config file not via -c http://localhost");
    }
    final RedisSimulationSeriesSource source = (RedisSimulationSeriesSource) getToolContext().getHistoricalTimeSeriesSource();

    String fileName = getCommandLine().getOptionValue(FILE_NAME_OPT);
    SheetFormat sheetFormat = SheetFormat.of(fileName);

    // most of these fields are dropped in redis - used here to allow us to use existing machinery
    String dataSource = getCommandLine().getOptionValue(TIME_SERIES_DATASOURCE_OPT);
    String dataProvider = getCommandLine().getOptionValue(TIME_SERIES_DATAPROVIDER_OPT);
    String dataField = getCommandLine().getOptionValue(TIME_SERIES_DATAFIELD_OPT);
    String observationTime = getCommandLine().getOptionValue(TIME_SERIES_OBSERVATIONTIME_OPT);
    String idScheme = getCommandLine().getOptionValue(TIME_SERIES_IDSCHEME_OPT);
    String dateFormat = getCommandLine().getOptionValue(TIME_SERIES_DATEFORMAT_OPT);
    // boolean write = getCommandLine().hasOption(WRITE_OPT);
    try {
      InputStream portfolioFileStream = new BufferedInputStream(new FileInputStream(fileName));

      TimeSeriesReader timeSeriesReader = new SingleSheetMultiTimeSeriesReader(sheetFormat,
                                                                               portfolioFileStream,
                                                                               dataSource,
                                                                               dataProvider,
                                                                               dataField,
                                                                               observationTime,
                                                                               idScheme,
                                                                               dateFormat);

      timeSeriesReader.writeTo(new TimeSeriesWriter() {
        @Override
        public LocalDateDoubleTimeSeries writeDataPoints(ExternalId htsId,
                                                         String dataSource,
                                                         String dataProvider,
                                                         String dataField,
                                                         String observationTime,
                                                         LocalDateDoubleTimeSeries series) {
          final LocalDateDoubleEntryIterator iterator = series.iterator();
          while (iterator.hasNext()) {
            final Map.Entry<LocalDate, Double> entry = iterator.next();
            final UniqueId id = UniqueId.of(htsId.getScheme().getName(), htsId.getValue());
            source.updateTimeSeriesPoint(id, source.getCurrentSimulationExecutionDate(), entry.getKey(), entry.getValue());
          }
          return series;
        }

        @Override
        public void flush() {
          return;
        }
      });
    } catch (FileNotFoundException e) {
      throw new OpenGammaRuntimeException("Could not find timeseries file", e);
    }
  }

  @Override
  protected  Options createOptions(boolean contextProvided) {
    
    Options options = super.createOptions(contextProvided);
    
    Option filenameOption = new Option(
        FILE_NAME_OPT, "filename", true, "The path to the file containing data to import (CSV or ZIP)");
    filenameOption.setRequired(true);
    options.addOption(filenameOption);
    
    Option timeSeriesDataSourceOption = new Option(
        TIME_SERIES_DATASOURCE_OPT, "source", true, "The name of the time series data source");
    options.addOption(timeSeriesDataSourceOption);
    
    Option timeSeriesDataProviderOption = new Option(
        TIME_SERIES_DATAPROVIDER_OPT, "provider", true, "The name of the time series data provider");
    options.addOption(timeSeriesDataProviderOption);
    
    Option timeSeriesDataFieldOption = new Option(
        TIME_SERIES_DATAFIELD_OPT, "field", true, "The name of the time series data field");
    options.addOption(timeSeriesDataFieldOption);
    
    Option timeSeriesObservationTimeOption = new Option(
        TIME_SERIES_OBSERVATIONTIME_OPT, "time", true, "The time series observation time");
    options.addOption(timeSeriesObservationTimeOption);
    
    Option timeSeriesIdSchemeOption = new Option(
        TIME_SERIES_IDSCHEME_OPT, "scheme", true, "The time series ID scheme (e.g. RIC)");
    options.addOption(timeSeriesIdSchemeOption);
    
    Option timeSeriesDateFormatOption = new Option(
        TIME_SERIES_DATEFORMAT_OPT, "date", true, "The JodaTime date format (e.g. yyyyMMdd)");
    options.addOption(timeSeriesDateFormatOption);
    
    Option writeOption = new Option(
        WRITE_OPT, "write", false, 
        "Actually persists the time series to the database if specified, otherwise pretty-prints without persisting");
    options.addOption(writeOption);
        
    return options;
  }

}
