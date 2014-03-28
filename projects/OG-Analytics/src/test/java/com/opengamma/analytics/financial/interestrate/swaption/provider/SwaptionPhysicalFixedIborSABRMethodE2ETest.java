/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.swaption.provider;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.index.GeneratorAttributeIR;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIbor;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIborMaster;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.instrument.swaption.SwaptionPhysicalFixedIborDefinition;
import com.opengamma.analytics.financial.interestrate.datasets.StandardDataSetsMulticurveUSD;
import com.opengamma.analytics.financial.interestrate.datasets.StandardDataSetsSABRSwaptionUSD;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateParameters;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.SABRSwaptionProviderDiscount;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.Pair;

/**
 * Test related to swaption end-to-end using standardized market data.
 */
public class SwaptionPhysicalFixedIborSABRMethodE2ETest {

  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2014, 1, 22);
  private static final IborIndex[] INDEX_IBOR_LIST = StandardDataSetsMulticurveUSD.indexIborArrayUSDOisL1L3L6();
  private static final IborIndex USDLIBOR3M = INDEX_IBOR_LIST[1];
  private static final Calendar NYC = StandardDataSetsMulticurveUSD.calendarArray()[0];
  private static final Currency USD = USDLIBOR3M.getCurrency();
  // Standard conventions
  private static final GeneratorSwapFixedIborMaster GENERATOR_SWAP_FIXED_IBOR_MASTER = GeneratorSwapFixedIborMaster.getInstance();
  private static final GeneratorSwapFixedIbor USD6MLIBOR3M = GENERATOR_SWAP_FIXED_IBOR_MASTER.getGenerator("USD6MLIBOR3M", NYC);
  // Curve Data
  private static final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> MULTICURVE_PAIR = StandardDataSetsMulticurveUSD.getCurvesUSDOisL1L3L6();
  private static final MulticurveProviderDiscount MULTICURVE = MULTICURVE_PAIR.getFirst();
  private static final CurveBuildingBlockBundle BLOCK = MULTICURVE_PAIR.getSecond();
  // SABR data
  private static final SABRInterestRateParameters SABR_PARAMETER = StandardDataSetsSABRSwaptionUSD.createSABR1();
  private static final SABRSwaptionProviderDiscount SABR_MULTICURVES = new SABRSwaptionProviderDiscount(MULTICURVE, SABR_PARAMETER, USD6MLIBOR3M);

  // Standard swaption
  private static final double NOTIONAL = 100000000; //100m
  private static final ZonedDateTime EXPIRY_DATE = DateUtils.getUTCDate(2016, 1, 22); // 2Y
  private static final Period TENOR_SWAP_3M = Period.ofYears(7);
  private static final double FIXED_RATE_3M = 0.0150;
  private static final GeneratorAttributeIR ATTRIBUTE_3M = new GeneratorAttributeIR(TENOR_SWAP_3M);
  private static final SwapFixedIborDefinition SWAP_PAYER_DEFINITION = USD6MLIBOR3M.generateInstrument(EXPIRY_DATE, FIXED_RATE_3M, NOTIONAL, ATTRIBUTE_3M);
  private static final SwaptionPhysicalFixedIborDefinition SWAPTION_P_2Yx7Y_DEFINITION = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, SWAP_PAYER_DEFINITION, true, true);

  //  private static final SwaptionPhysicalFixedIbor SWAPTION_P_2Yx7Y = SWAPTION_P_2Yx7Y_DEFINITION.toDerivative(REFERENCE_DATE);

  @Test
  public void presentValue() {
  }

}
