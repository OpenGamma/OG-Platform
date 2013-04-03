/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.future;

import java.util.HashSet;
import java.util.Set;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.simpleinstruments.pricing.SimpleFutureDataBundle;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesBundle;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.future.FutureSecurity;
import com.opengamma.util.money.Currency;

/**
 * @param <T> The type of the data returned from the calculator
 */
public abstract class CostOfCarryFuturesFunction<T> extends FuturesFunction<T> {
  /** The calculation method name */
  public static final String CALCULATION_METHOD_NAME = "CostOfCarry";

  /**
   * @param valueRequirementName String describes the value requested
   * @param calculator The calculator
   * @param closingPriceField The field name of the historical time series for price, e.g. "PX_LAST", "Close". Set in *FunctionConfiguration
   * @param costOfCarryField The field name of the historical time series for cost of carry e.g. "COST_OF_CARRY". Set in *FunctionConfiguration
   * @param resolutionKey The key defining how the time series resolution is to occur e.g. "DEFAULT_TSS_CONFIG"
   */
  public CostOfCarryFuturesFunction(final String valueRequirementName, final InstrumentDerivativeVisitor<SimpleFutureDataBundle, T> calculator, 
      String closingPriceField, String costOfCarryField, String resolutionKey) {
    super(valueRequirementName, calculator, closingPriceField, costOfCarryField, resolutionKey);
  }

  
  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final FutureSecurity security = (FutureSecurity)  target.getTrade().getSecurity();
    final Set<ValueRequirement> requirements = new HashSet<ValueRequirement>();
    // Spot
    final ValueRequirement refPriceReq = getReferencePriceRequirement(context, security);
    if (refPriceReq == null) {
      return null;
    }
    requirements.add(refPriceReq);
    requirements.add(getSpotAssetRequirement(security));
    requirements.add(getCostOfCarryRequirement(security));
    return requirements;
  }

  @Override
  protected ValueProperties.Builder createValueProperties(final ComputationTarget target) {
    final Currency ccy = FinancialSecurityUtils.getCurrency(target.getTrade().getSecurity());
    final ValueProperties.Builder properties = createValueProperties()
      .with(ValuePropertyNames.CURRENCY, ccy.getCode())
      .with(ValuePropertyNames.CALCULATION_METHOD, CALCULATION_METHOD_NAME);
    return properties;
  }

  @Override
  protected SimpleFutureDataBundle getFutureDataBundle(final FutureSecurity security, final FunctionInputs inputs,
    final HistoricalTimeSeriesBundle timeSeriesBundle, final ValueRequirement desiredValue) {
    final Double spotUnderlyer = getSpot(inputs);
    final Double costOfCarry = getCostOfCarry(security, inputs);
    return new SimpleFutureDataBundle(null, null, spotUnderlyer, null, costOfCarry);
  }

  private ValueRequirement getCostOfCarryRequirement(final FutureSecurity security) {
    return new ValueRequirement(MarketDataRequirementNames.COST_OF_CARRY, ComputationTargetType.PRIMITIVE, getSpotAssetId(security)); // [PLAT-2286] Is this PRIMITIVE or SECURITY?
  }

  private Double getCostOfCarry(final FutureSecurity security, final FunctionInputs inputs) {
    final ValueRequirement costOfCarryRequirement = getCostOfCarryRequirement(security);
    final Object costOfCarryObject = inputs.getValue(costOfCarryRequirement);
    if (costOfCarryObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + costOfCarryRequirement);
    }
    return (Double) costOfCarryObject;
  }

}
