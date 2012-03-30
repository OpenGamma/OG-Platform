/**
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
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.analytics.fixedincome.InterestRateInstrumentType;
import com.opengamma.financial.analytics.ircurve.YieldCurveFunction;
import com.opengamma.financial.property.DefaultPropertyFunction;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.util.ArgumentChecker;

/**
 * Dummy function for injecting default curve names into the dependency graph.
 */
public class InterestRateInstrumentDefaultCurveNameFunction extends DefaultPropertyFunction {
  private static final String[] s_valueNames = new String[] {
    ValueRequirementNames.PRESENT_VALUE,
    ValueRequirementNames.PAR_RATE,
    ValueRequirementNames.PAR_RATE_CURVE_SENSITIVITY,
    ValueRequirementNames.PAR_RATE_PARALLEL_CURVE_SHIFT,
    ValueRequirementNames.PV01,
    ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES};
  private final String _curveCalculationMethod;
  private final String _forwardCurve;
  private final String _fundingCurve;
  private final String[] _applicableCurrencyNames;

  public InterestRateInstrumentDefaultCurveNameFunction(final String curveCalculationMethod, final String forwardCurve, final String fundingCurve, final String... applicableCurrencyNames) {
    super(ComputationTargetType.SECURITY, true);
    ArgumentChecker.notNull(curveCalculationMethod, "curve calculation method");
    ArgumentChecker.notNull(forwardCurve, "forward curve name");
    ArgumentChecker.notNull(fundingCurve, "funding curve name");
    ArgumentChecker.notNull(applicableCurrencyNames, "applicable currency names list");
    ArgumentChecker.notEmpty(applicableCurrencyNames, "applicable currency names list");
    _curveCalculationMethod = curveCalculationMethod;
    _forwardCurve = forwardCurve;
    _fundingCurve = fundingCurve;
    _applicableCurrencyNames = applicableCurrencyNames;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (!(target.getSecurity() instanceof FinancialSecurity)) {
      return false;
    }
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    if (security instanceof SwapSecurity) {
      final String currencyName = FinancialSecurityUtils.getCurrency(security).getCode();
      final InterestRateInstrumentType type = InterestRateInstrumentType.getInstrumentTypeFromSecurity(security);
      if (type == InterestRateInstrumentType.SWAP_FIXED_IBOR || type == InterestRateInstrumentType.SWAP_FIXED_IBOR_WITH_SPREAD
            || type == InterestRateInstrumentType.SWAP_IBOR_IBOR || type == InterestRateInstrumentType.SWAP_FIXED_OIS) {
        for (final String applicableCurrencyName : _applicableCurrencyNames) {
          if (currencyName.equals(applicableCurrencyName)) {
            return true;
          }
        }        
      }
    }
    if (InterestRateInstrumentType.isFixedIncomeInstrumentType(security)) {      
      final String currencyName = FinancialSecurityUtils.getCurrency(security).getCode();
      for (final String applicableCurrencyName : _applicableCurrencyNames) {
        if (currencyName.equals(applicableCurrencyName)) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  protected void getDefaults(final PropertyDefaults defaults) {
    for (final String valueName : s_valueNames) {
      defaults.addValuePropertyName(valueName, YieldCurveFunction.PROPERTY_FORWARD_CURVE);
      defaults.addValuePropertyName(valueName, YieldCurveFunction.PROPERTY_FUNDING_CURVE);
      defaults.addValuePropertyName(valueName, ValuePropertyNames.CURVE_CALCULATION_METHOD);
    }
  }

  @Override
  protected Set<String> getDefaultValue(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue, final String propertyName) {
    if (YieldCurveFunction.PROPERTY_FORWARD_CURVE.equals(propertyName)) {
      return Collections.singleton(_forwardCurve);
    }
    if (YieldCurveFunction.PROPERTY_FUNDING_CURVE.equals(propertyName)) {
      return Collections.singleton(_fundingCurve);
    }
    if (ValuePropertyNames.CURVE_CALCULATION_METHOD.equals(propertyName)) {
      return Collections.singleton(_curveCalculationMethod);
    }
    return null;
  }

  @Override
  public PriorityClass getPriority() {
    if ("SECONDARY".equals(_forwardCurve) || "SECONDARY".equals(_fundingCurve)) {
      return PriorityClass.BELOW_NORMAL;
    }
    return super.getPriority();
  }

}
