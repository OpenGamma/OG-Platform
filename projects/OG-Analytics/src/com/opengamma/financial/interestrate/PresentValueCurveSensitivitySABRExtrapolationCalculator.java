/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.payments.CapFloorCMS;
import com.opengamma.financial.interestrate.payments.CapFloorIbor;
import com.opengamma.financial.interestrate.payments.CouponCMS;
import com.opengamma.financial.interestrate.payments.method.CapFloorCMSSABRExtrapolationRightReplicationMethod;
import com.opengamma.financial.interestrate.payments.method.CapFloorIborSABRExtrapolationRightMethod;
import com.opengamma.financial.interestrate.payments.method.CouponCMSSABRExtrapolationRightReplicationMethod;
import com.opengamma.financial.interestrate.swaption.derivative.SwaptionCashFixedIbor;
import com.opengamma.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.financial.interestrate.swaption.method.SwaptionCashFixedIborSABRExtrapolationRightMethod;
import com.opengamma.financial.interestrate.swaption.method.SwaptionPhysicalFixedIborSABRExtrapolationRightMethod;
import com.opengamma.financial.model.option.definition.SABRInterestRateDataBundle;
import com.opengamma.financial.model.option.definition.SABRInterestRateExtrapolationParameters;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Present value curve sensitivity calculator for interest rate instruments using SABR with extrapolation for high strike methods.
 */
public final class PresentValueCurveSensitivitySABRExtrapolationCalculator extends PresentValueCurveSensitivityCalculator {

  /**
   * The instance of the calculator.
   */
  private static final PresentValueCurveSensitivitySABRExtrapolationCalculator s_instance = new PresentValueCurveSensitivitySABRExtrapolationCalculator();

  /**
   * Return the instance of the calculator.
   * @return The calculator.
   */
  public static PresentValueCurveSensitivitySABRExtrapolationCalculator getInstance() {
    return s_instance;
  }

  /**
   * Private constructor.
   */
  private PresentValueCurveSensitivitySABRExtrapolationCalculator() {
  }

  @Override
  public Map<String, List<DoublesPair>> visitCapFloorIbor(final CapFloorIbor cap, final YieldCurveBundle curves) {
    Validate.notNull(cap);
    Validate.notNull(curves);
    if (curves instanceof SABRInterestRateDataBundle) {
      final SABRInterestRateDataBundle sabr = (SABRInterestRateDataBundle) curves;
      if (sabr.getSABRParameter() instanceof SABRInterestRateExtrapolationParameters) {
        final SABRInterestRateExtrapolationParameters sabrExtrapolation = (SABRInterestRateExtrapolationParameters) sabr.getSABRParameter();
        final CapFloorIborSABRExtrapolationRightMethod method = new CapFloorIborSABRExtrapolationRightMethod(sabrExtrapolation.getCutOffStrike(), sabrExtrapolation.getMu());
        return method.presentValueSensitivity(cap, sabr).getSensitivities();
      }
    }
    throw new UnsupportedOperationException("The PresentValueCurveSensitivitySABRExtrapolationCalculator visitor visitCapFloorIbor requires a SABRInterestRateExtrapolationParameter as data.");
  }

  @Override
  public Map<String, List<DoublesPair>> visitSwaptionCashFixedIbor(final SwaptionCashFixedIbor swaption, final YieldCurveBundle curves) {
    Validate.notNull(swaption);
    Validate.notNull(curves);
    if (curves instanceof SABRInterestRateDataBundle) {
      final SABRInterestRateDataBundle sabr = (SABRInterestRateDataBundle) curves;
      if (sabr.getSABRParameter() instanceof SABRInterestRateExtrapolationParameters) {
        final SABRInterestRateExtrapolationParameters sabrExtrapolation = (SABRInterestRateExtrapolationParameters) sabr.getSABRParameter();
        final SwaptionCashFixedIborSABRExtrapolationRightMethod method = new SwaptionCashFixedIborSABRExtrapolationRightMethod(sabrExtrapolation.getCutOffStrike(), sabrExtrapolation.getMu());
        return method.presentValueSensitivity(swaption, sabr).getSensitivities();
      }
    }
    throw new UnsupportedOperationException(
        "The PresentValueCurveSensitivitySABRExtrapolationCalculator visitor visitSwaptionCashFixedIbor requires a SABRInterestRateExtrapolationParameter as data.");
  }

