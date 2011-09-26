/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate;

import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.financial.model.option.definition.SABRInterestRateParameters;
import com.opengamma.financial.model.volatility.smile.function.SABRHaganVolatilityFunction;
import com.opengamma.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.math.curve.InterpolatedDoublesCurve;
import com.opengamma.math.interpolation.GridInterpolator2D;
import com.opengamma.math.interpolation.Interpolator2D;
import com.opengamma.math.interpolation.LinearInterpolator1D;
import com.opengamma.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.math.surface.ConstantDoublesSurface;
import com.opengamma.math.surface.InterpolatedDoublesSurface;

/**
 * Sets of market data used in tests: 30-Sep-2010.
 */
public class DataSet2010Sep30 {

  /**
   * Linear interpolator. Used for SABR parameters interpolation.
   */
  private static final LinearInterpolator1D LINEAR = new LinearInterpolator1D();
  /**
   * The standard day count 30/360 used in the data set. 
   */
  private static final DayCount DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("30/360");

  /**
   * Create a yield curve bundle with three EUR curves (discounting, forward3M, forward 6M).
   * @return The yield curve bundle.
   */
  public static YieldCurveBundle createCurves() {
    final String discountingCurveName = "EUR Discounting";
    double[] timeDsc = new double[] {0.002739726, 0.010958904, 0.01369863, 0.030136986, 0.049315068, 0.095890411, 0.183561644, 0.263013699, 0.347945205, 0.424657534, 0.509589041, 0.591780822,
        0.682191781, 0.75890411, 0.843835616, 0.931506849, 1.010958904, 1.263013699, 1.512328767, 1.761643836, 2.01369863, 3.01369863, 4.019178082, 5.016438356, 6.016438356, 7.016438356, 8.016438356,
        9.016438356, 10.02191781, 11.01917808, 12.01917808, 13.01917808, 14.02191781, 15.02739726, 16.02465753, 17.02191781, 18.02465753, 19.02465753, 20.02465753, 21.03013699, 22.02739726,
        23.02739726, 24.02739726, 25.02739726, 26.03561644, 27.03287671, 28.03013699, 29.03013699, 30.03287671, 35.03561644, 40.03835616, 50.04657534, 60.05753425};
    double[] rateDsc = new double[] {0.0053, 0.0055, 0.0055, 0.0055, 0.0056, 0.0062, 0.0072, 0.008, 0.0089, 0.0092, 0.0095, 0.0098, 0.0101, 0.0103, 0.0106, 0.0108, 0.0112, 0.0117, 0.0121, 0.0126,
        0.013, 0.0148, 0.0165, 0.0183, 0.02, 0.0215, 0.0229, 0.024, 0.0251, 0.026, 0.0269, 0.0276, 0.0282, 0.0287, 0.0291, 0.0294, 0.0296, 0.0298, 0.0299, 0.0299, 0.0298, 0.0297, 0.0295, 0.0293,
        0.029, 0.0287, 0.0283, 0.028, 0.0277, 0.0265, 0.0257, 0.0254, 0.0250};
    // Curve data is zero-coupon continuously compounded.
    InterpolatedDoublesCurve curveDsc = InterpolatedDoublesCurve.from(timeDsc, rateDsc, new LinearInterpolator1D(), discountingCurveName);
    final String forwardCurve3MName = "EUR Forward 3M";
    double[] timeFwd3 = new double[] {0.002739726, 0.010958904, 0.01369863, 0.030136986, 0.049315068, 0.095890411, 0.183561644, 0.263013699, 0.347945205, 0.424657534, 0.509589041, 0.591780822,
        0.682191781, 0.75890411, 0.843835616, 0.931506849, 1.010958904, 1.263013699, 1.512328767, 1.761643836, 2.01369863, 3.01369863, 4.019178082, 5.016438356, 6.016438356, 7.016438356, 8.016438356,
        9.016438356, 10.02191781, 11.01917808, 12.01917808, 13.01917808, 14.02191781, 15.02739726, 16.02465753, 17.02191781, 18.02465753, 19.02465753, 20.02465753, 21.03013699, 22.02739726,
        23.02739726, 24.02739726, 25.02739726, 26.03561644, 27.03287671, 28.03013699, 29.03013699, 30.03287671, 35.03561644, 40.03835616, 50.04657534, 60.05753425};
    double[] rateFwd3 = new double[] {0.0075, 0.0075, 0.0075, 0.0075, 0.0075, 0.0077, 0.0084, 0.009, 0.0093, 0.0096, 0.0099, 0.0102, 0.0104, 0.0106, 0.0108, 0.011, 0.0112, 0.0117, 0.0122, 0.0126,
        0.013, 0.0148, 0.0165, 0.0183, 0.02, 0.0215, 0.0229, 0.024, 0.0251, 0.026, 0.0269, 0.0276, 0.0282, 0.0287, 0.0291, 0.0294, 0.0296, 0.0298, 0.0299, 0.0299, 0.0298, 0.0297, 0.0295, 0.0293,
        0.029, 0.0287, 0.0283, 0.028, 0.0277, 0.0265, 0.0257, 0.0254, 0.0250};
    InterpolatedDoublesCurve curveFwd3 = InterpolatedDoublesCurve.from(timeFwd3, rateFwd3, new LinearInterpolator1D(), forwardCurve3MName);
    final String forwardCurve6MName = "EUR Forward 6M";
    double[] timeFwd6 = new double[] {0.002739726, 0.010958904, 0.01369863, 0.030136986, 0.049315068, 0.095890411, 0.183561644, 0.263013699, 0.347945205, 0.424657534, 0.509589041, 0.591780822,
        0.682191781, 0.75890411, 0.843835616, 0.931506849, 1.010958904, 1.263013699, 1.512328767, 1.761643836, 2.01369863, 3.01369863, 4.019178082, 5.016438356, 6.016438356, 7.016438356, 8.016438356,
        9.016438356, 10.02191781, 11.01917808, 12.01917808, 13.01917808, 14.02191781, 15.02739726, 16.02465753, 17.02191781, 18.02465753, 19.02465753, 20.02465753, 21.03013699, 22.02739726,
        23.02739726, 24.02739726, 25.02739726, 26.03561644, 27.03287671, 28.03013699, 29.03013699, 30.03287671, 35.03561644, 40.03835616, 50.04657534, 60.05753425};
    double[] rateFwd6 = new double[] {0.0075, 0.0075, 0.0075, 0.0075, 0.0075, 0.0080, 0.0090, 0.0105, 0.0109, 0.0112, 0.0116, 0.0118, 0.012, 0.0122, 0.0125, 0.0127, 0.0129, 0.0133, 0.0138, 0.0142,
        0.0146, 0.0163, 0.018, 0.0197, 0.0213, 0.0228, 0.0241, 0.0252, 0.0261, 0.027, 0.0278, 0.0285, 0.029, 0.0295, 0.0298, 0.0301, 0.0303, 0.0304, 0.0305, 0.0305, 0.0304, 0.0302, 0.03, 0.0298,
        0.0295, 0.0291, 0.0288, 0.0285, 0.0282, 0.0269, 0.026, 0.0256, 0.0250};
    InterpolatedDoublesCurve curveFwd6 = InterpolatedDoublesCurve.from(timeFwd6, rateFwd6, new LinearInterpolator1D(), forwardCurve6MName);
    final YieldCurveBundle curves = new YieldCurveBundle();
    curves.setCurve(discountingCurveName, new YieldCurve(curveDsc));
    curves.setCurve(forwardCurve3MName, new YieldCurve(curveFwd3));
    curves.setCurve(forwardCurve6MName, new YieldCurve(curveFwd6));
    return curves;
  }

