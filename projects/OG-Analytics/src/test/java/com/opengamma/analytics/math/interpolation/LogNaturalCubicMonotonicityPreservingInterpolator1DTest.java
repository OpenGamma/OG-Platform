/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.analytics.math.function.PiecewisePolynomialWithSensitivityFunction1D;
import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.analytics.math.interpolation.data.Interpolator1DPiecewisePoynomialDataBundle;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;

/**
 * 
 */
public class LogNaturalCubicMonotonicityPreservingInterpolator1DTest {

  /**
   * Parameter for centered finite difference method
   */
  private static final double EPS = 1.e-7;

  /**
   * 
   */
  @Test
  public void sampleDiscountFactorTest() {
    final double[] xValues = new double[] {0.0027378507871300e0, 0.0054757015742600e0, 0.0082135523614000e0, 0.2491444216290200e0, 0.5010266940451800e0, 0.7501711156742000e0, 0.9993155373032200e0,
        1.2511978097193699e0, 1.5003422313483901e0, 1.7494866529774100e0, 2.0013689253935700e0, 3.0006844626967801e0, 4.0000000000000000e0, 4.9993155373032199e0, 6.0013689253935700e0,
        7.0006844626967801e0, 8.0000000000000000e0, 8.9993155373032199e0, 10.0013689253935993e0, 12.0000000000000000e0, 15.0006844626967997e0, 20.0000000000000000e0, 24.9993155373032003e0,
        30.0013689253935993e0, 40.0000000000000000e0, 50.0013689253935993e0 };
    final double[] yValues = new double[] {0.99999593708369e0, 0.99999187419186e0, 0.99998781131373e0, 0.99963871457804e0, 0.99931197383134e0, 0.99899586019624e0, 0.99866966149844e0,
        0.99832564422180e0, 0.99792999296885e0, 0.99743454286751e0, 0.99682495767659e0, 0.99292937367859e0, 0.98496305698586e0, 0.97175664897203e0, 0.95354966017410e0, 0.93180043033936e0,
        0.90782313592531e0, 0.88229750227640e0, 0.85563390241246e0, 0.80140694953051e0, 0.72507946553451e0, 0.61947164354046e0, 0.53291256117173e0, 0.45861337697437e0, 0.35127110211518e0,
        0.28213093243876e0 };
    final int nData = 26;
    final double[] logY = new double[nData];

    for (int i = 0; i < nData; ++i) {
      logY[i] = Math.log(yValues[i]);
    }

    final double[] xKeys = new double[] {0.005063344512662158, 0.005139627715404907, 0.007960760570640704, 0.008179170667708428, 0.22910692625871368, 0.23266742356895445, 0.47859017867010933,
        0.49392862228176115, 0.6904378202261836, 0.7263996219722134, 0.8199005224855304, 0.945873126790612, 1.2498749135827558, 1.2507661649174142, 1.3539547623621575, 1.460371621341975,
        1.6308673887369953, 1.6774521371423725, 1.812262913758786, 1.8443590023112892, 2.512207186635973, 2.7902874895903347, 3.5970425685297793, 3.741479211510104, 4.954414808553191,
        4.95841711559786, 5.777693163699214, 5.966957943112449, 6.7619078149733625, 6.797340882922661, 7.755712878276715, 7.761246168446458, 8.264202963790074, 8.957012101380226, 9.670583873712175,
        9.9744531730438, 11.744410440279495, 11.98851589537167, 13.757067713855939, 13.85816318412105, 19.330436883319244, 19.705909739712666, 21.99262439001881, 24.38460449420516,
        27.179508865117416, 27.53957839017066, 30.222135785652547, 37.33113813086212, 46.96368200755808, 47.53142371305861 };
    final int nKeys = xKeys.length;
    final double[] expected = new double[] {0.99999248611457103, 0.99999237291298715, 0.99998818645318877, 0.99998786233578885, 0.99966645224297979, 0.99966149753818778, 0.99934021998070011,
        0.99932090829766274, 0.99907242015512665, 0.9990264143643216, 0.99890554044171165, 0.99874030223560051, 0.99832755119482808, 0.99832626661226365, 0.99817218413513975, 0.99799939541010563,
        0.99768395031950818, 0.99758892295068913, 0.99729267363216323, 0.9972175285149506, 0.99520743791165955, 0.99401887684461598, 0.9887707042535494, 0.9875037479908445, 0.97246312858285799,
        0.97240057799605961, 0.95798143421057969, 0.95424357758140332, 0.93724127896579301, 0.93644213223383377, 0.913846914817395, 0.91371154774487628, 0.90120456534821014, 0.88340289900659885,
        0.86452578663030977, 0.85636016639709245, 0.80828492265546992, 0.80171503936611166, 0.7555875393367949, 0.7530419913565255, 0.63227742511869778, 0.62505304217145563, 0.58326729332102834,
        0.54286498363894087, 0.49892233087124682, 0.49352999629538352, 0.45565965848843298, 0.37515433502472273, 0.30073974985150431, 0.29714840245998997 };

    final PiecewisePolynomialInterpolator prim = new LogNaturalSplineHelper();
    final PiecewisePolynomialInterpolator primOriginal = new LogNaturalSplineHelper();
    final PiecewisePolynomialInterpolator interp = new MonotonicityPreservingCubicSplineInterpolator(prim);
    final PiecewisePolynomialInterpolator interpOriginal = new MonotonicityPreservingCubicSplineInterpolator(primOriginal);
    final PiecewisePolynomialWithSensitivityFunction1D func = new PiecewisePolynomialWithSensitivityFunction1D();

    final PiecewisePolynomialResultsWithSensitivity result = interp.interpolateWithSensitivity(xValues, logY);
    final PiecewisePolynomialResultsWithSensitivity resultOriginal = interpOriginal.interpolateWithSensitivity(xValues, logY);

    final DoubleMatrix2D resCoef = result.getCoefMatrix();
    final DoubleMatrix2D resCoefOriginal = resultOriginal.getCoefMatrix();
    final DoubleMatrix2D[] resSense = result.getCoefficientSensitivityAll();
    final DoubleMatrix2D[] resOriginal = resultOriginal.getCoefficientSensitivityAll();
    for (int i = 0; i < nData - 1; ++i) {
      for (int j = 0; j < 4; ++j) {
        assertEquals(resCoef.getData()[i][j], resCoefOriginal.getData()[i][j], 1.e-15);
        for (int k = 0; k < nData; ++k) {
          assertEquals(resSense[i].getData()[j][k], resOriginal[i].getData()[j][k], 1.e-15);
        }
      }
    }

    for (int i = 0; i < nKeys; ++i) {
      final double res = Math.exp(func.evaluate(result, xKeys[i]).getEntry(0));
      assertEquals(res, expected[i], 1.e-15);
    }

    final Interpolator1D interpWrap = Interpolator1DFactory.getInterpolator(Interpolator1DFactory.LOG_NATURAL_CUBIC_MONOTONE);
    final Interpolator1DDataBundle bundle = interpWrap.getDataBundle(xValues, yValues);
    for (int i = 0; i < nKeys; ++i) {
      final double res = interpWrap.interpolate(bundle, xKeys[i]);
      assertEquals(expected[i], res, 1.e-15);
    }

    /*
     * Test sensitivity
     */
    final double[] yValues1Up = new double[nData];
    final double[] yValues1Dw = new double[nData];
    for (int i = 0; i < nData; ++i) {
      yValues1Up[i] = yValues[i];
      yValues1Dw[i] = yValues[i];
    }
    for (int j = 0; j < nData; ++j) {
      yValues1Up[j] = yValues[j] * (1. + EPS);
      yValues1Dw[j] = yValues[j] * (1. - EPS);
      Interpolator1DDataBundle dataBund1Up = interpWrap.getDataBundleFromSortedArrays(xValues, yValues1Up);
      Interpolator1DDataBundle dataBund1Dw = interpWrap.getDataBundleFromSortedArrays(xValues, yValues1Dw);
      for (int i = 0; i < nKeys; ++i) {
        double res1 = 0.5 * (interpWrap.interpolate(dataBund1Up, xKeys[i]) - interpWrap.interpolate(dataBund1Dw, xKeys[i])) / EPS / yValues[j];
        assertEquals(res1, interpWrap.getNodeSensitivitiesForValue(bundle, xKeys[i])[j], Math.max(Math.abs(yValues[j]) * EPS, EPS));
      }
      yValues1Up[j] = yValues[j];
      yValues1Dw[j] = yValues[j];
    }

    /*
     * Test new bundle
     */
    final double[] values = bundle.getValues();
    final double[] breaks = ((Interpolator1DPiecewisePoynomialDataBundle) bundle).getBreakPointsY();
    for (int i = 0; i < nData; ++i) {
      assertEquals(values[i], yValues[i], 1.e-15);
      assertEquals(breaks[i], yValues[i], 1.e-15);
      assertEquals(bundle.get(xValues[i]), yValues[i], 1.e-15);
    }
    assertEquals(values[0], bundle.firstValue(), 1.e-15);
    assertEquals(values[nData - 1], bundle.lastValue(), 1.e-15);
  }

