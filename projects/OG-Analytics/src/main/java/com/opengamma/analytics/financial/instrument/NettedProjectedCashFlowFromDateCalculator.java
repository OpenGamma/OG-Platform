/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponIborSpreadDefinition;
import com.opengamma.analytics.financial.instrument.fra.ForwardRateAgreementDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborSpreadDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapIborIborDefinition;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * @deprecated {@link YieldCurveBundle} is deprecated
 */
@Deprecated
public final class NettedProjectedCashFlowFromDateCalculator {
  private static final NettedProjectedCashFlowFromDateCalculator INSTANCE = new NettedProjectedCashFlowFromDateCalculator();
  private static final PaymentsVisitor PAY_DATES = new PaymentsVisitor(true);
  private static final PaymentsVisitor RECEIVE_DATES = new PaymentsVisitor(false);

  public static NettedProjectedCashFlowFromDateCalculator getInstance() {
    return INSTANCE;
  }

  private NettedProjectedCashFlowFromDateCalculator() {
  }

  /**
   * Gets the netted projected cash-flows as of a particular date.
   * @param instrument The instrument, not null
   * @param yieldCurveNames The yield curve names, not null
   * @param date The date, not null
   * @param data The yield curves, not null
   * @return The projected netted cash flows as of a particular date
   */
  public Map<LocalDate, MultipleCurrencyAmount> getCashFlows(final InstrumentDefinition<?> instrument, final String[] yieldCurveNames,
      final ZonedDateTime date, final YieldCurveBundle data) {
    ArgumentChecker.notNull(instrument, "instrument");
    ArgumentChecker.notNull(yieldCurveNames, "yield curve names");
    ArgumentChecker.notNull(date, "date");
    ArgumentChecker.notNull(data, "data");
    final List<ZonedDateTime> payDates = instrument.accept(PAY_DATES, date);
    final List<ZonedDateTime> receiveDates = instrument.accept(RECEIVE_DATES, date);
    final List<MultipleCurrencyAmount> payCashFlows = new ArrayList<>();
    final List<MultipleCurrencyAmount> receiveCashFlows = new ArrayList<>();
    final InstrumentDerivative derivative = instrument.toDerivative(date, yieldCurveNames);
    if (payDates != null) {
      payCashFlows.addAll(derivative.accept(ProjectedPayCashFlowVisitor.getInstance(), data));
      if (payCashFlows.size() != payDates.size()) {
        throw new IllegalStateException("Did not have same number of payments as dates");
      }
    }
    if (receiveDates != null) {
      receiveCashFlows.addAll(derivative.accept(ProjectedReceiveCashFlowVisitor.getInstance(), data));
      if (receiveCashFlows.size() != receiveDates.size()) {
        throw new IllegalStateException("Did not have same number of receive payments as dates");
      }
    }
    return add(payCashFlows, payDates, receiveCashFlows, receiveDates);
  }

  /**
   * Gets the netted projected cash-flows as of a particular date.
   * @param instrument The instrument, not null
   * @param date The date, not null
   * @param data The yield curves, not null
   * @return The projected netted cash flows as of a particular date
   */
  public Map<LocalDate, MultipleCurrencyAmount> getCashFlows(final InstrumentDefinition<?> instrument, final ZonedDateTime date, final YieldCurveBundle data) {
    ArgumentChecker.notNull(instrument, "instrument");
    ArgumentChecker.notNull(date, "date");
    ArgumentChecker.notNull(data, "data");
    final List<ZonedDateTime> payDates = instrument.accept(PAY_DATES, date);
    final List<ZonedDateTime> receiveDates = instrument.accept(RECEIVE_DATES, date);
    final List<MultipleCurrencyAmount> payCashFlows = new ArrayList<>();
    final List<MultipleCurrencyAmount> receiveCashFlows = new ArrayList<>();
    final InstrumentDerivative derivative = instrument.toDerivative(date);
    if (payDates != null) {
      payCashFlows.addAll(derivative.accept(ProjectedPayCashFlowVisitor.getInstance(), data));
      if (payCashFlows.size() != payDates.size()) {
        throw new IllegalStateException("Did not have same number of payments as dates");
      }
    }
    if (receiveDates != null) {
      receiveCashFlows.addAll(derivative.accept(ProjectedReceiveCashFlowVisitor.getInstance(), data));
      if (receiveCashFlows.size() != receiveDates.size()) {
        throw new IllegalStateException("Did not have same number of receive payments as dates");
      }
    }
    return add(payCashFlows, payDates, receiveCashFlows, receiveDates);
  }

