/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.exchange;

import static org.junit.Assert.assertEquals;

import java.util.TimeZone;

import javax.time.Instant;

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
import com.opengamma.master.exchange.ExchangeHistoryRequest;
import com.opengamma.master.exchange.ExchangeHistoryResult;
import com.opengamma.master.exchange.ManageableExchange;

/**
 * Tests ModifyExchangeDbExchangeMasterWorker.
 */
public class ModifyExchangeDbExchangeMasterWorkerCorrectTest extends AbstractDbExchangeMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(ModifyExchangeDbExchangeMasterWorkerCorrectTest.class);
  private static final IdentifierBundle BUNDLE = IdentifierBundle.of(Identifier.of("A", "B"));
  private static final IdentifierBundle REGION = IdentifierBundle.of(Identifier.of("C", "D"));

  private ModifyExchangeDbExchangeMasterWorker _worker;
  private DbExchangeMasterWorker _queryWorker;

  public ModifyExchangeDbExchangeMasterWorkerCorrectTest(String databaseType, String databaseVersion) {
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
  public void test_correctExchange_nullDocument() {
    _worker.correct(null);
  }

  @Test(expected = NullPointerException.class)
  public void test_correct_noExchangeId() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbExg", "101");
    ManageableExchange exchange = new ManageableExchange(BUNDLE, "Test", REGION, null);
    exchange.setUniqueIdentifier(uid);
    ExchangeDocument doc = new ExchangeDocument(exchange);
    doc.setExchangeId(null);
    _worker.correct(doc);
  }

  @Test(expected = NullPointerException.class)
  public void test_correct_noExchange() {
    ExchangeDocument doc = new ExchangeDocument();
    doc.setExchangeId(UniqueIdentifier.of("DbExg", "101", "0"));
    _worker.correct(doc);
  }

  @Test(expected = DataNotFoundException.class)
  public void test_correct_notFound() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbExg", "0", "0");
    ManageableExchange exchange = new ManageableExchange(BUNDLE, "Test", REGION, null);
    exchange.setUniqueIdentifier(uid);
    ExchangeDocument doc = new ExchangeDocument(exchange);
    _worker.correct(doc);
  }

//  @Test(expected = IllegalArgumentException.class)
//  public void test_correct_notLatestCorrection() {
//    UniqueIdentifier uid = UniqueIdentifier.of("DbExg", "201", "0");
//    ManageableExchange exchange = new ManageableExchange(uid, "Name", "Type", IdentifierBundle.of(Identifier.of("A", "B")));
//    ExchangeDocument doc = new ExchangeDocument(exchange);
//    _worker.correct(doc);
//  }

  @Test
  public void test_correct_getUpdateGet() {
    Instant now = Instant.now(_exgMaster.getTimeSource());
    
    UniqueIdentifier uid = UniqueIdentifier.of("DbExg", "101", "0");
    ExchangeDocument base = _queryWorker.get(uid);
    ManageableExchange exchange = new ManageableExchange(BUNDLE, "Test", REGION, null);
    exchange.setUniqueIdentifier(uid);
    ExchangeDocument input = new ExchangeDocument(exchange);
    
    ExchangeDocument corrected = _worker.correct(input);
    assertEquals(false, base.getExchangeId().equals(corrected.getExchangeId()));
    assertEquals(base.getVersionFromInstant(), corrected.getVersionFromInstant());
    assertEquals(base.getVersionToInstant(), corrected.getVersionToInstant());
    assertEquals(now, corrected.getCorrectionFromInstant());
    assertEquals(null, corrected.getCorrectionToInstant());
    assertEquals(input.getExchange(), corrected.getExchange());
    
    ExchangeDocument old = _queryWorker.get(UniqueIdentifier.of("DbExg", "101", "0"));
    assertEquals(base.getExchangeId(), old.getExchangeId());
    assertEquals(base.getVersionFromInstant(), old.getVersionFromInstant());
    assertEquals(base.getVersionToInstant(), old.getVersionToInstant());
    assertEquals(base.getCorrectionFromInstant(), old.getCorrectionFromInstant());
    assertEquals(now, old.getCorrectionToInstant());  // old version ended
    assertEquals(base.getExchange(), old.getExchange());
    
    ExchangeHistoryRequest search = new ExchangeHistoryRequest(base.getExchangeId(), now, null);
    ExchangeHistoryResult searchResult = _queryWorker.history(search);
    assertEquals(2, searchResult.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    assertEquals(_worker.getClass().getSimpleName() + "[DbExg]", _worker.toString());
  }

}
