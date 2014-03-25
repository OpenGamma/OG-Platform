/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.volatilityswap;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * 
 */
@Test(groups = TestGroup.UNIT)
public class VolatilitySwapCalculatorResultTest {

  private static final double[] putWeights = new double[] {0., -1., 1.5, };
  private static final double straddleWeight = 1.e2;
  private static final double[] callWeights = new double[] {11. / 7. };
  private static final double[] putPrices = new double[] {3.1, 4., 5.214 };
  private static final double straddlePrice = 2.2;
  private static final double[] callPrices = new double[] {33. };
  private static final double cash = 11.3;

  private static final double[] putStrikes = new double[] {1.1, 1.3, 1.4 };
  private static final double[] callStrikes = new double[] {1.6 };

  /**
   * 
   */
  @Test
  public void accessTest() {

    final VolatilitySwapCalculatorResult res = new VolatilitySwapCalculatorResult(putWeights, straddleWeight, callWeights, putPrices, straddlePrice, callPrices, cash);
    final VolatilitySwapCalculatorResultWithStrikes resStrikes = new VolatilitySwapCalculatorResultWithStrikes(putStrikes, callStrikes, putWeights, straddleWeight, callWeights, putPrices,
        straddlePrice, callPrices, cash);
    final int nPuts = putWeights.length;
    final int nCalls = callWeights.length;

    double optionTotal = straddleWeight * straddlePrice;
    for (int i = 0; i < nPuts; ++i) {
      assertEquals(putWeights[i], res.getPutWeights()[i]);
      assertEquals(putPrices[i], res.getPutPrices()[i]);
      assertEquals(putStrikes[i], resStrikes.getPutStrikes()[i]);
      optionTotal += putWeights[i] * putPrices[i];
    }
    assertEquals(straddleWeight, res.getStraddleWeight());
    assertEquals(straddlePrice, res.getStraddlePrice());
    for (int i = 0; i < nCalls; ++i) {
      assertEquals(callWeights[i], res.getCallWeights()[i]);
      assertEquals(callPrices[i], res.getCallPrices()[i]);
      assertEquals(callStrikes[i], resStrikes.getCallStrikes()[i]);
      optionTotal += callWeights[i] * callPrices[i];
    }

    assertEquals(cash, res.getCash());
    assertEquals(optionTotal, res.getOptionTotal());
    assertEquals(optionTotal + cash, res.getFairValue());

    final VolatilitySwapCalculatorResultWithStrikes resStrikesFrom = res.withStrikes(putStrikes, callStrikes);
    assertEquals(resStrikes.hashCode(), resStrikesFrom.hashCode());
    assertEquals(resStrikes, resStrikesFrom);
  }

  /**
   * Equals and hashcode are tested
   */
  @Test
  public void hashEqualsTest() {
    final VolatilitySwapCalculatorResult res1 = new VolatilitySwapCalculatorResult(putWeights, straddleWeight, callWeights, putPrices, straddlePrice, callPrices, cash);
    final VolatilitySwapCalculatorResult res2 = new VolatilitySwapCalculatorResult(putWeights, straddleWeight, callWeights, putPrices, straddlePrice, callPrices, cash);
    final VolatilitySwapCalculatorResult res3 = new VolatilitySwapCalculatorResult(new double[] {0., 1., 1.5, }, straddleWeight, callWeights, putPrices, straddlePrice, callPrices, cash);
    final VolatilitySwapCalculatorResult res4 = new VolatilitySwapCalculatorResult(putWeights, straddleWeight + 2., callWeights, putPrices, straddlePrice, callPrices, cash);
    final VolatilitySwapCalculatorResult res5 = new VolatilitySwapCalculatorResult(putWeights, straddleWeight, new double[] {1.5, }, putPrices, straddlePrice, callPrices, cash);
    final VolatilitySwapCalculatorResult res6 = new VolatilitySwapCalculatorResult(putWeights, straddleWeight, callWeights, new double[] {1., 1., 1.5, }, straddlePrice, callPrices, cash);
    final VolatilitySwapCalculatorResult res7 = new VolatilitySwapCalculatorResult(putWeights, straddleWeight, callWeights, putPrices, straddlePrice + 1., callPrices, cash);
    final VolatilitySwapCalculatorResult res8 = new VolatilitySwapCalculatorResult(putWeights, straddleWeight, callWeights, putPrices, straddlePrice, new double[] {2. }, cash);
    final VolatilitySwapCalculatorResult res9 = new VolatilitySwapCalculatorResult(putWeights, straddleWeight, callWeights, putPrices, straddlePrice, callPrices, cash + 1.);

    assertTrue(res1.equals(res1));

    assertTrue(res1.equals(res2));
    assertTrue(res2.equals(res1));
    assertEquals(res1.hashCode(), res2.hashCode());

    assertTrue(!(res1.equals(res3)));
    assertTrue(!(res3.equals(res1)));

    assertTrue(!(res1.equals(res3)));
    assertTrue(!(res3.equals(res1)));

    assertTrue(!(res1.equals(res4)));
    assertTrue(!(res4.equals(res1)));

    assertTrue(!(res1.equals(res5)));
    assertTrue(!(res5.equals(res1)));

    assertTrue(!(res1.equals(res6)));
    assertTrue(!(res6.equals(res1)));

    assertTrue(!(res1.equals(res7)));
    assertTrue(!(res7.equals(res1)));

    assertTrue(!(res1.equals(res8)));
    assertTrue(!(res8.equals(res1)));

    assertTrue(!(res1.equals(res9)));
    assertTrue(!(res9.equals(res1)));

    assertTrue(!(res1.equals(null)));
    assertTrue(!(res1.equals(new CarrLeeSeasonedSyntheticVolatilitySwapCalculator())));

    final int size = 8;
    List<VolatilitySwapCalculatorResult> list = new ArrayList<>(size);
    list.add(res2);
    list.add(res3);
    list.add(res4);
    list.add(res5);
    list.add(res6);
    list.add(res7);
    list.add(res8);
    list.add(res9);

    for (int i = 0; i < size; ++i) {
      if (res1.hashCode() != list.get(i).hashCode()) {
        assertTrue(!(res1.equals(list.get(i))));
      }
    }

    final VolatilitySwapCalculatorResultWithStrikes resStrikesFrom1 = res1.withStrikes(putStrikes, callStrikes);
    final VolatilitySwapCalculatorResultWithStrikes resStrikesFrom3 = res3.withStrikes(putStrikes, callStrikes);
    final VolatilitySwapCalculatorResultWithStrikes resStrikesFrom11 = res1.withStrikes(putStrikes, callWeights);
    final VolatilitySwapCalculatorResultWithStrikes resStrikesFrom12 = res1.withStrikes(putWeights, callStrikes);

    assertTrue(resStrikesFrom1.equals(resStrikesFrom1));
    assertTrue(!(resStrikesFrom1.equals(null)));
    assertTrue(!(resStrikesFrom1.equals(res1)));

    assertTrue(!(resStrikesFrom1.equals(resStrikesFrom3)));
    assertTrue(!(resStrikesFrom1.equals(resStrikesFrom11)));
    assertTrue(!(resStrikesFrom1.equals(resStrikesFrom12)));
  }
}
