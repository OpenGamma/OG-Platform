/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.lookup;

import static com.opengamma.financial.security.lookup.SecurityAttribute.DIRECTION;
import static com.opengamma.financial.security.lookup.SecurityAttribute.FLOAT_FREQUENCY;
import static com.opengamma.financial.security.lookup.SecurityAttribute.FREQUENCY;
import static com.opengamma.financial.security.lookup.SecurityAttribute.INDEX;
import static com.opengamma.financial.security.lookup.SecurityAttribute.MATURITY;
import static com.opengamma.financial.security.lookup.SecurityAttribute.PRODUCT;
import static com.opengamma.financial.security.lookup.SecurityAttribute.QUANTITY;
import static com.opengamma.financial.security.lookup.SecurityAttribute.RATE;
import static com.opengamma.financial.security.lookup.SecurityAttribute.START;
import static com.opengamma.financial.security.lookup.SecurityAttribute.TYPE;

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
import com.opengamma.financial.security.future.FederalFundsFutureSecurity;
import com.opengamma.financial.security.future.FutureSecurity;
import com.opengamma.financial.security.future.IndexFutureSecurity;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
import com.opengamma.financial.security.future.MetalFutureSecurity;
import com.opengamma.financial.security.future.StockFutureSecurity;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.financial.security.fx.NonDeliverableFXForwardSecurity;
import com.opengamma.financial.security.lookup.swap.SwapFloatFrequencyProvider;
import com.opengamma.financial.security.lookup.swap.SwapFrequencyProvider;
import com.opengamma.financial.security.lookup.swap.SwapIndexProvider;
import com.opengamma.financial.security.lookup.swap.SwapPayReceiveProvider;
import com.opengamma.financial.security.lookup.swap.SwapProductProvider;
import com.opengamma.financial.security.lookup.swap.SwapQuantityProvider;
import com.opengamma.financial.security.lookup.swap.SwapRateProvider;
import com.opengamma.financial.security.lookup.swap.SwapTypeProvider;
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

/**
 * Configures a {@link SecurityAttributeMapper} with default mappings to
 * fields on different security types.
 */
public final class DefaultSecurityAttributeMappings {

  private DefaultSecurityAttributeMappings() {
  }

  /**
   * Creates the default {@link SecurityAttributeMapper} instance using
   * a standard set of security mappings.
   * @param currencyPairs the {@link CurrencyPairs} config to use. Used for correct display of FX-type instrument fields.
   * @return the default {@link SecurityAttributeMapper}
   */
  public static SecurityAttributeMapper create(final CurrencyPairs currencyPairs) {
    SecurityAttributeMapper mapper = new SecurityAttributeMapper();

    mapBond(mapper);
    mapBondFutureOption(mapper);
    mapCommodityForward(mapper);
    mapCommodityFutureOption(mapper);
    mapCapFloor(mapper);
    mapCapFloorCMSSpread(mapper);
    mapFuture(mapper);
    mapIndexFuture(mapper);
    mapInterestRateFuture(mapper);
    mapIRFutureOption(mapper);
    mapFRA(mapper);
    mapSwap(mapper);
    mapSwaption(mapper);
    mapEquity(mapper);
    mapEquityOption(mapper);
    mapEquityIndexOption(mapper);
    mapEquityBarrierOption(mapper);
    mapEquityVarianceSwap(mapper);
    mapFXForward(mapper, currencyPairs);
    mapFXOption(mapper, currencyPairs);
    mapFXBarrierOption(mapper, currencyPairs);
    mapNonDeliverableFXDigitalOption(mapper, currencyPairs);
    mapNonDeliverableFXForward(mapper, currencyPairs);
    mapNonDeliverableFXOption(mapper, currencyPairs);
    mapFXDigitalOption(mapper, currencyPairs);

    // -------------------
    // fallback type name for securities that haven't been explicitly mapped
    mapper.mapColumn(TYPE, ManageableSecurity.class, new SecurityValueProvider<ManageableSecurity>() {
      @Override
      public Object getValue(ManageableSecurity security) {
        return security.getClass().getSimpleName();
      }
    });

    return mapper;
  }

