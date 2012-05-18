/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.generator;

import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.master.position.ManageableTrade;

/**
 * Source of random, but reasonable, FX forward security instances.
 */
public class FXForwardSecurityGenerator extends AbstractFXSecurityGenerator<FXForwardSecurity> {

  @Override
  public FXForwardSecurity createSecurity() {
    return createFXForwardSecurity(createBundle());
  }

  @Override
  public ManageableTrade createSecurityTrade(final QuantityGenerator quantity, final SecurityPersister persister, final NameGenerator counterPartyGenerator) {
    return createFXForwardSecurityTrade(createBundle(), quantity.createQuantity(), persister, counterPartyGenerator);
  }

}
