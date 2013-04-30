/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.future;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.core.position.Trade;
import com.opengamma.core.security.Security;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.model.equity.ScenarioPnLPropertyNamesAndValues;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.future.FutureSecurity;
import com.opengamma.util.async.AsynchronousExecution;

/**
 * Simple scenario Function returns the difference in PresentValue between defined Scenario and current market conditions. <p>
 * Price shift may be Additive or Relative. If Additive, the pnl is simply the provided price shift, scaled by unit notional and trade quantity. <p> 
 * If Relative, the market value under shift, d = (1 + d ) * market_value, and pnl = scenario_value - market_value = d * market_value. <p>
 * Shift to volatilities, if provided, clearly have no effect, and are ignored. The Function will still resolve.
 * @author casey
 */
public class MarkToMarketScenarioPnLFuturesFunction extends AbstractFunction.NonCompiledInvoker {

  private static final String s_priceShift = ScenarioPnLPropertyNamesAndValues.PROPERTY_PRICE_SHIFT;
  private static final String s_volShift = ScenarioPnLPropertyNamesAndValues.PROPERTY_VOL_SHIFT;
  private static final String s_priceShiftType = ScenarioPnLPropertyNamesAndValues.PROPERTY_PRICE_SHIFT_TYPE;
  private static final String s_volShiftType = ScenarioPnLPropertyNamesAndValues.PROPERTY_VOL_SHIFT_TYPE; 
  
  private String getValueRequirementName() {
    return ValueRequirementNames.PNL;
  }
  
  @Override
  public Set<ComputedValue> execute(FunctionExecutionContext executionContext, FunctionInputs inputs, ComputationTarget target, Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
    
    // Get security price (market value)
    final Trade trade = target.getTrade();
    final FutureSecurity security = (FutureSecurity) trade.getSecurity();
    final double price = (Double) inputs.getValue(new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, ComputationTargetType.SECURITY, security.getUniqueId()));
    
    // Get shift to price, if provided, and hence PNL
    final double pnl;
    
    ValueProperties constraints = desiredValues.iterator().next().getConstraints();
    String priceConstraint = constraints.getValues(s_priceShift).iterator().next();
    String priceShiftTypeConstraint = constraints.getValues(s_priceShiftType).iterator().next();
    
    if (priceConstraint.equals("")) { 
      pnl = 0.0;
    } else {
      
      final Double priceShift = Double.valueOf(priceConstraint);
      
      if (priceShiftTypeConstraint.equals("Additive")) {
        // The shift is itself the pnl
        pnl = priceShift;
      } else if (priceShiftTypeConstraint.equals("Multiplicative")) {
        // The market value under shift, d = (1 + d ) * market_value, hence pnl = scenario_value - market_value = d * market_value
        pnl = priceShift * price;
      } else {
        s_logger.debug("Valid PriceShiftType's: Additive and Multiplicative. Found: " + priceShiftTypeConstraint + " Defaulting to Multiplicative.");
        pnl = priceShift * price;
      }
    }
    // Scale by unit notional of contract and trade size
    final Double scaledPnl = pnl * security.getUnitAmount() * trade.getQuantity().floatValue();
    // Return PNL with specification
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final ValueSpecification valueSpec = new ValueSpecification(getValueRequirementName(), target.toSpecification(), desiredValue.getConstraints());
    return Collections.singleton(new ComputedValue(valueSpec, scaledPnl));

  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.TRADE;
  }
  
  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    final Security security = target.getTrade().getSecurity();
    return security instanceof FutureSecurity;
  }

  @Override
  public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target) {
    ValueProperties properties = createValueProperties()
        .withAny(s_priceShift).withAny(s_priceShiftType)
        .withAny(s_volShift).withAny(s_volShiftType)
        .with(ValuePropertyNames.CURRENCY, FinancialSecurityUtils.getCurrency(target.getTrade().getSecurity()).getCode())
        .get();
    return Collections.singleton(new ValueSpecification(getValueRequirementName(), target.toSpecification(), properties));
  }
  
  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    ValueSpecification input = inputs.keySet().iterator().next();
    if (getValueRequirementName().equals(input.getValueName())) {
      return inputs.keySet();
    } else {    
      return getResults(context, target);
    }
  }

  @Override
  public Set<ValueRequirement> getRequirements(FunctionCompilationContext context, ComputationTarget target, ValueRequirement desiredValue) {
    
    // Test constraints are provided, else set to ""
    final ValueProperties constraints = desiredValue.getConstraints();
    ValueProperties.Builder scenarioDefaults = null;

    final Set<String> priceShiftSet = constraints.getValues(s_priceShift);
    if (priceShiftSet == null || priceShiftSet.isEmpty()) {
      scenarioDefaults = constraints.copy().withoutAny(s_priceShift).with(s_priceShift, ""); 
    }
    final Set<String> priceShiftTypeSet = constraints.getValues(s_priceShiftType);
    if (priceShiftTypeSet == null || priceShiftTypeSet.isEmpty()) {
      if (scenarioDefaults == null) {
        scenarioDefaults = constraints.copy().withoutAny(s_priceShiftType).with(s_priceShiftType, "Multiplicative");
      } else {
        scenarioDefaults = scenarioDefaults.withoutAny(s_priceShiftType).with(s_priceShiftType, "Multiplicative");
      }
    }
    final Set<String> volShiftSet = constraints.getValues(s_volShift);
    if (volShiftSet == null || volShiftSet.isEmpty()) {
      if (scenarioDefaults == null) {
        scenarioDefaults = constraints.copy().withoutAny(s_volShift).with(s_volShift, "");
      } else {
        scenarioDefaults = scenarioDefaults.withoutAny(s_volShift).with(s_volShift, "");
      }
    }
    final Set<String> volShiftSetType = constraints.getValues(s_volShiftType);
    if (volShiftSetType == null || volShiftSetType.isEmpty()) {
      if (scenarioDefaults == null) {
        scenarioDefaults = constraints.copy().withoutAny(s_volShiftType).with(s_volShiftType, "Multiplicative");
      } else {
        scenarioDefaults = scenarioDefaults.withoutAny(s_volShiftType).with(s_volShiftType, "Multiplicative");
      }
    }
    
    // If defaults have been added, this adds additional copy of the Function into dep graph with the adjusted constraints
    if (scenarioDefaults != null) {
      return Collections.singleton(new ValueRequirement(getValueRequirementName(), target.toSpecification(), scenarioDefaults.get()));
    } else {  // Scenarios are defined, so we're satisfied
      return Collections.singleton(new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, ComputationTargetType.SECURITY, target.getTrade().getSecurity().getUniqueId()));
    }
  }
  
  private static final Logger s_logger = LoggerFactory.getLogger(MarkToMarketScenarioPnLFuturesFunction.class);
}
