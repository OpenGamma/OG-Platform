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

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.analytics.AbstractAnalyticFunction;
import com.opengamma.engine.analytics.AnalyticFunctionInputs;
import com.opengamma.engine.analytics.AnalyticValue;
import com.opengamma.engine.analytics.AnalyticValueDefinition;
import com.opengamma.engine.analytics.FunctionExecutionContext;
import com.opengamma.engine.analytics.MarketDataAnalyticValue;
import com.opengamma.engine.analytics.MarketDataAnalyticValueDefinitionFactory;
import com.opengamma.engine.analytics.SecurityAnalyticFunctionDefinition;
import com.opengamma.engine.analytics.SecurityAnalyticFunctionInvoker;
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
public class HardCodedBSMEquityOptionVolatilitySurfaceAnalyticFunction
extends AbstractAnalyticFunction
implements SecurityAnalyticFunctionDefinition, SecurityAnalyticFunctionInvoker {
  public static final String PRICE_FIELD_NAME = "PRICE";
  
  private final VolatilitySurfaceModel<OptionDefinition, StandardOptionDataBundle> _volatilitySurfaceModel;
    
  public HardCodedBSMEquityOptionVolatilitySurfaceAnalyticFunction() {
    _volatilitySurfaceModel = new BlackScholesMertonImpliedVolatilitySurfaceModel();
  }

  @Override
  public Collection<AnalyticValue<?>> execute(
      FunctionExecutionContext executionContext, AnalyticFunctionInputs inputs,
      Security security) {
    final ZonedDateTime today = Clock.system(TimeZone.UTC).zonedDateTime();
    if (security.getSecurityType().equals(EquityOptionSecurity.EQUITY_OPTION_TYPE)) {
      final EquityOptionSecurity equityOptionSec = (EquityOptionSecurity)security;
      AnalyticValueDefinition<?> justThisOptionHeader = MarketDataAnalyticValueDefinitionFactory.constructHeaderDefinition(equityOptionSec);
      AnalyticValueDefinition<?> underlyingHeader = MarketDataAnalyticValueDefinitionFactory.constructHeaderDefinition(equityOptionSec.getUnderlying());
      AnalyticValueDefinition<?> discountCurveForCurrency = new DiscountCurveValueDefinition(equityOptionSec.getCurrency());
      FudgeFieldContainer optionDataFields = (FudgeFieldContainer)inputs.getValue(justThisOptionHeader);
      if(optionDataFields == null) {
        throw new NullPointerException("No market data available for " + justThisOptionHeader);
      }
      Double priceObj = optionDataFields.getDouble(MarketDataAnalyticValue.INDICATIVE_VALUE_NAME);
      if(priceObj == null) {
        throw new NullPointerException("Got a market data container, but no indicative value.");
      }
      final double price = priceObj;
      FudgeFieldContainer underlyingDataFields = (FudgeFieldContainer) inputs.getValue(underlyingHeader);
      final double spot = underlyingDataFields.getDouble(MarketDataAnalyticValue.INDICATIVE_VALUE_NAME);
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
      return Collections.<AnalyticValue<?>>singleton(
          new VolatilitySurfaceAnalyticValue(
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
      AnalyticValueDefinition<?> underlyingHeader = MarketDataAnalyticValueDefinitionFactory.constructHeaderDefinition(equityOptionSec.getUnderlying());
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
