/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.portfolio;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.opengamma.masterdb.DbMasterTestUtils;
import com.opengamma.util.test.DBTest;

/**
 * Test DbPortfolioMaster.
 */
public class DbPortfolioMasterTest extends DBTest {

  private static final Logger s_logger = LoggerFactory.getLogger(DbPortfolioMasterTest.class);

  private DbPortfolioMaster _prtMaster;

  @Factory(dataProvider = "databases", dataProviderClass = DBTest.class)
  public DbPortfolioMasterTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
  }

  @BeforeMethod
  public void setUp() throws Exception {
    super.setUp();
    ConfigurableApplicationContext context = DbMasterTestUtils.getContext(getDatabaseType());
    _prtMaster = (DbPortfolioMaster) context.getBean(getDatabaseType() + "DbPortfolioMaster");
  }

  @AfterMethod
  public void tearDown() throws Exception {
    super.tearDown();
    _prtMaster = null;
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_basics() throws Exception {
    assertNotNull(_prtMaster);
    assertEquals(true, _prtMaster.getIdentifierScheme().equals("DbPrt"));
    assertNotNull(_prtMaster.getDbSource());
    assertNotNull(_prtMaster.getTimeSource());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    assertEquals("DbPortfolioMaster[DbPrt]", _prtMaster.toString());
  }

}
