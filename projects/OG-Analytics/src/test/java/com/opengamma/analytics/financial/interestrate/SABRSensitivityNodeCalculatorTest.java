/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Map;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateParameters;
import com.opengamma.analytics.financial.provider.description.SABRDataSets;
import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.analytics.util.amount.SurfaceValue;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Tests the distribution of a given SABR sensitivity to the nodes. *
 */
@Test(groups = TestGroup.UNIT)
public class SABRSensitivityNodeCalculatorTest {

  private static final SABRInterestRateParameters SABR_PARAMETERS = SABRDataSets.createSABR2();

  private static final double TOLERANCE = 1.0E-10;

  @Test
  /**
   * Test for a sensitivity with only one sensitivity point.
   */
  public void onePoint() {
    final DoublesPair point = DoublesPair.of(1.25, 4.0);
    final double alphaValue = 12345.0;
    final SurfaceValue alpha = SurfaceValue.from(point, alphaValue);
    final double betaValue = 4321.0;
    final SurfaceValue beta = SurfaceValue.from(point, betaValue);
    final double rhoValue = 2345.0;
    final SurfaceValue rho = SurfaceValue.from(point, 2345.0);
    final double nuValue = 345.0;
    final SurfaceValue nu = SurfaceValue.from(point, 345.0);
    final PresentValueSABRSensitivityDataBundle onePoint = new PresentValueSABRSensitivityDataBundle(alpha, beta, rho, nu);
    final PresentValueSABRSensitivityDataBundle node = SABRSensitivityNodeCalculator.calculateNodeSensitivities(onePoint, SABR_PARAMETERS);
    final DoublesPair[] nodeExpected = new DoublesPair[4];
    nodeExpected[0] = DoublesPair.of(1.0, 2.0);
    nodeExpected[1] = DoublesPair.of(1.0, 5.0);
    nodeExpected[2] = DoublesPair.of(5.0, 2.0);
    nodeExpected[3] = DoublesPair.of(5.0, 5.0);
    for (int loopnode = 0; loopnode < 4; loopnode++) {
      assertTrue("SABR Node calculator " + loopnode, node.getAlpha().getMap().get(nodeExpected[loopnode]) != null);
      assertTrue("SABR Node calculator", Math.abs(node.getAlpha().getMap().get(nodeExpected[loopnode])) > TOLERANCE);
    }
    final Map<Double, Interpolator1DDataBundle> alphaData = (Map<Double, Interpolator1DDataBundle>) SABR_PARAMETERS.getAlphaSurface().getInterpolatorData();
    final Map<DoublesPair, Double> weight = SABR_PARAMETERS.getAlphaSurface().getInterpolator().getNodeSensitivitiesForValue(alphaData, point);
    for (int loopnode = 0; loopnode < 4; loopnode++) {
      assertEquals("SABR Node calculator " + loopnode, node.getAlpha().getMap().get(nodeExpected[loopnode]), weight.get(nodeExpected[loopnode]) * alphaValue, TOLERANCE);
      assertEquals("SABR Node calculator " + loopnode, node.getRho().getMap().get(nodeExpected[loopnode]), weight.get(nodeExpected[loopnode]) * rhoValue, TOLERANCE);
      assertEquals("SABR Node calculator " + loopnode, node.getNu().getMap().get(nodeExpected[loopnode]), weight.get(nodeExpected[loopnode]) * nuValue, TOLERANCE);
    }
  }