  /**
   * Data contain minima
   */
  @Test
  public void localMinimumTest() {
    final double[] xValues = new double[] {1., 2., 3., 4., 5., 6., 7., 8. };
    final double[] yValues = new double[] {11., 8., 5., 1.001, 1.001, 5., 8., 11. };

    final double[] xKeys = new double[] {1., 1.3, 1.6, 2., 2.3, 2.6, 3., 3.3, 3.6, 4., 4.3, 4.7, 5., 5.3, 5.7, 6., 6.3, 6.7, 7., 7.3, 7.7, 8. };
    final int nKeys = xKeys.length;
    final double[] expected = new double[] {11.000000000000002, 9.8292188543753305, 8.8720987912733342, 7.9999999999999982, 7.5805212645035995, 6.8919800777834626, 4.9999999999999991,
        3.1906863006920174, 1.8676668553236566, 1.0009999999999999, 0.77713354870874862, 0.77713354870874862, 1.0009999999999999, 1.5674583835931175, 3.1906863006920183, 4.9999999999999991,
        6.5303730578459076, 7.5805212645035995, 7.9999999999999982, 8.6084760049832294, 9.8292188543753305, 11.000000000000002 };
    final Interpolator1D interpWrap = Interpolator1DFactory.getInterpolator(Interpolator1DFactory.LOG_NATURAL_CUBIC_MONOTONE);
    final Interpolator1DDataBundle bundle = interpWrap.getDataBundle(xValues, yValues);
    for (int i = 0; i < nKeys; ++i) {
      final double res = interpWrap.interpolate(bundle, xKeys[i]);
      assertEquals(res, expected[i], expected[i] * 1.e-15);
    }

    /*
     * Test sensitivity
     */
    final int nData = 8;
    final double[] yValues1Up = new double[nData];
    final double[] yValues1Dw = new double[nData];
    for (int i = 0; i < nData; ++i) {
      yValues1Up[i] = yValues[i];
      yValues1Dw[i] = yValues[i];
    }
    for (int j = 0; j < nData; ++j) {
      yValues1Up[j] = yValues[j] * (1. + EPS);
      yValues1Dw[j] = yValues[j] * (1. - EPS);
      Interpolator1DDataBundle dataBund1Up = interpWrap.getDataBundleFromSortedArrays(xValues, yValues1Up);
      Interpolator1DDataBundle dataBund1Dw = interpWrap.getDataBundleFromSortedArrays(xValues, yValues1Dw);
      for (int i = 0; i < nKeys; ++i) {
        double res1 = 0.5 * (interpWrap.interpolate(dataBund1Up, xKeys[i]) - interpWrap.interpolate(dataBund1Dw, xKeys[i])) / EPS / yValues[j];
        assertEquals(res1, interpWrap.getNodeSensitivitiesForValue(bundle, xKeys[i])[j], Math.max(Math.abs(yValues[j]) * EPS, EPS));
      }
      yValues1Up[j] = yValues[j];
      yValues1Dw[j] = yValues[j];
    }

  }

