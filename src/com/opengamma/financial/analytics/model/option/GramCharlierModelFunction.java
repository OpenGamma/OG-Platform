/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.option;

import java.util.HashSet;
import java.util.Set;

import javax.time.calendar.Clock;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import org.fudgemsg.FudgeFieldContainer;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.financial.model.interestrate.curve.DiscountCurve;
import com.opengamma.financial.model.option.definition.EuropeanVanillaOptionDefinition;
import com.opengamma.financial.model.option.definition.OptionDefinition;
import com.opengamma.financial.model.option.definition.SkewKurtosisOptionDataBundle;
import com.opengamma.financial.model.option.pricing.analytic.AnalyticOptionModel;
import com.opengamma.financial.model.option.pricing.analytic.GramCharlierModel;
import com.opengamma.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.financial.security.option.AmericanVanillaOption;
import com.opengamma.financial.security.option.Option;
import com.opengamma.financial.security.option.OptionSecurity;
import com.opengamma.financial.security.option.OptionType;
import com.opengamma.id.DomainSpecificIdentifier;
import com.opengamma.livedata.normalization.MarketDataFieldNames;
import com.opengamma.util.time.DateUtil;
import com.opengamma.util.time.Expiry;

/**
 * 
 *
 * @author emcleod
 */
public class GramCharlierModelFunction extends AnalyticOptionModelFunction {
  private final AnalyticOptionModel<OptionDefinition, SkewKurtosisOptionDataBundle> _model = new GramCharlierModel();

  @Override
  protected SkewKurtosisOptionDataBundle getDataBundle(final OptionSecurity option, final FunctionInputs inputs) {
    final ZonedDateTime now = Clock.system(TimeZone.UTC).zonedDateTime();
    final DomainSpecificIdentifier optionID = option.getIdentityKey();
    final double spot = (((FudgeFieldContainer) inputs.getValue(getUnderlyingMarketDataRequirement(option.getUnderlyingIdentityKey().getIdentityKey()))))
        .getDouble(MarketDataFieldNames.INDICATIVE_VALUE_FIELD);
    final DiscountCurve discountCurve = (DiscountCurve) inputs.getValue(getDiscountCurveMarketDataRequirement(option.getCurrency().getIdentityKey()));
    final VolatilitySurface volatilitySurface = (VolatilitySurface) inputs.getValue(getVolatilitySurfaceMarketDataRequirement(optionID));
    // TODO cost of carry model
    final Expiry expiry = option.getExpiry();
    final double t = DateUtil.getDifferenceInYears(now, expiry.getExpiry().toInstant());
    final double b = discountCurve.getInterestRate(t);// TODO
    final double skew = (Double) inputs.getValue(getSkewRequirement(optionID));
    final double kurtosis = (Double) inputs.getValue(getKurtosisRequirement(optionID));
    return new SkewKurtosisOptionDataBundle(discountCurve, b, volatilitySurface, spot, now, skew, kurtosis);
  }

  @Override
  protected AnalyticOptionModel<OptionDefinition, SkewKurtosisOptionDataBundle> getModel() {
    return _model;
  }

  @Override
  protected OptionDefinition getOptionDefinition(final OptionSecurity option) {
    return new EuropeanVanillaOptionDefinition(option.getStrike(), option.getExpiry(), option.getOptionType() == OptionType.CALL);
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getType() != ComputationTargetType.SECURITY)
      return false;
    if (target.getSecurity() instanceof Option && (Option) target.getSecurity() instanceof AmericanVanillaOption)
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
      requirements.add(getSkewRequirement(option.getIdentityKey()));
      requirements.add(getKurtosisRequirement(option.getIdentityKey()));
      // ValueRequirement costOfCarryRequirement = getCostOfCarryMarketDataRequirement();
      return requirements;
    }
    return null;
  }

  @Override
  public String getShortName() {
    return "GramCharlierModel";
  }

  private ValueRequirement getSkewRequirement(final DomainSpecificIdentifier id) {
    return new ValueRequirement(SkewKurtosisFromImpliedVolatilityFunction.SKEW, ComputationTargetType.SECURITY, id);
  }

  private ValueRequirement getKurtosisRequirement(final DomainSpecificIdentifier id) {
    return new ValueRequirement(SkewKurtosisFromImpliedVolatilityFunction.KURTOSIS, ComputationTargetType.SECURITY, id);
  }
}
