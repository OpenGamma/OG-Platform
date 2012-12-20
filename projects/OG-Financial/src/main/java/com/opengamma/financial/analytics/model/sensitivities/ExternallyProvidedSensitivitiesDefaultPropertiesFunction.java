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
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.FunctionCompilationContext;
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
  private final DefaultPropertyFunction.PriorityClass _priority;
  private final Map<String, String> _currencyAndCurveConfigNames;

  public ExternallyProvidedSensitivitiesDefaultPropertiesFunction(String priority, String[] currencyAndCurveConfigNames) {
    super(ComputationTargetType.POSITION, true);
    ArgumentChecker.notNull(priority, "priority");
    ArgumentChecker.notNull(currencyAndCurveConfigNames, "currency and curve config names");
    int nPairs = currencyAndCurveConfigNames.length;
    ArgumentChecker.isTrue(nPairs % 2 == 0, "Must have one curve config name per currency");
    _priority = DefaultPropertyFunction.PriorityClass.valueOf(priority);
    _currencyAndCurveConfigNames = new HashMap<String, String>();
    for (int i = 0; i < currencyAndCurveConfigNames.length; i += 2) {
      _currencyAndCurveConfigNames.put(currencyAndCurveConfigNames[i], currencyAndCurveConfigNames[(i + 1)]);
    }
  }

  public boolean canApplyTo(FunctionCompilationContext context, ComputationTarget target) {
    if (!(target.getPosition().getSecurity() instanceof RawSecurity)) {
      return false;
    }
    RawSecurity security = (RawSecurity) target.getPosition().getSecurity();
    if (!security.getSecurityType().equals(SecurityEntryData.EXTERNAL_SENSITIVITIES_SECURITY_TYPE)) {
      return false;
    }
    Currency currency = FinancialSecurityUtils.getCurrency(security);
    if (currency == null) {
      return false;
    }
    String currencyName = currency.getCode();
    if (!this._currencyAndCurveConfigNames.containsKey(currencyName)) {
      return false;
    }
    return true;
  }

  protected void getDefaults(DefaultPropertyFunction.PropertyDefaults defaults) {
    for (String valueName : s_valueNames) {
      defaults.addValuePropertyName(valueName, "CurveCalculationConfig");
    }
  }

  protected Set<String> getDefaultValue(FunctionCompilationContext context, ComputationTarget target, ValueRequirement desiredValue, String propertyName) {
    if ("CurveCalculationConfig".equals(propertyName)) {
      String currencyName = FinancialSecurityUtils.getCurrency(target.getPosition().getSecurity()).getCode();
      String configName = (String) _currencyAndCurveConfigNames.get(currencyName);
      if (configName == null) {
        s_logger.error("Could not get config for currency " + currencyName + "; should never happen");
        return null;
      }
      return Collections.singleton(configName);
    }
    return null;
  }

  public DefaultPropertyFunction.PriorityClass getPriority() {
    return this._priority;
  }

  public String getMutualExclusionGroup() {
    return OpenGammaFunctionExclusions.EXTERNALLY_PROVIDED_SENSITIVITIES_DEFAULTS;
  }
}
