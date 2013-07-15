/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.exchange;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.threeten.bp.ZoneId;

import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.master.exchange.ExchangeDocument;
import com.opengamma.master.exchange.ManageableExchange;
import com.opengamma.util.test.AbstractDbTest;
import com.opengamma.util.test.DbTest;
import com.opengamma.util.test.TestGroup;

/**
 * Test DbExchangeMaster.
 */
@Test(groups = TestGroup.UNIT_DB)
public class DbExchangeMasterTest extends AbstractDbTest {

  private static final Logger s_logger = LoggerFactory.getLogger(DbExchangeMasterTest.class);

  private DbExchangeMaster _exgMaster;

  @Factory(dataProvider = "databases", dataProviderClass = DbTest.class)
  public DbExchangeMasterTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
  }

  //-------------------------------------------------------------------------
  @Override
  protected void doSetUp() {
    _exgMaster = new DbExchangeMaster(getDbConnector());
  }

  @Override
  protected void doTearDown() {
    _exgMaster = null;
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_basics() throws Exception {
    assertNotNull(_exgMaster);
    assertEquals(true, _exgMaster.getUniqueIdScheme().equals("DbExg"));
    assertNotNull(_exgMaster.getDbConnector());
    assertNotNull(_exgMaster.getClock());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_example() throws Exception {
    ManageableExchange exchange = new ManageableExchange();
    exchange.setExternalIdBundle(ExternalIdBundle.of(ExternalId.of("A", "B"), ExternalId.of("C", "D")));
    exchange.setName("Test");
    exchange.setRegionIdBundle(ExternalIdBundle.of(ExternalId.of("E", "F"), ExternalId.of("G", "H")));
    exchange.setTimeZone(ZoneId.of("Europe/London"));
    ExchangeDocument addDoc = new ExchangeDocument(exchange);
    ExchangeDocument added = _exgMaster.add(addDoc);
    
    ExchangeDocument loaded = _exgMaster.get(added.getUniqueId());
    assertEquals(added, loaded);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    assertEquals("DbExchangeMaster[DbExg]", _exgMaster.toString());
  }

}
