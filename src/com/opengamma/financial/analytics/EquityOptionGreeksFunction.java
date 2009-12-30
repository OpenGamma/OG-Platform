/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.time.calendar.Clock;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import org.fudgemsg.FudgeFieldContainer;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.AbstractSecurityFunction;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.function.NewFunctionDefinition;
import com.opengamma.engine.function.NewFunctionInputs;
import com.opengamma.engine.function.NewFunctionInvoker;
import com.opengamma.engine.function.SecurityFunctionDefinition;
import com.opengamma.engine.function.SecurityFunctionInvoker;
import com.opengamma.engine.security.Security;
import com.opengamma.engine.value.AnalyticValueDefinition;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.MarketDataAnalyticValueDefinitionFactory;
import com.opengamma.engine.value.MarketDataComputedValue;
import com.opengamma.engine.value.NewComputedValue;
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
public class EquityOptionGreeksFunction extends AbstractSecurityFunction
implements SecurityFunctionDefinition, SecurityFunctionInvoker,
NewFunctionDefinition, NewFunctionInvoker {
  
  public static final String PRICE_FIELD_NAME = "PRICE";

  @Override
  public Collection<ComputedValue<?>> execute(
      FunctionExecutionContext executionContext, FunctionInputs inputs,
      Security security) {
    if (security.getSecurityType().equals(EquityOptionSecurity.EQUITY_OPTION_TYPE)) {
      final EquityOptionSecurity equityOption = (EquityOptionSecurity) security;
      final DiscountCurve discountCurve = (DiscountCurve) inputs.getValue(new DiscountCurveValueDefinition(equityOption.getCurrency()));
      // TODO: The following line .toString() is a quick hack just to make it compile.
      final VolatilitySurface volSurface = (VolatilitySurface) inputs.getValue(new VolatilitySurfaceValueDefinition(equityOption.getIdentityKey().toString()));
      FudgeFieldContainer underlyingDataFields = (FudgeFieldContainer) inputs.getValue(MarketDataAnalyticValueDefinitionFactory.constructHeaderDefinition(equityOption.getUnderlyingIdentityKey()));
      final ZonedDateTime today = Clock.system(TimeZone.UTC).zonedDateTime();
      final Expiry expiry = equityOption.getExpiry();
      final double costOfCarry_b = discountCurve.getInterestRate(DateUtil.getDifferenceInYears(today, expiry.getExpiry().toInstant()));
      final double spot = underlyingDataFields.getDouble(MarketDataComputedValue.INDICATIVE_VALUE_NAME);       
      StandardOptionDataBundle bundle = new StandardOptionDataBundle(discountCurve, costOfCarry_b, volSurface, spot, today);
      EuropeanVanillaOptionDefinition definition = new EuropeanVanillaOptionDefinition(equityOption.getStrike(), expiry, equityOption.getOptionType() == OptionType.CALL);
      AnalyticOptionModel<OptionDefinition, StandardOptionDataBundle> model = new BlackScholesMertonModel();
      GreekResultCollection greeks = model.getGreeks(definition, bundle, Arrays.asList(new Greek[] {Greek.PRICE, Greek.DELTA, Greek.GAMMA, Greek.RHO}));
      return Collections.<ComputedValue<?>>singleton(new GreeksComputedValue(new GreeksResultValueDefinition(security.getIdentityKey()), greeks));
    } else {
      throw new IllegalStateException("Illegal security type "+security.getSecurityType());
    }
  }

  @Override
  public Collection<AnalyticValueDefinition<?>> getInputs(Security security) {
    if (security.getSecurityType().equals(EquityOptionSecurity.EQUITY_OPTION_TYPE)) {
      final EquityOptionSecurity equityOption = (EquityOptionSecurity) security;
      final Collection<AnalyticValueDefinition<?>> inputs = new ArrayList<AnalyticValueDefinition<?>>();
      inputs.add(new DiscountCurveValueDefinition(equityOption.getCurrency()));
      inputs.add(new VolatilitySurfaceValueDefinition(equityOption.getIdentityKey()));
      // we do this in two lists so we can separate out what MIGHT be specific to the option type in some cases.
      final Collection<AnalyticValueDefinition<?>> justThisOption = new ArrayList<AnalyticValueDefinition<?>>();
      justThisOption.add(MarketDataAnalyticValueDefinitionFactory.constructHeaderDefinition(equityOption.getUnderlyingIdentityKey()));
      
      Collection<AnalyticValueDefinition<?>> result = equityOption.accept(new OptionVisitor<Collection<AnalyticValueDefinition<?>>>() {
        @Override
        public Collection<AnalyticValueDefinition<?>> visitAmericanVanillaOption(
            AmericanVanillaOption option) {
          return justThisOption;
        }

        @Override
        public Collection<AnalyticValueDefinition<?>> visitEuropeanVanillaOption(
            EuropeanVanillaOption option) {
          return justThisOption;
        }

        @Override
        public Collection<AnalyticValueDefinition<?>> visitPoweredOption(PoweredOption option) {
          return justThisOption;
        }
      });
      inputs.addAll(result);
      return inputs;  
    } else {
      throw new IllegalStateException("illegal security type "+security.getSecurityType());
    }
    
  }

  @Override
  public Collection<AnalyticValueDefinition<?>> getPossibleResults(Security security) {
    return Collections.<AnalyticValueDefinition<?>>singleton(new GreeksResultValueDefinition(security.getIdentityKey()));
  }

  @Override
  public String getShortName() {
    return "Greeks Analytic Function";
  }

  @Override
  public boolean isApplicableTo(String securityType) {
    return securityType.equals("EQUITY_OPTION");
  }

  // NewFunction* Methods:
  
  @Override
  public boolean canApplyTo(ComputationTarget target) {
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
  public Set<ValueRequirement> getRequirements(ComputationTarget target) {
    if(!canApplyTo(target)) {
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
  public Set<ValueSpecification> getResults(ComputationTarget target) {
    if(!canApplyTo(target)) {
      return null;
    }
    EquityOptionSecurity equityOptionSec = (EquityOptionSecurity)target.getSecurity();
    ValueSpecification priceSpecification = new ValueSpecification(new ValueRequirement(ValueRequirementNames.FAIR_VALUE, ComputationTargetType.SECURITY, equityOptionSec.getIdentityKey()));
    ValueSpecification deltaSpecification = new ValueSpecification(new ValueRequirement(ValueRequirementNames.DELTA, ComputationTargetType.SECURITY, equityOptionSec.getIdentityKey()));
    ValueSpecification gammaSpecification = new ValueSpecification(new ValueRequirement(ValueRequirementNames.GAMMA, ComputationTargetType.SECURITY, equityOptionSec.getIdentityKey()));
    ValueSpecification rhoSpecification = new ValueSpecification(new ValueRequirement(ValueRequirementNames.RHO, ComputationTargetType.SECURITY, equityOptionSec.getIdentityKey()));
    
    Set<ValueSpecification> results = new HashSet<ValueSpecification>();
    results.add(priceSpecification);
    results.add(deltaSpecification);
    results.add(gammaSpecification);
    results.add(rhoSpecification);
    
    return results;
  }

  @Override
  public Set<NewComputedValue> execute(
      FunctionExecutionContext executionContext, NewFunctionInputs inputs,
      ComputationTarget target) {
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
    final double spot = underlyingMarketData.getDouble(MarketDataComputedValue.INDICATIVE_VALUE_NAME);       
    StandardOptionDataBundle bundle = new StandardOptionDataBundle(discountCurve, costOfCarry_b, volatilitySurface, spot, today);
    EuropeanVanillaOptionDefinition definition = new EuropeanVanillaOptionDefinition(equityOptionSec.getStrike(), expiry, equityOptionSec.getOptionType() == OptionType.CALL);
    AnalyticOptionModel<OptionDefinition, StandardOptionDataBundle> model = new BlackScholesMertonModel();
    GreekResultCollection greeks = model.getGreeks(definition, bundle, Arrays.asList(new Greek[] {Greek.PRICE, Greek.DELTA, Greek.GAMMA, Greek.RHO}));

    // Translate and package results
    GreekResult<?> priceResult = greeks.get(Greek.PRICE);
    GreekResult<?> deltaResult = greeks.get(Greek.DELTA);
    GreekResult<?> gammaResult = greeks.get(Greek.GAMMA);
    GreekResult<?> rhoResult = greeks.get(Greek.RHO);
    
    ValueSpecification priceSpecification = new ValueSpecification(new ValueRequirement(ValueRequirementNames.FAIR_VALUE, ComputationTargetType.SECURITY, equityOptionSec.getIdentityKey()));
    ValueSpecification deltaSpecification = new ValueSpecification(new ValueRequirement(ValueRequirementNames.DELTA, ComputationTargetType.SECURITY, equityOptionSec.getIdentityKey()));
    ValueSpecification gammaSpecification = new ValueSpecification(new ValueRequirement(ValueRequirementNames.GAMMA, ComputationTargetType.SECURITY, equityOptionSec.getIdentityKey()));
    ValueSpecification rhoSpecification = new ValueSpecification(new ValueRequirement(ValueRequirementNames.RHO, ComputationTargetType.SECURITY, equityOptionSec.getIdentityKey()));
    
    NewComputedValue priceValue = new NewComputedValue(priceSpecification, priceResult.getResult());
    NewComputedValue deltaValue = new NewComputedValue(deltaSpecification, deltaResult.getResult());
    NewComputedValue gammaValue = new NewComputedValue(gammaSpecification, gammaResult.getResult());
    NewComputedValue rhoValue = new NewComputedValue(rhoSpecification, rhoResult.getResult());
    
    Set<NewComputedValue> results = new HashSet<NewComputedValue>();
    results.add(priceValue);
    results.add(deltaValue);
    results.add(gammaValue);
    results.add(rhoValue);
    return results;
  }
  
}
