/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.generator;

import java.math.BigDecimal;

import com.opengamma.core.position.impl.SimplePosition;
import com.opengamma.financial.security.bond.BondSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.master.position.ManageableTrade;

/**
 * Creates positions in bonds.
 */
public class BondPositionGenerator extends SimplePositionGenerator<BondSecurity> {

  public BondPositionGenerator(final QuantityGenerator quantityGenerator, final BondSecurityGenerator<?> securityGenerator, final SecurityPersister securityPersister) {
    super(quantityGenerator, securityGenerator, securityPersister);
  }

  public BondPositionGenerator(final BondSecurityGenerator<?> securityGenerator, final SecurityPersister securityPersister) {
    super(securityGenerator, securityPersister);
  }

  @Override
  protected void addTrades(final BigDecimal quantity, final BondSecurity security, final SimplePosition position) {
    position.addTrade(new ManageableTrade(quantity, position.getSecurityLink().getExternalId(), security.getSettlementDate().toLocalDate(), security.getSettlementDate().toOffsetTime(), ExternalId.of(
        "CParty", "OPENGAMMA")));
  }

}
