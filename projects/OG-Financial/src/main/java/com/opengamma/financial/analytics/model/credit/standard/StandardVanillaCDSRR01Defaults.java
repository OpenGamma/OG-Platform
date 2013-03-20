/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.credit.standard;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.analytics.model.credit.CreditInstrumentPropertyNamesAndValues;
import com.opengamma.financial.property.DefaultPropertyFunction;
import com.opengamma.financial.security.FinancialSecurityTypes;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class StandardVanillaCDSRR01Defaults extends DefaultPropertyFunction {
  private static final String[] VALUE_REQUIREMENT = new String[] {
    ValueRequirementNames.RR01,
  };
  private final PriorityClass _priority;
  private final Map<String, String> _currencyToRecoveryRateBump;
  private final Map<String, String> _currencyToRecoveryBumpType;
  private final Map<String, String> _currencyToPriceType;

  public StandardVanillaCDSRR01Defaults(final String priority, final String... perCurrencyDefaults) {
    super(FinancialSecurityTypes.STANDARD_VANILLA_CDS_SECURITY.or(FinancialSecurityTypes.LEGACY_VANILLA_CDS_SECURITY), true);
    ArgumentChecker.notNull(priority, "priority");
    ArgumentChecker.notNull(perCurrencyDefaults, "per currency defaults");
    ArgumentChecker.isTrue(perCurrencyDefaults.length % 4 == 0, "Must have one recovery rate bump, recovery rate bump type and price type per currency");
    _priority = PriorityClass.valueOf(priority);
    _currencyToRecoveryRateBump = new HashMap<>();
    _currencyToRecoveryBumpType = new HashMap<>();
    _currencyToPriceType = new HashMap<>();
    for (int i = 0; i < perCurrencyDefaults.length; i += 4) {
      final String currency = perCurrencyDefaults[i];
      _currencyToRecoveryRateBump.put(currency, perCurrencyDefaults[i + 1]);
      _currencyToRecoveryBumpType.put(currency, perCurrencyDefaults[i + 2]);
      _currencyToPriceType.put(currency, perCurrencyDefaults[i + 3]);
    }
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    return _currencyToRecoveryRateBump.containsKey(FinancialSecurityUtils.getCurrency(target.getSecurity()).getCode());
  }

  @Override
  protected void getDefaults(final PropertyDefaults defaults) {
    for (final String valueRequirement : VALUE_REQUIREMENT) {
      defaults.addValuePropertyName(valueRequirement, CreditInstrumentPropertyNamesAndValues.PROPERTY_RECOVERY_RATE_CURVE_BUMP);
      defaults.addValuePropertyName(valueRequirement, CreditInstrumentPropertyNamesAndValues.PROPERTY_RECOVERY_RATE_BUMP_TYPE);
      defaults.addValuePropertyName(valueRequirement, CreditInstrumentPropertyNamesAndValues.PROPERTY_CDS_PRICE_TYPE);
    }
  }

  @Override
  protected Set<String> getDefaultValue(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue, final String propertyName) {
    final String currency = FinancialSecurityUtils.getCurrency(target.getSecurity()).getCode();
    if (CreditInstrumentPropertyNamesAndValues.PROPERTY_RECOVERY_RATE_CURVE_BUMP.equals(propertyName)) {
      return Collections.singleton(_currencyToRecoveryRateBump.get(currency));
    }
    if (CreditInstrumentPropertyNamesAndValues.PROPERTY_RECOVERY_RATE_BUMP_TYPE.equals(propertyName)) {
      return Collections.singleton(_currencyToRecoveryBumpType.get(currency));
    }
    if (CreditInstrumentPropertyNamesAndValues.PROPERTY_CDS_PRICE_TYPE.equals(propertyName)) {
      return Collections.singleton(_currencyToPriceType.get(currency));
    }
    return null;
  }

  @Override
  public PriorityClass getPriority() {
    return _priority;
  }
}
