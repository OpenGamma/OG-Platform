/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.capletstripping;

import java.util.Arrays;
import java.util.List;

import org.testng.annotations.Test;

import cern.jet.random.engine.MersenneTwister;
import cern.jet.random.engine.MersenneTwister64;
import cern.jet.random.engine.RandomEngine;

import com.opengamma.analytics.financial.model.volatility.discrete.DiscreteVolatilityFunctionProvider;
import com.opengamma.analytics.financial.model.volatility.discrete.DiscreteVolatilityFunctionProviderFromInterpolatedTermStructure;
import com.opengamma.analytics.math.differentiation.VectorFieldFirstOrderDifferentiator;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.analytics.math.interpolation.TransformedInterpolator1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.math.minimization.ParameterLimitsTransform;
import com.opengamma.analytics.math.minimization.ParameterLimitsTransform.LimitType;
import com.opengamma.analytics.math.minimization.SingleRangeLimitTransform;
import com.opengamma.util.test.TestGroup;

/**
 * 
 */
@Test(groups = TestGroup.UNIT)
public class CapletStripperInterpolatedTermStructureTest extends CapletStrippingSetup {
  private static final RandomEngine RANDOM = new MersenneTwister64(MersenneTwister.DEFAULT_SEED);
  private static final VectorFieldFirstOrderDifferentiator DIFF = new VectorFieldFirstOrderDifferentiator();
  private static final String DEFAULT_INTERPOLATOR = Interpolator1DFactory.DOUBLE_QUADRATIC;
  private static final String DEFAULT_EXTRAPOLATOR = Interpolator1DFactory.LINEAR_EXTRAPOLATOR;
  private static final ParameterLimitsTransform TRANSFORM = new SingleRangeLimitTransform(0.0, LimitType.GREATER_THAN);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void badKnotsTest() {
    List<CapFloor> caps = getATMCaps();
    MultiCapFloorPricer pricer = new MultiCapFloorPricer(caps, getYieldCurves());
    int nCaps = caps.size();
    double[] knots = new double[nCaps + 1];
    for (int i = 0; i <= nCaps; i++) {
      knots[i] = i;
    }
    new CapletStripperInterpolatedTermStructure(pricer, knots);
  }

  /**
   * Test fitting the ATM Caps. This implicitly makes the assumption that the caplet volatilities have no strike
   * dependence, so caplets belonging the different caps but with the same expiry, will have the same volatility despite
   * having different strikes.
   */
  @Test
  public void atmTest() {
    MultiCapFloorPricer pricer = new MultiCapFloorPricer(getATMCaps(), getYieldCurves());
    CapletStripper stripper = new CapletStripperInterpolatedTermStructure(pricer);

    DoubleMatrix1D expFitParms = new DoubleMatrix1D(0.0750077681018984, -0.0417132310902179, 0.443334588249104, 0.166354129225537, -0.0955443232821012, -0.535813313851482, -0.81400903918072);
    double expChiSqr = 0.0;

    testStripping(stripper, getATMCapPrices(), MarketDataType.PRICE, expChiSqr, expFitParms, 1e-8, false);
  }

