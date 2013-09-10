/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.annuity.derivative;

import java.util.ArrayList;
import java.util.Arrays;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.interestrate.ParRateCalculator;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CapFloorIbor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborGearing;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborRatchet;
import com.opengamma.util.ArgumentChecker;

/**
 * A wrapper class for a AnnuityDefinition containing mainly CouponIborRatchetDefinition. The first coupon should be a CouponFixed or a CouponIborGearing.
 * The other coupons should be CouponFixed or a CouponIborRatchet.
 */
@SuppressWarnings("deprecation")
public class AnnuityCouponIborRatchet extends Annuity<Coupon> {

  /**
   * List of calibration types for the Ratchet Ibor coupon annuity.
   */
  public enum RatchetIborCalibrationType {
    /**
     * The calibration instruments are caps at strike given by the coupon in the forward curve.
     */
    FORWARD_COUPON
  }

  /**
   * Flag indicating if a coupon is already fixed.
   */
  private final boolean[] _isFixed;

  /**
   * @param payments The payments composing the annuity.
   */
  public AnnuityCouponIborRatchet(final Coupon[] payments) {
    super(payments);
    _isFixed = new boolean[payments.length];
    ArgumentChecker.isTrue((payments[0] instanceof CouponFixed) || (payments[0] instanceof CouponIborGearing), "First coupon should be CouponFixed or a CouponIborGearing");
    _isFixed[0] = (payments[0] instanceof CouponFixed);
    for (int looppay = 1; looppay < payments.length; looppay++) {
      ArgumentChecker.isTrue((payments[looppay] instanceof CouponFixed) || (payments[looppay] instanceof CouponIborRatchet), "Next coupons should be CouponFixed or CouponIborRatchet");
      _isFixed[looppay] = (payments[looppay] instanceof CouponFixed);
    }
  }

  /**
   * Gets the flag indicating if a coupon is already fixed.
   * @return The flag.
   */
  public boolean[] isFixed() {
    return _isFixed;
  }

  /**
   * @param type The calibration type
   * @param curves The yield curves
   * @return A list of coupons that are used in calibration
   * @deprecated {@link YieldCurveBundle} is deprecated
   */
  //REVIEW emcleod This method does not belong in this class
  @Deprecated
  public InstrumentDerivative[] calibrationBasket(final RatchetIborCalibrationType type, final YieldCurveBundle curves) {
    final ArrayList<InstrumentDerivative> calibration = new ArrayList<>();
    final ParRateCalculator prc = ParRateCalculator.getInstance();
    switch (type) {
      case FORWARD_COUPON:
        final int nbCpn = getNumberOfPayments();
        final double[] cpnRate = new double[nbCpn];
        for (int loopcpn = 0; loopcpn < nbCpn; loopcpn++) {
          if (getNthPayment(loopcpn) instanceof CouponIborRatchet) {
            final CouponIborRatchet cpn = (CouponIborRatchet) getNthPayment(loopcpn);
            final double ibor = prc.visitCouponIborSpread(cpn, curves);
            final double cpnMain = cpn.getMainCoefficients()[0] * cpnRate[loopcpn - 1] + cpn.getMainCoefficients()[1] * ibor + cpn.getMainCoefficients()[2];
            final double cpnFloor = cpn.getFloorCoefficients()[0] * cpnRate[loopcpn - 1] + cpn.getFloorCoefficients()[1] * ibor + cpn.getFloorCoefficients()[2];
            final double cpnCap = cpn.getCapCoefficients()[0] * cpnRate[loopcpn - 1] + cpn.getCapCoefficients()[1] * ibor + cpn.getCapCoefficients()[2];
            cpnRate[loopcpn] = Math.min(Math.max(cpnFloor, cpnMain), cpnCap);
            calibration.add(new CapFloorIbor(cpn.getCurrency(), cpn.getPaymentTime(), cpn.getFundingCurveName(), cpn.getPaymentYearFraction(), cpn.getNotional(), cpn.getFixingTime(), cpn.getIndex(),
                cpn.getFixingPeriodStartTime(), cpn.getFixingPeriodEndTime(), cpn.getFixingAccrualFactor(), cpn.getForwardCurveName(), cpnRate[loopcpn], true));
          } else {
            if (getNthPayment(loopcpn) instanceof CouponFixed) {
              final CouponFixed cpn = (CouponFixed) getNthPayment(loopcpn);
              cpnRate[loopcpn] = cpn.getFixedRate();
            } else {
              final CouponIborGearing cpn = (CouponIborGearing) getNthPayment(loopcpn);
              final double ibor = prc.visitCouponIborGearing(cpn, curves);
              cpnRate[loopcpn] = cpn.getFactor() * ibor + cpn.getSpread();
            }
          }
        }
        break;

      default:
        break;
    }
    return calibration.toArray(new InstrumentDerivative[calibration.size()]);
  }

  @Override
  public <S, T> T accept(final InstrumentDerivativeVisitor<S, T> visitor, final S data) {
    return visitor.visitAnnuityCouponIborRatchet(this, data);
  }

  @Override
  public <T> T accept(final InstrumentDerivativeVisitor<?, T> visitor) {
    return visitor.visitAnnuityCouponIborRatchet(this);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + Arrays.hashCode(_isFixed);
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (!(obj instanceof AnnuityCouponIborRatchet)) {
      return false;
    }
    final AnnuityCouponIborRatchet other = (AnnuityCouponIborRatchet) obj;
    if (!Arrays.equals(_isFixed, other._isFixed)) {
      return false;
    }
    return true;
  }

}
