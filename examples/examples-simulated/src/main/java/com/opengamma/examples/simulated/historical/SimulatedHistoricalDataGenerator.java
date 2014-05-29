/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.simulated.historical;

import java.util.Map.Entry;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.LocalDate;

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
 * Generates random historical time series data.
 */
public class SimulatedHistoricalDataGenerator extends SimulatedHistoricalData {

  private static final Logger s_logger = LoggerFactory.getLogger(SimulatedHistoricalDataGenerator.class);

  /** Master for writing the generated time series data. */
  private final HistoricalTimeSeriesMaster _htsMaster;

  /** Default length of time series to load in months (2.5 years). */
  private static final int TS_LENGTH = 30;

  /** Length of time series to load in months. */
  private final int _timeSeriesLengthMonths;

  /**
   * @param timeSeriesMaster master for writing the generated time series data
   */
  public SimulatedHistoricalDataGenerator(HistoricalTimeSeriesMaster timeSeriesMaster) {
    this(timeSeriesMaster, TS_LENGTH);
  }

  /**
   * @param timeSeriesMaster master for writing the generated time series data
   * @param timeSeriesLengthMonths length in months of the time series
   */
  public SimulatedHistoricalDataGenerator(HistoricalTimeSeriesMaster timeSeriesMaster, int timeSeriesLengthMonths) {
    _htsMaster = ArgumentChecker.notNull(timeSeriesMaster, "timeSeriesMaster");
    _timeSeriesLengthMonths = ArgumentChecker.notNegativeOrZero(timeSeriesLengthMonths, "timeSeriesLengthMonths");
  }

  public void run() {
    Random random = new Random(); // noMarket need for SecureRandom here..
    StringBuilder buf = new StringBuilder("loading ").append(getFinishValues().size()).append(" timeseries");
    for (Entry<Pair<ExternalId, String>, Double> entry : getFinishValues().entrySet()) {
      ExternalId identifier = entry.getKey().getFirst();
      String dataField = entry.getKey().getSecond();
      Double finishValue = entry.getValue();
      ManageableHistoricalTimeSeriesInfo info = getSimulatedTimeSeriesInfo(dataField, identifier);
      buf.append("\t").append(identifier).append(" ").append(dataField).append("\n");
      HistoricalTimeSeriesInfoDocument addedDoc = _htsMaster.add(new HistoricalTimeSeriesInfoDocument(info));
      LocalDateDoubleTimeSeries timeSeries = getHistoricalDataPoints(random, finishValue, _timeSeriesLengthMonths);
      _htsMaster.updateTimeSeriesDataPoints(addedDoc.getInfo().getTimeSeriesObjectId(), timeSeries);
    }
    s_logger.info(buf.toString());
  }

  private static ManageableHistoricalTimeSeriesInfo getSimulatedTimeSeriesInfo(String dataField, ExternalId identifier) {
    ManageableHistoricalTimeSeriesInfo info = new ManageableHistoricalTimeSeriesInfo();
    info.setName(dataField + " " + identifier);
    info.setDataField(dataField);
    info.setDataSource(OG_DATA_SOURCE);
    info.setDataProvider(OG_DATA_PROVIDER);
    info.setObservationTime(HistoricalTimeSeriesConstants.LONDON_CLOSE);
    ExternalIdWithDates id = ExternalIdWithDates.of(identifier, null, null);
    ExternalIdBundleWithDates bundle = ExternalIdBundleWithDates.of(id);
    info.setExternalIdBundle(bundle);
    return info;
  }

  private static LocalDateDoubleTimeSeries getHistoricalDataPoints(Random random, Double finishValue, int tsLength) {
    LocalDateDoubleTimeSeriesBuilder bld = ImmutableLocalDateDoubleTimeSeries.builder();
    LocalDate now = LocalDate.now();
    LocalDate stopDate = DateUtils.previousWeekDay(now.minusMonths(tsLength));
    double currentValue = finishValue;
    do {
      currentValue = wiggleValue(random, currentValue, finishValue);
      bld.put(now, currentValue);
      now = DateUtils.previousWeekDay(now);
    } while (now.isAfter(stopDate));
    return bld.build();
  }

}

