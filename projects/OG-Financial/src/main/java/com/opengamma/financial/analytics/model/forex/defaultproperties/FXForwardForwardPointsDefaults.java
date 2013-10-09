/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex.defaultproperties;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.analytics.model.CalculationPropertyNamesAndValues;
import com.opengamma.financial.analytics.model.forex.ForexVisitors;
import com.opengamma.financial.property.DefaultPropertyFunction;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityTypes;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.UnorderedCurrencyPair;

/**
 *
 */
public class FXForwardForwardPointsDefaults extends DefaultPropertyFunction {
  private static final String[] VALUE_REQUIREMENTS = new String[] {
    ValueRequirementNames.FX_PRESENT_VALUE,
    ValueRequirementNames.FX_CURRENCY_EXPOSURE,
    ValueRequirementNames.FX_CURVE_SENSITIVITIES,
    ValueRequirementNames.PV01,
    ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES,
    ValueRequirementNames.VALUE_THETA,
    ValueRequirementNames.PRESENT_VALUE,
    ValueRequirementNames.FX_FORWARD_POINTS_NODE_SENSITIVITIES,
    ValueRequirementNames.PNL_SERIES
  };
  private final Map<UnorderedCurrencyPair, String> _currencyPairToForwardCurveNames;

  public FXForwardForwardPointsDefaults(final String... currencyPairToForwardCurveNames) {
    super(FinancialSecurityTypes.FX_FORWARD_SECURITY.or(FinancialSecurityTypes.NON_DELIVERABLE_FX_FORWARD_SECURITY), true);
    ArgumentChecker.notNull(currencyPairToForwardCurveNames, "currency pair to forward curve names");
    final int n = currencyPairToForwardCurveNames.length;
    ArgumentChecker.isTrue(n % 3 == 0, "Must have one forward curve name per currency pair");
    _currencyPairToForwardCurveNames = new HashMap<>();
    for (int i = 0; i < currencyPairToForwardCurveNames.length; i += 3) {
      final UnorderedCurrencyPair pair = UnorderedCurrencyPair.of(Currency.of(currencyPairToForwardCurveNames[i]), Currency.of(currencyPairToForwardCurveNames[i + 1]));
      _currencyPairToForwardCurveNames.put(pair, currencyPairToForwardCurveNames[i + 2]);
    }
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    final FinancialSecurity fxSecurity = (FinancialSecurity) target.getSecurity();
    final Currency payCurrency = fxSecurity.accept(ForexVisitors.getPayCurrencyVisitor());
    final Currency receiveCurrency = fxSecurity.accept(ForexVisitors.getReceiveCurrencyVisitor());
    return _currencyPairToForwardCurveNames.containsKey(UnorderedCurrencyPair.of(payCurrency, receiveCurrency));
  }

  @Override
  protected void getDefaults(final PropertyDefaults defaults) {
    for (final String valueRequirement : VALUE_REQUIREMENTS) {
      defaults.addValuePropertyName(valueRequirement, ValuePropertyNames.FORWARD_CURVE_NAME);
    }
  }

  @Override
  protected Set<String> getDefaultValue(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue, final String propertyName) {
    final String calculationMethod = desiredValue.getConstraint(ValuePropertyNames.CALCULATION_METHOD);
    if (!CalculationPropertyNamesAndValues.FORWARD_POINTS.equals(calculationMethod)) {
      return null;
    }
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    final Currency payCurrency = security.accept(ForexVisitors.getPayCurrencyVisitor());
    final Currency receiveCurrency = security.accept(ForexVisitors.getReceiveCurrencyVisitor());
    if (ValuePropertyNames.FORWARD_CURVE_NAME.equals(propertyName)) {
      return Collections.singleton(_currencyPairToForwardCurveNames.get(UnorderedCurrencyPair.of(payCurrency, receiveCurrency)));
    }
    return null;
  }
  //
  //  @Override
  //  public String getMutualExclusionGroup() {
  //    return OpenGammaFunctionExclusions.FX_FORWARD_DEFAULTS;
  //  }

}
