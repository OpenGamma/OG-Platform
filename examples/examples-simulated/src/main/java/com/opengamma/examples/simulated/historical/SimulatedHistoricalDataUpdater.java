/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.simulated.historical;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.LocalDate;

import com.opengamma.component.tool.AbstractTool;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.id.ExternalId;
import com.opengamma.integration.tool.GUIFeedback;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesGetFilter;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoDocument;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoSearchRequest;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoSearchResult;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;
import com.opengamma.master.historicaltimeseries.ManageableHistoricalTimeSeries;
import com.opengamma.scripts.Scriptable;
import com.opengamma.timeseries.date.localdate.ImmutableLocalDateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.Pair;

/**
 * Updates the time series database with simulated points for any working days since it was last populated.
 * <p>
 * It is designed to run against a running server.
 */
@Scriptable
public class SimulatedHistoricalDataUpdater extends AbstractTool<ToolContext> {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(SimulatedHistoricalDataUpdater.class);

  /**
   * The properties file.
   */
  public static final String TOOLCONTEXT_EXAMPLE_PROPERTIES = "classpath:/toolcontext-remote/toolcontext-examplessimulated.properties";

  private final SimulatedHistoricalData _data;
  private final GUIFeedback _feedback;

  //-------------------------------------------------------------------------
  /**
   * Main method to run the tool.
   * 
   * @param args  the standard tool arguments, not null
   */
  public static void main(final String[] args) { // CSIGNORE
    s_logger.info("Updating example database");
    try (GUIFeedback feedback = new GUIFeedback("Updating simulated historical time series database")) {
      if (!new SimulatedHistoricalDataUpdater(feedback).initAndRun(args, TOOLCONTEXT_EXAMPLE_PROPERTIES, null, ToolContext.class)) {
        feedback.done("Could not update the time-series database - check that the server has been started");
      } else {
        System.exit(0);
      }
    } catch (final Exception ex) {
      GUIFeedback.shout(ex.getClass().getSimpleName() + " - " + ex.getMessage());
      s_logger.error("Caught exception", ex);
      ex.printStackTrace();
    }
    System.exit(1);
  }

  //-------------------------------------------------------------------------
  public SimulatedHistoricalDataUpdater(final GUIFeedback feedback) {
    _data = new SimulatedHistoricalData();
    _feedback = feedback;
  }

  @Override
  protected void doRun() {
    try {
      final HistoricalTimeSeriesMaster htsMaster = getToolContext().getHistoricalTimeSeriesMaster();
      final HistoricalTimeSeriesGetFilter filter = new HistoricalTimeSeriesGetFilter();
      filter.setMaxPoints(-1);
      final LocalDate now = LocalDate.now();
      final Random rnd = new Random();
      int pointsAdded = 0;
      int timeSeriesUpdated = 0;
      _feedback.workRequired(_data.getFinishValues().size());
      for (Map.Entry<Pair<ExternalId, String>, Double> centreValue : _data.getFinishValues().entrySet()) {
        s_logger.debug("Updating TS points for {}/{}", centreValue.getKey().getFirst(), centreValue.getKey().getSecond());
        final HistoricalTimeSeriesInfoSearchRequest searchRequest = new HistoricalTimeSeriesInfoSearchRequest();
        searchRequest.addExternalId(centreValue.getKey().getFirst());
        searchRequest.setDataField(centreValue.getKey().getSecond());
        searchRequest.setDataProvider(SimulatedHistoricalData.OG_DATA_PROVIDER);
        searchRequest.setDataSource(SimulatedHistoricalData.OG_DATA_SOURCE);
        final HistoricalTimeSeriesInfoSearchResult searchResult = htsMaster.search(searchRequest);
        if (searchResult.getDocuments().isEmpty()) {
          s_logger.warn("No time series for {}/{}", centreValue.getKey().getFirst(), centreValue.getKey().getSecond());
        } else {
          for (HistoricalTimeSeriesInfoDocument infoDoc : searchResult.getDocuments()) {
            s_logger.debug("Updating {}", infoDoc.getUniqueId());
            final ManageableHistoricalTimeSeries hts = htsMaster.getTimeSeries(infoDoc.getUniqueId(), filter);
            final LocalDateDoubleTimeSeries ldts = hts.getTimeSeries();
            LocalDate pointDate = ldts.getLatestTime();
            if (pointDate.isBefore(now)) {
              final double centre = centreValue.getValue();
              double pointValue = ldts.getLatestValueFast();
              pointDate = DateUtils.nextWeekDay(pointDate);
              List<LocalDate> dates = new ArrayList<LocalDate>();
              List<Double> values = new ArrayList<Double>();
              while (pointDate.isBefore(now)) {
                pointValue = SimulatedHistoricalData.wiggleValue(rnd, pointValue, centre);
                dates.add(pointDate);
                values.add(pointValue);
                pointDate = DateUtils.nextWeekDay(pointDate);
              }
              if (!dates.isEmpty()) {
                s_logger.debug("Adding {} new points to {}", dates.size(), infoDoc.getUniqueId());
                htsMaster.updateTimeSeriesDataPoints(infoDoc, ImmutableLocalDateDoubleTimeSeries.of(dates, values));
                pointsAdded += dates.size();
                timeSeriesUpdated++;
                if ((timeSeriesUpdated % 100) == 0) {
                  GUIFeedback.say("Updated " + timeSeriesUpdated + " of " + _data.getFinishValues().size() + " time series");
                }
              }
            }
          }
          _feedback.workCompleted(1);
        }
      }
      final String message = "Added " + pointsAdded + " points to " + timeSeriesUpdated + " time series";
      s_logger.info("{}", message);
      _feedback.done(message);
    } catch (Exception e) {
      _feedback.done("An error occurred updating the time series database");
      throw e;
    }
  }
}
