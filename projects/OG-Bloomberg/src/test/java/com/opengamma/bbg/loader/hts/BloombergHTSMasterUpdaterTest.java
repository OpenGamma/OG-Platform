/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.loader.hts;

import static com.opengamma.bbg.BloombergConstants.BLOOMBERG_DATA_SOURCE_NAME;
import static com.opengamma.util.time.DateUtils.nextWeekDay;
import static com.opengamma.util.time.DateUtils.previousWeekDay;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.util.List;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoDocument;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.tuple.Pair;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class BloombergHTSMasterUpdaterTest extends AbstractBloombergHTSTest {

  public void update() throws Exception {
    HistoricalTimeSeriesMaster htsMaster = getHtsMaster();
    BloombergHTSMasterUpdater htsMasterUpdater = getHtsMasterUpdater();
    List<Pair<HistoricalTimeSeriesInfoDocument, HistoricalTimeSeries>> previousSeriesDocs = addTimeSeries();
    htsMasterUpdater.run();
    for (Pair<HistoricalTimeSeriesInfoDocument, HistoricalTimeSeries> previous : previousSeriesDocs) {
      HistoricalTimeSeriesInfoDocument currentDoc = htsMaster.get(previous.getFirst().getObjectId(), VersionCorrection.LATEST);
      assertEquals(previous.getFirst().getUniqueId(), currentDoc.getUniqueId());  // document not changed
      HistoricalTimeSeries currentSeries = htsMaster.getTimeSeries(previous.getSecond().getUniqueId(), VersionCorrection.LATEST);
      assertNotNull(currentSeries);
      //will update only for Bloomberg dataSource
      if (previous.getFirst().getInfo().getDataSource().equals(BLOOMBERG_DATA_SOURCE_NAME)) {
        assertEquals(previousWeekDay(), currentSeries.getTimeSeries().getLatestTime());
        LocalDateDoubleTimeSeries previousSeries = previous.getSecond().getTimeSeries();
        final LocalDate previousStart = previousSeries.getEarliestTime();
        final LocalDate previousEnd = previousSeries.getLatestTime();
        assertEquals(previousSeries, currentSeries.getTimeSeries().subSeries(previousStart, true, previousEnd, true));
      } else {
        assertEquals(previous.getSecond().getTimeSeries(), currentSeries.getTimeSeries());
      }
    }
  }

  public void updateGivenDates() throws Exception {
    List<Pair<HistoricalTimeSeriesInfoDocument, HistoricalTimeSeries>> previousSeriesDocs = addTimeSeries();
    final LocalDate previousSeriesEnd = previousWeekDay().minusWeeks(1);
    final LocalDate startDate = nextWeekDay(previousSeriesEnd);
    final LocalDate endDate = nextWeekDay(startDate);
    
    HistoricalTimeSeriesMaster htsMaster = getHtsMaster();
    BloombergHTSMasterUpdater htsMasterUpdater = getHtsMasterUpdater();
    
    htsMasterUpdater.setStartDate(startDate);
    htsMasterUpdater.setEndDate(endDate);
    htsMasterUpdater.run();
    for (Pair<HistoricalTimeSeriesInfoDocument, HistoricalTimeSeries> previous : previousSeriesDocs) {
      HistoricalTimeSeries currentSeries = htsMaster.getTimeSeries(previous.getSecond().getUniqueId(), VersionCorrection.LATEST);
      assertNotNull(currentSeries);
      //will update only for Bloomberg dataSource
      if (previous.getFirst().getInfo().getDataSource().equals(BLOOMBERG_DATA_SOURCE_NAME)) {
        assertEquals(endDate, currentSeries.getTimeSeries().getLatestTime());
        LocalDateDoubleTimeSeries previousSeries = previous.getSecond().getTimeSeries();
        final LocalDate previousStart = previousSeries.getEarliestTime();
        final LocalDate previousEnd = previousSeries.getLatestTime();
        assertEquals(previousSeries, currentSeries.getTimeSeries().subSeries(previousStart, true, previousEnd, true));
      } else {
        assertEquals(previous.getSecond().getTimeSeries().getLatestTime(), currentSeries.getTimeSeries().getLatestTime());
      }
    }
  }

  public void reload() throws Exception {
    List<Pair<HistoricalTimeSeriesInfoDocument, HistoricalTimeSeries>> previousDocs = addTimeSeries();
    HistoricalTimeSeriesMaster htsMaster = getHtsMaster();
    BloombergHTSMasterUpdater htsMasterUpdater = getHtsMasterUpdater();
    //sleep for 1sec because of nano sec accuracy lost in the database
    Thread.sleep(1000);
    htsMasterUpdater.setReload(true);
    htsMasterUpdater.run();
    for (Pair<HistoricalTimeSeriesInfoDocument, HistoricalTimeSeries> previous : previousDocs) {
      HistoricalTimeSeries currentSeries = htsMaster.getTimeSeries(previous.getSecond().getUniqueId(), VersionCorrection.LATEST);
      assertNotNull(currentSeries);
      // after reload time series start date should be same, because the test provider returns
      // time-series with previous start date, but this may be different in production
      assertEquals(previous.getSecond().getTimeSeries().getEarliestTime(), currentSeries.getTimeSeries().getEarliestTime());
      // will reload only for Bloomberg dataSource
      if (previous.getFirst().getInfo().getDataSource().equals(BLOOMBERG_DATA_SOURCE_NAME)) {
        assertTrue(!previous.getSecond().getTimeSeries().equals(currentSeries.getTimeSeries()));
      } else {
        assertEquals(previous.getSecond().getTimeSeries(), currentSeries.getTimeSeries());
      }
    }
  }

}
