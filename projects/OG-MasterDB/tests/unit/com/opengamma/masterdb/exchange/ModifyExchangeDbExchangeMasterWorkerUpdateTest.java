/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.exchange;

import static org.testng.AssertJUnit.assertEquals;

import java.util.TimeZone;

import javax.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.BadSqlGrammarException;
import org.testng.Assert;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.opengamma.DataNotFoundException;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.exchange.ExchangeDocument;
import com.opengamma.master.exchange.ExchangeHistoryRequest;
import com.opengamma.master.exchange.ExchangeHistoryResult;
import com.opengamma.master.exchange.ManageableExchange;
import com.opengamma.util.test.DBTest;

/**
 * Tests ModifyExchangeDbExchangeMasterWorker.
 */
public class ModifyExchangeDbExchangeMasterWorkerUpdateTest extends AbstractDbExchangeMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(ModifyExchangeDbExchangeMasterWorkerUpdateTest.class);
  private static final IdentifierBundle BUNDLE = IdentifierBundle.of("A", "B");
  private static final IdentifierBundle REGION = IdentifierBundle.of("C", "D");

  @Factory(dataProvider = "databases", dataProviderClass = DBTest.class)
  public ModifyExchangeDbExchangeMasterWorkerUpdateTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_updateExchange_nullDocument() {
    _exgMaster.update(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_update_noExchangeId() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbExg", "101");
    ManageableExchange exchange = new ManageableExchange(BUNDLE, "Test", REGION, null);
    exchange.setUniqueId(uid);
    ExchangeDocument doc = new ExchangeDocument();
    doc.setExchange(exchange);
    _exgMaster.update(doc);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_update_noExchange() {
    ExchangeDocument doc = new ExchangeDocument();
    doc.setUniqueId(UniqueIdentifier.of("DbExg", "101", "0"));
    _exgMaster.update(doc);
  }

  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_update_notFound() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbExg", "0", "0");
    ManageableExchange exchange = new ManageableExchange(BUNDLE, "Test", REGION, null);
    exchange.setUniqueId(uid);
    ExchangeDocument doc = new ExchangeDocument(exchange);
    _exgMaster.update(doc);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_update_notLatestVersion() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbExg", "201", "0");
    ManageableExchange exchange = new ManageableExchange(BUNDLE, "Test", REGION, null);
    exchange.setUniqueId(uid);
    ExchangeDocument doc = new ExchangeDocument(exchange);
    _exgMaster.update(doc);
  }

  @Test
  public void test_update_getUpdateGet() {
    Instant now = Instant.now(_exgMaster.getTimeSource());
    
    UniqueIdentifier uid = UniqueIdentifier.of("DbExg", "101", "0");
    ExchangeDocument base = _exgMaster.get(uid);
    ManageableExchange exchange = new ManageableExchange(BUNDLE, "Test", REGION, null);
    exchange.setUniqueId(uid);
    ExchangeDocument input = new ExchangeDocument(exchange);
    
    ExchangeDocument updated = _exgMaster.update(input);
    assertEquals(false, base.getUniqueId().equals(updated.getUniqueId()));
    assertEquals(now, updated.getVersionFromInstant());
    assertEquals(null, updated.getVersionToInstant());
    assertEquals(now, updated.getCorrectionFromInstant());
    assertEquals(null, updated.getCorrectionToInstant());
    assertEquals(input.getExchange(), updated.getExchange());
    
    ExchangeDocument old = _exgMaster.get(uid);
    assertEquals(base.getUniqueId(), old.getUniqueId());
    assertEquals(base.getVersionFromInstant(), old.getVersionFromInstant());
    assertEquals(now, old.getVersionToInstant());  // old version ended
    assertEquals(base.getCorrectionFromInstant(), old.getCorrectionFromInstant());
    assertEquals(base.getCorrectionToInstant(), old.getCorrectionToInstant());
    assertEquals(base.getExchange(), old.getExchange());
    
    ExchangeHistoryRequest search = new ExchangeHistoryRequest(base.getUniqueId(), null, now);
    ExchangeHistoryResult searchResult = _exgMaster.history(search);
    assertEquals(2, searchResult.getDocuments().size());
  }

  @Test
  public void test_update_rollback() {
    DbExchangeMaster w = new DbExchangeMaster(_exgMaster.getDbSource()) {
      @Override
      protected String sqlSelectIdKey() {
        return "SELECT";  // bad sql
      };
    };
    final ExchangeDocument base = _exgMaster.get(UniqueIdentifier.of("DbExg", "101", "0"));
    UniqueIdentifier uid = UniqueIdentifier.of("DbExg", "101", "0");
    ManageableExchange exchange = new ManageableExchange(BUNDLE, "Test", REGION, null);
    exchange.setUniqueId(uid);
    ExchangeDocument input = new ExchangeDocument(exchange);
    try {
      w.update(input);
      Assert.fail();
    } catch (BadSqlGrammarException ex) {
      // expected
    }
    final ExchangeDocument test = _exgMaster.get(UniqueIdentifier.of("DbExg", "101", "0"));
    
    assertEquals(base, test);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    assertEquals(_exgMaster.getClass().getSimpleName() + "[DbExg]", _exgMaster.toString());
  }

}
