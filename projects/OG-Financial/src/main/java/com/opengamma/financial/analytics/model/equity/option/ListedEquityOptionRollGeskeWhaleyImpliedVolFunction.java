/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity.option;

import java.util.Collections;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.analytics.financial.equity.EqyOptRollGeskeWhaleyImpliedVolatilityCalculator;
import com.opengamma.analytics.financial.equity.StaticReplicationDataBundle;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetReference;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.security.FinancialSecurity;

/**
 *
 */
public class ListedEquityOptionRollGeskeWhaleyImpliedVolFunction extends ListedEquityOptionRollGeskeWhaleyFunction {
  /** The logger */
  private static final Logger s_logger = LoggerFactory.getLogger(ListedEquityOptionRollGeskeWhaleyImpliedVolFunction.class);

  /**
   * Implied volatility calculator
   */
  private static final EqyOptRollGeskeWhaleyImpliedVolatilityCalculator s_volCalculator = EqyOptRollGeskeWhaleyImpliedVolatilityCalculator.getInstance();

  /** Default constructor */
  public ListedEquityOptionRollGeskeWhaleyImpliedVolFunction() {
    super(ValueRequirementNames.IMPLIED_VOLATILITY);
  }

  @Override
  protected Set<ComputedValue> computeValues(final InstrumentDerivative derivative, final StaticReplicationDataBundle market, final FunctionInputs inputs,
      final Set<ValueRequirement> desiredValues, final ComputationTargetSpecification targetSpec, final ValueProperties resultProperties) {

    Double marketPrice = null;
    final ComputedValue mktPriceObj = inputs.getComputedValue(MarketDataRequirementNames.MARKET_VALUE);
    if (mktPriceObj == null) {
      s_logger.info(MarketDataRequirementNames.MARKET_VALUE + " not available," + targetSpec);
    } else {
      marketPrice = (Double) mktPriceObj.getValue();
    }

    Double impliedVol = null;
    try {
      impliedVol = s_volCalculator.getRollGeskeWhaleyImpliedVol(derivative, market, marketPrice);
    } catch (final IllegalArgumentException e) {
      s_logger.info(MarketDataRequirementNames.IMPLIED_VOLATILITY + " undefined" + targetSpec);
      impliedVol = derivative.accept(s_volCalculator, market);
    }

    final ValueSpecification resultSpec = new ValueSpecification(getValueRequirementNames()[0], targetSpec, resultProperties);
    return Collections.singleton(new ComputedValue(resultSpec, impliedVol));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final Set<ValueRequirement> requirements = super.getRequirements(context, target, desiredValue);
    if (requirements == null) {
      return null;
    }
    // Add live market_value of the option
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    final ComputationTargetReference securityTarget = new ComputationTargetSpecification(ComputationTargetType.SECURITY, security.getUniqueId());
    final ValueRequirement securityValueReq = new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, securityTarget);
    requirements.add(securityValueReq);

    return requirements;
  }

}