  private static Map<LocalDate, MultipleCurrencyAmount> add(final List<MultipleCurrencyAmount> payAmounts, final List<ZonedDateTime> payDates,
      final List<MultipleCurrencyAmount> receiveAmounts, final List<ZonedDateTime> receiveDates) {
    final TreeMap<LocalDate, MultipleCurrencyAmount> result = new TreeMap<>();
    for (int i = 0; i < payAmounts.size(); i++) {
      result.put(payDates.get(i).toLocalDate(), payAmounts.get(i).multipliedBy(-1));
    }
    for (int i = 0; i < receiveAmounts.size(); i++) {
      final LocalDate date = receiveDates.get(i).toLocalDate();
      if (result.containsKey(date)) {
        result.put(date, result.get(date).plus(receiveAmounts.get(i)));
      } else {
        result.put(date, receiveAmounts.get(i));
      }
    }
    return result;
  }

  private static class PaymentsVisitor extends InstrumentDefinitionVisitorAdapter<ZonedDateTime, List<ZonedDateTime>> {
    private final boolean _isPay;

    PaymentsVisitor(final boolean isPay) {
      _isPay = isPay;
    }

    @Override
    public List<ZonedDateTime> visitForwardRateAgreementDefinition(final ForwardRateAgreementDefinition fra, final ZonedDateTime date) {
      if (date.isBefore(fra.getFixingDate())) {
        return Collections.singletonList(fra.getPaymentDate());
      }
      return null;
    }

    @Override
    public List<ZonedDateTime> visitSwapFixedIborDefinition(final SwapFixedIborDefinition swap, final ZonedDateTime date) {
      if (_isPay && swap.getIborLeg().isPayer() || !_isPay && !swap.getIborLeg().isPayer()) {
        final CouponIborDefinition[] coupons = swap.getIborLeg().getPayments();
        final List<ZonedDateTime> payments = new ArrayList<>();
        for (final CouponIborDefinition coupon : coupons) {
          if (!date.isBefore(coupon.getPaymentDate())) {
            payments.add(coupon.getPaymentDate());
          }
        }
      }
      return Collections.emptyList();
    }

    @Override
    public List<ZonedDateTime> visitSwapFixedIborSpreadDefinition(final SwapFixedIborSpreadDefinition swap, final ZonedDateTime date) {
      if (_isPay && swap.getIborLeg().isPayer() || !_isPay && !swap.getIborLeg().isPayer()) {
        final PaymentDefinition[] coupons = swap.getIborLeg().getPayments();
        final List<ZonedDateTime> payments = new ArrayList<>();
        for (final PaymentDefinition coupon : coupons) {
          if (!date.isBefore(coupon.getPaymentDate())) {
            payments.add(coupon.getPaymentDate());
          }
        }
        return payments;
      }
      return Collections.emptyList();
    }

    @Override
    public List<ZonedDateTime> visitSwapIborIborDefinition(final SwapIborIborDefinition swap, final ZonedDateTime date) {
      AnnuityCouponIborSpreadDefinition payLeg;
      if (_isPay) {
        payLeg = swap.getFirstLeg().isPayer() ? swap.getFirstLeg() : swap.getSecondLeg();
      } else {
        payLeg = swap.getFirstLeg().isPayer() ? swap.getSecondLeg() : swap.getFirstLeg();
      }
      final List<ZonedDateTime> payments = new ArrayList<>();
      for (final PaymentDefinition coupon : payLeg.getPayments()) {
        if (!date.isBefore(coupon.getPaymentDate())) {
          payments.add(coupon.getPaymentDate());
        }
      }
      return payments;
    }
  }
}
