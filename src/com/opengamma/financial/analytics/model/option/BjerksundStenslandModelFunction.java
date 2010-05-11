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

import org.fudgemsg.FudgeFieldContainer;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.financial.model.interestrate.curve.DiscountCurve;
import com.opengamma.financial.model.option.definition.AmericanVanillaOptionDefinition;
import com.opengamma.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.financial.model.option.pricing.analytic.AnalyticOptionModel;
import com.opengamma.financial.model.option.pricing.analytic.BjerksundStenslandModel;
import com.opengamma.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.financial.security.option.AmericanVanillaOption;
import com.opengamma.financial.security.option.Option;
import com.opengamma.financial.security.option.OptionSecurity;
import com.opengamma.financial.security.option.OptionType;
import com.opengamma.livedata.normalization.MarketDataFieldNames;
import com.opengamma.util.time.DateUtil;
import com.opengamma.util.time.Expiry;

/**
 * 
 *
 * @author emcleod
 */
public class BjerksundStenslandModelFunction extends AnalyticOptionModelFunction {
  private final AnalyticOptionModel<AmericanVanillaOptionDefinition, StandardOptionDataBundle> _model = new BjerksundStenslandModel();

  @Override
  protected StandardOptionDataBundle getDataBundle(final Clock relevantTime, final OptionSecurity option, final FunctionInputs inputs) {
    final ZonedDateTime now = relevantTime.zonedDateTime();
    final double spot = (((FudgeFieldContainer) inputs.getValue(getUnderlyingMarketDataRequirement(option.getUnderlyingSecurity()))))
        .getDouble(MarketDataFieldNames.INDICATIVE_VALUE_FIELD);
    final DiscountCurve discountCurve = (DiscountCurve) inputs.getValue(getDiscountCurveMarketDataRequirement(option.getCurrency().getUniqueIdentifier()));
    final VolatilitySurface volatilitySurface = (VolatilitySurface) inputs.getValue(getVolatilitySurfaceMarketDataRequirement(option.getUniqueIdentifier()));
    // TODO cost of carry model
    final Expiry expiry = option.getExpiry();
    final double t = DateUtil.getDifferenceInYears(now, expiry.getExpiry().toInstant());
    final double b = discountCurve.getInterestRate(t);// TODO
    return new StandardOptionDataBundle(discountCurve, b, volatilitySurface, spot, now);
  }

  @Override
  protected AnalyticOptionModel<AmericanVanillaOptionDefinition, StandardOptionDataBundle> getModel() {
    return _model;
  }

  @Override
  protected AmericanVanillaOptionDefinition getOptionDefinition(final OptionSecurity option) {
    return new AmericanVanillaOptionDefinition(option.getStrike(), option.getExpiry(), option.getOptionType() == OptionType.CALL);
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
      requirements.add(getUnderlyingMarketDataRequirement(option.getUnderlyingSecurity()));
      requirements.add(getDiscountCurveMarketDataRequirement(option.getCurrency().getUniqueIdentifier()));
      requirements.add(getVolatilitySurfaceMarketDataRequirement(option.getUniqueIdentifier()));
      // ValueRequirement costOfCarryRequirement = getCostOfCarryMarketDataRequirement();
      return requirements;
    }
    return null;
  }

  @Override
  public String getShortName() {
    return "BjerksundStenslandModel";
  }

}
