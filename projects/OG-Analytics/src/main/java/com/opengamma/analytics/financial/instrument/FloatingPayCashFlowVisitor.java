/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.threeten.bp.LocalDate;

import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.analytics.financial.instrument.cash.DepositIborDefinition;
import com.opengamma.analytics.financial.instrument.fra.ForwardRateAgreementDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborGearingDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborSpreadDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborSpreadDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapIborIborDefinition;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Returns all of the floating cash-flows of an instrument. The notionals returned are adjusted for the year fraction
 * (i.e. a semi-annual swap with a notional of $1MM will return notionals of ~$0.5MM)
 */
public final class FloatingPayCashFlowVisitor extends InstrumentDefinitionVisitorAdapter<Object, Map<LocalDate, MultipleCurrencyAmount>> {
  private static final FloatingPayCashFlowVisitor INSTANCE = new FloatingPayCashFlowVisitor();

  public static FloatingPayCashFlowVisitor getInstance() {
    return INSTANCE;
  }

  /**
   * If the notional is positive (i.e. the amount is to be received) returns
   * an empty map. Otherwise, returns a map containing a single payment date and the notional amount
   * multiplied by the accrual period.
   * @param deposit The deposit instrument, not null
   * @return A map containing the (single) payment date and amount, or an empty map, as appropriate
   */
  @Override
  public Map<LocalDate, MultipleCurrencyAmount> visitDepositIborDefinition(final DepositIborDefinition deposit) {
    ArgumentChecker.notNull(deposit, "deposit");
    final LocalDate endDate = deposit.getEndDate().toLocalDate();
    if (deposit.getNotional() > 0) {
      return Collections.emptyMap();
    }
    final double amount = -deposit.getNotional() * deposit.getRate() * deposit.getAccrualFactor();
    return Collections.singletonMap(endDate, MultipleCurrencyAmount.of(CurrencyAmount.of(deposit.getCurrency(), amount)));
  }

  /**
   * If the notional is positive (i.e. the amount is to be received) returns
   * an empty map. Otherwise, returns a map containing a single payment date and the notional amount
   * multiplied by the accrual period.
   * @param deposit The deposit instrument, not null
   * @param data Not used
   * @return A map containing the (single) payment date and amount, or an empty map, as appropriate
   */
  @Override
  public Map<LocalDate, MultipleCurrencyAmount> visitDepositIborDefinition(final DepositIborDefinition deposit, final Object data) {
    return visitDepositIborDefinition(deposit);
  }

  /**
   * If the notional is positive (i.e. the amount is to be received), returns
   * an empty map. Otherwise, returns a map containing a single payment date and the notional amount
   * multiplied by the accrual period.
   * @param coupon The coupon instrument, not null
   * @return A map containing the (single) payment date and amount, or an empty map, as appropriate
   */
  @Override
  public Map<LocalDate, MultipleCurrencyAmount> visitCouponIborDefinition(final CouponIborDefinition coupon) {
    ArgumentChecker.notNull(coupon, "coupon");
    final LocalDate endDate = coupon.getPaymentDate().toLocalDate();
    if (coupon.getNotional() > 0) {
      return Collections.emptyMap();
    }
    final double amount = -coupon.getNotional() * coupon.getPaymentYearFraction();
    return Collections.singletonMap(endDate, MultipleCurrencyAmount.of(CurrencyAmount.of(coupon.getCurrency(), amount)));
  }

  /**
   * If the notional is positive (i.e. the amount is to be received), returns
   * an empty map. Otherwise, returns a map containing a single payment date and the notional amount
   * multiplied by the accrual period.
   * @param coupon The coupon instrument, not null
   * @param data Not used
   * @return A map containing the (single) payment date and amount, or an empty map, as appropriate
   */
  @Override
  public Map<LocalDate, MultipleCurrencyAmount> visitCouponIborDefinition(final CouponIborDefinition coupon, final Object data) {
    return visitCouponIborDefinition(coupon);
  }

  /**
   * If the notional is positive (i.e. the amount is to be received), returns
   * an empty map. Otherwise, returns a map containing a single payment date and the notional amount
   * multiplied by the accrual period.
   * @param coupon The coupon instrument, not null
   * @return A map containing the (single) payment date and amount, or an empty map, as appropriate
   */
  @Override
  public Map<LocalDate, MultipleCurrencyAmount> visitCouponIborSpreadDefinition(final CouponIborSpreadDefinition coupon) {
    ArgumentChecker.notNull(coupon, "coupon");
    final LocalDate endDate = coupon.getPaymentDate().toLocalDate();
    if (coupon.getNotional() > 0) {
      return Collections.emptyMap();
    }
    final double amount = -coupon.getNotional() * coupon.getFixingPeriodAccrualFactor() * (1 + coupon.getSpread());
    return Collections.singletonMap(endDate, MultipleCurrencyAmount.of(CurrencyAmount.of(coupon.getCurrency(), amount)));
  }

