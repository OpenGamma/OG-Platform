/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.option;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.time.calendar.Clock;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import org.fudgemsg.FudgeFieldContainer;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.function.FunctionInvoker;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.MarketDataFieldNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.greeks.Greek;
import com.opengamma.financial.greeks.GreekResult;
import com.opengamma.financial.greeks.GreekResultCollection;
import com.opengamma.financial.model.interestrate.curve.DiscountCurve;
import com.opengamma.financial.model.option.definition.EuropeanVanillaOptionDefinition;
import com.opengamma.financial.model.option.definition.OptionDefinition;
import com.opengamma.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.financial.model.option.pricing.analytic.AnalyticOptionModel;
import com.opengamma.financial.model.option.pricing.analytic.BlackScholesMertonModel;
import com.opengamma.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.financial.security.option.Option;
import com.opengamma.financial.security.option.OptionSecurity;
import com.opengamma.financial.security.option.OptionType;
import com.opengamma.id.DomainSpecificIdentifier;
import com.opengamma.util.time.DateUtil;
import com.opengamma.util.time.Expiry;

/**
 * 
 *
 * @author emcleod
 */
public class BlackScholesMertonModelFunction extends AbstractFunction implements FunctionInvoker {
  private final AnalyticOptionModel<OptionDefinition, StandardOptionDataBundle> _model = new BlackScholesMertonModel();
  private static final Map<String, Greek> AVAILABLE_GREEKS;

