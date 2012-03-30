/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.interestrate.payments.CapFloorCMS;
import com.opengamma.analytics.financial.interestrate.payments.CapFloorIbor;
import com.opengamma.analytics.financial.interestrate.payments.CouponCMS;
import com.opengamma.analytics.financial.interestrate.payments.method.CapFloorCMSSABRExtrapolationRightReplicationMethod;
import com.opengamma.analytics.financial.interestrate.payments.method.CapFloorIborSABRExtrapolationRightMethod;
import com.opengamma.analytics.financial.interestrate.payments.method.CouponCMSSABRExtrapolationRightReplicationMethod;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionCashFixedIbor;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.analytics.financial.interestrate.swaption.method.SwaptionCashFixedIborSABRExtrapolationRightMethod;
import com.opengamma.analytics.financial.interestrate.swaption.method.SwaptionPhysicalFixedIborSABRExtrapolationRightMethod;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateDataBundle;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateExtrapolationParameters;

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
  public Double visitCapFloorIbor(final CapFloorIbor cap, final YieldCurveBundle curves) {
    Validate.notNull(cap);
    Validate.notNull(curves);
    if (curves instanceof SABRInterestRateDataBundle) {
      final SABRInterestRateDataBundle sabr = (SABRInterestRateDataBundle) curves;
      if (sabr.getSABRParameter() instanceof SABRInterestRateExtrapolationParameters) {
        final SABRInterestRateExtrapolationParameters sabrExtrapolation = (SABRInterestRateExtrapolationParameters) sabr.getSABRParameter();
        final CapFloorIborSABRExtrapolationRightMethod method = new CapFloorIborSABRExtrapolationRightMethod(sabrExtrapolation.getCutOffStrike(), sabrExtrapolation.getMu());
        return method.presentValue(cap, sabr).getAmount();
      }
    }
    throw new UnsupportedOperationException("The PresentValueSABRExtrapolationCalculator visitor visitCapFloorIbor requires a SABRInterestRateExtrapolationParameter as data.");
  }

  @Override
  public Double visitSwaptionCashFixedIbor(final SwaptionCashFixedIbor swaption, final YieldCurveBundle curves) {
    Validate.notNull(swaption);
    Validate.notNull(curves);
    if (curves instanceof SABRInterestRateDataBundle) {
      final SABRInterestRateDataBundle sabr = (SABRInterestRateDataBundle) curves;
      if (sabr.getSABRParameter() instanceof SABRInterestRateExtrapolationParameters) {
        final SABRInterestRateExtrapolationParameters sabrExtrapolation = (SABRInterestRateExtrapolationParameters) sabr.getSABRParameter();
        final SwaptionCashFixedIborSABRExtrapolationRightMethod method = new SwaptionCashFixedIborSABRExtrapolationRightMethod(sabrExtrapolation.getCutOffStrike(), sabrExtrapolation.getMu());
        return method.presentValue(swaption, sabr);
      }
    }
    throw new UnsupportedOperationException("The PresentValueSABRExtrapolationCalculator visitor visitSwaptionCashFixedIbor requires a SABRInterestRateExtrapolationParameter as data.");
  }

  @Override
  public Double visitSwaptionPhysicalFixedIbor(final SwaptionPhysicalFixedIbor swaption, final YieldCurveBundle curves) {
    Validate.notNull(swaption);
    Validate.notNull(curves);
    if (curves instanceof SABRInterestRateDataBundle) {
      final SABRInterestRateDataBundle sabr = (SABRInterestRateDataBundle) curves;
      if (sabr.getSABRParameter() instanceof SABRInterestRateExtrapolationParameters) {
        final SABRInterestRateExtrapolationParameters sabrExtrapolation = (SABRInterestRateExtrapolationParameters) sabr.getSABRParameter();
        final SwaptionPhysicalFixedIborSABRExtrapolationRightMethod method = new SwaptionPhysicalFixedIborSABRExtrapolationRightMethod(sabrExtrapolation.getCutOffStrike(), sabrExtrapolation.getMu());
        return method.presentValue(swaption, sabr);
      }
    }
    throw new UnsupportedOperationException("The PresentValueSABRExtrapolationCalculator visitor visitSwaptionPhysicalFixedIbor requires a SABRInterestRateExtrapolationParameter as data.");
  }

  @Override
  public Double visitCouponCMS(final CouponCMS payment, final YieldCurveBundle curves) {
    Validate.notNull(curves);
    Validate.notNull(payment);
    if (curves instanceof SABRInterestRateDataBundle) {
      final SABRInterestRateDataBundle sabr = (SABRInterestRateDataBundle) curves;
      if (sabr.getSABRParameter() instanceof SABRInterestRateExtrapolationParameters) {
        final SABRInterestRateExtrapolationParameters sabrExtrapolation = (SABRInterestRateExtrapolationParameters) sabr.getSABRParameter();
        final CouponCMSSABRExtrapolationRightReplicationMethod replication = new CouponCMSSABRExtrapolationRightReplicationMethod(sabrExtrapolation.getCutOffStrike(), sabrExtrapolation.getMu());
        return replication.presentValue(payment, sabr);
      }
    }
    throw new UnsupportedOperationException("The PresentValueSABRExtrapolationCalculator visitor visitCouponCMS requires a SABRInterestRateExtrapolationParameter as data.");
  }

  @Override
  public Double visitCapFloorCMS(final CapFloorCMS payment, final YieldCurveBundle curves) {
    Validate.notNull(curves);
    Validate.notNull(payment);
    if (curves instanceof SABRInterestRateDataBundle) {
      final SABRInterestRateDataBundle sabr = (SABRInterestRateDataBundle) curves;
      if (sabr.getSABRParameter() instanceof SABRInterestRateExtrapolationParameters) {
        final SABRInterestRateExtrapolationParameters sabrExtrapolation = (SABRInterestRateExtrapolationParameters) sabr.getSABRParameter();
        final CapFloorCMSSABRExtrapolationRightReplicationMethod replication = new CapFloorCMSSABRExtrapolationRightReplicationMethod(sabrExtrapolation.getCutOffStrike(), sabrExtrapolation.getMu());
        return replication.presentValue(payment, sabr).getAmount();
      }
    }
    throw new UnsupportedOperationException("The PresentValueSABRExtrapolationCalculator visitor visitCapFloorCMS requires a SABRInterestRateExtrapolationParameter as data.");
  }

}
