/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.currency.CurrencyPair;

/**
 *
 */
/* package */ class SpotRateUtils {

  private SpotRateUtils() {
  }

  /* package */ static CurrencyPair getCurrencyPair(ValueSpecification valueSpec) {
    ComputationTargetType targetType = valueSpec.getTargetSpecification().getType();
    String idValue = valueSpec.getTargetSpecification().getUniqueId().getValue();
    if (targetType.equals(CurrencyPair.TYPE)) {
      return CurrencyPair.parse(idValue);
    /*} else if (targetType.equals(ComputationTargetType.UNORDERED_CURRENCY_PAIR)) {
      String quotedPair = valueSpec.getProperties().getStrictValue(ConventionBasedFXRateFunction.QUOTING_CONVENTION_PROPERTY);
      return CurrencyPair.parse(quotedPair);*/
    } else {
      throw new IllegalArgumentException("Only currency pair target types supported. type=" + targetType);
    }
  }
}
