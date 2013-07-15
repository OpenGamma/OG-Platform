/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.bond.method;

import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.bond.BondFixedSecurityDefinition;
import com.opengamma.analytics.financial.interestrate.PresentValueCalculator;
import com.opengamma.analytics.financial.interestrate.TestsDataSetsSABR;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.convention.yield.SimpleYieldConvention;
import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;

/**
 * Tests related to the discounting method for bond security.
 */
public class BondSecurityFRDiscountingMethodTest {

  // Calculators
  private static final PresentValueCalculator PVC = PresentValueCalculator.getInstance();
  private static final BondSecurityDiscountingMethod METHOD = BondSecurityDiscountingMethod.getInstance();

  private static final double TOLERANCE_PRICE = 1.0E-8;

  private static final String REPO_TYPE = "General collateral";
  private static final Currency EUR = Currency.EUR;
  private static final Calendar TARGET = new MondayToFridayCalendar("TARGET");
  private static final DayCount DAY_COUNT_ACTACTICMA = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ICMA");
  private static final BusinessDayConvention BUSINESS_DAY_FOL = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following");
  private static final boolean IS_EOM_FIXED = false;

  // To derivatives
  private static final DayCount ACT_ACT = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ISDA");
  private static final String CREDIT_CURVE_NAME = "Credit";
  private static final String REPO_CURVE_NAME = "Repo";
  private static final String FORWARD_CURVE_NAME = "Forward";
  private static final String[] CURVES_NAME = {CREDIT_CURVE_NAME, REPO_CURVE_NAME, FORWARD_CURVE_NAME };
  private static final YieldCurveBundle CURVES = TestsDataSetsSABR.createCurvesBond1();

  // FRTR 4 1/2 04/25/41 - ISIN:  FR0010773192
  private static final String ISSUER_FR = "FRANCE (GOVT OF)";
  private static final YieldConvention YIELD_CONVENTION_FRANCE = SimpleYieldConvention.FRANCE_COMPOUND_METHOD;
  private static final int SETTLEMENT_DAYS_FR = 3;
  private static final Period PAYMENT_TENOR_FR = Period.ofMonths(12);
  //  private static final int COUPON_PER_YEAR_FR = 1;
  private static final ZonedDateTime BOND_START_FR = DateUtils.getUTCDate(2009, 4, 25);
  private static final ZonedDateTime BOND_MATURITY_FR = DateUtils.getUTCDate(2041, 4, 25);
  private static final ZonedDateTime BOND_FIRSTCPN_FR = DateUtils.getUTCDate(2010, 4, 25);
  private static final double RATE_FR = 0.0450;
  private static final BondFixedSecurityDefinition BOND_FR_SECURITY_DEFINITION = BondFixedSecurityDefinition.from(EUR, BOND_START_FR, BOND_FIRSTCPN_FR, BOND_MATURITY_FR, PAYMENT_TENOR_FR, RATE_FR,
      SETTLEMENT_DAYS_FR, TARGET, DAY_COUNT_ACTACTICMA, BUSINESS_DAY_FOL, YIELD_CONVENTION_FRANCE, IS_EOM_FIXED, ISSUER_FR);

}
