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
public class ISDALegacyVanillaCDSDirtyPriceFunction extends LegacyVanillaCDSFunction {

  public ISDALegacyVanillaCDSDirtyPriceFunction() {
    super(ValueRequirementNames.DIRTY_PRICE);
  }

}