  @Override
  public Map<String, List<DoublesPair>> visitSwaptionPhysicalFixedIbor(final SwaptionPhysicalFixedIbor swaption, final YieldCurveBundle curves) {
    Validate.notNull(swaption);
    Validate.notNull(curves);
    if (curves instanceof SABRInterestRateDataBundle) {
      final SABRInterestRateDataBundle sabr = (SABRInterestRateDataBundle) curves;
      if (sabr.getSABRParameter() instanceof SABRInterestRateExtrapolationParameters) {
        final SABRInterestRateExtrapolationParameters sabrExtrapolation = (SABRInterestRateExtrapolationParameters) sabr.getSABRParameter();
        final SwaptionPhysicalFixedIborSABRExtrapolationRightMethod method = new SwaptionPhysicalFixedIborSABRExtrapolationRightMethod(sabrExtrapolation.getCutOffStrike(), sabrExtrapolation.getMu());
        return method.presentValueSensitivity(swaption, sabr).getSensitivities();
      }
    }
    throw new UnsupportedOperationException(
        "The PresentValueCurveSensitivitySABRExtrapolationCalculator visitor visitSwaptionPhysicalFixedIbor requires a SABRInterestRateExtrapolationParameter as data.");

  }

  @Override
  public Map<String, List<DoublesPair>> visitCouponCMS(final CouponCMS payment, final YieldCurveBundle curves) {
    Validate.notNull(curves);
    Validate.notNull(payment);
    if (curves instanceof SABRInterestRateDataBundle) {
      final SABRInterestRateDataBundle sabr = (SABRInterestRateDataBundle) curves;
      if (sabr.getSABRParameter() instanceof SABRInterestRateExtrapolationParameters) {
        final SABRInterestRateExtrapolationParameters sabrExtrapolation = (SABRInterestRateExtrapolationParameters) sabr.getSABRParameter();
        final CouponCMSSABRExtrapolationRightReplicationMethod replication = new CouponCMSSABRExtrapolationRightReplicationMethod(sabrExtrapolation.getCutOffStrike(), sabrExtrapolation.getMu());
        return replication.presentValueCurveSensitivity(payment, sabr).getSensitivities();
      }
    }
    throw new UnsupportedOperationException("The PresentValueCurveSensitivitySABRExtrapolationCalculator visitor visitCouponCMS requires a SABRInterestRateExtrapolationParameter as data.");
  }

  @Override
  public Map<String, List<DoublesPair>> visitCapFloorCMS(final CapFloorCMS payment, final YieldCurveBundle curves) {
    Validate.notNull(curves);
    Validate.notNull(payment);
    if (curves instanceof SABRInterestRateDataBundle) {
      final SABRInterestRateDataBundle sabr = (SABRInterestRateDataBundle) curves;
      if (sabr.getSABRParameter() instanceof SABRInterestRateExtrapolationParameters) {
        final SABRInterestRateExtrapolationParameters sabrExtrapolation = (SABRInterestRateExtrapolationParameters) sabr.getSABRParameter();
        final CapFloorCMSSABRExtrapolationRightReplicationMethod replication = new CapFloorCMSSABRExtrapolationRightReplicationMethod(sabrExtrapolation.getCutOffStrike(), sabrExtrapolation.getMu());
        return replication.presentValueSensitivity(payment, sabr).getSensitivities();
      }
    }
    throw new UnsupportedOperationException("The PresentValueCurveSensitivitySABRExtrapolationCalculator visitor visitCapFloorCMS requires a SABRInterestRateExtrapolationParameter as data.");
  }

}
