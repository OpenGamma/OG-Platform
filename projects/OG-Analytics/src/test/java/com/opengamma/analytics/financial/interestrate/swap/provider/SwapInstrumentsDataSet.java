/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.swap.provider;

import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.analytics.financial.instrument.index.GeneratorAttributeIR;
import com.opengamma.analytics.financial.instrument.index.GeneratorLegIbor;
import com.opengamma.analytics.financial.instrument.index.GeneratorLegIborMaster;
import com.opengamma.analytics.financial.instrument.index.GeneratorLegONArithmeticAverage;
import com.opengamma.analytics.financial.instrument.index.GeneratorLegONCompounded;
import com.opengamma.analytics.financial.instrument.index.GeneratorLegOnAaMaster;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIbor;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIborMaster;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedON;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedONMaster;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapSingleCurrency;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexIborMaster;
import com.opengamma.analytics.financial.instrument.swap.SwapDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.interestrate.datasets.StandardDataSetsMulticurveUSD;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.swap.derivative.Swap;
import com.opengamma.financial.convention.StubType;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.timeseries.precise.zdt.ImmutableZonedDateTimeDoubleTimeSeries;
import com.opengamma.timeseries.precise.zdt.ZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;

/**
 * Data set of swaps used in the end-to-end tests.
 */
public class SwapInstrumentsDataSet {

  private SwapInstrumentsDataSet() { /* private constructor */ }

  private static final ZonedDateTime VALUATION_DATE = DateUtils.getUTCDate(2014, 1, 22);
  private static final Calendar NYC = StandardDataSetsMulticurveUSD.calendarArray()[0];
  private static final IndexIborMaster MASTER_IBOR = IndexIborMaster.getInstance();
//  private static final IborIndex USDLIBOR1M = MASTER_IBOR.getIndex("USDLIBOR1M");
  private static final IborIndex USDLIBOR3M = MASTER_IBOR.getIndex("USDLIBOR3M");
//  private static final IborIndex USDLIBOR6M = MASTER_IBOR.getIndex("USDLIBOR6M");
  private static final Currency USD = USDLIBOR3M.getCurrency();
  
  /** Fixing data */
  private static final ZonedDateTimeDoubleTimeSeries TS_USDLIBOR3M =
      ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(
          new ZonedDateTime[] {DateUtils.getUTCDate(2013, 12, 10), DateUtils.getUTCDate(2013, 12, 12) },
          new double[] {0.0024185, 0.0024285});
  private static final ZonedDateTimeDoubleTimeSeries TS_USDLIBOR6M = 
      ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(
          new ZonedDateTime[] {DateUtils.getUTCDate(2013, 12, 10), DateUtils.getUTCDate(2013, 12, 12) }, 
          new double[] {0.0030, 0.0035 });
  private static final ZonedDateTimeDoubleTimeSeries TS_USDON =
      ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(
          new ZonedDateTime[] {
              DateUtils.getUTCDate(2014, 1, 17),
              DateUtils.getUTCDate(2014, 1, 21),
              DateUtils.getUTCDate(2014, 1, 22)
          },
          new double[] {0.0007, 0.0007, 0.0007});
  private static final ZonedDateTimeDoubleTimeSeries[] TS_ARRAY_USDLIBOR3M = 
      new ZonedDateTimeDoubleTimeSeries[] {TS_USDLIBOR3M };
  private static final ZonedDateTimeDoubleTimeSeries[] TS_ARRAY_USDLIBOR3M_USDLIBOR6M = 
      new ZonedDateTimeDoubleTimeSeries[] {TS_USDLIBOR3M, TS_USDLIBOR6M };
  private static final ZonedDateTimeDoubleTimeSeries[] TS_ARRAY_USDON =
      new ZonedDateTimeDoubleTimeSeries[] {TS_USDON };

