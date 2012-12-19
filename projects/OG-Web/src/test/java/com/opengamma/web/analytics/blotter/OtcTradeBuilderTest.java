/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.blotter;

import static org.mockito.Mockito.mock;

import org.testng.annotations.Test;

import com.opengamma.financial.conversion.JodaBeanConverters;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.security.SecurityMaster;

/**
 *
 */
public class OtcTradeBuilderTest {

  static {
    JodaBeanConverters.getInstance();
  }

  @Test
  public void newSecurityWithNoUnderlying() {
    SecurityMaster securityMaster = mock(SecurityMaster.class);
    PositionMaster positionMaster = mock(PositionMaster.class);
    NewOtcTradeBuilder builder = new NewOtcTradeBuilder(securityMaster, positionMaster, BlotterResource.s_metaBeans);
    BeanDataSource tradeData;
    BeanDataSource securityData;
    //builder.buildTrade(tradeData, securityData, null);
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
