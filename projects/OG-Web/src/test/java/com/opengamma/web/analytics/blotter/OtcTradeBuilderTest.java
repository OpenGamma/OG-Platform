/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.blotter;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import java.math.BigDecimal;

import org.apache.commons.lang.ArrayUtils;
import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalTime;
import org.threeten.bp.OffsetTime;
import org.threeten.bp.ZoneOffset;

import com.google.common.collect.ImmutableMap;
import com.opengamma.financial.conversion.JodaBeanConverters;
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.position.impl.InMemoryPositionMaster;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.master.security.impl.InMemorySecurityMaster;
import com.opengamma.util.money.Currency;

/**
 *
 */
public class OtcTradeBuilderTest {

  private static final ImmutableMap<String,String> ATTRIBUTES = ImmutableMap.of("attr1", "val1", "attr2", "val2");
  private static final OffsetTime PREMIUM_TIME = LocalTime.of(13, 0).atOffset(ZoneOffset.UTC);
  private static final OffsetTime TRADE_TIME = LocalTime.of(10, 0).atOffset(ZoneOffset.UTC);
  private static final LocalDate PREMIUM_DATE = LocalDate.of(2012, 12, 25);
  private static final LocalDate TRADE_DATE = LocalDate.of(2012, 12, 21);
  private static final double PREMIUM = 1234d;
  private static final ExternalId COUNTERPARTY_ID = ExternalId.of("Cpty", "testCpty");

  // TODO test that the URL ID is always unversioned and the trade ID is always versioned
  // TODO what happens if an existing trade's security is changed?

  static {
    JodaBeanConverters.getInstance();
  }

  // TODO create trade with various fields missing (especially attributes)

  // TODO move to BlotterTestUtils?
  private static BeanDataSource createTradeData(Object... valuePairs) {
    Object[] basicData = {
        "type", "OtcTrade",
        "counterparty", "testCpty",
        "tradeDate", "2012-12-21",
        "tradeTime", "10:00+00:00",
        "premium", "1234",
        "premiumCurrency", "GBP",
        "premiumDate", "2012-12-25",
        "premiumTime", "13:00+00:00",
        "attributes", ATTRIBUTES};
    Object[] tradeData = ArrayUtils.addAll(basicData, valuePairs);
    return BlotterTestUtils.beanData(tradeData);
  }

  @Test
  public void newSecurityWithNoUnderlying() {
    SecurityMaster securityMaster = new InMemorySecurityMaster();
    PositionMaster positionMaster = new InMemoryPositionMaster();
    NewOtcTradeBuilder builder = new NewOtcTradeBuilder(securityMaster,
                                                        positionMaster,
                                                        BlotterResource.s_metaBeans,
                                                        BlotterResource.getStringConvert());
    UniqueId tradeId = builder.buildAndSaveTrade(createTradeData(), BlotterTestUtils.FX_FORWARD_DATA_SOURCE, null);
    ManageableTrade trade = positionMaster.getTrade(tradeId);
    UniqueId positionId = trade.getParentPositionId();
    ManageablePosition position = positionMaster.get(positionId).getPosition();
    assertEquals(BigDecimal.ONE, trade.getQuantity());
    assertEquals(BigDecimal.ONE, position.getQuantity());
    ManageableSecurity security = securityMaster.get(trade.getSecurityLink().getObjectId(),
                                                     VersionCorrection.LATEST).getSecurity();
    assertNotNull(security);
    security.setUniqueId(null); // so it can be tested for equality against the unsaved version
    assertEquals(BlotterTestUtils.FX_FORWARD, security);
    assertEquals(COUNTERPARTY_ID, trade.getCounterpartyExternalId());
    assertEquals(PREMIUM, trade.getPremium());
    assertEquals(Currency.GBP, trade.getPremiumCurrency());
    assertEquals(PREMIUM_DATE, trade.getPremiumDate());
    assertEquals(TRADE_DATE, trade.getTradeDate());
    assertEquals(PREMIUM_TIME, trade.getPremiumTime());
    assertEquals(TRADE_TIME, trade.getTradeTime());
    assertEquals(ATTRIBUTES, trade.getAttributes());
  }

  @Test
  public void newSecurityWithFungibleUnderlying() {
    SecurityMaster securityMaster = new InMemorySecurityMaster();
    PositionMaster positionMaster = new InMemoryPositionMaster();
    NewOtcTradeBuilder builder = new NewOtcTradeBuilder(securityMaster,
                                                        positionMaster,
                                                        BlotterResource.s_metaBeans,
                                                        BlotterResource.getStringConvert());
    BeanDataSource tradeData = createTradeData();
    UniqueId tradeId = builder.buildAndSaveTrade(tradeData, BlotterTestUtils.EQUITY_VARIANCE_SWAP_DATA_SOURCE, null);

    ManageableTrade trade = positionMaster.getTrade(tradeId);
    UniqueId positionId = trade.getParentPositionId();
    ManageablePosition position = positionMaster.get(positionId).getPosition();
    assertEquals(BigDecimal.ONE, position.getQuantity());
    ManageableSecurity security =
        securityMaster.get(trade.getSecurityLink().getObjectId(), VersionCorrection.LATEST).getSecurity();
    assertNotNull(security);
    security.setUniqueId(null); // so it can be tested for equality against the unsaved version
    assertEquals(BlotterTestUtils.EQUITY_VARIANCE_SWAP, security);

    assertEquals(COUNTERPARTY_ID, trade.getCounterpartyExternalId());
    assertEquals(PREMIUM, trade.getPremium());
    assertEquals(Currency.GBP, trade.getPremiumCurrency());
    assertEquals(PREMIUM_DATE, trade.getPremiumDate());
    assertEquals(TRADE_DATE, trade.getTradeDate());
    assertEquals(PREMIUM_TIME, trade.getPremiumTime());
    assertEquals(TRADE_TIME, trade.getTradeTime());
    assertEquals(ATTRIBUTES, trade.getAttributes());
    assertEquals(position.getUniqueId(), trade.getParentPositionId());
  }

  @Test
  public void newSecurityWithOtcUnderlying() {
    // TODO i.e. a swaption
  }

  @Test
  public void existingSecurityWithNoUnderlying() {

  }

  @Test
  public void existingSecurityWithFungibleUnderlying() {

  }

  @Test
  public void existingSecurityWithOtcUnderlying() {

  }
}
