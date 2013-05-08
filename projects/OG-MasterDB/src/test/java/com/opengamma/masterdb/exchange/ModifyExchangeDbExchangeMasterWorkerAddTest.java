/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.exchange;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.master.exchange.ExchangeDocument;
import com.opengamma.master.exchange.ManageableExchange;
import com.opengamma.util.test.DbTest;
import com.opengamma.util.test.TestGroup;

/**
 * Tests ModifyExchangeDbExchangeMasterWorker.
 */
@Test(groups = TestGroup.UNIT_DB)
public class ModifyExchangeDbExchangeMasterWorkerAddTest extends AbstractDbExchangeMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(ModifyExchangeDbExchangeMasterWorkerAddTest.class);
  private static final ExternalIdBundle BUNDLE = ExternalIdBundle.of("A", "B");
  private static final ExternalIdBundle REGION = ExternalIdBundle.of("C", "D");

  @Factory(dataProvider = "databases", dataProviderClass = DbTest.class)
  public ModifyExchangeDbExchangeMasterWorkerAddTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion, false);
    s_logger.info("running testcases for {}", databaseType);
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_addExchange_nullDocument() {
    _exgMaster.add(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_add_noExchange() {
    ExchangeDocument doc = new ExchangeDocument();
    _exgMaster.add(doc);
  }

  @Test
  public void test_add() {
    Instant now = Instant.now(_exgMaster.getClock());
    
    ManageableExchange exchange = new ManageableExchange(BUNDLE, "Test", REGION, null);
    ExchangeDocument doc = new ExchangeDocument(exchange);
    ExchangeDocument test = _exgMaster.add(doc);
    
    UniqueId uniqueId = test.getUniqueId();
    assertNotNull(uniqueId);
    assertEquals("DbExg", uniqueId.getScheme());
    assertTrue(uniqueId.isVersioned());
    assertTrue(Long.parseLong(uniqueId.getValue()) >= 1000);
    assertEquals("0", uniqueId.getVersion());
    assertEquals(now, test.getVersionFromInstant());
    assertEquals(null, test.getVersionToInstant());
    assertEquals(now, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    ManageableExchange testExchange = test.getExchange();
    assertNotNull(testExchange);
    assertEquals(uniqueId, testExchange.getUniqueId());
    assertEquals("Test", test.getName());
    assertEquals(BUNDLE, testExchange.getExternalIdBundle());
    assertEquals(REGION, testExchange.getRegionIdBundle());
    assertEquals(null, testExchange.getTimeZone());
  }

  @Test
  public void test_add_addThenGet() {
    ManageableExchange exchange = new ManageableExchange(BUNDLE, "Test", REGION, null);
    ExchangeDocument doc = new ExchangeDocument(exchange);
    ExchangeDocument added = _exgMaster.add(doc);
    
    ExchangeDocument test = _exgMaster.get(added.getUniqueId());
    assertEquals(added, test);
  }


  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_add_addWithMissingNameProperty() {
    ManageableExchange exchange = new ManageableExchange();
    ExchangeDocument doc = new ExchangeDocument(exchange);
    _exgMaster.add(doc);
  }

  @Test
  public void test_add_addWithMinimalProperties() {
    ManageableExchange exchange = new ManageableExchange();
    exchange.setName("Test");
    ExchangeDocument doc = new ExchangeDocument(exchange);
    _exgMaster.add(doc);
  }

}