  /**
   * First and last intervals are flat
   */
  @Test
  public void correctedEndIntervalsTest() {
    final double[] xValues = new double[] {1., 2., 3., 4., 5., 6., 7., 8. };
    final double[] yValues = new double[] {1.001, 1.001, 5., 8., 11., 12., 18., 18. };

    final double[] xKeys = new double[] {1., 1.3, 1.6, 2., 2.3, 2.6, 3., 3.3, 3.6, 4., 4.3, 4.7, 5., 5.3, 5.7, 6., 6.3, 6.7, 7., 7.3, 7.7, 8. };
    final int nKeys = xKeys.length;
    final double[] expected = new double[] {1.0009999999999999, 1.0009999999999999, 1.0009999999999999, 1.0009999999999999, 1.3100031054806665, 2.3728322906920676, 4.9999999999999991,
        6.547464969992709, 7.3863523497161641, 7.9999999999999982, 8.7698200360758953, 10.147409335950455, 11.000000000000002, 11.32816743116623, 11.473242089855484, 12, 13.599314796416568,
        16.75801353957614, 17.999999999999996, 17.999999999999996, 17.999999999999996, 17.999999999999996 };
    final PiecewisePolynomialInterpolator1D interpWrap = new LogNaturalCubicMonotonicityPreservingInterpolator1D();
    final Interpolator1DDataBundle bundle = interpWrap.getDataBundle(xValues, yValues);
    for (int i = 0; i < nKeys; ++i) {
      final double res = interpWrap.interpolate(bundle, xKeys[i]);
      assertEquals(res, expected[i], expected[i] * 1.e-15);
    }

    /*
     * Test sensitivity
     */
    final int nData = 8;
    final double[] yValues1Up = new double[nData];
    final double[] yValues1Dw = new double[nData];
    for (int i = 0; i < nData; ++i) {
      yValues1Up[i] = yValues[i];
      yValues1Dw[i] = yValues[i];
    }
    for (int j = 0; j < nData; ++j) {
      yValues1Up[j] = yValues[j] * (1. + EPS);
      yValues1Dw[j] = yValues[j] * (1. - EPS);
      Interpolator1DDataBundle dataBund1Up = interpWrap.getDataBundleFromSortedArrays(xValues, yValues1Up);
      Interpolator1DDataBundle dataBund1Dw = interpWrap.getDataBundleFromSortedArrays(xValues, yValues1Dw);
      for (int i = 0; i < nKeys; ++i) {
        double res1 = 0.5 * (interpWrap.interpolate(dataBund1Up, xKeys[i]) - interpWrap.interpolate(dataBund1Dw, xKeys[i])) / EPS / yValues[j];
        assertEquals(res1, interpWrap.getNodeSensitivitiesForValue(bundle, xKeys[i])[j], Math.max(Math.abs(yValues[j]) * EPS, EPS));
      }
      yValues1Up[j] = yValues[j];
      yValues1Dw[j] = yValues[j];
    }
  }

