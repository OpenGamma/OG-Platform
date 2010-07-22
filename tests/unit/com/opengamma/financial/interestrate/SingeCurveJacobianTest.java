/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate;


/**
 * 
 */
public class SingeCurveJacobianTest {
  // private static final List<InterestRateDerivative> CASH;
  // private static final List<InterestRateDerivative> FRA;
  // private static final double[] CASH_NODES;
  // private static final double[] FRA_NODES;
  // private static final Extrapolator1D<Interpolator1DDataBundle, InterpolationResultWithSensitivities> EXTRAPOLATOR;
  // private static final DoubleMatrix1D X;
  // private static final JacobianCalculator CASH_ONLY_JACOBIAN;
  // private static final JacobianCalculator FRA_ONLY_JACOBIAN;
  // private static final int N = 10;
  //
  // static {
  // CASH = new ArrayList<InterestRateDerivative>();
  // FRA = new ArrayList<InterestRateDerivative>();
  // CASH_NODES = new double[N];
  // FRA_NODES = new double[N];
  // final double[] data = new double[N];
  // for (int i = 0; i < N; i++) {
  // CASH.add(new Cash(i));
  // FRA.add(new ForwardRateAgreement(i, i + 0.5));
  // CASH_NODES[i] = i;
  // FRA_NODES[i] = i + 0.5;
  // data[i] = Math.random() / 10;
  // }
  //
  // final Interpolator1DWithSensitivities<Interpolator1DDataBundle> interpolator = new LinearInterpolator1DWithSensitivities();
  // final ExtrapolatorMethod<Interpolator1DDataBundle, InterpolationResultWithSensitivities> flat_em_sense = new FlatExtrapolatorWithSensitivities<Interpolator1DDataBundle,
  // InterpolationResultWithSensitivities>();
  // EXTRAPOLATOR = new Extrapolator1D<Interpolator1DDataBundle, InterpolationResultWithSensitivities>(flat_em_sense, interpolator);
  //
  // CASH_ONLY_JACOBIAN = new SingleCurveJacobian<Interpolator1DDataBundle>(CASH, CASH_NODES, EXTRAPOLATOR);
  // FRA_ONLY_JACOBIAN = new SingleCurveJacobian<Interpolator1DDataBundle>(FRA, FRA_NODES, EXTRAPOLATOR);
  // X = new DoubleMatrix1D(data);
  // }
  //
  // @Test(expected = IllegalArgumentException.class)
  // public void testNullDerivatives() {
  // new SingleCurveJacobian<Interpolator1DDataBundle>(null, CASH_NODES, EXTRAPOLATOR);
  // }
  //
  // @Test(expected = IllegalArgumentException.class)
  // public void testNullTimes() {
  // new SingleCurveJacobian<Interpolator1DDataBundle>(CASH, null, EXTRAPOLATOR);
  // }
  //
  // @Test(expected = IllegalArgumentException.class)
  // public void testNullInterpolator() {
  // new SingleCurveJacobian<Interpolator1DDataBundle>(CASH, CASH_NODES, null);
  // }
  //
  // @Test(expected = IllegalArgumentException.class)
  // public void testEmptyDerivatives() {
  // new SingleCurveJacobian<Interpolator1DDataBundle>(new ArrayList<InterestRateDerivative>(), CASH_NODES, EXTRAPOLATOR);
  // }
  //
  // @Test(expected = IllegalArgumentException.class)
  // public void testEmptyTimes() {
  // new SingleCurveJacobian<Interpolator1DDataBundle>(CASH, new double[0], EXTRAPOLATOR);
  // }
  //
  // @Test
  // public void testFundingOnlySensitivities() {
  // final DoubleMatrix2D jacobian = CASH_ONLY_JACOBIAN.evaluate(X, (Function1D<DoubleMatrix1D, DoubleMatrix1D>[]) null);
  // assertEquals(N, jacobian.getNumberOfRows());
  // assertEquals(N, jacobian.getNumberOfColumns());
  // for (int i = 0; i < N; i++) {
  // for (int j = 0; j < N; j++) {
  // if (i == j) {
  // assertEquals(1.0, jacobian.getEntry(i, i), 0.0);
  // } else {
  // assertEquals(0.0, jacobian.getEntry(i, j), 0.0);
  // }
  // }
  // }
  //
  // }
  //
  // @Test
  // public void testForwardOnlySensitivities() {
  // final DoubleMatrix2D jacobian = FRA_ONLY_JACOBIAN.evaluate(X, (Function1D<DoubleMatrix1D, DoubleMatrix1D>[]) null);
  // final double[][] data = jacobian.getData();
  // assertEquals(data.length, N);
  // double[] row;
  // for (int i = 0; i < N; i++) {
  // row = data[i];
  // assertEquals(row.length, N);
  // for (int j = 0; j < N; j++) {
  // if (i == 0) {
  // if (j != 0) {
  // assertEquals(row[j], 0, 0);
  // }
  // } else if (j != i && j + 1 != i) {
  // assertEquals(row[j], 0, 0);
  // }
  // }
  // }
  // }

}
