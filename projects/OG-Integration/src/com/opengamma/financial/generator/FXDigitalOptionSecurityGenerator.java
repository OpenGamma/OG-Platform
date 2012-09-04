/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.generator;

import com.opengamma.financial.security.option.FXDigitalOptionSecurity;
import com.opengamma.master.position.ManageableTrade;

/**
 * Source of random, but reasonable, FX digital option security instances.
 */
public class FXDigitalOptionSecurityGenerator extends AbstractFXSecurityGenerator<FXDigitalOptionSecurity> {

  @Override
  public FXDigitalOptionSecurity createSecurity() {
    return createFXDigitalOptionSecurity(createBundle());
  }

  @Override
  public ManageableTrade createSecurityTrade(final QuantityGenerator quantity, final SecurityPersister persister, final NameGenerator counterPartyGenerator) {
    return createFXDigitalOptionSecurityTrade(createBundle(), quantity.createQuantity(), persister, counterPartyGenerator);
  }

}
