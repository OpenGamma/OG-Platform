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
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.opengamma.masterdb.DbMasterTestUtils;
import com.opengamma.util.test.DBTest;

/**
 * Test DbHistoricalTimeSeriesMaster.
 */
public class DbHistoricalTimeSeriesMasterTest extends DBTest {

  private static final Logger s_logger = LoggerFactory.getLogger(DbHistoricalTimeSeriesMasterTest.class);

  private DbHistoricalTimeSeriesMaster _htsMaster;

  @Factory(dataProvider = "databases", dataProviderClass = DBTest.class)
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

  //-------------------------------------------------------------------------
  @Test
  public void test_basics() throws Exception {
    assertNotNull(_htsMaster);
    assertEquals(true, _htsMaster.getUniqueIdScheme().equals("DbHts"));
    assertNotNull(_htsMaster.getDbSource());
    assertNotNull(_htsMaster.getTimeSource());
  }

//  //-------------------------------------------------------------------------
//  @Test
//  public void test_sample() throws Exception {
//    if (_htsMaster.getDbSource().getDialect() instanceof PostgreSQLDbHelper) {
//      HistoricalTimeSeriesInfoSearchRequest req = new HistoricalTimeSeriesInfoSearchRequest();
//      req.setPagingRequest(PagingRequest.FIRST_PAGE);
//      req.addExternalId(SecurityUtils.bloombergBuidSecurityId("EQ0010102100001000"));
//      req.addExternalId(SecurityUtils.bloombergTickerSecurityId("MOT US Equity"));
//      req.addExternalId(SecurityUtils.cusipSecurityId("620076109"));
//      req.addExternalId(SecurityUtils.isinSecurityId("US6200761095"));
//      req.addExternalId(SecurityUtils.sedol1SecurityId("2606600"));
//      req.setDataField("CUR_MKT_CAP");
//      HistoricalTimeSeriesInfoSearchResult res = _htsMaster.search(req);
//      assertEquals(1, res.getDocuments().size());
//      assertEquals("6142", res.getFirstInfo().getUniqueId().getValue());
//    }
//  }

  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    assertEquals("DbHistoricalTimeSeriesMaster[DbHts]", _htsMaster.toString());
  }

}
