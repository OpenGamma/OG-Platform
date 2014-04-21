/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity.option;

import java.util.Collections;
import java.util.Set;

import com.opengamma.core.security.Security;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.analytics.OpenGammaFunctionExclusions;
import com.opengamma.financial.property.StaticDefaultPropertyFunction;
import com.opengamma.financial.security.FinancialSecurityTypes;
import com.opengamma.financial.security.option.EquityBarrierOptionSecurity;
import com.opengamma.financial.security.option.EquityIndexFutureOptionSecurity;
import com.opengamma.financial.security.option.EquityIndexOptionSecurity;
import com.opengamma.financial.security.option.EquityOptionSecurity;
import com.opengamma.util.ArgumentChecker;

/**
 * Populates {@link EquityOptionFunction}, with CalculationMethods for each of the following assets:
 * <ul>
 * <li>FinancialSecurityTypes.EQUITY_OPTION_SECURITY
 * <li>FinancialSecurityTypes.EQUITY_INDEX_OPTION_SECURITY
 * <li>FinancialSecurityTypes.EQUITY_INDEX_FUTURE_OPTION_SECURITY
 * </ul>
 */
public class EquityOptionCalculationMethodDefaultFunction extends StaticDefaultPropertyFunction {

  /** The priority of this set of defaults */
  private final PriorityClass _priority;

  /** The CalculationMethod for EQUITY_OPTION_SECURITY */
  private final Set<String> _equityOptionMethod;

  /** The CalculationMethod for EQUITY_INDEX_OPTION_SECURITY */
  private final Set<String> _equityIndexOptionMethod;

  /** The CalculationMethod for EQUITY_INDEX_FUTURE_OPTION_SECURITY */
  private final Set<String> _equityIndexFutureOptionMethod;

  /** The CalculationMethod for EQUITY_BARRIER_OPTION_SECURITY */
  private final Set<String> _equityBarrierOptionMethod;

  /** The value requirement names for which these defaults apply */
  private static final String[] VALUE_REQUIREMENTS = new String[] {
      ValueRequirementNames.PRESENT_VALUE,
      ValueRequirementNames.VEGA_QUOTE_MATRIX,
      ValueRequirementNames.VALUE_VEGA,
      ValueRequirementNames.IMPLIED_VOLATILITY,
      ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES,
      ValueRequirementNames.FORWARD,
      ValueRequirementNames.SPOT,
      ValueRequirementNames.VALUE_DELTA,
      ValueRequirementNames.VALUE_GAMMA,
      ValueRequirementNames.VALUE_VOMMA,
      ValueRequirementNames.VALUE_VANNA,
      ValueRequirementNames.VALUE_RHO,
      ValueRequirementNames.VALUE_CARRY_RHO,
      ValueRequirementNames.VALUE_THETA,
      ValueRequirementNames.VALUE_DUAL_DELTA,
      ValueRequirementNames.DELTA,
      ValueRequirementNames.GAMMA,
      ValueRequirementNames.VOMMA,
      ValueRequirementNames.VANNA,
      ValueRequirementNames.RHO,
      ValueRequirementNames.CARRY_RHO,
      ValueRequirementNames.THETA,
      ValueRequirementNames.DUAL_DELTA,
      ValueRequirementNames.VEGA,
      ValueRequirementNames.PNL, // Produced by EquityOption*ScenarioFunction
      ValueRequirementNames.POSITION_DELTA,
      ValueRequirementNames.POSITION_GAMMA,
      ValueRequirementNames.POSITION_RHO,
      ValueRequirementNames.POSITION_THETA,
      ValueRequirementNames.POSITION_VEGA,
      ValueRequirementNames.POSITION_WEIGHTED_VEGA
  };

  public EquityOptionCalculationMethodDefaultFunction(final String priority,
      final String equityOptionMethod, final String equityIndexOptionMethod,
      final String equityIndexFutureOptionMethod, final String equityBarrierOptionMethod) {
    super(FinancialSecurityTypes.EQUITY_OPTION_SECURITY.or(FinancialSecurityTypes.EQUITY_INDEX_OPTION_SECURITY)
        .or(FinancialSecurityTypes.EQUITY_INDEX_FUTURE_OPTION_SECURITY).or(FinancialSecurityTypes.EQUITY_BARRIER_OPTION_SECURITY), ValuePropertyNames.CALCULATION_METHOD,
        true, VALUE_REQUIREMENTS);
    ArgumentChecker.notNull(priority, "priority");
    ArgumentChecker.notNull(equityOptionMethod, "No CalculationMethod provided for " + FinancialSecurityTypes.EQUITY_OPTION_SECURITY);
    ArgumentChecker.notNull(equityIndexOptionMethod, "No CalculationMethod provided for " + FinancialSecurityTypes.EQUITY_INDEX_OPTION_SECURITY);
    ArgumentChecker.notNull(equityIndexFutureOptionMethod, "No CalculationMethod provided for " + FinancialSecurityTypes.EQUITY_INDEX_FUTURE_OPTION_SECURITY);
    ArgumentChecker.notNull(equityBarrierOptionMethod, "No CalculationMethod provided for " + FinancialSecurityTypes.EQUITY_BARRIER_OPTION_SECURITY);
    _priority = PriorityClass.valueOf(priority);
    _equityOptionMethod = Collections.singleton(equityOptionMethod);
    _equityIndexOptionMethod = Collections.singleton(equityIndexOptionMethod);
    _equityIndexFutureOptionMethod = Collections.singleton(equityIndexFutureOptionMethod);
    _equityBarrierOptionMethod = Collections.singleton(equityBarrierOptionMethod);
  }

