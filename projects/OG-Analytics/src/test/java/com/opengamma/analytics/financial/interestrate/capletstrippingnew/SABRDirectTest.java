/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.capletstrippingnew;

import static com.opengamma.analytics.math.matrix.AssertMatrix.assertEqualsMatrix;

import java.util.Arrays;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.differentiation.VectorFieldFirstOrderDifferentiator;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.analytics.math.interpolation.PenaltyMatrixGenerator;
import com.opengamma.analytics.math.matrix.AssertMatrix;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.math.matrix.IdentityMatrix;

/**
 * 
 */
public class SABRDirectTest extends CapletStrippingSetup {

  @Test
  public void functionsTest() {

    //  final ParameterizedSABRModelDiscreteVolatilityFunctionProvider pro = new ParameterizedSABRModelDiscreteVolatilityFunctionProvider(null, null)

    final MultiCapFloorPricer pricer = new MultiCapFloorPricer(getAllCaps(), getYieldCurves());
    final double[] t = pricer.getCapletExpiries();
    final double[] fwd = pricer.getCapletForwardRates();
    final ForwardCurve fwdCurve = new ForwardCurve(InterpolatedDoublesCurve.from(t, fwd,
        CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.LINEAR, Interpolator1DFactory.LINEAR_EXTRAPOLATOR)));

    final int nExp = t.length;
    final int nMP = 4 * nExp;

    final VectorFunction direct = new VectorFunction() {
      private final DoubleMatrix2D _i = new IdentityMatrix(nMP);

      @Override
      public DoubleMatrix1D evaluate(final DoubleMatrix1D x) {
        return x;
      }

      @Override
      public DoubleMatrix2D evaluateJacobian(final DoubleMatrix1D x) {
        return _i;
      }

      @Override
      public int getSizeOfDomain() {
        return nMP;
      }

      @Override
      public int getSizeOfRange() {
        return nMP;
      }
    };

    final ParameterizedSABRModelDiscreteVolatilityFunctionProvider pro = new ParameterizedSABRModelDiscreteVolatilityFunctionProvider(fwdCurve, direct);

    final DoubleMatrix1D x = new DoubleMatrix1D(nMP, 0.5);
    final DiscreteVolatilityFunction func = pro.from(pricer.getExpiryStrikeArray());
    //  final DoubleMatrix1D y = func.evaluate(x);
    //    System.out.println(y);
    final DoubleMatrix2D jac = func.evaluateJacobian(x);
    final DoubleMatrix2D jacFD = func.evaluateJacobianViaFD(x);
    //   System.out.println(jac);
    // System.out.println(jacFD);

