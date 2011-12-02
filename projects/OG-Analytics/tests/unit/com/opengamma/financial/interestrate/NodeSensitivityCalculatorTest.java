/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate;

import static com.opengamma.math.interpolation.Interpolator1DFactory.FLAT_EXTRAPOLATOR;
import static com.opengamma.math.interpolation.Interpolator1DFactory.LINEAR_EXTRAPOLATOR;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.time.calendar.Period;

import org.testng.annotations.Test;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.instrument.index.IborIndex;
import com.opengamma.financial.interestrate.swap.definition.FixedFloatSwap;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.math.curve.InterpolatedDoublesCurve;
import com.opengamma.math.differentiation.ScalarFieldFirstOrderDifferentiator;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.interpolation.CombinedInterpolatorExtrapolator;
import com.opengamma.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.math.interpolation.Interpolator1DFactory;
import com.opengamma.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.DoublesPair;

/**
 * 
 */
public abstract class NodeSensitivityCalculatorTest {

  protected static final String FUNDING_CURVE_NAME = "funding";
  protected static final String LIBOR_CURVE_NAME = "libor";
  protected static final InstrumentDerivative IRD;
  protected static final YieldCurveBundle INTERPOLATED_CURVES;
  protected static final YieldAndDiscountCurve FUNDING_CURVE;
  protected static final YieldAndDiscountCurve LIBOR_CURVE;
  protected static final Currency CUR = Currency.USD;

  private static final Period TENOR = Period.ofMonths(6);
  private static final int SETTLEMENT_DAYS = 2;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final DayCount DAY_COUNT_INDEX = DayCountFactory.INSTANCE.getDayCount("Actual/360");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
  private static final boolean IS_EOM = true;
  private static final IborIndex INDEX = new IborIndex(CUR, TENOR, SETTLEMENT_DAYS, CALENDAR, DAY_COUNT_INDEX, BUSINESS_DAY, IS_EOM);

  static {
    final double[] fixedPaymentTimes = new double[] {1, 2, 3, 4, 5};
    final double[] floatingPaymentTimes = new double[] {0.5, 1, 1.5, 2, 2.5, 3, 3.5, 4, 4.5, 5};

    final double[] fundingCurveNodes = new double[] {0.5, 1, 1.5, 2.0, 3.1, 4.1, 5};
    final double[] fundingCurveYields = new double[] {0.03, 0.04, 0.043, 0.046, 0.4, 0.036, 0.03};
    final double[] liborCurveNodes = new double[] {1, 1.5, 1.9, 3., 4.0, 6.0};
    final double[] liborCurveYields = new double[] {0.041, 0.043, 0.048, 0.41, 0.0362, 0.032};

    CombinedInterpolatorExtrapolator extrapolator = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.DOUBLE_QUADRATIC, LINEAR_EXTRAPOLATOR, FLAT_EXTRAPOLATOR);

