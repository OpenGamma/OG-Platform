/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics;

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
import com.opengamma.engine.value.MarketDataFieldNames;
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
import com.opengamma.financial.security.AmericanVanillaOption;
import com.opengamma.financial.security.EquityOptionSecurity;
import com.opengamma.financial.security.EuropeanVanillaOption;
import com.opengamma.financial.security.OptionType;
import com.opengamma.financial.security.OptionVisitor;
import com.opengamma.financial.security.PoweredOption;
import com.opengamma.util.time.DateUtil;
import com.opengamma.util.time.Expiry;

/**
 * 
 *
 * @author jim
 */
public class HardCodedBSMEquityOptionVolatilitySurfaceFunction
extends AbstractFunction
implements FunctionInvoker {
  @SuppressWarnings("unused")
  private static final Logger s_logger = LoggerFactory.getLogger(HardCodedBSMEquityOptionVolatilitySurfaceFunction.class);
  public static final String PRICE_FIELD_NAME = "PRICE";
  
  private final VolatilitySurfaceModel<OptionDefinition, StandardOptionDataBundle> _volatilitySurfaceModel;
    
  public HardCodedBSMEquityOptionVolatilitySurfaceFunction() {
    _volatilitySurfaceModel = new BlackScholesMertonImpliedVolatilitySurfaceModel();
  }

  @Override
  public String getShortName() {
    return "HardCodedBSMEquityOptionVolatilitySurface";
  }

  // NEW METHODS:
  @Override
  public boolean canApplyTo(FunctionCompilationContext context, ComputationTarget target) {
    if(target.getType() != ComputationTargetType.SECURITY) {
      return false;
    }
    if(target.getSecurity() instanceof EquityOptionSecurity) {
      return true;
    }
    EquityOptionSecurity equityOptionSec = (EquityOptionSecurity) target.getSecurity();
    Boolean canApply = equityOptionSec.accept(new OptionVisitor<Boolean>() {
      @Override
      public Boolean visitAmericanVanillaOption(AmericanVanillaOption option) {
        return true;
      }

      @Override
      public Boolean visitEuropeanVanillaOption(EuropeanVanillaOption option) {
        return true;
      }

      @Override
      public Boolean visitPoweredOption(PoweredOption option) {
        return true;
      }
      
    });
    return canApply;
  }

  @Override
  public Set<ValueRequirement> getRequirements(FunctionCompilationContext context, ComputationTarget target) {
    if(!canApplyTo(context, target)) {
      return null;
    }
    final EquityOptionSecurity equityOptionSec = (EquityOptionSecurity)target.getSecurity();
    ValueRequirement equityOptionMarketDataReq = new ValueRequirement(ValueRequirementNames.MARKET_DATA_HEADER, ComputationTargetType.SECURITY, equityOptionSec.getIdentityKey());
    ValueRequirement underlyingMarketDataReq = new ValueRequirement(ValueRequirementNames.MARKET_DATA_HEADER, ComputationTargetType.SECURITY, equityOptionSec.getUnderlyingIdentityKey());
    ValueRequirement discountCurveReq = new ValueRequirement(ValueRequirementNames.DISCOUNT_CURVE, ComputationTargetType.PRIMITIVE, equityOptionSec.getCurrency().getISOCode());
    final Set<ValueRequirement> optionRequirements = new HashSet<ValueRequirement>();
    optionRequirements.add(equityOptionMarketDataReq);
    optionRequirements.add(underlyingMarketDataReq);
    optionRequirements.add(discountCurveReq);
    // Since we check the state in canApply, no need to have a visitor. 
    return optionRequirements;
  }
  
  @Override
  public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target, Set<ValueRequirement> requirements) {
    if(!canApplyTo(context, target)) {
      return null;
    }
    return Collections.singleton(createResultSpecification(target.getSecurity()));
  }

  @Override
  public Set<ComputedValue> execute(
      FunctionExecutionContext executionContext,
      FunctionInputs inputs,
      ComputationTarget target) {
    final ZonedDateTime today = Clock.system(TimeZone.UTC).zonedDateTime();
    final EquityOptionSecurity equityOptionSec = (EquityOptionSecurity)target.getSecurity();
    
    // Get inputs:
    ValueRequirement equityOptionMarketDataReq = new ValueRequirement(ValueRequirementNames.MARKET_DATA_HEADER, ComputationTargetType.SECURITY, equityOptionSec.getIdentityKey());
    ValueRequirement underlyingMarketDataReq = new ValueRequirement(ValueRequirementNames.MARKET_DATA_HEADER, ComputationTargetType.SECURITY, equityOptionSec.getUnderlyingIdentityKey());
    ValueRequirement discountCurveReq = new ValueRequirement(ValueRequirementNames.DISCOUNT_CURVE, ComputationTargetType.PRIMITIVE, equityOptionSec.getCurrency().getISOCode());
    
    FudgeFieldContainer equityOptionMarketData = (FudgeFieldContainer)inputs.getValue(equityOptionMarketDataReq);
    FudgeFieldContainer underlyingMarketData = (FudgeFieldContainer)inputs.getValue(underlyingMarketDataReq);
    final DiscountCurve discountCurve = (DiscountCurve)inputs.getValue(discountCurveReq);
    
    final double optionPrice = equityOptionMarketData.getDouble(MarketDataFieldNames.INDICATIVE_VALUE_NAME);
    final double spotPrice = underlyingMarketData.getDouble(MarketDataFieldNames.INDICATIVE_VALUE_NAME);
    
    // No need for a visitor since we check it in canApply.
    
    // Perform the calculation:
    Expiry expiry = equityOptionSec.getExpiry();
    double years = DateUtil.getDifferenceInYears(today, expiry.getExpiry().toInstant());
    final double b = discountCurve.getInterestRate(years);
    OptionDefinition europeanVanillaOptionDefinition = new EuropeanVanillaOptionDefinition(equityOptionSec.getStrike(), expiry, (equityOptionSec.getOptionType() == OptionType.CALL));
    Map<OptionDefinition, Double> prices = new HashMap<OptionDefinition, Double>();
    prices.put(europeanVanillaOptionDefinition, optionPrice);
    VolatilitySurface volatilitySurface = _volatilitySurfaceModel.getSurface(prices, new StandardOptionDataBundle(discountCurve, b, null, spotPrice, today));
    
    // Package the result
    ValueSpecification resultSpec = createResultSpecification(equityOptionSec);
    ComputedValue resultValue = new ComputedValue(resultSpec, volatilitySurface);
    return Collections.singleton(resultValue);
  }
  
  protected static ValueSpecification createResultSpecification(Security security) {
    ValueRequirement resultRequirement = new ValueRequirement(ValueRequirementNames.VOLATILITY_SURFACE, ComputationTargetType.SECURITY, security.getIdentityKey());
    ValueSpecification resultSpec = new ValueSpecification(resultRequirement);
    return resultSpec;
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.SECURITY;
  }

}
