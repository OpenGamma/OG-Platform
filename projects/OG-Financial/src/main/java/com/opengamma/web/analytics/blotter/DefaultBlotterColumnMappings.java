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
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.security.equity.EquityVarianceSwapSecurity;
import com.opengamma.financial.security.forward.AgricultureForwardSecurity;
import com.opengamma.financial.security.forward.CommodityForwardSecurity;
import com.opengamma.financial.security.forward.EnergyForwardSecurity;
import com.opengamma.financial.security.forward.MetalForwardSecurity;
import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.financial.security.future.AgricultureFutureSecurity;
import com.opengamma.financial.security.future.BondFutureSecurity;
import com.opengamma.financial.security.future.EnergyFutureSecurity;
import com.opengamma.financial.security.future.EquityFutureSecurity;
import com.opengamma.financial.security.future.EquityIndexDividendFutureSecurity;
import com.opengamma.financial.security.future.FXFutureSecurity;
import com.opengamma.financial.security.future.FutureSecurity;
import com.opengamma.financial.security.future.IndexFutureSecurity;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
import com.opengamma.financial.security.future.MetalFutureSecurity;
import com.opengamma.financial.security.future.StockFutureSecurity;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.financial.security.fx.NonDeliverableFXForwardSecurity;
import com.opengamma.financial.security.option.BondFutureOptionSecurity;
import com.opengamma.financial.security.option.CommodityFutureOptionSecurity;
import com.opengamma.financial.security.option.EquityBarrierOptionSecurity;
import com.opengamma.financial.security.option.EquityIndexOptionSecurity;
import com.opengamma.financial.security.option.EquityOptionSecurity;
import com.opengamma.financial.security.option.FXBarrierOptionSecurity;
import com.opengamma.financial.security.option.FXDigitalOptionSecurity;
import com.opengamma.financial.security.option.FXOptionSecurity;
import com.opengamma.financial.security.option.IRFutureOptionSecurity;
import com.opengamma.financial.security.option.NonDeliverableFXDigitalOptionSecurity;
import com.opengamma.financial.security.option.NonDeliverableFXOptionSecurity;
import com.opengamma.financial.security.option.SwaptionSecurity;
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

    // ------------------- Bond
    mapper.mapColumn(TYPE, GovernmentBondSecurity.class, "Government Bond");
    mapper.mapColumn(TYPE, CorporateBondSecurity.class, "Corporate Bond");
    mapper.mapColumn(TYPE, MunicipalBondSecurity.class, "Municipal Bond");
    mapper.mapColumn(PRODUCT, BondSecurity.meta().issuerName());
    mapper.mapColumn(RATE, BondSecurity.meta().couponRate());
    mapper.mapColumn(FREQUENCY, BondSecurity.meta().couponFrequency());
    mapper.mapColumn(START, BondSecurity.meta().firstCouponDate());
    mapper.mapColumn(MATURITY, BondSecurity.meta().settlementDate());

    // ------------------- BondFutureOption
    mapper.mapColumn(TYPE, BondFutureOptionSecurity.class, "Bond Future Option");
    mapper.mapColumn(MATURITY, BondFutureOptionSecurity.meta().expiry());
    mapper.mapColumn(RATE, BondFutureOptionSecurity.meta().strike());
    mapper.mapColumn(DIRECTION, BondFutureOptionSecurity.meta().optionType());

    // ------------------- CommodityForward
    mapper.mapColumn(TYPE, MetalForwardSecurity.class, "Metal Forward");
    mapper.mapColumn(TYPE, EnergyForwardSecurity.class, "Energy Forward");
    mapper.mapColumn(TYPE, AgricultureForwardSecurity.class, "Agriculture Forward");
    mapper.mapColumn(MATURITY, CommodityForwardSecurity.meta().expiry());
    mapper.mapColumn(QUANTITY, CommodityForwardSecurity.meta().unitAmount());
    mapper.mapColumn(PRODUCT, CommodityForwardSecurity.meta().contractCategory());

    // ------------------- CommodityFutureOption
    mapper.mapColumn(TYPE, CommodityFutureOptionSecurity.class, "Commodity Future Option");
    mapper.mapColumn(MATURITY, CommodityFutureOptionSecurity.meta().expiry());
    mapper.mapColumn(RATE, CommodityFutureOptionSecurity.meta().strike());
    mapper.mapColumn(DIRECTION, CommodityFutureOptionSecurity.meta().optionType());

    // ------------------- CapFloor
    mapper.mapColumn(TYPE, CapFloorSecurity.class, "Cap/Floor");
    mapper.mapColumn(QUANTITY, CapFloorSecurity.meta().notional());
    mapper.mapColumn(START, CapFloorSecurity.meta().startDate());
    mapper.mapColumn(MATURITY, CapFloorSecurity.meta().maturityDate());
    mapper.mapColumn(RATE, CapFloorSecurity.meta().strike());
    mapper.mapColumn(FREQUENCY, CapFloorSecurity.meta().frequency());

    // ------------------- CapFloorCMSSpread
    mapper.mapColumn(TYPE, CapFloorCMSSpreadSecurity.class, "Cap/Floor CMS Spread");
    mapper.mapColumn(START, CapFloorCMSSpreadSecurity.meta().startDate());
    mapper.mapColumn(MATURITY, CapFloorCMSSpreadSecurity.meta().maturityDate());
    mapper.mapColumn(QUANTITY, CapFloorCMSSpreadSecurity.meta().notional());
    mapper.mapColumn(RATE, CapFloorCMSSpreadSecurity.meta().strike());
    mapper.mapColumn(FREQUENCY, CapFloorCMSSpreadSecurity.meta().frequency());
    mapper.mapColumn(PRODUCT, CapFloorCMSSpreadSecurity.class, new CellValueProvider<CapFloorCMSSpreadSecurity>() {
      @Override
      public Object getValue(CapFloorCMSSpreadSecurity security) {
        return security.getLongId().getValue() + "/" + security.getShortId().getValue();
      }
    });

    // ------------------- Futures
    mapper.mapColumn(MATURITY, FutureSecurity.meta().expiry());
    mapper.mapColumn(QUANTITY, FutureSecurity.meta().unitAmount());
    mapper.mapColumn(PRODUCT, FutureSecurity.meta().contractCategory());
    mapper.mapColumn(TYPE, BondFutureSecurity.class, "Bond Future");
    mapper.mapColumn(TYPE, EnergyFutureSecurity.class, "Energy Future");
    mapper.mapColumn(TYPE, MetalFutureSecurity.class, "Metal Future");
    mapper.mapColumn(TYPE, AgricultureFutureSecurity.class, "Agriculture Future");
    mapper.mapColumn(TYPE, FXFutureSecurity.class, "FX Future");
    mapper.mapColumn(TYPE, StockFutureSecurity.class, "Stock Future");
    mapper.mapColumn(TYPE, IndexFutureSecurity.class, "Index Future");
    mapper.mapColumn(TYPE, EquityFutureSecurity.class, "Equity Future");
    mapper.mapColumn(TYPE, EquityIndexDividendFutureSecurity.class, "Equity Index Dividend Future");

    // ------------------- IndexFuture
    mapper.mapColumn(INDEX, IndexFutureSecurity.class, new CellValueProvider<IndexFutureSecurity>() {
      @Override
      public Object getValue(IndexFutureSecurity security) {
        return security.getUnderlyingId().getValue();
      }
    });

    // ------------------- InterestRateFuture
    mapper.mapColumn(TYPE, InterestRateFutureSecurity.class, "Interest Rate Future");
    mapper.mapColumn(PRODUCT, InterestRateFutureSecurity.meta().currency());
    mapper.mapColumn(INDEX, InterestRateFutureSecurity.class, new CellValueProvider<InterestRateFutureSecurity>() {
      @Override
      public Object getValue(InterestRateFutureSecurity security) {
        return security.getUnderlyingId().getValue();
      }
    });

    // ------------------- IRFutureOption
    mapper.mapColumn(TYPE, IRFutureOptionSecurity.class, "Interest Rate Future Option");
    mapper.mapColumn(MATURITY, IRFutureOptionSecurity.meta().expiry());
    mapper.mapColumn(RATE, IRFutureOptionSecurity.meta().strike());
    mapper.mapColumn(DIRECTION, IRFutureOptionSecurity.meta().optionType());

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

    // ------------------- Swaption
    mapper.mapColumn(TYPE, SwaptionSecurity.class, "Swaption");
    mapper.mapColumn(MATURITY, SwaptionSecurity.meta().expiry());
    // TODO this is tricky - need the underlying swap. where can I store it?
    // TODO need a security link on swaption so the target can be resolved
    // otherwise I need a security source / master in the provider. and hit the DB to get the swap for every cell
    // TODO direction
    // TODO product
    // TODO start
    // TODO quantity
    // TODO frequency
    // TODO rate
    // TODO what about float freq?

    // ------------------- Equity
    mapper.mapColumn(TYPE, EquitySecurity.class, "Equity");
    mapper.mapColumn(PRODUCT, EquitySecurity.meta().companyName());

    // ------------------- EquityOption
    mapper.mapColumn(TYPE, EquityOptionSecurity.class, "Equity Option");
    mapper.mapColumn(RATE, EquityOptionSecurity.meta().strike());
    mapper.mapColumn(MATURITY, EquityOptionSecurity.meta().expiry());
    mapper.mapColumn(DIRECTION, EquityOptionSecurity.meta().optionType());

    // ------------------- EquityIndexOption
    mapper.mapColumn(TYPE, EquityIndexOptionSecurity.class, "Equity Index Option");
    mapper.mapColumn(RATE, EquityIndexOptionSecurity.meta().strike());
    mapper.mapColumn(MATURITY, EquityIndexOptionSecurity.meta().expiry());
    mapper.mapColumn(DIRECTION, EquityIndexOptionSecurity.meta().optionType());
    mapper.mapColumn(PRODUCT, EquityIndexOptionSecurity.class, new CellValueProvider<EquityIndexOptionSecurity>() {
      @Override
      public Object getValue(EquityIndexOptionSecurity security) {
        return security.getUnderlyingId().getValue();
      }
    });

    // ------------------- EquityBarrierOption
    mapper.mapColumn(TYPE, EquityBarrierOptionSecurity.class, "Equity Barrier Option");
    mapper.mapColumn(RATE, EquityBarrierOptionSecurity.meta().strike());
    mapper.mapColumn(MATURITY, EquityBarrierOptionSecurity.meta().expiry());
    mapper.mapColumn(DIRECTION, EquityBarrierOptionSecurity.meta().optionType());
    mapper.mapColumn(PRODUCT, EquityBarrierOptionSecurity.class, new CellValueProvider<EquityBarrierOptionSecurity>() {
      @Override
      public Object getValue(EquityBarrierOptionSecurity security) {
        return security.getUnderlyingId().getValue();
      }
    });

    // ------------------- EquityVarianceSwap
    mapper.mapColumn(TYPE, EquityVarianceSwapSecurity.class, "Equity Variance Swap");
    mapper.mapColumn(QUANTITY, EquityVarianceSwapSecurity.meta().notional());

    // ------------------- FXForward
    mapper.mapColumn(TYPE, FXForwardSecurity.class, "FX Forward");
    mapper.mapColumn(MATURITY, FXForwardSecurity.meta().forwardDate());
    mapper.mapColumn(PRODUCT, FXForwardSecurity.class, new CellValueProvider<FXForwardSecurity>() {
      @Override
      public String getValue(FXForwardSecurity security) {
        return currencyPairName(security.getPayCurrency(), security.getReceiveCurrency(), currencyPairs);
      }
    });
    mapper.mapColumn(QUANTITY, FXForwardSecurity.class, new CellValueProvider<FXForwardSecurity>() {
      @Override
      public FXAmounts getValue(FXForwardSecurity security) {
        return FXAmounts.forForward(security.getPayCurrency(),
                                    security.getReceiveCurrency(),
                                    security.getPayAmount(),
                                    security.getReceiveAmount(),
                                    currencyPairs);
      }
    });
    mapper.mapColumn(RATE, FXForwardSecurity.class, new CellValueProvider<FXForwardSecurity>() {
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
    mapper.mapColumn(PRODUCT, FXOptionSecurity.class, new CellValueProvider<FXOptionSecurity>() {
      @Override
      public Object getValue(FXOptionSecurity security) {
        CurrencyPair pair = currencyPairs.getCurrencyPair(security.getPutCurrency(), security.getCallCurrency());
        return pair.getBase() + "/" + pair.getCounter();
      }
    });
    mapper.mapColumn(QUANTITY, FXOptionSecurity.class, new CellValueProvider<FXOptionSecurity>() {
      @Override
      public FXAmounts getValue(FXOptionSecurity security) {
        return FXAmounts.forOption(security.getPutCurrency(),
                                   security.getCallCurrency(),
                                   security.getPutAmount(),
                                   security.getCallAmount(),
                                   security.isLong(),
                                   currencyPairs);
      }
    });
    mapper.mapColumn(RATE, FXOptionSecurity.class, new CellValueProvider<FXOptionSecurity>() {
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
    mapper.mapColumn(QUANTITY, FXBarrierOptionSecurity.class, new CellValueProvider<FXBarrierOptionSecurity>() {
      @Override
      public Object getValue(FXBarrierOptionSecurity security) {
        return FXAmounts.forOption(security.getPutCurrency(),
                                   security.getCallCurrency(),
                                   security.getPutAmount(),
                                   security.getCallAmount(),
                                   security.isLong(),
                                   currencyPairs);
      }
    });
    mapper.mapColumn(PRODUCT, FXBarrierOptionSecurity.class, new CellValueProvider<FXBarrierOptionSecurity>() {
      @Override
      public Object getValue(FXBarrierOptionSecurity security) {
        return currencyPairName(security.getPutCurrency(), security.getCallCurrency(), currencyPairs);
      }
    });
    mapper.mapColumn(RATE, FXBarrierOptionSecurity.class, new CellValueProvider<FXBarrierOptionSecurity>() {
      @Override
      public Object getValue(FXBarrierOptionSecurity security) {
        return CurrencyUtils.getRate(security.getPutCurrency(),
                                     security.getCallCurrency(),
                                     security.getPutAmount(),
                                     security.getCallAmount(),
                                     currencyPairs);
      }
    });

    // ------------------- NonDeliverableFXDigitalOption
    mapper.mapColumn(TYPE, NonDeliverableFXDigitalOptionSecurity.class, "Non Deliverable FX Digital Option");
    mapper.mapColumn(MATURITY, NonDeliverableFXDigitalOptionSecurity.meta().expiry());
    mapper.mapColumn(PRODUCT, NonDeliverableFXDigitalOptionSecurity.class, new CellValueProvider<NonDeliverableFXDigitalOptionSecurity>() {
      @Override
      public Object getValue(NonDeliverableFXDigitalOptionSecurity security) {
        return currencyPairName(security.getPutCurrency(), security.getCallCurrency(), currencyPairs);
      }
    });
    mapper.mapColumn(QUANTITY, NonDeliverableFXDigitalOptionSecurity.class, new CellValueProvider<NonDeliverableFXDigitalOptionSecurity>() {
      @Override
      public Object getValue(NonDeliverableFXDigitalOptionSecurity security) {
        return FXAmounts.forOption(security.getPutCurrency(),
                                   security.getCallCurrency(),
                                   security.getPutAmount(),
                                   security.getCallAmount(),
                                   security.isLong(),
                                   currencyPairs
        );
      }
    });
    mapper.mapColumn(RATE, NonDeliverableFXDigitalOptionSecurity.class, new CellValueProvider<NonDeliverableFXDigitalOptionSecurity>() {
      @Override
      public Object getValue(NonDeliverableFXDigitalOptionSecurity security) {
        return CurrencyUtils.getRate(security.getPutCurrency(),
                                     security.getCallCurrency(),
                                     security.getPutAmount(),
                                     security.getCallAmount(),
                                     currencyPairs);
      }
    });

    // ------------------- NonDeliverableFXForward
    mapper.mapColumn(TYPE, NonDeliverableFXForwardSecurity.class, "Non Deliverable FX Forward");
    mapper.mapColumn(MATURITY, NonDeliverableFXForwardSecurity.meta().forwardDate());
    mapper.mapColumn(PRODUCT, NonDeliverableFXForwardSecurity.class, new CellValueProvider<NonDeliverableFXForwardSecurity>() {
      @Override
      public Object getValue(NonDeliverableFXForwardSecurity security) {
        return currencyPairName(security.getPayCurrency(), security.getReceiveCurrency(), currencyPairs);
      }
    });
    mapper.mapColumn(QUANTITY, NonDeliverableFXForwardSecurity.class, new CellValueProvider<NonDeliverableFXForwardSecurity>() {
      @Override
      public Object getValue(NonDeliverableFXForwardSecurity security) {
        return FXAmounts.forForward(security.getPayCurrency(),
                                    security.getReceiveCurrency(),
                                    security.getPayAmount(),
                                    security.getReceiveAmount(),
                                    currencyPairs);
      }
    });
    mapper.mapColumn(RATE, NonDeliverableFXForwardSecurity.class, new CellValueProvider<NonDeliverableFXForwardSecurity>() {
      @Override
      public Object getValue(NonDeliverableFXForwardSecurity security) {
        return CurrencyUtils.getRate(security.getPayCurrency(),
                                     security.getReceiveCurrency(),
                                     security.getPayAmount(),
                                     security.getReceiveAmount(),
                                     currencyPairs);
      }
    });

        // ------------------- NonDeliverableFXOption
    mapper.mapColumn(TYPE, NonDeliverableFXOptionSecurity.class, "Non-deliverable FX Option");
    mapper.mapColumn(MATURITY, NonDeliverableFXOptionSecurity.meta().expiry());
    mapper.mapColumn(QUANTITY, NonDeliverableFXOptionSecurity.class, new CellValueProvider<NonDeliverableFXOptionSecurity>() {
      @Override
      public Object getValue(NonDeliverableFXOptionSecurity security) {
        return FXAmounts.forOption(security.getPutCurrency(),
                                   security.getCallCurrency(),
                                   security.getPutAmount(),
                                   security.getCallAmount(),
                                   security.isLong(),
                                   currencyPairs);
      }
    });
    mapper.mapColumn(PRODUCT, NonDeliverableFXOptionSecurity.class, new CellValueProvider<NonDeliverableFXOptionSecurity>() {
      @Override
      public Object getValue(NonDeliverableFXOptionSecurity security) {
        return currencyPairName(security.getPutCurrency(), security.getCallCurrency(), currencyPairs);
      }
    });
    mapper.mapColumn(RATE, NonDeliverableFXOptionSecurity.class, new CellValueProvider<NonDeliverableFXOptionSecurity>() {
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
    mapper.mapColumn(QUANTITY, FXDigitalOptionSecurity.class, new CellValueProvider<FXDigitalOptionSecurity>() {
      @Override
      public Object getValue(FXDigitalOptionSecurity security) {
        return FXAmounts.forOption(security.getPutCurrency(),
                                   security.getCallCurrency(),
                                   security.getPutAmount(),
                                   security.getCallAmount(),
                                   security.isLong(),
                                   currencyPairs);
      }
    });
    mapper.mapColumn(PRODUCT, FXDigitalOptionSecurity.class, new CellValueProvider<FXDigitalOptionSecurity>() {
      @Override
      public Object getValue(FXDigitalOptionSecurity security) {
        return currencyPairName(security.getPutCurrency(), security.getCallCurrency(), currencyPairs);
      }
    });
    mapper.mapColumn(RATE, FXDigitalOptionSecurity.class, new CellValueProvider<FXDigitalOptionSecurity>() {
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
    mapper.mapColumn(TYPE, ManageableSecurity.class, new CellValueProvider<ManageableSecurity>() {
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
