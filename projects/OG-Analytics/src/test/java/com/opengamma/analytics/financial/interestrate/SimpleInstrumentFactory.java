/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import javax.time.calendar.Period;

import cern.jet.random.engine.MersenneTwister;
import cern.jet.random.engine.MersenneTwister64;
import cern.jet.random.engine.RandomEngine;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.interestrate.cash.derivative.Cash;
import com.opengamma.analytics.financial.interestrate.fra.derivative.ForwardRateAgreement;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFuture;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.convention.frequency.SimpleFrequency;
import com.opengamma.util.money.Currency;

/**
 * A set of methods to generate simply interest rate derivatives for testing purposes
 */
public abstract class SimpleInstrumentFactory {

  private static final Currency CUR = Currency.EUR;
  private static final Period TENOR = Period.ofMonths(6);
  private static final int SETTLEMENT_DAYS = 2;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final DayCount DAY_COUNT_INDEX = DayCountFactory.INSTANCE.getDayCount("Actual/360");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
  private static final boolean IS_EOM = true;
  private static final IborIndex INDEX = new IborIndex(CUR, TENOR, SETTLEMENT_DAYS, CALENDAR, DAY_COUNT_INDEX, BUSINESS_DAY, IS_EOM);

  /** Random number generator */
  protected static final RandomEngine RANDOM = new MersenneTwister64(MersenneTwister.DEFAULT_SEED);
  /** Replaces rates */
  protected static final RateReplacingInterestRateDerivativeVisitor REPLACE_RATE = RateReplacingInterestRateDerivativeVisitor.getInstance();
  private static final Currency DUMMY_CUR = Currency.EUR;
  private static final IborIndex DUMMY_INDEX = new IborIndex(DUMMY_CUR, Period.ofMonths(1), 2, new MondayToFridayCalendar("A"), DayCountFactory.INSTANCE.getDayCount("Actual/365"),
      BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following"), true);
  private static final IndexON DUMMY_OIS_INDEX = new IndexON("OIS", DUMMY_CUR, DayCountFactory.INSTANCE.getDayCount("Actual/365"), 0, new MondayToFridayCalendar("A"));

  public static InstrumentDerivative makeCash(final double time, final String fundCurveName, final double rate, final double notional) {
    return new Cash(DUMMY_CUR, 0, time, notional, rate, time, fundCurveName);
  }

  public static InstrumentDerivative makeLibor(final double time, final String indexCurveName, final double rate, final double notional) {
    return new Cash(DUMMY_CUR, 0, time, notional, rate, time, indexCurveName);
  }

  /**
   * makes a very simple FRA with  payment time, fixing time and fixing period start being identical and an amount tau before fixing period end. The payment and fixing year fractions are
   * Identically equal to tau.
   * @param time The fixing period end (the last relevant date for the FRA)
   * @param paymentFreq for a 3M FRA the payment freq is quarterly
   * @param fundCurveName Name of funding curve
   * @param indexCurveName Name of index curve
   * @param rate The FRA rate
   * @param notional the notional amount
   * @return A FRA
   */
  public static InstrumentDerivative makeFRA(final double time, final SimpleFrequency paymentFreq, final String fundCurveName, final String indexCurveName, final double rate, final double notional) {
    final double tau = 1. / paymentFreq.getPeriodsPerYear();
    return new ForwardRateAgreement(DUMMY_CUR, time - tau, fundCurveName, tau, notional, DUMMY_INDEX, time - tau, time - tau, time, tau, rate, indexCurveName);
  }

  public static InstrumentDerivative makeFuture(final double time, final SimpleFrequency paymentFreq, final String fundCurveName, final String indexCurveName) {
    final double tau = 1. / paymentFreq.getPeriodsPerYear();
    return new InterestRateFuture(time, DUMMY_INDEX, time, time + tau, tau, 0, 1, tau, 1, "N", fundCurveName, indexCurveName);
  }

}
