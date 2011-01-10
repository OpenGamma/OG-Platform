/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.exchange;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.TimeZone;

import javax.time.Instant;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.BadSqlGrammarException;

import com.opengamma.DataNotFoundException;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.exchange.ExchangeDocument;
import com.opengamma.master.exchange.ExchangeHistoryRequest;
import com.opengamma.master.exchange.ExchangeHistoryResult;
import com.opengamma.master.exchange.ManageableExchange;

/**
 * Tests ModifyExchangeDbExchangeMasterWorker.
 */
public class ModifyExchangeDbExchangeMasterWorkerUpdateTest extends AbstractDbExchangeMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(ModifyExchangeDbExchangeMasterWorkerUpdateTest.class);
  private static final IdentifierBundle BUNDLE = IdentifierBundle.of(Identifier.of("A", "B"));
  private static final IdentifierBundle REGION = IdentifierBundle.of(Identifier.of("C", "D"));

  private ModifyExchangeDbExchangeMasterWorker _worker;
  private DbExchangeMasterWorker _queryWorker;

  public ModifyExchangeDbExchangeMasterWorkerUpdateTest(String databaseType, String databaseVersion) {
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
  public void test_updateExchange_nullDocument() {
    _worker.update(null);
  }

  @Test(expected = NullPointerException.class)
  public void test_update_noExchangeId() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbExg", "101");
    ManageableExchange exchange = new ManageableExchange(BUNDLE, "Test", REGION, null);
    exchange.setUniqueId(uid);
    ExchangeDocument doc = new ExchangeDocument();
    doc.setExchange(exchange);
    _worker.update(doc);
  }

  @Test(expected = NullPointerException.class)
  public void test_update_noExchange() {
    ExchangeDocument doc = new ExchangeDocument();
    doc.setUniqueId(UniqueIdentifier.of("DbExg", "101", "0"));
    _worker.update(doc);
  }

  @Test(expected = DataNotFoundException.class)
  public void test_update_notFound() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbExg", "0", "0");
    ManageableExchange exchange = new ManageableExchange(BUNDLE, "Test", REGION, null);
    exchange.setUniqueId(uid);
    ExchangeDocument doc = new ExchangeDocument(exchange);
    _worker.update(doc);
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_update_notLatestVersion() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbExg", "201", "0");
    ManageableExchange exchange = new ManageableExchange(BUNDLE, "Test", REGION, null);
    exchange.setUniqueId(uid);
    ExchangeDocument doc = new ExchangeDocument(exchange);
    _worker.update(doc);
  }

  @Test
  public void test_update_getUpdateGet() {
    Instant now = Instant.now(_exgMaster.getTimeSource());
    
    UniqueIdentifier uid = UniqueIdentifier.of("DbExg", "101", "0");
    ExchangeDocument base = _queryWorker.get(uid);
    ManageableExchange exchange = new ManageableExchange(BUNDLE, "Test", REGION, null);
    exchange.setUniqueId(uid);
    ExchangeDocument input = new ExchangeDocument(exchange);
    
    ExchangeDocument updated = _worker.update(input);
    assertEquals(false, base.getUniqueId().equals(updated.getUniqueId()));
    assertEquals(now, updated.getVersionFromInstant());
    assertEquals(null, updated.getVersionToInstant());
    assertEquals(now, updated.getCorrectionFromInstant());
    assertEquals(null, updated.getCorrectionToInstant());
    assertEquals(input.getExchange(), updated.getExchange());
    
    ExchangeDocument old = _queryWorker.get(uid);
    assertEquals(base.getUniqueId(), old.getUniqueId());
    assertEquals(base.getVersionFromInstant(), old.getVersionFromInstant());
    assertEquals(now, old.getVersionToInstant());  // old version ended
    assertEquals(base.getCorrectionFromInstant(), old.getCorrectionFromInstant());
    assertEquals(base.getCorrectionToInstant(), old.getCorrectionToInstant());
    assertEquals(base.getExchange(), old.getExchange());
    
    ExchangeHistoryRequest search = new ExchangeHistoryRequest(base.getUniqueId(), null, now);
    ExchangeHistoryResult searchResult = _queryWorker.history(search);
    assertEquals(2, searchResult.getDocuments().size());
  }

  @Test
  public void test_update_rollback() {
    ModifyExchangeDbExchangeMasterWorker w = new ModifyExchangeDbExchangeMasterWorker() {
      @Override
      protected String sqlSelectIdKey() {
        return "SELECT";  // bad sql
      };
    };
    w.init(_exgMaster);
    final ExchangeDocument base = _queryWorker.get(UniqueIdentifier.of("DbExg", "101", "0"));
    UniqueIdentifier uid = UniqueIdentifier.of("DbExg", "101", "0");
    ManageableExchange exchange = new ManageableExchange(BUNDLE, "Test", REGION, null);
    exchange.setUniqueId(uid);
    ExchangeDocument input = new ExchangeDocument(exchange);
    try {
      w.update(input);
      fail();
    } catch (BadSqlGrammarException ex) {
      // expected
    }
    final ExchangeDocument test = _queryWorker.get(UniqueIdentifier.of("DbExg", "101", "0"));
    
    assertEquals(base, test);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    assertEquals(_worker.getClass().getSimpleName() + "[DbExg]", _worker.toString());
  }

}
