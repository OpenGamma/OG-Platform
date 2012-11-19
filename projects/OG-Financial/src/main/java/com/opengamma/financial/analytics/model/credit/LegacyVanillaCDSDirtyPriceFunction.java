/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.credit;

import com.opengamma.engine.value.ValueRequirementNames;

/**
 *
 */
public class LegacyVanillaCDSDirtyPriceFunction extends LegacyVanillaCDSFunction {

  public LegacyVanillaCDSDirtyPriceFunction() {
    super(ValueRequirementNames.DIRTY_PRICE);
  }

}
