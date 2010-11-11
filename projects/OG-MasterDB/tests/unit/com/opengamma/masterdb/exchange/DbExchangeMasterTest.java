/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.exchange;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import javax.time.calendar.TimeZone;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;

import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.master.exchange.ExchangeDocument;
import com.opengamma.master.exchange.ManageableExchange;
import com.opengamma.masterdb.DbMasterTestUtils;
import com.opengamma.util.test.DBTest;

/**
 * Test DbExchangeMaster.
 */
public class DbExchangeMasterTest extends DBTest {

  private static final Logger s_logger = LoggerFactory.getLogger(DbExchangeMasterTest.class);

  private DbExchangeMaster _exgMaster;

  public DbExchangeMasterTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
    java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("UTC"));
  }

  @Before
  public void setUp() throws Exception {
    super.setUp();
    ConfigurableApplicationContext context = DbMasterTestUtils.getContext(getDatabaseType());
    _exgMaster = (DbExchangeMaster) context.getBean(getDatabaseType() + "DbExchangeMaster");
  }

  @After
  public void tearDown() throws Exception {
    super.tearDown();
    _exgMaster = null;
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_basics() throws Exception {
    assertNotNull(_exgMaster);
    assertEquals(true, _exgMaster.getIdentifierScheme().equals("DbExg"));
    assertNotNull(_exgMaster.getDbSource());
    assertNotNull(_exgMaster.getTimeSource());
    assertNotNull(_exgMaster.getWorkers());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_example() throws Exception {
    ManageableExchange exchange = new ManageableExchange();
    exchange.setIdentifiers(IdentifierBundle.of(Identifier.of("A", "B"), Identifier.of("C", "D")));
    exchange.setName("Test");
    exchange.setRegionId(IdentifierBundle.of(Identifier.of("E", "F"), Identifier.of("G", "H")));
    exchange.setTimeZone(TimeZone.of("Europe/London"));
    ExchangeDocument addDoc = new ExchangeDocument(exchange);
    ExchangeDocument added = _exgMaster.add(addDoc);
    
    ExchangeDocument loaded = _exgMaster.get(added.getExchangeId());
    assertEquals(added, loaded);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    assertEquals("DbExchangeMaster[DbExg]", _exgMaster.toString());
  }

}
