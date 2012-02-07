/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.historicaltimeseries;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.opengamma.masterdb.DbMasterTestUtils;
import com.opengamma.util.test.DbTest;

/**
 * Test DbHistoricalTimeSeriesMaster.
 */
public class DbHistoricalTimeSeriesMasterTest extends DbTest {

  private static final Logger s_logger = LoggerFactory.getLogger(DbHistoricalTimeSeriesMasterTest.class);

  private DbHistoricalTimeSeriesMaster _htsMaster;

  @Factory(dataProvider = "databases", dataProviderClass = DbTest.class)
  public DbHistoricalTimeSeriesMasterTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
  }

  @BeforeMethod
  public void setUp() throws Exception {
    super.setUp();
    ConfigurableApplicationContext context = DbMasterTestUtils.getContext(getDatabaseType());
    _htsMaster = (DbHistoricalTimeSeriesMaster) context.getBean(getDatabaseType() + "DbHistoricalTimeSeriesMaster");
  }

  @AfterMethod
  public void tearDown() throws Exception {
    super.tearDown();
    _htsMaster = null;
  }

  @AfterSuite
  public static void closeAfterSuite() {
    DbMasterTestUtils.closeAfterSuite();
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_basics() throws Exception {
    assertNotNull(_htsMaster);
    assertEquals(true, _htsMaster.getUniqueIdScheme().equals("DbHts"));
    assertNotNull(_htsMaster.getDbConnector());
    assertNotNull(_htsMaster.getTimeSource());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    assertEquals("DbHistoricalTimeSeriesMaster[DbHts]", _htsMaster.toString());
  }

}
