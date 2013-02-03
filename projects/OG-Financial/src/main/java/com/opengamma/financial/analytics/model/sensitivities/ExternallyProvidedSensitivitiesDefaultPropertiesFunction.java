/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.sensitivities;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.financial.analytics.OpenGammaFunctionExclusions;
import com.opengamma.financial.property.DefaultPropertyFunction;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.sensitivities.SecurityEntryData;
import com.opengamma.master.security.RawSecurity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Provides defaults for externally provided sensitivities values
 */
public class ExternallyProvidedSensitivitiesDefaultPropertiesFunction extends DefaultPropertyFunction {
  private static final Logger s_logger = LoggerFactory.getLogger(ExternallyProvidedSensitivitiesDefaultPropertiesFunction.class);
  private static final String[] s_valueNames = {
    "Present Value",
    "PV01",
    "CS01",
    "Yield Curve Node Sensitivities" };
  private final Map<String, String> _currencyAndCurveConfigNames;

  public ExternallyProvidedSensitivitiesDefaultPropertiesFunction(final String[] currencyAndCurveConfigNames) {
    super(ComputationTargetType.POSITION, true);
    ArgumentChecker.notNull(currencyAndCurveConfigNames, "currency and curve config names");
    final int nPairs = currencyAndCurveConfigNames.length;
    ArgumentChecker.isTrue(nPairs % 2 == 0, "Must have one curve config name per currency");
    _currencyAndCurveConfigNames = new HashMap<String, String>();
    for (int i = 0; i < currencyAndCurveConfigNames.length; i += 2) {
      _currencyAndCurveConfigNames.put(currencyAndCurveConfigNames[i], currencyAndCurveConfigNames[(i + 1)]);
    }
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (!(target.getPosition().getSecurity() instanceof RawSecurity)) {
      return false;
    }
    final RawSecurity security = (RawSecurity) target.getPosition().getSecurity();
    if (!security.getSecurityType().equals(SecurityEntryData.EXTERNAL_SENSITIVITIES_SECURITY_TYPE)) {
      return false;
    }
    final Currency currency = FinancialSecurityUtils.getCurrency(security);
    if (currency == null) {
      return false;
    }
    final String currencyName = currency.getCode();
    if (!this._currencyAndCurveConfigNames.containsKey(currencyName)) {
      return false;
    }
    return true;
  }

  @Override
  protected void getDefaults(final DefaultPropertyFunction.PropertyDefaults defaults) {
    for (final String valueName : s_valueNames) {
      defaults.addValuePropertyName(valueName, "CurveCalculationConfig");
    }
  }

  @Override
  protected Set<String> getDefaultValue(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue, final String propertyName) {
    if ("CurveCalculationConfig".equals(propertyName)) {
      final String currencyName = FinancialSecurityUtils.getCurrency(target.getPosition().getSecurity()).getCode();
      final String configName = _currencyAndCurveConfigNames.get(currencyName);
      if (configName == null) {
        s_logger.error("Could not get config for currency " + currencyName + "; should never happen");
        return null;
      }
      return Collections.singleton(configName);
    }
    return null;
  }

  @Override
  public String getMutualExclusionGroup() {
    return OpenGammaFunctionExclusions.EXTERNALLY_PROVIDED_SENSITIVITIES_DEFAULTS;
  }

}
