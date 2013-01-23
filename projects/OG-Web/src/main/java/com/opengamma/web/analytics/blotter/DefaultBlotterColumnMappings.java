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
import com.opengamma.financial.security.equity.EquityVarianceSwapSecurity;
import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.financial.security.option.FXBarrierOptionSecurity;
import com.opengamma.financial.security.option.FXDigitalOptionSecurity;
import com.opengamma.financial.security.option.FXOptionSecurity;
import com.opengamma.financial.security.option.NonDeliverableFXOptionSecurity;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.money.Currency;
import com.opengamma.web.analytics.blotter.swap.SwapFloatFrequencyProvider;
import com.opengamma.web.analytics.blotter.swap.SwapFrequencyProvider;
import com.opengamma.web.analytics.blotter.swap.SwapIndexProvider;
import com.opengamma.web.analytics.blotter.swap.SwapPayReceiveProvider;
import com.opengamma.web.analytics.blotter.swap.SwapProductProvider;
import com.opengamma.web.analytics.blotter.swap.SwapQuantityProvider;
import com.opengamma.web.analytics.blotter.swap.SwapRateProvider;
import com.opengamma.web.analytics.blotter.swap.SwapTypeProvider;

/**
 *
 */
public class DefaultBlotterColumnMappings {

  private DefaultBlotterColumnMappings() {
  }

