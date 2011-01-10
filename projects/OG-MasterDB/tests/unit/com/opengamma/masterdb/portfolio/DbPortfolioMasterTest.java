/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.portfolio;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.TimeZone;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;

import com.opengamma.masterdb.DbMasterTestUtils;
import com.opengamma.util.test.DBTest;

/**
 * Test DbPortfolioMaster.
 */
public class DbPortfolioMasterTest extends DBTest {

  private static final Logger s_logger = LoggerFactory.getLogger(DbPortfolioMasterTest.class);

  private DbPortfolioMaster _prtMaster;

  public DbPortfolioMasterTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
  }

  @Before
  public void setUp() throws Exception {
    super.setUp();
    ConfigurableApplicationContext context = DbMasterTestUtils.getContext(getDatabaseType());
    _prtMaster = (DbPortfolioMaster) context.getBean(getDatabaseType() + "DbPortfolioMaster");
  }

  @After
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
