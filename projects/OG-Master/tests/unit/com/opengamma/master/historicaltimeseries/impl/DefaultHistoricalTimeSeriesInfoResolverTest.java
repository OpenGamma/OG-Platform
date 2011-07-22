/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.historicaltimeseries.impl;

import static com.opengamma.master.historicaltimeseries.impl.HistoricalTimeSeriesRatingFieldNames.DATA_PROVIDER_NAME;
import static com.opengamma.master.historicaltimeseries.impl.HistoricalTimeSeriesRatingFieldNames.DATA_SOURCE_NAME;
import static com.opengamma.master.historicaltimeseries.impl.HistoricalTimeSeriesRatingFieldNames.STAR_VALUE;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.util.ArrayList;
import java.util.List;

import javax.time.calendar.LocalDate;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesFields;
import com.opengamma.core.security.SecurityUtils;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.IdentifierBundleWithDates;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigMasterUtils;
import com.opengamma.master.config.impl.InMemoryConfigMaster;
import com.opengamma.master.config.impl.MasterConfigSource;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoDocument;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;
import com.opengamma.master.historicaltimeseries.ManageableHistoricalTimeSeriesInfo;
import com.opengamma.util.time.DateUtil;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;

/**
 * Test DefaultHistoricalTimeSeriesInfoResolver.
 */
@Test
public class DefaultHistoricalTimeSeriesInfoResolverTest {

  private static final int TS_DATASET_SIZE = 1;
  private static final String LCLOSE_OBSERVATION_TIME = "LCLOSE";
  private static final String DEFAULT_DATA_SOURCE = "BLOOMBERG";
  private static final String DEFAULT_DATA_PROVIDER = "CMPL";

  private static final String[] DATA_FIELDS = new String[] { "PX_LAST", "VOLUME" };
  private static final String[] DATA_PROVIDERS = new String[] { "UNKNOWN", "CMPL", "CMPT" };
  private static final String[] DATA_SOURCES = new String[] { "BLOOMBERG", "REUTERS", "JPM" };
  private static final String CONFIG_DOC_NAME = "TEST";

  private DefaultHistoricalTimeSeriesResolver _infoResolver;
  private HistoricalTimeSeriesMaster _htsMaster = new InMemoryHistoricalTimeSeriesMaster();

  @BeforeMethod
  public void setUp() throws Exception {
    InMemoryConfigMaster configMaster = new InMemoryConfigMaster();
    populateConfigMaster(configMaster);
    _infoResolver = new DefaultHistoricalTimeSeriesResolver(_htsMaster, new MasterConfigSource(configMaster));
  }

  private void populateConfigMaster(InMemoryConfigMaster configMaster) {
    ConfigDocument<HistoricalTimeSeriesRating> testDoc = new ConfigDocument<HistoricalTimeSeriesRating>(HistoricalTimeSeriesRating.class);
    testDoc.setName(CONFIG_DOC_NAME);
    testDoc.setValue(createRules());
    ConfigMasterUtils.storeByName(configMaster, testDoc);
  }

  private HistoricalTimeSeriesRating createRules() {
    List<HistoricalTimeSeriesRatingRule> rules = new ArrayList<HistoricalTimeSeriesRatingRule>();
    rules.add(new HistoricalTimeSeriesRatingRule(DATA_SOURCE_NAME, "BLOOMBERG", 3));
    rules.add(new HistoricalTimeSeriesRatingRule(DATA_SOURCE_NAME, "REUTERS", 2));
    rules.add(new HistoricalTimeSeriesRatingRule(DATA_SOURCE_NAME, "JPM", 1));
    rules.add(new HistoricalTimeSeriesRatingRule(DATA_SOURCE_NAME, "XXX", 0));
    rules.add(new HistoricalTimeSeriesRatingRule(DATA_SOURCE_NAME, STAR_VALUE, 0));
    
    rules.add(new HistoricalTimeSeriesRatingRule(DATA_PROVIDER_NAME, "CMPL", 3));
    rules.add(new HistoricalTimeSeriesRatingRule(DATA_PROVIDER_NAME, "CMPT", 2));
    rules.add(new HistoricalTimeSeriesRatingRule(DATA_PROVIDER_NAME, "CMPN", 1));
    rules.add(new HistoricalTimeSeriesRatingRule(DATA_PROVIDER_NAME, "EXCH_LSE", 0));
    rules.add(new HistoricalTimeSeriesRatingRule(DATA_PROVIDER_NAME, STAR_VALUE, 0));
    
    HistoricalTimeSeriesRating config = new HistoricalTimeSeriesRating(rules);
    return config;
  }

  @AfterMethod
  public void tearDown() throws Exception {
    _infoResolver = null;
    _htsMaster = null;
  }

  //-------------------------------------------------------------------------
  public void test() throws Exception {
    List<IdentifierBundleWithDates> identifiers = addAndTestTimeSeries();
    for (IdentifierBundleWithDates identifierBundleWithDates : identifiers) {
      UniqueIdentifier uniqueId = _infoResolver.resolve(HistoricalTimeSeriesFields.LAST_PRICE, identifierBundleWithDates.asIdentifierBundle(), null, CONFIG_DOC_NAME);
      assertNotNull(uniqueId);
      HistoricalTimeSeriesInfoDocument doc = _htsMaster.get(uniqueId);
      assertEquals(DEFAULT_DATA_SOURCE, doc.getInfo().getDataSource());
      assertEquals(DEFAULT_DATA_PROVIDER, doc.getInfo().getDataProvider());
      assertEquals("PX_LAST", doc.getInfo().getDataField());
    }
  }

  protected List<IdentifierBundleWithDates> addAndTestTimeSeries() {
    List<IdentifierBundleWithDates> result = new ArrayList<IdentifierBundleWithDates>(); 
    for (int i = 0; i < TS_DATASET_SIZE; i++) {
      IdentifierBundle identifiers = IdentifierBundle.of(SecurityUtils.bloombergTickerSecurityId("ticker" + i), SecurityUtils.bloombergBuidSecurityId("buid" + i));
      result.add(IdentifierBundleWithDates.of(identifiers));
      
      LocalDate start = DateUtil.previousWeekDay().minusDays(7);
      for (String dataSource : DATA_SOURCES) {
        for (String dataProvider : DATA_PROVIDERS) {
          for (String datafield : DATA_FIELDS) {
            ManageableHistoricalTimeSeriesInfo series = new ManageableHistoricalTimeSeriesInfo();
            series.setDataField(datafield);
            series.setDataProvider(dataProvider);
            series.setDataSource(dataSource);
            series.setObservationTime(LCLOSE_OBSERVATION_TIME);
            series.setIdentifiers(IdentifierBundleWithDates.of(identifiers));
            HistoricalTimeSeriesInfoDocument doc = _htsMaster.add(new HistoricalTimeSeriesInfoDocument(series));
            assertNotNull(doc);
            assertNotNull(doc.getUniqueId());
            
            LocalDateDoubleTimeSeries timeSeries = RandomTimeSeriesGenerator.makeRandomTimeSeries(start, 7);
            assertTrue(timeSeries.size() == 7);
            assertEquals(start, timeSeries.getEarliestTime());
            _htsMaster.updateTimeSeriesDataPoints(doc.getInfo().getTimeSeriesObjectId(), timeSeries);
          }
        }
      }
    }
    return result;
  }

}