  /** Standard market conventions */
  private static final GeneratorLegIbor LEG_USDLIBOR3M =  GeneratorLegIborMaster.getInstance().getGenerator("USDLIBOR3M", NYC);
  private static final GeneratorLegIbor LEG_USDLIBOR6M =  GeneratorLegIborMaster.getInstance().getGenerator("USDLIBOR6M", NYC);
  private static final GeneratorLegONArithmeticAverage LEG_USDFEDFUNDAA3M = GeneratorLegOnAaMaster.getInstance().getGenerator("USDFEDFUNDAA3M", NYC);
  private static final GeneratorLegONCompounded LEG_USDFEDFUNDCMP1Y =
      new GeneratorLegONCompounded("LEG", USD, LEG_USDFEDFUNDAA3M.getIndexON(), Period.ofMonths(12), 2, 2, 
          LEG_USDFEDFUNDAA3M.getBusinessDayConvention(), true, StubType.SHORT_START, false, NYC, NYC);
  private static final GeneratorSwapSingleCurrency USDFFAA3MLIBOR3M = new GeneratorSwapSingleCurrency("USDFEDFUNDAA3MLIBOR3M",
      LEG_USDFEDFUNDAA3M, LEG_USDLIBOR3M);
  private static final GeneratorSwapFixedIborMaster GENERATOR_SWAP_FIXED_IBOR_MASTER = GeneratorSwapFixedIborMaster.getInstance();
  private static final GeneratorSwapFixedONMaster GENERATOR_SWAP_FIXED_ONCMP_MASTER = GeneratorSwapFixedONMaster.getInstance();
  private static final GeneratorSwapFixedIbor USD6MLIBOR1M = GENERATOR_SWAP_FIXED_IBOR_MASTER.getGenerator("USD6MLIBOR1M", NYC);
  private static final GeneratorSwapFixedIbor USD6MLIBOR3M = GENERATOR_SWAP_FIXED_IBOR_MASTER.getGenerator("USD6MLIBOR3M", NYC);
  private static final GeneratorSwapFixedON USD1YFEDFUND = GENERATOR_SWAP_FIXED_ONCMP_MASTER.getGenerator("USD1YFEDFUND", NYC);

  /** Instruments descriptions */
  private static final double NOTIONAL = 100000000; //100 m
  
  // Instrument description: Swap Fixed vs ON Cmp (X)
  private static final ZonedDateTime TRADE_DATE_ON = DateUtils.getUTCDate(2014, 2, 3);
  private static final Period TENOR_SWAP_ON = Period.ofMonths(2);
  private static final double FIXED_RATE_ON = 0.00123;
  private static final GeneratorAttributeIR ATTRIBUTE_ON = new GeneratorAttributeIR(TENOR_SWAP_ON);
  private static final SwapDefinition SWAP_FIXED_ON_DEFINITION = 
      USD1YFEDFUND.generateInstrument(TRADE_DATE_ON, FIXED_RATE_ON, NOTIONAL, ATTRIBUTE_ON);
  public static final Swap<? extends Payment, ? extends Payment> SWAP_FIXED_ON =
      SWAP_FIXED_ON_DEFINITION.toDerivative(VALUATION_DATE);


  // Instrument description: Swap Fixed vs ON Cmp Already started (with fixing) (X)
  private static final ZonedDateTime TRADE_DATE_ON_S = DateUtils.getUTCDate(2014, 1, 15);
  private static final Period TENOR_SWAP_ON_S = Period.ofMonths(2);
  private static final double FIXED_RATE_ON_S = 0.00123;
  private static final GeneratorAttributeIR ATTRIBUTE_ON_S = new GeneratorAttributeIR(TENOR_SWAP_ON_S);
  private static final SwapDefinition SWAP_FIXED_ON_DEFINITION_S =
      USD1YFEDFUND.generateInstrument(TRADE_DATE_ON_S, FIXED_RATE_ON_S, NOTIONAL, ATTRIBUTE_ON_S);
  public static final Swap<? extends Payment, ? extends Payment> SWAP_FIXED_ON_S =
      SWAP_FIXED_ON_DEFINITION_S.toDerivative(VALUATION_DATE, TS_ARRAY_USDON);

