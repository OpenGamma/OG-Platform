/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fudgemsg;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.option.definition.SmileDeltaTermStructureParameter;

/**
 * 
 */
public class SmileDeltaTermStructureParameterBuilderTest extends AnalyticsTestBase {  

  @Test
  public void test() {
    final double[] t = {0.0, 0.25, 0.50, 1.00, 2.00};
    final double[] atm = {0.175, 0.185, 0.18, 0.17, 0.16};
    final double[] delta = new double[] {0.10, 0.25};
    final double[][] rr = new double[][] { {-0.010, -0.0050}, {-0.011, -0.0060}, {-0.012, -0.0070}, {-0.013, -0.0080}, {-0.014, -0.0090}};
    final double[][] strangle = new double[][] { {0.0300, 0.0100}, {0.0310, 0.0110}, {0.0320, 0.0120}, {0.0330, 0.0130}, {0.0340, 0.0140}};
    final SmileDeltaTermStructureParameter smiles = new SmileDeltaTermStructureParameter(t, delta, atm, rr, strangle);
    assertEquals(smiles, cycleObject(SmileDeltaTermStructureParameter.class, smiles));
  }
}
