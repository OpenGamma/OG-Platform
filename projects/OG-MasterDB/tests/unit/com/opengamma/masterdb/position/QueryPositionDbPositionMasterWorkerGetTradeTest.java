/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.position;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.math.BigDecimal;

import javax.time.calendar.LocalDate;
import javax.time.calendar.OffsetTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.opengamma.DataNotFoundException;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.position.PositionDocument;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.DbTest;

/**
 * Tests QueryPositionDbPositionMasterWorker.
 */
public class QueryPositionDbPositionMasterWorkerGetTradeTest extends AbstractDbPositionMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(QueryPositionDbPositionMasterWorkerGetTradeTest.class);

  @Factory(dataProvider = "databases", dataProviderClass = DbTest.class)
  public QueryPositionDbPositionMasterWorkerGetTradeTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion, true);
    s_logger.info("running testcases for {}", databaseType);
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_getTrade_nullUID() {
    _posMaster.get(null);
  }

  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_getTrade_versioned_notFound() {
    UniqueId uniqueId = UniqueId.of("DbPos", "0", "0");
    _posMaster.get(uniqueId);
  }

  @Test
  public void test_getTradePosition_versioned_404() {
    UniqueId uniqueId = UniqueId.of("DbPos", "404", "0");
    ManageableTrade test = _posMaster.getTrade(uniqueId);
    
    ExternalIdBundle secKey = ExternalIdBundle.of(ExternalId.of("NASDAQ", "ORCL135"), ExternalId.of("TICKER", "ORCL134"));
    ManageableTrade expected = new ManageableTrade(BigDecimal.valueOf(100.987), secKey, _now.toLocalDate(), _now.toOffsetTime().minusSeconds(404), ExternalId.of("CPARTY", "C104"));
    expected.setParentPositionId(UniqueId.of("DbPos", "123", "0"));
    expected.setUniqueId(uniqueId);
    expected.setProviderId(ExternalId.of("B", "404"));
    assertEquals(expected, test);
  }

  @Test
  public void test_getTradePosition_versioned_405() {
    UniqueId uniqueId = UniqueId.of("DbPos", "405", "0");
    ManageableTrade test = _posMaster.getTrade(uniqueId);
    
    ExternalIdBundle secKey = ExternalIdBundle.of(ExternalId.of("NASDAQ", "ORCL135"), ExternalId.of("TICKER", "ORCL134"));
    ManageableTrade expected = new ManageableTrade(BigDecimal.valueOf(200.987), secKey, _now.toLocalDate(), _now.toOffsetTime().minusSeconds(405), ExternalId.of("CPARTY", "C105"));
    expected.setParentPositionId(UniqueId.of("DbPos", "123", "0"));
    expected.setUniqueId(uniqueId);
    expected.setProviderId(ExternalId.of("B", "405"));
    assertEquals(expected, test);
  }

  @Test
  public void test_getTradePosition_versioned_notLatest() {
    UniqueId uniqueId = UniqueId.of("DbPos", "407", "0");
    ManageableTrade test = _posMaster.getTrade(uniqueId);
    
    ExternalIdBundle secKey = ExternalIdBundle.of("TICKER", "IBMC");
    ManageableTrade expected = new ManageableTrade(BigDecimal.valueOf(221.987), secKey, _now.toLocalDate(), _now.toOffsetTime().minusSeconds(407), ExternalId.of("CPARTY", "C221"));
    expected.setParentPositionId(UniqueId.of("DbPos", "221", "0"));
    expected.setUniqueId(uniqueId);
    expected.setProviderId(ExternalId.of("B", "407"));
    assertEquals(expected, test);
  }

  @Test
  public void test_getTradePosition_versioned_latest() {
    UniqueId uniqueId = UniqueId.of("DbPos", "407", "1");
    ManageableTrade test = _posMaster.getTrade(uniqueId);
    
    ExternalIdBundle secKey = ExternalIdBundle.of("TICKER", "IBMC");
    ManageableTrade expected = new ManageableTrade(BigDecimal.valueOf(222.987), secKey, _now.toLocalDate(), _now.toOffsetTime().minusSeconds(408), ExternalId.of("CPARTY", "C222"));
    expected.setParentPositionId(UniqueId.of("DbPos", "221", "1"));
    expected.setUniqueId(uniqueId);
    expected.setProviderId(ExternalId.of("B", "408"));
    assertEquals(expected, test);
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_getTradePosition_unversioned_notFound() {
    UniqueId uniqueId = UniqueId.of("DbPos", "0");
    _posMaster.get(uniqueId);
  }

  @Test
  public void test_getTradePosition_unversioned() {
    UniqueId oid = UniqueId.of("DbPos", "407");
    ManageableTrade test = _posMaster.getTrade(oid);
    
    ExternalIdBundle secKey = ExternalIdBundle.of("TICKER", "IBMC");
    ManageableTrade expected = new ManageableTrade(BigDecimal.valueOf(222.987), secKey, _now.toLocalDate(), _now.toOffsetTime().minusSeconds(408), ExternalId.of("CPARTY", "C222"));
    expected.setParentPositionId(UniqueId.of("DbPos", "221", "1"));
    expected.setUniqueId(UniqueId.of("DbPos", "407", "1"));
    expected.setProviderId(ExternalId.of("B", "408"));
    assertEquals(expected, test);
  }

  @Test
  public void test_getTradePosition_withPremium() {
    ManageablePosition position = new ManageablePosition(BigDecimal.TEN, ExternalId.of("A", "B"));
    
    LocalDate tradeDate = _now.toLocalDate();
    OffsetTime tradeTime = _now.toOffsetTime().minusSeconds(500);
    
    ManageableTrade trade1 = new ManageableTrade(BigDecimal.TEN, ExternalId.of("A", "B"), tradeDate, tradeTime, ExternalId.of("CPS", "CPV"));
    trade1.setPremium(1000000.00);
    trade1.setPremiumCurrency(Currency.USD);
    trade1.setPremiumDate(tradeDate.plusDays(1));
    trade1.setPremiumTime(tradeTime);
    position.getTrades().add(trade1);
    
    ManageableTrade trade2 = new ManageableTrade(BigDecimal.TEN, ExternalId.of("C", "D"), tradeDate, tradeTime, ExternalId.of("CPS2", "CPV2"));
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
    ManageablePosition position = new ManageablePosition(BigDecimal.TEN, ExternalId.of("A", "B"));
    
    LocalDate tradeDate = _now.toLocalDate();
    OffsetTime tradeTime = _now.toOffsetTime().minusSeconds(500);
    
    ManageableTrade trade1 = new ManageableTrade(BigDecimal.TEN, ExternalId.of("A", "B"), tradeDate, tradeTime, ExternalId.of("CPS", "CPV"));
    trade1.addAttribute("key11", "Value11");
    trade1.addAttribute("key12", "Value12");
    position.getTrades().add(trade1);
    
    ManageableTrade trade2 = new ManageableTrade(BigDecimal.TEN, ExternalId.of("C", "D"), tradeDate, tradeTime, ExternalId.of("CPS2", "CPV2"));
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

}