  /**
   * If the notional is positive (i.e. the amount is to be received), returns
   * an empty map. Otherwise, returns a map containing a single payment date and the notional amount
   * multiplied by the accrual period.
   * @param coupon The coupon instrument, not null
   * @param data Not used
   * @return A map containing the (single) payment date and amount, or an empty map, as appropriate
   */
  @Override
  public Map<LocalDate, MultipleCurrencyAmount> visitCouponIborSpreadDefinition(final CouponIborSpreadDefinition coupon, final Object data) {
    return visitCouponIborSpreadDefinition(coupon);
  }

  /**
   * If the notional is positive (i.e. the amount is to be received), returns
   * an empty map. Otherwise, returns a map containing a single payment date and the notional amount
   * multiplied by the accrual period.
   * @param coupon The coupon instrument, not null
   * @return A map containing the (single) payment date and amount, or an empty map, as appropriate
   */
  @Override
  public Map<LocalDate, MultipleCurrencyAmount> visitCouponIborGearingDefinition(final CouponIborGearingDefinition coupon) {
    ArgumentChecker.notNull(coupon, "coupon");
    final LocalDate endDate = coupon.getPaymentDate().toLocalDate();
    if (coupon.getNotional() > 0) {
      return Collections.emptyMap();
    }
    final double amount = -coupon.getNotional() * coupon.getFixingPeriodAccrualFactor() * (coupon.getFactor() + coupon.getSpread());
    return Collections.singletonMap(endDate, MultipleCurrencyAmount.of(CurrencyAmount.of(coupon.getCurrency(), amount)));
  }

  /**
   * If the notional is positive (i.e. the amount is to be received), returns
   * an empty map. Otherwise, returns a map containing a single payment date and the notional amount
   * multiplied by the accrual period.
   * @param coupon The coupon instrument, not null
   * @param data Not used
   * @return A map containing the (single) payment date and amount, or an empty map, as appropriate
   */
  @Override
  public Map<LocalDate, MultipleCurrencyAmount> visitCouponIborGearingDefinition(final CouponIborGearingDefinition coupon, final Object data) {
    return visitCouponIborGearingDefinition(coupon);
  }

  /**
   * If the notional is positive (i.e. the FRA is a payer), returns an empty map. Otherwise, returns
   * a map containing a single payment date and the notional amount multiplied by the accrual period
   * @param forwardRateAgreement The FRA, not null
   * @return A map containing the (single) payment date and amount, or an empty map, as appropriate
   */
  @Override
  public Map<LocalDate, MultipleCurrencyAmount> visitForwardRateAgreementDefinition(final ForwardRateAgreementDefinition forwardRateAgreement) {
    ArgumentChecker.notNull(forwardRateAgreement, "FRA");
    final LocalDate endDate = forwardRateAgreement.getPaymentDate().toLocalDate();
    if (forwardRateAgreement.getNotional() > 0) {
      return Collections.emptyMap();
    }
    final double amount = -forwardRateAgreement.getNotional() * forwardRateAgreement.getPaymentYearFraction();
    return Collections.singletonMap(endDate, MultipleCurrencyAmount.of(CurrencyAmount.of(forwardRateAgreement.getCurrency(), amount)));
  }

  /**
   * If the notional is positive (i.e. the FRA is a payer), returns an empty map. Otherwise, returns
   * a map containing a single payment date and the notional amount multiplied by the accrual period
   * @param forwardRateAgreement The FRA, not null
   * @param data Not used
   * @return A map containing the (single) payment date and amount, or an empty map, as appropriate
   */
  @Override
  public Map<LocalDate, MultipleCurrencyAmount> visitForwardRateAgreementDefinition(final ForwardRateAgreementDefinition forwardRateAgreement, final Object data) {
    return visitForwardRateAgreementDefinition(forwardRateAgreement);
  }

  /**
   * Returns a map containing all of the floating payments to be made in an annuity. If there are no floating payments to be made,
   * an empty map is returned
   * @param annuity The annuity, not null
   * @return A map containing the payment dates and amounts
   */
  @Override
  public Map<LocalDate, MultipleCurrencyAmount> visitAnnuityDefinition(final AnnuityDefinition<? extends PaymentDefinition> annuity) {
    ArgumentChecker.notNull(annuity, "annuity");
    return getDatesFromAnnuity(annuity);
  }

