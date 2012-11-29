/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.volatility.surface.black.defaultproperties;

import java.util.Set;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.id.UniqueId;
import com.opengamma.util.money.UnorderedCurrencyPair;

/**
 *
 */
public class FXBlackVolatilitySurfaceDefaults extends InstrumentSpecificBlackVolatilitySurfaceDefaults {

  public FXBlackVolatilitySurfaceDefaults(final String... defaultsPerUid) {
    super(defaultsPerUid);
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getType() != ComputationTargetType.PRIMITIVE) {
      return false;
    }
    final UniqueId uniqueId = target.getUniqueId();
    if (UnorderedCurrencyPair.OBJECT_SCHEME.equals(uniqueId.getScheme())) {
      final String uid = uniqueId.getValue();
      final Set<String> uids = getUids();
      if (uids.contains(uid)) {
        return true;
      }
      final String firstCcy = uid.substring(0, 3);
      final String secondCcy = uid.substring(3, 6);
      final String reversedCcys = secondCcy + firstCcy;
      return uids.contains(reversedCcys);
    }
    return false;
  }

}
