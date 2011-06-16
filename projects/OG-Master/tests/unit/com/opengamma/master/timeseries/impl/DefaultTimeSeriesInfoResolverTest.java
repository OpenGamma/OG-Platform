/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.timeseries.impl;


import static com.opengamma.master.timeseries.impl.TimeSeriesInfoFieldNames.DATA_PROVIDER_NAME;
import static com.opengamma.master.timeseries.impl.TimeSeriesInfoFieldNames.DATA_SOURCE_NAME;
import static com.opengamma.master.timeseries.impl.TimeSeriesInfoFieldNames.STAR_VALUE;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.util.ArrayList;
import java.util.List;

import javax.time.calendar.LocalDate;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.opengamma.core.security.SecurityUtils;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.IdentifierBundleWithDates;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigMasterUtils;
import com.opengamma.master.config.impl.InMemoryConfigMaster;
import com.opengamma.master.config.impl.MasterConfigSource;
import com.opengamma.master.timeseries.TimeSeriesDocument;
import com.opengamma.master.timeseries.TimeSeriesInfo;
import com.opengamma.master.timeseries.TimeSeriesInfoResolver;
import com.opengamma.master.timeseries.TimeSeriesMaster;
import com.opengamma.util.time.DateUtil;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;

/**
 * Test DefaultTimeSeriesResolver.
 */
@Test
public class DefaultTimeSeriesInfoResolverTest {

  private static final int TS_DATASET_SIZE = 1;
  private static final String LCLOSE_OBSERVATION_TIME = "LCLOSE";
  private static final String DEFAULT_DATA_SOURCE = "BLOOMBERG";
  private static final String DEFAULT_DATA_PROVIDER = "CMPL";
  
  private static final String[] DATA_FIELDS = new String[] { TimeSeriesInfoResolver.DEFAULT_DATA_FIELD, "VOLUME" };
  private static final String[] DATA_PROVIDERS = new String[] { "UNKNOWN", "CMPL", "CMPT" };
  private static final String[] DATA_SOURCES = new String[] { "BLOOMBERG", "REUTERS", "JPM" };
  private static final String CONFIG_DOC_NAME = "TEST";

  private DefaultTimeSeriesInfoResolver _infoResolver;
  private TimeSeriesMaster _tsMaster = new InMemoryHistoricalTimeSeriesMaster();

  @BeforeMethod
  public void setUp() throws Exception {
    InMemoryConfigMaster configMaster = new InMemoryConfigMaster();
    populateConfigMaster(configMaster);
    _infoResolver = new DefaultTimeSeriesInfoResolver(_tsMaster, new MasterConfigSource(configMaster));
  }

  private void populateConfigMaster(InMemoryConfigMaster configMaster) {
    ConfigDocument<TimeSeriesInfoConfiguration> testDoc = new ConfigDocument<TimeSeriesInfoConfiguration>(TimeSeriesInfoConfiguration.class);
    testDoc.setName(CONFIG_DOC_NAME);
    testDoc.setValue(createRules());
    ConfigMasterUtils.storeByName(configMaster, testDoc);
  }

  private TimeSeriesInfoConfiguration createRules() {
    List<TimeSeriesInfoRating> rules = new ArrayList<TimeSeriesInfoRating>();
    rules.add(new TimeSeriesInfoRating(DATA_SOURCE_NAME, "BLOOMBERG", 3));
    rules.add(new TimeSeriesInfoRating(DATA_SOURCE_NAME, "REUTERS", 2));
    rules.add(new TimeSeriesInfoRating(DATA_SOURCE_NAME, "JPM", 1));
    rules.add(new TimeSeriesInfoRating(DATA_SOURCE_NAME, "XXX", 0));
    rules.add(new TimeSeriesInfoRating(DATA_SOURCE_NAME, STAR_VALUE, 0));
    
    rules.add(new TimeSeriesInfoRating(DATA_PROVIDER_NAME, "CMPL", 3));
    rules.add(new TimeSeriesInfoRating(DATA_PROVIDER_NAME, "CMPT", 2));
    rules.add(new TimeSeriesInfoRating(DATA_PROVIDER_NAME, "CMPN", 1));
    rules.add(new TimeSeriesInfoRating(DATA_PROVIDER_NAME, "EXCH_LSE", 0));
    rules.add(new TimeSeriesInfoRating(DATA_PROVIDER_NAME, STAR_VALUE, 0));
    
    TimeSeriesInfoConfiguration config = new TimeSeriesInfoConfiguration(rules);
    return config;
  }

  @AfterMethod
  public void tearDown() throws Exception {
    _infoResolver = null;
    _tsMaster = null;
  }

  //-------------------------------------------------------------------------
  public void test() throws Exception {
    addAndTestTimeSeries();
    List<IdentifierBundleWithDates> identifiers = _tsMaster.getAllIdentifiers();
    for (IdentifierBundleWithDates identifierBundleWithDates : identifiers) {
      TimeSeriesInfo defaultMetaData = _infoResolver.getInfo(identifierBundleWithDates.asIdentifierBundle(), CONFIG_DOC_NAME);
      assertNotNull(defaultMetaData);
      assertEquals(DEFAULT_DATA_SOURCE, defaultMetaData.getDataSource());
      assertEquals(DEFAULT_DATA_PROVIDER, defaultMetaData.getDataProvider());
      assertEquals(TimeSeriesInfoResolver.DEFAULT_DATA_FIELD, defaultMetaData.getDataField());
    }
  }

  protected List<TimeSeriesDocument> addAndTestTimeSeries() {
    List<TimeSeriesDocument> result = new ArrayList<TimeSeriesDocument>(); 
    for (int i = 0; i < TS_DATASET_SIZE; i++) {
      IdentifierBundle identifiers = IdentifierBundle.of(SecurityUtils.bloombergTickerSecurityId("ticker" + i), SecurityUtils.bloombergBuidSecurityId("buid" + i));
      
      LocalDate start = DateUtil.previousWeekDay().minusDays(7);
      for (String dataSource : DATA_SOURCES) {
        for (String dataProvider : DATA_PROVIDERS) {
          for (String datafield : DATA_FIELDS) {
            TimeSeriesDocument tsDocument = new TimeSeriesDocument();
            tsDocument.setDataField(datafield);
            tsDocument.setDataProvider(dataProvider);
            tsDocument.setDataSource(dataSource);
            tsDocument.setObservationTime(LCLOSE_OBSERVATION_TIME);
            tsDocument.setIdentifiers(IdentifierBundleWithDates.of(identifiers));
            LocalDateDoubleTimeSeries timeSeries = RandomTimeSeriesGenerator.makeRandomTimeSeries(start, 7);
            assertTrue(timeSeries.size() == 7);
            assertEquals(start, timeSeries.getEarliestTime());
            tsDocument.setTimeSeries(timeSeries);
            
            tsDocument = _tsMaster.add(tsDocument);
            
            assertNotNull(tsDocument);
            assertNotNull(tsDocument.getUniqueId());
            
            TimeSeriesDocument actualDoc = _tsMaster.get(tsDocument.getUniqueId());
            assertNotNull(actualDoc);
            assertEquals(timeSeries, actualDoc.getTimeSeries());
            result.add(tsDocument);
          }
        }
      }
    }
    return result;
  }

}
