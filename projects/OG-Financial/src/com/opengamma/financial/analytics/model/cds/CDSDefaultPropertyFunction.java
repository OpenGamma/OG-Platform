/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.cds;

import java.util.Set;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.financial.property.DefaultPropertyFunction;

/**
 * @see InterestRateInstrumentDefaultPropertiesFunction
 */
public class CDSDefaultPropertyFunction extends DefaultPropertyFunction {

  protected CDSDefaultPropertyFunction(ComputationTargetType targetType, boolean permitWithout) {
    super(targetType, permitWithout);
  }

  @Override
  protected void getDefaults(PropertyDefaults defaults) {
  }

  @Override
  protected Set<String> getDefaultValue(FunctionCompilationContext context, ComputationTarget target, ValueRequirement desiredValue, String propertyName) {
    return null;
  }

}