  // Instrument description: Swap ON AA + Spread vs Libor 3M (X)
  private static final ZonedDateTime TRADE_DATE_FF = DateUtils.getUTCDate(2014, 9, 10);
  private static final Period TENOR_SWAP_FF = Period.ofYears(6);
  private static final double SPREAD_FF = 0.0025;
  private static final GeneratorAttributeIR ATTRIBUTE_FF = new GeneratorAttributeIR(TENOR_SWAP_FF);
  private static final SwapDefinition SWAP_FF_3M_0_DEFINITION = 
      USDFFAA3MLIBOR3M.generateInstrument(TRADE_DATE_FF, 0.0, NOTIONAL, ATTRIBUTE_FF);
  public static final Swap<? extends Payment, ? extends Payment> SWAP_FF_3M_0 = SWAP_FF_3M_0_DEFINITION.toDerivative(VALUATION_DATE);
  private static final SwapDefinition SWAP_FF_3M_DEFINITION = 
      USDFFAA3MLIBOR3M.generateInstrument(TRADE_DATE_FF, SPREAD_FF, NOTIONAL, ATTRIBUTE_FF);
  public static final Swap<? extends Payment, ? extends Payment> SWAP_FF_3M = SWAP_FF_3M_DEFINITION.toDerivative(VALUATION_DATE);

  // Instrument description: Swap Fixed vs Libor3M (X)
  private static final ZonedDateTime TRADE_DATE_3M = DateUtils.getUTCDate(2014, 9, 10);
  private static final Period TENOR_SWAP_3M = Period.ofYears(7);
  private static final double FIXED_RATE_3M = 0.0150;
  private static final GeneratorAttributeIR ATTRIBUTE_3M = new GeneratorAttributeIR(TENOR_SWAP_3M);
  private static final SwapDefinition SWAP_FIXED_3M_DEFINITION = 
      USD6MLIBOR3M.generateInstrument(TRADE_DATE_3M, FIXED_RATE_3M, NOTIONAL, ATTRIBUTE_3M);
  public static final Swap<? extends Payment, ? extends Payment> SWAP_FIXED_3M =
      SWAP_FIXED_3M_DEFINITION.toDerivative(VALUATION_DATE);

  // Instrument description: Swap Fixed vs Libor1M
  private static final ZonedDateTime TRADE_DATE_1M = DateUtils.getUTCDate(2014, 9, 10);
  private static final Period TENOR_SWAP_1M = Period.ofYears(2);
  private static final double FIXED_RATE_1M = 0.0125;
  private static final GeneratorAttributeIR ATTRIBUTE_1M = new GeneratorAttributeIR(TENOR_SWAP_1M);
  private static final SwapDefinition SWAP_FIXED_1M_DEFINITION = 
      USD6MLIBOR1M.generateInstrument(TRADE_DATE_1M, FIXED_RATE_1M, NOTIONAL, ATTRIBUTE_1M);
  public static final Swap<? extends Payment, ? extends Payment> SWAP_FIXED_1M = SWAP_FIXED_1M_DEFINITION.toDerivative(VALUATION_DATE);

  // Instrument description: Swap Fixed vs Libor3M Already started (with fixing) (X)
  private static final ZonedDateTime TRADE_DATE_3M_S = DateUtils.getUTCDate(2013, 9, 10);
  private static final Period TENOR_SWAP_3M_S = Period.ofYears(7);
  private static final double FIXED_RATE_3M_S = 0.0150;
  private static final GeneratorAttributeIR ATTRIBUTE_3M_S = new GeneratorAttributeIR(TENOR_SWAP_3M_S);
  private static final SwapFixedIborDefinition SWAP_FIXED_3M_S_DEFINITION = 
      USD6MLIBOR3M.generateInstrument(TRADE_DATE_3M_S, FIXED_RATE_3M_S, NOTIONAL, ATTRIBUTE_3M_S);
  public static final Swap<? extends Payment, ? extends Payment> SWAP_FIXED_3M_S = 
      SWAP_FIXED_3M_S_DEFINITION.toDerivative(VALUATION_DATE, TS_ARRAY_USDLIBOR3M);

