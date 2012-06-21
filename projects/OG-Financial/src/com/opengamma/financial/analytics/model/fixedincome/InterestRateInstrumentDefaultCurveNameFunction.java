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
import com.opengamma.financial.analytics.OpenGammaFunctionExclusions;
import com.opengamma.financial.analytics.fixedincome.InterestRateInstrumentType;
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
    ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES,
    ValueRequirementNames.VALUE_THETA};
  private final String _curveCalculationMethod;
  private final String _curveCalculationConfig;
  private final String _excludedSecurityName;
  private final PriorityClass _priority;
  private final String _applicableCurrencyName;

  public InterestRateInstrumentDefaultCurveNameFunction(final String curveCalculationMethod, final String curveCalculationConfig, final String priority, final String applicableCurrencyName) {
    this(curveCalculationMethod, curveCalculationConfig, priority, null, applicableCurrencyName);
  }

  public InterestRateInstrumentDefaultCurveNameFunction(final String curveCalculationMethod, final String curveCalculationConfig, final String priority, final String excludedSecurityName,
      final String applicableCurrencyName) {
    super(ComputationTargetType.SECURITY, true);
    ArgumentChecker.notNull(curveCalculationMethod, "curve calculation method");
    ArgumentChecker.notNull(curveCalculationConfig, "curve calculation config");
    ArgumentChecker.notNull(priority, "priority");
    ArgumentChecker.notNull(applicableCurrencyName, "applicable currency name");
    _curveCalculationMethod = curveCalculationMethod;
    _curveCalculationConfig = curveCalculationConfig;
    _priority = PriorityClass.valueOf(priority);
    _excludedSecurityName = excludedSecurityName;
    _applicableCurrencyName = applicableCurrencyName;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (!(target.getSecurity() instanceof FinancialSecurity)) {
      return false;
    }
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    if (_excludedSecurityName != null && security.getClass().getName().equals(_excludedSecurityName)) {
      return false;
    }
    if (security instanceof SwapSecurity) {
      final String currencyName = FinancialSecurityUtils.getCurrency(security).getCode();
      final InterestRateInstrumentType type = InterestRateInstrumentType.getInstrumentTypeFromSecurity(security);
      if (type == InterestRateInstrumentType.SWAP_FIXED_IBOR || type == InterestRateInstrumentType.SWAP_FIXED_IBOR_WITH_SPREAD
          || type == InterestRateInstrumentType.SWAP_IBOR_IBOR || type == InterestRateInstrumentType.SWAP_FIXED_OIS) {
        if (currencyName.equals(_applicableCurrencyName)) {
          return true;
        }
      }
    }
    if (InterestRateInstrumentType.isFixedIncomeInstrumentType(security)) {
      final String currencyName = FinancialSecurityUtils.getCurrency(security).getCode();
      if (currencyName.equals(_applicableCurrencyName)) {
        return true;
      }
    }
    return false;
  }

  @Override
  protected void getDefaults(final PropertyDefaults defaults) {
    for (final String valueName : s_valueNames) {
      defaults.addValuePropertyName(valueName, ValuePropertyNames.CURVE_CALCULATION_CONFIG);
      defaults.addValuePropertyName(valueName, ValuePropertyNames.CURVE_CALCULATION_METHOD);
    }
  }

  @Override
  protected Set<String> getDefaultValue(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue, final String propertyName) {
    if (ValuePropertyNames.CURVE_CALCULATION_CONFIG.equals(propertyName)) {
      return Collections.singleton(_curveCalculationConfig);
    }
    if (ValuePropertyNames.CURVE_CALCULATION_METHOD.equals(propertyName)) {
      return Collections.singleton(_curveCalculationMethod);
    }
    return null;
  }

  @Override
  public PriorityClass getPriority() {
    return _priority;
  }

  @Override
  public String getMutualExclusionGroup() {
    return OpenGammaFunctionExclusions.INTEREST_RATE_INSTRUMENT_DEFAULTS;
  }
}
