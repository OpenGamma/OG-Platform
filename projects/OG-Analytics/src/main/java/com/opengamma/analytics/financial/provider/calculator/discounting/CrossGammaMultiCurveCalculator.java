/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.discounting;

import java.util.HashMap;
import java.util.Set;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.parameter.ParameterSensitivityParameterCalculator;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Computes the cross-gamma to the curve parameters for multi-curve with all the curves in the same currency and instruments.
 * The curves should be represented by a YieldCurve with an InterpolatedDoublesCurve on the zero-coupon rates.
 * By default the gamma is computed using a one basis-point shift. This default can be change in a constructor.
 * The results themselves are not scaled (the represent the second order derivative).
 * Note that currently, the calculator will work only if the same curve is not used for two indexes (REQS-427).
 * <p> Reference: Interest Rate Cross-gamma for Single and Multiple Curves. OpenGamma quantitative research 15, July 14
 */
public class CrossGammaMultiCurveCalculator {

  /** Default size of bump: 1 basis point. */
  private static final double BP1 = 1.0E-4;

  /** The sensitivity calculator to the curve parameters used for the delta computation */
  private final ParameterSensitivityParameterCalculator<ParameterProviderInterface> _psc;
  /** The shift used for finite difference Gamma using two deltas. */
  private final double _shift;

  /**
   * Constructor.
   * @param shift The shift used for finite difference Gamma using two deltas. Shift be larger in absolute value than 1.0E-10.
   * @param curveSensitivityCalculator The delta (curve sensitivity) calculator.
   */
  public CrossGammaMultiCurveCalculator(final double shift,
      final InstrumentDerivativeVisitor<ParameterProviderInterface, MultipleCurrencyMulticurveSensitivity> curveSensitivityCalculator) {
    ArgumentChecker.notNull(curveSensitivityCalculator, "sensitivity calculator");
    ArgumentChecker.isTrue(Math.abs(shift) > 1.0E-10, "shift should not be larger in absolute value than 1.0E-10");
    _psc = new ParameterSensitivityParameterCalculator<>(curveSensitivityCalculator);
    _shift = shift;
  }

  /**
   * Constructor.
   * The finite difference nump used is the default bump (1 basis point = 1.0E-4).
   * @param curveSensitivityCalculator The delta (curve sensitivity) calculator.
   */
  public CrossGammaMultiCurveCalculator(final InstrumentDerivativeVisitor<ParameterProviderInterface, MultipleCurrencyMulticurveSensitivity> curveSensitivityCalculator) {
    ArgumentChecker.notNull(curveSensitivityCalculator, "sensitivity calculator");
    _psc = new ParameterSensitivityParameterCalculator<>(curveSensitivityCalculator);
    _shift = BP1;
  }