  public static BlotterColumnMapper create(final CurrencyPairs currencyPairs) {
    BlotterColumnMapper mapper = new BlotterColumnMapper();

    // ------------------- EquityVarianceSwap
    mapper.mapColumn(TYPE, EquityVarianceSwapSecurity.class, "Equity Variance Swap");
    mapper.mapColumn(QUANTITY, EquityVarianceSwapSecurity.meta().notional());

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

    // ------------------- Swap
    mapper.mapColumn(TYPE, SwapSecurity.class, new SwapTypeProvider());
    mapper.mapColumn(PRODUCT, SwapSecurity.class, new SwapProductProvider());
    mapper.mapColumn(START, SwapSecurity.meta().effectiveDate());
    mapper.mapColumn(MATURITY, SwapSecurity.meta().maturityDate());
    mapper.mapColumn(FREQUENCY, SwapSecurity.class, new SwapFrequencyProvider());
    mapper.mapColumn(FLOAT_FREQUENCY, SwapSecurity.class, new SwapFloatFrequencyProvider());
    mapper.mapColumn(QUANTITY, SwapSecurity.class, new SwapQuantityProvider());
    mapper.mapColumn(INDEX, SwapSecurity.class, new SwapIndexProvider());
    mapper.mapColumn(RATE, SwapSecurity.class, new SwapRateProvider());
    mapper.mapColumn(DIRECTION, SwapSecurity.class, new SwapPayReceiveProvider());

    // ------------------- FXForward
    mapper.mapColumn(TYPE, FXForwardSecurity.class, "FX Forward");
    mapper.mapColumn(MATURITY, FXForwardSecurity.meta().forwardDate());
    mapper.mapColumn(PRODUCT, FXForwardSecurity.class, new ValueProvider<FXForwardSecurity>() {
      @Override
      public String getValue(FXForwardSecurity security) {
        return currencyPairName(security.getPayCurrency(), security.getReceiveCurrency(), currencyPairs);
      }
    });
    mapper.mapColumn(QUANTITY, FXForwardSecurity.class, new ValueProvider<FXForwardSecurity>() {
      @Override
      public FXAmounts getValue(FXForwardSecurity security) {
        return FXAmounts.forForward(security, currencyPairs);
      }
    });
    mapper.mapColumn(RATE, FXForwardSecurity.class, new ValueProvider<FXForwardSecurity>() {
      @Override
      public Double getValue(FXForwardSecurity security) {
        return CurrencyUtils.getRate(security.getPayCurrency(),
                                     security.getReceiveCurrency(),
                                     security.getPayAmount(),
                                     security.getReceiveAmount(),
                                     currencyPairs);
      }
    });

    // ------------------- FXOption
    mapper.mapColumn(TYPE, FXOptionSecurity.class, "FX Option");
    mapper.mapColumn(MATURITY, FXOptionSecurity.meta().expiry());
    mapper.mapColumn(PRODUCT, FXOptionSecurity.class, new ValueProvider<FXOptionSecurity>() {
      @Override
      public Object getValue(FXOptionSecurity security) {
        CurrencyPair pair = currencyPairs.getCurrencyPair(security.getPutCurrency(), security.getCallCurrency());
        return pair.getBase() + "/" + pair.getCounter();
      }
    });
    mapper.mapColumn(QUANTITY, FXOptionSecurity.class, new ValueProvider<FXOptionSecurity>() {
      @Override
      public FXAmounts getValue(FXOptionSecurity security) {
        return FXAmounts.forOption(security, currencyPairs);
      }
    });
    mapper.mapColumn(RATE, FXOptionSecurity.class, new ValueProvider<FXOptionSecurity>() {
      @Override
      public Object getValue(FXOptionSecurity security) {
        return CurrencyUtils.getRate(security.getPutCurrency(),
                                     security.getCallCurrency(),
                                     security.getPutAmount(),
                                     security.getCallAmount(),
                                     currencyPairs);
      }
    });

    // ------------------- FXBarrierOption
    mapper.mapColumn(TYPE, FXBarrierOptionSecurity.class, "FX Barrier Option");
    mapper.mapColumn(MATURITY, FXBarrierOptionSecurity.meta().expiry());
    mapper.mapColumn(QUANTITY, FXBarrierOptionSecurity.class, new ValueProvider<FXBarrierOptionSecurity>() {
      @Override
      public Object getValue(FXBarrierOptionSecurity security) {
        return FXAmounts.forBarrierOption(security, currencyPairs);
      }
    });
    mapper.mapColumn(PRODUCT, FXBarrierOptionSecurity.class, new ValueProvider<FXBarrierOptionSecurity>() {
      @Override
      public Object getValue(FXBarrierOptionSecurity security) {
        return currencyPairName(security.getPutCurrency(), security.getCallCurrency(), currencyPairs);
      }
    });
    mapper.mapColumn(RATE, FXBarrierOptionSecurity.class, new ValueProvider<FXBarrierOptionSecurity>() {
      @Override
      public Object getValue(FXBarrierOptionSecurity security) {
        return CurrencyUtils.getRate(security.getPutCurrency(),
                                     security.getCallCurrency(),
                                     security.getPutAmount(),
                                     security.getCallAmount(),
                                     currencyPairs);
      }
    });

    // ------------------- NonDeliverableFXOption
    mapper.mapColumn(TYPE, NonDeliverableFXOptionSecurity.class, "Non-deliverable FX Option");
    mapper.mapColumn(MATURITY, NonDeliverableFXOptionSecurity.meta().expiry());
    mapper.mapColumn(QUANTITY, NonDeliverableFXOptionSecurity.class, new ValueProvider<NonDeliverableFXOptionSecurity>() {
      @Override
      public Object getValue(NonDeliverableFXOptionSecurity security) {
        return FXAmounts.forNonDeliverableOption(security, currencyPairs);
      }
    });
    mapper.mapColumn(PRODUCT, NonDeliverableFXOptionSecurity.class, new ValueProvider<NonDeliverableFXOptionSecurity>() {
      @Override
      public Object getValue(NonDeliverableFXOptionSecurity security) {
        return currencyPairName(security.getPutCurrency(), security.getCallCurrency(), currencyPairs);
      }
    });
    mapper.mapColumn(RATE, NonDeliverableFXOptionSecurity.class, new ValueProvider<NonDeliverableFXOptionSecurity>() {
      @Override
      public Object getValue(NonDeliverableFXOptionSecurity security) {
        return CurrencyUtils.getRate(security.getPutCurrency(),
                                     security.getCallCurrency(),
                                     security.getPutAmount(),
                                     security.getCallAmount(),
                                     currencyPairs);
      }
    });
    // ------------------- FXDigitalOption
    mapper.mapColumn(TYPE, FXDigitalOptionSecurity.class, "FX Digital Option");
    mapper.mapColumn(MATURITY, FXDigitalOptionSecurity.meta().expiry());
    mapper.mapColumn(QUANTITY, FXDigitalOptionSecurity.class, new ValueProvider<FXDigitalOptionSecurity>() {
      @Override
      public Object getValue(FXDigitalOptionSecurity security) {
        return FXAmounts.forDigitalOption(security, currencyPairs);
      }
    });
    mapper.mapColumn(PRODUCT, FXDigitalOptionSecurity.class, new ValueProvider<FXDigitalOptionSecurity>() {
      @Override
      public Object getValue(FXDigitalOptionSecurity security) {
        return currencyPairName(security.getPutCurrency(), security.getCallCurrency(), currencyPairs);
      }
    });
    mapper.mapColumn(RATE, FXDigitalOptionSecurity.class, new ValueProvider<FXDigitalOptionSecurity>() {
      @Override
      public Object getValue(FXDigitalOptionSecurity security) {
        return CurrencyUtils.getRate(security.getPutCurrency(),
                                     security.getCallCurrency(),
                                     security.getPutAmount(),
                                     security.getCallAmount(),
                                     currencyPairs);
      }
    });

    // -------------------
    // fallback type name for securities that haven't been explicitly mapped
    mapper.mapColumn(TYPE, ManageableSecurity.class, new ValueProvider<ManageableSecurity>() {
      @Override
      public Object getValue(ManageableSecurity security) {
        return security.getClass().getSimpleName();
      }
    });
    return mapper;
  }

  private static String currencyPairName(Currency payCurrency, Currency receiveCurrency, CurrencyPairs currencyPairs) {
    CurrencyPair pair = currencyPairs.getCurrencyPair(payCurrency, receiveCurrency);
    if (pair != null) {
      return pair.getName();
    } else {
      return payCurrency.getCode() + "/" + receiveCurrency.getCode();
    }
  }
}
