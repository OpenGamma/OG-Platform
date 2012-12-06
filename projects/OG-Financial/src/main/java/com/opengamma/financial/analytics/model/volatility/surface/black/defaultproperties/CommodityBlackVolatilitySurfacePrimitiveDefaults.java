/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.volatility.surface.black.defaultproperties;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.id.UniqueId;
import com.opengamma.util.money.Currency;

/**
 *
 */
public class CommodityBlackVolatilitySurfacePrimitiveDefaults extends CommodityBlackVolatilitySurfaceDefaults {
  private static final String[] VALUE_REQUIREMENTS = new String[] {
    ValueRequirementNames.BLACK_VOLATILITY_SURFACE,
    ValueRequirementNames.LOCAL_VOLATILITY_SURFACE,
  };

  public CommodityBlackVolatilitySurfacePrimitiveDefaults(final String... defaultsPerCurrency) {
    super(ComputationTargetType.PRIMITIVE, VALUE_REQUIREMENTS, defaultsPerCurrency);
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getType() != ComputationTargetType.PRIMITIVE) {
      return false;
    }
    final UniqueId uniqueId = target.getUniqueId();
    if (!Currency.OBJECT_SCHEME.equals(uniqueId.getScheme())) {
      return false;
    }
    return getAllCurrencies().contains(uniqueId.getValue());
  }

  @Override
  protected String getCurrency(final ComputationTarget target) {
    return target.getUniqueId().getValue();
  }

}
