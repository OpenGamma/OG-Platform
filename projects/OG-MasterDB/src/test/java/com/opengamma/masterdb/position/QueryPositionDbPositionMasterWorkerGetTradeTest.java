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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.OffsetTime;

import com.opengamma.DataNotFoundException;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.position.PositionDocument;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.DbTest;
import com.opengamma.util.test.TestGroup;

/**
 * Tests QueryPositionDbPositionMasterWorker.
 */
@Test(groups = TestGroup.UNIT_DB)
public class QueryPositionDbPositionMasterWorkerGetTradeTest extends AbstractDbPositionMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(QueryPositionDbPositionMasterWorkerGetTradeTest.class);

  @Factory(dataProvider = "databases", dataProviderClass = DbTest.class)
  public QueryPositionDbPositionMasterWorkerGetTradeTest(final String databaseType, final String databaseVersion) {
    super(databaseType, databaseVersion, true);
    s_logger.info("running testcases for {}", databaseType);
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_getTrade_nullUID() {
    _posMaster.get((UniqueId)null);
  }

  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_getTrade_versioned_notFound() {
    final UniqueId uniqueId = UniqueId.of("DbPos", "0", "0");
    _posMaster.get(uniqueId);
  }

  @Test
  public void test_getTradePosition_versioned_404() {
    final UniqueId uniqueId = UniqueId.of("DbPos", "404", "0");
    final ManageableTrade test = _posMaster.getTrade(uniqueId);

    final ExternalIdBundle secKey = ExternalIdBundle.of(ExternalId.of("NASDAQ", "ORCL135"), ExternalId.of("TICKER", "ORCL134"));
    final ManageableTrade expected = new ManageableTrade(BigDecimal.valueOf(100.987), secKey, _now.toLocalDate(), _now.toOffsetTime().minusSeconds(404), ExternalId.of("CPARTY", "C104"));
    expected.setUniqueId(uniqueId);
    expected.setProviderId(ExternalId.of("B", "404"));
    expected.setParentPositionId(UniqueId.of("DbPos", "123", "0"));
    assertEquals(expected, test);
  }

  @Test
  public void test_getTradePosition_versioned_405() {
    final UniqueId uniqueId = UniqueId.of("DbPos", "405", "0");
    final ManageableTrade test = _posMaster.getTrade(uniqueId);

    final ExternalIdBundle secKey = ExternalIdBundle.of(ExternalId.of("NASDAQ", "ORCL135"), ExternalId.of("TICKER", "ORCL134"));
    final ManageableTrade expected = new ManageableTrade(BigDecimal.valueOf(200.987), secKey, _now.toLocalDate(), _now.toOffsetTime().minusSeconds(405), ExternalId.of("CPARTY", "C105"));
    expected.setUniqueId(uniqueId);
    expected.setProviderId(ExternalId.of("B", "405"));
    expected.setParentPositionId(UniqueId.of("DbPos", "123", "0"));
    assertEquals(expected, test);
  }

  @Test
  public void test_getTradePosition_versioned_notLatest() {
    final UniqueId uniqueId = UniqueId.of("DbPos", "407", "0");
    final ManageableTrade test = _posMaster.getTrade(uniqueId);

    final ExternalIdBundle secKey = ExternalIdBundle.of("TICKER", "IBMC");
    final ManageableTrade expected = new ManageableTrade(BigDecimal.valueOf(221.987), secKey, _now.toLocalDate(), _now.toOffsetTime().minusSeconds(407), ExternalId.of("CPARTY", "C221"));
    expected.setUniqueId(uniqueId);
    expected.setProviderId(ExternalId.of("B", "407"));
    expected.setParentPositionId(UniqueId.of("DbPos", "221", "0"));
    assertEquals(expected, test);
  }

  @Test
  public void test_getTradePosition_versioned_latest() {
    final UniqueId uniqueId = UniqueId.of("DbPos", "407", "1");
    final ManageableTrade test = _posMaster.getTrade(uniqueId);

    final ExternalIdBundle secKey = ExternalIdBundle.of("TICKER", "IBMC");
    final ManageableTrade expected = new ManageableTrade(BigDecimal.valueOf(222.987), secKey, _now.toLocalDate(), _now.toOffsetTime().minusSeconds(408), ExternalId.of("CPARTY", "C222"));
    expected.setUniqueId(uniqueId);
    expected.setProviderId(ExternalId.of("B", "408"));
    expected.setParentPositionId(UniqueId.of("DbPos", "221", "1"));
    assertEquals(expected, test);
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_getTradePosition_unversioned_notFound() {
    final UniqueId uniqueId = UniqueId.of("DbPos", "0");
    _posMaster.get(uniqueId);
  }

  @Test
  public void test_getTradePosition_unversioned() {
    final UniqueId oid = UniqueId.of("DbPos", "407");
    final ManageableTrade test = _posMaster.getTrade(oid);

    final ExternalIdBundle secKey = ExternalIdBundle.of("TICKER", "IBMC");
    final ManageableTrade expected = new ManageableTrade(BigDecimal.valueOf(222.987), secKey, _now.toLocalDate(), _now.toOffsetTime().minusSeconds(408), ExternalId.of("CPARTY", "C222"));
    expected.setUniqueId(UniqueId.of("DbPos", "407", "1"));
    expected.setProviderId(ExternalId.of("B", "408"));
    expected.setParentPositionId(UniqueId.of("DbPos", "221", "1"));
    assertEquals(expected, test);
  }

  @Test
  public void test_getTradePosition_withPremium() {
    final ManageablePosition position = new ManageablePosition(BigDecimal.TEN, ExternalId.of("A", "B"));

    final LocalDate tradeDate = _now.toLocalDate();
    final OffsetTime tradeTime = _now.toOffsetTime().minusSeconds(500);

    final ManageableTrade trade1 = new ManageableTrade(BigDecimal.TEN, ExternalId.of("A", "B"), tradeDate, tradeTime, ExternalId.of("CPS", "CPV"));
    trade1.setPremium(1000000.00);
    trade1.setPremiumCurrency(Currency.USD);
    trade1.setPremiumDate(tradeDate.plusDays(1));
    trade1.setPremiumTime(tradeTime);
    position.getTrades().add(trade1);

    final ManageableTrade trade2 = new ManageableTrade(BigDecimal.TEN, ExternalId.of("C", "D"), tradeDate, tradeTime, ExternalId.of("CPS2", "CPV2"));
    trade2.setPremium(100.00);
    trade2.setPremiumCurrency(Currency.GBP);
    trade2.setPremiumDate(tradeDate.plusDays(10));
    trade2.setPremiumTime(tradeTime.plusHours(1));
    position.getTrades().add(trade2);

    final PositionDocument doc = new PositionDocument();
    doc.setPosition(position);
    _posMaster.add(doc);
    assertNotNull(trade1.getUniqueId());
    assertNotNull(trade2.getUniqueId());

    assertEquals(trade1, _posMaster.getTrade(trade1.getUniqueId()));
    assertEquals(trade2, _posMaster.getTrade(trade2.getUniqueId()));

    final PositionDocument storedDoc = _posMaster.get(position.getUniqueId());
    assertNotNull(storedDoc);
    assertNotNull(storedDoc.getPosition());
    assertNotNull(storedDoc.getPosition().getTrades());
    assertEquals(2, storedDoc.getPosition().getTrades().size());
    assertTrue(storedDoc.getPosition().getTrades().contains(trade1));
    assertTrue(storedDoc.getPosition().getTrades().contains(trade2));
  }

  @Test
  public void test_getTradePosition_withAttributes() {
    final ManageablePosition position = new ManageablePosition(BigDecimal.TEN, ExternalId.of("A", "B"));

    final LocalDate tradeDate = _now.toLocalDate();
    final OffsetTime tradeTime = _now.toOffsetTime().minusSeconds(500);

    final ManageableTrade trade1 = new ManageableTrade(BigDecimal.TEN, ExternalId.of("A", "B"), tradeDate, tradeTime, ExternalId.of("CPS", "CPV"));
    trade1.addAttribute("key11", "Value11");
    trade1.addAttribute("key12", "Value12");
    position.getTrades().add(trade1);

    final ManageableTrade trade2 = new ManageableTrade(BigDecimal.TEN, ExternalId.of("C", "D"), tradeDate, tradeTime, ExternalId.of("CPS2", "CPV2"));
    trade2.addAttribute("key21", "Value21");
    trade2.addAttribute("key22", "Value22");
    position.getTrades().add(trade2);

    final PositionDocument doc = new PositionDocument();
    doc.setPosition(position);
    _posMaster.add(doc);
    assertNotNull(trade1.getUniqueId());
    assertNotNull(trade2.getUniqueId());

    assertEquals(trade1, _posMaster.getTrade(trade1.getUniqueId()));
    assertEquals(trade2, _posMaster.getTrade(trade2.getUniqueId()));

    final PositionDocument storedDoc = _posMaster.get(position.getUniqueId());
    assertNotNull(storedDoc);
    assertNotNull(storedDoc.getPosition());
    assertNotNull(storedDoc.getPosition().getTrades());
    assertEquals(2, storedDoc.getPosition().getTrades().size());
    assertTrue(storedDoc.getPosition().getTrades().contains(trade1));
    assertTrue(storedDoc.getPosition().getTrades().contains(trade2));
  }

}
