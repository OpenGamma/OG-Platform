/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.cashflow;

import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitorAdapter;
import com.opengamma.analytics.financial.instrument.payment.CouponCMSDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedAccruedCompoundingDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborAverageDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborCompoundingDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborCompoundingFlatSpreadDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborCompoundingSpreadDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborGearingDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborRatchetDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborSpreadDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponONCompoundedDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponONDefinition;
import com.opengamma.util.money.CurrencyAmount;

/**
 * Class that gets the notional of a coupon.
 */
public final class CouponNotionalVisitor extends InstrumentDefinitionVisitorAdapter<Void, CurrencyAmount> {
  /** The singleton instance */
  private static final InstrumentDefinitionVisitor<Void, CurrencyAmount> INSTANCE = new CouponNotionalVisitor();

  /**
   * Gets the singleton instance.
   * @return The instance
   */
  public static InstrumentDefinitionVisitor<Void, CurrencyAmount> getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor.
   */
  private CouponNotionalVisitor() {
  }

  @Override
  public CurrencyAmount visitCouponFixedDefinition(final CouponFixedDefinition payment) {
    return CurrencyAmount.of(payment.getCurrency(), payment.getNotional());
  }

  @Override
  public CurrencyAmount visitCouponFixedAccruedCompoundingDefinition(final CouponFixedAccruedCompoundingDefinition payment) {
    return CurrencyAmount.of(payment.getCurrency(), payment.getNotional());
  }

  @Override
  public CurrencyAmount visitCouponIborDefinition(final CouponIborDefinition payment) {
    return CurrencyAmount.of(payment.getCurrency(), payment.getNotional());
  }

  @Override
  public CurrencyAmount visitCouponIborAverageDefinition(final CouponIborAverageDefinition payment) {
    return CurrencyAmount.of(payment.getCurrency(), payment.getNotional());
  }

  @Override
  public CurrencyAmount visitCouponIborSpreadDefinition(final CouponIborSpreadDefinition payment) {
    return CurrencyAmount.of(payment.getCurrency(), payment.getNotional());
  }

  @Override
  public CurrencyAmount visitCouponIborGearingDefinition(final CouponIborGearingDefinition payment) {
    return CurrencyAmount.of(payment.getCurrency(), payment.getNotional());
  }

  @Override
  public CurrencyAmount visitCouponIborCompoundingDefinition(final CouponIborCompoundingDefinition payment) {
    return CurrencyAmount.of(payment.getCurrency(), payment.getNotional());
  }

  @Override
  public CurrencyAmount visitCouponIborCompoundingSpreadDefinition(final CouponIborCompoundingSpreadDefinition payment) {
    return CurrencyAmount.of(payment.getCurrency(), payment.getNotional());
  }

  @Override
  public CurrencyAmount visitCouponIborCompoundingFlatSpreadDefinition(final CouponIborCompoundingFlatSpreadDefinition payment) {
    return CurrencyAmount.of(payment.getCurrency(), payment.getNotional());
  }

  @Override
  public CurrencyAmount visitCouponIborRatchetDefinition(final CouponIborRatchetDefinition payment) {
    return CurrencyAmount.of(payment.getCurrency(), payment.getNotional());
  }

  @Override
  public CurrencyAmount visitCouponOISDefinition(final CouponONDefinition payment) {
    return CurrencyAmount.of(payment.getCurrency(), payment.getNotional());
  }

  @Override
  public CurrencyAmount visitCouponONCompoundedDefinition(final CouponONCompoundedDefinition payment) {
    return CurrencyAmount.of(payment.getCurrency(), payment.getNotional());
  }

  @Override
  public CurrencyAmount visitCouponCMSDefinition(final CouponCMSDefinition payment) {
    return CurrencyAmount.of(payment.getCurrency(), payment.getNotional());
  }

}
