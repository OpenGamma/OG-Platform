/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.pnl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.opengamma.financial.greeks.Greek;
import com.opengamma.financial.riskfactor.RiskFactorResult;
import com.opengamma.financial.sensitivity.Sensitivity;
import com.opengamma.financial.sensitivity.ValueGreek;
import com.opengamma.financial.sensitivity.ValueGreekSensitivity;
import com.opengamma.math.function.Function1D;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.fast.DateTimeNumericEncoding;
import com.opengamma.util.timeseries.fast.longint.FastArrayLongDoubleTimeSeries;
import com.opengamma.util.timeseries.fast.longint.FastLongDoubleTimeSeries;

/**
 *
 */
public class SensitivityPnLCalculatorTest {
  private static final ValueGreekSensitivity DELTA = new ValueGreekSensitivity(new ValueGreek(Greek.DELTA), "A");
  private static final RiskFactorResult DELTA_RESULT = new RiskFactorResult(100.);
  private static final ValueGreekSensitivity GAMMA = new ValueGreekSensitivity(new ValueGreek(Greek.GAMMA), "A");
  private static final RiskFactorResult GAMMA_RESULT = new RiskFactorResult(200.);
  private static final ValueGreekSensitivity VEGA = new ValueGreekSensitivity(new ValueGreek(Greek.VEGA), "A");
  private static final RiskFactorResult VEGA_RESULT = new RiskFactorResult(300.);
  private static final ValueGreekSensitivity VANNA = new ValueGreekSensitivity(new ValueGreek(Greek.VANNA), "A");
  private static final RiskFactorResult VANNA_RESULT = new RiskFactorResult(400.);
  private static final ValueGreekSensitivity VOMMA = new ValueGreekSensitivity(new ValueGreek(Greek.VOMMA), "A");
  private static final RiskFactorResult VOMMA_RESULT = new RiskFactorResult(500.);
  private static final ValueGreekSensitivity RHO = new ValueGreekSensitivity(new ValueGreek(Greek.RHO), "A");
  private static final RiskFactorResult RHO_RESULT = new RiskFactorResult(600.);
  private static final Map<Sensitivity<?>, Map<Object, DoubleTimeSeries<?>>> TS_RETURNS = new HashMap<Sensitivity<?>, Map<Object, DoubleTimeSeries<?>>>();
  private static final Map<Sensitivity<?>, RiskFactorResult> SENSITIVITIES = new HashMap<Sensitivity<?>, RiskFactorResult>();
  private static final DateTimeNumericEncoding ENCODING = DateTimeNumericEncoding.TIME_EPOCH_MILLIS;
  private static final long[] TIMES = new long[] { 400, 401 };
  private static final double[] TS_SPOT_DATA = new double[] { 0.4, 0.45 };
  private static final double[] TS_IR_DATA = new double[] { 0.3, 0.35 };
  private static final double[] TS_IMP_VOL_DATA = new double[] { 0.7, 0.75 };
  private static final DoubleTimeSeries<?> TS_SPOT = new FastArrayLongDoubleTimeSeries(ENCODING, TIMES, TS_SPOT_DATA);
  private static final DoubleTimeSeries<?> TS_IR = new FastArrayLongDoubleTimeSeries(ENCODING, TIMES, TS_IR_DATA);
  private static final DoubleTimeSeries<?> TS_IMP_VOL = new FastArrayLongDoubleTimeSeries(ENCODING, TIMES, TS_IMP_VOL_DATA);
  private static final PnLDataBundle DATA;
  private static final Function1D<PnLDataBundle, DoubleTimeSeries<?>> CALCULATOR = new SensitivityPnLCalculator();

  static {
    SENSITIVITIES.put(DELTA, DELTA_RESULT);
    SENSITIVITIES.put(GAMMA, GAMMA_RESULT);
    SENSITIVITIES.put(RHO, RHO_RESULT);
    SENSITIVITIES.put(VANNA, VANNA_RESULT);
    SENSITIVITIES.put(VEGA, VEGA_RESULT);
    SENSITIVITIES.put(VOMMA, VOMMA_RESULT);

    Map<Object, DoubleTimeSeries<?>> data = new HashMap<Object, DoubleTimeSeries<?>>();
    data.put(UnderlyingType.SPOT_PRICE, TS_SPOT);
    TS_RETURNS.put(DELTA, data);
    TS_RETURNS.put(GAMMA, data);
    data = new HashMap<Object, DoubleTimeSeries<?>>();
    data.put(UnderlyingType.INTEREST_RATE, TS_IR);
    TS_RETURNS.put(RHO, data);
    data = new HashMap<Object, DoubleTimeSeries<?>>();
    data.put(UnderlyingType.SPOT_PRICE, TS_SPOT);
    data.put(UnderlyingType.IMPLIED_VOLATILITY, TS_IMP_VOL);
    TS_RETURNS.put(VANNA, data);
    data = new HashMap<Object, DoubleTimeSeries<?>>();
    data.put(UnderlyingType.IMPLIED_VOLATILITY, TS_IMP_VOL);
    TS_RETURNS.put(VEGA, data);
    data = new HashMap<Object, DoubleTimeSeries<?>>();
    data.put(UnderlyingType.IMPLIED_VOLATILITY, TS_IMP_VOL);
    TS_RETURNS.put(VOMMA, data);
    DATA = new PnLDataBundle(SENSITIVITIES, TS_RETURNS);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNull() {
    CALCULATOR.evaluate((PnLDataBundle) null);
  }

  @Test
  public void test() {
    final DoubleTimeSeries<?> ts = CALCULATOR.evaluate(DATA);
    assertTrue(ts instanceof FastLongDoubleTimeSeries);
    final FastLongDoubleTimeSeries pnl = ts.toFastLongDoubleTimeSeries();
    assertEquals(pnl.getEncoding(), ENCODING);
    assertEquals(pnl.getTime(0).longValue(), TIMES[0]);
    assertEquals(pnl.getTime(1).longValue(), TIMES[1]);
    assertEquals(pnl.getValueAt(0), 680.5, 1e-9);
    assertEquals(pnl.getValueAt(1), 775.875, 1e-9);
  }
}