  @Test
  /**
   * Test with a sensitivity with only one sensitivity point not in the center.
   */
  public void onePointBorder() {
    final DoublesPair point = DoublesPair.of(1.25, 1.5);
    final double alphaValue = 12345.0;
    final SurfaceValue alpha = SurfaceValue.from(point, alphaValue);
    final double betaValue = 4321.0;
    final SurfaceValue beta = SurfaceValue.from(point, betaValue);
    final double rhoValue = 2345.6;
    final SurfaceValue rho = SurfaceValue.from(point, rhoValue);
    final double nuValue = 345.67;
    final SurfaceValue nu = SurfaceValue.from(point, nuValue);
    final PresentValueSABRSensitivityDataBundle onePoint = new PresentValueSABRSensitivityDataBundle(alpha, beta, rho, nu);
    final PresentValueSABRSensitivityDataBundle node = SABRSensitivityNodeCalculator.calculateNodeSensitivities(onePoint, SABR_PARAMETERS);
    final DoublesPair[] nodeExpected = new DoublesPair[4];
    nodeExpected[0] = DoublesPair.of(1.0, 1.0);
    nodeExpected[1] = DoublesPair.of(1.0, 2.0);
    nodeExpected[2] = DoublesPair.of(5.0, 2.0);
    for (int loopnode = 0; loopnode < 3; loopnode++) {
      assertTrue("SABR Node calculator " + loopnode, node.getAlpha().getMap().get(nodeExpected[loopnode]) != null);
      assertTrue("SABR Node calculator", Math.abs(node.getAlpha().getMap().get(nodeExpected[loopnode])) > TOLERANCE);
    }
    final Map<Double, Interpolator1DDataBundle> alphaData = (Map<Double, Interpolator1DDataBundle>) SABR_PARAMETERS.getAlphaSurface().getInterpolatorData();
    final Map<DoublesPair, Double> weight = SABR_PARAMETERS.getAlphaSurface().getInterpolator().getNodeSensitivitiesForValue(alphaData, point);
    for (int loopnode = 0; loopnode < 3; loopnode++) {
      assertEquals("SABR Node calculator " + loopnode, node.getAlpha().getMap().get(nodeExpected[loopnode]), weight.get(nodeExpected[loopnode]) * alphaValue, TOLERANCE);
      assertEquals("SABR Node calculator " + loopnode, node.getRho().getMap().get(nodeExpected[loopnode]), weight.get(nodeExpected[loopnode]) * rhoValue, TOLERANCE);
      assertEquals("SABR Node calculator " + loopnode, node.getNu().getMap().get(nodeExpected[loopnode]), weight.get(nodeExpected[loopnode]) * nuValue, TOLERANCE);
    }
  }

  @Test
  /**
   * Test for a sensitivity with two sensitivity points.
   */
  public void twoPoints() {
    final DoublesPair point1 = DoublesPair.of(1.25, 4.0);
    final SurfaceValue alpha1 = SurfaceValue.from(point1, 12345.0);
    final SurfaceValue beta1 = SurfaceValue.from(point1, 4321.0);
    final SurfaceValue rho1 = SurfaceValue.from(point1, 2345.6);
    final SurfaceValue nu1 = SurfaceValue.from(point1, 345.67);
    final DoublesPair point2 = DoublesPair.of(5.5, 9.0);
    final SurfaceValue alpha2 = SurfaceValue.from(point2, 2345.0);
    final SurfaceValue beta2 = SurfaceValue.from(point2, 5432.0);
    final SurfaceValue rho2 = SurfaceValue.from(point2, 345.6);
    final SurfaceValue nu2 = SurfaceValue.from(point2, 45.67);
    final PresentValueSABRSensitivityDataBundle onePoint1 = new PresentValueSABRSensitivityDataBundle(alpha1, beta1, rho1, nu1);
    final PresentValueSABRSensitivityDataBundle onePoint2 = new PresentValueSABRSensitivityDataBundle(alpha2, beta2, rho2, nu2);
    final PresentValueSABRSensitivityDataBundle twoPoints = onePoint1.plus(onePoint2);
    final PresentValueSABRSensitivityDataBundle node1 = SABRSensitivityNodeCalculator.calculateNodeSensitivities(onePoint1, SABR_PARAMETERS);
    final PresentValueSABRSensitivityDataBundle node2 = SABRSensitivityNodeCalculator.calculateNodeSensitivities(onePoint2, SABR_PARAMETERS);
    final PresentValueSABRSensitivityDataBundle nodeSum = SABRSensitivityNodeCalculator.calculateNodeSensitivities(twoPoints, SABR_PARAMETERS);
    assertEquals("SABR Node calculator", node1.plus(node2), nodeSum);
  }

}
