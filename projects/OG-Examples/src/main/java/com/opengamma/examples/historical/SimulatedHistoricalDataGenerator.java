/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.historical;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.LocalDate;

import au.com.bytecode.opencsv.CSVReader;

import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesConstants;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundleWithDates;
import com.opengamma.id.ExternalIdWithDates;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoDocument;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;
import com.opengamma.master.historicaltimeseries.ManageableHistoricalTimeSeriesInfo;
import com.opengamma.timeseries.date.localdate.ImmutableLocalDateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeriesBuilder;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.time.DateUtils;
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
  private final Map<Pair<ExternalId, String>, Double> _finishValues = new HashMap<Pair<ExternalId, String>, Double>();

  private static final int NUM_FIELDS = 4;
  private static final double SCALING_FACTOR = 0.005; // i.e. 0.5% * 1SD
  private static final int TS_LENGTH = 30; // length of timeseries in months (2.5 years)

  public SimulatedHistoricalDataGenerator(final HistoricalTimeSeriesMaster htsMaster) {
    ArgumentChecker.notNull(htsMaster, "htsMaster");
    _htsMaster = htsMaster;
    readFinishValues(_finishValues);
  }

  private static void readFinishValues(final Map<Pair<ExternalId, String>, Double> finishValues) {
    CSVReader reader = null;
    try {
      reader = new CSVReader(new BufferedReader(new InputStreamReader(SimulatedHistoricalDataGenerator.class.getResourceAsStream("historical-data.csv"))));
      // Read header row
      @SuppressWarnings("unused")
      final
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
          final String scheme = line[0];
          final String identifier = line[1];
          final String fieldName = line[2];
          final String valueStr = line[3];
          final Double value = Double.parseDouble(valueStr);
          final ExternalId id = ExternalId.of(scheme, identifier);
          finishValues.put(Pair.of(id, fieldName), value);
        }
      }
    } catch (final FileNotFoundException e) {
      e.printStackTrace();
    } catch (final IOException e) {
      e.printStackTrace();
    } finally {
      IOUtils.closeQuietly(reader);
    }
  }

  public static ManageableHistoricalTimeSeriesInfo getSimulatedTimeSeriesInfo(final String dataField, final ExternalId identifier) {
    final ManageableHistoricalTimeSeriesInfo info = new ManageableHistoricalTimeSeriesInfo();
    info.setName(dataField + " " + identifier);
    info.setDataField(dataField);
    info.setDataSource(OG_DATA_SOURCE);
    info.setDataProvider(OG_DATA_PROVIDER);
    info.setObservationTime(HistoricalTimeSeriesConstants.LONDON_CLOSE);
    final ExternalIdWithDates id = ExternalIdWithDates.of(identifier, null, null);
    final ExternalIdBundleWithDates bundle = ExternalIdBundleWithDates.of(id);
    info.setExternalIdBundle(bundle);
    return info;
  }

  public void run() {
    final Random random = new Random(); // noMarket need for SecureRandom here..
    final StringBuilder buf = new StringBuilder("loading ").append(_finishValues.size()).append(" timeseries");
    for (final Entry<Pair<ExternalId, String>, Double> entry : _finishValues.entrySet()) {
      final ExternalId identifier = entry.getKey().getFirst();
      final String dataField = entry.getKey().getSecond();
      final Double finishValue = entry.getValue();
      final ManageableHistoricalTimeSeriesInfo info = getSimulatedTimeSeriesInfo(dataField, identifier);
      buf.append("\t").append(identifier).append(" ").append(dataField).append("\n");
      final HistoricalTimeSeriesInfoDocument addedDoc = _htsMaster.add(new HistoricalTimeSeriesInfoDocument(info));
      final LocalDateDoubleTimeSeries timeSeries = getHistoricalDataPoints(random, finishValue, TS_LENGTH);
      _htsMaster.updateTimeSeriesDataPoints(addedDoc.getInfo().getTimeSeriesObjectId(), timeSeries);
    }
    s_logger.info(buf.toString());
  }

  public static LocalDateDoubleTimeSeries getHistoricalDataPoints(final Random random, final Double finishValue, final int tsLength) {
    final LocalDateDoubleTimeSeriesBuilder bld = ImmutableLocalDateDoubleTimeSeries.builder();
    LocalDate now = LocalDate.now();
    final LocalDate stopDate = DateUtils.previousWeekDay(now.minusMonths(tsLength));
    double currentValue = finishValue;
    do {
      currentValue = wiggleValue(random, currentValue, finishValue);
      bld.put(now, currentValue);
      now = DateUtils.previousWeekDay(now);
    } while (now.isAfter(stopDate));
    return bld.build();
  }

  private static double wiggleValue(final Random random, final double value, final double centre) {
    return (9 * value + centre) / 10 + (random.nextGaussian() * (value * SCALING_FACTOR));
  }

}
