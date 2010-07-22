/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate;


/**
 * 
 */
public class DoubleCurveJacobianTest {
  // private static final List<InterestRateDerivative> CASH;
  // private static final List<InterestRateDerivative> FRA;
  // private static final List<InterestRateDerivative> MIXED_INSTRUMENT;
  // private static final double[] FORWARD_NODES;
  // private static final double[] FUNDING_NODES;
  // private static final Extrapolator1D<Interpolator1DDataBundle, InterpolationResultWithSensitivities> EXTRAPOLATOR;
  // private static final DoubleMatrix1D XN;
  // private static final DoubleMatrix1D XM;
  // private static final DoubleMatrix1D XNM;
  // private static final JacobianCalculator CASH_ONLY;
  // private static final JacobianCalculator FRA_ONLY;
  // private static final JacobianCalculator MIXED;
  // private static final int N = 10;
  // private static final int M = 5;

  // static {
  // CASH = new ArrayList<InterestRateDerivative>();
  // FRA = new ArrayList<InterestRateDerivative>();
  // MIXED_INSTRUMENT = new ArrayList<InterestRateDerivative>();
  // FORWARD_NODES = new double[N];
  // FUNDING_NODES = new double[M];
  // double[] dataN = new double[N];
  // double[] dataM = new double[M];
  // final double[] dataNpM = new double[N + M];
  // for (int i = 0; i < N; i++) {
  // InterestRateDerivative ird = new ForwardRateAgreement(i, i + 0.5);
  // FRA.add(ird);
  // MIXED_INSTRUMENT.add(ird);
  // FORWARD_NODES[i] = i + 1;
  // dataN[i] = Math.random() / 10;
  // dataNpM[i] = dataN[i];
  // }
  //
  // for (int i = 0; i < M; i++) {
  // InterestRateDerivative ird = new Cash(i);
  // CASH.add(ird);
  // MIXED_INSTRUMENT.add(ird);
  // FUNDING_NODES[i] = i;
  // dataM[i] = Math.random() / 10;
  // dataNpM[i + N] = dataM[i];
  // }
  //
  // final Interpolator1DWithSensitivities<Interpolator1DDataBundle> interpolator = new LinearInterpolator1DWithSensitivities();
  // final ExtrapolatorMethod<Interpolator1DDataBundle, InterpolationResultWithSensitivities> flat_em_sense = new FlatExtrapolatorWithSensitivities<Interpolator1DDataBundle,
  // InterpolationResultWithSensitivities>();
  // EXTRAPOLATOR = new Extrapolator1D<Interpolator1DDataBundle, InterpolationResultWithSensitivities>(flat_em_sense, interpolator);
  //
  // XM = new DoubleMatrix1D(dataM);
  // XN = new DoubleMatrix1D(dataN);
  // XNM = new DoubleMatrix1D(dataNpM);
  // CASH_ONLY = new DoubleCurveJacobian<Interpolator1DDataBundle>(CASH, null, FUNDING_NODES, new ConstantYieldCurve(0.05), null, EXTRAPOLATOR, EXTRAPOLATOR);
  // FRA_ONLY = new DoubleCurveJacobian<Interpolator1DDataBundle>(FRA, FORWARD_NODES, null, null, new ConstantYieldCurve(0.05), EXTRAPOLATOR, EXTRAPOLATOR);
  // MIXED = new DoubleCurveJacobian<Interpolator1DDataBundle>(MIXED_INSTRUMENT, FORWARD_NODES, FUNDING_NODES, EXTRAPOLATOR, EXTRAPOLATOR);
  // }
  //
  // @Test(expected = IllegalArgumentException.class)
  // public void testNullDerivatives() {
  // new DoubleCurveJacobian<Interpolator1DDataBundle>(null, FORWARD_NODES, FUNDING_NODES, EXTRAPOLATOR, EXTRAPOLATOR);
  // }
  //
  // @Test(expected = IllegalArgumentException.class)
  // public void testNullForwardInterpolator() {
  // new DoubleCurveJacobian<Interpolator1DDataBundle>(MIXED_INSTRUMENT, FORWARD_NODES, FUNDING_NODES, null, EXTRAPOLATOR);
  // }
  //
  // @Test(expected = IllegalArgumentException.class)
  // public void testNullFundingInterpolator() {
  // new DoubleCurveJacobian<Interpolator1DDataBundle>(MIXED_INSTRUMENT, FORWARD_NODES, FUNDING_NODES, EXTRAPOLATOR, null);
  // }
  //
  // @Test(expected = IllegalArgumentException.class)
  // public void testEmptyDerivatives() {
  // new DoubleCurveJacobian<Interpolator1DDataBundle>(new ArrayList<InterestRateDerivative>(), FORWARD_NODES, FUNDING_NODES, EXTRAPOLATOR, EXTRAPOLATOR);
  // }
  //
  // @Test(expected = IllegalArgumentException.class)
  // public void testWrongNumberOfNodes() {
  // new DoubleCurveJacobian<Interpolator1DDataBundle>(MIXED_INSTRUMENT, FORWARD_NODES, FORWARD_NODES, EXTRAPOLATOR, EXTRAPOLATOR);
  // }
  //
  // @Test(expected = IllegalArgumentException.class)
  // public void testNullVector() {
  // CASH_ONLY.evaluate((DoubleMatrix1D) null, (Function1D<DoubleMatrix1D, DoubleMatrix1D>[]) null);
  // }
  //
  // @Test(expected = IllegalArgumentException.class)
  // public void testWrongNumberOfElements() {
  // CASH_ONLY.evaluate(XN, (Function1D<DoubleMatrix1D, DoubleMatrix1D>[]) null);
  // }
  //
  // @Test
  // public void testCashOnly() {
  // final DoubleMatrix2D jacobian = CASH_ONLY.evaluate(XM, (Function1D<DoubleMatrix1D, DoubleMatrix1D>[]) null);
  // assertEquals(M, jacobian.getNumberOfRows());
  // assertEquals(M, jacobian.getNumberOfColumns());
  // for (int i = 0; i < M; i++) {
  // for (int j = 0; j < M; j++) {
  // if (i == j) {
  // assertEquals(1.0, jacobian.getEntry(i, i), 0.0);
  // } else {
  // assertEquals(0.0, jacobian.getEntry(i, j), 0.0);
  // }
  // }
  // }
  // }
  //
  // @Test
  // public void testFRAOnly() {
  // final DoubleMatrix2D jacobian = FRA_ONLY.evaluate(XN, (Function1D<DoubleMatrix1D, DoubleMatrix1D>[]) null);
  // assertEquals(N, jacobian.getNumberOfRows());
  // assertEquals(N, jacobian.getNumberOfColumns());
  // for (int i = 0; i < N; i++) {
  // for (int j = 0; j < N; j++) {
  // if (i == j) {
  // assertTrue(jacobian.getEntry(i, j) > 0.0);
  // } else if (i == (j + 1)) {
  // assertTrue(jacobian.getEntry(i, j) < 0.0);
  // } else {
  // assertEquals(0.0, jacobian.getEntry(i, j), 0.0);
  // }
  //
  // }
  // }
  // }
  //
  // @Test
  // public void testMixed() {
  // final DoubleMatrix2D jacobian = MIXED.evaluate(XNM, (Function1D<DoubleMatrix1D, DoubleMatrix1D>[]) null);
  // assertEquals(N + M, jacobian.getNumberOfRows());
  // assertEquals(N + M, jacobian.getNumberOfColumns());
  //
  // for (int i = 0; i < N; i++) {
  // for (int j = 0; j < N + M; j++) {
  // if (i == j) {
  // assertTrue(jacobian.getEntry(i, j) > 0.0);
  // } else if (i == (j + 1)) {
  // assertTrue(jacobian.getEntry(i, j) < 0.0);
  // } else {
  // assertEquals(0.0, jacobian.getEntry(i, j), 0.0);
  // }
  // }
  // }
  // for (int i = N; i < N + M; i++) {
  // for (int j = 0; j < N + M; j++) {
  // if (i == j) {
  // assertEquals(1.0, jacobian.getEntry(i, i), 0.0);
  // } else {
  // assertEquals(0.0, jacobian.getEntry(i, j), 0.0);
  // }
  // }
  // }
  // }
}
