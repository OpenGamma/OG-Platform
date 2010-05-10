/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.surface;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.time.calendar.Clock;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import org.fudgemsg.FudgeFieldContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.function.FunctionInvoker;
import com.opengamma.engine.security.Security;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.model.interestrate.curve.DiscountCurve;
import com.opengamma.financial.model.option.definition.EuropeanVanillaOptionDefinition;
import com.opengamma.financial.model.option.definition.OptionDefinition;
import com.opengamma.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.financial.model.volatility.surface.BlackScholesMertonImpliedVolatilitySurfaceModel;
import com.opengamma.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.financial.model.volatility.surface.VolatilitySurfaceModel;
import com.opengamma.financial.security.option.Option;
import com.opengamma.financial.security.option.OptionSecurity;
import com.opengamma.financial.security.option.OptionType;
import com.opengamma.id.Identifier;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.livedata.normalization.MarketDataFieldNames;
import com.opengamma.util.time.DateUtil;
import com.opengamma.util.time.Expiry;

/**
 * 
 *
 * @author jim
 */
public class BlackScholesMertonImpliedVolatilitySurfaceFunction extends AbstractFunction implements FunctionInvoker {
  private static final Logger s_logger = LoggerFactory.getLogger(BlackScholesMertonImpliedVolatilitySurfaceFunction.class);
  private final VolatilitySurfaceModel<OptionDefinition, StandardOptionDataBundle> _volatilitySurfaceModel;

  public BlackScholesMertonImpliedVolatilitySurfaceFunction() {
    _volatilitySurfaceModel = new BlackScholesMertonImpliedVolatilitySurfaceModel();
  }

  @Override
  public String getShortName() {
    return "BlackScholesMertonImpliedVolatilitySurface";
  }

  // NEW METHODS:
  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getType() != ComputationTargetType.SECURITY) {
      return false;
    }
    if (target.getSecurity() instanceof Option) {
      return true;
    }
    return false;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target) {
    if (!canApplyTo(context, target)) {
      return null;
    }
    final OptionSecurity optionSec = (OptionSecurity) target.getSecurity();
    final ValueRequirement optionMarketDataReq = getOptionMarketDataRequirement(optionSec.getUniqueIdentifier());
    final ValueRequirement underlyingMarketDataReq = getUnderlyingMarketDataRequirement(optionSec.getUnderlyingSecurity());
    final ValueRequirement discountCurveReq = getDiscountCurveMarketDataRequirement(optionSec.getCurrency().getIdentityKey());
    // TODO will need a cost-of-carry model as well
    final Set<ValueRequirement> optionRequirements = new HashSet<ValueRequirement>();
    optionRequirements.add(optionMarketDataReq);
    optionRequirements.add(underlyingMarketDataReq);
    optionRequirements.add(discountCurveReq);
    return optionRequirements;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    if (!canApplyTo(context, target)) {
      return null;
    }
    return Collections.singleton(createResultSpecification(target.getSecurity()));
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues) {
    final ZonedDateTime today = Clock.system(TimeZone.UTC).zonedDateTime();
    final OptionSecurity optionSec = (OptionSecurity) target.getSecurity();

    // Get inputs:
    final ValueRequirement optionMarketDataReq = getOptionMarketDataRequirement(optionSec.getUniqueIdentifier());
    final ValueRequirement underlyingMarketDataReq = getUnderlyingMarketDataRequirement(optionSec.getUnderlyingSecurity());
    final ValueRequirement discountCurveReq = getDiscountCurveMarketDataRequirement(optionSec.getCurrency().getIdentityKey());

    final FudgeFieldContainer optionMarketData = (FudgeFieldContainer) inputs.getValue(optionMarketDataReq);
    final FudgeFieldContainer underlyingMarketData = (FudgeFieldContainer) inputs.getValue(underlyingMarketDataReq);
    final DiscountCurve discountCurve = (DiscountCurve) inputs.getValue(discountCurveReq);
    // TODO cost-of-carry model
    if(optionMarketData.getByName(MarketDataFieldNames.INDICATIVE_VALUE_FIELD) == null) {
      s_logger.warn("No indicative value for option price, have {}", optionMarketData);
    }
    if(underlyingMarketData.getByName(MarketDataFieldNames.INDICATIVE_VALUE_FIELD) == null) {
      s_logger.warn("No indicative value for underlying price, have {}", underlyingMarketData);
    }
    final double optionPrice = optionMarketData.getDouble(MarketDataFieldNames.INDICATIVE_VALUE_FIELD);
    final double spotPrice = underlyingMarketData.getDouble(MarketDataFieldNames.INDICATIVE_VALUE_FIELD);

    // Perform the calculation:
    final Expiry expiry = optionSec.getExpiry();
    final double years = DateUtil.getDifferenceInYears(today, expiry.getExpiry().toInstant());
    final double b = discountCurve.getInterestRate(years);// TODO
    final OptionDefinition europeanVanillaOptionDefinition = new EuropeanVanillaOptionDefinition(optionSec.getStrike(), expiry, (optionSec.getOptionType() == OptionType.CALL));
    final Map<OptionDefinition, Double> prices = new HashMap<OptionDefinition, Double>();
    prices.put(europeanVanillaOptionDefinition, optionPrice);
    final VolatilitySurface volatilitySurface = _volatilitySurfaceModel.getSurface(prices, new StandardOptionDataBundle(discountCurve, b, null, spotPrice, today));

    // Package the result
    final ValueSpecification resultSpec = createResultSpecification(optionSec);
    final ComputedValue resultValue = new ComputedValue(resultSpec, volatilitySurface);
    return Collections.singleton(resultValue);
  }

  protected static ValueSpecification createResultSpecification(final Security security) {
    final ValueRequirement resultRequirement = new ValueRequirement(ValueRequirementNames.VOLATILITY_SURFACE, security);
    final ValueSpecification resultSpec = new ValueSpecification(resultRequirement);
    return resultSpec;
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.SECURITY;
  }

  private ValueRequirement getOptionMarketDataRequirement(final UniqueIdentifier uid) {
    return new ValueRequirement(ValueRequirementNames.MARKET_DATA_HEADER, ComputationTargetType.SECURITY, uid);
  }

  private ValueRequirement getUnderlyingMarketDataRequirement(final UniqueIdentifier uid) {
    return new ValueRequirement(ValueRequirementNames.MARKET_DATA_HEADER, ComputationTargetType.SECURITY, uid);
  }

  private ValueRequirement getDiscountCurveMarketDataRequirement(final Identifier id) {
    return new ValueRequirement(ValueRequirementNames.DISCOUNT_CURVE, ComputationTargetType.PRIMITIVE, id);
  }
}
