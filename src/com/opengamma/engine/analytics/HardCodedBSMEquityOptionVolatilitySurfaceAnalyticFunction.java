/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.analytics;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.time.InstantProvider;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.position.Position;
import com.opengamma.engine.security.AmericanVanillaOption;
import com.opengamma.engine.security.EquityOptionSecurity;
import com.opengamma.engine.security.EuropeanVanillaOption;
import com.opengamma.engine.security.OptionType;
import com.opengamma.engine.security.OptionVisitor;
import com.opengamma.engine.security.PoweredOption;
import com.opengamma.engine.security.Security;
import com.opengamma.engine.security.SecurityKey;
import com.opengamma.engine.security.SecurityKeyImpl;
import com.opengamma.financial.model.interestrate.curve.DiscountCurve;
import com.opengamma.financial.model.option.definition.EuropeanVanillaOptionDefinition;
import com.opengamma.financial.model.option.definition.OptionDefinition;
import com.opengamma.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.financial.model.option.pricing.OptionPricingException;
import com.opengamma.financial.model.volatility.VolatilitySurfaceModel;
import com.opengamma.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.util.KeyValuePair;
import com.opengamma.util.time.DateUtil;

// REVIEW kirk 2009-09-16 -- Changed name to USD as it's holding all the strips
// that are specific to USD, and can only generate one type of result definition.
// This would not be usual practice.
// REVIEW jim 2009-09-16 -- You don't say...
/**
 * 
 *
 * @author jim
 */
public class HardCodedBSMEquityOptionVolatilitySurfaceAnalyticFunction<T extends OptionDefinition, U extends StandardOptionDataBundle> implements AnalyticFunction {

  @SuppressWarnings("unused")
  private static final String PRICE_FIELD_NAME = "PRICE";
  
  private final VolatilitySurfaceModel<EuropeanVanillaOptionDefinition, StandardOptionDataBundle> _volatilitySurfaceModel;
    
  public HardCodedBSMEquityOptionVolatilitySurfaceAnalyticFunction(VolatilitySurfaceModel<EuropeanVanillaOptionDefinition, StandardOptionDataBundle> volatilitySurfaceModel) {
    _volatilitySurfaceModel = volatilitySurfaceModel;
  }

  @Override
  public Collection<AnalyticValue<?>> execute(AnalyticFunctionInputs inputs, Position position) {
    throw new UnsupportedOperationException();
  }
  
