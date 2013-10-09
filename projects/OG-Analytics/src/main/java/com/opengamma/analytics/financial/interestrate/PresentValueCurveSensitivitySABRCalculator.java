/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginTransaction;
import com.opengamma.analytics.financial.interestrate.future.method.InterestRateFutureOptionMarginTransactionSABRMethod;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CapFloorCMS;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CapFloorCMSSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CapFloorIbor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponCMS;
import com.opengamma.analytics.financial.interestrate.payments.method.CapFloorCMSSABRReplicationMethod;
import com.opengamma.analytics.financial.interestrate.payments.method.CapFloorCMSSpreadSABRBinormalMethod;
import com.opengamma.analytics.financial.interestrate.payments.method.CapFloorIborSABRMethod;
import com.opengamma.analytics.financial.interestrate.payments.method.CouponCMSSABRReplicationMethod;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionCashFixedIbor;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.analytics.financial.interestrate.swaption.method.SwaptionCashFixedIborSABRMethod;
import com.opengamma.analytics.financial.interestrate.swaption.method.SwaptionPhysicalFixedIborSABRMethod;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateCorrelationParameters;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateDataBundle;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterProviderInterface;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Present value curve sensitivity calculator for interest rate instruments using SABR volatility formula.
 * @deprecated Use the calculators that reference {@link ParameterProviderInterface}
 */
@Deprecated
public final class PresentValueCurveSensitivitySABRCalculator extends PresentValueCurveSensitivityCalculator {

  /**
   * The instance of the calculator.
   */
  private static final PresentValueCurveSensitivitySABRCalculator s_instance = new PresentValueCurveSensitivitySABRCalculator();

  /**
   * Return the instance of the calculator.
   * @return The calculator.
   */
  public static PresentValueCurveSensitivitySABRCalculator getInstance() {
    return s_instance;
  }

  /**
   * Private constructor.
   */
  private PresentValueCurveSensitivitySABRCalculator() {
  }

  @Override
  public Map<String, List<DoublesPair>> visitCapFloorIbor(final CapFloorIbor cap, final YieldCurveBundle curves) {
    Validate.notNull(cap);
    Validate.notNull(curves);
    if (curves instanceof SABRInterestRateDataBundle) {
      final SABRInterestRateDataBundle sabr = (SABRInterestRateDataBundle) curves;
      final CapFloorIborSABRMethod method = CapFloorIborSABRMethod.getInstance();
      return method.presentValueSensitivity(cap, sabr).getSensitivities();
    }
    throw new UnsupportedOperationException("The PresentValueCurveSensitivitySABRCalculator visitor visitCapFloorIbor requires a SABRInterestRateDataBundle as data.");
  }

  @Override
  public Map<String, List<DoublesPair>> visitSwaptionCashFixedIbor(final SwaptionCashFixedIbor swaption, final YieldCurveBundle curves) {
    Validate.notNull(swaption);
    Validate.notNull(curves);
    if (curves instanceof SABRInterestRateDataBundle) {
      final SABRInterestRateDataBundle sabr = (SABRInterestRateDataBundle) curves;
      final SwaptionCashFixedIborSABRMethod method = SwaptionCashFixedIborSABRMethod.getInstance();
      return method.presentValueSensitivity(swaption, sabr).getSensitivities();
    }
    throw new UnsupportedOperationException("The PresentValueCurveSensitivitySABRCalculator visitor visitSwaptionCashFixedIbor requires a SABRInterestRateDataBundle as data.");
  }

  @Override
  public Map<String, List<DoublesPair>> visitSwaptionPhysicalFixedIbor(final SwaptionPhysicalFixedIbor swaption, final YieldCurveBundle curves) {
    Validate.notNull(swaption);
    Validate.notNull(curves);
    if (curves instanceof SABRInterestRateDataBundle) {
      final SABRInterestRateDataBundle sabr = (SABRInterestRateDataBundle) curves;
      final SwaptionPhysicalFixedIborSABRMethod method = SwaptionPhysicalFixedIborSABRMethod.getInstance();
      return method.presentValueCurveSensitivity(swaption, sabr).getSensitivities();
    }
    throw new UnsupportedOperationException("The PresentValueCurveSensitivitySABRCalculator visitor visitSwaptionPhysicalFixedIbor requires a SABRInterestRateDataBundle as data.");

  }

