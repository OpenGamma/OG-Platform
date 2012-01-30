/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.swaption.derivative.SwaptionCashFixedIbor;
import com.opengamma.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.financial.interestrate.swaption.method.SwaptionCashFixedIborBlackMethod;
import com.opengamma.financial.interestrate.swaption.method.SwaptionPhysicalFixedIborBlackMethod;
import com.opengamma.financial.model.option.definition.YieldCurveWithBlackSwaptionBundle;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Present value curve sensitivity calculator for interest rate instruments using SABR volatility formula.
 */
public final class PresentValueCurveSensitivityBlackSwaptionCalculator extends PresentValueCurveSensitivityCalculator {

  /**
   * The instance of the calculator.
   */
  private static final PresentValueCurveSensitivityBlackSwaptionCalculator s_instance = new PresentValueCurveSensitivityBlackSwaptionCalculator();

  /**
   * Return the instance of the calculator.
   * @return The calculator.
   */
  public static PresentValueCurveSensitivityBlackSwaptionCalculator getInstance() {
    return s_instance;
  }

  /**
   * Private constructor.
   */
  private PresentValueCurveSensitivityBlackSwaptionCalculator() {
  }

  /**
   * Methods.
   */
  private static final SwaptionPhysicalFixedIborBlackMethod METHOD_SWAPTION_PHYSICAL = SwaptionPhysicalFixedIborBlackMethod.getInstance();
  private static final SwaptionCashFixedIborBlackMethod METHOD_SWAPTION_CASH = SwaptionCashFixedIborBlackMethod.getInstance();

  @Override
  public Map<String, List<DoublesPair>> visitSwaptionCashFixedIbor(final SwaptionCashFixedIbor swaption, final YieldCurveBundle curves) {
    Validate.notNull(swaption);
    Validate.notNull(curves);
    if (curves instanceof YieldCurveWithBlackSwaptionBundle) {
      final YieldCurveWithBlackSwaptionBundle curvesBlack = (YieldCurveWithBlackSwaptionBundle) curves;
      return METHOD_SWAPTION_CASH.presentValueCurveSensitivity(swaption, curvesBlack).getSensitivities();
    }
    throw new UnsupportedOperationException("The PresentValueCurveSensitivityBlackSwaptionCalculator visitor visitSwaptionCashFixedIbor requires a YieldCurveWithBlackSwaptionBundle as data.");
  }

  @Override
  public Map<String, List<DoublesPair>> visitSwaptionPhysicalFixedIbor(final SwaptionPhysicalFixedIbor swaption, final YieldCurveBundle curves) {
    Validate.notNull(swaption);
    Validate.notNull(curves);
    if (curves instanceof YieldCurveWithBlackSwaptionBundle) {
      final YieldCurveWithBlackSwaptionBundle curvesBlack = (YieldCurveWithBlackSwaptionBundle) curves;
      return METHOD_SWAPTION_PHYSICAL.presentValueCurveSensitivity(swaption, curvesBlack).getSensitivities();
    }
    throw new UnsupportedOperationException("The PresentValueCurveSensitivityBlackSwaptionCalculator visitor visitSwaptionPhysicalFixedIbor requires a YieldCurveWithBlackSwaptionBundle as data.");
  }

}