    FUNDING_CURVE = new YieldCurve(InterpolatedDoublesCurve.fromSorted(fundingCurveNodes, fundingCurveYields, extrapolator));
    extrapolator = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.NATURAL_CUBIC_SPLINE, LINEAR_EXTRAPOLATOR, FLAT_EXTRAPOLATOR);
    LIBOR_CURVE = new YieldCurve(InterpolatedDoublesCurve.fromSorted(liborCurveNodes, liborCurveYields, extrapolator));

    LinkedHashMap<String, YieldAndDiscountCurve> curves = new LinkedHashMap<String, YieldAndDiscountCurve>();
    curves.put(FUNDING_CURVE_NAME, FUNDING_CURVE);
    curves.put(LIBOR_CURVE_NAME, LIBOR_CURVE);
    INTERPOLATED_CURVES = new YieldCurveBundle(curves);

    final double couponRate = 0.07;
    IRD = new FixedFloatSwap(CUR, fixedPaymentTimes, floatingPaymentTimes, INDEX, couponRate, FUNDING_CURVE_NAME, LIBOR_CURVE_NAME, true);

  }

  protected abstract NodeSensitivityCalculator getCalculator();

  protected abstract InstrumentDerivativeVisitor<YieldCurveBundle, Map<String, List<DoublesPair>>> getSensitivityCalculator();

  protected abstract InstrumentDerivativeVisitor<YieldCurveBundle, Double> getValueCalculator();

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullInstrument() {
    getCalculator().calculateSensitivities(null, getSensitivityCalculator(), null, INTERPOLATED_CURVES);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCalculator() {
    getCalculator().calculateSensitivities(IRD, null, null, INTERPOLATED_CURVES);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullInterpolatedCurves() {
    getCalculator().calculateSensitivities(IRD, getSensitivityCalculator(), null, (YieldCurveBundle) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongNames() {
    getCalculator().calculateSensitivities(IRD, getSensitivityCalculator(), new YieldCurveBundle(INTERPOLATED_CURVES), INTERPOLATED_CURVES);
  }

  @Test
  public void testWithKnownCurve() {
    final YieldCurveBundle fixedCurve = new YieldCurveBundle();
    fixedCurve.setCurve(FUNDING_CURVE_NAME, FUNDING_CURVE);
    final LinkedHashMap<String, YieldAndDiscountCurve> fittingCurveMap = new LinkedHashMap<String, YieldAndDiscountCurve>();
    fittingCurveMap.put(LIBOR_CURVE_NAME, LIBOR_CURVE);
    YieldCurveBundle fittingCurve = new YieldCurveBundle(fittingCurveMap);
    final InstrumentDerivativeVisitor<YieldCurveBundle, Double> valueCalculator = getValueCalculator();
    final InstrumentDerivativeVisitor<YieldCurveBundle, Map<String, List<DoublesPair>>> sensitivityCalculator = getSensitivityCalculator();
    final DoubleMatrix1D result = getCalculator().calculateSensitivities(IRD, sensitivityCalculator, fixedCurve, fittingCurve);
    final DoubleMatrix1D fdResult = finiteDiffNodeSensitivities(IRD, valueCalculator, fixedCurve, fittingCurve);
    assertArrayEquals(fdResult.getData(), result.getData(), 1e-8);
  }

  protected DoubleMatrix1D finiteDiffNodeSensitivities(final InstrumentDerivative ird, final InstrumentDerivativeVisitor<YieldCurveBundle, Double> valueCalculator,
      final YieldCurveBundle fixedCurves, final YieldCurveBundle interpolatedCurves) {

    int nNodes = 0;
    for (final String curveName : interpolatedCurves.getAllNames()) {
      final Interpolator1DDataBundle dataBundle = ((InterpolatedDoublesCurve) interpolatedCurves.getCurve(curveName).getCurve()).getDataBundle();
      nNodes += dataBundle.size();
    }

    final double[] yields = new double[nNodes];
    int index = 0;
    for (final String curveName : interpolatedCurves.getAllNames()) {
      final Interpolator1DDataBundle dataBundle = ((InterpolatedDoublesCurve) interpolatedCurves.getCurve(curveName).getCurve()).getDataBundle();
      for (final double y : dataBundle.getValues()) {
        yields[index++] = y;
      }
    }

    final Function1D<DoubleMatrix1D, Double> f = new Function1D<DoubleMatrix1D, Double>() {

      @Override
      public Double evaluate(final DoubleMatrix1D x) {

        final YieldCurveBundle curves = new YieldCurveBundle();
        int index2 = 0;
        for (final String name : interpolatedCurves.getAllNames()) {
          final YieldAndDiscountCurve curve = interpolatedCurves.getCurve(name);
          final Interpolator1DDataBundle dataBundle = ((InterpolatedDoublesCurve) curve.getCurve()).getDataBundle();
          final int numberOfNodes = dataBundle.size();

          final double[] yields1 = Arrays.copyOfRange(x.getData(), index2, index2 + numberOfNodes);
          index2 += numberOfNodes;

          final YieldAndDiscountCurve newCurve = new YieldCurve(InterpolatedDoublesCurve.from(dataBundle.getKeys(), yields1, ((InterpolatedDoublesCurve) curve.getCurve()).getInterpolator()));
          curves.setCurve(name, newCurve);
        }
        if (fixedCurves != null) {
          curves.addAll(fixedCurves);
        }
        return valueCalculator.visit(ird, curves);
      }
    };

    final ScalarFieldFirstOrderDifferentiator fd = new ScalarFieldFirstOrderDifferentiator();
    final Function1D<DoubleMatrix1D, DoubleMatrix1D> grad = fd.differentiate(f);

    return grad.evaluate(new DoubleMatrix1D(yields));

  }
}