  /**
   * Creates mappings for {@link FXDigitalOptionSecurity}
   * @param mapper the mapper instance to use
   * @param currencyPairs the {@link CurrencyPairs} config to use
   */
  private static void mapFXDigitalOption(SecurityAttributeMapper mapper, final CurrencyPairs currencyPairs) {
    // ------------------- FXDigitalOption
    mapper.mapColumn(TYPE, FXDigitalOptionSecurity.class, "FX Digital Option");
    mapper.mapColumn(MATURITY, FXDigitalOptionSecurity.meta().expiry());
    mapper.mapColumn(QUANTITY, FXDigitalOptionSecurity.class, new SecurityValueProvider<FXDigitalOptionSecurity>() {
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
    mapper.mapColumn(PRODUCT, FXDigitalOptionSecurity.class, new SecurityValueProvider<FXDigitalOptionSecurity>() {
      @Override
      public Object getValue(FXDigitalOptionSecurity security) {
        return currencyPairName(security.getPutCurrency(), security.getCallCurrency(), currencyPairs);
      }
    });
    mapper.mapColumn(RATE, FXDigitalOptionSecurity.class, new SecurityValueProvider<FXDigitalOptionSecurity>() {
      @Override
      public Object getValue(FXDigitalOptionSecurity security) {
        return CurrencyUtils.getRate(security.getPutCurrency(),
            security.getCallCurrency(),
            security.getPutAmount(),
            security.getCallAmount(),
            currencyPairs);
      }
    });
  }

  /**
   * Creates mappings for {@link NonDeliverableFXOptionSecurity}
   * @param mapper the mapper instance to use
   * @param currencyPairs the {@link CurrencyPairs} config to use
   */
  private static void mapNonDeliverableFXOption(SecurityAttributeMapper mapper, final CurrencyPairs currencyPairs) {
    // ------------------- NonDeliverableFXOption
    mapper.mapColumn(TYPE, NonDeliverableFXOptionSecurity.class, "Non-deliverable FX Option");
    mapper.mapColumn(MATURITY, NonDeliverableFXOptionSecurity.meta().expiry());
    mapper.mapColumn(QUANTITY, NonDeliverableFXOptionSecurity.class, new SecurityValueProvider<NonDeliverableFXOptionSecurity>() {
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
    mapper.mapColumn(PRODUCT, NonDeliverableFXOptionSecurity.class, new SecurityValueProvider<NonDeliverableFXOptionSecurity>() {
      @Override
      public Object getValue(NonDeliverableFXOptionSecurity security) {
        return currencyPairName(security.getPutCurrency(), security.getCallCurrency(), currencyPairs);
      }
    });
    mapper.mapColumn(RATE, NonDeliverableFXOptionSecurity.class, new SecurityValueProvider<NonDeliverableFXOptionSecurity>() {
      @Override
      public Object getValue(NonDeliverableFXOptionSecurity security) {
        return CurrencyUtils.getRate(security.getPutCurrency(),
            security.getCallCurrency(),
            security.getPutAmount(),
            security.getCallAmount(),
            currencyPairs);
      }
    });
  }

  /**
   * Creates mappings for {@link NonDeliverableFXForwardSecurity}
   * @param mapper the mapper instance to use
   * @param currencyPairs the {@link CurrencyPairs} config to use
   */
  private static void mapNonDeliverableFXForward(SecurityAttributeMapper mapper, final CurrencyPairs currencyPairs) {
    // ------------------- NonDeliverableFXForward
    mapper.mapColumn(TYPE, NonDeliverableFXForwardSecurity.class, "Non Deliverable FX Forward");
    mapper.mapColumn(MATURITY, NonDeliverableFXForwardSecurity.meta().forwardDate());
    mapper.mapColumn(PRODUCT, NonDeliverableFXForwardSecurity.class, new SecurityValueProvider<NonDeliverableFXForwardSecurity>() {
      @Override
      public Object getValue(NonDeliverableFXForwardSecurity security) {
        return currencyPairName(security.getPayCurrency(), security.getReceiveCurrency(), currencyPairs);
      }
    });
    mapper.mapColumn(QUANTITY, NonDeliverableFXForwardSecurity.class, new SecurityValueProvider<NonDeliverableFXForwardSecurity>() {
      @Override
      public Object getValue(NonDeliverableFXForwardSecurity security) {
        return FXAmounts.forForward(security.getPayCurrency(),
            security.getReceiveCurrency(),
            security.getPayAmount(),
            security.getReceiveAmount(),
            currencyPairs);
      }
    });
    mapper.mapColumn(RATE, NonDeliverableFXForwardSecurity.class, new SecurityValueProvider<NonDeliverableFXForwardSecurity>() {
      @Override
      public Object getValue(NonDeliverableFXForwardSecurity security) {
        return CurrencyUtils.getRate(security.getPayCurrency(),
            security.getReceiveCurrency(),
            security.getPayAmount(),
            security.getReceiveAmount(),
            currencyPairs);
      }
    });
  }

  /**
   * Creates mappings for {@link NonDeliverableFXDigitalOptionSecurity}
   * @param mapper the mapper instance to use
   * @param currencyPairs the {@link CurrencyPairs} config to use
   */
  private static void mapNonDeliverableFXDigitalOption(SecurityAttributeMapper mapper, final CurrencyPairs currencyPairs) {
    // ------------------- NonDeliverableFXDigitalOption
    mapper.mapColumn(TYPE, NonDeliverableFXDigitalOptionSecurity.class, "Non Deliverable FX Digital Option");
    mapper.mapColumn(MATURITY, NonDeliverableFXDigitalOptionSecurity.meta().expiry());
    mapper.mapColumn(PRODUCT, NonDeliverableFXDigitalOptionSecurity.class, new SecurityValueProvider<NonDeliverableFXDigitalOptionSecurity>() {
      @Override
      public Object getValue(NonDeliverableFXDigitalOptionSecurity security) {
        return currencyPairName(security.getPutCurrency(), security.getCallCurrency(), currencyPairs);
      }
    });
    mapper.mapColumn(QUANTITY, NonDeliverableFXDigitalOptionSecurity.class, new SecurityValueProvider<NonDeliverableFXDigitalOptionSecurity>() {
      @Override
      public Object getValue(NonDeliverableFXDigitalOptionSecurity security) {
        return FXAmounts.forOption(security.getPutCurrency(),
            security.getCallCurrency(),
            security.getPutAmount(),
            security.getCallAmount(),
            security.isLong(),
            currencyPairs);
      }
    });
    mapper.mapColumn(RATE, NonDeliverableFXDigitalOptionSecurity.class, new SecurityValueProvider<NonDeliverableFXDigitalOptionSecurity>() {
      @Override
      public Object getValue(NonDeliverableFXDigitalOptionSecurity security) {
        return CurrencyUtils.getRate(security.getPutCurrency(),
            security.getCallCurrency(),
            security.getPutAmount(),
            security.getCallAmount(),
            currencyPairs);
      }
    });
  }

  /**
   * Creates mappings for {@link FXBarrierOptionSecurity}
   * @param mapper the mapper instance to use
   * @param currencyPairs the {@link CurrencyPairs} config to use
   */
  private static void mapFXBarrierOption(SecurityAttributeMapper mapper, final CurrencyPairs currencyPairs) {
    // ------------------- FXBarrierOption
    mapper.mapColumn(TYPE, FXBarrierOptionSecurity.class, "FX Barrier Option");
    mapper.mapColumn(MATURITY, FXBarrierOptionSecurity.meta().expiry());
    mapper.mapColumn(QUANTITY, FXBarrierOptionSecurity.class, new SecurityValueProvider<FXBarrierOptionSecurity>() {
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
    mapper.mapColumn(PRODUCT, FXBarrierOptionSecurity.class, new SecurityValueProvider<FXBarrierOptionSecurity>() {
      @Override
      public Object getValue(FXBarrierOptionSecurity security) {
        return currencyPairName(security.getPutCurrency(), security.getCallCurrency(), currencyPairs);
      }
    });
    mapper.mapColumn(RATE, FXBarrierOptionSecurity.class, new SecurityValueProvider<FXBarrierOptionSecurity>() {
      @Override
      public Object getValue(FXBarrierOptionSecurity security) {
        return CurrencyUtils.getRate(security.getPutCurrency(),
            security.getCallCurrency(),
            security.getPutAmount(),
            security.getCallAmount(),
            currencyPairs);
      }
    });
  }

  /**
   * Creates mappings for {@link FXOptionSecurity}
   * @param mapper the mapper instance to use
   * @param currencyPairs the {@link CurrencyPairs} config to use
   */
  private static void mapFXOption(SecurityAttributeMapper mapper, final CurrencyPairs currencyPairs) {
    // ------------------- FXOption
    mapper.mapColumn(TYPE, FXOptionSecurity.class, "FX Option");
    mapper.mapColumn(MATURITY, FXOptionSecurity.meta().expiry());
    mapper.mapColumn(PRODUCT, FXOptionSecurity.class, new SecurityValueProvider<FXOptionSecurity>() {
      @Override
      public Object getValue(FXOptionSecurity security) {
        CurrencyPair pair = currencyPairs.getCurrencyPair(security.getPutCurrency(), security.getCallCurrency());
        return pair.getBase() + "/" + pair.getCounter();
      }
    });
    mapper.mapColumn(QUANTITY, FXOptionSecurity.class, new SecurityValueProvider<FXOptionSecurity>() {
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
    mapper.mapColumn(RATE, FXOptionSecurity.class, new SecurityValueProvider<FXOptionSecurity>() {
      @Override
      public Object getValue(FXOptionSecurity security) {
        return CurrencyUtils.getRate(security.getPutCurrency(),
            security.getCallCurrency(),
            security.getPutAmount(),
            security.getCallAmount(),
            currencyPairs);
      }
    });
  }

  /**
   * Creates mappings for {@link FXForwardSecurity}
   * @param mapper the mapper instance to use
   * @param currencyPairs the {@link CurrencyPairs} config to use
   */
  private static void mapFXForward(SecurityAttributeMapper mapper, final CurrencyPairs currencyPairs) {
    // ------------------- FXForward
    mapper.mapColumn(TYPE, FXForwardSecurity.class, "FX Forward");
    mapper.mapColumn(MATURITY, FXForwardSecurity.meta().forwardDate());
    mapper.mapColumn(PRODUCT, FXForwardSecurity.class, new SecurityValueProvider<FXForwardSecurity>() {
      @Override
      public String getValue(FXForwardSecurity security) {
        return currencyPairName(security.getPayCurrency(), security.getReceiveCurrency(), currencyPairs);
      }
    });
    mapper.mapColumn(QUANTITY, FXForwardSecurity.class, new SecurityValueProvider<FXForwardSecurity>() {
      @Override
      public FXAmounts getValue(FXForwardSecurity security) {
        return FXAmounts.forForward(security.getPayCurrency(),
            security.getReceiveCurrency(),
            security.getPayAmount(),
            security.getReceiveAmount(),
            currencyPairs);
      }
    });
    mapper.mapColumn(RATE, FXForwardSecurity.class, new SecurityValueProvider<FXForwardSecurity>() {
      @Override
      public Double getValue(FXForwardSecurity security) {
        return CurrencyUtils.getRate(security.getPayCurrency(),
            security.getReceiveCurrency(),
            security.getPayAmount(),
            security.getReceiveAmount(),
            currencyPairs);
      }
    });
  }

  /**
   * Creates mappings for {@link EquityVarianceSwapSecurity}
   * @param mapper the mapper instance to use
   */
  private static void mapEquityVarianceSwap(SecurityAttributeMapper mapper) {
    // ------------------- EquityVarianceSwap
    mapper.mapColumn(TYPE, EquityVarianceSwapSecurity.class, "Equity Variance Swap");
    mapper.mapColumn(QUANTITY, EquityVarianceSwapSecurity.meta().notional());
  }

  /**
   * Creates mappings for {@link EquityBarrierOptionSecurity}
   * @param mapper the mapper instance to use
   */
  private static void mapEquityBarrierOption(SecurityAttributeMapper mapper) {
    // ------------------- EquityBarrierOption
    mapper.mapColumn(TYPE, EquityBarrierOptionSecurity.class, "Equity Barrier Option");
    mapper.mapColumn(RATE, EquityBarrierOptionSecurity.meta().strike());
    mapper.mapColumn(MATURITY, EquityBarrierOptionSecurity.meta().expiry());
    mapper.mapColumn(DIRECTION, EquityBarrierOptionSecurity.meta().optionType());
    mapper.mapColumn(PRODUCT, EquityBarrierOptionSecurity.class, new SecurityValueProvider<EquityBarrierOptionSecurity>() {
      @Override
      public Object getValue(EquityBarrierOptionSecurity security) {
        return security.getUnderlyingId().getValue();
      }
    });
  }

  /**
   * Creates mappings for {@link EquityIndexOptionSecurity}
   * @param mapper the mapper instance to use
   */
  private static void mapEquityIndexOption(SecurityAttributeMapper mapper) {
    // ------------------- EquityIndexOption
    mapper.mapColumn(TYPE, EquityIndexOptionSecurity.class, "Equity Index Option");
    mapper.mapColumn(RATE, EquityIndexOptionSecurity.meta().strike());
    mapper.mapColumn(MATURITY, EquityIndexOptionSecurity.meta().expiry());
    mapper.mapColumn(DIRECTION, EquityIndexOptionSecurity.meta().optionType());
    mapper.mapColumn(PRODUCT, EquityIndexOptionSecurity.class, new SecurityValueProvider<EquityIndexOptionSecurity>() {
      @Override
      public Object getValue(EquityIndexOptionSecurity security) {
        return security.getUnderlyingId().getValue();
      }
    });
  }

  /**
   * Creates mappings for {@link EquityOptionSecurity}
   * @param mapper the mapper instance to use
   */
  private static void mapEquityOption(SecurityAttributeMapper mapper) {
    // ------------------- EquityOption
    mapper.mapColumn(TYPE, EquityOptionSecurity.class, "Equity Option");
    mapper.mapColumn(RATE, EquityOptionSecurity.meta().strike());
    mapper.mapColumn(MATURITY, EquityOptionSecurity.meta().expiry());
    mapper.mapColumn(DIRECTION, EquityOptionSecurity.meta().optionType());
  }

  /**
   * Creates mappings for {@link EquitySecurity}
   * @param mapper the mapper instance to use
   */
  private static void mapEquity(SecurityAttributeMapper mapper) {
    // ------------------- Equity
    mapper.mapColumn(TYPE, EquitySecurity.class, "Equity");
    mapper.mapColumn(PRODUCT, EquitySecurity.meta().companyName());
  }

  /**
   * Creates mappings for {@link SwaptionSecurity}
   * @param mapper the mapper instance to use
   */
  private static void mapSwaption(SecurityAttributeMapper mapper) {
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
  }

  /**
   * Creates mappings for {@link SwapSecurity}
   * @param mapper the mapper instance to use
   */
  private static void mapSwap(SecurityAttributeMapper mapper) {
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
  }

  /**
   * Creates mappings for {@link FRASecurity}
   * @param mapper the mapper instance to use
   */
  private static void mapFRA(SecurityAttributeMapper mapper) {
    // ------------------- FRA
    mapper.mapColumn(TYPE, FRASecurity.class, "FRA");
    mapper.mapColumn(PRODUCT, FRASecurity.meta().currency());
    mapper.mapColumn(QUANTITY, FRASecurity.meta().amount());
    mapper.mapColumn(START, FRASecurity.meta().startDate());
    mapper.mapColumn(MATURITY, FRASecurity.meta().endDate());
    mapper.mapColumn(RATE, FRASecurity.meta().rate());
  }

  /**
   * Creates mappings for {@link IRFutureOptionSecurity}
   * @param mapper the mapper instance to use
   */
  private static void mapIRFutureOption(SecurityAttributeMapper mapper) {
    // ------------------- IRFutureOption
    mapper.mapColumn(TYPE, IRFutureOptionSecurity.class, "Interest Rate Future Option");
    mapper.mapColumn(MATURITY, IRFutureOptionSecurity.meta().expiry());
    mapper.mapColumn(RATE, IRFutureOptionSecurity.meta().strike());
    mapper.mapColumn(DIRECTION, IRFutureOptionSecurity.meta().optionType());
  }

  /**
   * Creates mappings for {@link InterestRateFutureSecurity}
   * @param mapper the mapper instance to use
   */
  private static void mapInterestRateFuture(SecurityAttributeMapper mapper) {
    // ------------------- InterestRateFuture
    mapper.mapColumn(TYPE, InterestRateFutureSecurity.class, "Interest Rate Future");
    mapper.mapColumn(PRODUCT, InterestRateFutureSecurity.meta().currency());
    mapper.mapColumn(INDEX, InterestRateFutureSecurity.class, new SecurityValueProvider<InterestRateFutureSecurity>() {
      @Override
      public Object getValue(InterestRateFutureSecurity security) {
        return security.getUnderlyingId().getValue();
      }
    });
  }

  /**
   * Creates mappings for {@link IndexFutureSecurity}
   * @param mapper the mapper instance to use
   */
  private static void mapIndexFuture(SecurityAttributeMapper mapper) {
    // ------------------- IndexFuture
    mapper.mapColumn(INDEX, IndexFutureSecurity.class, new SecurityValueProvider<IndexFutureSecurity>() {
      @Override
      public Object getValue(IndexFutureSecurity security) {
        return security.getUnderlyingId().getValue();
      }
    });
  }

  /**
   * Creates mappings for {@link FutureSecurity}
   * @param mapper the mapper instance to use
   */
  private static void mapFuture(SecurityAttributeMapper mapper) {
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
    mapper.mapColumn(TYPE, FederalFundsFutureSecurity.class, "Federal Funds Future");
  }

  /**
   * Creates mappings for {@link CapFloorCMSSpreadSecurity}
   * @param mapper the mapper instance to use
   */
  private static void mapCapFloorCMSSpread(SecurityAttributeMapper mapper) {
    // ------------------- CapFloorCMSSpread
    mapper.mapColumn(TYPE, CapFloorCMSSpreadSecurity.class, "Cap/Floor CMS Spread");
    mapper.mapColumn(START, CapFloorCMSSpreadSecurity.meta().startDate());
    mapper.mapColumn(MATURITY, CapFloorCMSSpreadSecurity.meta().maturityDate());
    mapper.mapColumn(QUANTITY, CapFloorCMSSpreadSecurity.meta().notional());
    mapper.mapColumn(RATE, CapFloorCMSSpreadSecurity.meta().strike());
    mapper.mapColumn(FREQUENCY, CapFloorCMSSpreadSecurity.meta().frequency());
    mapper.mapColumn(PRODUCT, CapFloorCMSSpreadSecurity.class, new SecurityValueProvider<CapFloorCMSSpreadSecurity>() {
      @Override
      public Object getValue(CapFloorCMSSpreadSecurity security) {
        return security.getLongId().getValue() + "/" + security.getShortId().getValue();
      }
    });
  }

  /**
   * Creates mappings for {@link CapFloorSecurity}
   * @param mapper the mapper instance to use
   */
  private static void mapCapFloor(SecurityAttributeMapper mapper) {
    // ------------------- CapFloor
    mapper.mapColumn(TYPE, CapFloorSecurity.class, "Cap/Floor");
    mapper.mapColumn(QUANTITY, CapFloorSecurity.meta().notional());
    mapper.mapColumn(START, CapFloorSecurity.meta().startDate());
    mapper.mapColumn(MATURITY, CapFloorSecurity.meta().maturityDate());
    mapper.mapColumn(RATE, CapFloorSecurity.meta().strike());
    mapper.mapColumn(FREQUENCY, CapFloorSecurity.meta().frequency());
  }

  /**
   * Creates mappings for {@link CommodityFutureOptionSecurity}
   * @param mapper the mapper instance to use
   */
  private static void mapCommodityFutureOption(SecurityAttributeMapper mapper) {
    // ------------------- CommodityFutureOption
    mapper.mapColumn(TYPE, CommodityFutureOptionSecurity.class, "Commodity Future Option");
    mapper.mapColumn(MATURITY, CommodityFutureOptionSecurity.meta().expiry());
    mapper.mapColumn(RATE, CommodityFutureOptionSecurity.meta().strike());
    mapper.mapColumn(DIRECTION, CommodityFutureOptionSecurity.meta().optionType());
  }

  /**
   * Creates mappings for {@link CommodityForwardSecurity} type securities
   * @param mapper the mapper instance to use
   */
  private static void mapCommodityForward(SecurityAttributeMapper mapper) {
    // ------------------- CommodityForward
    mapper.mapColumn(TYPE, MetalForwardSecurity.class, "Metal Forward");
    mapper.mapColumn(TYPE, EnergyForwardSecurity.class, "Energy Forward");
    mapper.mapColumn(TYPE, AgricultureForwardSecurity.class, "Agriculture Forward");
    mapper.mapColumn(MATURITY, CommodityForwardSecurity.meta().expiry());
    mapper.mapColumn(QUANTITY, CommodityForwardSecurity.meta().unitAmount());
    mapper.mapColumn(PRODUCT, CommodityForwardSecurity.meta().contractCategory());
  }

  /**
   * Creates mappings for {@link BondFutureOptionSecurity}
   * @param mapper the mapper instance to use
   */
  private static void mapBondFutureOption(SecurityAttributeMapper mapper) {
    // ------------------- BondFutureOption
    mapper.mapColumn(TYPE, BondFutureOptionSecurity.class, "Bond Future Option");
    mapper.mapColumn(MATURITY, BondFutureOptionSecurity.meta().expiry());
    mapper.mapColumn(RATE, BondFutureOptionSecurity.meta().strike());
    mapper.mapColumn(DIRECTION, BondFutureOptionSecurity.meta().optionType());
  }

  /**
   * Creates mappings for {@link BondSecurity} type securities
   * @param mapper the mapper instance to use
   */
  private static void mapBond(SecurityAttributeMapper mapper) {
    // ------------------- Bond
    mapper.mapColumn(TYPE, GovernmentBondSecurity.class, "Government Bond");
    mapper.mapColumn(TYPE, CorporateBondSecurity.class, "Corporate Bond");
    mapper.mapColumn(TYPE, MunicipalBondSecurity.class, "Municipal Bond");
    mapper.mapColumn(PRODUCT, BondSecurity.meta().issuerName());
    mapper.mapColumn(RATE, BondSecurity.meta().couponRate());
    mapper.mapColumn(FREQUENCY, BondSecurity.meta().couponFrequency());
    mapper.mapColumn(START, BondSecurity.meta().firstCouponDate());
    mapper.mapColumn(MATURITY, BondSecurity.meta().settlementDate());
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
