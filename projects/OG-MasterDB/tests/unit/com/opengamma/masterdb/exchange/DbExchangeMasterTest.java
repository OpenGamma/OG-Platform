/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.exchange;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import javax.time.calendar.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

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

  @Factory(dataProvider = "databases", dataProviderClass = DBTest.class)
  public DbExchangeMasterTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
    java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("UTC"));
  }

  @BeforeMethod
  public void setUp() throws Exception {
    super.setUp();
    ConfigurableApplicationContext context = DbMasterTestUtils.getContext(getDatabaseType());
    _exgMaster = (DbExchangeMaster) context.getBean(getDatabaseType() + "DbExchangeMaster");
  }

  @AfterMethod
  public void tearDown() throws Exception {
    _exgMaster = null;
    super.tearDown();
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_basics() throws Exception {
    assertNotNull(_exgMaster);
    assertEquals(true, _exgMaster.getIdentifierScheme().equals("DbExg"));
    assertNotNull(_exgMaster.getDbSource());
    assertNotNull(_exgMaster.getTimeSource());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_example() throws Exception {
    ManageableExchange exchange = new ManageableExchange();
    exchange.setIdentifiers(IdentifierBundle.of(Identifier.of("A", "B"), Identifier.of("C", "D")));
    exchange.setName("Test");
    exchange.setRegionKey(IdentifierBundle.of(Identifier.of("E", "F"), Identifier.of("G", "H")));
    exchange.setTimeZone(TimeZone.of("Europe/London"));
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
