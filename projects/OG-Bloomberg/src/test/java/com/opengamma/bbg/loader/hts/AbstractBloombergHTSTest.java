/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.bbg.loader.hts;

import static com.opengamma.bbg.BloombergConstants.BLOOMBERG_DATA_SOURCE_NAME;
import static com.opengamma.util.time.DateUtils.previousWeekDay;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.opengamma.bbg.BloombergConstants;
import com.opengamma.bbg.BloombergIdentifierProvider;
import com.opengamma.bbg.util.MockReferenceDataProvider;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalIdBundleWithDates;
import com.opengamma.id.ExternalIdWithDates;
import com.opengamma.id.UniqueId;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoDocument;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;
import com.opengamma.master.historicaltimeseries.ManageableHistoricalTimeSeriesInfo;
import com.opengamma.master.historicaltimeseries.impl.InMemoryHistoricalTimeSeriesMaster;
import com.opengamma.provider.historicaltimeseries.HistoricalTimeSeriesProvider;
import com.opengamma.provider.historicaltimeseries.HistoricalTimeSeriesProviderGetRequest;
import com.opengamma.provider.historicaltimeseries.HistoricalTimeSeriesProviderGetResult;
import com.opengamma.provider.historicaltimeseries.impl.AbstractHistoricalTimeSeriesProvider;
import com.opengamma.timeseries.date.localdate.ImmutableLocalDateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeriesBuilder;
import com.opengamma.util.MapUtils;
import com.opengamma.util.time.LocalDateRange;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * 
 */
public abstract class AbstractBloombergHTSTest {

  private static final Logger s_logger = LoggerFactory.getLogger(AbstractBloombergHTSTest.class);
  
  protected static final String[] DATA_FIELDS = new String[] {"PX_LAST", "VOLUME"};
  protected static final String[] DATA_PROVIDERS = new String[] {"UNKNOWN", "CMPL", "CMPT", "DEFAULT"};
  protected static final String[] DATA_SOURCES = new String[] {BLOOMBERG_DATA_SOURCE_NAME, "REUTERS", "JPM"};
  protected static final int TS_DATASET_SIZE = 2;
  protected static final Map<String, String> s_provider2ObservationTime = ImmutableMap.of("UNKNOWN", "UNKNOWN",
      "CMPL", "LONDON_CLOSE",
      "CMPT", "TOKYO_CLOSE",
      "DEFAULT", "DEFAULT");

  private HistoricalTimeSeriesMaster _htsMaster;
  private BloombergHTSMasterUpdater _htsMasterUpdater;
  private BloombergHistoricalTimeSeriesLoader _loader;
  private HistoricalTimeSeriesProvider _historicalTimeSeriesProvider;

  /**
   * Creates an instance.
   */
  public AbstractBloombergHTSTest() {
    super();
  }

  //-------------------------------------------------------------------------
  @BeforeMethod(alwaysRun = true)
  protected void doSetUp() {
    _htsMaster = new InMemoryHistoricalTimeSeriesMaster();
    _historicalTimeSeriesProvider = new UnitTestHistoricalTimeSeriesProvider();
    BloombergIdentifierProvider idProvider = new BloombergIdentifierProvider(new MockReferenceDataProvider());
    _htsMasterUpdater = new BloombergHTSMasterUpdater(_htsMaster, _historicalTimeSeriesProvider, idProvider);
    _loader = new BloombergHistoricalTimeSeriesLoader(_htsMaster, _historicalTimeSeriesProvider, idProvider);
  }

  @AfterMethod(alwaysRun = true)
  protected void doTearDown() {
    _htsMaster = null;
  }

  //-------------------------------------------------------------------------
  private static class UnitTestHistoricalTimeSeriesProvider extends AbstractHistoricalTimeSeriesProvider {
    //keep track of start date to use the same for reloading
    Map<ExternalIdBundle, LocalDate> _startDateMap = new HashMap<ExternalIdBundle, LocalDate>();

