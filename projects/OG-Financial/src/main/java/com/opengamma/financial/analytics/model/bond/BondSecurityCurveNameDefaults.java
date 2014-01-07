/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.bond;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.opengamma.core.security.Security;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.property.DefaultPropertyFunction;
import com.opengamma.financial.security.FinancialSecurityTypes;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.bond.BondSecurity;
import com.opengamma.financial.security.future.BondFutureSecurity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 *
 */
public class BondSecurityCurveNameDefaults extends DefaultPropertyFunction {

  private static final String[] s_bondValueNames = new String[] {
    ValueRequirementNames.CLEAN_PRICE,
    ValueRequirementNames.DIRTY_PRICE,
    ValueRequirementNames.MACAULAY_DURATION,
    ValueRequirementNames.MODIFIED_DURATION,
    ValueRequirementNames.PRESENT_VALUE,
    ValueRequirementNames.YTM,
    ValueRequirementNames.Z_SPREAD,
    ValueRequirementNames.PRESENT_VALUE_Z_SPREAD_SENSITIVITY,
    ValueRequirementNames.CONVEXITY,
    ValueRequirementNames.ACCRUED_INTEREST,
    ValueRequirementNames.PV01,
    ValueRequirementNames.DV01,
  };

  private static final String[] s_bondFutureValueNames = new String[] {
    ValueRequirementNames.GROSS_BASIS,
    ValueRequirementNames.NET_BASIS
  };

  private final Map<String, Pair<String, String>> _currencyAndRiskFreeCurveNames;
  private final Map<String, Pair<String, String>> _currencyAndCreditCurveNames;

  public BondSecurityCurveNameDefaults(final String... currencyAndCurveConfigNames) {
    super(FinancialSecurityTypes.BOND_SECURITY.or(FinancialSecurityTypes.BOND_FUTURE_SECURITY), true);
    ArgumentChecker.notNull(currencyAndCurveConfigNames, "currency and curve config names");
    ArgumentChecker.isTrue(currencyAndCurveConfigNames.length % 5 == 0,
        "Must have a risk-free curve name, risk-free curve config, credit curve name and credit curve config per currency");
    _currencyAndCreditCurveNames = new HashMap<>();
    _currencyAndRiskFreeCurveNames = new HashMap<>();
    for (int i = 0; i < currencyAndCurveConfigNames.length; i += 5) {
      final String currency = currencyAndCurveConfigNames[i];
      final String riskFreeCurve = currencyAndCurveConfigNames[i + 1];
      final String riskFreeConfig = currencyAndCurveConfigNames[i + 2];
      final String creditCurve = currencyAndCurveConfigNames[i + 3];
      final String creditConfig = currencyAndCurveConfigNames[i + 4];
      _currencyAndRiskFreeCurveNames.put(currency, Pairs.of(riskFreeCurve, riskFreeConfig));
      _currencyAndCreditCurveNames.put(currency, Pairs.of(creditCurve, creditConfig));
    }
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    final String currency = FinancialSecurityUtils.getCurrency(target.getSecurity()).getCode();
    return _currencyAndCreditCurveNames.containsKey(currency);
  }

  private static void addProperties(final PropertyDefaults defaults, final String[] values) {
    for (final String value : values) {
      defaults.addValuePropertyName(value, BondFunction.PROPERTY_CREDIT_CURVE);
      defaults.addValuePropertyName(value, BondFunction.PROPERTY_RISK_FREE_CURVE);
      defaults.addValuePropertyName(value, BondFunction.PROPERTY_CREDIT_CURVE_CONFIG);
      defaults.addValuePropertyName(value, BondFunction.PROPERTY_RISK_FREE_CURVE_CONFIG);
    }
  }

  @Override
  protected void getDefaults(final PropertyDefaults defaults) {
    final Security target = defaults.getTarget().getSecurity();
    if (target instanceof BondSecurity) {
      addProperties(defaults, s_bondValueNames);
    } else {
      assert target instanceof BondFutureSecurity;
      addProperties(defaults, s_bondFutureValueNames);
    }
  }

  @Override
  protected Set<String> getDefaultValue(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue, final String propertyName) {
    final String currency = FinancialSecurityUtils.getCurrency(target.getSecurity()).getCode();
    if (BondFunction.PROPERTY_CREDIT_CURVE.equals(propertyName)) {
      return Collections.singleton(_currencyAndCreditCurveNames.get(currency).getFirst());
    }
    if (BondFunction.PROPERTY_CREDIT_CURVE_CONFIG.equals(propertyName)) {
      return Collections.singleton(_currencyAndCreditCurveNames.get(currency).getSecond());
    }
    if (BondFunction.PROPERTY_RISK_FREE_CURVE.equals(propertyName)) {
      return Collections.singleton(_currencyAndRiskFreeCurveNames.get(currency).getFirst());
    }
    if (BondFunction.PROPERTY_RISK_FREE_CURVE_CONFIG.equals(propertyName)) {
      return Collections.singleton(_currencyAndRiskFreeCurveNames.get(currency).getSecond());
    }
    return null;
  }

}
