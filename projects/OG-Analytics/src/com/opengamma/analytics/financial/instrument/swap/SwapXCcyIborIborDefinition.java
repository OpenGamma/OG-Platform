/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.swap;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponIborSpreadDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapXCcyIborIbor;
import com.opengamma.analytics.financial.instrument.payment.PaymentDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentFixedDefinition;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.swap.derivative.Swap;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.timeseries.DoubleTimeSeries;

/**
 * Class describing a Ibor+Spread for Ibor+Spread payments swap. The two legs can be in different currencies.
 */
public class SwapXCcyIborIborDefinition extends SwapDefinition {

  /**
   * Constructor of the ibor-ibor swap from its two legs.
   * @param firstLeg The first Ibor leg.
   * @param secondLeg The second Ibor leg.
   */
  public SwapXCcyIborIborDefinition(final AnnuityDefinition<PaymentDefinition> firstLeg, final AnnuityDefinition<PaymentDefinition> secondLeg) {
    super(firstLeg, secondLeg);
  }

  /**
   * Builder from the settlement date and a generator. The legs have different notionals. 
   * The notionals are paid on the settlement date and final payment date of each leg.
   * @param settlementDate The settlement date.
   * @param tenor The swap tenor.
   * @param generator The Ibor/Ibor swap generator.
   * @param notional1 The first leg notional.
   * @param notional2 The second leg notional.
   * @param spread The spread to be applied to the first leg.
   * @param isPayer The payer flag for the first leg.
   * @return The swap.
   */
  public static SwapXCcyIborIborDefinition from(final ZonedDateTime settlementDate, final Period tenor, final GeneratorSwapXCcyIborIbor generator, final double notional1, final double notional2,
      final double spread, final boolean isPayer) {
    ArgumentChecker.notNull(settlementDate, "settlement date");
    ArgumentChecker.notNull(tenor, "Tenor");
    ArgumentChecker.notNull(generator, "Swap generator");
    final double sign = (isPayer) ? -1.0 : 1.0;
    final AnnuityCouponIborSpreadDefinition firstLegNoNotional = AnnuityCouponIborSpreadDefinition.from(settlementDate, tenor, notional1, generator.getIborIndex1(), spread, isPayer);
    final int nbPay1 = firstLegNoNotional.getNumberOfPayments();
    final PaymentDefinition[] firstLegNotional = new PaymentDefinition[nbPay1 + 2];
    firstLegNotional[0] = new PaymentFixedDefinition(firstLegNoNotional.getCurrency(), settlementDate, -notional1 * sign);
    for (int loopp = 0; loopp < nbPay1; loopp++) {
      firstLegNotional[loopp + 1] = firstLegNoNotional.getNthPayment(loopp);
    }
    firstLegNotional[nbPay1 + 1] = new PaymentFixedDefinition(firstLegNoNotional.getCurrency(), firstLegNoNotional.getNthPayment(nbPay1 - 1).getPaymentDate(), notional1 * sign);
    final AnnuityCouponIborSpreadDefinition secondLegNoNotional = AnnuityCouponIborSpreadDefinition.from(settlementDate, tenor, notional2, generator.getIborIndex2(), 0.0, !isPayer);
    final int nbPay2 = secondLegNoNotional.getNumberOfPayments();
    final PaymentDefinition[] secondLegNotional = new PaymentDefinition[nbPay2 + 2];
    secondLegNotional[0] = new PaymentFixedDefinition(secondLegNoNotional.getCurrency(), settlementDate, notional2 * sign);
    for (int loopp = 0; loopp < nbPay1; loopp++) {
      secondLegNotional[loopp + 1] = secondLegNoNotional.getNthPayment(loopp);
    }
    secondLegNotional[nbPay1 + 1] = new PaymentFixedDefinition(secondLegNoNotional.getCurrency(), secondLegNoNotional.getNthPayment(nbPay2 - 1).getPaymentDate(), -notional2 * sign);
    return new SwapXCcyIborIborDefinition(new AnnuityDefinition<PaymentDefinition>(firstLegNotional), new AnnuityDefinition<PaymentDefinition>(secondLegNotional));
  }

  @Override
  public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
    return visitor.visitSwapXCcyIborIborDefinition(this, data);
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    return visitor.visitSwapXCcyIborIborDefinition(this);
  }

  @Override
  public Swap<Payment, Payment> toDerivative(final ZonedDateTime date, final String... yieldCurveNames) {
    ArgumentChecker.isTrue(yieldCurveNames.length >= 4, "Should have at least 4 curve names");
    final String[] firstLegCurveNames = new String[] {yieldCurveNames[0], yieldCurveNames[1] };
    final String[] secondLegCurveNames = new String[] {yieldCurveNames[2], yieldCurveNames[3] };
    @SuppressWarnings("unchecked")
    final Annuity<Payment> firstLeg = (Annuity<Payment>) getFirstLeg().toDerivative(date, firstLegCurveNames);
    @SuppressWarnings("unchecked")
    final Annuity<Payment> secondLeg = (Annuity<Payment>) getSecondLeg().toDerivative(date, secondLegCurveNames);
    return new Swap<Payment, Payment>(firstLeg, secondLeg);
  }

  @Override
  public Swap<Payment, Payment> toDerivative(final ZonedDateTime date, final DoubleTimeSeries<ZonedDateTime>[] indexDataTS, final String... yieldCurveNames) {
    Validate.notNull(indexDataTS, "index data time series array");
    Validate.isTrue(indexDataTS.length > 1, "index data time series must contain at least two elements");
    ArgumentChecker.isTrue(yieldCurveNames.length >= 4, "Should have at least 4 curve names");
    final String[] firstLegCurveNames = new String[] {yieldCurveNames[0], yieldCurveNames[1] };
    final String[] secondLegCurveNames = new String[] {yieldCurveNames[2], yieldCurveNames[3] };
    @SuppressWarnings("unchecked")
    final Annuity<Payment> firstLeg = (Annuity<Payment>) getFirstLeg().toDerivative(date, indexDataTS[0], firstLegCurveNames);
    @SuppressWarnings("unchecked")
    final Annuity<Payment> secondLeg = (Annuity<Payment>) getSecondLeg().toDerivative(date, indexDataTS[1], secondLegCurveNames);
    return new Swap<Payment, Payment>(firstLeg, secondLeg);
  }
}
