/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.curve;

import javax.time.calendar.Period;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.curve.building.CurveBuildingFunction;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.index.GeneratorDepositON;
import com.opengamma.analytics.financial.instrument.index.GeneratorDepositONCounterpart;
import com.opengamma.analytics.financial.instrument.index.GeneratorFixedON;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.interestrate.LastTimeCalculator;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.util.money.Currency;

/**
 * Build curves using bills and bonds.
 */
public class CurveConstructionBillBondTest {

  private static final Interpolator1D INTERPOLATOR_LINEAR = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.LINEAR, Interpolator1DFactory.FLAT_EXTRAPOLATOR,
      Interpolator1DFactory.FLAT_EXTRAPOLATOR);
  private static final LastTimeCalculator MATURITY_CALCULATOR = LastTimeCalculator.getInstance();
  private static final double TOLERANCE_ROOT = 1.0E-10;
  private static final int STEP_MAX = 100;
  private static final CurveBuildingFunction CURVE_BUILDING_FUNCTION = new CurveBuildingFunction(TOLERANCE_ROOT, TOLERANCE_ROOT, STEP_MAX);
  private static final Currency CCY_USD = Currency.USD;
  private static final FXMatrix FX_MATRIX = new FXMatrix(CCY_USD);
  private static final Calendar CALENDAR = new MondayToFridayCalendar("CAL");
  private static final int SPOT_LAG = 2;
  private static final DayCount DAY_COUNT_CASH = DayCountFactory.INSTANCE.getDayCount("Actual/360");
  private static final double NOTIONAL = 1.0;
  private static final BusinessDayConvention BDC = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
  private static final IndexON INDEX_FED_FUND = new IndexON("Fed Fund", CCY_USD, DAY_COUNT_CASH, 1, CALENDAR);
  private static final GeneratorDepositON GENERATOR_DEPOSIT_ON_USD = new GeneratorDepositON("USD Deposit ON", CCY_USD, CALENDAR, DAY_COUNT_CASH);
  private static final GeneratorFixedON GENERATOR_OIS_USD = new GeneratorFixedON("USD1YFEDFUND", INDEX_FED_FUND, Period.ofMonths(12), DAY_COUNT_CASH, BDC, true, SPOT_LAG, SPOT_LAG);

  private static final String NAME_COUNTERPART = "US GOVT";
  private static final GeneratorDepositONCounterpart GENERATOR_DEPOSIT_ON_USGOVT = new GeneratorDepositONCounterpart("US GOVT Deposit ON", CCY_USD, CALENDAR, DAY_COUNT_CASH, NAME_COUNTERPART);

  //  private static final GeneratorBill GENERATOR

  @Test
  public void f() {
  }
}
