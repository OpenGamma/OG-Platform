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
import com.opengamma.config.ConfigMaster;
import com.opengamma.config.DefaultConfigDocument;
import com.opengamma.config.db.MongoDBConfigMaster;
import com.opengamma.config.test.MongoDBConfigMasterTestCase;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.util.MongoDBConnectionSettings;
import com.opengamma.util.test.MongoDBTestUtils;


/**
 * Test that TimeSeriesMetaDataDefinition can be serialized
 */
public class MongoTimeSeriesMetaDataTest extends MongoDBConfigMasterTestCase<TimeSeriesMetaDataDefinition>{

  private MongoDBConnectionSettings _mongoSettings;
  private Random _random = new Random();
  
  /**
   * @param entityType
   */
  public MongoTimeSeriesMetaDataTest() {
    super(TimeSeriesMetaDataDefinition.class);
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
  protected void assertConfigDocumentValue(TimeSeriesMetaDataDefinition expected, TimeSeriesMetaDataDefinition actual) {
    assertEquals(expected.getDefaultDataField(), actual.getDefaultDataField());
    assertEquals(expected.getDataFields(), actual.getDataFields());
    assertEquals(expected.getDefaultDataProvider(), actual.getDefaultDataProvider());
    assertEquals(expected.getDataProviders(), actual.getDataProviders());
    assertEquals(expected.getDefaultDataSource(), actual.getDefaultDataSource());
    assertEquals(expected.getDataSources(), actual.getDataSources());
    assertEquals(expected.getSecurityType(), actual.getSecurityType());
  }

  @Override
  protected ConfigMaster<TimeSeriesMetaDataDefinition> createMongoConfigMaster() {
    //use className as collection so do not set collectionName
    MongoDBConnectionSettings settings = MongoDBTestUtils.makeTestSettings(null, false);
    _mongoSettings = settings;
    return new MongoDBConfigMaster<TimeSeriesMetaDataDefinition>(TimeSeriesMetaDataDefinition.class, settings, true);
  }

  @Override
  protected MongoDBConnectionSettings getMongoDBConnectionSettings() {
    return _mongoSettings;
  }

  @Override
  protected TimeSeriesMetaDataDefinition makeRandomConfigDoc() {
    String securityType = EquitySecurity.SECURITY_TYPE;
    String dataSourceDefault = "BLOOMBERG" + _random.nextInt();
    String dataFieldDefault = "PX_LAST" + _random.nextInt();
    String dataProviderDefault = "EXCH";
    TimeSeriesMetaDataDefinition definition = new TimeSeriesMetaDataDefinition(securityType, dataSourceDefault, dataFieldDefault, dataProviderDefault);
    definition.addDataFields(dataFieldDefault);
    definition.addDataFields("VOLUME" + _random.nextInt());
    definition.addDataSources(dataSourceDefault);
    definition.addDataSources("REUTERS" + _random.nextInt());
    return definition;
  }

  @Override
  protected ConfigDocument<TimeSeriesMetaDataDefinition> makeTestConfigDoc(int version) {
    DefaultConfigDocument<TimeSeriesMetaDataDefinition> doc = new DefaultConfigDocument<TimeSeriesMetaDataDefinition>();
    doc.setName("TestName" + version);
    doc.setValue(makeRandomConfigDoc());
    return doc;
  }

}