    @Override
    protected HistoricalTimeSeriesProviderGetResult doBulkGet(HistoricalTimeSeriesProviderGetRequest request) {
      Map<ExternalIdBundle, LocalDateDoubleTimeSeries> tsMap = Maps.newHashMap();
      LocalDate start = request.getDateRange().getStartDateInclusive();
      LocalDate end = request.getDateRange().getEndDateInclusive();
      s_logger.debug("producing TS for startDate={} endDate={}", start, end);
      for (ExternalIdBundle identifiers : request.getExternalIdBundles()) {
        LocalDate cachedStart = MapUtils.putIfAbsentGet(_startDateMap, identifiers, start);

        if (start.isBefore(cachedStart)) {
          start = cachedStart;
        }
        if (end.equals(LocalDate.MAX)) {
          end = previousWeekDay();
        }
        LocalDateDoubleTimeSeries timeSeries = makeRandomTimeSeries(start, end);
        tsMap.put(identifiers, timeSeries);
      }
      return new HistoricalTimeSeriesProviderGetResult(tsMap);
    }

    private LocalDateDoubleTimeSeries makeRandomTimeSeries(LocalDate start, LocalDate end) {
      LocalDateDoubleTimeSeriesBuilder tsMap = ImmutableLocalDateDoubleTimeSeries.builder();
      LocalDate current = start;
      tsMap.put(current, Math.random());
      while (current.isBefore(end)) {
        current = current.plusDays(1);
        if (isWeekday(current)) {
          tsMap.put(current, Math.random());
        }
      }
      return tsMap.build();
    }

    private boolean isWeekday(LocalDate day) {
      return (day.getDayOfWeek() != DayOfWeek.SATURDAY && day.getDayOfWeek() != DayOfWeek.SUNDAY);
    }
  } 
  
  protected List<Pair<HistoricalTimeSeriesInfoDocument, HistoricalTimeSeries>> addTimeSeries() {
    List<Pair<HistoricalTimeSeriesInfoDocument, HistoricalTimeSeries>> result = Lists.newArrayList();
    for (int i = 0; i < TS_DATASET_SIZE; i++) {

      LocalDate end = previousWeekDay().minusWeeks(1);
      LocalDate start = end.minusWeeks(2);
      
      for (String dataSource : DATA_SOURCES) {
        for (String dataProvider : DATA_PROVIDERS) {
          for (String dataField : DATA_FIELDS) {
            ManageableHistoricalTimeSeriesInfo info = new ManageableHistoricalTimeSeriesInfo();
            info.setName(dataField + " " + dataSource);
            info.setDataField(dataField);
            info.setDataProvider(dataProvider);
            info.setDataSource(dataSource);
            info.setObservationTime(s_provider2ObservationTime.get(dataProvider));
            
            ExternalId ticker = ExternalSchemes.bloombergTickerSecurityId("ticker" + i);
            ExternalId buid = ExternalSchemes.bloombergBuidSecurityId("buid" + i);
            final ExternalIdBundleWithDates bundleWithDates = ExternalIdBundleWithDates.of(ExternalIdWithDates.of(ticker), 
                ExternalIdWithDates.of(buid));
            info.setExternalIdBundle(bundleWithDates);
            HistoricalTimeSeriesInfoDocument added = _htsMaster.add(new HistoricalTimeSeriesInfoDocument(info));
            assertNotNull(added);
            assertNotNull(added.getUniqueId());
            
            Map<ExternalIdBundle, LocalDateDoubleTimeSeries> resultMap = _historicalTimeSeriesProvider.getHistoricalTimeSeries(
                Collections.singleton(bundleWithDates.toBundle()), BloombergConstants.BLOOMBERG_DATA_SOURCE_NAME, dataProvider, dataField, LocalDateRange.of(start, end, true));
            LocalDateDoubleTimeSeries timeSeries = resultMap.get(bundleWithDates.toBundle());
            UniqueId tsUid = _htsMaster.updateTimeSeriesDataPoints(added.getInfo().getTimeSeriesObjectId(), timeSeries);
            
            HistoricalTimeSeries hts = _htsMaster.getTimeSeries(tsUid);
            assertNotNull(hts);
            assertEquals(timeSeries, hts.getTimeSeries());
            result.add(Pairs.of(added, hts));
          }
        }
      }
    }
    return result;
  }
  
  /**
   * Gets the htsMasterUpdater.
   * @return the htsMasterUpdater
   */
  protected BloombergHTSMasterUpdater getHtsMasterUpdater() {
    return _htsMasterUpdater;
  }

  /**
   * Gets the loader.
   * @return the loader
   */
  protected BloombergHistoricalTimeSeriesLoader getLoader() {
    return _loader;
  }

  /**
   * Gets the htsMaster.
   * @return the htsMaster
   */
  protected HistoricalTimeSeriesMaster getHtsMaster() {
    return _htsMaster;
  }
}