  /**
   * Computes the gamma matrix for a given instrument. 
   * The curve provider should contain multi-curve in one currency which should each be of the 
   * type YieldCurve with an underlying InterpolatedDoublesCurve.
   * @param instrument The instrument for which the cross-gamma should be computed. Same currency as the curves.
   * @param multicurve The multi-curve provider.
   * @return The cross-gamma matrices as map from curve name to matrix. One matrix is provided for each curve. It represents the intra-curve cross-gammas.
   */
  public HashMap<String, DoubleMatrix2D> calculateCrossGammaIntraCurve(final InstrumentDerivative instrument, final MulticurveProviderDiscount multicurve) {
    ArgumentChecker.notNull(instrument, "instrument");
    ArgumentChecker.notNull(multicurve, "multi-curve provider");
    Set<String> names = multicurve.getAllNames();
    int nbCurve = names.size();
    String[] namesArray = names.toArray(new String[nbCurve]);
    Currency ccy = checkUniqueCurrency(multicurve);
    InterpolatedDoublesCurve[] interpolatedCurves = interpolatedCurves(multicurve, nbCurve, namesArray);
    // Curves description
    double[][] y = new double[nbCurve][];
    double[][] x = new double[nbCurve][];
    int[] nbNode = new int[nbCurve];
    for (int loopcurve = 0; loopcurve < nbCurve; loopcurve++) {
      y[loopcurve] = interpolatedCurves[loopcurve].getYDataAsPrimitive();
      x[loopcurve] = interpolatedCurves[loopcurve].getXDataAsPrimitive();
      nbNode[loopcurve] = x[loopcurve].length;
    }
    // Initial sensitivity
    MultipleCurrencyParameterSensitivity ps0 = _psc.calculateSensitivity(instrument, multicurve);
    double[][] ps0Array = sensitivitiesAsArrayOfMatrix(nbCurve, namesArray, ccy, nbNode, ps0);
    // Bump and recompute for each curve and each point
    HashMap<String, DoubleMatrix2D> result = new HashMap<>();
    for (int loopcurve = 0; loopcurve < nbCurve; loopcurve++) {
      MultipleCurrencyParameterSensitivity[] psShift = new MultipleCurrencyParameterSensitivity[nbNode[loopcurve]];
      double[][] gammaArray = new double[nbNode[loopcurve]][nbNode[loopcurve]];
      for (int loopnode = 0; loopnode < nbNode[loopcurve]; loopnode++) {
        final double[] yieldBumped = y[loopcurve].clone();
        yieldBumped[loopnode] += _shift;
        MulticurveProviderDiscount multicurveBumped = bumpedProvider(multicurve, namesArray[loopcurve],
            interpolatedCurves[loopcurve].getInterpolator(), x[loopcurve], yieldBumped);
        psShift[loopnode] = _psc.calculateSensitivity(instrument, multicurveBumped);
        DoubleMatrix1D sensiCurve = psShift[loopnode].getSensitivity(namesArray[loopcurve], ccy);
        double[] psShiftArray;
        if (sensiCurve != null) {
          psShiftArray = sensiCurve.getData();
        } else {
          psShiftArray = new double[nbNode[loopcurve]];
        }
        for (int loopnode2 = 0; loopnode2 < nbNode[loopcurve]; loopnode2++) {
          gammaArray[loopnode][loopnode2] = (psShiftArray[loopnode2] - ps0Array[loopcurve][loopnode2]) / _shift;
        }
      }
      DoubleMatrix2D gammaMat = new DoubleMatrix2D(gammaArray);
      result.put(namesArray[loopcurve], gammaMat);
    }
    return result;
  }