  public static SABRInterestRateParameters createSABR() {
    Interpolator2D<? extends Interpolator1DDataBundle> interpolator = new GridInterpolator2D<Interpolator1DDataBundle, Interpolator1DDataBundle>(LINEAR, LINEAR);

    double[] expiryAlpha = new double[] {0.07945205479452055, 0.16712328767123288, 0.25205479452054796, 0.4986301369863014, 0.747945205479452, 1.0, 1.497963919455049, 1.9952316790178906, 3.0, 4.0,
        5.0, 6.0006961598922075, 6.997260273972603, 7.994520547945205, 9.0, 10.000696159892208, 12.0, 15.0, 20.0, 24.994520547945207, 29.99523167901789};
    double[] tenorAlpha = new double[] {1.0, 2.0, 3.0, 4.0, 5.0, 7.0, 10.0, 15.0, 20.0, 25.0, 30.0};
    double[] alpha = new double[] {0.0421, 0.0451, 0.0461, 0.0475, 0.049, 0.0515, 0.0561, 0.056, 0.057, 0.0594, 0.0624, 0.0443, 0.048, 0.0485, 0.0503, 0.0532, 0.0534, 0.0569, 0.0571, 0.0576, 0.0611,
        0.0643, 0.0464, 0.051, 0.0512, 0.0529, 0.0528, 0.0542, 0.0563, 0.056, 0.0565, 0.0595, 0.0635, 0.0507, 0.0545, 0.0525, 0.0524, 0.0533, 0.0526, 0.0539, 0.054, 0.0557, 0.0573, 0.0605, 0.0557,
        0.0551, 0.0541, 0.0527, 0.0534, 0.0524, 0.053, 0.0528, 0.0538, 0.0564, 0.0592, 0.0568, 0.0557, 0.0543, 0.0524, 0.0519, 0.0516, 0.052, 0.0517, 0.0526, 0.0541, 0.0569, 0.0608, 0.0559, 0.0537,
        0.053, 0.0521, 0.0506, 0.0509, 0.0501, 0.051, 0.0533, 0.0547, 0.062, 0.0555, 0.0529, 0.0515, 0.051, 0.05, 0.0493, 0.0488, 0.0496, 0.0513, 0.0535, 0.0572, 0.0519, 0.0493, 0.0481, 0.0474,
        0.0471, 0.0472, 0.0457, 0.0464, 0.049, 0.0504, 0.0518, 0.0471, 0.0457, 0.0445, 0.0438, 0.0435, 0.0439, 0.0426, 0.0431, 0.0449, 0.0472, 0.0466, 0.0438, 0.0422, 0.0413, 0.0409, 0.0412, 0.0412,
        0.041, 0.0415, 0.042, 0.0443, 0.0424, 0.0407, 0.0405, 0.039, 0.0395, 0.0393, 0.0398, 0.0385, 0.0394, 0.0409, 0.0426, 0.0391, 0.0381, 0.0377, 0.0368, 0.0364, 0.0367, 0.0371, 0.0373, 0.0381,
        0.039, 0.0408, 0.0374, 0.0373, 0.036, 0.036, 0.0358, 0.0363, 0.0365, 0.0363, 0.0374, 0.038, 0.0394, 0.0367, 0.0351, 0.0351, 0.0346, 0.0347, 0.0347, 0.0358, 0.0362, 0.0362, 0.0373, 0.0394,
        0.0348, 0.0342, 0.0339, 0.0335, 0.0337, 0.0343, 0.0349, 0.0355, 0.0366, 0.0368, 0.0386, 0.0342, 0.0336, 0.0341, 0.0334, 0.0336, 0.0345, 0.0351, 0.035, 0.0367, 0.0369, 0.0377, 0.0328, 0.0336,
        0.0337, 0.0334, 0.0339, 0.0346, 0.036, 0.0361, 0.0371, 0.037, 0.038, 0.0343, 0.0352, 0.0365, 0.0375, 0.0382, 0.0393, 0.0412, 0.04, 0.0391, 0.038, 0.0373, 0.0393, 0.0399, 0.0407, 0.0419,
        0.043, 0.0432, 0.0435, 0.0397, 0.0369, 0.0356, 0.0356, 0.0393, 0.0382, 0.0383, 0.0385, 0.039, 0.0392, 0.0391, 0.0345, 0.0337, 0.0337, 0.034};
    final InterpolatedDoublesSurface alphaSurface = InterpolatedDoublesSurface.fromGrid(expiryAlpha, tenorAlpha, alpha, interpolator);
    final VolatilitySurface alphaVolatility = new VolatilitySurface(alphaSurface);

    double beta = 0.50;
    ConstantDoublesSurface betaSurface = ConstantDoublesSurface.from(beta);
    final VolatilitySurface betaVolatility = new VolatilitySurface(betaSurface);

    double[] expiryRho = new double[] {0.07945205479452055, 0.16712328767123288, 0.25205479452054796, 0.4986301369863014, 0.747945205479452, 1.0, 1.497963919455049, 1.9952316790178906, 3.0, 4.0, 5.0,
        6.997260273972603, 10.000696159892208, 15.0, 20.0, 24.994520547945207, 29.99523167901789};
    double[] tenorRho = new double[] {1.0, 2.0, 5.0, 10.0, 15.0, 20.0, 30.0};
    double[] rho = new double[] {0.56, 0.46, -0.24, -0.4, -0.42, -0.46, -0.54, 0.56, 0.46, -0.25, -0.39, -0.42, -0.45, -0.55, 0.56, 0.46, -0.25, -0.38, -0.42, -0.44, -0.54, 0.56, 0.46, -0.25, -0.38,
        -0.42, -0.43, -0.53, 0.56, 0.45, -0.25, -0.38, -0.4, -0.42, -0.52, 0.56, 0.19, -0.13, -0.24, -0.28, -0.33, -0.42, 0.41, 0.19, -0.13, -0.23, -0.27, -0.31, -0.4, 0.36, 0.19, -0.13, -0.22,
        -0.26, -0.3, -0.38, 0.31, 0.1, -0.13, -0.21, -0.25, -0.29, -0.36, 0.25, 0, -0.14, -0.2, -0.24, -0.28, -0.34, 0.12, -0.1, -0.15, -0.2, -0.24, -0.25, -0.29, -0.1, -0.12, -0.17, -0.21, -0.23,
        -0.25, -0.28, -0.16, -0.2, -0.21, -0.22, -0.24, -0.25, -0.28, -0.18, -0.21, -0.24, -0.26, -0.27, -0.27, -0.29, -0.21, -0.21, -0.26, -0.3, -0.3, -0.3, -0.3, -0.26, -0.21, -0.24, -0.3, -0.3,
        -0.3, -0.32, -0.28, -0.2, -0.22, -0.3, -0.3, -0.3, -0.34};
    final InterpolatedDoublesSurface rhoSurface = InterpolatedDoublesSurface.fromGrid(expiryRho, tenorRho, rho, interpolator);
    final VolatilitySurface rhoVolatility = new VolatilitySurface(rhoSurface);

    double[] nu = new double[] {0.43, 0.56, 0.8, 0.64, 0.85, 0.9, 0.9, 0.43, 0.56, 0.77, 0.63, 0.8, 0.85, 0.85, 0.43, 0.56, 0.74, 0.62, 0.68, 0.7, 0.8, 0.43, 0.56, 0.71, 0.6, 0.63, 0.65, 0.7, 0.43,
        0.56, 0.6, 0.57, 0.59, 0.6, 0.63, 0.43, 0.55, 0.55, 0.55, 0.55, 0.55, 0.55, 0.43, 0.5, 0.53, 0.53, 0.53, 0.54, 0.54, 0.41, 0.47, 0.52, 0.51, 0.52, 0.53, 0.53, 0.41, 0.46, 0.5, 0.49, 0.51,
        0.52, 0.52, 0.4, 0.45, 0.48, 0.47, 0.5, 0.51, 0.51, 0.4, 0.44, 0.45, 0.47, 0.48, 0.49, 0.5, 0.4, 0.42, 0.438, 0.45, 0.45, 0.44, 0.44, 0.38, 0.38, 0.42, 0.42, 0.4, 0.37, 0.35, 0.34, 0.3,
        0.325, 0.325, 0.3, 0.31, 0.3, 0.28, 0.23, 0.23, 0.23, 0.23, 0.24, 0.24, 0.26, 0.23, 0.23, 0.23, 0.23, 0.24, 0.24, 0.24, 0.23, 0.23, 0.23, 0.23, 0.23, 0.24};
    final InterpolatedDoublesSurface nuSurface = InterpolatedDoublesSurface.fromGrid(expiryRho, tenorRho, nu, interpolator);
    final VolatilitySurface nuVolatility = new VolatilitySurface(nuSurface);

    return new SABRInterestRateParameters(alphaVolatility, betaVolatility, rhoVolatility, nuVolatility, DAY_COUNT, new SABRHaganVolatilityFunction());
  }

}