  /**
   * Fit caps at each absolute strike in turn
   */
  @Test
  public void singleStrikeTest() {
    double[][] expFitParms = new double[][] { {0.07519558925857502, 0.06099595006987337, 0.37887791113990626, 0.2900814932812122, 0.06960865030557412, -0.05342521479925498 },
      {0.2888421248987955, -0.0023192971048977065, -0.15913446980405835, -0.2962382393117878, -0.4773062729997372 },
      {-0.17164504785623153, 0.17570630956403765, -0.09852806432927508, -0.23506962123817332, -0.41048624505277914, -0.6301356867782051 },
      {0.3908797227276975, -0.3238072319276149, -0.5393466581064617, -0.7213032534286311 },
      {-0.7283917245109782, 0.2553434284914208, -0.2378442519094908, -0.3704381964960703, -0.536862242806576, -0.8058029253911454 },
      {0.827445095514852, -0.4198900590695616, -0.6269795252250308, -0.8875937407303798 },
      {0.9532565315179226, 0.2851870481750914, 0.23441102409602055, -0.16207126482856957, -0.652262655104053, -0.9099696638475132 },
      {0.9371181616741376, 0.34005473961956495, 0.23237903957042214, -0.19618930329921386, -0.40683572799405837, -0.6596470520871216, -0.9613088609863392 },
      {0.7059889604800385, 0.45927089229020324, 0.11425056866252968, -0.36451987986750245 },
      {0.955999407622807, 0.40220172717867936, 0.19913928224094274, -0.15816695045653018, -0.4590787700861603, -0.6932984278278611, -0.9875408880086803 },
      {0.9448245261357552, 0.41772275182543434, 0.19395773066035696, -0.15365694720478604, -0.4229538051369399, -0.6989629404321959, -0.997435029791332 },
      {0.9644014702150517, 0.4533491035091301, 0.1699873426536261, -0.16922659200325504, -0.42043388566407425, -0.6892676775943389, -1.0090832784881916 },
      {0.6984521044968122, 0.5556741207844491, -0.1921052130631996, -0.39739908140320057, -0.684338560893483, -1.016715992733998 },
      {1.1191378168668937, -0.18251768035905208, -0.3962744821361997, -0.6884914142373438, -0.9972361085539848 },
      {0.5838225819129124, 0.17592675122488316, -0.16435508774233396, -0.4081223090812207, -0.6539226486174113, -0.9615281610297096 },
      {0.5857751937847466, 0.18152902949991212, -0.16919316844578738, -0.39593555754486615, -0.6440943082426583, -0.9374060706820373 },
      {1.149929104142057, -0.17606620627774683, -0.3775255884563137, -0.6097720432994848, -0.9129668308247708 },
      {1.1522779659159654, -0.17449603280382392, -0.3744748246651557, -0.6040440327679161, -0.9031355380153249 } };

    int n = getNumberOfStrikes();
    CapletStripper[] strippers = new CapletStripper[n];
    // first solve for prices
    for (int i = 0; i < n; i++) {
      MultiCapFloorPricer pricer = new MultiCapFloorPricer(getCaps(i), getYieldCurves());
      strippers[i] = new CapletStripperInterpolatedTermStructure(pricer);
      testStripping(strippers[i], getCapPrices(i), MarketDataType.PRICE, 0.0, new DoubleMatrix1D(expFitParms[i]), 1e-4, false);
    }

    // then solve for vols - there are two nested root finds here; the multi-dimensional one to find the knots values
    // and the 1D one to find the implied volatility, hence the resultant fit parameters from a root find for cap
    // volatility will differ slightly from the root find for prices - this is why the tolerance is 1e-4
    for (int i = 0; i < n; i++) {
      testStripping(strippers[i], getCapVols(i), MarketDataType.VOL, 0.0, new DoubleMatrix1D(expFitParms[i]), 1e-4, false);
    }
  }

  /**
   * Start from random initial guesses. If there is a unique solution, this should always be found, however due to the
   * finite stopping criteria, the fit parameters (i.e. the stopping point) will only match to some tolerance
   */
  @Test
  public void randomStartTest() {
    double[] vols = getCapVols(0);
    double[] prices = getCapPrices(0);
    MultiCapFloorPricer pricer = new MultiCapFloorPricer(getCaps(0), getYieldCurves()); // the 0.5% strikes
    CapletStripper stripper = new CapletStripperInterpolatedTermStructure(pricer);

    // these differ as discussed in singleStrikeTest
    DoubleMatrix1D expVolFitParms = new DoubleMatrix1D(0.0751955883987435, 0.0609959501482534, 0.378877911140902, 0.290081493287973, 0.0696086502994876, -0.0534252147847549);
    DoubleMatrix1D expPriceFitParms = new DoubleMatrix1D(0.075195589258575, 0.0609959500698733, 0.378877911139906, 0.290081493281212, 0.0696086503055741, -0.0534252147992549);

    int nKnots = vols.length;
    int nSamples = 20;
    for (int i = 0; i < nSamples; i++) {
      DoubleMatrix1D guess = new DoubleMatrix1D(nKnots);
      for (int j = 0; j < nKnots; j++) {
        guess.getData()[j] = 0.2 + 0.5 * RANDOM.nextDouble();
      }
      testStripping(stripper, vols, MarketDataType.VOL, guess, 0.0, expVolFitParms, 1e-7, false);
      testStripping(stripper, prices, MarketDataType.PRICE, guess, 0.0, expPriceFitParms, 1e-7, false);
    }
  }

