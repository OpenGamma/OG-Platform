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
import java.util.List;
import java.util.Map;

import javax.time.calendar.Clock;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import org.fudgemsg.FudgeFieldContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.analytics.AbstractSecurityFunction;
import com.opengamma.engine.analytics.FunctionInputs;
import com.opengamma.engine.analytics.ComputedValue;
import com.opengamma.engine.analytics.AnalyticValueDefinition;
import com.opengamma.engine.analytics.FunctionExecutionContext;
import com.opengamma.engine.analytics.MarketDataComputedValue;
import com.opengamma.engine.analytics.MarketDataAnalyticValueDefinitionFactory;
import com.opengamma.engine.analytics.SecurityFunctionDefinition;
import com.opengamma.engine.analytics.SecurityFunctionInvoker;
import com.opengamma.engine.security.Security;
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
implements SecurityFunctionDefinition, SecurityFunctionInvoker {
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
}
