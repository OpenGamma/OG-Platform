/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.generator;

import java.math.BigDecimal;

import com.opengamma.core.position.impl.SimplePosition;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.master.position.ManageableTrade;

/**
 * Creates positions in swaps.
 */
public class SwapPositionGenerator extends SimplePositionGenerator<SwapSecurity> {

  public SwapPositionGenerator(final QuantityGenerator quantityGenerator, final AbstractSwapSecurityGenerator securityGenerator, final SecurityPersister securityPersister) {
    super(quantityGenerator, securityGenerator, securityPersister);
  }

  public SwapPositionGenerator(final AbstractSwapSecurityGenerator securityGenerator, final SecurityPersister securityPersister) {
    super(securityGenerator, securityPersister);
  }

  @Override
  protected void addTrades(final BigDecimal quantity, final SwapSecurity security, final SimplePosition position) {
    position.addTrade(new ManageableTrade(quantity, position.getSecurityLink().getExternalId(), security.getTradeDate().toLocalDate(), security.getTradeDate().toOffsetTime(), ExternalId.of("CParty",
        security.getCounterparty())));
  }

}
