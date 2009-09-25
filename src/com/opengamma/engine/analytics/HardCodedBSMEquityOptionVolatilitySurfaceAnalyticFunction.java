/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.analytics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.time.Instant;
import javax.time.calendar.Clock;
import javax.time.calendar.TimeZone;

import org.apache.commons.lang.NotImplementedException;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.depgraph.DependencyNodeResolver;
import com.opengamma.engine.position.Position;
import com.opengamma.engine.security.AmericanVanillaOption;
import com.opengamma.engine.security.EquityOptionSecurity;
import com.opengamma.engine.security.EuropeanVanillaOption;
import com.opengamma.engine.security.Option;
import com.opengamma.engine.security.OptionType;
import com.opengamma.engine.security.OptionVisitor;
import com.opengamma.engine.security.PoweredOption;
import com.opengamma.engine.security.Security;
import com.opengamma.financial.model.interestrate.curve.DiscountCurve;
import com.opengamma.financial.model.option.definition.EuropeanVanillaOptionDefinition;
import com.opengamma.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.financial.model.option.pricing.OptionPricingException;
import com.opengamma.financial.model.volatility.surface.BlackScholesMertonImpliedVolatilitySurfaceModel;
import com.opengamma.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.financial.model.volatility.surface.VolatilitySurfaceModel;
import com.opengamma.util.time.DateUtil;
import com.opengamma.util.time.Expiry;

/**
 * 
 *
 * @author jim
 */
public class HardCodedBSMEquityOptionVolatilitySurfaceAnalyticFunction
extends AbstractAnalyticFunction
implements AnalyticFunctionInvoker {
  public static final String PRICE_FIELD_NAME = "PRICE";
  
  private final VolatilitySurfaceModel<EuropeanVanillaOptionDefinition, StandardOptionDataBundle> _volatilitySurfaceModel;
    
  public HardCodedBSMEquityOptionVolatilitySurfaceAnalyticFunction() {
    _volatilitySurfaceModel = new BlackScholesMertonImpliedVolatilitySurfaceModel();
  }

  @Override
  public Collection<AnalyticValue<?>> execute(AnalyticFunctionInputs inputs, Position position) {
    throw new UnsupportedOperationException();
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public Collection<AnalyticValue<?>> execute(AnalyticFunctionInputs inputs, Security security) {

    final Instant today = Clock.system(TimeZone.UTC).instant();
    if (security.getSecurityType().equals("EQUITY_OPTION")) {
      final EquityOptionSecurity equityOptionSec = (EquityOptionSecurity)security;
      //AnalyticValueDefinition<?> justThisOptionDefinition = new ResolveSecurityKeyToSecurityDefinition(equityOptionSec.getIndentityKey());
      AnalyticValueDefinition<?> justThisOptionHeader = new ResolveSecurityKeyToMarketDataHeaderDefinition(equityOptionSec.getIndentityKey());
      AnalyticValueDefinition<?> underlyingHeader = new ResolveSecurityKeyToMarketDataHeaderDefinition(equityOptionSec.getUnderlying());
      AnalyticValueDefinition<?> discountCurveForCurrency = new DiscountCurveValueDefinition(equityOptionSec.getCurrency());
      Map<String, Double> optionDataFields = (Map<String, Double>) inputs.getValue(justThisOptionHeader);
      final double price = optionDataFields.get(PRICE_FIELD_NAME);
      Map<String, Double> underlyingDataFields = (Map<String, Double>) inputs.getValue(underlyingHeader);
      final double spot = underlyingDataFields.get(PRICE_FIELD_NAME);
      final DiscountCurve discountCurve = (DiscountCurve) inputs.getValue(discountCurveForCurrency);
      final VolatilitySurface volSurface = equityOptionSec.accept(new OptionVisitor<VolatilitySurface>() {
        private VolatilitySurface visitOption(Option option) {
          Expiry expiry = option.getExpiry();
          double years = DateUtil.getDifferenceInYears(expiry.getExpiry().toInstant(), today);
          final double b = discountCurve.getInterestRate(years);
          EuropeanVanillaOptionDefinition europeanVanillaOptionDefinition = new EuropeanVanillaOptionDefinition(option.getStrike(), expiry, (option.getOptionType() == OptionType.CALL));
          Map<EuropeanVanillaOptionDefinition, Double> prices = new HashMap<EuropeanVanillaOptionDefinition, Double>();
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
            new VolatilitySurfaceValueDefinition(security.getIndentityKey()), 
            volSurface
          )
        );
     }
     throw new OpenGammaRuntimeException("Only EQUITY_OPTIONs should be passed here");
   }
  
  @Override
  public Collection<AnalyticValueDefinition<?>> getInputs(Security security) {
    if (security.getSecurityType().equals("EQUITY_OPTION")) {
      final EquityOptionSecurity equityOptionSec = (EquityOptionSecurity)security;
      //AnalyticValueDefinition<?> justThisOptionDefinition = new ResolveSecurityKeyToSecurityDefinition(equityOptionSec.getIndentityKey());
      AnalyticValueDefinition<?> justThisOptionHeader = new ResolveSecurityKeyToMarketDataHeaderDefinition(equityOptionSec.getIndentityKey());
      AnalyticValueDefinition<?> underlyingHeader = new ResolveSecurityKeyToMarketDataHeaderDefinition(equityOptionSec.getUnderlying());
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
    return Collections.<AnalyticValueDefinition<?>>singleton(new VolatilitySurfaceValueDefinition(null));
  }

  @Override
  public String getShortName() {
    return "HardCodedBSMEquityOptionVolatilitySurface";
  }

  @Override
  public boolean isApplicableTo(String securityType) {
    return true;
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

  @Override
  public DependencyNode buildSubGraph(Security security,
      AnalyticFunctionResolver functionResolver,
      DependencyNodeResolver dependencyNodeResolver) {
    throw new NotImplementedException();
  }

  @Override
  public boolean buildsOwnSubGraph() {
    return false;
  }

}
