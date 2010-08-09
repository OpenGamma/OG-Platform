/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.annuity.definition;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.model.interestrate.curve.ConstantYieldCurve;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.rootfinding.RealSingleRootFinder;
import com.opengamma.math.rootfinding.VanWijngaardenDekkerBrentSingleRootFinder;

/**
 * 
 */
public class ContinouslyCompoundedYieldCalculator {

  private final RealSingleRootFinder _root = new VanWijngaardenDekkerBrentSingleRootFinder();

  public double calculate(final FixedAnnuity annuity, final Double dirtyPrice) {
    Validate.notNull(annuity, "annuity");

    final String curveName = annuity.getFundingCurveName();
    if (dirtyPrice <= 0) {
      throw new IllegalArgumentException("Price must be positive");
    }

    final Function1D<Double, Double> f = new Function1D<Double, Double>() {
      private final com.opengamma.financial.interestrate.PresentValueCalculator _pvc = com.opengamma.financial.interestrate.PresentValueCalculator.getInstance();

      @Override
      public Double evaluate(final Double y) {
        YieldCurveBundle curves = new YieldCurveBundle();
        curves.setCurve(curveName, new ConstantYieldCurve(y));
        return _pvc.getValue(annuity, curves) - dirtyPrice;
      }

    };
    return _root.getRoot(f, 0., 10.);
  }

}
