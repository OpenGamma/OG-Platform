/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.generator;

import com.opengamma.financial.security.option.FXOptionSecurity;
import com.opengamma.master.position.ManageableTrade;

/**
 * Source of random, but reasonable, FX barrier option security instances.
 */
public class FXOptionSecurityGenerator extends AbstractFXSecurityGenerator<FXOptionSecurity> {

  @Override
  public FXOptionSecurity createSecurity() {
    return createFXOptionSecurity(createBundle());
  }

  @Override
  public ManageableTrade createSecurityTrade(final QuantityGenerator quantity, final SecurityPersister persister, final NameGenerator counterPartyGenerator) {
    return createFXOptionSecurityTrade(createBundle(), quantity.createQuantity(), persister, counterPartyGenerator);
  }

}
