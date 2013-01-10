/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.blotter;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import java.math.BigDecimal;

import javax.time.calendar.LocalDate;
import javax.time.calendar.OffsetTime;
import javax.time.calendar.Period;
import javax.time.calendar.ZoneOffset;

import org.testng.annotations.Test;

import com.google.common.collect.ImmutableMap;
import com.opengamma.financial.conversion.JodaBeanConverters;
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.position.PositionSearchRequest;
import com.opengamma.master.position.PositionSearchResult;
import com.opengamma.master.position.impl.InMemoryPositionMaster;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.master.security.impl.InMemorySecurityMaster;
import com.opengamma.util.money.Currency;

/**
 *
 */
public class OtcTradeBuilderTest {

  // TODO test that the URL ID is always unversioned and the trade ID is always versioned

  static {
    JodaBeanConverters.getInstance();
  }

  // TODO create trade with various fields missing (especially attributes)

  @Test
  public void newSecurityWithNoUnderlying() {
    SecurityMaster securityMaster = new InMemorySecurityMaster();
    PositionMaster positionMaster = new InMemoryPositionMaster();
    NewOtcTradeBuilder builder = new NewOtcTradeBuilder(securityMaster, positionMaster, BlotterResource.s_metaBeans);
    ImmutableMap<String, String> attributes = ImmutableMap.of("attr1", "val1", "attr2", "val2");
    BeanDataSource tradeData = BlotterTestUtils.beanData(
        "type", "OtcTrade",
        "counterparty", "testCpty",
        "tradeDate", "2012-12-21",
        "tradeTime", "10:00+00:00",
        "premium", "1234",
        "premiumCurrency", "GBP",
        "premiumDate", "2012-12-25",
        "premiumTime", "13:00+00:00",
        "attributes", attributes
    );
    UniqueId uniqueId = builder.buildAndSaveTrade(tradeData, BlotterTestUtils.FX_FORWARD_DATA_SOURCE, null);
    PositionSearchRequest searchRequest = new PositionSearchRequest();
    searchRequest.addTradeObjectId(uniqueId.getObjectId());
    PositionSearchResult searchResult = positionMaster.search(searchRequest);
    assertEquals(1, searchResult.getPositions().size());
    ManageablePosition position = searchResult.getFirstPosition();
    ManageableTrade trade = position.getTrade(uniqueId.getObjectId());
    assertEquals(BigDecimal.ONE, position.getQuantity());
    assertNotNull(trade);
    ManageableSecurity security = securityMaster.get(trade.getSecurityLink().getObjectId(),
                                                     VersionCorrection.LATEST).getSecurity();
    assertNotNull(security);
    security.setUniqueId(null); // so it can be tested for equality against the unsaved version
    assertEquals(BlotterTestUtils.FX_FORWARD, security);
    assertEquals(ExternalId.of("Cpty", "testCpty"), trade.getCounterpartyExternalId());
    assertEquals(1234d, trade.getPremium());
    assertEquals(Currency.GBP, trade.getPremiumCurrency());
    assertEquals(LocalDate.of(2012, 12, 25), trade.getPremiumDate());
    assertEquals(LocalDate.of(2012, 12, 21), trade.getTradeDate());
    assertEquals(OffsetTime.of(13, 0, ZoneOffset.of(Period.ZERO)), trade.getPremiumTime());
    assertEquals(OffsetTime.of(10, 0, ZoneOffset.of(Period.ZERO)), trade.getTradeTime());
    assertEquals(attributes, trade.getAttributes());
  }

  @Test
  public void newSecurityWithFungibleUnderlying() {

  }

  @Test
  public void newSecurityWithOtcUnderlying() {

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