  /**
   * Data are totally flat
   */
  @Test
  public void constantTest() {
    final double[] xValues = new double[] {1., 2., 3., 4., 5., 6., 7., 8. };
    final double[] yValues = new double[] {1.001, 1.001, 1.001, 1.001, 1.001, 1.001, 1.001, 1.001 };

    final double[] xKeys = new double[] {1., 1.3, 1.6, 2., 2.3, 2.6, 3., 3.3, 3.6, 4., 4.3, 4.7, 5., 5.3, 5.7, 6., 6.3, 6.7, 7., 7.3, 7.7, 8. };
    final int nKeys = xKeys.length;
    final double[] expected = new double[] {1.0009999999999999, 1.0009999999999999, 1.0009999999999999, 1.0009999999999999, 1.0009999999999999, 1.0009999999999999, 1.0009999999999999,
        1.0009999999999999, 1.0009999999999999, 1.0009999999999999, 1.0009999999999999, 1.0009999999999999, 1.0009999999999999, 1.0009999999999999, 1.0009999999999999, 1.0009999999999999,
        1.0009999999999999, 1.0009999999999999, 1.0009999999999999, 1.0009999999999999, 1.0009999999999999, 1.0009999999999999 };
    final PiecewisePolynomialInterpolator1D interpWrap = new LogNaturalCubicMonotonicityPreservingInterpolator1D();
    final Interpolator1DDataBundle bundle = interpWrap.getDataBundle(xValues, yValues);
    for (int i = 0; i < nKeys; ++i) {
      final double res = interpWrap.interpolate(bundle, xKeys[i]);
      assertEquals(expected[i], res, expected[i] * 1.e-15);
    }

    /*
     * Test sensitivity
     */
    final int nData = 8;
    final double[] yValues1Up = new double[nData];
    final double[] yValues1Dw = new double[nData];
    for (int i = 0; i < nData; ++i) {
      yValues1Up[i] = yValues[i];
      yValues1Dw[i] = yValues[i];
    }
    for (int j = 0; j < nData; ++j) {
      yValues1Up[j] = yValues[j] * (1. + EPS);
      yValues1Dw[j] = yValues[j] * (1. - EPS);
      Interpolator1DDataBundle dataBund1Up = interpWrap.getDataBundleFromSortedArrays(xValues, yValues1Up);
      Interpolator1DDataBundle dataBund1Dw = interpWrap.getDataBundleFromSortedArrays(xValues, yValues1Dw);
      for (int i = 0; i < nKeys; ++i) {
        double res1 = 0.5 * (interpWrap.interpolate(dataBund1Up, xKeys[i]) - interpWrap.interpolate(dataBund1Dw, xKeys[i])) / EPS / yValues[j];
        assertEquals(res1, interpWrap.getNodeSensitivitiesForValue(bundle, xKeys[i])[j], Math.max(Math.abs(yValues[j]) * EPS, EPS));
      }
      yValues1Up[j] = yValues[j];
      yValues1Dw[j] = yValues[j];
    }
  }

