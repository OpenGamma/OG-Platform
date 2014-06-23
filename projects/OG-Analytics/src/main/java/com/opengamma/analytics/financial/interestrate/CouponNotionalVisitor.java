/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitorAdapter;
import com.opengamma.analytics.financial.instrument.payment.CouponCMSDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedAccruedCompoundingDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedCompoundingDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborAverageCompoundingDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborAverageIndexDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborAverageFixingDatesDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborCompoundingDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborCompoundingFlatSpreadDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborCompoundingSpreadDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborFlatCompoundingSpreadDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborGearingDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborRatchetDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborSpreadDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponONArithmeticAverageDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponONArithmeticAverageSpreadDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponONCompoundedDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponONDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponONSpreadDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentFixedDefinition;
import com.opengamma.util.money.CurrencyAmount;

/**
 *
 */
public class CouponNotionalVisitor extends InstrumentDefinitionVisitorAdapter<Void, CurrencyAmount> {

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
  public CurrencyAmount visitCouponIborAverageDefinition(final CouponIborAverageIndexDefinition payment) {
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
  public CurrencyAmount visitCouponONSpreadDefinition(final CouponONSpreadDefinition payment) {
    return CurrencyAmount.of(payment.getCurrency(), payment.getNotional());
  }

  @Override
  public CurrencyAmount visitCouponCMSDefinition(final CouponCMSDefinition payment) {
    return CurrencyAmount.of(payment.getCurrency(), payment.getNotional());
  }

  @Override
  public CurrencyAmount visitCouponFixedCompoundingDefinition(final CouponFixedCompoundingDefinition payment) {
    return CurrencyAmount.of(payment.getCurrency(), payment.getNotional());
  }

  @Override
  public CurrencyAmount visitCouponArithmeticAverageONSpreadDefinition(final CouponONArithmeticAverageSpreadDefinition payment) {
    return CurrencyAmount.of(payment.getCurrency(), payment.getNotional());
  }

  @Override
  public CurrencyAmount visitPaymentFixedDefinition(final PaymentFixedDefinition payment) {
    return CurrencyAmount.of(payment.getCurrency(), payment.getReferenceAmount());
  }

  @Override
  public CurrencyAmount visitCouponArithmeticAverageONDefinition(final CouponONArithmeticAverageDefinition payment) {
    return CurrencyAmount.of(payment.getCurrency(), payment.getReferenceAmount());
  }

  @Override
  public CurrencyAmount visitCouponIborAverageSinglePeriodDefinition(CouponIborAverageFixingDatesDefinition payment, Void data) {
    return CurrencyAmount.of(payment.getCurrency(), payment.getNotional());
  }

  @Override
  public CurrencyAmount visitCouponIborAverageSinglePeriodDefinition(CouponIborAverageFixingDatesDefinition payment) {
    return CurrencyAmount.of(payment.getCurrency(), payment.getNotional());
  }

  @Override
  public CurrencyAmount visitCouponIborAverageCompoundingDefinition(CouponIborAverageCompoundingDefinition payment, Void data) {
    return CurrencyAmount.of(payment.getCurrency(), payment.getNotional());
  }

  @Override
  public CurrencyAmount visitCouponIborAverageCompoundingDefinition(CouponIborAverageCompoundingDefinition payment) {
    return CurrencyAmount.of(payment.getCurrency(), payment.getNotional());
  }

  @Override
  public CurrencyAmount visitCouponIborFlatCompoundingSpreadDefinition(CouponIborFlatCompoundingSpreadDefinition payment, Void data) {
    return CurrencyAmount.of(payment.getCurrency(), payment.getNotional());
  }

  @Override
  public CurrencyAmount visitCouponIborFlatCompoundingSpreadDefinition(CouponIborFlatCompoundingSpreadDefinition payment) {
    return CurrencyAmount.of(payment.getCurrency(), payment.getNotional());
  }
}
