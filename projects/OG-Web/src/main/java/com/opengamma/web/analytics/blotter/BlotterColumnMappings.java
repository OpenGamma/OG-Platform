/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.blotter;

import static com.opengamma.web.analytics.blotter.BlotterColumn.DIRECTION;
import static com.opengamma.web.analytics.blotter.BlotterColumn.FLOAT_FREQUENCY;
import static com.opengamma.web.analytics.blotter.BlotterColumn.FREQUENCY;
import static com.opengamma.web.analytics.blotter.BlotterColumn.INDEX;
import static com.opengamma.web.analytics.blotter.BlotterColumn.MATURITY;
import static com.opengamma.web.analytics.blotter.BlotterColumn.PRODUCT;
import static com.opengamma.web.analytics.blotter.BlotterColumn.QUANTITY;
import static com.opengamma.web.analytics.blotter.BlotterColumn.RATE;
import static com.opengamma.web.analytics.blotter.BlotterColumn.START;
import static com.opengamma.web.analytics.blotter.BlotterColumn.TYPE;

import java.util.Map;

import org.joda.beans.MetaProperty;

import com.google.common.collect.Maps;
import com.opengamma.financial.currency.CurrencyPair;
import com.opengamma.financial.currency.CurrencyPairs;
import com.opengamma.financial.currency.CurrencyUtils;
import com.opengamma.financial.security.capfloor.CapFloorSecurity;
import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.web.analytics.blotter.swap.FloatFrequencyProvider;
import com.opengamma.web.analytics.blotter.swap.FrequencyProvider;
import com.opengamma.web.analytics.blotter.swap.IndexProvider;
import com.opengamma.web.analytics.blotter.swap.PayReceiveProvider;
import com.opengamma.web.analytics.blotter.swap.ProductProvider;
import com.opengamma.web.analytics.blotter.swap.QuantityProvider;
import com.opengamma.web.analytics.blotter.swap.RateProvider;

/**
 * Maps the properties of each blotter column to properties in each supported security type.
 */
@SuppressWarnings("unchecked")
public class BlotterColumnMappings {

  private final Map<Class<? extends ManageableSecurity>, Map<BlotterColumn, ValueProvider>> _mappings = Maps.newHashMap();

  // TODO make this static and use BlotterResource converters
  public BlotterColumnMappings(final CurrencyPairs currencyPairs) {

    // ------------------- CapFloor
    mapColumn(TYPE, CapFloorSecurity.class, "Cap/Floor");
    mapColumn(QUANTITY, CapFloorSecurity.meta().notional());
    mapColumn(START, CapFloorSecurity.meta().startDate());
    mapColumn(MATURITY, CapFloorSecurity.meta().maturityDate());
    mapColumn(RATE, CapFloorSecurity.meta().strike());
    mapColumn(FREQUENCY, CapFloorSecurity.meta().frequency());

    // ------------------- FRA
    mapColumn(TYPE, FRASecurity.class, "FRA");
    mapColumn(PRODUCT, FRASecurity.meta().currency());
    mapColumn(QUANTITY, FRASecurity.meta().amount());
    mapColumn(START, FRASecurity.meta().startDate());
    mapColumn(MATURITY, FRASecurity.meta().endDate());
    mapColumn(RATE, FRASecurity.meta().rate());

    // ------------------- FXOption
    /*mapColumn(TYPE, FXOptionSecurity.class, "FX Option");
    mapColumn(MATURITY, FXOptionSecurity.meta().expiry());
    // TODO custom providers needed
    mapColumn(PRODUCT, );
    mapColumn(QUANTITY, );*/

    // ------------------- Swap
    // TODO basis swap for float/float swaps
    // TODO XCCY swap if the currencies are different
    mapColumn(TYPE, SwapSecurity.class, "Interest Rate Swap");
    mapColumn(PRODUCT, SwapSecurity.class, new ProductProvider());
    mapColumn(START, SwapSecurity.meta().effectiveDate());
    mapColumn(MATURITY, SwapSecurity.meta().maturityDate());
    mapColumn(FREQUENCY, SwapSecurity.class, new FrequencyProvider());
    mapColumn(FLOAT_FREQUENCY, SwapSecurity.class, new FloatFrequencyProvider());
    mapColumn(QUANTITY, SwapSecurity.class, new QuantityProvider());
    mapColumn(INDEX, SwapSecurity.class, new IndexProvider());
    mapColumn(RATE, SwapSecurity.class, new RateProvider());
    mapColumn(DIRECTION, SwapSecurity.class, new PayReceiveProvider());

    // ------------------- FXForward TODO move to separate class?
    ValueProvider<FXForwardSecurity> fxForwardProductProvider = new ValueProvider<FXForwardSecurity>() {
      @Override
      public String getValue(FXForwardSecurity security) {
        CurrencyPair pair = currencyPairs.getCurrencyPair(security.getPayCurrency(), security.getReceiveCurrency());
        return pair.getBase() + "/" + pair.getCounter() + " FX Forward";
      }
    };
    // TODO FXAmounts class and formatter so individual amounts get formatted at the right time
    ValueProvider<FXForwardSecurity> fxForwardAmountProvider = new ValueProvider<FXForwardSecurity>() {
      @Override
      public String getValue(FXForwardSecurity security) {
        Double baseAmount = CurrencyUtils.getBaseAmount(security.getPayCurrency(),
                                                        security.getReceiveCurrency(),
                                                        security.getPayAmount(),
                                                        security.getReceiveAmount(),
                                                        currencyPairs);
        Double counterAmount = CurrencyUtils.getCounterAmount(security.getPayCurrency(),
                                                              security.getReceiveCurrency(),
                                                              security.getPayAmount(),
                                                              security.getReceiveAmount(),
                                                              currencyPairs);
        CurrencyPair pair = currencyPairs.getCurrencyPair(security.getPayCurrency(), security.getReceiveCurrency());
        if (pair.getBase().equals(security.getPayCurrency())) {
          baseAmount = baseAmount * -1;
        } else {
          counterAmount = counterAmount * -1;
        }
        return baseAmount + "/" + counterAmount;
      }
    };
    // TODO format rate
    ValueProvider<FXForwardSecurity> fxForwardRateProvider = new ValueProvider<FXForwardSecurity>() {
      @Override
      public Double getValue(FXForwardSecurity security) {
        return CurrencyUtils.getRate(security.getPayCurrency(),
                                     security.getReceiveCurrency(),
                                     security.getPayAmount(),
                                     security.getReceiveAmount(),
                                     currencyPairs);
      }
    };
    mapColumn(TYPE, FXForwardSecurity.class, "FX Forward");
    mapColumn(PRODUCT, FXForwardSecurity.class, fxForwardProductProvider);
    mapColumn(QUANTITY, FXForwardSecurity.class, fxForwardAmountProvider);
    mapColumn(RATE, FXForwardSecurity.class, fxForwardRateProvider);
    mapColumn(MATURITY, FXForwardSecurity.meta().forwardDate());
  }

