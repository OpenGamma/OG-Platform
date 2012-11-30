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
import com.opengamma.core.security.Security;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionInputs;
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
public abstract class MarkToMarketFuturesFunction<T> extends FuturesFunction<T> {
  /** The calculation method name */
  public static final String CALCULATION_METHOD_NAME = "MarkToMarket";

  public MarkToMarketFuturesFunction(final String valueRequirementName, final InstrumentDerivativeVisitor<SimpleFutureDataBundle, T> calculator)  {
    super(valueRequirementName, calculator);
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
    requirements.add(getMarketPriceRequirement(security));
    requirements.add(getSpotAssetRequirement(security));
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
    final Double marketPrice = getMarketPrice(security, inputs);
    final Double spotUnderlyer = getSpot(security, inputs);
    return new SimpleFutureDataBundle(null, marketPrice, spotUnderlyer, null, null);
  }

  private ValueRequirement getMarketPriceRequirement(final Security security) {
    return new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, ComputationTargetType.SECURITY, security.getUniqueId());
  }

  private Double getMarketPrice(final Security security, final FunctionInputs inputs) {
    final ValueRequirement marketPriceRequirement = getMarketPriceRequirement(security);
    final Object marketPriceObject = inputs.getValue(marketPriceRequirement);
    if (marketPriceObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + marketPriceRequirement);
    }
    return (Double) marketPriceObject;
  }

}
