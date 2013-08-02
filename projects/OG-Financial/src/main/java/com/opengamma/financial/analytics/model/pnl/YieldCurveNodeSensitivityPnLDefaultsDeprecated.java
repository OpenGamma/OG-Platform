/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.pnl;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.security.Security;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.analytics.OpenGammaFunctionExclusions;
import com.opengamma.financial.analytics.fixedincome.InterestRateInstrumentType;
import com.opengamma.financial.analytics.ircurve.YieldCurveFunction;
import com.opengamma.financial.analytics.model.forex.FXUtils;
import com.opengamma.financial.property.DefaultPropertyFunction;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.util.ArgumentChecker;

/**
 * @deprecated Use the version that does not refer to funding or forward curves
 * @see YieldCurveNodePnLDefaults
 */
@Deprecated
public class YieldCurveNodeSensitivityPnLDefaultsDeprecated extends DefaultPropertyFunction {
  private final String _forwardCurveName;
  private final String _fundingCurveName;
  private final String _curveCalculationMethod;
  private final String _samplingPeriod;
  private final String _scheduleCalculator;
  private final String _samplingFunction;
  private final String[] _applicableCurrencies;

  public YieldCurveNodeSensitivityPnLDefaultsDeprecated(final String forwardCurveName, final String fundingCurveName, final String curveCalculationMethod,
      final String samplingPeriod, final String scheduleCalculator, final String samplingFunction, final String... applicableCurrencies) {
    super(ComputationTargetType.POSITION, true);
    ArgumentChecker.notNull(forwardCurveName, "forward curve name");
    ArgumentChecker.notNull(fundingCurveName, "funding curve name");
    ArgumentChecker.notNull(curveCalculationMethod, "curve calculation method");
    ArgumentChecker.notNull(samplingPeriod, "sampling period");
    ArgumentChecker.notNull(scheduleCalculator, "schedule calculator");
    ArgumentChecker.notNull(samplingFunction, "sampling function");
    ArgumentChecker.notNull(applicableCurrencies, "applicable currencies");
    _forwardCurveName = forwardCurveName;
    _fundingCurveName = fundingCurveName;
    _curveCalculationMethod = curveCalculationMethod;
    _samplingPeriod = samplingPeriod;
    _scheduleCalculator = scheduleCalculator;
    _samplingFunction = samplingFunction;
    _applicableCurrencies = applicableCurrencies;
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
        if (type != InterestRateInstrumentType.SWAP_FIXED_IBOR && type != InterestRateInstrumentType.SWAP_FIXED_IBOR_WITH_SPREAD && type != InterestRateInstrumentType.SWAP_IBOR_IBOR) {
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
    if (Arrays.binarySearch(_applicableCurrencies, currency) < 0) {
      return false;
    }
    return true;
  }

  @Override
  protected void getDefaults(final PropertyDefaults defaults) {
    defaults.addValuePropertyName(ValueRequirementNames.PNL_SERIES, YieldCurveFunction.PROPERTY_FORWARD_CURVE);
    defaults.addValuePropertyName(ValueRequirementNames.PNL_SERIES, YieldCurveFunction.PROPERTY_FUNDING_CURVE);
    defaults.addValuePropertyName(ValueRequirementNames.PNL_SERIES, ValuePropertyNames.CURVE_CALCULATION_METHOD);
    defaults.addValuePropertyName(ValueRequirementNames.PNL_SERIES, ValuePropertyNames.SAMPLING_PERIOD);
    defaults.addValuePropertyName(ValueRequirementNames.PNL_SERIES, ValuePropertyNames.SCHEDULE_CALCULATOR);
    defaults.addValuePropertyName(ValueRequirementNames.PNL_SERIES, ValuePropertyNames.SAMPLING_FUNCTION);
  }

  @Override
  protected Set<String> getDefaultValue(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue,
      final String propertyName) {
    if (YieldCurveFunction.PROPERTY_FORWARD_CURVE.equals(propertyName)) {
      return Collections.singleton(_forwardCurveName);
    }
    if (YieldCurveFunction.PROPERTY_FUNDING_CURVE.equals(propertyName)) {
      return Collections.singleton(_fundingCurveName);
    }
    if (ValuePropertyNames.CURVE_CALCULATION_METHOD.equals(propertyName)) {
      return Collections.singleton(_curveCalculationMethod);
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
  public String getMutualExclusionGroup() {
    return OpenGammaFunctionExclusions.PNL_SERIES;
  }

}