  @SuppressWarnings("unused")
  private Map<String, Map<SecurityKey, Object>> unpackResolvedSecurities(Collection<AnalyticValue<?>> inputs) {
    Map<String, Map<SecurityKey, Object>> resolvedSecurities = new HashMap<String, Map<SecurityKey, Object>>();
    for (AnalyticValue<?> value : inputs) {
      if (!resolvedSecurities.containsKey(value.getDefinition().getValue("TYPE"))) {
        resolvedSecurities.put((String)value.getDefinition().getValue("TYPE"), new HashMap<SecurityKey, Object>());
      }
      resolvedSecurities.get((String)value.getDefinition().getValue("TYPE")).put((SecurityKey) value.getDefinition().getValue("SECURITY_KEY"), value.getValue());
    }
    return resolvedSecurities;
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public Collection<AnalyticValue<?>> execute(AnalyticFunctionInputs inputs, Security security) {
    final double price = 0.0;
    final double spot = 0.0;
    final double b = 0.0;
    final DiscountCurve discountCurve = null;
    if (security.getSecurityType().equals("EQUITY_OPTION")) {
      final EquityOptionSecurity equityOptionSec = (EquityOptionSecurity)security;
      final SecurityKey key = new SecurityKeyImpl(equityOptionSec.getIdentifiers());
      final VolatilitySurface volSurface = equityOptionSec.accept(new OptionVisitor<VolatilitySurface>() {
        @Override
        public VolatilitySurface visitAmericanVanillaOption(AmericanVanillaOption option) {
          InstantProvider expiry = option.getExpiry().getExpiry();
          EuropeanVanillaOptionDefinition europeanVanillaOptionDefinition = new EuropeanVanillaOptionDefinition(option.getStrike(), DateUtil.instantToDate(expiry.toInstant()), (option.getOptionType() == OptionType.CALL));
          Map<EuropeanVanillaOptionDefinition, Double> prices = new HashMap<EuropeanVanillaOptionDefinition, Double>();
          prices.put(europeanVanillaOptionDefinition, price);
          try {
            return _volatilitySurfaceModel.getSurface(prices, new StandardOptionDataBundle(discountCurve, b, null, spot, DateUtil.today()));
          } catch (OptionPricingException ope) {
            throw new OpenGammaRuntimeException("Option Pricing Exception", ope); 
          }
        }

        @Override
        public VolatilitySurface visitEuropeanVanillaOption(EuropeanVanillaOption option) {
          InstantProvider expiry = option.getExpiry().getExpiry();
          EuropeanVanillaOptionDefinition europeanVanillaOptionDefinition = new EuropeanVanillaOptionDefinition(option.getStrike(), DateUtil.instantToDate(expiry.toInstant()), (option.getOptionType() == OptionType.CALL));
          Map<EuropeanVanillaOptionDefinition, Double> prices = new HashMap<EuropeanVanillaOptionDefinition, Double>();
          prices.put(europeanVanillaOptionDefinition, price);
          try {
            return _volatilitySurfaceModel.getSurface(prices, new StandardOptionDataBundle(discountCurve, b, null, spot, DateUtil.today()));
          } catch (OptionPricingException ope) {
            throw new OpenGammaRuntimeException("Option Pricing Exception", ope);
          }
        }

        @Override
        public VolatilitySurface visitPoweredOption(PoweredOption option) {
          InstantProvider expiry = option.getExpiry().getExpiry();
          EuropeanVanillaOptionDefinition europeanVanillaOptionDefinition = new EuropeanVanillaOptionDefinition(option.getStrike(), DateUtil.instantToDate(expiry.toInstant()), (option.getOptionType() == OptionType.CALL));
          Map<EuropeanVanillaOptionDefinition, Double> prices = new HashMap<EuropeanVanillaOptionDefinition, Double>();
          prices.put(europeanVanillaOptionDefinition, price);
          try {
            return _volatilitySurfaceModel.getSurface(prices, new StandardOptionDataBundle(discountCurve, b, null, spot, DateUtil.today()));
          } catch (OptionPricingException ope) {
            throw new OpenGammaRuntimeException("Option Pricing Exception", ope);
          }
        }
      });
      return Collections.<AnalyticValue<?>>singleton(
          new VolatilitySurfaceAnalyticValue(
            new AnalyticValueDefinitionImpl(
              new KeyValuePair<String, Object>("TYPE", "VOLATILITY_SURFACE"),
              new KeyValuePair<String, Object>("SECURITY", security)
            ), 
            volSurface
          )
        );
     }
     throw new OpenGammaRuntimeException("Only EQUITY_OPTIONs should be passed here");
   }
  
  @SuppressWarnings("unchecked")
  @Override
  public Collection<AnalyticValueDefinition<?>> getInputs(Security security) {
    if (security.getSecurityType().equals("EQUITY_OPTION")) {
      final EquityOptionSecurity equityOptionSec = (EquityOptionSecurity)security;
      final SecurityKey key = new SecurityKeyImpl(equityOptionSec.getIdentifiers());
      AnalyticValueDefinition<?> justThisOptionDefinition = 
        new AnalyticValueDefinitionImpl(new KeyValuePair<String, Object>("TYPE", "RESOLVE_KEY_TO_SECURITY"),
                                        new KeyValuePair<String, Object>("SECURITY_KEY", key));
      AnalyticValueDefinition<?> justThisOptionHeader = 
        new AnalyticValueDefinitionImpl(new KeyValuePair<String, Object>("TYPE", "MARKET_DATA_HEADER"),
                                        new KeyValuePair<String, Object>("SECURITY_KEY", key));
      //AnalyticValueDefinition justThisOptionDefinition = 
      //  new AnalyticValueDefinitionImpl(new KeyValuePair<String, Object>("TYPE", "RESOLVE_KEY_TO_SECURITY"),
      //                                  new KeyValuePair<String, Object>("SECURITY_KEY", key));
      
      
      final Collection<AnalyticValueDefinition<?>> justThisOption = Collections.<AnalyticValueDefinition<?>>singleton(justThisOptionDefinition);
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
  public Collection<AnalyticValueDefinition<?>> getPossibleResults() {
    return Collections.<AnalyticValueDefinition<?>>singleton(null);//getDiscountCurveValueDefinition());
  }

  @Override
  public String getShortName() {
    return "HardCodedUSDDiscountCurve";
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



}
