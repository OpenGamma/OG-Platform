/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.integration.copier.timeseries.reader;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.format.DateTimeFormatterBuilder;

import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalScheme;
import com.opengamma.integration.copier.sheet.SheetFormat;
import com.opengamma.integration.copier.sheet.reader.SheetReader;
import com.opengamma.integration.copier.timeseries.TimeSeriesLoader;
import com.opengamma.integration.copier.timeseries.writer.TimeSeriesWriter;
import com.opengamma.timeseries.date.localdate.ImmutableLocalDateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeriesBuilder;

/**
 * Reads data points, possibly from multiple time series, from an single sheet
 */
public class SingleSheetMultiTimeSeriesReader implements TimeSeriesReader {

  private static final Logger s_logger = LoggerFactory.getLogger(TimeSeriesLoader.class);
//  private static final String ID_SCHEME = "TIME_SERIES_LOADER";
  private static final int BUFFER_SIZE = 32;

  // CSOFF
  /** Standard date-time formatter for the input */
  protected DateTimeFormatter CSV_DATE_FORMATTER;

  private static final String ID = "id";
  private static final String DATE = "date";
  private static final String VALUE = "value";
//  public static final String DATA_SOURCE = "data source";
//  public static final String DATA_PROVIDER = "data provider";
//  public static final String DATA_FIELD = "data field";
//  public static final String OBSERVATION_TIME = "observation time";
  // CSON


  private SheetReader _sheet;         // The spreadsheet from which to import

  private String _dataSource, _dataProvider, _dataField, _observationTime, _idScheme;

  public SingleSheetMultiTimeSeriesReader(SheetReader sheet,
                                          String dataSource,
                                          String dataProvider,
                                          String dataField,
                                          String observationTime,
                                          String idScheme,
                                          String dateFormat) {
    _sheet = sheet;

    _dataSource = dataSource;
    _dataProvider = dataProvider;
    _dataField = dataField;
    _observationTime = observationTime;
    _idScheme = idScheme;

    DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
    builder.appendPattern(dateFormat == null ? "yyyyMMdd" : dateFormat);
    CSV_DATE_FORMATTER = builder.toFormatter();
  }

  public SingleSheetMultiTimeSeriesReader(SheetFormat sheetFormat,
                                          InputStream portfolioFileStream,
                                          String dataSource,
                                          String dataProvider,
                                          String dataField,
                                          String observationTime,
                                          String idScheme,
                                          String dateFormat) {
    _sheet = SheetReader.newSheetReader(sheetFormat, portfolioFileStream);

    _dataSource = dataSource;
    _dataProvider = dataProvider;
    _dataField = dataField;
    _observationTime = observationTime;
    _idScheme = idScheme;

    DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
    builder.appendPattern(dateFormat == null ? "yyyyMMdd" : dateFormat);
    CSV_DATE_FORMATTER = builder.toFormatter();    
  }

  @Override
  public void writeTo(TimeSeriesWriter timeSeriesWriter) {

    Map<String, String> rawRow = null;
    do {
      Map<String, LocalDateDoubleTimeSeriesBuilder> tsData = new HashMap<String, LocalDateDoubleTimeSeriesBuilder>();
      int count = 0;

      // Get the next set of rows from the sheet up to the memory buffer limit
      while ((count < BUFFER_SIZE) && ((rawRow = _sheet.loadNextRow()) != null)) { // CSIGNORE
        try {
          String ric = getWithException(rawRow, ID);
          if (!tsData.containsKey(ric)) {
            tsData.put(ric, ImmutableLocalDateDoubleTimeSeries.builder());
          }
          LocalDate date = getDateWithException(rawRow, DATE);
          double value = Double.parseDouble(getWithException(rawRow, VALUE));
          tsData.get(ric).put(date, value);
        } catch (Throwable e) {
          s_logger.warn("Could not parse time series row " + rawRow + "; " + e.toString());
        }
        count++;
      }

      // Write out the gathered time series points across all time series keys
      for (String key : tsData.keySet()) {
        if (tsData.get(key).size() > 0) {
          s_logger.info("Writing " + tsData.get(key).size() + " data points to time series " + key);
          timeSeriesWriter.writeDataPoints(
              ExternalId.of(ExternalScheme.of(_idScheme), key), 
              _dataSource,
              _dataProvider,
              _dataField,
              _observationTime,
              tsData.get(key).build());
        }
      }

    } while (rawRow != null);
    
  }

  protected String getWithException(Map<String, String> fieldValueMap, String fieldName) {
    String result = fieldValueMap.get(fieldName);
    if (result == null) {
      System.err.println(fieldValueMap);
      throw new IllegalArgumentException("Could not find field '" + fieldName + "'");
    }
    return result;
  }

  protected LocalDate getDateWithException(Map<String, String> fieldValueMap, String fieldName) {
    return LocalDate.parse(getWithException(fieldValueMap, fieldName), CSV_DATE_FORMATTER);
  }

}
