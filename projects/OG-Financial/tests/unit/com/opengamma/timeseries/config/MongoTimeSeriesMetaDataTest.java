/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.timeseries.config;

import static org.junit.Assert.assertEquals;

import java.util.Random;

import org.junit.After;
import org.junit.Before;

import com.opengamma.config.ConfigDocument;
import com.opengamma.config.DefaultConfigDocument;
import com.opengamma.config.db.MongoDBConfigMaster;
import com.opengamma.config.test.MongoDBConfigMasterTestCase;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.timeseries.config.TimeSeriesMetaDataConfiguration;
import com.opengamma.util.MongoDBConnectionSettings;
import com.opengamma.util.test.MongoDBTestUtils;


/**
 * Test that TimeSeriesMetaDataDefinition can be serialized
 */
public class MongoTimeSeriesMetaDataTest extends MongoDBConfigMasterTestCase<TimeSeriesMetaDataConfiguration>{

  private MongoDBConnectionSettings _mongoSettings;
  private Random _random = new Random();
  
  /**
   * @param entityType
   */
  public MongoTimeSeriesMetaDataTest() {
    super(TimeSeriesMetaDataConfiguration.class);
  }
  
  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    super.setUp();
  }

  /**
   * @throws java.lang.Exception
   */
  @After
  public void tearDown() throws Exception {
    super.tearDown();
  }

  @Override
  protected void assertConfigDocumentValue(TimeSeriesMetaDataConfiguration expected, TimeSeriesMetaDataConfiguration actual) {
    assertEquals(expected.getDefaultDataField(), actual.getDefaultDataField());
    assertEquals(expected.getDataFields(), actual.getDataFields());
    assertEquals(expected.getDefaultDataProvider(), actual.getDefaultDataProvider());
    assertEquals(expected.getDataProviders(), actual.getDataProviders());
    assertEquals(expected.getDefaultDataSource(), actual.getDefaultDataSource());
    assertEquals(expected.getDataSources(), actual.getDataSources());
    assertEquals(expected.getSecurityType(), actual.getSecurityType());
  }

  @Override
  protected MongoDBConfigMaster<TimeSeriesMetaDataConfiguration> createMongoConfigMaster() {
    //use className as collection so do not set collectionName
    MongoDBConnectionSettings settings = MongoDBTestUtils.makeTestSettings(null, false);
    _mongoSettings = settings;
    return new MongoDBConfigMaster<TimeSeriesMetaDataConfiguration>(TimeSeriesMetaDataConfiguration.class, settings, true);
  }

  @Override
  protected MongoDBConnectionSettings getMongoDBConnectionSettings() {
    return _mongoSettings;
  }

  @Override
  protected TimeSeriesMetaDataConfiguration makeRandomConfigDoc() {
    String securityType = EquitySecurity.SECURITY_TYPE;
    String dataSourceDefault = "BLOOMBERG" + _random.nextInt();
    String dataFieldDefault = "PX_LAST" + _random.nextInt();
    String dataProviderDefault = "EXCH";
    TimeSeriesMetaDataConfiguration definition = new TimeSeriesMetaDataConfiguration(securityType, dataSourceDefault, dataFieldDefault, dataProviderDefault);
    definition.addDataField(dataFieldDefault);
    definition.addDataField("VOLUME" + _random.nextInt());
    definition.addDataSource(dataSourceDefault);
    definition.addDataSource("REUTERS" + _random.nextInt());
    return definition;
  }

  @Override
  protected ConfigDocument<TimeSeriesMetaDataConfiguration> makeTestConfigDoc(int version) {
    DefaultConfigDocument<TimeSeriesMetaDataConfiguration> doc = new DefaultConfigDocument<TimeSeriesMetaDataConfiguration>();
    doc.setName("TestName" + version);
    doc.setValue(makeRandomConfigDoc());
    return doc;
  }

}
