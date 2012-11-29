/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.volatility.surface.black.defaultproperties;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.id.UniqueId;
import com.opengamma.util.money.Currency;

/**
 *
 */
public class SoybeanFutureOptionBlackVolatilitySurfaceDefaults extends InstrumentSpecificBlackVolatilitySurfaceDefaults {

  public SoybeanFutureOptionBlackVolatilitySurfaceDefaults(final String... defaultsPerUid) {
    super(defaultsPerUid);
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getType() != ComputationTargetType.PRIMITIVE) {
      return false;
    }
    final UniqueId uniqueId = target.getUniqueId();
    if (Currency.OBJECT_SCHEME.equals(uniqueId.getScheme())) {
      final String uid = uniqueId.getValue();
      return getUids().contains(uid);
    }
    return false;
  }

}
