/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.exchange;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import javax.time.Instant;
import javax.time.calendar.TimeZone;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.DataNotFoundException;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.exchange.ExchangeDocument;
import com.opengamma.master.exchange.ManageableExchange;

/**
 * Tests ModifyExchangeDbExchangeMasterWorker.
 */
public class ModifyExchangeDbExchangeMasterWorkerRemoveTest extends AbstractDbExchangeMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(ModifyExchangeDbExchangeMasterWorkerRemoveTest.class);

  private ModifyExchangeDbExchangeMasterWorker _worker;
  private DbExchangeMasterWorker _queryWorker;

  public ModifyExchangeDbExchangeMasterWorkerRemoveTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
    java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("UTC"));
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
  @Test(expected = DataNotFoundException.class)
  public void test_removeExchange_versioned_notFound() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbExg", "0", "0");
    _worker.remove(uid);
  }

  @Test
  public void test_remove_removed() {
    Instant now = Instant.now(_exgMaster.getTimeSource());
    
    UniqueIdentifier uid = UniqueIdentifier.of("DbExg", "101", "0");
    _worker.remove(uid);
    ExchangeDocument test = _queryWorker.get(uid);
    
    assertEquals(uid, test.getUniqueId());
    assertEquals(_version1Instant, test.getVersionFromInstant());
    assertEquals(now, test.getVersionToInstant());
    assertEquals(_version1Instant, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    ManageableExchange exchange = test.getExchange();
    assertNotNull(exchange);
    assertEquals(uid, exchange.getUniqueId());
    assertEquals("TestExchange101", test.getName());
    assertEquals(TimeZone.of("Europe/London"), exchange.getTimeZone());
    assertEquals(IdentifierBundle.of(Identifier.of("A", "B"), Identifier.of("C", "D"), Identifier.of("E", "F")), exchange.getIdentifiers());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    assertEquals(_worker.getClass().getSimpleName() + "[DbExg]", _worker.toString());
  }

}
