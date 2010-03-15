/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.riskfactor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.opengamma.financial.greeks.Greek;
import com.opengamma.financial.greeks.GreekResult;
import com.opengamma.financial.greeks.GreekResultCollection;
import com.opengamma.financial.greeks.MultipleGreekResult;
import com.opengamma.financial.greeks.SingleGreekResult;
import com.opengamma.financial.pnl.OptionTradeData;
import com.opengamma.financial.pnl.Underlying;
import com.opengamma.math.function.Function1D;

/**
 * @author emcleod
 *
 */
public class GreekToPositionGreekConverterTest {
  private static final Double N = 345.;
  private static final String VEGA_1 = "V1";
  private static final String VEGA_2 = "V2";
  private static final SingleGreekResult DELTA = new SingleGreekResult(0.45);
  private static final SingleGreekResult GAMMA = new SingleGreekResult(0.12);
  private static final Double VEGA_V1 = 0.78;
  private static final Double VEGA_V2 = 0.89;
  private static final Function1D<RiskFactorDataBundle, RiskFactorResultCollection> CONVERTER = new GreekToPositionGreekConverter();
  private static final GreekResultCollection GREEKS;
  private static final RiskFactorDataBundle DATA;

  static {
    GREEKS = new GreekResultCollection();
    GREEKS.put(Greek.DELTA, DELTA);
    GREEKS.put(Greek.GAMMA, GAMMA);
    final Map<String, Double> vega = new HashMap<String, Double>();
    vega.put(VEGA_1, VEGA_V1);
    vega.put(VEGA_2, VEGA_V2);
    GREEKS.put(Greek.VEGA, new MultipleGreekResult(vega));
    final Map<Greek, Map<Object, Double>> map = new HashMap<Greek, Map<Object, Double>>();
    final Map<Object, Double> m = new HashMap<Object, Double>();
    m.put(Underlying.SPOT_PRICE, 34.);
    m.put(OptionTradeData.NUMBER_OF_CONTRACTS, N);
    map.put(Greek.DELTA, m);
    map.put(Greek.GAMMA, m);
    map.put(Greek.VEGA, m);
    DATA = new RiskFactorDataBundle(GREEKS, map);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNull() {
    CONVERTER.evaluate((RiskFactorDataBundle) null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGreekResultType() {
    final GreekResult<?> temp = new GreekResult<Double>() {

      @Override
      public Double getResult() {
        return 5.;
      }

      @Override
      public boolean isMultiValued() {
        return true;
      }

    };
    final GreekResultCollection greeks = new GreekResultCollection();
    greeks.put(Greek.DELTA, temp);
    CONVERTER.evaluate(new RiskFactorDataBundle(greeks, Collections.<Greek, Map<Object, Double>> singletonMap(Greek.DELTA, Collections.<Object, Double> singletonMap(
        OptionTradeData.NUMBER_OF_CONTRACTS, N))));
  }

  @Test
  public void test() {
    final RiskFactorResultCollection result = CONVERTER.evaluate(DATA);
    assertEquals(result.get(PositionGreek.POSITION_DELTA).getResult(), DELTA.getResult() * N);
    assertEquals(result.get(PositionGreek.POSITION_GAMMA).getResult(), GAMMA.getResult() * N);
    assertTrue(result.get(PositionGreek.POSITION_VEGA) instanceof MultipleRiskFactorResult);
    final Map<Object, Double> vega = ((MultipleRiskFactorResult) result.get(PositionGreek.POSITION_VEGA)).getResult();
    assertEquals(vega.size(), 2);
    assertEquals(vega.get(VEGA_1), VEGA_V1 * N, 1e-12);
    assertEquals(vega.get(VEGA_2), VEGA_V2 * N, 1e-12);
  }
}
