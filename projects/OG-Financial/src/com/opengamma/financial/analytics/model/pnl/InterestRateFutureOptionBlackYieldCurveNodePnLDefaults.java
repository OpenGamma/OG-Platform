/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.pnl;

import java.util.Collections;
import java.util.Set;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.security.Security;
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
import com.opengamma.financial.security.fx.FXUtils;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class InterestRateFutureOptionBlackYieldCurveNodePnLDefaults extends DefaultPropertyFunction {
  private final String _curveCalculationConfig;
  private final String _surfaceName;
  private final String _samplingPeriod;
  private final String _scheduleCalculator;
  private final String _samplingFunction;
  private final String _applicableCurrency;

  public InterestRateFutureOptionBlackYieldCurveNodePnLDefaults(final String curveCalculationConfig, final String surfaceName, final String samplingPeriod,
      final String scheduleCalculator, final String samplingFunction, final String applicableCurrency) {
    super(ComputationTargetType.POSITION, true);
    ArgumentChecker.notNull(curveCalculationConfig, "curve calculation config");
    ArgumentChecker.notNull(surfaceName, "surface name");
    ArgumentChecker.notNull(samplingPeriod, "sampling period");
    ArgumentChecker.notNull(scheduleCalculator, "schedule calculator");
    ArgumentChecker.notNull(samplingFunction, "sampling function");
    ArgumentChecker.notNull(applicableCurrency, "applicable currency");
    _curveCalculationConfig = curveCalculationConfig;
    _surfaceName = surfaceName;
    _samplingPeriod = samplingPeriod;
    _scheduleCalculator = scheduleCalculator;
    _samplingFunction = samplingFunction;
    _applicableCurrency = applicableCurrency;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    final Security security = target.getPositionOrTrade().getSecurity();
    if (!(security instanceof FinancialSecurity)) {
      return false;
    }
    if (FXUtils.isFXSecurity(security)) {
      return false;
    }
    if (security instanceof SwapSecurity) {
      try {
        final InterestRateInstrumentType type = InterestRateInstrumentType.getInstrumentTypeFromSecurity((SwapSecurity) security);
        if (type != InterestRateInstrumentType.SWAP_FIXED_IBOR && type != InterestRateInstrumentType.SWAP_FIXED_IBOR_WITH_SPREAD && type != InterestRateInstrumentType.SWAP_IBOR_IBOR
            && type != InterestRateInstrumentType.SWAP_FIXED_OIS) {
          return false;
        }
      } catch (final OpenGammaRuntimeException ogre) {
        return false;
      }
    }
    if (!InterestRateInstrumentType.isFixedIncomeInstrumentType((FinancialSecurity) security)) {
      return false;
    }
    final String currency = FinancialSecurityUtils.getCurrency(security).getCode();
    if (_applicableCurrency.equals(currency)) {
      return true;
    }
    return false;
  }

  @Override
  protected void getDefaults(final PropertyDefaults defaults) {
    defaults.addValuePropertyName(ValueRequirementNames.PNL_SERIES, ValuePropertyNames.CURVE_CALCULATION_CONFIG);
    defaults.addValuePropertyName(ValueRequirementNames.PNL_SERIES, ValuePropertyNames.SURFACE);
    defaults.addValuePropertyName(ValueRequirementNames.PNL_SERIES, ValuePropertyNames.SAMPLING_PERIOD);
    defaults.addValuePropertyName(ValueRequirementNames.PNL_SERIES, ValuePropertyNames.SCHEDULE_CALCULATOR);
    defaults.addValuePropertyName(ValueRequirementNames.PNL_SERIES, ValuePropertyNames.SAMPLING_FUNCTION);
  }

  @Override
  protected Set<String> getDefaultValue(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue,
      final String propertyName) {
    if (ValuePropertyNames.CURVE_CALCULATION_CONFIG.equals(propertyName)) {
      return Collections.singleton(_curveCalculationConfig);
    }
    if (ValuePropertyNames.SURFACE.equals(propertyName)) {
      return Collections.singleton(_surfaceName);
    }
    if (ValuePropertyNames.SAMPLING_PERIOD.equals(propertyName)) {
      return Collections.singleton(_samplingPeriod);
    }
    if (ValuePropertyNames.SCHEDULE_CALCULATOR.equals(propertyName)) {
      return Collections.singleton(_scheduleCalculator);
    }
    if (ValuePropertyNames.SAMPLING_FUNCTION.equals(propertyName)) {
      return Collections.singleton(_samplingFunction);
    }
    return null;
  }

  @Override
  public PriorityClass getPriority() {
    return PriorityClass.ABOVE_NORMAL;
  }

  @Override
  public String getMutualExclusionGroup() {
    return OpenGammaFunctionExclusions.PNL_SERIES;
  }

}
