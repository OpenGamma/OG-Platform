/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.time.calendar.Clock;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import org.fudgemsg.FudgeFieldContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
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
import com.opengamma.financial.model.interestrate.curve.DiscountCurve;
import com.opengamma.financial.model.option.definition.EuropeanVanillaOptionDefinition;
import com.opengamma.financial.model.option.definition.OptionDefinition;
import com.opengamma.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.financial.model.option.pricing.OptionPricingException;
import com.opengamma.financial.model.volatility.surface.BlackScholesMertonImpliedVolatilitySurfaceModel;
import com.opengamma.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.financial.model.volatility.surface.VolatilitySurfaceModel;
import com.opengamma.financial.security.AmericanVanillaOption;
import com.opengamma.financial.security.EquityOptionSecurity;
import com.opengamma.financial.security.EuropeanVanillaOption;
import com.opengamma.financial.security.Option;
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
extends AbstractSecurityFunction
implements SecurityFunctionDefinition, SecurityFunctionInvoker,
NewFunctionDefinition, NewFunctionInvoker {
  private static final Logger s_logger = LoggerFactory.getLogger(HardCodedBSMEquityOptionVolatilitySurfaceFunction.class);
  public static final String PRICE_FIELD_NAME = "PRICE";
  
  private final VolatilitySurfaceModel<OptionDefinition, StandardOptionDataBundle> _volatilitySurfaceModel;
    
  public HardCodedBSMEquityOptionVolatilitySurfaceFunction() {
    _volatilitySurfaceModel = new BlackScholesMertonImpliedVolatilitySurfaceModel();
  }

  @Override
  public Collection<ComputedValue<?>> execute(
      FunctionExecutionContext executionContext, FunctionInputs inputs,
      Security security) {
    final ZonedDateTime today = Clock.system(TimeZone.UTC).zonedDateTime();
    if (security.getSecurityType().equals(EquityOptionSecurity.EQUITY_OPTION_TYPE)) {
      final EquityOptionSecurity equityOptionSec = (EquityOptionSecurity)security;
      AnalyticValueDefinition<?> justThisOptionHeader = MarketDataAnalyticValueDefinitionFactory.constructHeaderDefinition(equityOptionSec);
      s_logger.debug("In execute() asking for option header {}", justThisOptionHeader);
      AnalyticValueDefinition<?> underlyingHeader = MarketDataAnalyticValueDefinitionFactory.constructHeaderDefinition(equityOptionSec.getUnderlyingIdentityKey());
      AnalyticValueDefinition<?> discountCurveForCurrency = new DiscountCurveValueDefinition(equityOptionSec.getCurrency());
      FudgeFieldContainer optionDataFields = (FudgeFieldContainer)inputs.getValue(justThisOptionHeader);
      if(optionDataFields == null) {
        throw new NullPointerException("No market data available for " + justThisOptionHeader);
      }
      Double priceObj = optionDataFields.getDouble(MarketDataComputedValue.INDICATIVE_VALUE_NAME);
      if(priceObj == null) {
        throw new NullPointerException("Got a market data container, but no indicative value.");
      }
      final double price = priceObj;
      FudgeFieldContainer underlyingDataFields = (FudgeFieldContainer) inputs.getValue(underlyingHeader);
      if(underlyingDataFields == null) {
        throw new OpenGammaRuntimeException("No data available for underlying header " + underlyingHeader);
      }
      final double spot = underlyingDataFields.getDouble(MarketDataComputedValue.INDICATIVE_VALUE_NAME);
      final DiscountCurve discountCurve = (DiscountCurve) inputs.getValue(discountCurveForCurrency);
      final VolatilitySurface volSurface = equityOptionSec.accept(new OptionVisitor<VolatilitySurface>() {
        private VolatilitySurface visitOption(Option option) {
          Expiry expiry = option.getExpiry();
          double years = DateUtil.getDifferenceInYears(today, expiry.getExpiry().toInstant());
          final double b = discountCurve.getInterestRate(years);
          OptionDefinition europeanVanillaOptionDefinition = new EuropeanVanillaOptionDefinition(option.getStrike(), expiry, (option.getOptionType() == OptionType.CALL));
          Map<OptionDefinition, Double> prices = new HashMap<OptionDefinition, Double>();
          prices.put(europeanVanillaOptionDefinition, price);
          try {
            return _volatilitySurfaceModel.getSurface(prices, new StandardOptionDataBundle(discountCurve, b, null, spot, today));
          } catch (OptionPricingException ope) {
            throw new OpenGammaRuntimeException("Option Pricing Exception", ope); 
          } 
        }
        @Override
        public VolatilitySurface visitAmericanVanillaOption(AmericanVanillaOption option) {
          return visitOption(option);
        }

        @Override
        public VolatilitySurface visitEuropeanVanillaOption(EuropeanVanillaOption option) {
          return visitOption(option);
        }

        @Override
        public VolatilitySurface visitPoweredOption(PoweredOption option) {
          return visitOption(option);
        }
      });
      return Collections.<ComputedValue<?>>singleton(
          new VolatilitySurfaceComputedValue(
            new VolatilitySurfaceValueDefinition(security.getIdentityKey()), 
            volSurface
          )
        );
     }
     throw new OpenGammaRuntimeException("Only EQUITY_OPTIONs should be passed here");
  }

  @Override
  public Collection<AnalyticValueDefinition<?>> getInputs(Security security) {
    if (security.getSecurityType().equals(EquityOptionSecurity.EQUITY_OPTION_TYPE)) {
      final EquityOptionSecurity equityOptionSec = (EquityOptionSecurity)security;
      AnalyticValueDefinition<?> justThisOptionHeader = MarketDataAnalyticValueDefinitionFactory.constructHeaderDefinition(equityOptionSec);
      s_logger.debug("In getInputs() asking for option header {}", justThisOptionHeader);
      AnalyticValueDefinition<?> underlyingHeader = MarketDataAnalyticValueDefinitionFactory.constructHeaderDefinition(equityOptionSec.getUnderlyingIdentityKey());
      AnalyticValueDefinition<?> discountCurveForCurrency = new DiscountCurveValueDefinition(equityOptionSec.getCurrency());
      final List<AnalyticValueDefinition<?>> justThisOption = new ArrayList<AnalyticValueDefinition<?>>();
      //justThisOption.add(justThisOptionDefinition);
      justThisOption.add(justThisOptionHeader);
      justThisOption.add(underlyingHeader);
      justThisOption.add(discountCurveForCurrency);
      Collection<AnalyticValueDefinition<?>> result = equityOptionSec.accept(new OptionVisitor<Collection<AnalyticValueDefinition<?>>>() {
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
      return result;    
    } else {
      return Collections.emptySet();
    }
  }

  @Override
  public Collection<AnalyticValueDefinition<?>> getPossibleResults(Security security) {
    return Collections.<AnalyticValueDefinition<?>>singleton(new VolatilitySurfaceValueDefinition(security.getIdentityKey()));
  }

  @Override
  public String getShortName() {
    return "HardCodedBSMEquityOptionVolatilitySurface";
  }

  @Override
  public boolean isApplicableTo(String securityType) {
    return true;
  }

  // NEW METHODS:
  @Override
  public boolean canApplyTo(ComputationTarget target) {
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
  public Set<ValueRequirement> getRequirements(ComputationTarget target) {
    if(!canApplyTo(target)) {
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
  public Set<ValueSpecification> getResults(ComputationTarget target) {
    if(!canApplyTo(target)) {
      return null;
    }
    return Collections.singleton(createResultSpecification(target.getSecurity()));
  }

  @Override
  public Set<NewComputedValue> execute(
      FunctionExecutionContext executionContext,
      NewFunctionInputs inputs,
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
    
    final double optionPrice = equityOptionMarketData.getDouble(MarketDataComputedValue.INDICATIVE_VALUE_NAME);
    final double spotPrice = underlyingMarketData.getDouble(MarketDataComputedValue.INDICATIVE_VALUE_NAME);
    
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
    NewComputedValue resultValue = new NewComputedValue(resultSpec, volatilitySurface);
    return Collections.singleton(resultValue);
  }
  
  protected static ValueSpecification createResultSpecification(Security security) {
    ValueRequirement resultRequirement = new ValueRequirement(ValueRequirementNames.VOLATILITY_SURFACE, ComputationTargetType.SECURITY, security.getIdentityKey());
    ValueSpecification resultSpec = new ValueSpecification(resultRequirement);
    return resultSpec;
  }

}
