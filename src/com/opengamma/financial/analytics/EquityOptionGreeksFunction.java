/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics;

import java.util.Collections;
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
public class EquityOptionGreeksFunction extends AbstractFunction
implements FunctionInvoker {
  private static final Map<String, Greek> s_greeksByValueName;
  
  static {
    Map<String, Greek> greeksMap = new TreeMap<String,Greek>();
    greeksMap.put(ValueRequirementNames.FAIR_VALUE, Greek.PRICE);
    greeksMap.put(ValueRequirementNames.DELTA, Greek.DELTA);
    greeksMap.put(ValueRequirementNames.DELTA_BLEED, Greek.DELTA_BLEED);
    greeksMap.put(ValueRequirementNames.STRIKE_DELTA, Greek.STRIKE_DELTA);
    greeksMap.put(ValueRequirementNames.DRIFTLESS_DELTA, Greek.DRIFTLESS_THETA);
    
    greeksMap.put(ValueRequirementNames.GAMMA, Greek.GAMMA);
    greeksMap.put(ValueRequirementNames.GAMMA_P, Greek.GAMMA_P);
    greeksMap.put(ValueRequirementNames.STRIKE_GAMMA, Greek.STRIKE_GAMMA);
    greeksMap.put(ValueRequirementNames.GAMMA_BLEED, Greek.GAMMA_BLEED);
    greeksMap.put(ValueRequirementNames.GAMMA_P_BLEED, Greek.GAMMA_P_BLEED);
    
    greeksMap.put(ValueRequirementNames.VEGA, Greek.VEGA);
    greeksMap.put(ValueRequirementNames.VEGA_P, Greek.VEGA_P);
    greeksMap.put(ValueRequirementNames.VARIANCE_VEGA, Greek.VARIANCE_VEGA);
    greeksMap.put(ValueRequirementNames.VEGA_BLEED, Greek.VEGA_BLEED);
    
    greeksMap.put(ValueRequirementNames.THETA, Greek.THETA);
    
    greeksMap.put(ValueRequirementNames.RHO, Greek.RHO);
    greeksMap.put(ValueRequirementNames.CARRY_RHO, Greek.CARRY_RHO);
    
    greeksMap.put(ValueRequirementNames.ZETA, Greek.ZETA);
    greeksMap.put(ValueRequirementNames.ZETA_BLEED, Greek.ZETA_BLEED);
    greeksMap.put(ValueRequirementNames.DZETA_DVOL, Greek.DZETA_DVOL);
    
    greeksMap.put(ValueRequirementNames.ELASTICITY, Greek.ELASTICITY);
    greeksMap.put(ValueRequirementNames.PHI, Greek.PHI);
    
    greeksMap.put(ValueRequirementNames.ZOMMA, Greek.ZOMMA);
    greeksMap.put(ValueRequirementNames.ZOMMA_P, Greek.ZOMMA_P);
    
    greeksMap.put(ValueRequirementNames.ULTIMA, Greek.ULTIMA);
    greeksMap.put(ValueRequirementNames.VARIANCE_ULTIMA, Greek.VARIANCE_ULTIMA);
    
    greeksMap.put(ValueRequirementNames.SPEED, Greek.SPEED);
    greeksMap.put(ValueRequirementNames.SPEED_P, Greek.SPEED_P);
    
    greeksMap.put(ValueRequirementNames.VANNA, Greek.VANNA);
    greeksMap.put(ValueRequirementNames.VARIANCE_VANNA, Greek.VARIANCE_VANNA);
    greeksMap.put(ValueRequirementNames.DVANNA_DVOL, Greek.DVANNA_DVOL);
    
    greeksMap.put(ValueRequirementNames.VOMMA, Greek.VOMMA);
    greeksMap.put(ValueRequirementNames.VOMMA_P, Greek.VOMMA_P);
    greeksMap.put(ValueRequirementNames.VARIANCE_VOMMA, Greek.VARIANCE_VOMMA);
    
    s_greeksByValueName = Collections.unmodifiableMap(greeksMap);
  }

  @Override
  public String getShortName() {
    return "Equity Option Greeks Analytic Function";
  }

  // NewFunction* Methods:
  
  @Override
  public boolean canApplyTo(FunctionCompilationContext context, ComputationTarget target) {
    return canApplyTo(target);
  }

  protected boolean canApplyTo(ComputationTarget target) {
    if(target.getType() != ComputationTargetType.SECURITY) {
      return false;
    }
    if(!(target.getValue() instanceof EquityOptionSecurity)) {
      return false;
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
    EquityOptionSecurity equityOptionSec = (EquityOptionSecurity)target.getSecurity();
    ValueRequirement discountCurveReq = new ValueRequirement(ValueRequirementNames.DISCOUNT_CURVE, ComputationTargetType.PRIMITIVE, equityOptionSec.getCurrency().getISOCode());
    ValueRequirement volatilitySurfaceReq = new ValueRequirement(ValueRequirementNames.VOLATILITY_SURFACE, ComputationTargetType.SECURITY, equityOptionSec.getIdentityKey());
    ValueRequirement underlyingMarketDataReq = new ValueRequirement(ValueRequirementNames.MARKET_DATA_HEADER, ComputationTargetType.SECURITY, equityOptionSec.getUnderlyingIdentityKey());
    // No need to do a visitor as of this stage.
    Set<ValueRequirement> requirements = new HashSet<ValueRequirement>();
    requirements.add(discountCurveReq);
    requirements.add(volatilitySurfaceReq);
    requirements.add(underlyingMarketDataReq);
    return requirements;
  }

  @Override
  public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target) {
    if(!canApplyTo(context, target)) {
      return null;
    }
    EquityOptionSecurity equityOptionSec = (EquityOptionSecurity)target.getSecurity();
    Set<ValueSpecification> results = new HashSet<ValueSpecification>();
    addAllPotentialGreeks(results, equityOptionSec.getIdentityKey());
    return results;
  }

  /**
   * @param results
   * @param identityKey
   */
  protected static void addAllPotentialGreeks(
      Set<ValueSpecification> results,
      String identityKey) {
    for(String valueName : s_greeksByValueName.keySet()) {
      results.add(new ValueSpecification(new ValueRequirement(valueName, ComputationTargetType.SECURITY, identityKey)));
    }
  }

  @Override
  public Set<ComputedValue> execute(
      FunctionExecutionContext executionContext, FunctionInputs inputs,
      ComputationTarget target,
      Set<ValueRequirement> desiredValues) {
    if(!canApplyTo(target)) {
      return null;
    }
    // Gather Inputs:
    EquityOptionSecurity equityOptionSec = (EquityOptionSecurity)target.getSecurity();
    DiscountCurve discountCurve = (DiscountCurve)inputs.getValue(ValueRequirementNames.DISCOUNT_CURVE);
    VolatilitySurface volatilitySurface = (VolatilitySurface)inputs.getValue(ValueRequirementNames.VOLATILITY_SURFACE);
    ValueRequirement underlyingMarketDataReq = new ValueRequirement(ValueRequirementNames.MARKET_DATA_HEADER, ComputationTargetType.SECURITY, equityOptionSec.getUnderlyingIdentityKey());
    FudgeFieldContainer underlyingMarketData = (FudgeFieldContainer)inputs.getValue(underlyingMarketDataReq);
    
    // Perform Calculation:
    final ZonedDateTime today = Clock.system(TimeZone.UTC).zonedDateTime();
    final Expiry expiry = equityOptionSec.getExpiry();
    final double costOfCarry_b = discountCurve.getInterestRate(DateUtil.getDifferenceInYears(today, expiry.getExpiry().toInstant()));
    final double spot = underlyingMarketData.getDouble(MarketDataFieldNames.INDICATIVE_VALUE_NAME);       
    StandardOptionDataBundle bundle = new StandardOptionDataBundle(discountCurve, costOfCarry_b, volatilitySurface, spot, today);
    EuropeanVanillaOptionDefinition definition = new EuropeanVanillaOptionDefinition(equityOptionSec.getStrike(), expiry, equityOptionSec.getOptionType() == OptionType.CALL);
    AnalyticOptionModel<OptionDefinition, StandardOptionDataBundle> model = new BlackScholesMertonModel();
    
    // Process the desired values:
    Set<Greek> desiredGreeks = new HashSet<Greek>();
    for(ValueRequirement desiredValue : desiredValues) {
      Greek desiredGreek = s_greeksByValueName.get(desiredValue.getValueName());
      if(desiredGreek == null) {
        throw new IllegalArgumentException("Told to produce " + desiredValue + " but couldn't be mapped to a Greek.");
      }
      desiredGreeks.add(desiredGreek);
    }

    // Invoke calculation:
    GreekResultCollection greeks = model.getGreeks(definition, bundle, desiredGreeks);
    
    // Package results:
    Set<ComputedValue> results = new HashSet<ComputedValue>();
    for(ValueRequirement desiredValue : desiredValues) {
      Greek desiredGreek = s_greeksByValueName.get(desiredValue.getValueName());
      assert desiredGreek != null : "Should have thrown IllegalArgumentException above.";
      GreekResult<?> greekResult = greeks.get(desiredGreek);
      ValueSpecification resultSpecification = new ValueSpecification(
          new ValueRequirement(desiredValue.getValueName(), ComputationTargetType.SECURITY, equityOptionSec.getIdentityKey()));
      ComputedValue resultValue = new ComputedValue(resultSpecification, greekResult.getResult());
      results.add(resultValue);
    }
    return results;
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.SECURITY;
  }
  
}