  @Override
  public Map<String, List<DoublesPair>> visitCouponCMS(final CouponCMS payment, final YieldCurveBundle curves) {
    Validate.notNull(curves);
    Validate.notNull(payment);
    if (curves instanceof SABRInterestRateDataBundle) {
      final SABRInterestRateDataBundle sabrBundle = (SABRInterestRateDataBundle) curves;
      final CouponCMSSABRReplicationMethod replication = CouponCMSSABRReplicationMethod.getInstance();
      return replication.presentValueCurveSensitivity(payment, sabrBundle).getSensitivities();
    }
    throw new UnsupportedOperationException("The PresentValueCurveSensitivitySABRCalculator visitor visitCouponCMS requires a SABRInterestRateDataBundle as data.");
  }

  @Override
  public Map<String, List<DoublesPair>> visitCapFloorCMS(final CapFloorCMS payment, final YieldCurveBundle curves) {
    Validate.notNull(curves);
    Validate.notNull(payment);
    if (curves instanceof SABRInterestRateDataBundle) {
      final SABRInterestRateDataBundle sabrBundle = (SABRInterestRateDataBundle) curves;
      final CapFloorCMSSABRReplicationMethod replication = CapFloorCMSSABRReplicationMethod.getDefaultInstance();
      return replication.presentValueCurveSensitivity(payment, sabrBundle).getSensitivities();
    }
    throw new UnsupportedOperationException("The PresentValueCurveSensitivitySABRCalculator visitor visitCapFloorCMS requires a SABRInterestRateDataBundle as data.");
  }

  @Override
  public Map<String, List<DoublesPair>> visitCapFloorCMSSpread(final CapFloorCMSSpread payment, final YieldCurveBundle curves) {
    Validate.notNull(curves);
    Validate.notNull(payment);
    if (curves instanceof SABRInterestRateDataBundle) {
      final SABRInterestRateDataBundle sabrBundle = (SABRInterestRateDataBundle) curves;
      if (sabrBundle.getSABRParameter() instanceof SABRInterestRateCorrelationParameters) {
        final SABRInterestRateCorrelationParameters sabrCorrelation = (SABRInterestRateCorrelationParameters) sabrBundle.getSABRParameter();
        final CapFloorCMSSpreadSABRBinormalMethod method = new CapFloorCMSSpreadSABRBinormalMethod(sabrCorrelation.getCorrelation(), CapFloorCMSSABRReplicationMethod.getDefaultInstance(),
            CouponCMSSABRReplicationMethod.getInstance());
        return method.presentValueCurveSensitivity(payment, sabrBundle).getSensitivities();
      }
    }
    throw new UnsupportedOperationException("The PresentValueCurveSensitivitySABRCalculator visitor visitCapFloorCMSSpread requires a SABRInterestRateDataBundle with correlation as data.");
  }

  @Override
  public Map<String, List<DoublesPair>> visitInterestRateFutureOptionMarginTransaction(final InterestRateFutureOptionMarginTransaction option, final YieldCurveBundle curves) {
    Validate.notNull(curves);
    Validate.notNull(option);
    if (curves instanceof SABRInterestRateDataBundle) {
      final SABRInterestRateDataBundle sabrBundle = (SABRInterestRateDataBundle) curves;
      final InterestRateFutureOptionMarginTransactionSABRMethod method = InterestRateFutureOptionMarginTransactionSABRMethod.getInstance();
      return method.presentValueCurveSensitivity(option, sabrBundle).getSensitivities();
    }
    throw new UnsupportedOperationException("The PresentValueSABRCalculator visitor visitInterestRateFutureOptionMarginTransaction requires a SABRInterestRateDataBundle as data.");
  }

}
