/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.marketdata;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import javax.time.calendar.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.bytecode.opencsv.CSVReader;

import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundleWithDates;
import com.opengamma.id.ExternalIdWithDates;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoDocument;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;
import com.opengamma.master.historicaltimeseries.ManageableHistoricalTimeSeriesInfo;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.MapLocalDateDoubleTimeSeries;
import com.opengamma.util.tuple.Pair;

/**
 * An ultra-simple historical data simulator, we load the initial values from a CSV file (with a header row)
 * and the format 
 * <identification-scheme>, <identifier-value>, <datafield>, <value>
 */
public class SimulatedHistoricalDataGenerator {

  /**
   * OG Simulated data provider name
   */
  public static final String OG_DATA_PROVIDER = "OG_DATA_PROVIDER";

  /**
   * OG Simulated data source name
   */
  public static final String OG_DATA_SOURCE = "OG_DATA_SOURCE";

  private static final Logger s_logger = LoggerFactory.getLogger(SimulatedHistoricalDataGenerator.class);
  
  private final HistoricalTimeSeriesMaster _htsMaster;
  private Map<Pair<ExternalId, String>, Double> _finishValues = new HashMap<Pair<ExternalId, String>, Double>();

  private static final int NUM_FIELDS = 4;
  private static final double SCALING_FACTOR = 0.005; // i.e. 0.5% * 1SD
  private static final int TS_LENGTH = 2; // length of timeseries in years
  
  public SimulatedHistoricalDataGenerator(HistoricalTimeSeriesMaster htsMaster) {
    ArgumentChecker.notNull(htsMaster, "htsMaster");
    _htsMaster = htsMaster;
    readFinishValues(_finishValues);
  }

  private static void readFinishValues(Map<Pair<ExternalId, String>, Double> finishValues) {
    try {
      CSVReader reader = new CSVReader(new BufferedReader(new InputStreamReader(SimulatedHistoricalDataGenerator.class.getResourceAsStream("historical-data.csv"))));
      // Read header row
      @SuppressWarnings("unused")
      String[] headers = reader.readNext();
      String[] line;
      int lineNum = 0;
      while ((line = reader.readNext()) != null) {
        lineNum++;
        if ((line.length == 0) || line[0].startsWith("#")) {
          s_logger.debug("Empty line on {}", lineNum);
        } else if (line.length != NUM_FIELDS) {
          s_logger.error("Invalid number of fields ({}) in CSV on line {}", line.length, lineNum);
        } else {
          String scheme = line[0];
          String identifier = line[1];
          String fieldName = line[2];
          String valueStr = line[3];
          Double value = Double.parseDouble(valueStr);
          ExternalId id = ExternalId.of(scheme, identifier);
          finishValues.put(Pair.of(id, fieldName), value);
        }
      }
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }    
  }

  public static ManageableHistoricalTimeSeriesInfo getSimulatedTimeSeriesInfo(final String dataField, final ExternalId identifier) {
    final ManageableHistoricalTimeSeriesInfo info = new ManageableHistoricalTimeSeriesInfo();
    info.setName(dataField + " " + identifier);
    info.setDataField(dataField);
    info.setDataSource(OG_DATA_SOURCE);
    info.setDataProvider(OG_DATA_PROVIDER);
    info.setObservationTime("LONDON_CLOSE");
    ExternalIdWithDates id = ExternalIdWithDates.of(identifier, null, null);
    ExternalIdBundleWithDates bundle = ExternalIdBundleWithDates.of(id);
    info.setExternalIdBundle(bundle);
    return info;
  }

  public void run() {
    final Random random = new Random(); // noMarket need for SecureRandom here..
    StringBuilder buf = new StringBuilder("loading ").append(_finishValues.size()).append(" timeseries");
    for (Entry<Pair<ExternalId, String>, Double> entry : _finishValues.entrySet()) {
      final ExternalId identifier = entry.getKey().getFirst();
      final String dataField = entry.getKey().getSecond();
      final Double finishValue = entry.getValue();
      final ManageableHistoricalTimeSeriesInfo info = getSimulatedTimeSeriesInfo(dataField, identifier);
      buf.append("\t").append(identifier).append(" ").append(dataField).append("\n");
      HistoricalTimeSeriesInfoDocument addedDoc = _htsMaster.add(new HistoricalTimeSeriesInfoDocument(info));
      LocalDateDoubleTimeSeries timeSeries = getHistoricalDataPoints(random, finishValue, TS_LENGTH);
      _htsMaster.updateTimeSeriesDataPoints(addedDoc.getInfo().getTimeSeriesObjectId(), timeSeries);
    }
    s_logger.info(buf.toString());
  }

  public static LocalDateDoubleTimeSeries getHistoricalDataPoints(final Random random, final Double finishValue, final int tsLength) {
    final MapLocalDateDoubleTimeSeries result = new MapLocalDateDoubleTimeSeries();
    LocalDate now = LocalDate.now();
    final LocalDate stopDate = DateUtils.previousWeekDay(now.minusYears(tsLength));
    double currentValue = finishValue;
    do {
      currentValue = wiggleValue(random, currentValue);
      result.putDataPoint(now, currentValue);
      now = DateUtils.previousWeekDay(now);
    } while (now.isAfter(stopDate));
    return result;
  }

  private static double wiggleValue(final Random random, final double value) {
    double result = value + (random.nextGaussian() * (value * SCALING_FACTOR));
    //s_logger.warn("wiggleValue = {}", result);
    return result;
  }

}
