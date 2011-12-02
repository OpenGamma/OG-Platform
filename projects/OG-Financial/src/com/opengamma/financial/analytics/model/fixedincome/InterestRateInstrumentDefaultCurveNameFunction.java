/*
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.fixedincome;

import java.util.Collections;
import java.util.Set;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.financial.analytics.fixedincome.InterestRateInstrumentType;
import com.opengamma.financial.analytics.ircurve.YieldCurveFunction;
import com.opengamma.financial.property.DefaultPropertyFunction;
import com.opengamma.financial.security.FinancialSecurity;

/**
 * Dummy function for injecting default curve names into the dependency graph.
 */
public class InterestRateInstrumentDefaultCurveNameFunction extends DefaultPropertyFunction {

  private final String[] _valueNames;
  private final String _forwardCurve;
  private final String _fundingCurve;

  public InterestRateInstrumentDefaultCurveNameFunction(final String forwardCurve, final String fundingCurve, final String... valueNames) {
    super(ComputationTargetType.SECURITY, true);
    _forwardCurve = forwardCurve;
    _fundingCurve = fundingCurve;
    _valueNames = valueNames;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (!(target.getSecurity() instanceof FinancialSecurity)) {
      return false;
    }
    return InterestRateInstrumentType.isFixedIncomeInstrumentType((FinancialSecurity) target.getSecurity());
  }

  @Override
  protected void getDefaults(final PropertyDefaults defaults) {
    for (String valueName : _valueNames) {
      defaults.addValuePropertyName(valueName, YieldCurveFunction.PROPERTY_FORWARD_CURVE);
      defaults.addValuePropertyName(valueName, YieldCurveFunction.PROPERTY_FUNDING_CURVE);
    }
  }

  @Override
  protected Set<String> getDefaultValue(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue, final String propertyName) {
    if (YieldCurveFunction.PROPERTY_FORWARD_CURVE.equals(propertyName)) {
      return Collections.singleton(_forwardCurve);
    } else if (YieldCurveFunction.PROPERTY_FUNDING_CURVE.equals(propertyName)) {
      return Collections.singleton(_fundingCurve);
    } else {
      return null;
    }
  }

  @Override
  public PriorityClass getPriority() {
    if ("SECONDARY".equals(_forwardCurve) || "SECONDARY".equals(_fundingCurve)) {
      return PriorityClass.BELOW_NORMAL;
    } else {
      return super.getPriority();
    }
  }

}