  // Instrument description: Swap Libor3M+S vs Libor6M (X)
  private static final ZonedDateTime TRADE_DATE_3M_6M_BS = DateUtils.getUTCDate(2014, 8, 27);
  private static final Period TENOR_SWAP_3M_6M_BS = Period.ofYears(10);
  private static final double SPREAD_3M_6M_BS = 0.0010;
  private static final GeneratorAttributeIR ATTRIBUTE_3M_6M_BS = new GeneratorAttributeIR(TENOR_SWAP_3M_6M_BS);
  private static final AnnuityDefinition<?> LEG_3M_S = 
      LEG_USDLIBOR3M.generateInstrument(TRADE_DATE_3M_6M_BS, SPREAD_3M_6M_BS, NOTIONAL, ATTRIBUTE_3M_6M_BS); // Receiver
  private static final AnnuityDefinition<?> LEG_6M = 
      LEG_USDLIBOR6M.generateInstrument(TRADE_DATE_3M_6M_BS, 0.0, -NOTIONAL, ATTRIBUTE_3M_6M_BS); // Payer
  private static final SwapDefinition BS_3M_S_6M_DEFINITION = new SwapDefinition(LEG_3M_S, LEG_6M);
  public static final Swap<? extends Payment, ? extends Payment> BS_3M_S_6M = 
      BS_3M_S_6M_DEFINITION.toDerivative(VALUATION_DATE, TS_ARRAY_USDLIBOR3M_USDLIBOR6M);

  // Instrument description: Swap ON Comp+spread v ON AA - Such swap would not be traded, used only for testing
  private static final ZonedDateTime TRADE_DATE_ONCMP_AA = DateUtils.getUTCDate(2014, 8, 27);
  private static final Period TENOR_SWAP_ONCMP_AA = Period.ofYears(5);
  private static final double SPREAD_ONCMP_AA = 0.0010;
  private static final GeneratorAttributeIR ATTRIBUTE_ONCMP_AA = new GeneratorAttributeIR(TENOR_SWAP_ONCMP_AA);
  private static final AnnuityDefinition<?> LEG_ONCMP_S = 
      LEG_USDFEDFUNDCMP1Y.generateInstrument(TRADE_DATE_ONCMP_AA, SPREAD_ONCMP_AA, -NOTIONAL, ATTRIBUTE_ONCMP_AA); // Payer
  private static final AnnuityDefinition<?> LEG_ONAA = 
      LEG_USDFEDFUNDAA3M.generateInstrument(TRADE_DATE_ONCMP_AA, 0.0, NOTIONAL, ATTRIBUTE_ONCMP_AA); // Receiver
  private static final SwapDefinition BS_ONCMP_S_ONAA_DEFINITION = new SwapDefinition(LEG_ONCMP_S, LEG_ONAA);
  public static final Swap<? extends Payment, ? extends Payment> BS_ONCMP_S_ONAA = 
      BS_ONCMP_S_ONAA_DEFINITION.toDerivative(VALUATION_DATE);
  
  // Instrument description: Swap LIBOR3M Compounding 6M v LIBOR6M
  private static final ZonedDateTime TRADE_DATE_3MCMP_6M_BS = DateUtils.getUTCDate(2014, 8, 27);
  private static final Period TENOR_SWAP_3MCMP_6M_BS = Period.ofYears(10);
  private static final double SPREAD_3MCMP_6M_BS = 0.0010;
  private static final GeneratorAttributeIR ATTRIBUTE_3MCMP_6M_BS = new GeneratorAttributeIR(TENOR_SWAP_3MCMP_6M_BS);
  private static final AnnuityDefinition<?> LEG_3MCMP_S = 
      LEG_USDLIBOR3M.generateInstrument(TRADE_DATE_3MCMP_6M_BS, SPREAD_3MCMP_6M_BS, NOTIONAL, ATTRIBUTE_3MCMP_6M_BS); // Receiver
  private static final AnnuityDefinition<?> LEG_6MCMP = 
      LEG_USDLIBOR6M.generateInstrument(TRADE_DATE_3MCMP_6M_BS, 0.0, -NOTIONAL, ATTRIBUTE_3M_6M_BS); // Payer
  private static final SwapDefinition BS_3MCMP_S_6M_DEFINITION = new SwapDefinition(LEG_3MCMP_S, LEG_6MCMP);
  public static final Swap<? extends Payment, ? extends Payment> BS_3MCMP_S_6M = 
      BS_3MCMP_S_6M_DEFINITION.toDerivative(VALUATION_DATE, TS_ARRAY_USDLIBOR3M_USDLIBOR6M);
  

}
