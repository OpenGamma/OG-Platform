/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.exchange;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.util.TimeZone;

import javax.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.exchange.ExchangeDocument;
import com.opengamma.master.exchange.ManageableExchange;
import com.opengamma.util.test.DBTest;

/**
 * Tests ModifyExchangeDbExchangeMasterWorker.
 */
public class ModifyExchangeDbExchangeMasterWorkerAddTest extends AbstractDbExchangeMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(ModifyExchangeDbExchangeMasterWorkerAddTest.class);
  private static final IdentifierBundle BUNDLE = IdentifierBundle.of("A", "B");
  private static final IdentifierBundle REGION = IdentifierBundle.of("C", "D");

  @Factory(dataProvider = "databases", dataProviderClass = DBTest.class)
  public ModifyExchangeDbExchangeMasterWorkerAddTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
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
    Instant now = Instant.now(_exgMaster.getTimeSource());
    
    ManageableExchange exchange = new ManageableExchange(BUNDLE, "Test", REGION, null);
    ExchangeDocument doc = new ExchangeDocument(exchange);
    ExchangeDocument test = _exgMaster.add(doc);
    
    UniqueIdentifier uid = test.getUniqueId();
    assertNotNull(uid);
    assertEquals("DbExg", uid.getScheme());
    assertTrue(uid.isVersioned());
    assertTrue(Long.parseLong(uid.getValue()) >= 1000);
    assertEquals("0", uid.getVersion());
    assertEquals(now, test.getVersionFromInstant());
    assertEquals(null, test.getVersionToInstant());
    assertEquals(now, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    ManageableExchange testExchange = test.getExchange();
    assertNotNull(testExchange);
    assertEquals(uid, testExchange.getUniqueId());
    assertEquals("Test", test.getName());
    assertEquals(BUNDLE, testExchange.getIdentifiers());
    assertEquals(REGION, testExchange.getRegionKey());
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

  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    assertEquals(_exgMaster.getClass().getSimpleName() + "[DbExg]", _exgMaster.toString());
  }

}