  /**
   * Computes the cross-gamma matrix for a given instrument. 
   * The curve provider should contain multi-curve in one currency which should each be of the 
   * type YieldCurve with an underlying InterpolatedDoublesCurve.
   * @param instrument The instrument for which the cross-gamma should be computed. Same currency as the curves.
   * @param multicurve The multi-curve provider.
   * @return The cross-gamma matrix. It represents the cross-curve cross-gamma. 
   * The order of the curve is the one provided by the  {@link MulticurveProviderDiscount#getAllNames()}.
   */
  public DoubleMatrix2D calculateCrossGammaCrossCurve(final InstrumentDerivative instrument, final MulticurveProviderDiscount multicurve) {
    ArgumentChecker.notNull(instrument, "instrument");
    ArgumentChecker.notNull(multicurve, "multi-curve provider");
    Set<String> names = multicurve.getAllNames();
    int nbCurve = names.size();
    String[] namesArray = names.toArray(new String[nbCurve]);
    Currency ccy = checkUniqueCurrency(multicurve);
    InterpolatedDoublesCurve[] interpolatedCurves = interpolatedCurves(multicurve, nbCurve, namesArray);
    // Curves description
    double[][] y = new double[nbCurve][];
    double[][] x = new double[nbCurve][];
    int[] nbNodeByCurve = new int[nbCurve];
    int nbNodeTotal = 0;
    for (int loopcurve = 0; loopcurve < nbCurve; loopcurve++) {
      y[loopcurve] = interpolatedCurves[loopcurve].getYDataAsPrimitive();
      x[loopcurve] = interpolatedCurves[loopcurve].getXDataAsPrimitive();
      nbNodeByCurve[loopcurve] = x[loopcurve].length;
      nbNodeTotal += nbNodeByCurve[loopcurve];
    }
    // Initial sensitivity
    MultipleCurrencyParameterSensitivity ps0 = _psc.calculateSensitivity(instrument, multicurve);
    double[][] ps0Array = sensitivitiesAsArrayOfMatrix(nbCurve, namesArray, ccy, nbNodeByCurve, ps0);
    // Bump and recompute for each curve and each point
    double[][] gammaArray = new double[nbNodeTotal][nbNodeTotal];
    int loopnodetotal = 0;
    for (int loopcurve = 0; loopcurve < nbCurve; loopcurve++) {
      MultipleCurrencyParameterSensitivity[] psShift = new MultipleCurrencyParameterSensitivity[nbNodeByCurve[loopcurve]];
      for (int loopnode = 0; loopnode < nbNodeByCurve[loopcurve]; loopnode++) {
        final double[] yieldBumped = y[loopcurve].clone();
        yieldBumped[loopnode] += _shift;
        MulticurveProviderDiscount multicurveBumped = bumpedProvider(multicurve, namesArray[loopcurve],
            interpolatedCurves[loopcurve].getInterpolator(), x[loopcurve], yieldBumped);
        psShift[loopnode] = _psc.calculateSensitivity(instrument, multicurveBumped);
        int loopnodetotal2 = 0;
        for (int loopcurve2 = 0; loopcurve2 < nbCurve; loopcurve2++) {
          DoubleMatrix1D sensiCurve = psShift[loopnode].getSensitivity(namesArray[loopcurve2], ccy);
          double[] psShiftArray = new double[nbNodeByCurve[loopcurve2]];
          if (sensiCurve != null) {
            psShiftArray = sensiCurve.getData();
          }
          for (int loopnode2 = 0; loopnode2 < nbNodeByCurve[loopcurve2]; loopnode2++) {
            gammaArray[loopnodetotal][loopnodetotal2] = (psShiftArray[loopnode2] - ps0Array[loopcurve2][loopnode2]) / _shift;
            loopnodetotal2++;
          }
        }
        loopnodetotal++;
      }
    }
    return new DoubleMatrix2D(gammaArray);
  }

  /**
   * Transform the sensitivities as a sensitivity into a array of array to be used in the computations.
   * @param nbCurve The number of curves.
   * @param names The curves names.
   * @param ccy The currency of the curves.
   * @param nbNodeByCurve The number of node for each curve.
   * @param ps The sensitivity.
   * @return The array. When no sensitivity is present an array of zeros is returned.
   */
  private double[][] sensitivitiesAsArrayOfMatrix(int nbCurve, String[] names, Currency ccy, int[] nbNodeByCurve, MultipleCurrencyParameterSensitivity ps) {
    DoubleMatrix1D[] ps0Mat = new DoubleMatrix1D[nbCurve];
    double[][] ps0Array = new double[nbCurve][];
    for (int loopcurve = 0; loopcurve < nbCurve; loopcurve++) {
      ps0Mat[loopcurve] = ps.getSensitivity(names[loopcurve], ccy);
      if (ps0Mat[loopcurve] == null) {
        ps0Array[loopcurve] = new double[nbNodeByCurve[loopcurve]];
      } else {
        ps0Array[loopcurve] = ps0Mat[loopcurve].getData();
      }
    }
    return ps0Array;
  }

