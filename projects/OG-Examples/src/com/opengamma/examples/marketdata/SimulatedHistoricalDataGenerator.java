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
import org.springframework.core.io.Resource;

import au.com.bytecode.opencsv.CSVReader;

import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundleWithDates;
import com.opengamma.id.IdentifierWithDates;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoDocument;
import com.opengamma.master.historicaltimeseries.ManageableHistoricalTimeSeriesInfo;
import com.opengamma.masterdb.historicaltimeseries.DbHistoricalTimeSeriesMaster;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.time.DateUtil;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.MapLocalDateDoubleTimeSeries;
import com.opengamma.util.tuple.Pair;

/**
 * An ultra-simple historical data simulator, we load the initial values from a CSV file (with a header row)
 * and the format 
 * <identification-scheme>, <identifier-value>, <datafield>, <value>
 */
public class SimulatedHistoricalDataGenerator {
  private static final String OG_DATA_PROVIDER = "OG_DATA_PROVIDER";

  private static final String OG_DATA_SOURCE = "OG_DATA_SOURCE";

  private static final Logger s_logger = LoggerFactory.getLogger(SimulatedHistoricalDataGenerator.class);
  
  private final DbHistoricalTimeSeriesMaster _htsMaster;
  private Map<Pair<Identifier, String>, Double> _initialValues = new HashMap<Pair<Identifier, String>, Double>();

  private static final int NUM_FIELDS = 4;
  private static final double SCALING_FACTOR = 0.005; // i.e. 0.5% * 1SD
  private static final int TS_LENGTH = 2; // length of timeseries in years
  
  public SimulatedHistoricalDataGenerator(DbHistoricalTimeSeriesMaster htsMaster, Resource initialValuesFile) {
    ArgumentChecker.notNull(htsMaster, "htsMaster");
    ArgumentChecker.notNull(initialValuesFile, "initialValuesFile");
    _htsMaster = htsMaster;
    readInitialValues(initialValuesFile);
  }
  
  public void readInitialValues(Resource initialValuesFile) {
    try {
      CSVReader reader = new CSVReader(new BufferedReader(new InputStreamReader(initialValuesFile.getInputStream())));
      // Read header row
      @SuppressWarnings("unused")
      String[] headers = reader.readNext();
      String[] line;
      int lineNum = 1;
      while ((line = reader.readNext()) != null) {
        lineNum++;
        if (line.length != NUM_FIELDS) {
          s_logger.error("Not enough fields in CSV on line " + lineNum);
        } else {
          String scheme = line[0];
          String identifier = line[1];
          String fieldName = line[2];
          String valueStr = line[3];
          Double value = Double.parseDouble(valueStr);
          Identifier id = Identifier.of(scheme, identifier);
          _initialValues.put(Pair.of(id, fieldName), value);
        }
      }
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }    
  }
  
  public void run() {
    Random random = new Random(); // noMarket need for SecureRandom here..
    for (Entry<Pair<Identifier, String>, Double> entry : _initialValues.entrySet()) {
      Identifier identifier = entry.getKey().getFirst();
      String dataField = entry.getKey().getSecond();
      Double startValue = entry.getValue();
      ManageableHistoricalTimeSeriesInfo info = new ManageableHistoricalTimeSeriesInfo();
      info.setName(dataField + " " + identifier);
      info.setDataField(dataField);
      info.setDataSource(OG_DATA_SOURCE);
      info.setDataProvider(OG_DATA_PROVIDER);
      info.setObservationTime("LONDON_CLOSE");
      IdentifierWithDates id = IdentifierWithDates.of(identifier, null, null);
      IdentifierBundleWithDates bundle = IdentifierBundleWithDates.of(id);
      info.setIdentifiers(bundle);
      s_logger.info("loading timeseries for {} {}/{}/{}", new Object[]{identifier, dataField, OG_DATA_SOURCE, OG_DATA_PROVIDER});
      HistoricalTimeSeriesInfoDocument addedDoc = _htsMaster.add(new HistoricalTimeSeriesInfoDocument(info));
      LocalDateDoubleTimeSeries timeSeries = getHistoricalDataPoints(random, startValue, TS_LENGTH);
      _htsMaster.updateTimeSeriesDataPoints(addedDoc.getInfo().getTimeSeriesObjectId(), timeSeries);
    }
  }
  
  private LocalDateDoubleTimeSeries getHistoricalDataPoints(Random random, Double startValue, int tsLength) {
    MapLocalDateDoubleTimeSeries result = new MapLocalDateDoubleTimeSeries();
    LocalDate endDate = DateUtil.previousWeekDay();
    LocalDate startDate = DateUtil.previousWeekDay(endDate.minusYears(tsLength));
    result.putDataPoint(startDate, startValue);
    LocalDate nextDate = DateUtil.nextWeekDay(startDate);
    while (nextDate.isBefore(endDate)) {
      result.putDataPoint(nextDate, wiggleValue(random, startValue));
      nextDate = DateUtil.nextWeekDay(nextDate);
    }
    return result;
  }

  private double wiggleValue(Random random, double value) {
    double result = value + (random.nextGaussian() * (value * SCALING_FACTOR));
    //s_logger.warn("wiggleValue = {}", result);
    return result;
  }
}