  /**
   * Intervals are random
   */
  @Test
  public void differentIntervalsTest() {
    final double[] xValues = new double[] {1.0328724558967068, 1.2692381049172323, 2.8611430465380905, 4.296118458251132, 7.011992052151352, 7.293354144919639, 8.557971037612713, 8.77306861567384,
        10.572470371584489, 12.96945799507056 };
    final double[][] yValues = new double[][] {
        {1.1593075755231343, 2.794957672828094, 4.674733634811079, 5.517689918508841, 6.138447304104604, 6.264375977142906, 6.581666492568779, 8.378685055774037,
            10.005246918325483, 10.468304334744241 },
        {9.95780079114617, 8.733013195721913, 8.192165283188197, 6.539369493529048, 6.3868683960757515, 4.700471352238411, 4.555354921077598, 3.780781869340659, 2.299369456202763, 0.9182441378327986 } };
    final double[] xKeys = new double[] {1.183889983084374, 1.2385908948332678, 1.9130960889984017, 2.6751399625052708, 3.061475076285611, 3.8368242544942768, 5.18374791977202, 6.237315353617813,
        7.165363988178849, 7.292538274335414, 8.555769928347884, 8.556400741450425, 8.743446029721234, 8.758174808226803, 10.354819113659708, 10.54773504143382, 12.665952152847833, 12.720941630811579 };
    final int nKeys = xKeys.length;
    final double[][] expected = new double[][] {
        {2.2498175275276355, 2.6604493559730114, 4.1934731104936809, 4.6708996453788716, 4.7026667337466561, 5.1700040449097751, 5.8211261852832061,
            5.8734369002551237, 6.230372939183983, 6.2643743298420214, 6.5799716327905839, 6.5804567123216771, 8.2244714149632099, 8.320106237623623, 10.002105989812062, 10.005242307319149,
            10.311404607585567, 10.336575152039961 },
        {9.0115183927982674, 8.7923765265015685, 8.3742614861844871, 8.2582572028821648, 8.0294763379975009, 6.8678544068867886, 6.53398463318387, 6.4832650514113164, 5.3777446119604493,
            4.7007909964955576, 4.5560983095534917, 4.5558857802921331, 3.886702136938859, 3.8309544446032437, 2.3465700101454354, 2.3057759170296173, 1.0734291352697389, 1.0436689951231841, } };
    final Interpolator1D interpWrap = Interpolator1DFactory.getInterpolator(Interpolator1DFactory.LOG_NATURAL_CUBIC_MONOTONE);

    final int yDim = yValues.length;
    for (int k = 0; k < yDim; ++k) {
      final Interpolator1DDataBundle bundle = interpWrap.getDataBundle(xValues, yValues[k]);
      for (int i = 0; i < nKeys; ++i) {
        final double res = interpWrap.interpolate(bundle, xKeys[i]);
        assertEquals(res, expected[k][i], expected[k][i] * 1.e-15);
      }

      /*
       * Test sensitivity
       */
      final int nData = 10;
      final double[] yValues1Up = new double[nData];
      final double[] yValues1Dw = new double[nData];
      for (int i = 0; i < nData; ++i) {
        yValues1Up[i] = yValues[k][i];
        yValues1Dw[i] = yValues[k][i];
      }
      for (int j = 0; j < nData; ++j) {
        yValues1Up[j] = yValues[k][j] * (1. + EPS);
        yValues1Dw[j] = yValues[k][j] * (1. - EPS);
        Interpolator1DDataBundle dataBund1Up = interpWrap.getDataBundleFromSortedArrays(xValues, yValues1Up);
        Interpolator1DDataBundle dataBund1Dw = interpWrap.getDataBundleFromSortedArrays(xValues, yValues1Dw);
        for (int i = 0; i < nKeys; ++i) {
          double res1 = 0.5 * (interpWrap.interpolate(dataBund1Up, xKeys[i]) - interpWrap.interpolate(dataBund1Dw, xKeys[i])) / EPS / yValues[k][j];
          assertEquals(res1, interpWrap.getNodeSensitivitiesForValue(bundle, xKeys[i])[j], Math.max(Math.abs(yValues[k][j]) * EPS, EPS));
        }
        yValues1Up[j] = yValues[k][j];
        yValues1Dw[j] = yValues[k][j];
      }
    }
  }

}
