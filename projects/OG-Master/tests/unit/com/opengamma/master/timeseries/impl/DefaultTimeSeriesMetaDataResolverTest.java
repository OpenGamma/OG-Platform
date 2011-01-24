/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.timeseries.impl;


import static com.opengamma.master.timeseries.impl.TimeSeriesMetaDataFieldNames.DATA_PROVIDER_NAME;
import static com.opengamma.master.timeseries.impl.TimeSeriesMetaDataFieldNames.DATA_SOURCE_NAME;
import static com.opengamma.master.timeseries.impl.TimeSeriesMetaDataFieldNames.STAR_VALUE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import javax.time.calendar.LocalDate;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.opengamma.core.security.SecurityUtils;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.IdentifierBundleWithDates;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigMasterUtils;
import com.opengamma.master.config.ConfigTypeMaster;
import com.opengamma.master.config.impl.InMemoryConfigMaster;
import com.opengamma.master.config.impl.MasterConfigSource;
import com.opengamma.master.timeseries.TimeSeriesDocument;
import com.opengamma.master.timeseries.TimeSeriesMaster;
import com.opengamma.master.timeseries.TimeSeriesMetaData;
import com.opengamma.master.timeseries.TimeSeriesMetaDataResolver;
import com.opengamma.master.timeseries.impl.DefaultTimeSeriesMetaDataResolver;
import com.opengamma.master.timeseries.impl.InMemoryLocalDateTimeSeriesMaster;
import com.opengamma.master.timeseries.impl.RandomTimeSeriesGenerator;
import com.opengamma.master.timeseries.impl.TimeSeriesMetaDataConfiguration;
import com.opengamma.master.timeseries.impl.TimeSeriesMetaDataRating;
import com.opengamma.util.time.DateUtil;
import com.opengamma.util.timeseries.DoubleTimeSeries;

/**
 * Test DefaultTimeSeriesResolver.
 */
public class DefaultTimeSeriesMetaDataResolverTest {

  private static final int TS_DATASET_SIZE = 1;
  private static final String LCLOSE_OBSERVATION_TIME = "LCLOSE";
  private static final String DEFAULT_DATA_SOURCE = "BLOOMBERG";
  private static final String DEFAULT_DATA_PROVIDER = "CMPL";
  
  private static final String[] DATA_FIELDS = new String[] { TimeSeriesMetaDataResolver.DEFAULT_DATA_FIELD, "VOLUME" };
  private static final String[] DATA_PROVIDERS = new String[] { "UNKNOWN", "CMPL", "CMPT" };
  private static final String[] DATA_SOURCES = new String[] { "BLOOMBERG", "REUTERS", "JPM" };
  private static final String CONFIG_DOC_NAME = "TEST";

  private DefaultTimeSeriesMetaDataResolver<LocalDate> _metaDataResolver;
  private TimeSeriesMaster<LocalDate> _tsMaster = new InMemoryLocalDateTimeSeriesMaster();

  @Before
  public void setUp() throws Exception {
    InMemoryConfigMaster configMaster = new InMemoryConfigMaster();
    populateConfigMaster(configMaster);
    _metaDataResolver = new DefaultTimeSeriesMetaDataResolver<LocalDate>(_tsMaster, new MasterConfigSource(configMaster));
  }

  private void populateConfigMaster(InMemoryConfigMaster configMaster) {
    ConfigTypeMaster<TimeSeriesMetaDataConfiguration> timeSeriesConfigMaster = configMaster.typed(TimeSeriesMetaDataConfiguration.class);
    ConfigDocument<TimeSeriesMetaDataConfiguration> testDoc = new ConfigDocument<TimeSeriesMetaDataConfiguration>();
    testDoc.setName(CONFIG_DOC_NAME);
    testDoc.setValue(createRules());
    ConfigMasterUtils.storeByName(timeSeriesConfigMaster, testDoc);
  }

