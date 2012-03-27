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
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.Lifecycle;
import org.springframework.core.io.Resource;

import au.com.bytecode.opencsv.CSVReader;

import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.engine.marketdata.MarketDataInjector;
import com.opengamma.id.ExternalId;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.util.tuple.Pair;

/**
 * An ultra-simple market data simulator, we load the initial values from a CSV file (with a header row)
 * and the format 
 * <identification-scheme>, <identifier-value>, <requirement-name>, <value>
 * typically, for last price, you'd use "Market_Value" @see MarketDataRequirementNames
 */
public class SimulatedMarketDataGenerator implements Runnable, Lifecycle {

  private static final Logger s_logger = LoggerFactory.getLogger(SimulatedMarketDataGenerator.class);
  private static final int NUM_FIELDS = 4;
  private static final double SCALING_FACTOR = 0.005; // i.e. 0.5% * 1SD
  private static final int MAX_MILLIS_BETWEEN_TICKS = 50;

  private final MarketDataInjector _marketDataInjector;
  private final Map<Pair<ExternalId, String>, Object> _initialValues = new HashMap<Pair<ExternalId, String>, Object>();
  private Thread _backgroundUpdateThread;
  private volatile boolean _running;

  public SimulatedMarketDataGenerator(final MarketDataInjector marketDataInjector, final Resource initialValuesFile, final SecurityMaster securities, final HistoricalTimeSeriesSource timeSeries) {
    _marketDataInjector = marketDataInjector;
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
        if (line.length > 0 && line[0].startsWith("#")) {
          continue;
        }
        if (line.length != NUM_FIELDS) {
          s_logger.error("Not enough fields in CSV on line " + lineNum);
        } else {
          String scheme = line[0];
          String identifier = line[1];
          String fieldName = line[2];
          String valueStr = line[3];
          Double value = Double.parseDouble(valueStr);
          ExternalId id = ExternalId.of(scheme, identifier);
          _initialValues.put(Pair.of(id, fieldName), value);
        }
      }
      // make an array of the keys so we can randomly choose ones to update.
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }    
  }
  
  @Override
  public void start() {
    _running = true;
    for (Map.Entry<Pair<ExternalId, String>, Object> initialValue : _initialValues.entrySet()) {
      _marketDataInjector.addValue(initialValue.getKey().getFirst(), initialValue.getKey().getSecond(), initialValue.getValue());
    }
    _backgroundUpdateThread = new Thread(this);
    _backgroundUpdateThread.start();
  }
  
  public void run() {
    @SuppressWarnings("unchecked")
    final Pair<ExternalId, String>[] identifiers = _initialValues.keySet().toArray(new Pair[0]);
    final Random random = new Random(); // no need for SecureRandom here..
    while (_running) {      
      Pair<ExternalId, String> idFieldPair = identifiers[random.nextInt(identifiers.length)];
      Object initialValue = _initialValues.get(idFieldPair);
      if (initialValue instanceof Double) {
        double value = wiggleValue(random, (Double) initialValue);
        _marketDataInjector.addValue(idFieldPair.getFirst(), idFieldPair.getSecond(), value);  
        _initialValues.put(idFieldPair, value);
      } else {
        // in case we support non-scalars at some point.
        _marketDataInjector.addValue(idFieldPair.getFirst(), idFieldPair.getSecond(), initialValue);
      }
      try {
        Thread.sleep(random.nextInt(MAX_MILLIS_BETWEEN_TICKS));
      } catch (InterruptedException e) {
        s_logger.error("Sleep interrupted, finishing");
        return;
      }
    }
  }

  private double wiggleValue(Random random, double value) {
    double result = value + (random.nextGaussian() * (value * SCALING_FACTOR));
    //s_logger.warn("wiggleValue = {}", result);
    return result;
  }

  /**
   * Stops the generator.
   */
  @Override
  public void stop() {
    _running = false;
  }

  @Override
  public boolean isRunning() {
    return _running;
  }

}
