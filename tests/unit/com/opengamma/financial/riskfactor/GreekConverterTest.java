/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.riskfactor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.opengamma.financial.greeks.Greek;
import com.opengamma.financial.greeks.GreekResult;
import com.opengamma.financial.greeks.GreekResultCollection;
import com.opengamma.financial.greeks.MultipleGreekResult;
import com.opengamma.financial.greeks.SingleGreekResult;
import com.opengamma.financial.pnl.TradeData;
import com.opengamma.financial.pnl.Underlying;
import com.opengamma.financial.sensitivity.Sensitivity;
import com.opengamma.financial.sensitivity.ValueGreek;
import com.opengamma.math.function.Function1D;

/**
 *
 */
public class GreekConverterTest {
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
  private static final double IMPLIED_VOLATILITY = 0.5;
  private static final Function1D<GreekDataBundle, Map<PositionGreek, RiskFactorResult<?>>> G_TO_PG_CONVERTER = new GreekToPositionGreekConverter();
  private static final Function1D<GreekDataBundle, Map<Sensitivity<Greek>, RiskFactorResult<?>>> G_TO_VG_CONVERTER = new GreekToValueGreekConverter();
  private static final Function1D<PositionGreekDataBundle, Map<Sensitivity<Greek>, RiskFactorResult<?>>> PG_TO_VG_CONVERTER = new PositionGreekToValueGreekConverter();
  private static final GreekResultCollection GREEKS;
  private static final GreekDataBundle GREEKS_DATA;
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
    m1.put(TradeData.NUMBER_OF_CONTRACTS, N);
    m1.put(TradeData.OPTION_POINT_VALUE, PV);
    m2.put(Underlying.IMPLIED_VOLATILITY, IMPLIED_VOLATILITY);
    m2.put(TradeData.NUMBER_OF_CONTRACTS, N);
    m2.put(TradeData.OPTION_POINT_VALUE, PV);
    m3.put(Underlying.SPOT_PRICE, SPOT_PRICE);
    m3.put(Underlying.IMPLIED_VOLATILITY, IMPLIED_VOLATILITY);
    m3.put(TradeData.NUMBER_OF_CONTRACTS, N);
    m3.put(TradeData.OPTION_POINT_VALUE, PV);
    map.put(Greek.DELTA, m1);
    map.put(Greek.GAMMA, m1);
    map.put(Greek.VEGA, m2);
    map.put(Greek.VANNA, m3);
    GREEKS_DATA = new GreekDataBundle(GREEKS, map);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNull1() {
    G_TO_PG_CONVERTER.evaluate((GreekDataBundle) null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNull2() {
    G_TO_VG_CONVERTER.evaluate((GreekDataBundle) null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNull3() {
    PG_TO_VG_CONVERTER.evaluate((PositionGreekDataBundle) null);
  }

  @Test
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
    final Map<Greek, Map<Object, Double>> data = Collections.<Greek, Map<Object, Double>> singletonMap(Greek.DELTA, Collections.<Object, Double> singletonMap(
        TradeData.NUMBER_OF_CONTRACTS, N));
    try {
      G_TO_PG_CONVERTER.evaluate(new GreekDataBundle(greeks, data));
      fail();
    } catch (final IllegalArgumentException e) {
      // expected
    }
    try {
      G_TO_VG_CONVERTER.evaluate(new GreekDataBundle(greeks, data));
      fail();
    } catch (final IllegalArgumentException e) {
      // expected
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void testRiskFactorResultType() {
    final RiskFactorResult<?> temp = new RiskFactorResult<Double>() {

      @Override
      public Double getResult() {
        return 5.;
      }

      @Override
      public boolean isMultiValued() {
        return true;
      }

    };
    final Map<PositionGreek, RiskFactorResult<?>> riskFactors = Collections.<PositionGreek, RiskFactorResult<?>> singletonMap(new PositionGreek(Greek.DELTA), temp);
    final Map<Greek, Map<Object, Double>> data = Collections.<Greek, Map<Object, Double>> singletonMap(Greek.DELTA, Collections.<Object, Double> singletonMap(
        TradeData.NUMBER_OF_CONTRACTS, N));
    PG_TO_VG_CONVERTER.evaluate(new PositionGreekDataBundle(riskFactors, data));
  }

  @Test
  public void test() {
    final Map<PositionGreek, RiskFactorResult<?>> positionGreeks = G_TO_PG_CONVERTER.evaluate(GREEKS_DATA);
    assertEquals((Double) positionGreeks.get(new PositionGreek(Greek.DELTA)).getResult(), DELTA.getResult() * N, EPS);
    assertEquals((Double) positionGreeks.get(new PositionGreek(Greek.GAMMA)).getResult(), GAMMA.getResult() * N, EPS);
    assertEquals((Double) positionGreeks.get(new PositionGreek(Greek.VANNA)).getResult(), VANNA.getResult() * N, EPS);
    assertTrue(positionGreeks.get(new PositionGreek(Greek.VEGA)) instanceof MultipleRiskFactorResult);
    Map<Object, Double> m = ((MultipleRiskFactorResult) positionGreeks.get(new PositionGreek(Greek.VEGA))).getResult();
    assertEquals(m.size(), 2);
    assertEquals(m.get(VEGA_1), VEGA_V1 * N, EPS);
    assertEquals(m.get(VEGA_2), VEGA_V2 * N, EPS);
    final Map<Sensitivity<Greek>, RiskFactorResult<?>> valueGreeks = G_TO_VG_CONVERTER.evaluate(GREEKS_DATA);
    assertEquals((Double) valueGreeks.get(new ValueGreek(Greek.DELTA)).getResult(), DELTA.getResult() * N * PV * SPOT_PRICE, EPS);
    assertEquals((Double) valueGreeks.get(new ValueGreek(Greek.GAMMA)).getResult(), GAMMA.getResult() * N * PV * SPOT_PRICE * SPOT_PRICE, EPS);
    assertEquals((Double) valueGreeks.get(new ValueGreek(Greek.VANNA)).getResult(), VANNA.getResult() * N * PV * SPOT_PRICE * IMPLIED_VOLATILITY, EPS);
    assertTrue(valueGreeks.get(new ValueGreek(Greek.VEGA)) instanceof MultipleRiskFactorResult);
    m = ((MultipleRiskFactorResult) valueGreeks.get(new ValueGreek(Greek.VEGA))).getResult();
    assertEquals(m.size(), 2);
    assertEquals(m.get(VEGA_1), VEGA_V1 * N * PV * IMPLIED_VOLATILITY, EPS);
    assertEquals(m.get(VEGA_2), VEGA_V2 * N * PV * IMPLIED_VOLATILITY, EPS);
    final Map<Sensitivity<Greek>, RiskFactorResult<?>> valueGreeksFromPositionGreeks = PG_TO_VG_CONVERTER.evaluate(new PositionGreekDataBundle(positionGreeks, GREEKS_DATA
        .getAllUnderlyingData()));
    assertEquals(valueGreeksFromPositionGreeks, valueGreeks);
  }
}
