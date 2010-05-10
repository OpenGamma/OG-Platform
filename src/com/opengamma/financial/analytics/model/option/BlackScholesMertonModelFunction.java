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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ValueRequirement;
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
import com.opengamma.livedata.normalization.MarketDataFieldNames;
import com.opengamma.util.time.DateUtil;
import com.opengamma.util.time.Expiry;

/**
 * 
 *
 * @author emcleod
 */
public class BlackScholesMertonModelFunction extends AnalyticOptionModelFunction {
  private static final Logger s_logger = LoggerFactory.getLogger(BlackScholesMertonModelFunction.class);
  private final AnalyticOptionModel<OptionDefinition, StandardOptionDataBundle> _model = new BlackScholesMertonModel();

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
      requirements.add(getUnderlyingMarketDataRequirement(option.getUnderlyingSecurity()));
      requirements.add(getDiscountCurveMarketDataRequirement(option.getCurrency().getIdentityKey()));
      requirements.add(getVolatilitySurfaceMarketDataRequirement(option.getUniqueIdentifier()));
      // ValueRequirement costOfCarryRequirement = getCostOfCarryMarketDataRequirement();
      return requirements;
    }
    return null;
  }

  @Override
  public String getShortName() {
    return "BlackScholesMertonModel";
  }

  @Override
  protected AnalyticOptionModel<OptionDefinition, StandardOptionDataBundle> getModel() {
    return _model;
  }

  @Override
  protected OptionDefinition getOptionDefinition(final OptionSecurity option) {
    return new EuropeanVanillaOptionDefinition(option.getStrike(), option.getExpiry(), option.getOptionType() == OptionType.CALL);
  }

  @Override
  protected StandardOptionDataBundle getDataBundle(final Clock relevantTime, final OptionSecurity option, final FunctionInputs inputs) {
    final ZonedDateTime now = relevantTime.zonedDateTime();
    FudgeFieldContainer underlyingLiveData = (FudgeFieldContainer) inputs.getValue(getUnderlyingMarketDataRequirement(option.getUnderlyingSecurity()));
    Double spotAsObject = underlyingLiveData.getDouble(MarketDataFieldNames.INDICATIVE_VALUE_FIELD);
    if(spotAsObject == null) {
      s_logger.warn("Didn't have indicative value for {}, did have {}", option.getUnderlyingSecurity(), underlyingLiveData);
      throw new NullPointerException("No spot value for underlying instrument.");
    }
    final double spot = spotAsObject;
    final DiscountCurve discountCurve = (DiscountCurve) inputs.getValue(getDiscountCurveMarketDataRequirement(option.getCurrency().getIdentityKey()));
    final VolatilitySurface volatilitySurface = (VolatilitySurface) inputs.getValue(getVolatilitySurfaceMarketDataRequirement(option.getUniqueIdentifier()));
    // TODO cost of carry model
    final Expiry expiry = option.getExpiry();
    final double t = DateUtil.getDifferenceInYears(now, expiry.getExpiry().toInstant());
    final double b = discountCurve.getInterestRate(t);// TODO
    return new StandardOptionDataBundle(discountCurve, b, volatilitySurface, spot, now);
  }

}
