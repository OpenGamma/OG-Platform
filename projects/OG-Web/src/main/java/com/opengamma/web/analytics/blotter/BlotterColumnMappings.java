/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.blotter;

import static com.opengamma.web.analytics.blotter.BlotterColumn.FREQUENCY;
import static com.opengamma.web.analytics.blotter.BlotterColumn.MATURITY;
import static com.opengamma.web.analytics.blotter.BlotterColumn.PRODUCT;
import static com.opengamma.web.analytics.blotter.BlotterColumn.QUANTITY;
import static com.opengamma.web.analytics.blotter.BlotterColumn.RATE;
import static com.opengamma.web.analytics.blotter.BlotterColumn.START;
import static com.opengamma.web.analytics.blotter.BlotterColumn.TYPE;

import java.util.Map;

import org.joda.beans.MetaProperty;
import org.joda.convert.StringConvert;
import org.joda.convert.StringConverter;

import com.google.common.collect.Maps;
import com.opengamma.financial.currency.CurrencyPair;
import com.opengamma.financial.currency.CurrencyPairs;
import com.opengamma.financial.currency.CurrencyUtils;
import com.opengamma.financial.security.capfloor.CapFloorSecurity;
import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.ArgumentChecker;

/**
 * Maps the properties of each
 */
@SuppressWarnings("unchecked")
public class BlotterColumnMappings {

  private final Map<Class<? extends ManageableSecurity>, Map<BlotterColumn, ValueProvider>> _mappings = Maps.newHashMap();
  private final StringConvert _stringConvert;

  // TODO make this static and use BlotterResource converters
  public BlotterColumnMappings(StringConvert stringConvert, final CurrencyPairs currencyPairs) {
    ArgumentChecker.notNull(stringConvert, "stringConvert");
    _stringConvert = stringConvert;

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

    // ------------------- FXForward
    ValueProvider<FXForwardSecurity> fxForwardProductProvider = new ValueProvider<FXForwardSecurity>() {
      @Override
      public String getValue(FXForwardSecurity security) {
        CurrencyPair pair = currencyPairs.getCurrencyPair(security.getPayCurrency(), security.getReceiveCurrency());
        return pair.getBase() + "/" + pair.getCounter() + " FX Forward";
      }
    };
    // TODO format amounts
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
      public String getValue(FXForwardSecurity security) {
        Double rate = CurrencyUtils.getRate(security.getPayCurrency(),
                                            security.getReceiveCurrency(),
                                            security.getPayAmount(),
                                            security.getReceiveAmount(),
                                            currencyPairs);
        return Double.toString(rate);
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
      StringConverter<?> converter = _stringConvert.findConverter(property.propertyType());
      return new PropertyValueProvider(property, converter);
    }
  }

  public String valueFor(BlotterColumn column, ManageableSecurity security) {
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

  private interface ValueProvider<T extends ManageableSecurity> {

    String getValue(T security);
  }

  private static class PropertyValueProvider<T extends ManageableSecurity> implements ValueProvider<T> {

    private final MetaProperty<?> _property;
    private final StringConverter _converter;

    private PropertyValueProvider(MetaProperty<?> property, StringConverter converter) {
      ArgumentChecker.notNull(property, "property");
      ArgumentChecker.notNull(converter, "converter");
      _converter = converter;
      _property = property;
    }

    @SuppressWarnings("unchecked")
    @Override
    public String getValue(T security) {
      Object value = _property.get(security);
      if (value == null) {
        return "";
      } else {
        return _converter.convertToString(value);
      }
    }
  }

  private static class StaticValueProvider implements ValueProvider {

    private final String _value;

    private StaticValueProvider(String value) {
      ArgumentChecker.notNull(value, "value");
      _value = value;
    }

    @Override
    public String getValue(ManageableSecurity security) {
      return _value;
    }
  }

}


