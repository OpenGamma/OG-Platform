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
import com.opengamma.financial.security.bond.BondSecurity;
import com.opengamma.financial.security.bond.CorporateBondSecurity;
import com.opengamma.financial.security.bond.GovernmentBondSecurity;
import com.opengamma.financial.security.bond.MunicipalBondSecurity;
import com.opengamma.financial.security.capfloor.CapFloorCMSSpreadSecurity;
import com.opengamma.financial.security.capfloor.CapFloorSecurity;
import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.financial.security.option.FXOptionSecurity;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.ClassMap;
import com.opengamma.web.analytics.blotter.swap.FloatFrequencyProvider;
import com.opengamma.web.analytics.blotter.swap.FrequencyProvider;
import com.opengamma.web.analytics.blotter.swap.IndexProvider;
import com.opengamma.web.analytics.blotter.swap.PayReceiveProvider;
import com.opengamma.web.analytics.blotter.swap.ProductProvider;
import com.opengamma.web.analytics.blotter.swap.QuantityProvider;
import com.opengamma.web.analytics.blotter.swap.RateProvider;
import com.opengamma.web.analytics.blotter.swap.TypeProvider;

/**
 * Maps the properties of each blotter column to properties in each supported security type.
 */
@SuppressWarnings("unchecked")
public class BlotterColumnMapper {

  private final Map<Class<?>, Map<BlotterColumn, ValueProvider>> _mappings = new ClassMap<>();