  static {
    AVAILABLE_GREEKS = new TreeMap<String, Greek>();
    AVAILABLE_GREEKS.put(ValueRequirementNames.FAIR_VALUE, Greek.FAIR_PRICE);
    AVAILABLE_GREEKS.put(ValueRequirementNames.DELTA, Greek.DELTA);
    AVAILABLE_GREEKS.put(ValueRequirementNames.DELTA_BLEED, Greek.DELTA_BLEED);
    AVAILABLE_GREEKS.put(ValueRequirementNames.STRIKE_DELTA, Greek.STRIKE_DELTA);
    AVAILABLE_GREEKS.put(ValueRequirementNames.DRIFTLESS_DELTA, Greek.DRIFTLESS_THETA);

    AVAILABLE_GREEKS.put(ValueRequirementNames.GAMMA, Greek.GAMMA);
    AVAILABLE_GREEKS.put(ValueRequirementNames.GAMMA_P, Greek.GAMMA_P);
    AVAILABLE_GREEKS.put(ValueRequirementNames.STRIKE_GAMMA, Greek.STRIKE_GAMMA);
    AVAILABLE_GREEKS.put(ValueRequirementNames.GAMMA_BLEED, Greek.GAMMA_BLEED);
    AVAILABLE_GREEKS.put(ValueRequirementNames.GAMMA_P_BLEED, Greek.GAMMA_P_BLEED);

    AVAILABLE_GREEKS.put(ValueRequirementNames.VEGA, Greek.VEGA);
    AVAILABLE_GREEKS.put(ValueRequirementNames.VEGA_P, Greek.VEGA_P);
    AVAILABLE_GREEKS.put(ValueRequirementNames.VARIANCE_VEGA, Greek.VARIANCE_VEGA);
    AVAILABLE_GREEKS.put(ValueRequirementNames.VEGA_BLEED, Greek.VEGA_BLEED);

    AVAILABLE_GREEKS.put(ValueRequirementNames.THETA, Greek.THETA);

    AVAILABLE_GREEKS.put(ValueRequirementNames.RHO, Greek.RHO);
    AVAILABLE_GREEKS.put(ValueRequirementNames.CARRY_RHO, Greek.CARRY_RHO);

    AVAILABLE_GREEKS.put(ValueRequirementNames.ZETA, Greek.ZETA);
    AVAILABLE_GREEKS.put(ValueRequirementNames.ZETA_BLEED, Greek.ZETA_BLEED);
    AVAILABLE_GREEKS.put(ValueRequirementNames.DZETA_DVOL, Greek.DZETA_DVOL);

    AVAILABLE_GREEKS.put(ValueRequirementNames.ELASTICITY, Greek.ELASTICITY);
    AVAILABLE_GREEKS.put(ValueRequirementNames.PHI, Greek.PHI);

    AVAILABLE_GREEKS.put(ValueRequirementNames.ZOMMA, Greek.ZOMMA);
    AVAILABLE_GREEKS.put(ValueRequirementNames.ZOMMA_P, Greek.ZOMMA_P);

    AVAILABLE_GREEKS.put(ValueRequirementNames.ULTIMA, Greek.ULTIMA);
    AVAILABLE_GREEKS.put(ValueRequirementNames.VARIANCE_ULTIMA, Greek.VARIANCE_ULTIMA);

    AVAILABLE_GREEKS.put(ValueRequirementNames.SPEED, Greek.SPEED);
    AVAILABLE_GREEKS.put(ValueRequirementNames.SPEED_P, Greek.SPEED_P);

    AVAILABLE_GREEKS.put(ValueRequirementNames.VANNA, Greek.VANNA);
    AVAILABLE_GREEKS.put(ValueRequirementNames.VARIANCE_VANNA, Greek.VARIANCE_VANNA);
    AVAILABLE_GREEKS.put(ValueRequirementNames.DVANNA_DVOL, Greek.DVANNA_DVOL);

    AVAILABLE_GREEKS.put(ValueRequirementNames.VOMMA, Greek.VOMMA);
    AVAILABLE_GREEKS.put(ValueRequirementNames.VOMMA_P, Greek.VOMMA_P);
    AVAILABLE_GREEKS.put(ValueRequirementNames.VARIANCE_VOMMA, Greek.VARIANCE_VOMMA);
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues) {
    final ZonedDateTime now = Clock.system(TimeZone.UTC).zonedDateTime();
    final OptionSecurity option = (OptionSecurity) target.getSecurity();
    final double spot = (((FudgeFieldContainer) inputs.getValue(getUnderlyingMarketDataRequirement(option.getUnderlyingIdentityKey().getIdentityKey()))))
        .getDouble(MarketDataFieldNames.INDICATIVE_VALUE_NAME);
    final DiscountCurve discountCurve = (DiscountCurve) inputs.getValue(getDiscountCurveMarketDataRequirement(option.getCurrency().getIdentityKey()));
    final VolatilitySurface volatilitySurface = (VolatilitySurface) inputs.getValue(getVolatilitySurfaceMarketDataRequirement(option.getIdentityKey()));
    // TODO cost of carry model
    final Expiry expiry = option.getExpiry();
    final double t = DateUtil.getDifferenceInYears(now, expiry.getExpiry().toInstant());
    final double b = discountCurve.getInterestRate(t);// TODO
    final StandardOptionDataBundle data = new StandardOptionDataBundle(discountCurve, b, volatilitySurface, spot, now);
    final OptionDefinition definition = new EuropeanVanillaOptionDefinition(option.getStrike(), expiry, option.getOptionType() == OptionType.CALL);
    final Set<Greek> requiredGreeks = new HashSet<Greek>();
    for (final ValueRequirement dV : desiredValues) {
      final Greek desiredGreek = AVAILABLE_GREEKS.get(dV.getValueName());
      if (desiredGreek == null) {
        throw new IllegalArgumentException("Told to produce " + dV + " but couldn't be mapped to a Greek.");
      }
      requiredGreeks.add(desiredGreek);
    }
    final GreekResultCollection greeks = _model.getGreeks(definition, data, requiredGreeks);
    final Set<ComputedValue> results = new HashSet<ComputedValue>();
    for (final ValueRequirement dV : desiredValues) {
      final Greek greek = AVAILABLE_GREEKS.get(dV.getValueName());
      assert greek != null : "Should have thrown IllegalArgumentException above.";
      final GreekResult<?> greekResult = greeks.get(greek);
      final ValueSpecification resultSpecification = new ValueSpecification(new ValueRequirement(dV.getValueName(), ComputationTargetType.SECURITY, option.getIdentityKey()));
      final ComputedValue resultValue = new ComputedValue(resultSpecification, greekResult.getResult());
      results.add(resultValue);
    }
    return results;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getType() != ComputationTargetType.SECURITY)
      return false;
    if (target.getSecurity() instanceof Option)
      return true;
    return false;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target) {
    if (canApplyTo(context, target)) {
      final OptionSecurity option = (OptionSecurity) target.getSecurity();
      final Set<ValueRequirement> requirements = new HashSet<ValueRequirement>();
      requirements.add(getUnderlyingMarketDataRequirement(option.getUnderlyingIdentityKey().getIdentityKey()));
      requirements.add(getDiscountCurveMarketDataRequirement(option.getCurrency().getIdentityKey()));
      requirements.add(getVolatilitySurfaceMarketDataRequirement(option.getIdentityKey()));
      // ValueRequirement costOfCarryRequirement = getCostOfCarryMarketDataRequirement();
      return requirements;
    }
    return null;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getShortName() {
    return "BlackScholesMertonModel";
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.SECURITY;
  }

  private ValueRequirement getUnderlyingMarketDataRequirement(final DomainSpecificIdentifier id) {
    return new ValueRequirement(ValueRequirementNames.MARKET_DATA_HEADER, ComputationTargetType.SECURITY, id);
  }

  private ValueRequirement getDiscountCurveMarketDataRequirement(final DomainSpecificIdentifier id) {
    return new ValueRequirement(ValueRequirementNames.DISCOUNT_CURVE, ComputationTargetType.PRIMITIVE, id);
  }

  private ValueRequirement getCostOfCarryMarketDataRequirement() {
    // TODO
    return null;
  }

  private ValueRequirement getVolatilitySurfaceMarketDataRequirement(final DomainSpecificIdentifier id) {
    return new ValueRequirement(ValueRequirementNames.VOLATILITY_SURFACE, ComputationTargetType.PRIMITIVE, id);
  }

}
