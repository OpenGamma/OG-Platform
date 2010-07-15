/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.option;

import java.util.HashSet;
import java.util.Set;

import javax.time.calendar.Clock;
import javax.time.calendar.ZonedDateTime;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.security.Security;
import com.opengamma.engine.security.SecurityMaster;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
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
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.time.DateUtil;
import com.opengamma.util.time.Expiry;

/**
 * 
 *
 */
public class GramCharlierModelFunction extends AnalyticOptionModelFunction {
  private final AnalyticOptionModel<OptionDefinition, SkewKurtosisOptionDataBundle> _model = new GramCharlierModel();

  @Override
  protected SkewKurtosisOptionDataBundle getDataBundle(final SecurityMaster secMaster, final Clock relevantTime, final OptionSecurity option, final FunctionInputs inputs) {
    final ZonedDateTime now = relevantTime.zonedDateTime();
    final UniqueIdentifier optionID = option.getUniqueIdentifier();
    final Security underlying = secMaster.getSecurity(new IdentifierBundle(option.getUnderlyingIdentifier()));
    final double spot = (Double) inputs.getValue(getUnderlyingMarketDataRequirement(underlying.getUniqueIdentifier()));
    final YieldAndDiscountCurve discountCurve = (YieldAndDiscountCurve) inputs.getValue(getDiscountCurveMarketDataRequirement(option.getCurrency().getUniqueIdentifier()));
    final VolatilitySurface volatilitySurface = (VolatilitySurface) inputs.getValue(getVolatilitySurfaceMarketDataRequirement(optionID));
    // TODO cost of carry model
    final Expiry expiry = option.getExpiry();
    final double t = DateUtil.getDifferenceInYears(now, expiry.getExpiry().toInstant());
    final double b = discountCurve.getInterestRate(t); // TODO
    final double skew = (Double) inputs.getValue(getSkewRequirement(optionID));
    final double kurtosis = (Double) inputs.getValue(getKurtosisRequirement(optionID));
    return new SkewKurtosisOptionDataBundle(discountCurve, b, volatilitySurface, spot, now, skew, kurtosis);
  }

  @SuppressWarnings("unchecked")
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
    if (target.getType() != ComputationTargetType.SECURITY) {
      return false;
    }
    if (target.getSecurity() instanceof Option && (Option) target.getSecurity() instanceof AmericanVanillaOption) {
      return true;
    }
    return false;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target) {
    if (canApplyTo(context, target)) {
      final OptionSecurity option = (OptionSecurity) target.getSecurity();
      final Set<ValueRequirement> requirements = new HashSet<ValueRequirement>();
      final SecurityMaster securityMaster = context.getSecurityMaster();
      final Security underlying = securityMaster.getSecurity(new IdentifierBundle(option.getUnderlyingIdentifier()));
      requirements.add(getUnderlyingMarketDataRequirement(underlying.getUniqueIdentifier()));
      requirements.add(getDiscountCurveMarketDataRequirement(option.getCurrency().getUniqueIdentifier()));
      requirements.add(getVolatilitySurfaceMarketDataRequirement(option.getUniqueIdentifier()));
      requirements.add(getSkewRequirement(option.getUniqueIdentifier()));
      requirements.add(getKurtosisRequirement(option.getUniqueIdentifier()));
      // ValueRequirement costOfCarryRequirement = getCostOfCarryMarketDataRequirement();
      return requirements;
    }
    return null;
  }

  @Override
  public String getShortName() {
    return "GramCharlierModel";
  }

  private ValueRequirement getSkewRequirement(final UniqueIdentifier uid) {
    return new ValueRequirement(SkewKurtosisFromImpliedVolatilityFunction.SKEW, ComputationTargetType.SECURITY, uid);
  }

  private ValueRequirement getKurtosisRequirement(final UniqueIdentifier uid) {
    return new ValueRequirement(SkewKurtosisFromImpliedVolatilityFunction.KURTOSIS, ComputationTargetType.SECURITY, uid);
  }
}
