/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Map;

import org.testng.annotations.Test;

import com.opengamma.financial.model.option.definition.SABRInterestRateParameters;
import com.opengamma.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.util.surface.SurfaceValue;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Tests the distribution of a given SABR sensitivity to the nodes.
 */
public class SABRSensitivityNodeCalculatorTest {

  private static final SABRSensitivityNodeCalculator NODE_CALCULATOR = new SABRSensitivityNodeCalculator();
  private static final SABRInterestRateParameters SABR_PARAMETERS = TestsDataSets.createSABR2();

  private static final double TOLERANCE = 1.0E-10;

  @Test
  /**
   * Test for a sensitivity with only one sensitivity point.
   */
  public void onePoint() {
    DoublesPair point = new DoublesPair(1.25, 4.0);
    double alphaValue = 12345.0;
    SurfaceValue alpha = SurfaceValue.from(point, alphaValue);
    double rhoValue = 2345.0;
    SurfaceValue rho = SurfaceValue.from(point, 2345.0);
    double nuValue = 345.0;
    SurfaceValue nu = SurfaceValue.from(point, 345.0);
    PresentValueSABRSensitivityDataBundle onePoint = new PresentValueSABRSensitivityDataBundle(alpha, rho, nu);
    PresentValueSABRSensitivityDataBundle node = NODE_CALCULATOR.calculateNodeSensitivities(onePoint, SABR_PARAMETERS);
    DoublesPair[] nodeExpected = new DoublesPair[4];
    nodeExpected[0] = new DoublesPair(1.0, 2.0);
    nodeExpected[1] = new DoublesPair(1.0, 5.0);
    nodeExpected[2] = new DoublesPair(5.0, 2.0);
    nodeExpected[3] = new DoublesPair(5.0, 5.0);
    for (int loopnode = 0; loopnode < 4; loopnode++) {
      assertTrue("SABR Node calculator " + loopnode, node.getAlpha().getMap().get(nodeExpected[loopnode]) != null);
      assertTrue("SABR Node calculator", Math.abs(node.getAlpha().getMap().get(nodeExpected[loopnode])) > TOLERANCE);
    }
    @SuppressWarnings("unchecked")
    Map<Double, Interpolator1DDataBundle> alphaData = (Map<Double, Interpolator1DDataBundle>) SABR_PARAMETERS.getAlphaSurface().getInterpolatorData();
    Map<DoublesPair, Double> weight = SABR_PARAMETERS.getAlphaSurface().getInterpolator().getNodeSensitivitiesForValue(alphaData, point);
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
    DoublesPair point = new DoublesPair(1.25, 1.5);
    double alphaValue = 12345.0;
    SurfaceValue alpha = SurfaceValue.from(point, alphaValue);
    double rhoValue = 2345.6;
    SurfaceValue rho = SurfaceValue.from(point, rhoValue);
    double nuValue = 345.67;
    SurfaceValue nu = SurfaceValue.from(point, nuValue);
    PresentValueSABRSensitivityDataBundle onePoint = new PresentValueSABRSensitivityDataBundle(alpha, rho, nu);
    PresentValueSABRSensitivityDataBundle node = NODE_CALCULATOR.calculateNodeSensitivities(onePoint, SABR_PARAMETERS);
    DoublesPair[] nodeExpected = new DoublesPair[4];
    nodeExpected[0] = new DoublesPair(1.0, 1.0);
    nodeExpected[1] = new DoublesPair(1.0, 2.0);
    nodeExpected[2] = new DoublesPair(5.0, 2.0);
    for (int loopnode = 0; loopnode < 3; loopnode++) {
      assertTrue("SABR Node calculator " + loopnode, node.getAlpha().getMap().get(nodeExpected[loopnode]) != null);
      assertTrue("SABR Node calculator", Math.abs(node.getAlpha().getMap().get(nodeExpected[loopnode])) > TOLERANCE);
    }
    @SuppressWarnings("unchecked")
    Map<Double, Interpolator1DDataBundle> alphaData = (Map<Double, Interpolator1DDataBundle>) SABR_PARAMETERS.getAlphaSurface().getInterpolatorData();
    Map<DoublesPair, Double> weight = SABR_PARAMETERS.getAlphaSurface().getInterpolator().getNodeSensitivitiesForValue(alphaData, point);
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
    DoublesPair point1 = new DoublesPair(1.25, 4.0);
    SurfaceValue alpha1 = SurfaceValue.from(point1, 12345.0);
    SurfaceValue rho1 = SurfaceValue.from(point1, 2345.6);
    SurfaceValue nu1 = SurfaceValue.from(point1, 345.67);
    DoublesPair point2 = new DoublesPair(5.5, 9.0);
    SurfaceValue alpha2 = SurfaceValue.from(point2, 2345.0);
    SurfaceValue rho2 = SurfaceValue.from(point2, 345.6);
    SurfaceValue nu2 = SurfaceValue.from(point2, 45.67);
    PresentValueSABRSensitivityDataBundle onePoint1 = new PresentValueSABRSensitivityDataBundle(alpha1, rho1, nu1);
    PresentValueSABRSensitivityDataBundle onePoint2 = new PresentValueSABRSensitivityDataBundle(alpha2, rho2, nu2);
    PresentValueSABRSensitivityDataBundle twoPoints = PresentValueSABRSensitivityDataBundle.plus(onePoint1, onePoint2);
    PresentValueSABRSensitivityDataBundle node1 = NODE_CALCULATOR.calculateNodeSensitivities(onePoint1, SABR_PARAMETERS);
    PresentValueSABRSensitivityDataBundle node2 = NODE_CALCULATOR.calculateNodeSensitivities(onePoint2, SABR_PARAMETERS);
    PresentValueSABRSensitivityDataBundle nodeSum = NODE_CALCULATOR.calculateNodeSensitivities(twoPoints, SABR_PARAMETERS);
    assertEquals("SABR Node calculator", PresentValueSABRSensitivityDataBundle.plus(node1, node2), nodeSum);
  }

}
