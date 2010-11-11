/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.exchange;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.TimeZone;

import javax.time.Instant;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.exchange.ExchangeDocument;
import com.opengamma.master.exchange.ManageableExchange;

/**
 * Tests ModifyExchangeDbExchangeMasterWorker.
 */
public class ModifyExchangeDbExchangeMasterWorkerAddTest extends AbstractDbExchangeMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(ModifyExchangeDbExchangeMasterWorkerAddTest.class);
  private static final IdentifierBundle BUNDLE = IdentifierBundle.of(Identifier.of("A", "B"));
  private static final IdentifierBundle REGION = IdentifierBundle.of(Identifier.of("C", "D"));

  private ModifyExchangeDbExchangeMasterWorker _worker;
  private DbExchangeMasterWorker _queryWorker;

  public ModifyExchangeDbExchangeMasterWorkerAddTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
  }

  @Before
  public void setUp() throws Exception {
    super.setUp();
    _worker = new ModifyExchangeDbExchangeMasterWorker();
    _worker.init(_exgMaster);
    _queryWorker = new QueryExchangeDbExchangeMasterWorker();
    _queryWorker.init(_exgMaster);
  }

  @After
  public void tearDown() throws Exception {
    super.tearDown();
    _worker = null;
    _queryWorker = null;
  }

  //-------------------------------------------------------------------------
  @Test(expected = NullPointerException.class)
  public void test_addExchange_nullDocument() {
    _worker.add(null);
  }

  @Test(expected = NullPointerException.class)
  public void test_add_noExchange() {
    ExchangeDocument doc = new ExchangeDocument();
    _worker.add(doc);
  }

  @Test
  public void test_add() {
    Instant now = Instant.now(_exgMaster.getTimeSource());
    
    ManageableExchange exchange = new ManageableExchange(BUNDLE, "Test", REGION, null);
    ExchangeDocument doc = new ExchangeDocument(exchange);
    ExchangeDocument test = _worker.add(doc);
    
    UniqueIdentifier uid = test.getExchangeId();
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
    assertEquals(uid, testExchange.getUniqueIdentifier());
    assertEquals("Test", test.getName());
    assertEquals(BUNDLE, testExchange.getIdentifiers());
    assertEquals(REGION, testExchange.getRegionId());
    assertEquals(null, testExchange.getTimeZone());
  }

  @Test
  public void test_add_addThenGet() {
    ManageableExchange exchange = new ManageableExchange(BUNDLE, "Test", REGION, null);
    ExchangeDocument doc = new ExchangeDocument(exchange);
    ExchangeDocument added = _worker.add(doc);
    
    ExchangeDocument test = _queryWorker.get(added.getExchangeId());
    assertEquals(added, test);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    assertEquals(_worker.getClass().getSimpleName() + "[DbExg]", _worker.toString());
  }

}
