/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity.option;

import java.util.Collections;

import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.value.ValueRenamingFunction;

/**
 * Renames the {@link ValueRequirementNames#IMPLIED_VOLATILITY} result to
 * {@link ValueRequirementNames#SECURITY_IMPLIED_VOLATILITY}.
 */
public class ImpliedVolatilityRenamingFunction extends ValueRenamingFunction {

  /**
   * Sets the value to rename to {@link ValueRequirementNames#IMPLIED_VOLATILITY}, the new
   * result name to {@link ValueRequirementNames#SECURITY_IMPLIED_VOLATILITY} and the target
   * to be either a security or trade.
   */
  public ImpliedVolatilityRenamingFunction() {
    super(Collections.singleton(ValueRequirementNames.IMPLIED_VOLATILITY), ValueRequirementNames.SECURITY_IMPLIED_VOLATILITY,
        ComputationTargetType.SECURITY.or(ComputationTargetType.TRADE));
  }
}
