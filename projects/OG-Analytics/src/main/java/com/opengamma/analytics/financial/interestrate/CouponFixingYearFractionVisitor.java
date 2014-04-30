/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitorAdapter;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborCompoundingDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborCompoundingFlatSpreadDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborGearingDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborRatchetDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborSpreadDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponONArithmeticAverageSpreadDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponONDefinition;

/**
 *
 */
public class CouponFixingYearFractionVisitor extends InstrumentDefinitionVisitorAdapter<Void, Double> {

  @Override
  public Double visitCouponIborDefinition(final CouponIborDefinition payment) {
    return payment.getFixingPeriodAccrualFactor();
  }

  @Override
  public Double visitCouponIborSpreadDefinition(final CouponIborSpreadDefinition payment) {
    return payment.getFixingPeriodAccrualFactor();
  }

  @Override
  public Double visitCouponIborGearingDefinition(final CouponIborGearingDefinition payment) {
    return payment.getFixingPeriodAccrualFactor();
  }

  @Override
  public Double visitCouponIborRatchetDefinition(final CouponIborRatchetDefinition payment) {
    return payment.getFixingPeriodAccrualFactor();
  }

  @Override
  public Double visitCouponIborCompoundingDefinition(final CouponIborCompoundingDefinition payment) {
    double total = 0.0;
    for (double fraction : payment.getFixingPeriodAccrualFactors()) {
      total += fraction;
    }
    return total;
  }

  @Override
  public Double visitCouponOISDefinition(final CouponONDefinition payment) {
    double total = 0.0;
    for (double fraction : payment.getFixingPeriodAccrualFactor()) {
      total += fraction;
    }
    return total;
  }

  @Override
  public Double visitCouponArithmeticAverageONSpreadDefinition(final CouponONArithmeticAverageSpreadDefinition payment) {
    double total = 0.0;
    for (double fraction : payment.getFixingPeriodAccrualFactors()) {
      total += fraction;
    }
    return total;
  }

  @Override
  public Double visitCouponIborCompoundingFlatSpreadDefinition(final CouponIborCompoundingFlatSpreadDefinition payment) {
    double total = 0.0;
    for (double fraction : payment.getFixingSubperiodAccrualFactors()) {
      total += fraction;
    }
    return total;
  }

  @Override
  public Double visitCouponFixedDefinition(final CouponFixedDefinition payment) {
    return null;
  }

}
