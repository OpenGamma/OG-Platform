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
import org.threeten.bp.Instant;
import org.threeten.bp.ZoneId;

import com.opengamma.DataNotFoundException;
import com.opengamma.id.ExternalId;
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
public class ModifyExchangeDbExchangeMasterWorkerRemoveTest extends AbstractDbExchangeMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(ModifyExchangeDbExchangeMasterWorkerRemoveTest.class);

  @Factory(dataProvider = "databases", dataProviderClass = DbTest.class)
  public ModifyExchangeDbExchangeMasterWorkerRemoveTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion, false);
    s_logger.info("running testcases for {}", databaseType);
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_removeExchange_versioned_notFound() {
    UniqueId uniqueId = UniqueId.of("DbExg", "0", "0");
    _exgMaster.remove(uniqueId);
  }

  @Test
  public void test_remove_removed() {
    Instant now = Instant.now(_exgMaster.getClock());
    
    UniqueId uniqueId = UniqueId.of("DbExg", "101", "0");
    _exgMaster.remove(uniqueId);
    ExchangeDocument test = _exgMaster.get(uniqueId);
    
    assertEquals(uniqueId, test.getUniqueId());
    assertEquals(_version1Instant, test.getVersionFromInstant());
    assertEquals(now, test.getVersionToInstant());
    assertEquals(_version1Instant, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    ManageableExchange exchange = test.getExchange();
    assertNotNull(exchange);
    assertEquals(uniqueId, exchange.getUniqueId());
    assertEquals("TestExchange101", test.getName());
    assertEquals(ZoneId.of("Europe/London"), exchange.getTimeZone());
    assertEquals(ExternalIdBundle.of(ExternalId.of("A", "B"), ExternalId.of("C", "D"), ExternalId.of("E", "F")), exchange.getExternalIdBundle());
  }

}
