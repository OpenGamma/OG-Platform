/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.curve.inflation.generator;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import org.apache.commons.lang.ArrayUtils;
import org.testng.annotations.Test;
import org.testng.internal.junit.ArrayAsserts;

import com.google.common.primitives.Doubles;
import com.opengamma.analytics.financial.model.interestrate.curve.PriceIndexCurveSimple;
import com.opengamma.analytics.math.curve.DoublesCurveInterpolatedAnchor;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;

/**
 * Tests related to the generator of price index curves interpolated with an "anchor" node.
 */
public class GeneratorPriceIndexCurveInterpolatedAnchorNodeTest {

  private static final Interpolator1D INTERPOLATOR_LINEAR = 
      CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.LINEAR, 
          Interpolator1DFactory.FLAT_EXTRAPOLATOR, Interpolator1DFactory.FLAT_EXTRAPOLATOR);
  private static final double ANCHOR_NODE = 0.5;
  private static final double ANCHOR_VALUE = 1.0;
  private static final double[] NODES = {1.0, 2.0};
  private static final int NB_NODES = NODES.length;
  
  private static final double TOLERANCE_NODE = 1.0E-6;
  
  private static final GeneratorPriceIndexCurveInterpolatedAnchorNode GENERATOR =
      new GeneratorPriceIndexCurveInterpolatedAnchorNode(NODES, INTERPOLATOR_LINEAR, ANCHOR_NODE, ANCHOR_VALUE);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullNodes() {
    new GeneratorPriceIndexCurveInterpolatedAnchorNode(null, INTERPOLATOR_LINEAR, ANCHOR_NODE, ANCHOR_VALUE);
  }
  
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullInterpolator() {
    new GeneratorPriceIndexCurveInterpolatedAnchorNode(NODES, null, ANCHOR_NODE, ANCHOR_VALUE);
  }
  
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullNameGenerate() {
    GENERATOR.generateCurve(null, NODES);
  }
  
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void wrongSize() {
    GENERATOR.generateCurve("name", new double[NB_NODES+1]);
  }
  
  @Test
  public void getter() {
    assertEquals("GeneratorPriceIndexCurveInterpolatedAnchor: getter", ANCHOR_NODE, GENERATOR.getAnchorNode());
    assertEquals("GeneratorPriceIndexCurveInterpolatedAnchor: getter", ANCHOR_VALUE, GENERATOR.getAnchorValue());
    assertEquals("GeneratorPriceIndexCurveInterpolatedAnchor: getter",
        Doubles.asList(NODES), Doubles.asList(GENERATOR.getNodePoints()));
  }

  @Test
  public void generateCurve() {
    String name = "CRV";
    double[] values = new double[NB_NODES];
    PriceIndexCurveSimple generated = GENERATOR.generateCurve(name, values);
    assertTrue("GeneratorPriceIndexCurveInterpolatedAnchorNode", 
        generated.getCurve() instanceof DoublesCurveInterpolatedAnchor);
    DoublesCurveInterpolatedAnchor curveAnchor = (DoublesCurveInterpolatedAnchor) generated.getCurve();
    assertTrue("GeneratorPriceIndexCurveInterpolatedAnchorNode", 
        curveAnchor.getInterpolator().equals(INTERPOLATOR_LINEAR));
    assertTrue("GeneratorPriceIndexCurveInterpolatedAnchorNode", 
        curveAnchor.getXData().length == 3);
    ArrayAsserts.assertArrayEquals("GeneratorPriceIndexCurveInterpolatedAnchorNode", curveAnchor.getXDataAsPrimitive(), 
        ArrayUtils.addAll(new double[]{ANCHOR_NODE}, NODES), TOLERANCE_NODE);
    ArrayAsserts.assertArrayEquals("GeneratorPriceIndexCurveInterpolatedAnchorNode", curveAnchor.getYDataAsPrimitive(), 
        ArrayUtils.addAll(new double[]{ANCHOR_VALUE}, values), TOLERANCE_NODE);
  }
  
}
