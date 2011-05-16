/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.payments.CapFloorCMS;
import com.opengamma.financial.interestrate.payments.CouponCMS;
import com.opengamma.financial.interestrate.payments.method.CapFloorCMSSABRExtrapolationRightReplicationMethod;
import com.opengamma.financial.interestrate.payments.method.CouponCMSSABRExtrapolationRightReplicationMethod;
import com.opengamma.financial.interestrate.swaption.SwaptionCashFixedIbor;
import com.opengamma.financial.interestrate.swaption.SwaptionPhysicalFixedIbor;
import com.opengamma.financial.interestrate.swaption.method.SwaptionCashFixedIborSABRExtrapolationRightMethod;
import com.opengamma.financial.interestrate.swaption.method.SwaptionPhysicalFixedIborSABRExtrapolationRightMethod;
import com.opengamma.financial.model.option.definition.SABRInterestRateDataBundle;
import com.opengamma.financial.model.option.definition.SABRInterestRateExtrapolationParameter;

/**
 * Present value calculator for interest rate instruments using SABR volatility formula with extrapolation for high strikes.
 * OpenGamma implementation note for the extrapolation: Smile extrapolation, version 1.2, May 2011.
 */
public final class PresentValueSABRExtrapolationCalculator extends PresentValueCalculator {

  private static final PresentValueSABRExtrapolationCalculator s_instance = new PresentValueSABRExtrapolationCalculator();

  public static PresentValueSABRExtrapolationCalculator getInstance() {
    return s_instance;
  }

  private PresentValueSABRExtrapolationCalculator() {
  }

  @Override
  public Double visitSwaptionCashFixedIbor(final SwaptionCashFixedIbor swaption, final YieldCurveBundle curves) {
    Validate.notNull(swaption);
    Validate.notNull(curves);
    if (curves instanceof SABRInterestRateDataBundle) {
      SABRInterestRateDataBundle sabr = (SABRInterestRateDataBundle) curves;
      if (sabr.getSABRParameter() instanceof SABRInterestRateExtrapolationParameter) {
        SABRInterestRateExtrapolationParameter sabrExtrapolation = (SABRInterestRateExtrapolationParameter) sabr.getSABRParameter();
        SwaptionCashFixedIborSABRExtrapolationRightMethod method = new SwaptionCashFixedIborSABRExtrapolationRightMethod(sabrExtrapolation.getCutOffStrike(), sabrExtrapolation.getMu());
        return method.presentValue(swaption, sabr);
      }
    }
    throw new UnsupportedOperationException("The PresentValueSABRExtrapolationCalculator visitor visitSwaptionCashFixedIbor requires a SABRInterestRateDataBundle as data.");
  }

  @Override
  public Double visitSwaptionPhysicalFixedIbor(final SwaptionPhysicalFixedIbor swaption, final YieldCurveBundle curves) {
    Validate.notNull(swaption);
    Validate.notNull(curves);
    if (curves instanceof SABRInterestRateDataBundle) {
      SABRInterestRateDataBundle sabr = (SABRInterestRateDataBundle) curves;
      if (sabr.getSABRParameter() instanceof SABRInterestRateExtrapolationParameter) {
        SABRInterestRateExtrapolationParameter sabrExtrapolation = (SABRInterestRateExtrapolationParameter) sabr.getSABRParameter();
        SwaptionPhysicalFixedIborSABRExtrapolationRightMethod method = new SwaptionPhysicalFixedIborSABRExtrapolationRightMethod(sabrExtrapolation.getCutOffStrike(), sabrExtrapolation.getMu());
        return method.presentValue(swaption, sabr);
      }
    }
    throw new UnsupportedOperationException("The PresentValueSABRExtrapolationCalculator visitor visitSwaptionPhysicalFixedIbor requires a SABRInterestRateDataBundle as data.");

  }

  @Override
  public Double visitCouponCMS(CouponCMS payment, final YieldCurveBundle curves) {
    Validate.notNull(curves);
    Validate.notNull(payment);
    if (curves instanceof SABRInterestRateDataBundle) {
      SABRInterestRateDataBundle sabr = (SABRInterestRateDataBundle) curves;
      if (sabr.getSABRParameter() instanceof SABRInterestRateExtrapolationParameter) {
        SABRInterestRateExtrapolationParameter sabrExtrapolation = (SABRInterestRateExtrapolationParameter) sabr.getSABRParameter();
        CouponCMSSABRExtrapolationRightReplicationMethod replication = new CouponCMSSABRExtrapolationRightReplicationMethod(sabrExtrapolation.getCutOffStrike(), sabrExtrapolation.getMu());
        return replication.presentValue(payment, sabr);
      }
    }
    throw new UnsupportedOperationException("The PresentValueSABRExtrapolationCalculator visitor visitCouponCMS requires a SABRInterestRateDataBundle as data.");
  }

  @Override
  public Double visitCapFloorCMS(CapFloorCMS payment, final YieldCurveBundle curves) {
    Validate.notNull(curves);
    Validate.notNull(payment);
    if (curves instanceof SABRInterestRateDataBundle) {
      SABRInterestRateDataBundle sabr = (SABRInterestRateDataBundle) curves;
      if (sabr.getSABRParameter() instanceof SABRInterestRateExtrapolationParameter) {
        SABRInterestRateExtrapolationParameter sabrExtrapolation = (SABRInterestRateExtrapolationParameter) sabr.getSABRParameter();
        CapFloorCMSSABRExtrapolationRightReplicationMethod replication = new CapFloorCMSSABRExtrapolationRightReplicationMethod(sabrExtrapolation.getCutOffStrike(), sabrExtrapolation.getMu());
        return replication.presentValue(payment, sabr);
      }
    }
    throw new UnsupportedOperationException("The PresentValueSABRExtrapolationCalculator visitor visitCapFloorCMS requires a SABRInterestRateDataBundle as data.");
  }

}