  public EquityOptionCalculationMethodDefaultFunction(final String priority,
      final String equityOptionMethod, final String equityIndexOptionMethod,
      final String equityIndexFutureOptionMethod) {
    super(FinancialSecurityTypes.EQUITY_OPTION_SECURITY.or(FinancialSecurityTypes.EQUITY_INDEX_OPTION_SECURITY)
        .or(FinancialSecurityTypes.EQUITY_INDEX_FUTURE_OPTION_SECURITY).or(FinancialSecurityTypes.EQUITY_BARRIER_OPTION_SECURITY), ValuePropertyNames.CALCULATION_METHOD,
        true, VALUE_REQUIREMENTS);
    ArgumentChecker.notNull(priority, "priority");
    ArgumentChecker.notNull(equityOptionMethod, "No CalculationMethod provided for " + FinancialSecurityTypes.EQUITY_OPTION_SECURITY);
    ArgumentChecker.notNull(equityIndexOptionMethod, "No CalculationMethod provided for " + FinancialSecurityTypes.EQUITY_INDEX_OPTION_SECURITY);
    ArgumentChecker.notNull(equityIndexFutureOptionMethod, "No CalculationMethod provided for " + FinancialSecurityTypes.EQUITY_INDEX_FUTURE_OPTION_SECURITY);
    _priority = PriorityClass.valueOf(priority);
    _equityOptionMethod = Collections.singleton(equityOptionMethod);
    _equityIndexOptionMethod = Collections.singleton(equityIndexOptionMethod);
    _equityIndexFutureOptionMethod = Collections.singleton(equityIndexFutureOptionMethod);
    _equityBarrierOptionMethod = Collections.singleton(equityOptionMethod);
  }

  /**
   * All Equity Option types to be priced with the same CalculationMethod
   * 
   * @param priority Default priority
   * @param calculationMethod The single calculation method to be used for all Equity Options. e.g. BjerksundStenslandMethod. See CalculationPropertyNamesAndValues for more.
   */
  public EquityOptionCalculationMethodDefaultFunction(final String priority, final String calculationMethod) {
    super(FinancialSecurityTypes.EQUITY_OPTION_SECURITY.or(FinancialSecurityTypes.EQUITY_INDEX_OPTION_SECURITY)
        .or(FinancialSecurityTypes.EQUITY_INDEX_FUTURE_OPTION_SECURITY).or(FinancialSecurityTypes.EQUITY_BARRIER_OPTION_SECURITY), ValuePropertyNames.CALCULATION_METHOD,
        true, VALUE_REQUIREMENTS);
    ArgumentChecker.notNull(priority, "priority");
    ArgumentChecker.notNull(calculationMethod, "No CalculationMethod provided");
    _priority = PriorityClass.valueOf(priority);
    _equityOptionMethod = Collections.singleton(calculationMethod);
    _equityIndexOptionMethod = Collections.singleton(calculationMethod);
    _equityIndexFutureOptionMethod = Collections.singleton(calculationMethod);
    _equityBarrierOptionMethod = Collections.singleton(calculationMethod);
  }

  @Override
  protected Set<String> getDefaultValue(FunctionCompilationContext context, ComputationTarget target, ValueRequirement desiredValue) {
    final Security security = target.getSecurity();
    if (security instanceof EquityOptionSecurity) {
      return _equityOptionMethod;
    }
    if (security instanceof EquityIndexOptionSecurity) {
      return _equityIndexOptionMethod;
    }
    if (security instanceof EquityIndexFutureOptionSecurity) {
      return _equityIndexFutureOptionMethod;
    }
    if (security instanceof EquityBarrierOptionSecurity) {
      return _equityBarrierOptionMethod;
    }
    return null;
  }

  @Override
  public String getMutualExclusionGroup() {
    return OpenGammaFunctionExclusions.CALCULATION_METHOD_DEFAULTS;
  }

}
