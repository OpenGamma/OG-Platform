/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.pnl;

import static org.testng.AssertJUnit.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.greeks.Greek;
import com.opengamma.analytics.financial.sensitivity.ValueGreek;
import com.opengamma.analytics.financial.sensitivity.ValueGreekSensitivity;
import com.opengamma.analytics.math.function.Function;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.precise.PreciseDoubleTimeSeries;
import com.opengamma.timeseries.precise.instant.ImmutableInstantDoubleTimeSeries;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class SensitivityPnLCalculatorTest {
  private static final ValueGreekSensitivity DELTA = new ValueGreekSensitivity(new ValueGreek(Greek.DELTA), "A");
  private static final double DELTA_VALUE = 100.;
  private static final ValueGreekSensitivity GAMMA = new ValueGreekSensitivity(new ValueGreek(Greek.GAMMA), "A");
  private static final double GAMMA_VALUE = 200.;
  private static final ValueGreekSensitivity VEGA = new ValueGreekSensitivity(new ValueGreek(Greek.VEGA), "A");
  private static final double VEGA_VALUE = 300.;
  private static final ValueGreekSensitivity VANNA = new ValueGreekSensitivity(new ValueGreek(Greek.VANNA), "A");
  private static final double VANNA_VALUE = 400.;
  private static final ValueGreekSensitivity VOMMA = new ValueGreekSensitivity(new ValueGreek(Greek.VOMMA), "A");
  private static final double VOMMA_VALUE = 500.;
  private static final ValueGreekSensitivity RHO = new ValueGreekSensitivity(new ValueGreek(Greek.RHO), "A");
  private static final double RHO_VALUE = 600.;
  private static final long[] TIMES = new long[] {400, 401};
  private static final double[] TS_SPOT_DATA = new double[] {0.4, 0.45};
  private static final double[] TS_IR_DATA = new double[] {0.3, 0.35};
  private static final double[] TS_IMP_VOL_DATA = new double[] {0.7, 0.75};
  private static final DoubleTimeSeries<?> TS_SPOT = ImmutableInstantDoubleTimeSeries.of(TIMES, TS_SPOT_DATA);
  private static final DoubleTimeSeries<?> TS_IR = ImmutableInstantDoubleTimeSeries.of(TIMES, TS_IR_DATA);
  private static final DoubleTimeSeries<?> TS_IMP_VOL = ImmutableInstantDoubleTimeSeries.of(TIMES, TS_IMP_VOL_DATA);
  private static final SensitivityAndReturnDataBundle[] DATA;
  private static final Function<SensitivityAndReturnDataBundle, DoubleTimeSeries<?>> CALCULATOR = new SensitivityPnLCalculator();

  static {
    DATA = new SensitivityAndReturnDataBundle[6];
    final Map<UnderlyingType, DoubleTimeSeries<?>> m1 = new HashMap<>();
    m1.put(UnderlyingType.SPOT_PRICE, TS_SPOT);
    DATA[0] = new SensitivityAndReturnDataBundle(DELTA, DELTA_VALUE, m1);
    DATA[1] = new SensitivityAndReturnDataBundle(GAMMA, GAMMA_VALUE, m1);
    final Map<UnderlyingType, DoubleTimeSeries<?>> m2 = new HashMap<>();
    m2.put(UnderlyingType.IMPLIED_VOLATILITY, TS_IMP_VOL);
    DATA[2] = new SensitivityAndReturnDataBundle(VEGA, VEGA_VALUE, m2);
    final Map<UnderlyingType, DoubleTimeSeries<?>> m3 = new HashMap<>();
    m3.put(UnderlyingType.SPOT_PRICE, TS_SPOT);
    m3.put(UnderlyingType.IMPLIED_VOLATILITY, TS_IMP_VOL);
    DATA[3] = new SensitivityAndReturnDataBundle(VANNA, VANNA_VALUE, m3);
    DATA[4] = new SensitivityAndReturnDataBundle(VOMMA, VOMMA_VALUE, m2);
    final Map<UnderlyingType, DoubleTimeSeries<?>> m4 = new HashMap<>();
    m4.put(UnderlyingType.INTEREST_RATE, TS_IR);
    DATA[5] = new SensitivityAndReturnDataBundle(RHO, RHO_VALUE, m4);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData() {
    CALCULATOR.evaluate((SensitivityAndReturnDataBundle[]) null);
  }

  @Test
  public void test() {
    final DoubleTimeSeries<?> ts = CALCULATOR.evaluate(DATA);
    final PreciseDoubleTimeSeries<?> pnl = (PreciseDoubleTimeSeries<?>) ts;
    assertEquals(pnl.getTimeAtIndexFast(0), TIMES[0]);
    assertEquals(pnl.getTimeAtIndexFast(1), TIMES[1]);
    assertEquals(pnl.getValueAtIndexFast(0), 680.5, 1e-9);
    assertEquals(pnl.getValueAtIndexFast(1), 775.875, 1e-9);
  }
}
