/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.swaption;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.instrument.index.IndexSwap;
import com.opengamma.financial.instrument.index.IborIndex;
import com.opengamma.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.financial.interestrate.swaption.derivative.SwaptionCashFixedIbor;
import com.opengamma.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;

/**
 * Contains a set of Swaptions instruments that can be used in tests.
 */
public class SwaptionInstrumentsDescriptionDataSet {
  // Swaption: description
  private static final ZonedDateTime EXPIRY_DATE = DateUtils.getUTCDate(2011, 3, 28);
  private static final boolean IS_LONG = true;
  // Swap 2Y: description
  private static final Currency CUR = Currency.USD;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
  private static final boolean IS_EOM = true;
  private static final ZonedDateTime SETTLEMENT_DATE = DateUtils.getUTCDate(2011, 3, 30);
  private static final Period SWAP_TENOR = Period.ofYears(2);
  private static final double NOTIONAL = 1000000;
  private static final Period FIXED_PAYMENT_PERIOD = Period.ofMonths(6);
  private static final DayCount FIXED_DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("30/360");
  private static final double RATE = 0.0325;
  private static final boolean FIXED_IS_PAYER = true;
  private static final Period INDEX_TENOR = Period.ofMonths(3);
  private static final int SETTLEMENT_DAYS = 2;
  private static final DayCount DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("Actual/360");
  private static final IborIndex INDEX = new IborIndex(CUR, INDEX_TENOR, SETTLEMENT_DAYS, CALENDAR, DAY_COUNT, BUSINESS_DAY, IS_EOM);
  private static final IndexSwap CMS_INDEX = new IndexSwap(FIXED_PAYMENT_PERIOD, FIXED_DAY_COUNT, INDEX, SWAP_TENOR);
  private static final SwapFixedIborDefinition SWAP = SwapFixedIborDefinition.from(SETTLEMENT_DATE, CMS_INDEX, NOTIONAL, RATE, FIXED_IS_PAYER);

  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2010, 8, 18);
  private static final String FUNDING_CURVE_NAME = "Funding";
  private static final String FORWARD_CURVE_NAME = "Forward";
  private static final String[] CURVES_NAME = {FUNDING_CURVE_NAME, FORWARD_CURVE_NAME};

  public static SwaptionCashFixedIborDefinition createSwaptionCashFixedIborDefinition() {
    return SwaptionCashFixedIborDefinition.from(EXPIRY_DATE, SWAP, IS_LONG);
  }

  public static SwaptionCashFixedIbor createSwaptionCashFixedIbor() {
    return createSwaptionCashFixedIborDefinition().toDerivative(REFERENCE_DATE, CURVES_NAME);
  }

  public static SwaptionPhysicalFixedIborDefinition createSwaptionPhysicalFixedIborDefinition() {
    return SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, SWAP, IS_LONG);
  }

  public static SwaptionPhysicalFixedIbor createSwaptionPhysicalFixedIbor() {
    return createSwaptionPhysicalFixedIborDefinition().toDerivative(REFERENCE_DATE, CURVES_NAME);
  }

}