  /**
   * Check that all the discounting and forward curves in the provider are related to the same currency.
   * @param multicurve The multi-curve provider.
   * @return The unique currency of the provider.
   */
  private Currency checkUniqueCurrency(final MulticurveProviderDiscount multicurve) {
    Set<Currency> ccys = multicurve.getCurrencies();
    ArgumentChecker.isTrue(ccys.size() == 1, "only one currency allowed for multi-curve gamma");
    Currency ccy = ccys.iterator().next();
    Set<IborIndex> iborIndexes = multicurve.getIndexesIbor();
    for (IborIndex index : iborIndexes) {
      ArgumentChecker.isTrue(index.getCurrency().equals(ccy), "Ibor index should be in the same currency as discounting curve");
    }
    Set<IndexON> onIndexes = multicurve.getIndexesON();
    for (IndexON index : onIndexes) {
      ArgumentChecker.isTrue(index.getCurrency().equals(ccy), "Overnight index should be in the same currency as discounting curve");
    }
    return ccy;
  }

  /**
   * Check that all the curves are of the type YieldCurve backed by an InterpolatedDoublesCurve.
   * @param multicurve The multi-curve provider.
   * @param nbCurve The number of curves.
   * @param names The name of the curves.
   * @return The array of interpolated curves.
   */
  private InterpolatedDoublesCurve[] interpolatedCurves(final MulticurveProviderDiscount multicurve, int nbCurve, String[] names) {
    InterpolatedDoublesCurve[] interpolatedCurves = new InterpolatedDoublesCurve[nbCurve];
    for (int loopcurve = 0; loopcurve < nbCurve; loopcurve++) {
      YieldAndDiscountCurve curve = multicurve.getCurve(names[loopcurve]);
      ArgumentChecker.isTrue(curve instanceof YieldCurve, "curve should be YieldCurve");
      YieldCurve yieldCurve = (YieldCurve) curve;
      ArgumentChecker.isTrue(yieldCurve.getCurve() instanceof InterpolatedDoublesCurve, "Yield curve should be based on InterpolatedDoublesCurve");
      interpolatedCurves[loopcurve] = (InterpolatedDoublesCurve) yieldCurve.getCurve();
    }
    return interpolatedCurves;
  }

  /**
   * Bump the interpolated curve of a provider.
   * @param multicurve The current multi-curve. 
   * @param name The name of the curve to bump.
   * @param interpolator The interpolator associated to the curve to bump.
   * @param x The nodes of the interpolated curve.
   * @param yieldBumped The yield after the bump.
   * @return The bump curve provider.
   */
  private MulticurveProviderDiscount bumpedProvider(final MulticurveProviderDiscount multicurve, String name, Interpolator1D interpolator,
      double[] x, final double[] yieldBumped) {
    final YieldAndDiscountCurve curveBumped = new YieldCurve(name,
        new InterpolatedDoublesCurve(x, yieldBumped, interpolator, true));
    MulticurveProviderDiscount multicurveBumped = new MulticurveProviderDiscount();
    multicurveBumped.setForexMatrix(multicurve.getFxRates());
    for (Currency loopccy : multicurve.getCurrencies()) {
      if (loopccy.equals(multicurve.getCurrencyForName(name))) {
        multicurveBumped.setCurve(loopccy, curveBumped);
      } else {
        multicurveBumped.setCurve(loopccy, multicurve.getCurve(loopccy));
      }
    }
    for (IborIndex loopibor : multicurve.getIndexesIbor()) {
      if (loopibor.equals(multicurve.getIborIndexForName(name))) { // REQS-427
        multicurveBumped.setCurve(loopibor, curveBumped);
      } else {
        multicurveBumped.setCurve(loopibor, multicurve.getCurve(loopibor));
      }
    }
    for (IndexON loopon : multicurve.getIndexesON()) {
      if (loopon.equals(multicurve.getOvernightIndexForName(name))) { // REQS-427
        multicurveBumped.setCurve(loopon, curveBumped);
      } else {
        multicurveBumped.setCurve(loopon, multicurve.getCurve(loopon));
      }
    }
    return multicurveBumped;
  }

}
