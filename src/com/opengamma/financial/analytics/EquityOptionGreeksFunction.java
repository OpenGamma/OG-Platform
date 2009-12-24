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

import javax.time.calendar.Clock;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import org.fudgemsg.FudgeFieldContainer;

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
import com.opengamma.financial.greeks.Greek;
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
implements SecurityFunctionDefinition, SecurityFunctionInvoker {
  
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
}
