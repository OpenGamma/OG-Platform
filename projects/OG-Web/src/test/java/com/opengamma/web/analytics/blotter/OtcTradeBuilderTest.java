/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.blotter;

import org.testng.annotations.Test;

import com.google.common.collect.ImmutableMap;
import com.opengamma.financial.conversion.JodaBeanConverters;
import com.opengamma.id.UniqueId;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.position.impl.InMemoryPositionMaster;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.master.security.impl.InMemorySecurityMaster;

/**
 *
 */
public class OtcTradeBuilderTest {

  static {
    JodaBeanConverters.getInstance();
  }

  // TODO create trade with various fields missing (especially attributes)

  @Test
  public void newSecurityWithNoUnderlying() {
    SecurityMaster securityMaster = new InMemorySecurityMaster();
    PositionMaster positionMaster = new InMemoryPositionMaster();
    NewOtcTradeBuilder builder = new NewOtcTradeBuilder(securityMaster, positionMaster, BlotterResource.s_metaBeans);
    BeanDataSource tradeData = BlotterTestUtils.beanData(
        "counterparty", "testCpty",
        "tradeDate", "2012-12-21",
        "tradeTime", "10:00+00:00",
        "premium", "1234",
        "premiumCurrency", "GBP",
        "premiumDate", "2012-12-25",
        "premiumTime", "13:00+00:00",
        "attributes", ImmutableMap.of("attr1", "val1", "attr2", "val2")
    );
    UniqueId uniqueId = builder.buildAndSaveTrade(tradeData, BlotterTestUtils.FX_FORWARD_DATA_SOURCE, null);
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
