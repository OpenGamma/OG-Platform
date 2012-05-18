/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.generator;

import com.opengamma.financial.security.option.FXBarrierOptionSecurity;
import com.opengamma.master.position.ManageableTrade;

/**
 * Source of random, but reasonable, FX barrier option security instances.
 */
public class FXBarrierOptionSecurityGenerator extends AbstractFXSecurityGenerator<FXBarrierOptionSecurity> {

  @Override
  public FXBarrierOptionSecurity createSecurity() {
    return createFXBarrierOptionSecurity(createBundle());
  }

  @Override
  public ManageableTrade createSecurityTrade(final QuantityGenerator quantity, final SecurityPersister persister, final NameGenerator counterPartyGenerator) {
    return createFXBarrierOptionSecurityTrade(createBundle(), quantity.createQuantity(), persister, counterPartyGenerator);
  }

}
