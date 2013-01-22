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
import com.opengamma.web.analytics.blotter.swap.FloatFrequencyProvider;
import com.opengamma.web.analytics.blotter.swap.FrequencyProvider;
import com.opengamma.web.analytics.blotter.swap.IndexProvider;
import com.opengamma.web.analytics.blotter.swap.PayReceiveProvider;
import com.opengamma.web.analytics.blotter.swap.ProductProvider;
import com.opengamma.web.analytics.blotter.swap.QuantityProvider;
import com.opengamma.web.analytics.blotter.swap.RateProvider;
import com.opengamma.web.analytics.blotter.swap.TypeProvider;

/**
 *
 */
public class DefaultBlotterColumnMappings {

  private DefaultBlotterColumnMappings() {
  }

  public static BlotterColumnMapper create(final CurrencyPairs currencyPairs) {
    BlotterColumnMapper mapper = new BlotterColumnMapper();
    // ------------------- Bond
    mapper.mapColumn(TYPE, GovernmentBondSecurity.class, "Government Bond");
    mapper.mapColumn(TYPE, CorporateBondSecurity.class, "Corporate Bond");
    mapper.mapColumn(TYPE, MunicipalBondSecurity.class, "Municipal Bond");
    mapper.mapColumn(PRODUCT, BondSecurity.meta().issuerName());
    mapper.mapColumn(RATE, BondSecurity.meta().couponRate());
    mapper.mapColumn(FREQUENCY, BondSecurity.meta().couponFrequency());
    mapper.mapColumn(START, BondSecurity.meta().firstCouponDate());
    mapper.mapColumn(MATURITY, BondSecurity.meta().settlementDate());

    // ------------------- CapFloor
    mapper.mapColumn(TYPE, CapFloorSecurity.class, "Cap/Floor");
    mapper.mapColumn(QUANTITY, CapFloorSecurity.meta().notional());
    mapper.mapColumn(START, CapFloorSecurity.meta().startDate());
    mapper.mapColumn(MATURITY, CapFloorSecurity.meta().maturityDate());
    mapper.mapColumn(RATE, CapFloorSecurity.meta().strike());
    mapper.mapColumn(FREQUENCY, CapFloorSecurity.meta().frequency());

    // ------------------- CapFloorCMSSpread
    ValueProvider<CapFloorCMSSpreadSecurity> capFloorCMSSpreadProductProvider = new ValueProvider<CapFloorCMSSpreadSecurity>() {
      @Override
      public Object getValue(CapFloorCMSSpreadSecurity security) {
        return security.getLongId().getValue() + "/" + security.getShortId().getValue();
      }
    };
    mapper.mapColumn(TYPE, CapFloorCMSSpreadSecurity.class, "Cap/Floor CMS Spread");
    mapper.mapColumn(START, CapFloorCMSSpreadSecurity.meta().startDate());
    mapper.mapColumn(MATURITY, CapFloorCMSSpreadSecurity.meta().maturityDate());
    mapper.mapColumn(QUANTITY, CapFloorCMSSpreadSecurity.meta().notional());
    mapper.mapColumn(RATE, CapFloorCMSSpreadSecurity.meta().strike());
    mapper.mapColumn(FREQUENCY, CapFloorCMSSpreadSecurity.meta().frequency());
    mapper.mapColumn(PRODUCT, CapFloorCMSSpreadSecurity.class, capFloorCMSSpreadProductProvider);

    // ------------------- FRA
    mapper.mapColumn(TYPE, FRASecurity.class, "FRA");
    mapper.mapColumn(PRODUCT, FRASecurity.meta().currency());
    mapper.mapColumn(QUANTITY, FRASecurity.meta().amount());
    mapper.mapColumn(START, FRASecurity.meta().startDate());
    mapper.mapColumn(MATURITY, FRASecurity.meta().endDate());
    mapper.mapColumn(RATE, FRASecurity.meta().rate());

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
    mapper.mapColumn(TYPE, FXOptionSecurity.class, "FX Option");
    mapper.mapColumn(MATURITY, FXOptionSecurity.meta().expiry());
    mapper.mapColumn(PRODUCT, FXOptionSecurity.class, fxOptionProductProvider);
    mapper.mapColumn(QUANTITY, FXOptionSecurity.class, fxOptionQuantityProvider);
    mapper.mapColumn(RATE, FXOptionSecurity.class, fxOptionRateProvider);

    // ------------------- Swap
    mapper.mapColumn(TYPE, SwapSecurity.class, new TypeProvider());
    mapper.mapColumn(PRODUCT, SwapSecurity.class, new ProductProvider());
    mapper.mapColumn(START, SwapSecurity.meta().effectiveDate());
    mapper.mapColumn(MATURITY, SwapSecurity.meta().maturityDate());
    mapper.mapColumn(FREQUENCY, SwapSecurity.class, new FrequencyProvider());
    mapper.mapColumn(FLOAT_FREQUENCY, SwapSecurity.class, new FloatFrequencyProvider());
    mapper.mapColumn(QUANTITY, SwapSecurity.class, new QuantityProvider());
    mapper.mapColumn(INDEX, SwapSecurity.class, new IndexProvider());
    mapper.mapColumn(RATE, SwapSecurity.class, new RateProvider());
    mapper.mapColumn(DIRECTION, SwapSecurity.class, new PayReceiveProvider());

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
    mapper.mapColumn(TYPE, FXForwardSecurity.class, "FX Forward");
    mapper.mapColumn(PRODUCT, FXForwardSecurity.class, fxForwardProductProvider);
    mapper.mapColumn(QUANTITY, FXForwardSecurity.class, fxForwardQuantityProvider);
    mapper.mapColumn(RATE, FXForwardSecurity.class, fxForwardRateProvider);
    mapper.mapColumn(MATURITY, FXForwardSecurity.meta().forwardDate());

    return mapper;
  }
}