  private TimeSeriesMetaDataConfiguration createRules() {
    List<TimeSeriesMetaDataRating> rules = new ArrayList<TimeSeriesMetaDataRating>();
    rules.add(new TimeSeriesMetaDataRating(DATA_SOURCE_NAME, "BLOOMBERG", 3));
    rules.add(new TimeSeriesMetaDataRating(DATA_SOURCE_NAME, "REUTERS", 2));
    rules.add(new TimeSeriesMetaDataRating(DATA_SOURCE_NAME, "JPM", 1));
    rules.add(new TimeSeriesMetaDataRating(DATA_SOURCE_NAME, "XXX", 0));
    rules.add(new TimeSeriesMetaDataRating(DATA_SOURCE_NAME, STAR_VALUE, 0));
    
    rules.add(new TimeSeriesMetaDataRating(DATA_PROVIDER_NAME, "CMPL", 3));
    rules.add(new TimeSeriesMetaDataRating(DATA_PROVIDER_NAME, "CMPT", 2));
    rules.add(new TimeSeriesMetaDataRating(DATA_PROVIDER_NAME, "CMPN", 1));
    rules.add(new TimeSeriesMetaDataRating(DATA_PROVIDER_NAME, "EXCH_LSE", 0));
    rules.add(new TimeSeriesMetaDataRating(DATA_PROVIDER_NAME, STAR_VALUE, 0));
    
    TimeSeriesMetaDataConfiguration config = new TimeSeriesMetaDataConfiguration(rules);
    return config;
  }

  @After
  public void tearDown() throws Exception {
    _metaDataResolver = null;
    _tsMaster = null;
  }

  //-------------------------------------------------------------------------
  @Test
  public void test() throws Exception {
    addAndTestTimeSeries();
    List<IdentifierBundleWithDates> identifiers = _tsMaster.getAllIdentifiers();
    for (IdentifierBundleWithDates identifierBundleWithDates : identifiers) {
      TimeSeriesMetaData defaultMetaData = _metaDataResolver.getDefaultMetaData(identifierBundleWithDates.asIdentifierBundle(), CONFIG_DOC_NAME);
      assertNotNull(defaultMetaData);
      assertEquals(DEFAULT_DATA_SOURCE, defaultMetaData.getDataSource());
      assertEquals(DEFAULT_DATA_PROVIDER, defaultMetaData.getDataProvider());
      assertEquals(TimeSeriesMetaDataResolver.DEFAULT_DATA_FIELD, defaultMetaData.getDataField());
    }
  }

  protected List<TimeSeriesDocument<LocalDate>> addAndTestTimeSeries() {
    List<TimeSeriesDocument<LocalDate>> result = new ArrayList<TimeSeriesDocument<LocalDate>>(); 
    for (int i = 0; i < TS_DATASET_SIZE; i++) {
      IdentifierBundle identifiers = IdentifierBundle.of(SecurityUtils.bloombergTickerSecurityId("ticker" + i), SecurityUtils.bloombergBuidSecurityId("buid" + i));
      
      LocalDate start = DateUtil.previousWeekDay().minusDays(7);
      for (String dataSource : DATA_SOURCES) {
        for (String dataProvider : DATA_PROVIDERS) {
          for (String datafield : DATA_FIELDS) {
            TimeSeriesDocument<LocalDate> tsDocument = new TimeSeriesDocument<LocalDate>();
            tsDocument.setDataField(datafield);
            tsDocument.setDataProvider(dataProvider);
            tsDocument.setDataSource(dataSource);
            tsDocument.setObservationTime(LCLOSE_OBSERVATION_TIME);
            tsDocument.setIdentifiers(IdentifierBundleWithDates.of(identifiers));
            DoubleTimeSeries<LocalDate> timeSeries = RandomTimeSeriesGenerator.makeRandomTimeSeries(start, 7);
            assertTrue(timeSeries.size() == 7);
            assertEquals(start, timeSeries.getEarliestTime());
            tsDocument.setTimeSeries(timeSeries);
            
            tsDocument = _tsMaster.addTimeSeries(tsDocument);
            
            assertNotNull(tsDocument);
            assertNotNull(tsDocument.getUniqueId());
            
            TimeSeriesDocument<LocalDate> actualDoc = _tsMaster.getTimeSeries(tsDocument.getUniqueId());
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