    AssertMatrix.assertEqualsMatrix(jac, jacFD, 1e-5);
  }

  @Test
  public void test() {

    //  final ParameterizedSABRModelDiscreteVolatilityFunctionProvider pro = new ParameterizedSABRModelDiscreteVolatilityFunctionProvider(null, null)

    final MultiCapFloorPricer pricer = new MultiCapFloorPricer(getAllCaps(), getYieldCurves());
    final double[] t = pricer.getCapletExpiries();
    final double[] fwd = pricer.getCapletForwardRates();
    final ForwardCurve fwdCurve = new ForwardCurve(InterpolatedDoublesCurve.from(t, fwd,
        CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.LINEAR, Interpolator1DFactory.LINEAR_EXTRAPOLATOR)));

    final int nExp = t.length;
    final int nMP = 3 * nExp + 1;
    final int nSMP = 4 * nExp;

    final VectorFunction direct = new VectorFunction() {
      final DoubleMatrix2D res = new DoubleMatrix2D(nSMP, nMP);
      {
        final double[][] data = res.getData();
        for (int i = 0; i < nExp; i++) {
          data[i][i] = 1.0;
          data[i + nExp][nExp] = 1.0;
          data[i + 2 * nExp][nExp + 1 + i] = 1.0;
          data[i + 3 * nExp][2 * nExp + 1 + i] = 1.0;
        }
      };

      @Override
      public DoubleMatrix1D evaluate(final DoubleMatrix1D x) {
        final double[] data = new double[nSMP];
        System.arraycopy(x.getData(), 0, data, 0, nExp); //copy alpha values
        Arrays.fill(data, nExp, 2 * nExp, x.getEntry(nExp));//copy beta (flat) values
        System.arraycopy(x.getData(), nExp + 1, data, 2 * nExp, nExp); //copy nu values
        System.arraycopy(x.getData(), 2 * nExp + 1, data, 3 * nExp, nExp); //copy rho values
        return new DoubleMatrix1D(data);
      }

      @Override
      public DoubleMatrix2D evaluateJacobian(final DoubleMatrix1D x) {
        return res;
      }

      @Override
      public int getSizeOfDomain() {
        return nMP;
      }

      @Override
      public int getSizeOfRange() {
        return nSMP;
      }
    };

    final DiscreteVolatilityFunctionProvider pro = new ParameterizedSABRModelDiscreteVolatilityFunctionProvider(fwdCurve, direct);
    final CapletStrippingImp imp = new CapletStrippingImp(pricer, pro);

    final double[] capVols = getAllCapVols();
    final int nCaps = capVols.length;
    final double[] errors = new double[nCaps];
    Arrays.fill(errors, 1e-4);
    //    final double[] start = new double[] {0.0296090408077002, 0.0323148834800232, 0.0352636710236523, 0.0384764019297644, 0.0418094920057842, 0.0450663108114834, 0.0481879796436259,
    //      0.0511149694080318, 0.053793384076736, 0.0561669864939157, 0.0581760716261173, 0.0597663859191449, 0.0606774648043976, 0.0607880011275114, 0.0602823987128094, 0.0593613842785042,
    //      0.0582259907184873, 0.0570676250174128, 0.0560642989071224, 0.0553825384334904, 0.0549449390044103, 0.0545541173340718, 0.0541921900801821, 0.0538417816287757, 0.0534859622076629,
    //      0.0531082387740385, 0.0526925964087829, 0.0522235878157922, 0.0517107597447848, 0.0511741574271907, 0.0506146070441917, 0.0500329644691091, 0.0494301131701618, 0.0488069620569711,
    //      0.0481644432776557, 0.0475035099735261, 0.0468251339985147, 0.0461303036105888, 0.0454200211424711, 0.478625958, 0.215125875578167, 0.204968038606977, 0.194765856054328, 0.184521355836172,
    //      0.174316337756652, 0.164233771686256, 0.154277327880765, 0.14445049116498, 0.13475656278902, 0.125198662748784, 0.115779732537246, 0.106502538293104, 0.0973696743133885, 0.0883835668968935,
    //      0.0795464784858006, 0.0708605120734981, 0.0623276158474116, 0.0539495880366012, 0.0457280819349336, 0.0376646110717935, 0.0297605545035311, 0.0220171622001373, 0.0144355605029852,
    //      0.00701675763085366, -0.000238350787154128, -0.00732897617177812, -0.0142544314654339, -0.0210141256329016, -0.0276897286772523, -0.0343628628291843, -0.0410329343633177, -0.0476993506453877,
    //      -0.0543615203423975, -0.0610188536317878, -0.0676707624094668, -0.0743166604965439, -0.0809559638446153, -0.0875880907394484, -0.0942124620029167, 1.25993597778208, 1.13310876905195,
    //      1.0134963656095, 0.901523099305298, 0.802348834344513, 0.719542075141537, 0.651134474121717, 0.595310499711286, 0.551874799049583, 0.518814512207231, 0.492366775458919, 0.469390537147267,
    //      0.450565854130236, 0.437308358095147, 0.428125553275037, 0.421706471581847, 0.41684947986411, 0.412416669938032, 0.407313108089046, 0.400489898165876, 0.392145416766446, 0.383396277982277,
    //      0.37460117979898, 0.366093838189781, 0.358183302016885, 0.351156632434937, 0.345283777498388, 0.340824502395145, 0.337583379184493, 0.335181048471213, 0.333602740131358, 0.332838750155582,
    //      0.3328843812958, 0.333739914104967, 0.335410608670995, 0.337906737026997, 0.341243645899962, 0.345441849115763, 0.35052714858715 };
    final double[] start = new double[nMP];
    Arrays.fill(start, 0, nExp, 0.2); //alpha
    start[nExp] = 0.9; //beta
    Arrays.fill(start, nExp + 1, 2 * nExp + 1, 0.2); //rho
    Arrays.fill(start, 2 * nExp + 1, 3 * nExp + 1, 0.5); //nu

    final double[][] basePMatrix = PenaltyMatrixGenerator.getPenaltyMatrix(t, 2).getData();
    final double lambdaA = 0.001;
    final double lambdaR = 0.01;
    final double lambdaN = 0.01;

    final double[][] data = new double[nMP][nMP];
    for (int i = 0; i < nExp; i++) {
      for (int j = 0; j < nExp; j++) {
        data[i][j] = lambdaA * basePMatrix[i][j];
        data[i + nExp + 1][j + nExp + 1] = lambdaR * basePMatrix[i][j];
        data[i + 2 * nExp + 1][j + 2 * nExp + 1] = lambdaN * basePMatrix[i][j];
      }
    }
    final DoubleMatrix2D p = new DoubleMatrix2D(data);
    // System.out.println(p);
    final Function1D<DoubleMatrix1D, DoubleMatrix1D> f1 = imp.getCapVolFunction();
    final Function1D<DoubleMatrix1D, DoubleMatrix2D> j1 = imp.getCapVolJacobianFunction();
    final VectorFieldFirstOrderDifferentiator diff = new VectorFieldFirstOrderDifferentiator();
    final Function1D<DoubleMatrix1D, DoubleMatrix2D> j1FD = diff.differentiate(f1);
    assertEqualsMatrix(j1.evaluate(new DoubleMatrix1D(start)), j1FD.evaluate(new DoubleMatrix1D(start)), 1e-4);

    final Function1D<DoubleMatrix1D, Boolean> allowed = new Function1D<DoubleMatrix1D, Boolean>() {

      @Override
      public Boolean evaluate(final DoubleMatrix1D x) {
        if (x.getEntry(nExp) < 0.0) {
          return false;
        }
        for (int i = 0; i < nExp; i++) {
          if (x.getEntry(i) < 0) {
            return false;
          }
          if (x.getEntry(i + 1 + nExp) >= 1.0 || x.getEntry(i + 1 + nExp) <= -1.0) {
            return false;
          }
          if (x.getEntry(i + 1 + 2 * nExp) < 0) {
            return false;
          }
        }
        return true;
      }
    };

    final CapletStrippingResult res = imp.solveForCapVols(capVols, errors, new DoubleMatrix1D(start), p, allowed);
    System.out.println(res);
  }
}