  public BlotterColumnMapper(final CurrencyPairs currencyPairs) {
    // ------------------- Bond
    mapColumn(TYPE, GovernmentBondSecurity.class, "Government Bond");
    mapColumn(TYPE, CorporateBondSecurity.class, "Corporate Bond");
    mapColumn(TYPE, MunicipalBondSecurity.class, "Municipal Bond");
    mapColumn(PRODUCT, BondSecurity.meta().issuerName());
    mapColumn(RATE, BondSecurity.meta().couponRate());
    mapColumn(FREQUENCY, BondSecurity.meta().couponFrequency());
    mapColumn(START, BondSecurity.meta().firstCouponDate());
    mapColumn(MATURITY, BondSecurity.meta().settlementDate());

    // ------------------- CapFloor
    mapColumn(TYPE, CapFloorSecurity.class, "Cap/Floor");
    mapColumn(QUANTITY, CapFloorSecurity.meta().notional());
    mapColumn(START, CapFloorSecurity.meta().startDate());
    mapColumn(MATURITY, CapFloorSecurity.meta().maturityDate());
    mapColumn(RATE, CapFloorSecurity.meta().strike());
    mapColumn(FREQUENCY, CapFloorSecurity.meta().frequency());

    // ------------------- CapFloorCMSSpread
    ValueProvider<CapFloorCMSSpreadSecurity> capFloorCMSSpreadProductProvider =
        new ValueProvider<CapFloorCMSSpreadSecurity>() {
      @Override
      public Object getValue(CapFloorCMSSpreadSecurity security) {
        return security.getLongId().getValue() + "/" + security.getShortId().getValue();
      }
    };
    mapColumn(TYPE, CapFloorCMSSpreadSecurity.class, "Cap/Floor CMS Spread");
    mapColumn(START, CapFloorCMSSpreadSecurity.meta().startDate());
    mapColumn(MATURITY, CapFloorCMSSpreadSecurity.meta().maturityDate());
    mapColumn(QUANTITY, CapFloorCMSSpreadSecurity.meta().notional());
    mapColumn(RATE, CapFloorCMSSpreadSecurity.meta().strike());
    mapColumn(FREQUENCY, CapFloorCMSSpreadSecurity.meta().frequency());
    mapColumn(PRODUCT, CapFloorCMSSpreadSecurity.class, capFloorCMSSpreadProductProvider);

    // ------------------- FRA
    mapColumn(TYPE, FRASecurity.class, "FRA");
    mapColumn(PRODUCT, FRASecurity.meta().currency());
    mapColumn(QUANTITY, FRASecurity.meta().amount());
    mapColumn(START, FRASecurity.meta().startDate());
    mapColumn(MATURITY, FRASecurity.meta().endDate());
    mapColumn(RATE, FRASecurity.meta().rate());

    // ------------------- FXOption
    ValueProvider<FXOptionSecurity> fxOptionProductProvider = new ValueProvider<FXOptionSecurity>() {
      @Override
      public Object getValue(FXOptionSecurity security) {
        CurrencyPair pair = currencyPairs.getCurrencyPair(security.getPutCurrency(), security.getCallCurrency());
        return pair.getBase() + "/" + pair.getCounter();
      }
    };
    ValueProvider<FXOptionSecurity> fxOptionQuantityProvider = new ValueProvider<FXOptionSecurity>() {
      @Override
      public FXAmounts getValue(FXOptionSecurity security) {
        return FXAmounts.forOption(security, currencyPairs);
      }
    };
    ValueProvider<FXOptionSecurity> fxOptionRateProvider = new ValueProvider<FXOptionSecurity>() {
      @Override
      public Object getValue(FXOptionSecurity security) {
        return CurrencyUtils.getRate(security.getPutCurrency(),
                                     security.getCallCurrency(),
                                     security.getPutAmount(),
                                     security.getCallAmount(),
                                     currencyPairs);
      }
    };
    mapColumn(TYPE, FXOptionSecurity.class, "FX Option");
    mapColumn(MATURITY, FXOptionSecurity.meta().expiry());
    mapColumn(PRODUCT, FXOptionSecurity.class, fxOptionProductProvider);
    mapColumn(QUANTITY, FXOptionSecurity.class, fxOptionQuantityProvider);
    mapColumn(RATE, FXOptionSecurity.class, fxOptionRateProvider);

    // ------------------- Swap
    mapColumn(TYPE, SwapSecurity.class, new TypeProvider());
    mapColumn(PRODUCT, SwapSecurity.class, new ProductProvider());
    mapColumn(START, SwapSecurity.meta().effectiveDate());
    mapColumn(MATURITY, SwapSecurity.meta().maturityDate());
    mapColumn(FREQUENCY, SwapSecurity.class, new FrequencyProvider());
    mapColumn(FLOAT_FREQUENCY, SwapSecurity.class, new FloatFrequencyProvider());
    mapColumn(QUANTITY, SwapSecurity.class, new QuantityProvider());
    mapColumn(INDEX, SwapSecurity.class, new IndexProvider());
    mapColumn(RATE, SwapSecurity.class, new RateProvider());
    mapColumn(DIRECTION, SwapSecurity.class, new PayReceiveProvider());

    // ------------------- FXForward
    ValueProvider<FXForwardSecurity> fxForwardProductProvider = new ValueProvider<FXForwardSecurity>() {
      @Override
      public String getValue(FXForwardSecurity security) {
        CurrencyPair pair = currencyPairs.getCurrencyPair(security.getPayCurrency(), security.getReceiveCurrency());
        return pair.getBase() + "/" + pair.getCounter();
      }
    };
    ValueProvider<FXForwardSecurity> fxForwardQuantityProvider = new ValueProvider<FXForwardSecurity>() {
      @Override
      public FXAmounts getValue(FXForwardSecurity security) {
        return FXAmounts.forForward(security, currencyPairs);
      }
    };
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
    mapColumn(QUANTITY, FXForwardSecurity.class, fxForwardQuantityProvider);
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
    return getValue(column, security, security.getClass());
  }

  private Object getValue(BlotterColumn column, ManageableSecurity security, Class<?> type) {
    Map<BlotterColumn, ValueProvider> providerMap = getMappingsForType(type);
    if (providerMap == null) {
      return "";
    } else {
      ValueProvider valueProvider = providerMap.get(column);
      if (valueProvider != null) {
        return valueProvider.getValue(security);
      } else {
        Class<?> superclass = type.getSuperclass();
        if (superclass == null) {
          return "";
        } else {
          return getValue(column, security, superclass);
        }
      }
    }
  }

  private Map<BlotterColumn, ValueProvider> getMappingsForType(Class<?> type) {
    Map<BlotterColumn, ValueProvider> providerMap = _mappings.get(type);
    if (providerMap != null) {
      return providerMap;
    } else {
      Class<?> superclass = type.getSuperclass();
      if (superclass == null) {
        return null;
      } else {
        return getMappingsForType(superclass);
      }
    }
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

