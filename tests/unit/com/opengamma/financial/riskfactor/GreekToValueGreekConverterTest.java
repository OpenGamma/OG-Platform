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
import com.opengamma.financial.sensitivity.Sensitivity;
import com.opengamma.math.function.Function1D;

/**
 * @author emcleod
 *
 */
public class GreekToValueGreekConverterTest {
  private static final double N = 123.;
  private static final double PV = 456;
  private static final SingleGreekResult DELTA = new SingleGreekResult(0.12);
  private static final SingleGreekResult GAMMA = new SingleGreekResult(0.34);
  private static final SingleGreekResult VANNA = new SingleGreekResult(0.56);
  private static final Double VEGA_V1 = 0.78;
  private static final Double VEGA_V2 = 0.9;
  private static final String VEGA_1 = "V1";
  private static final String VEGA_2 = "V2";
  private static final double SPOT_PRICE = 100;
  private static final double SPOT_VOLATILITY = 0.5;
  private static final Function1D<RiskFactorDataBundle, RiskFactorResultCollection> CONVERTER = new GreekToValueGreekConverter();
  private static final GreekResultCollection GREEKS;
  private static final RiskFactorDataBundle DATA;
  private static final double EPS = 1e-7;

  static {
    GREEKS = new GreekResultCollection();
    GREEKS.put(Greek.DELTA, DELTA);
    GREEKS.put(Greek.GAMMA, GAMMA);
    GREEKS.put(Greek.VANNA, VANNA);
    final Map<String, Double> vega = new HashMap<String, Double>();
    vega.put(VEGA_1, VEGA_V1);
    vega.put(VEGA_2, VEGA_V2);
    GREEKS.put(Greek.VEGA, new MultipleGreekResult(vega));
    final Map<Greek, Map<Object, Double>> map = new HashMap<Greek, Map<Object, Double>>();
    final Map<Object, Double> m1 = new HashMap<Object, Double>();
    final Map<Object, Double> m2 = new HashMap<Object, Double>();
    final Map<Object, Double> m3 = new HashMap<Object, Double>();
    m1.put(Underlying.SPOT_PRICE, SPOT_PRICE);
    m1.put(OptionTradeData.NUMBER_OF_CONTRACTS, N);
    m1.put(OptionTradeData.POINT_VALUE, PV);
    m2.put(Underlying.SPOT_VOLATILITY, SPOT_VOLATILITY);
    m2.put(OptionTradeData.NUMBER_OF_CONTRACTS, N);
    m2.put(OptionTradeData.POINT_VALUE, PV);
    m3.put(Underlying.SPOT_PRICE, SPOT_PRICE);
    m3.put(Underlying.SPOT_VOLATILITY, SPOT_VOLATILITY);
    m3.put(OptionTradeData.NUMBER_OF_CONTRACTS, N);
    m3.put(OptionTradeData.POINT_VALUE, PV);
    map.put(Greek.DELTA, m1);
    map.put(Greek.GAMMA, m1);
    map.put(Greek.VEGA, m2);
    map.put(Greek.VANNA, m3);
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
    assertEquals((Double) result.get(Sensitivity.VALUE_DELTA).getResult(), DELTA.getResult() * N * PV * SPOT_PRICE, EPS);
    assertEquals((Double) result.get(Sensitivity.VALUE_GAMMA).getResult(), GAMMA.getResult() * N * PV * SPOT_PRICE * SPOT_PRICE, EPS);
    assertEquals((Double) result.get(Sensitivity.VALUE_VANNA).getResult(), VANNA.getResult() * N * PV * SPOT_PRICE * SPOT_VOLATILITY, EPS);
    assertTrue(result.get(Sensitivity.VALUE_VEGA) instanceof MultipleRiskFactorResult);
    final Map<Object, Double> m = ((MultipleRiskFactorResult) result.get(Sensitivity.VALUE_VEGA)).getResult();
    assertEquals(m.size(), 2);
    assertEquals(m.get(VEGA_1), VEGA_V1 * N * PV * SPOT_VOLATILITY, EPS);
    assertEquals(m.get(VEGA_2), VEGA_V2 * N * PV * SPOT_VOLATILITY, EPS);
  }
}
