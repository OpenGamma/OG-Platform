/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.analytic.formula;

import org.apache.activemq.util.BitArray;
import org.apache.commons.lang.Validate;

import com.opengamma.math.function.ParameterizedFunction;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.math.minimization.DoubleRangeLimitTransform;
import com.opengamma.math.minimization.ParameterLimitsTransform;
import com.opengamma.math.minimization.SingleRangeLimitTransform;
import com.opengamma.math.statistics.leastsquare.LeastSquareResults;
import com.opengamma.math.statistics.leastsquare.NonLinearLeastSquare;

/**
 * 
 */
public class SABRFitter {

  private static final NonLinearLeastSquare SOLVER = new NonLinearLeastSquare();

  private static final int N_PARAMETERS = 4;
  private final SABRFormula _formula;

  private static final ParameterLimitsTransform[] TRANSFORMS;

  static {
    TRANSFORMS = new ParameterLimitsTransform[4];
    TRANSFORMS[0] = new SingleRangeLimitTransform(0, true); // alpha > 0
    TRANSFORMS[1] = new DoubleRangeLimitTransform(0, 1.0); // 0 <= beta <= 1
    TRANSFORMS[2] = new SingleRangeLimitTransform(0, true); // nu > 0
    TRANSFORMS[3] = new DoubleRangeLimitTransform(-1, 1); // -1 <= rho <= 1
  }

  public SABRFitter(SABRFormula formula) {
    _formula = formula;
  }

  public LeastSquareResults solve(final double forward, final double maturity, final double[] strikes, final double[] blackVols, final double[] errors, final double[] initialValues,
      final BitArray fixed) {

    int n = strikes.length;
    Validate.isTrue(n == blackVols.length, "strikes and vols must be same length");

    final TransformParameters transforms = new TransformParameters(new DoubleMatrix1D(initialValues), TRANSFORMS, fixed);

    final ParameterizedFunction<Double, DoubleMatrix1D, Double> function = new ParameterizedFunction<Double, DoubleMatrix1D, Double>() {
      @SuppressWarnings("synthetic-access")
      @Override
      public Double evaluate(Double strike, DoubleMatrix1D fp) {
        DoubleMatrix1D mp = transforms.inverseTransform(fp);
        return _formula.impliedVolitility(forward, mp.getEntry(0), mp.getEntry(1), mp.getEntry(2), mp.getEntry(3), strike, maturity);
      }
    };

    DoubleMatrix1D fp = transforms.transform(new DoubleMatrix1D(initialValues));
    LeastSquareResults lsRes = SOLVER.solve(new DoubleMatrix1D(strikes), new DoubleMatrix1D(blackVols), new DoubleMatrix1D(errors), function, fp);
    DoubleMatrix1D res = transforms.inverseTransform(lsRes.getParameters());

    return new LeastSquareResults(lsRes.getChiSq(), res, new DoubleMatrix2D(new double[N_PARAMETERS][N_PARAMETERS]));
  }

  private class TransformParameters {
    private final DoubleMatrix1D _startValues;
    private final ParameterLimitsTransform[] _transforms;
    private final BitArray _fixed;
    private final int _nMP;
    private final int _nFP;

    public TransformParameters(final DoubleMatrix1D startValues, final ParameterLimitsTransform[] transforms, final BitArray fixed) {
      Validate.notNull(startValues, "null start values");
      Validate.notEmpty(transforms, "must specify transforms");
      Validate.notNull(fixed, "must specify what is fixed (even if none)");
      _nMP = startValues.getNumberOfElements();
      Validate.isTrue(_nMP == transforms.length, "must give a transform for each model parameter");

      int count = 0;
      for (int i = 0; i < _nMP; i++) {
        if (fixed.get(i)) {
          count++;
        }
      }
      Validate.isTrue(count < _nMP, "all parameters are fixed");
      _nFP = _nMP - count;
      _startValues = startValues;
      _transforms = transforms;
      _fixed = fixed;
    }

    public int getNumModelParameters() {
      return _nMP;
    }

    public int getNumFittingParameters() {
      return _nFP;
    }

    public DoubleMatrix1D transform(final DoubleMatrix1D modelParameter) {
      Validate.isTrue(modelParameter.getNumberOfElements() == _nMP, "modelParameter wrong dimension");
      double[] fittingParameter = new double[_nFP];
      for (int i = 0, j = 0; i < _nMP; i++) {
        if (!_fixed.get(i)) {
          fittingParameter[j] = _transforms[i].transform(modelParameter.getEntry(i));
          j++;
        }
      }
      return new DoubleMatrix1D(fittingParameter);
    }

    public DoubleMatrix1D inverseTransform(final DoubleMatrix1D fittingParameter) {
      Validate.isTrue(fittingParameter.getNumberOfElements() == _nFP, "fititngParameter wrong dimension");
      double[] modelParameter = new double[_nMP];
      for (int i = 0, j = 0; i < _nMP; i++) {
        if (_fixed.get(i)) {
          modelParameter[i] = _startValues.getEntry(i);
        } else {
          modelParameter[i] = _transforms[i].inverseTrasfrom(fittingParameter.getEntry(j));
          j++;
        }
      }
      return new DoubleMatrix1D(modelParameter);
    }

  }

}
