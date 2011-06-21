/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.historicaldata.impl;


import static com.opengamma.master.historicaldata.impl.HistoricalTimeSeriesInfoFieldNames.DATA_PROVIDER_NAME;
import static com.opengamma.master.historicaldata.impl.HistoricalTimeSeriesInfoFieldNames.DATA_SOURCE_NAME;
import static com.opengamma.master.historicaldata.impl.HistoricalTimeSeriesInfoFieldNames.STAR_VALUE;
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
import com.opengamma.master.historicaldata.HistoricalTimeSeriesDocument;
import com.opengamma.master.historicaldata.HistoricalTimeSeriesInfo;
import com.opengamma.master.historicaldata.HistoricalTimeSeriesResolver;
import com.opengamma.master.historicaldata.HistoricalTimeSeriesMaster;
import com.opengamma.master.historicaldata.ManageableHistoricalTimeSeries;
import com.opengamma.master.historicaldata.impl.DefaultHistoricalTimeSeriesResolver;
import com.opengamma.master.historicaldata.impl.HistoricalTimeSeriesInfoConfiguration;
import com.opengamma.master.historicaldata.impl.HistoricalTimeSeriesInfoRating;
import com.opengamma.master.historicaldata.impl.InMemoryHistoricalTimeSeriesMaster;
import com.opengamma.master.historicaldata.impl.RandomTimeSeriesGenerator;
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
  
  private static final String[] DATA_FIELDS = new String[] { HistoricalTimeSeriesResolver.DEFAULT_DATA_FIELD, "VOLUME" };
  private static final String[] DATA_PROVIDERS = new String[] { "UNKNOWN", "CMPL", "CMPT" };
  private static final String[] DATA_SOURCES = new String[] { "BLOOMBERG", "REUTERS", "JPM" };
  private static final String CONFIG_DOC_NAME = "TEST";

  private DefaultHistoricalTimeSeriesResolver _infoResolver;
  private HistoricalTimeSeriesMaster _tsMaster = new InMemoryHistoricalTimeSeriesMaster();

  @BeforeMethod
  public void setUp() throws Exception {
    InMemoryConfigMaster configMaster = new InMemoryConfigMaster();
    populateConfigMaster(configMaster);
    _infoResolver = new DefaultHistoricalTimeSeriesResolver(_tsMaster, new MasterConfigSource(configMaster));
  }

  private void populateConfigMaster(InMemoryConfigMaster configMaster) {
    ConfigDocument<HistoricalTimeSeriesInfoConfiguration> testDoc = new ConfigDocument<HistoricalTimeSeriesInfoConfiguration>(HistoricalTimeSeriesInfoConfiguration.class);
    testDoc.setName(CONFIG_DOC_NAME);
    testDoc.setValue(createRules());
    ConfigMasterUtils.storeByName(configMaster, testDoc);
  }

  private HistoricalTimeSeriesInfoConfiguration createRules() {
    List<HistoricalTimeSeriesInfoRating> rules = new ArrayList<HistoricalTimeSeriesInfoRating>();
    rules.add(new HistoricalTimeSeriesInfoRating(DATA_SOURCE_NAME, "BLOOMBERG", 3));
    rules.add(new HistoricalTimeSeriesInfoRating(DATA_SOURCE_NAME, "REUTERS", 2));
    rules.add(new HistoricalTimeSeriesInfoRating(DATA_SOURCE_NAME, "JPM", 1));
    rules.add(new HistoricalTimeSeriesInfoRating(DATA_SOURCE_NAME, "XXX", 0));
    rules.add(new HistoricalTimeSeriesInfoRating(DATA_SOURCE_NAME, STAR_VALUE, 0));
    
    rules.add(new HistoricalTimeSeriesInfoRating(DATA_PROVIDER_NAME, "CMPL", 3));
    rules.add(new HistoricalTimeSeriesInfoRating(DATA_PROVIDER_NAME, "CMPT", 2));
    rules.add(new HistoricalTimeSeriesInfoRating(DATA_PROVIDER_NAME, "CMPN", 1));
    rules.add(new HistoricalTimeSeriesInfoRating(DATA_PROVIDER_NAME, "EXCH_LSE", 0));
    rules.add(new HistoricalTimeSeriesInfoRating(DATA_PROVIDER_NAME, STAR_VALUE, 0));
    
    HistoricalTimeSeriesInfoConfiguration config = new HistoricalTimeSeriesInfoConfiguration(rules);
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
      HistoricalTimeSeriesInfo defaultMetaData = _infoResolver.getInfo(identifierBundleWithDates.asIdentifierBundle(), CONFIG_DOC_NAME);
      assertNotNull(defaultMetaData);
      assertEquals(DEFAULT_DATA_SOURCE, defaultMetaData.getDataSource());
      assertEquals(DEFAULT_DATA_PROVIDER, defaultMetaData.getDataProvider());
      assertEquals(HistoricalTimeSeriesResolver.DEFAULT_DATA_FIELD, defaultMetaData.getDataField());
    }
  }

  protected List<HistoricalTimeSeriesDocument> addAndTestTimeSeries() {
    List<HistoricalTimeSeriesDocument> result = new ArrayList<HistoricalTimeSeriesDocument>(); 
    for (int i = 0; i < TS_DATASET_SIZE; i++) {
      IdentifierBundle identifiers = IdentifierBundle.of(SecurityUtils.bloombergTickerSecurityId("ticker" + i), SecurityUtils.bloombergBuidSecurityId("buid" + i));
      
      LocalDate start = DateUtil.previousWeekDay().minusDays(7);
      for (String dataSource : DATA_SOURCES) {
        for (String dataProvider : DATA_PROVIDERS) {
          for (String datafield : DATA_FIELDS) {
            ManageableHistoricalTimeSeries series = new ManageableHistoricalTimeSeries();
            series.setDataField(datafield);
            series.setDataProvider(dataProvider);
            series.setDataSource(dataSource);
            series.setObservationTime(LCLOSE_OBSERVATION_TIME);
            series.setIdentifiers(IdentifierBundleWithDates.of(identifiers));
            LocalDateDoubleTimeSeries timeSeries = RandomTimeSeriesGenerator.makeRandomTimeSeries(start, 7);
            assertTrue(timeSeries.size() == 7);
            assertEquals(start, timeSeries.getEarliestTime());
            series.setTimeSeries(timeSeries);
            HistoricalTimeSeriesDocument tsDocument = new HistoricalTimeSeriesDocument(series);
            
            tsDocument = _tsMaster.add(tsDocument);
            
            assertNotNull(tsDocument);
            assertNotNull(tsDocument.getUniqueId());
            
            HistoricalTimeSeriesDocument actualDoc = _tsMaster.get(tsDocument.getUniqueId());
            assertNotNull(actualDoc);
            assertEquals(timeSeries, actualDoc.getSeries().getTimeSeries());
            result.add(tsDocument);
          }
        }
      }
    }
    return result;
  }

}