  private void mapColumn(BlotterColumn column, MetaProperty<?> metaProp) {
    ArgumentChecker.notNull(column, "column");
    ArgumentChecker.notNull(metaProp, "metaProp");
    Class<? extends ManageableSecurity> securityType = (Class<? extends ManageableSecurity>) metaProp.metaBean().beanType();
    Map<BlotterColumn, ValueProvider> mappings = mappingsFor(securityType);
    mappings.put(column, propertyProvider(metaProp));
  }

  private <T extends ManageableSecurity> void mapColumn(BlotterColumn column,
                                                        Class<T> securityType,
                                                        ValueProvider<T> provider) {
    ArgumentChecker.notNull(column, "column");
    ArgumentChecker.notNull(securityType, "securityType");
    ArgumentChecker.notNull(provider, "provider");
    Map<BlotterColumn, ValueProvider> mappings = mappingsFor(securityType);
    mappings.put(column, provider);
  }

  private <T extends ManageableSecurity> void mapColumn(BlotterColumn column,
                                                        Class<T> securityType,
                                                        String value) {
    ArgumentChecker.notNull(column, "column");
    ArgumentChecker.notNull(securityType, "securityType");
    ArgumentChecker.notNull(value, "value");
    Map<BlotterColumn, ValueProvider> mappings = mappingsFor(securityType);
    mappings.put(column, new StaticValueProvider(value));
  }

  private <T extends ManageableSecurity> Map<BlotterColumn, ValueProvider> mappingsFor(Class<T> securityType) {
    Map<BlotterColumn, ValueProvider> securityMappings = _mappings.get(securityType);
    if (securityMappings != null) {
      return securityMappings;
    } else {
      Map<BlotterColumn, ValueProvider> newMappings = Maps.newHashMap();
      _mappings.put(securityType, newMappings);
      return newMappings;
    }
  }

  private ValueProvider propertyProvider(MetaProperty<?> property) {
    if (property == null) { // for securities where it doesn't make sense to populate a particular column
      return new StaticValueProvider("");
    } else {
      return new PropertyValueProvider(property);
    }
  }

  public Object valueFor(BlotterColumn column, ManageableSecurity security) {
    // position rows have no security
    if (security == null) {
      return "";
    }
    Map<BlotterColumn, ValueProvider> providerMap = _mappings.get(security.getClass());
    if (providerMap == null) {
      return security.getClass().getSimpleName() + " not supported";
    } else {
      ValueProvider valueProvider = providerMap.get(column);
      if (valueProvider != null) {
        return valueProvider.getValue(security);
      } else {
        return "";
      }
    }
  }

  public interface ValueProvider<T extends ManageableSecurity> {

    Object getValue(T security);
  }

  private static class PropertyValueProvider<T extends ManageableSecurity> implements ValueProvider<T> {

    private final MetaProperty<?> _property;

    private PropertyValueProvider(MetaProperty<?> property) {
      ArgumentChecker.notNull(property, "property");
      _property = property;
    }

    @Override
    public Object getValue(T security) {
      return _property.get(security);
    }
  }

  private static class StaticValueProvider implements ValueProvider {

    private final Object _value;

    private StaticValueProvider(Object value) {
      ArgumentChecker.notNull(value, "value");
      _value = value;
    }

    @Override
    public Object getValue(ManageableSecurity security) {
      return _value;
    }
  }

}

