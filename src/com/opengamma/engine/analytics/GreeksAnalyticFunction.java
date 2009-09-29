/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.analytics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import javax.time.calendar.Clock;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.depgraph.DependencyNodeResolver;
import com.opengamma.engine.position.Position;
import com.opengamma.engine.security.AmericanVanillaOption;
import com.opengamma.engine.security.EquityOptionSecurity;
import com.opengamma.engine.security.EuropeanVanillaOption;
import com.opengamma.engine.security.OptionVisitor;
import com.opengamma.engine.security.PoweredOption;
import com.opengamma.engine.security.Security;
import com.opengamma.engine.security.OptionType;
import com.opengamma.financial.greeks.Delta;
import com.opengamma.financial.greeks.Gamma;
import com.opengamma.financial.greeks.Greek;
import com.opengamma.financial.greeks.Price;
import com.opengamma.financial.greeks.Rho;
import com.opengamma.financial.model.interestrate.curve.DiscountCurve;
import com.opengamma.financial.model.option.definition.EuropeanVanillaOptionDefinition;
import com.opengamma.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.financial.model.option.pricing.analytic.AnalyticOptionModel;
import com.opengamma.financial.model.option.pricing.analytic.BlackScholesMertonModel;
import com.opengamma.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.util.time.DateUtil;
import com.opengamma.util.time.Expiry;

/**
 * 
 *
 * @author jim
 */
public class GreeksAnalyticFunction extends AbstractAnalyticFunction implements
    AnalyticFunctionInvoker {
  
  public static final String PRICE_FIELD_NAME = "PRICE";

  @Override
  public Collection<AnalyticValue<?>> execute(AnalyticFunctionInputs inputs,
      Position position) {
    return null;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Collection<AnalyticValue<?>> execute(AnalyticFunctionInputs inputs,
      Security security) {
    if (security.getSecurityType().equals("EQUITY_OPTION")) {
      final EquityOptionSecurity equityOption = (EquityOptionSecurity) security;
      final DiscountCurve discountCurve = (DiscountCurve) inputs.getValue(new DiscountCurveValueDefinition(equityOption.getCurrency()));
      final VolatilitySurface volSurface = (VolatilitySurface) inputs.getValue(new VolatilitySurfaceValueDefinition(equityOption.getIdentityKey()));
      final Map<String, Double> underlyingDataFields = (Map<String, Double>) inputs.getValue(new ResolveSecurityKeyToMarketDataHeaderDefinition(equityOption.getUnderlying()));
      final ZonedDateTime today = Clock.system(TimeZone.UTC).zonedDateTime();
      final Expiry expiry = equityOption.getExpiry();
      final double costOfCarry_b = discountCurve.getInterestRate(DateUtil.getDifferenceInYears(today, expiry.getExpiry().toInstant()));
      final double spot = underlyingDataFields.get(PRICE_FIELD_NAME);       
      StandardOptionDataBundle bundle = new StandardOptionDataBundle(discountCurve, costOfCarry_b, volSurface, spot, today);
      EuropeanVanillaOptionDefinition definition = new EuropeanVanillaOptionDefinition(equityOption.getStrike(), expiry, equityOption.getOptionType() == OptionType.CALL);
      AnalyticOptionModel<EuropeanVanillaOptionDefinition, StandardOptionDataBundle> model = new BlackScholesMertonModel();
      Map<Greek, Map<String, Double>> greeks = model.getGreeks(definition, bundle, Arrays.asList(new Greek[] {new Price(), new Delta(), new Gamma(), new Rho()}));
      return Collections.<AnalyticValue<?>>singleton(new GreeksResultAnalyticValue(new GreeksResultValueDefinition(security.getIdentityKey()), greeks));
    } else {
      throw new IllegalStateException("Illegal security type "+security.getSecurityType());
    }
  }

  @Override
  public DependencyNode buildSubGraph(Security security,
      AnalyticFunctionResolver functionResolver,
      DependencyNodeResolver dependencyNodeResolver) {
    return null;
  }

  @Override
  public boolean buildsOwnSubGraph() {
    return false;
  }

  @Override
  public Collection<AnalyticValueDefinition<?>> getInputs(Security security) {
    if (security.getSecurityType().equals("EQUITY_OPTION")) {
      final EquityOptionSecurity equityOption = (EquityOptionSecurity) security;
      final Collection<AnalyticValueDefinition<?>> inputs = new ArrayList<AnalyticValueDefinition<?>>();
      inputs.add(new DiscountCurveValueDefinition(equityOption.getCurrency()));
      inputs.add(new VolatilitySurfaceValueDefinition(equityOption.getIdentityKey()));
      // we do this in two lists so we can separate out what MIGHT be specific to the option type in some cases.
      final Collection<AnalyticValueDefinition<?>> justThisOption = new ArrayList<AnalyticValueDefinition<?>>();
      justThisOption.add(new ResolveSecurityKeyToMarketDataHeaderDefinition(equityOption.getUnderlying()));
      
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
    return "GreeksResult";
  }

  @Override
  public boolean isApplicableTo(String securityType) {
    return securityType.equals("EQUITY_OPTION");
  }

  @Override
  public boolean isApplicableTo(Position position) {
    return false;
  }

  @Override
  public boolean isPositionSpecific() {
    return false;
  }

  @Override
  public boolean isSecuritySpecific() {
    return true;
  }

}