  /**
   * Try to fit all cap with a volatility surface that has no strike dependence.
   * Clearly the global fit in this case will not be good.
   */
  @Test
  public void globalFitTest() {
    double[] knots = new double[] {0.25, 0.5, 1.0, 2.0, 3.0, 5.0, 7.0, 10.0 };
    MultiCapFloorPricer pricer = new MultiCapFloorPricer(getAllCaps(), getYieldCurves());
    CapletStripper stripper = new CapletStripperInterpolatedTermStructure(pricer, knots);

    double[] vols = getAllCapVols();
    double[] prices = pricer.price(vols);
    double[] vega = pricer.vega(vols);
    int n = vols.length;
    double[] errors = new double[n];
    Arrays.fill(errors, 1e-3);// 10bps
    for (int i = 0; i < n; i++) {
      vega[i] *= errors[i];
    }

    // fit to prices (weighted by vega)
    double expPriceChiSqr = 238653.409503741;
    DoubleMatrix1D expPriceFitParms = new DoubleMatrix1D(-4.15580547109701, -0.304212974580643, 0.172831987560697, 0.198455163737413, -0.135298615671123, -0.565282388889337, -0.9691261199302,
        -0.947227800414312);
    testStripping(stripper, prices, MarketDataType.PRICE, vega, expPriceChiSqr, expPriceFitParms, 1e-7, false);

    // fit to vols
    double expVolChiSqr = 242619.18324317678;
    DoubleMatrix1D expVolFitParms = new DoubleMatrix1D(-4.30213860658499, -0.317785074052976, 0.171692755804742, 0.202209112938613, -0.134975254807032, -0.554874332703476, -0.973564599389498,
        -0.917346420590909);
    testStripping(stripper, vols, MarketDataType.VOL, errors, expVolChiSqr, expVolFitParms, 1e-7, false);

    // guess a start val
    DoubleMatrix1D guess = new DoubleMatrix1D(knots.length, 0.3);
    testStripping(stripper, prices, MarketDataType.PRICE, vega, guess, expPriceChiSqr, expPriceFitParms, 5e-5, false);
    testStripping(stripper, vols, MarketDataType.VOL, errors, guess, expVolChiSqr, expVolFitParms, 1e-4, false);
  }

  /**
   * check the Jacobian against the FD version
   */
  @Test
  public void jacobianTest() {
    double[] knots = new double[] {0.25, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
    MultiCapFloorPricer pricer = new MultiCapFloorPricer(getAllCaps(), getYieldCurves());

    TransformedInterpolator1D interpolator = new TransformedInterpolator1D(CombinedInterpolatorExtrapolatorFactory.getInterpolator(DEFAULT_INTERPOLATOR, DEFAULT_EXTRAPOLATOR), TRANSFORM);
    DiscreteVolatilityFunctionProvider pro = new DiscreteVolatilityFunctionProviderFromInterpolatedTermStructure(knots, interpolator);
    CapletStrippingImp imp = new CapletStrippingImp(pricer, pro);

    Function1D<DoubleMatrix1D, DoubleMatrix1D> func = imp.getCapVolFunction();
    Function1D<DoubleMatrix1D, DoubleMatrix2D> jacFunc = imp.getCapVolJacobianFunction();
    Function1D<DoubleMatrix1D, DoubleMatrix2D> jacFuncFD = DIFF.differentiate(func);

    int n = knots.length;
    DoubleMatrix1D pos = new DoubleMatrix1D(n);

    int nSamples = 20;
    for (int run = 0; run < nSamples; run++) {
      for (int i = 0; i < n; i++) {
        pos.getData()[i] = 6 * RANDOM.nextDouble() - 3.0;
      }

      compareJacobianFunc(jacFunc, jacFuncFD, pos, 1e-4);
    }
  }
}
