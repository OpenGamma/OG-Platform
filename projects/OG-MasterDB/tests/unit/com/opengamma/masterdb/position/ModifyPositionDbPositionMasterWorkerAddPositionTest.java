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
import java.util.TimeZone;

import javax.time.Instant;
import javax.time.calendar.LocalDate;
import javax.time.calendar.OffsetDateTime;
import javax.time.calendar.OffsetTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.position.PositionDocument;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.DBTest;

/**
 * Tests ModifyPositionDbPositionMasterWorker.
 */
public class ModifyPositionDbPositionMasterWorkerAddPositionTest extends AbstractDbPositionMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(ModifyPositionDbPositionMasterWorkerAddPositionTest.class);

  @Factory(dataProvider = "databases", dataProviderClass = DBTest.class)
  public ModifyPositionDbPositionMasterWorkerAddPositionTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_add_nullDocument() {
    _posMaster.add(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_add_noPosition() {
    PositionDocument doc = new PositionDocument();
    _posMaster.add(doc);
  }

  @Test
  public void test_add_add() {
    Instant now = Instant.now(_posMaster.getTimeSource());
    
    ManageablePosition position = new ManageablePosition(BigDecimal.TEN, Identifier.of("A", "B"));
    PositionDocument doc = new PositionDocument();
    doc.setPosition(position);
    PositionDocument test = _posMaster.add(doc);
    
    UniqueId uniqueId = test.getUniqueId();
    assertNotNull(uniqueId);
    assertEquals("DbPos", uniqueId.getScheme());
    assertTrue(uniqueId.isVersioned());
    assertTrue(Long.parseLong(uniqueId.getValue()) >= 1000);
    assertEquals("0", uniqueId.getVersion());
    assertEquals(now, test.getVersionFromInstant());
    assertEquals(null, test.getVersionToInstant());
    assertEquals(now, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    ManageablePosition testPosition = test.getPosition();
    assertNotNull(testPosition);
    assertEquals(uniqueId, testPosition.getUniqueId());
    assertEquals(BigDecimal.TEN, testPosition.getQuantity());
    IdentifierBundle secKey = testPosition.getSecurityLink().getBundleId();
    assertEquals(1, secKey.size());
    assertTrue(secKey.getIdentifiers().contains(Identifier.of("A", "B")));
  }
  
  @Test
  public void test_addWithOneTrade_add() {
    
    LocalDate tradeDate = _now.toLocalDate();
    OffsetTime tradeTime = _now.toOffsetTime().minusSeconds(500);
    
    ManageablePosition position = new ManageablePosition(BigDecimal.TEN, Identifier.of("A", "B"));
    position.getTrades().add(new ManageableTrade(BigDecimal.TEN, Identifier.of("A", "B"), tradeDate, tradeTime, Identifier.of("CPS", "CPV")));
    
    PositionDocument doc = new PositionDocument();
    doc.setPosition(position);
    PositionDocument test = _posMaster.add(doc);
    
    Instant now = Instant.now(_posMaster.getTimeSource());
    UniqueId uniqueId = test.getUniqueId();
    assertNotNull(uniqueId);
    assertEquals("DbPos", uniqueId.getScheme());
    assertTrue(uniqueId.isVersioned());
    assertTrue(Long.parseLong(uniqueId.getValue()) >= 1000);
    assertEquals("0", uniqueId.getVersion());
    assertEquals(now, test.getVersionFromInstant());
    assertEquals(null, test.getVersionToInstant());
    assertEquals(now, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    ManageablePosition testPosition = test.getPosition();
    assertNotNull(testPosition);
    assertEquals(uniqueId, testPosition.getUniqueId());
    assertEquals(BigDecimal.TEN, testPosition.getQuantity());
    IdentifierBundle secKey = testPosition.getSecurityLink().getBundleId();
    assertEquals(1, secKey.size());
    assertTrue(secKey.getIdentifiers().contains(Identifier.of("A", "B")));
    
    assertNotNull(testPosition.getTrades());
    assertEquals(1, testPosition.getTrades().size());
    ManageableTrade testTrade = testPosition.getTrades().get(0);
    assertNotNull(testTrade);
    assertEquals(BigDecimal.TEN, testTrade.getQuantity());
    assertEquals(tradeDate, testTrade.getTradeDate());
    assertEquals(tradeTime, testTrade.getTradeTime());
    assertEquals(Identifier.of("CPS", "CPV"), testTrade.getCounterpartyKey());
    assertEquals(secKey, testTrade.getSecurityLink().getBundleId());
  }

  @Test
  public void test_addWithOnePremiumTrade_add() {
    
    LocalDate tradeDate = _now.toLocalDate();
    OffsetTime tradeTime = _now.toOffsetTime().minusSeconds(500);
    
    ManageablePosition position = new ManageablePosition(BigDecimal.TEN, Identifier.of("A", "B"));
    ManageableTrade trade = new ManageableTrade(BigDecimal.TEN, Identifier.of("A", "B"), tradeDate, tradeTime, Identifier.of("CPS", "CPV"));
    trade.setPremium(1000000.00);
    trade.setPremiumCurrency(Currency.USD);
    trade.setPremiumDate(tradeDate.plusDays(1));
    trade.setPremiumTime(tradeTime);
    position.getTrades().add(trade);
    
    PositionDocument doc = new PositionDocument();
    doc.setPosition(position);
    PositionDocument test = _posMaster.add(doc);
    
    Instant now = Instant.now(_posMaster.getTimeSource());
    UniqueId uniqueId = test.getUniqueId();
    assertNotNull(uniqueId);
    assertEquals("DbPos", uniqueId.getScheme());
    assertTrue(uniqueId.isVersioned());
    assertTrue(Long.parseLong(uniqueId.getValue()) >= 1000);
    assertEquals("0", uniqueId.getVersion());
    assertEquals(now, test.getVersionFromInstant());
    assertEquals(null, test.getVersionToInstant());
    assertEquals(now, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    ManageablePosition testPosition = test.getPosition();
    assertNotNull(testPosition);
    assertEquals(uniqueId, testPosition.getUniqueId());
    assertEquals(BigDecimal.TEN, testPosition.getQuantity());
    IdentifierBundle secKey = testPosition.getSecurityLink().getBundleId();
    assertEquals(1, secKey.size());
    assertTrue(secKey.getIdentifiers().contains(Identifier.of("A", "B")));
    
    assertNotNull(testPosition.getTrades());
    assertEquals(1, testPosition.getTrades().size());
    ManageableTrade testTrade = testPosition.getTrades().get(0);
    assertNotNull(testTrade);
    assertEquals(BigDecimal.TEN, testTrade.getQuantity());
    assertEquals(tradeDate, testTrade.getTradeDate());
    assertEquals(tradeTime, testTrade.getTradeTime());
    assertEquals(Identifier.of("CPS", "CPV"), testTrade.getCounterpartyKey());
    assertEquals(secKey, testTrade.getSecurityLink().getBundleId());
    assertEquals(1000000.00, testTrade.getPremium());
    assertEquals(Currency.USD, testTrade.getPremiumCurrency());
    assertEquals(tradeDate.plusDays(1), testTrade.getPremiumDate());
    assertEquals(tradeTime, testTrade.getPremiumTime());
  }

  @Test
  public void test_addWithOnePremiumTrade_addThenGet() {
    ManageablePosition position = new ManageablePosition(BigDecimal.TEN, Identifier.of("A", "B"));
    
    LocalDate tradeDate = _now.toLocalDate();
    OffsetTime tradeTime = _now.toOffsetTime().minusSeconds(500);
    
    ManageableTrade trade = new ManageableTrade(BigDecimal.TEN, Identifier.of("A", "B"), tradeDate, tradeTime, Identifier.of("CPS", "CPV"));
    trade.setPremium(1000000.00);
    trade.setPremiumCurrency(Currency.USD);
    trade.setPremiumDate(tradeDate.plusDays(1));
    trade.setPremiumTime(tradeTime);
    
    position.getTrades().add(trade);
    
    PositionDocument doc = new PositionDocument();
    doc.setPosition(position);
    PositionDocument added = _posMaster.add(doc);
    
    PositionDocument test = _posMaster.get(added.getUniqueId());
        
    assertEquals(added, test);
  }

  @Test
  public void test_addWithTwoTrades_add() {
    Instant now = Instant.now(_posMaster.getTimeSource());
    
    OffsetDateTime offsetDateTime = OffsetDateTime.now();
    
    ManageablePosition position = new ManageablePosition(BigDecimal.TEN, Identifier.of("A", "B"));
    position.getTrades().add(new ManageableTrade(BigDecimal.TEN, Identifier.of("A", "C"), offsetDateTime.toLocalDate(), offsetDateTime.minusSeconds(600).toOffsetTime(), Identifier.of("CPS", "CPV")));
    position.getTrades().add(new ManageableTrade(BigDecimal.TEN, Identifier.of("A", "D"), offsetDateTime.toLocalDate(), offsetDateTime.minusSeconds(500).toOffsetTime(), Identifier.of("CPS", "CPV")));
    
    PositionDocument doc = new PositionDocument();
    doc.setPosition(position);
    PositionDocument test = _posMaster.add(doc);
    
    UniqueId portfolioId = test.getUniqueId();
    assertNotNull(portfolioId);
    assertEquals("DbPos", portfolioId.getScheme());
    assertTrue(portfolioId.isVersioned());
    assertTrue(Long.parseLong(portfolioId.getValue()) >= 1000);
    assertEquals("0", portfolioId.getVersion());
    assertEquals(now, test.getVersionFromInstant());
    assertEquals(null, test.getVersionToInstant());
    assertEquals(now, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    ManageablePosition testPosition = test.getPosition();
    assertNotNull(testPosition);
    assertEquals(portfolioId, testPosition.getUniqueId());
    assertEquals(BigDecimal.TEN, testPosition.getQuantity());
    IdentifierBundle secKey = testPosition.getSecurityLink().getBundleId();
    assertEquals(1, secKey.size());
    assertTrue(secKey.getIdentifiers().contains(Identifier.of("A", "B")));
    
    assertNotNull(testPosition.getTrades());
    assertTrue(testPosition.getTrades().size() == 2);
    for (ManageableTrade testTrade : testPosition.getTrades()) {
      assertNotNull(testTrade);
      UniqueId tradeId = testTrade.getUniqueId();
      assertNotNull(tradeId);
      assertEquals("DbPos", portfolioId.getScheme());
      assertTrue(portfolioId.isVersioned());
      assertTrue(Long.parseLong(portfolioId.getValue()) >= 1000);
      assertEquals("0", portfolioId.getVersion());
      assertEquals(portfolioId, testTrade.getParentPositionId());
    }
  }

  @Test
  public void test_add_addThenGet() {
    ManageablePosition position = new ManageablePosition(BigDecimal.TEN, Identifier.of("A", "B"));
    PositionDocument doc = new PositionDocument();
    doc.setPosition(position);
    PositionDocument added = _posMaster.add(doc);
    
    PositionDocument test = _posMaster.get(added.getUniqueId());
    assertEquals(added, test);
  }
  
  @Test
  public void test_addWithTradesAndAttributes_addThenGet() {
    ManageablePosition position = new ManageablePosition(BigDecimal.TEN, Identifier.of("A", "B"));
    position.addAttribute("PA1", "A");
    position.addAttribute("PA2", "B");
    
    LocalDate tradeDate = _now.toLocalDate();
    OffsetTime tradeTime = _now.toOffsetTime().minusSeconds(500);
    ManageableTrade trade1 = new ManageableTrade(BigDecimal.TEN, Identifier.of("A", "B"), tradeDate, tradeTime, Identifier.of("CPS", "CPV"));
    trade1.addAttribute("TA11", "C");
    trade1.addAttribute("TA12", "D");
    trade1.addAttribute("TA13", "E");
    position.getTrades().add(trade1);
    
    ManageableTrade trade2 = new ManageableTrade(BigDecimal.ONE, Identifier.of("C", "D"), tradeDate, tradeTime, Identifier.of("CPS", "CPV"));
    trade2.addAttribute("TA21", "F");
    trade2.addAttribute("TA22", "G");
    trade2.addAttribute("TA23", "H");
    trade2.addAttribute("TA24", "I");
    position.getTrades().add(trade2);
    
    PositionDocument doc = new PositionDocument();
    doc.setPosition(position);
    PositionDocument added = _posMaster.add(doc);
    
    PositionDocument test = _posMaster.get(added.getUniqueId());
    assertEquals(added, test);
  }

  @Test
  public void test_addWithOneTrade_addThenGet() {
    ManageablePosition position = new ManageablePosition(BigDecimal.TEN, Identifier.of("A", "B"));
    
    LocalDate tradeDate = _now.toLocalDate();
    OffsetTime tradeTime = _now.toOffsetTime().minusSeconds(500);
    
    position.getTrades().add(new ManageableTrade(BigDecimal.TEN, Identifier.of("A", "B"), tradeDate, tradeTime, Identifier.of("CPS", "CPV")));
    
    PositionDocument doc = new PositionDocument();
    doc.setPosition(position);
    PositionDocument added = _posMaster.add(doc);
    
    PositionDocument test = _posMaster.get(added.getUniqueId());
        
    assertEquals(added, test);
  }

  @Test
  public void test_addWithTwoTrades_addThenGet() {
    ManageablePosition position = new ManageablePosition(BigDecimal.valueOf(20), Identifier.of("A", "B"));
    
    OffsetDateTime offsetDateTime = OffsetDateTime.now();
    
    position.getTrades().add(new ManageableTrade(BigDecimal.TEN, Identifier.of("A", "B"), offsetDateTime.toLocalDate(), offsetDateTime.minusSeconds(600).toOffsetTime(), Identifier.of("CPS", "CPV")));
    position.getTrades().add(new ManageableTrade(BigDecimal.TEN, Identifier.of("A", "C"), offsetDateTime.toLocalDate(), offsetDateTime.minusSeconds(500).toOffsetTime(), Identifier.of("CPS", "CPV")));
    
    PositionDocument doc = new PositionDocument();
    doc.setPosition(position);
    PositionDocument added = _posMaster.add(doc);
    
    PositionDocument test = _posMaster.get(added.getUniqueId());
    assertEquals(added, test);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    assertEquals(_posMaster.getClass().getSimpleName() + "[DbPos]", _posMaster.toString());
  }

}
