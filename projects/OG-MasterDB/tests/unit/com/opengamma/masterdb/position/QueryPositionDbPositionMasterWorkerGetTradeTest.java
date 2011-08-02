/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.position;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.*;

import java.math.BigDecimal;
import java.util.TimeZone;

import javax.time.calendar.LocalDate;
import javax.time.calendar.OffsetTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.opengamma.DataNotFoundException;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.position.PositionDocument;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.DBTest;

/**
 * Tests QueryPositionDbPositionMasterWorker.
 */
public class QueryPositionDbPositionMasterWorkerGetTradeTest extends AbstractDbPositionMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(QueryPositionDbPositionMasterWorkerGetTradeTest.class);

  @Factory(dataProvider = "databases", dataProviderClass = DBTest.class)
  public QueryPositionDbPositionMasterWorkerGetTradeTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_getTrade_nullUID() {
    _posMaster.get(null);
  }

  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_getTrade_versioned_notFound() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbPos", "0", "0");
    _posMaster.get(uid);
  }

  @Test
  public void test_getTradePosition_versioned_404() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbPos", "404", "0");
    ManageableTrade test = _posMaster.getTrade(uid);
    
    IdentifierBundle secKey = IdentifierBundle.of(Identifier.of("NASDAQ", "ORCL135"), Identifier.of("TICKER", "ORCL134"));
    ManageableTrade expected = new ManageableTrade(BigDecimal.valueOf(100.987), secKey, _now.toLocalDate(), _now.toOffsetTime().minusSeconds(404), Identifier.of("CPARTY", "C104"));
    expected.setParentPositionId(UniqueIdentifier.of("DbPos", "123", "0"));
    expected.setUniqueId(uid);
    expected.setProviderKey(Identifier.of("B", "404"));
    assertEquals(expected, test);
  }

  @Test
  public void test_getTradePosition_versioned_405() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbPos", "405", "0");
    ManageableTrade test = _posMaster.getTrade(uid);
    
    IdentifierBundle secKey = IdentifierBundle.of(Identifier.of("NASDAQ", "ORCL135"), Identifier.of("TICKER", "ORCL134"));
    ManageableTrade expected = new ManageableTrade(BigDecimal.valueOf(200.987), secKey, _now.toLocalDate(), _now.toOffsetTime().minusSeconds(405), Identifier.of("CPARTY", "C105"));
    expected.setParentPositionId(UniqueIdentifier.of("DbPos", "123", "0"));
    expected.setUniqueId(uid);
    expected.setProviderKey(Identifier.of("B", "405"));
    assertEquals(expected, test);
  }

  @Test
  public void test_getTradePosition_versioned_notLatest() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbPos", "407", "0");
    ManageableTrade test = _posMaster.getTrade(uid);
    
    IdentifierBundle secKey = IdentifierBundle.of("TICKER", "IBMC");
    ManageableTrade expected = new ManageableTrade(BigDecimal.valueOf(221.987), secKey, _now.toLocalDate(), _now.toOffsetTime().minusSeconds(407), Identifier.of("CPARTY", "C221"));
    expected.setParentPositionId(UniqueIdentifier.of("DbPos", "221", "0"));
    expected.setUniqueId(uid);
    expected.setProviderKey(Identifier.of("B", "407"));
    assertEquals(expected, test);
  }

  @Test
  public void test_getTradePosition_versioned_latest() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbPos", "407", "1");
    ManageableTrade test = _posMaster.getTrade(uid);
    
    IdentifierBundle secKey = IdentifierBundle.of("TICKER", "IBMC");
    ManageableTrade expected = new ManageableTrade(BigDecimal.valueOf(222.987), secKey, _now.toLocalDate(), _now.toOffsetTime().minusSeconds(408), Identifier.of("CPARTY", "C222"));
    expected.setParentPositionId(UniqueIdentifier.of("DbPos", "221", "1"));
    expected.setUniqueId(uid);
    expected.setProviderKey(Identifier.of("B", "408"));
    assertEquals(expected, test);
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_getTradePosition_unversioned_notFound() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbPos", "0");
    _posMaster.get(uid);
  }

  @Test
  public void test_getTradePosition_unversioned() {
    UniqueIdentifier oid = UniqueIdentifier.of("DbPos", "407");
    ManageableTrade test = _posMaster.getTrade(oid);
    
    IdentifierBundle secKey = IdentifierBundle.of("TICKER", "IBMC");
    ManageableTrade expected = new ManageableTrade(BigDecimal.valueOf(222.987), secKey, _now.toLocalDate(), _now.toOffsetTime().minusSeconds(408), Identifier.of("CPARTY", "C222"));
    expected.setParentPositionId(UniqueIdentifier.of("DbPos", "221", "1"));
    expected.setUniqueId(UniqueIdentifier.of("DbPos", "407", "1"));
    expected.setProviderKey(Identifier.of("B", "408"));
    assertEquals(expected, test);
  }

  @Test
  public void test_getTradePosition_withPremium() {
    ManageablePosition position = new ManageablePosition(BigDecimal.TEN, Identifier.of("A", "B"));
    
    LocalDate tradeDate = _now.toLocalDate();
    OffsetTime tradeTime = _now.toOffsetTime().minusSeconds(500);
    
    ManageableTrade trade1 = new ManageableTrade(BigDecimal.TEN, Identifier.of("A", "B"), tradeDate, tradeTime, Identifier.of("CPS", "CPV"));
    trade1.setPremium(1000000.00);
    trade1.setPremiumCurrency(Currency.USD);
    trade1.setPremiumDate(tradeDate.plusDays(1));
    trade1.setPremiumTime(tradeTime);
    position.getTrades().add(trade1);
    
    ManageableTrade trade2 = new ManageableTrade(BigDecimal.TEN, Identifier.of("C", "D"), tradeDate, tradeTime, Identifier.of("CPS2", "CPV2"));
    trade2.setPremium(100.00);
    trade2.setPremiumCurrency(Currency.GBP);
    trade2.setPremiumDate(tradeDate.plusDays(10));
    trade2.setPremiumTime(tradeTime.plusHours(1));
    position.getTrades().add(trade2);
    
    PositionDocument doc = new PositionDocument();
    doc.setPosition(position);
    _posMaster.add(doc);
    assertNotNull(trade1.getUniqueId());
    assertNotNull(trade2.getUniqueId());
   
    assertEquals(trade1, _posMaster.getTrade(trade1.getUniqueId()));
    assertEquals(trade2, _posMaster.getTrade(trade2.getUniqueId()));
    
    PositionDocument storedDoc = _posMaster.get(position.getUniqueId());
    assertNotNull(storedDoc);
    assertNotNull(storedDoc.getPosition());
    assertNotNull(storedDoc.getPosition().getTrades());
    assertEquals(2, storedDoc.getPosition().getTrades().size());
    assertTrue(storedDoc.getPosition().getTrades().contains(trade1));
    assertTrue(storedDoc.getPosition().getTrades().contains(trade2));
  }

  @Test
  public void test_getTradePosition_withAttributes() {
    ManageablePosition position = new ManageablePosition(BigDecimal.TEN, Identifier.of("A", "B"));
    
    LocalDate tradeDate = _now.toLocalDate();
    OffsetTime tradeTime = _now.toOffsetTime().minusSeconds(500);
    
    ManageableTrade trade1 = new ManageableTrade(BigDecimal.TEN, Identifier.of("A", "B"), tradeDate, tradeTime, Identifier.of("CPS", "CPV"));
    trade1.addAttribute("key11", "Value11");
    trade1.addAttribute("key12", "Value12");
    position.getTrades().add(trade1);
    
    ManageableTrade trade2 = new ManageableTrade(BigDecimal.TEN, Identifier.of("C", "D"), tradeDate, tradeTime, Identifier.of("CPS2", "CPV2"));
    trade2.addAttribute("key21", "Value21");
    trade2.addAttribute("key22", "Value22");
    position.getTrades().add(trade2);
    
    PositionDocument doc = new PositionDocument();
    doc.setPosition(position);
    _posMaster.add(doc);
    assertNotNull(trade1.getUniqueId());
    assertNotNull(trade2.getUniqueId());
   
    assertEquals(trade1, _posMaster.getTrade(trade1.getUniqueId()));
    assertEquals(trade2, _posMaster.getTrade(trade2.getUniqueId()));
    
    PositionDocument storedDoc = _posMaster.get(position.getUniqueId());
    assertNotNull(storedDoc);
    assertNotNull(storedDoc.getPosition());
    assertNotNull(storedDoc.getPosition().getTrades());
    assertEquals(2, storedDoc.getPosition().getTrades().size());
    assertTrue(storedDoc.getPosition().getTrades().contains(trade1));
    assertTrue(storedDoc.getPosition().getTrades().contains(trade2));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    assertEquals(_posMaster.getClass().getSimpleName() + "[DbPos]", _posMaster.toString());
  }

}