  /**
   * Returns a map containing all of the floating payments to be made in an annuity. If there are no floating payments to be made,
   * an empty map is returned
   * @param annuity The annuity, not null
   * @param data Not used
   * @return A map containing the payment dates and amounts
   */
  @Override
  public Map<LocalDate, MultipleCurrencyAmount> visitAnnuityDefinition(final AnnuityDefinition<? extends PaymentDefinition> annuity, final Object data) {
    return visitAnnuityDefinition(annuity);
  }

  /**
   * If the swap is a payer, returns an empty map. Otherwise, returns a map containing all of the floating payments to be made
   * @param swap The swap, not null
   * @return A map containing floating payments, or an empty map, as appropriate
   */
  @Override
  public Map<LocalDate, MultipleCurrencyAmount> visitSwapFixedIborDefinition(final SwapFixedIborDefinition swap) {
    ArgumentChecker.notNull(swap, "swap");
    if (swap.getIborLeg().isPayer()) {
      return swap.getIborLeg().accept(this);
    }
    return Collections.emptyMap();
  }

  /**
   * If the swap is a payer, returns an empty map. Otherwise, returns a map containing all of the floating payments to be made
   * @param swap The swap, not null
   * @param data Not used
   * @return A map containing floating payments, or an empty map, as appropriate
   */
  @Override
  public Map<LocalDate, MultipleCurrencyAmount> visitSwapFixedIborDefinition(final SwapFixedIborDefinition swap, final Object data) {
    return visitSwapFixedIborDefinition(swap);
  }

  /**
   * If the swap is a payer, returns an empty map. Otherwise, returns a map containing all of the floating payments to be made
   * @param swap The swap, not null
   * @return A map containing floating payments, or an empty map, as appropriate
   */
  @Override
  public Map<LocalDate, MultipleCurrencyAmount> visitSwapFixedIborSpreadDefinition(final SwapFixedIborSpreadDefinition swap) {
    ArgumentChecker.notNull(swap, "swap");
    if (swap.getIborLeg().isPayer()) {
      return swap.getIborLeg().accept(this);
    }
    return Collections.emptyMap();
  }

  /**
   * If the swap is a payer, returns an empty map. Otherwise, returns a map containing all of the floating payments to be made
   * @param swap The swap, not null
   * @param data Not used
   * @return A map containing floating payments, or an empty map, as appropriate
   */
  @Override
  public Map<LocalDate, MultipleCurrencyAmount> visitSwapFixedIborSpreadDefinition(final SwapFixedIborSpreadDefinition swap, final Object data) {
    return visitSwapFixedIborSpreadDefinition(swap);
  }

  /**
   * Returns a map containing all of the floating payments in the pay leg
   * @param swap The swap, not null
   * @return A map containing floating payments
   */
  @Override
  public Map<LocalDate, MultipleCurrencyAmount> visitSwapIborIborDefinition(final SwapIborIborDefinition swap) {
    ArgumentChecker.notNull(swap, "swap");
    if (swap.getFirstLeg().isPayer()) {
      return swap.getFirstLeg().accept(this);
    }
    return swap.getSecondLeg().accept(this);
  }

  /**
   * Returns a map containing all of the floating payments in the pay leg
   * @param swap The swap, not null
   * @param data Not used
   * @return A map containing floating payments
   */
  @Override
  public Map<LocalDate, MultipleCurrencyAmount> visitSwapIborIborDefinition(final SwapIborIborDefinition swap, final Object data) {
    return visitSwapIborIborDefinition(swap);
  }

  private Map<LocalDate, MultipleCurrencyAmount> getDatesFromAnnuity(final AnnuityDefinition<? extends PaymentDefinition> annuity) {
    final Map<LocalDate, MultipleCurrencyAmount> result = new HashMap<>();
    for (final PaymentDefinition payment : annuity.getPayments()) {
      final Map<LocalDate, MultipleCurrencyAmount> payments = payment.accept(this);
      for (final Map.Entry<LocalDate, MultipleCurrencyAmount> entry : payments.entrySet()) {
        final int scale = entry.getValue().getCurrencyAmounts()[0].getAmount() < 0 ? -1 : 1;
        final MultipleCurrencyAmount mca = entry.getValue().multipliedBy(scale);
        final LocalDate key = entry.getKey();
        if (result.containsKey(key)) {
          result.put(key, result.get(key).plus(mca));
        } else {
          result.put(key, mca);
        }
      }
    }
    return result;
  }

}
