/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.var.parametric;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.covariance.CovarianceCalculator;
import com.opengamma.analytics.financial.covariance.CovarianceMatrixCalculator;
import com.opengamma.analytics.financial.greeks.Greek;
import com.opengamma.analytics.financial.greeks.GreekVisitor;
import com.opengamma.analytics.financial.greeks.MixedOrderUnderlying;
import com.opengamma.analytics.financial.greeks.NthOrderUnderlying;
import com.opengamma.analytics.financial.greeks.Underlying;
import com.opengamma.analytics.financial.pnl.SensitivityAndReturnDataBundle;
import com.opengamma.analytics.financial.pnl.UnderlyingType;
import com.opengamma.analytics.financial.sensitivity.Sensitivity;
import com.opengamma.analytics.financial.sensitivity.ValueGreek;
import com.opengamma.analytics.financial.sensitivity.ValueGreekSensitivity;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.math.matrix.Matrix;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.precise.instant.ImmutableInstantDoubleTimeSeries;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class VaRCovarianceMatrixCalculatorTest {
  private static final CovarianceCalculator COVARIANCE = new CovarianceCalculator() {

    @Override
    public Double evaluate(final DoubleTimeSeries<?>... x) {
      return x[0].getLatestValue() * x[1].getLatestValue();
    }

  };
  private static final CovarianceMatrixCalculator COVARIANCE_MATRIX = new CovarianceMatrixCalculator(COVARIANCE);
  private static final VaRCovarianceMatrixCalculator CALC = new VaRCovarianceMatrixCalculator(COVARIANCE_MATRIX);
  private static final SensitivityAndReturnDataBundle[] A_DATA_FIRST_ORDER_1;
  private static final SensitivityAndReturnDataBundle[] A_DATA_FIRST_ORDER_2;
  private static final SensitivityAndReturnDataBundle[] DATA_FIRST_ORDER;
  private static final SensitivityAndReturnDataBundle[] A_DATA_SECOND_ORDER_1;
  private static final SensitivityAndReturnDataBundle[] A_DATA_SECOND_ORDER_2;
  private static final SensitivityAndReturnDataBundle[] A_DATA_SECOND_ORDER_3;
  private static final SensitivityAndReturnDataBundle[] DATA_SECOND_ORDER;
  private static final SensitivityAndReturnDataBundle[] DATA;
  private static final double VEGA_A_1 = 53;
  private static final double DELTA_A_1 = 59;
  private static final double RHO_A_1 = 67;
  private static final double RHO_A_2 = 101;
  private static final double DELTA_A_2 = 107;
  private static final double RHO_B = 127;
  private static final double DELTA_B = 131;
  private static final double VEGA_B = 137;
  private static final double DELTA_C = 139;
  private static final double VEGA_C = 149;
  private static final double VARIANCE_VEGA_C = 3;
  private static final double GAMMA_A_1 = 71;
  private static final double VOMMA_A_1 = 79;
  private static final double STRIKE_GAMMA_A_1 = 83;
  private static final double GAMMA_A_2 = 89;
  private static final double VOMMA_A_2 = 97;
  private static final double DELTA_BLEED_A = 191;
  private static final double DUMMY_A = 200;
  private static final double VANNA_A = 193;
  private static final double GAMMA_B = 197;
  private static final double VOMMA_B = 199;
  private static final double GAMMA_C = 227;
  private static final double VANNA_C = 229;
  private static final double VOL_A_RETURN = 5;
  private static final double SPOT_A_RETURN = 7;
  private static final double R_A_RETURN = 11;
  private static final double R_B_RETURN = 13;
  private static final double SPOT_B_RETURN = 17;
  private static final double VOL_B_RETURN = 19;
  private static final double SPOT_C_RETURN = 23;
  private static final double VOL_C_RETURN = 29;
  private static final double VARIANCE_C_RETURN = 31;
  private static final double STRIKE_A_RETURN = 43;
  private static final double TIME_A_RETURN = 47;
  private static final double DUMMY_A_RETURN = 34;
  private static final double[][] C1 = new double[][] {new double[] {VOL_A_RETURN * VOL_A_RETURN, VOL_A_RETURN * SPOT_A_RETURN, VOL_A_RETURN * R_A_RETURN},
      new double[] {SPOT_A_RETURN * VOL_A_RETURN, SPOT_A_RETURN * SPOT_A_RETURN, SPOT_A_RETURN * R_A_RETURN},
      new double[] {R_A_RETURN * VOL_A_RETURN, R_A_RETURN * SPOT_A_RETURN, R_A_RETURN * R_A_RETURN}};
  private static final double[][] C2 = new double[][] {
      new double[] {VOL_A_RETURN * VOL_A_RETURN, VOL_A_RETURN * SPOT_A_RETURN, VOL_A_RETURN * R_A_RETURN, VOL_A_RETURN * R_B_RETURN, VOL_A_RETURN * SPOT_B_RETURN, VOL_A_RETURN * VOL_B_RETURN,
          VOL_A_RETURN * SPOT_C_RETURN, VOL_A_RETURN * VOL_C_RETURN, VOL_A_RETURN * VARIANCE_C_RETURN},
      new double[] {SPOT_A_RETURN * VOL_A_RETURN, SPOT_A_RETURN * SPOT_A_RETURN, SPOT_A_RETURN * R_A_RETURN, SPOT_A_RETURN * R_B_RETURN, SPOT_A_RETURN * SPOT_B_RETURN, SPOT_A_RETURN * VOL_B_RETURN,
          SPOT_A_RETURN * SPOT_C_RETURN, SPOT_A_RETURN * VOL_C_RETURN, SPOT_A_RETURN * VARIANCE_C_RETURN},
      new double[] {R_A_RETURN * VOL_A_RETURN, R_A_RETURN * SPOT_A_RETURN, R_A_RETURN * R_A_RETURN, R_A_RETURN * R_B_RETURN, R_A_RETURN * SPOT_B_RETURN, R_A_RETURN * VOL_B_RETURN,
          R_A_RETURN * SPOT_C_RETURN, R_A_RETURN * VOL_C_RETURN, R_A_RETURN * VARIANCE_C_RETURN},
      new double[] {R_B_RETURN * VOL_A_RETURN, R_B_RETURN * SPOT_A_RETURN, R_B_RETURN * R_A_RETURN, R_B_RETURN * R_B_RETURN, R_B_RETURN * SPOT_B_RETURN, R_B_RETURN * VOL_B_RETURN,
          R_B_RETURN * SPOT_C_RETURN, R_B_RETURN * VOL_C_RETURN, R_B_RETURN * VARIANCE_C_RETURN},
      new double[] {SPOT_B_RETURN * VOL_A_RETURN, SPOT_B_RETURN * SPOT_A_RETURN, SPOT_B_RETURN * R_A_RETURN, SPOT_B_RETURN * R_B_RETURN, SPOT_B_RETURN * SPOT_B_RETURN, SPOT_B_RETURN * VOL_B_RETURN,
          SPOT_B_RETURN * SPOT_C_RETURN, SPOT_B_RETURN * VOL_C_RETURN, SPOT_B_RETURN * VARIANCE_C_RETURN},
      new double[] {VOL_B_RETURN * VOL_A_RETURN, VOL_B_RETURN * SPOT_A_RETURN, VOL_B_RETURN * R_A_RETURN, VOL_B_RETURN * R_B_RETURN, VOL_B_RETURN * SPOT_B_RETURN, VOL_B_RETURN * VOL_B_RETURN,
          VOL_B_RETURN * SPOT_C_RETURN, VOL_B_RETURN * VOL_C_RETURN, VOL_B_RETURN * VARIANCE_C_RETURN},
      new double[] {SPOT_C_RETURN * VOL_A_RETURN, SPOT_C_RETURN * SPOT_A_RETURN, SPOT_C_RETURN * R_A_RETURN, SPOT_C_RETURN * R_B_RETURN, SPOT_C_RETURN * SPOT_B_RETURN, SPOT_C_RETURN * VOL_B_RETURN,
          SPOT_C_RETURN * SPOT_C_RETURN, SPOT_C_RETURN * VOL_C_RETURN, SPOT_C_RETURN * VARIANCE_C_RETURN},
      new double[] {VOL_C_RETURN * VOL_A_RETURN, VOL_C_RETURN * SPOT_A_RETURN, VOL_C_RETURN * R_A_RETURN, VOL_C_RETURN * R_B_RETURN, VOL_C_RETURN * SPOT_B_RETURN, VOL_C_RETURN * VOL_B_RETURN,
          VOL_C_RETURN * SPOT_C_RETURN, VOL_C_RETURN * VOL_C_RETURN, VOL_C_RETURN * VARIANCE_C_RETURN},
      new double[] {VARIANCE_C_RETURN * VOL_A_RETURN, VARIANCE_C_RETURN * SPOT_A_RETURN, VARIANCE_C_RETURN * R_A_RETURN, VARIANCE_C_RETURN * R_B_RETURN, VARIANCE_C_RETURN * SPOT_B_RETURN,
          VARIANCE_C_RETURN * VOL_B_RETURN, VARIANCE_C_RETURN * SPOT_C_RETURN, VARIANCE_C_RETURN * VOL_C_RETURN, VARIANCE_C_RETURN * VARIANCE_C_RETURN}};
  private static final double[][] C3 = new double[][] {new double[] {SPOT_A_RETURN * SPOT_A_RETURN, SPOT_A_RETURN * VOL_A_RETURN, SPOT_A_RETURN * STRIKE_A_RETURN},
      new double[] {VOL_A_RETURN * SPOT_A_RETURN, VOL_A_RETURN * VOL_A_RETURN, VOL_A_RETURN * STRIKE_A_RETURN},
      new double[] {STRIKE_A_RETURN * SPOT_A_RETURN, STRIKE_A_RETURN * VOL_A_RETURN, STRIKE_A_RETURN * STRIKE_A_RETURN}};
  private static final double[][] C4 = new double[][] {new double[] {SPOT_A_RETURN * SPOT_A_RETURN, SPOT_A_RETURN * VOL_A_RETURN, SPOT_A_RETURN * STRIKE_A_RETURN, SPOT_A_RETURN * TIME_A_RETURN},
      new double[] {VOL_A_RETURN * SPOT_A_RETURN, VOL_A_RETURN * VOL_A_RETURN, VOL_A_RETURN * STRIKE_A_RETURN, VOL_A_RETURN * TIME_A_RETURN},
      new double[] {STRIKE_A_RETURN * SPOT_A_RETURN, STRIKE_A_RETURN * VOL_A_RETURN, STRIKE_A_RETURN * STRIKE_A_RETURN, STRIKE_A_RETURN * TIME_A_RETURN},
      new double[] {TIME_A_RETURN * SPOT_A_RETURN, TIME_A_RETURN * VOL_A_RETURN, TIME_A_RETURN * STRIKE_A_RETURN, TIME_A_RETURN * TIME_A_RETURN}};
  private static final double[][] C5 = new double[][] {
      new double[] {SPOT_A_RETURN * SPOT_A_RETURN, SPOT_A_RETURN * VOL_A_RETURN, SPOT_A_RETURN * STRIKE_A_RETURN, SPOT_A_RETURN * TIME_A_RETURN, SPOT_A_RETURN * SPOT_B_RETURN,
          SPOT_A_RETURN * VOL_B_RETURN, SPOT_A_RETURN * SPOT_C_RETURN, SPOT_A_RETURN * VOL_C_RETURN, SPOT_A_RETURN * DUMMY_A_RETURN},
      new double[] {VOL_A_RETURN * SPOT_A_RETURN, VOL_A_RETURN * VOL_A_RETURN, VOL_A_RETURN * STRIKE_A_RETURN, VOL_A_RETURN * TIME_A_RETURN, VOL_A_RETURN * SPOT_B_RETURN, VOL_A_RETURN * VOL_B_RETURN,
          VOL_A_RETURN * SPOT_C_RETURN, VOL_A_RETURN * VOL_C_RETURN, VOL_A_RETURN * DUMMY_A_RETURN},
      new double[] {STRIKE_A_RETURN * SPOT_A_RETURN, STRIKE_A_RETURN * VOL_A_RETURN, STRIKE_A_RETURN * STRIKE_A_RETURN, STRIKE_A_RETURN * TIME_A_RETURN, STRIKE_A_RETURN * SPOT_B_RETURN,
          STRIKE_A_RETURN * VOL_B_RETURN, STRIKE_A_RETURN * SPOT_C_RETURN, STRIKE_A_RETURN * VOL_C_RETURN, STRIKE_A_RETURN * DUMMY_A_RETURN},
      new double[] {TIME_A_RETURN * SPOT_A_RETURN, TIME_A_RETURN * VOL_A_RETURN, TIME_A_RETURN * STRIKE_A_RETURN, TIME_A_RETURN * TIME_A_RETURN, TIME_A_RETURN * SPOT_B_RETURN,
          TIME_A_RETURN * VOL_B_RETURN, TIME_A_RETURN * SPOT_C_RETURN, TIME_A_RETURN * VOL_C_RETURN, TIME_A_RETURN * DUMMY_A_RETURN},
      new double[] {SPOT_B_RETURN * SPOT_A_RETURN, SPOT_B_RETURN * VOL_A_RETURN, SPOT_B_RETURN * STRIKE_A_RETURN, SPOT_B_RETURN * TIME_A_RETURN, SPOT_B_RETURN * SPOT_B_RETURN,
          SPOT_B_RETURN * VOL_B_RETURN, SPOT_B_RETURN * SPOT_C_RETURN, SPOT_B_RETURN * VOL_C_RETURN, SPOT_B_RETURN * DUMMY_A_RETURN},
      new double[] {VOL_B_RETURN * SPOT_A_RETURN, VOL_B_RETURN * VOL_A_RETURN, VOL_B_RETURN * STRIKE_A_RETURN, VOL_B_RETURN * TIME_A_RETURN, VOL_B_RETURN * SPOT_B_RETURN, VOL_B_RETURN * VOL_B_RETURN,
          VOL_B_RETURN * SPOT_C_RETURN, VOL_B_RETURN * VOL_C_RETURN, VOL_B_RETURN * DUMMY_A_RETURN},
      new double[] {SPOT_C_RETURN * SPOT_A_RETURN, SPOT_C_RETURN * VOL_A_RETURN, SPOT_C_RETURN * STRIKE_A_RETURN, SPOT_C_RETURN * TIME_A_RETURN, SPOT_C_RETURN * SPOT_B_RETURN,
          SPOT_C_RETURN * VOL_B_RETURN, SPOT_C_RETURN * SPOT_C_RETURN, SPOT_C_RETURN * VOL_C_RETURN, SPOT_C_RETURN * DUMMY_A_RETURN},
      new double[] {VOL_C_RETURN * SPOT_A_RETURN, VOL_C_RETURN * VOL_A_RETURN, VOL_C_RETURN * STRIKE_A_RETURN, VOL_C_RETURN * TIME_A_RETURN, VOL_C_RETURN * SPOT_B_RETURN, VOL_C_RETURN * VOL_B_RETURN,
          VOL_C_RETURN * SPOT_C_RETURN, VOL_C_RETURN * VOL_C_RETURN, VOL_C_RETURN * DUMMY_A_RETURN},
      new double[] {DUMMY_A_RETURN * SPOT_A_RETURN, DUMMY_A_RETURN * VOL_A_RETURN, DUMMY_A_RETURN * STRIKE_A_RETURN, DUMMY_A_RETURN * TIME_A_RETURN, DUMMY_A_RETURN * SPOT_B_RETURN,
          DUMMY_A_RETURN * VOL_B_RETURN, DUMMY_A_RETURN * SPOT_C_RETURN, DUMMY_A_RETURN * VOL_C_RETURN, DUMMY_A_RETURN * DUMMY_A_RETURN}};

  private static final double EPS = 1e-15;

  static {
    final String name1 = "A";
    final String name2 = "B";
    final String name3 = "C";
    final long[] t = new long[] {1};
    final DoubleTimeSeries<?> volATS = ImmutableInstantDoubleTimeSeries.of(t, new double[] {VOL_A_RETURN});
    final DoubleTimeSeries<?> spotATS = ImmutableInstantDoubleTimeSeries.of(t, new double[] {SPOT_A_RETURN});
    final DoubleTimeSeries<?> rATS = ImmutableInstantDoubleTimeSeries.of(t, new double[] {R_A_RETURN});
    final DoubleTimeSeries<?> rBTS = ImmutableInstantDoubleTimeSeries.of(t, new double[] {R_B_RETURN});
    final DoubleTimeSeries<?> spotBTS = ImmutableInstantDoubleTimeSeries.of(t, new double[] {SPOT_B_RETURN});
    final DoubleTimeSeries<?> volBTS = ImmutableInstantDoubleTimeSeries.of(t, new double[] {VOL_B_RETURN});
    final DoubleTimeSeries<?> spotCTS = ImmutableInstantDoubleTimeSeries.of(t, new double[] {SPOT_C_RETURN});
    final DoubleTimeSeries<?> volCTS = ImmutableInstantDoubleTimeSeries.of(t, new double[] {VOL_C_RETURN});
    final DoubleTimeSeries<?> varianceCTS = ImmutableInstantDoubleTimeSeries.of(t, new double[] {VARIANCE_C_RETURN});
    final DoubleTimeSeries<?> strikeATS = ImmutableInstantDoubleTimeSeries.of(t, new double[] {STRIKE_A_RETURN});
    final DoubleTimeSeries<?> timeATS = ImmutableInstantDoubleTimeSeries.of(t, new double[] {TIME_A_RETURN});
    final DoubleTimeSeries<?> dummyATS = ImmutableInstantDoubleTimeSeries.of(t, new double[] {DUMMY_A_RETURN});
    final Sensitivity<?> rhoA1 = new ValueGreekSensitivity(new ValueGreek(Greek.RHO), name1);
    final Sensitivity<?> deltaA1 = new ValueGreekSensitivity(new ValueGreek(Greek.DELTA), name1);
    final Sensitivity<?> vegaA1 = new ValueGreekSensitivity(new ValueGreek(Greek.VEGA), name1);
    final Sensitivity<?> rhoA2 = new ValueGreekSensitivity(new ValueGreek(Greek.RHO), name1);
    final Sensitivity<?> deltaA2 = new ValueGreekSensitivity(new ValueGreek(Greek.DELTA), name1);
    final Sensitivity<?> rhoB = new ValueGreekSensitivity(new ValueGreek(Greek.RHO), name2);
    final Sensitivity<?> deltaB = new ValueGreekSensitivity(new ValueGreek(Greek.DELTA), name2);
    final Sensitivity<?> vegaB = new ValueGreekSensitivity(new ValueGreek(Greek.VEGA), name2);
    final Sensitivity<?> deltaC = new ValueGreekSensitivity(new ValueGreek(Greek.DELTA), name3);
    final Sensitivity<?> vegaC = new ValueGreekSensitivity(new ValueGreek(Greek.VEGA), name3);
    final Sensitivity<?> varianceVegaC = new ValueGreekSensitivity(new ValueGreek(Greek.VARIANCE_VEGA), name3);
    final Sensitivity<?> gammaA1 = new ValueGreekSensitivity(new ValueGreek(Greek.GAMMA), name1);
    final Sensitivity<?> vommaA1 = new ValueGreekSensitivity(new ValueGreek(Greek.VOMMA), name1);
    final Sensitivity<?> strikeGammaA1 = new ValueGreekSensitivity(new ValueGreek(Greek.STRIKE_GAMMA), name1);
    final Sensitivity<?> gammaA2 = new ValueGreekSensitivity(new ValueGreek(Greek.GAMMA), name1);
    final Sensitivity<?> vommaA2 = new ValueGreekSensitivity(new ValueGreek(Greek.VOMMA), name1);
    final Sensitivity<?> deltaBleedA3 = new ValueGreekSensitivity(new ValueGreek(Greek.DELTA_BLEED), name1);
    final Sensitivity<?> vannaA3 = new ValueGreekSensitivity(new ValueGreek(Greek.VANNA), name1);
    final Sensitivity<?> gammaB = new ValueGreekSensitivity(new ValueGreek(Greek.GAMMA), name2);
    final Sensitivity<?> vommaB = new ValueGreekSensitivity(new ValueGreek(Greek.VOMMA), name2);
    final Sensitivity<?> gammaC = new ValueGreekSensitivity(new ValueGreek(Greek.GAMMA), name3);
    final Sensitivity<?> vannaC = new ValueGreekSensitivity(new ValueGreek(Greek.VANNA), name3);
    final Sensitivity<?> dummyA = new ValueGreekSensitivity(new ValueGreek(new DummyGreek(new MixedOrderUnderlying(Arrays.asList(new NthOrderUnderlying(1, UnderlyingType.YIELD),
        new NthOrderUnderlying(1, UnderlyingType.SPOT_PRICE))), "DUMMY")), name1);
    A_DATA_FIRST_ORDER_1 = new SensitivityAndReturnDataBundle[3];
    A_DATA_FIRST_ORDER_1[0] = new SensitivityAndReturnDataBundle(vegaA1, VEGA_A_1, Collections.<UnderlyingType, DoubleTimeSeries<?>> singletonMap(UnderlyingType.IMPLIED_VOLATILITY, volATS));
    A_DATA_FIRST_ORDER_1[1] = new SensitivityAndReturnDataBundle(deltaA1, DELTA_A_1, Collections.<UnderlyingType, DoubleTimeSeries<?>> singletonMap(UnderlyingType.SPOT_PRICE, spotATS));
    A_DATA_FIRST_ORDER_1[2] = new SensitivityAndReturnDataBundle(rhoA1, RHO_A_1, Collections.<UnderlyingType, DoubleTimeSeries<?>> singletonMap(UnderlyingType.INTEREST_RATE, rATS));
    A_DATA_FIRST_ORDER_2 = new SensitivityAndReturnDataBundle[5];
    A_DATA_FIRST_ORDER_2[0] = A_DATA_FIRST_ORDER_1[0];
    A_DATA_FIRST_ORDER_2[1] = A_DATA_FIRST_ORDER_1[1];
    A_DATA_FIRST_ORDER_2[2] = A_DATA_FIRST_ORDER_1[2];
    A_DATA_FIRST_ORDER_2[3] = new SensitivityAndReturnDataBundle(rhoA2, RHO_A_2, Collections.<UnderlyingType, DoubleTimeSeries<?>> singletonMap(UnderlyingType.INTEREST_RATE, rATS));
    A_DATA_FIRST_ORDER_2[4] = new SensitivityAndReturnDataBundle(deltaA2, DELTA_A_2, Collections.<UnderlyingType, DoubleTimeSeries<?>> singletonMap(UnderlyingType.SPOT_PRICE, spotATS));
    DATA_FIRST_ORDER = new SensitivityAndReturnDataBundle[11];
    DATA_FIRST_ORDER[0] = A_DATA_FIRST_ORDER_1[0];
    DATA_FIRST_ORDER[1] = A_DATA_FIRST_ORDER_1[1];
    DATA_FIRST_ORDER[2] = A_DATA_FIRST_ORDER_1[2];
    DATA_FIRST_ORDER[3] = A_DATA_FIRST_ORDER_2[3];
    DATA_FIRST_ORDER[4] = A_DATA_FIRST_ORDER_2[4];
    DATA_FIRST_ORDER[5] = new SensitivityAndReturnDataBundle(rhoB, RHO_B, Collections.<UnderlyingType, DoubleTimeSeries<?>> singletonMap(UnderlyingType.INTEREST_RATE, rBTS));
    DATA_FIRST_ORDER[6] = new SensitivityAndReturnDataBundle(deltaB, DELTA_B, Collections.<UnderlyingType, DoubleTimeSeries<?>> singletonMap(UnderlyingType.SPOT_PRICE, spotBTS));
    DATA_FIRST_ORDER[7] = new SensitivityAndReturnDataBundle(vegaB, VEGA_B, Collections.<UnderlyingType, DoubleTimeSeries<?>> singletonMap(UnderlyingType.IMPLIED_VOLATILITY, volBTS));
    DATA_FIRST_ORDER[8] = new SensitivityAndReturnDataBundle(deltaC, DELTA_C, Collections.<UnderlyingType, DoubleTimeSeries<?>> singletonMap(UnderlyingType.SPOT_PRICE, spotCTS));
    DATA_FIRST_ORDER[9] = new SensitivityAndReturnDataBundle(vegaC, VEGA_C, Collections.<UnderlyingType, DoubleTimeSeries<?>> singletonMap(UnderlyingType.IMPLIED_VOLATILITY, volCTS));
    DATA_FIRST_ORDER[10] = new SensitivityAndReturnDataBundle(varianceVegaC, VARIANCE_VEGA_C, Collections.<UnderlyingType, DoubleTimeSeries<?>> singletonMap(UnderlyingType.IMPLIED_VARIANCE,
        varianceCTS));
    A_DATA_SECOND_ORDER_1 = new SensitivityAndReturnDataBundle[3];
    A_DATA_SECOND_ORDER_1[0] = new SensitivityAndReturnDataBundle(gammaA1, GAMMA_A_1, Collections.<UnderlyingType, DoubleTimeSeries<?>> singletonMap(UnderlyingType.SPOT_PRICE, spotATS));
    A_DATA_SECOND_ORDER_1[1] = new SensitivityAndReturnDataBundle(vommaA1, VOMMA_A_1, Collections.<UnderlyingType, DoubleTimeSeries<?>> singletonMap(UnderlyingType.IMPLIED_VOLATILITY, volATS));
    A_DATA_SECOND_ORDER_1[2] = new SensitivityAndReturnDataBundle(strikeGammaA1, STRIKE_GAMMA_A_1, Collections.<UnderlyingType, DoubleTimeSeries<?>> singletonMap(UnderlyingType.STRIKE, strikeATS));
    A_DATA_SECOND_ORDER_2 = new SensitivityAndReturnDataBundle[5];
    A_DATA_SECOND_ORDER_2[0] = A_DATA_SECOND_ORDER_1[0];
    A_DATA_SECOND_ORDER_2[1] = A_DATA_SECOND_ORDER_1[1];
    A_DATA_SECOND_ORDER_2[2] = A_DATA_SECOND_ORDER_1[2];
    A_DATA_SECOND_ORDER_2[3] = new SensitivityAndReturnDataBundle(gammaA2, GAMMA_A_2, Collections.<UnderlyingType, DoubleTimeSeries<?>> singletonMap(UnderlyingType.SPOT_PRICE, spotATS));
    A_DATA_SECOND_ORDER_2[4] = new SensitivityAndReturnDataBundle(vommaA2, VOMMA_A_2, Collections.<UnderlyingType, DoubleTimeSeries<?>> singletonMap(UnderlyingType.IMPLIED_VOLATILITY, volATS));
    A_DATA_SECOND_ORDER_3 = new SensitivityAndReturnDataBundle[7];
    A_DATA_SECOND_ORDER_3[0] = A_DATA_SECOND_ORDER_2[0];
    A_DATA_SECOND_ORDER_3[1] = A_DATA_SECOND_ORDER_2[1];
    A_DATA_SECOND_ORDER_3[2] = A_DATA_SECOND_ORDER_2[2];
    A_DATA_SECOND_ORDER_3[3] = A_DATA_SECOND_ORDER_2[3];
    A_DATA_SECOND_ORDER_3[4] = A_DATA_SECOND_ORDER_2[4];
    final Map<UnderlyingType, DoubleTimeSeries<?>> deltaBleedMap = new HashMap<>();
    deltaBleedMap.put(UnderlyingType.SPOT_PRICE, spotATS);
    deltaBleedMap.put(UnderlyingType.TIME, timeATS);
    A_DATA_SECOND_ORDER_3[5] = new SensitivityAndReturnDataBundle(deltaBleedA3, DELTA_BLEED_A, deltaBleedMap);
    final Map<UnderlyingType, DoubleTimeSeries<?>> vannaAMap = new HashMap<>();
    vannaAMap.put(UnderlyingType.SPOT_PRICE, spotATS);
    vannaAMap.put(UnderlyingType.IMPLIED_VOLATILITY, volATS);
    A_DATA_SECOND_ORDER_3[6] = new SensitivityAndReturnDataBundle(vannaA3, VANNA_A, vannaAMap);
    DATA_SECOND_ORDER = new SensitivityAndReturnDataBundle[12];
    DATA_SECOND_ORDER[0] = A_DATA_SECOND_ORDER_3[0];
    DATA_SECOND_ORDER[1] = A_DATA_SECOND_ORDER_3[1];
    DATA_SECOND_ORDER[2] = A_DATA_SECOND_ORDER_3[2];
    DATA_SECOND_ORDER[3] = A_DATA_SECOND_ORDER_3[3];
    DATA_SECOND_ORDER[4] = A_DATA_SECOND_ORDER_3[4];
    DATA_SECOND_ORDER[5] = A_DATA_SECOND_ORDER_3[5];
    DATA_SECOND_ORDER[6] = A_DATA_SECOND_ORDER_3[6];
    DATA_SECOND_ORDER[7] = new SensitivityAndReturnDataBundle(gammaB, GAMMA_B, Collections.<UnderlyingType, DoubleTimeSeries<?>> singletonMap(UnderlyingType.SPOT_PRICE, spotBTS));
    DATA_SECOND_ORDER[8] = new SensitivityAndReturnDataBundle(vommaB, VOMMA_B, Collections.<UnderlyingType, DoubleTimeSeries<?>> singletonMap(UnderlyingType.IMPLIED_VOLATILITY, volBTS));
    DATA_SECOND_ORDER[9] = new SensitivityAndReturnDataBundle(gammaC, GAMMA_C, Collections.<UnderlyingType, DoubleTimeSeries<?>> singletonMap(UnderlyingType.SPOT_PRICE, spotCTS));
    final Map<UnderlyingType, DoubleTimeSeries<?>> vannaCMap = new HashMap<>();
    vannaCMap.put(UnderlyingType.SPOT_PRICE, spotCTS);
    vannaCMap.put(UnderlyingType.IMPLIED_VOLATILITY, volCTS);
    DATA_SECOND_ORDER[10] = new SensitivityAndReturnDataBundle(vannaC, VANNA_C, vannaCMap);
    final Map<UnderlyingType, DoubleTimeSeries<?>> dummyAMap = new HashMap<>();
    dummyAMap.put(UnderlyingType.YIELD, dummyATS);
    dummyAMap.put(UnderlyingType.SPOT_PRICE, spotATS);
    DATA_SECOND_ORDER[11] = new SensitivityAndReturnDataBundle(dummyA, DUMMY_A, dummyAMap);
    DATA = new SensitivityAndReturnDataBundle[23];
    for (int i = 0; i < 11; i++) {
      DATA[i] = DATA_FIRST_ORDER[i];
    }
    for (int i = 11; i < 23; i++) {
      DATA[i] = DATA_SECOND_ORDER[i - 11];
    }
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCalculator() {
    new VaRCovarianceMatrixCalculator(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData() {
    CALC.evaluate((SensitivityAndReturnDataBundle[]) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEmptyData() {
    CALC.evaluate(new SensitivityAndReturnDataBundle[0]);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDataValue() {
    CALC.evaluate(new SensitivityAndReturnDataBundle[] {null});
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testThirdOrderSensitivity() {
    final SensitivityAndReturnDataBundle data = new SensitivityAndReturnDataBundle(new ValueGreekSensitivity(new ValueGreek(Greek.SPEED), "A"), 10,
        Collections.<UnderlyingType, DoubleTimeSeries<?>> singletonMap(UnderlyingType.SPOT_PRICE, ImmutableInstantDoubleTimeSeries.of(new long[] {1},
            new double[] {1})));
    CALC.evaluate(data);
  }

  @Test
  public void testFirstOrderOnlyNoDuplicateGreeks() {
    final ParametricVaRDataBundle result = CALC.evaluate(A_DATA_FIRST_ORDER_1).get(1);
    final List<String> names = result.getNames();
    assertEquals(names, Arrays.asList("A_IMPLIED_VOLATILITY", "A_SPOT_PRICE", "A_INTEREST_RATE"));
    final Matrix<?> sensitivities = result.getSensitivities();
    assertEquals(sensitivities.getClass(), DoubleMatrix1D.class);
    final DoubleMatrix1D v = (DoubleMatrix1D) sensitivities;
    assertEquals(v.getNumberOfElements(), 3);
    assertArrayEquals(v.getData(), new double[] {VEGA_A_1, DELTA_A_1, RHO_A_1}, EPS);
    final Matrix<?> covariance = result.getCovarianceMatrix();
    assertEquals(covariance.getClass(), DoubleMatrix2D.class);
    final DoubleMatrix2D m = (DoubleMatrix2D) covariance;
    assertEquals(m.getNumberOfRows(), 3);
    assertEquals(m.getNumberOfColumns(), 3);
    assertArrayEquals(m.getData()[0], C1[0], EPS);
    assertArrayEquals(m.getData()[1], C1[1], EPS);
    assertArrayEquals(m.getData()[2], C1[2], EPS);
  }

  @Test
  public void testFirstOrderOneTradeOnly() {
    final ParametricVaRDataBundle result = CALC.evaluate(A_DATA_FIRST_ORDER_2).get(1);
    final List<String> names = result.getNames();
    assertEquals(names, Arrays.asList("A_IMPLIED_VOLATILITY", "A_SPOT_PRICE", "A_INTEREST_RATE"));
    final Matrix<?> sensitivities = result.getSensitivities();
    assertEquals(sensitivities.getClass(), DoubleMatrix1D.class);
    final DoubleMatrix1D v = (DoubleMatrix1D) sensitivities;
    assertEquals(v.getNumberOfElements(), 3);
    assertArrayEquals(v.getData(), new double[] {VEGA_A_1, DELTA_A_1 + DELTA_A_2, RHO_A_1 + RHO_A_2}, EPS);
    final Matrix<?> covariance = result.getCovarianceMatrix();
    assertEquals(covariance.getClass(), DoubleMatrix2D.class);
    final DoubleMatrix2D m = (DoubleMatrix2D) covariance;
    assertEquals(m.getNumberOfRows(), 3);
    assertEquals(m.getNumberOfColumns(), 3);
    assertArrayEquals(m.getData()[0], C1[0], EPS);
    assertArrayEquals(m.getData()[1], C1[1], EPS);
    assertArrayEquals(m.getData()[2], C1[2], EPS);
  }

  @Test
  public void testFirstOrderOnly() {
    final ParametricVaRDataBundle result = CALC.evaluate(DATA_FIRST_ORDER).get(1);
    final List<String> names = result.getNames();
    assertEquals(names, Arrays.asList("A_IMPLIED_VOLATILITY", "A_SPOT_PRICE", "A_INTEREST_RATE", "B_INTEREST_RATE", "B_SPOT_PRICE", "B_IMPLIED_VOLATILITY", "C_SPOT_PRICE", "C_IMPLIED_VOLATILITY",
        "C_IMPLIED_VARIANCE"));
    final Matrix<?> sensitivities = result.getSensitivities();
    assertEquals(sensitivities.getClass(), DoubleMatrix1D.class);
    final DoubleMatrix1D v = (DoubleMatrix1D) sensitivities;
    assertEquals(v.getNumberOfElements(), 9);
    assertArrayEquals(v.getData(), new double[] {VEGA_A_1, DELTA_A_1 + DELTA_A_2, RHO_A_1 + RHO_A_2, RHO_B, DELTA_B, VEGA_B, DELTA_C, VEGA_C, VARIANCE_VEGA_C}, EPS);
    final Matrix<?> covariance = result.getCovarianceMatrix();
    assertEquals(covariance.getClass(), DoubleMatrix2D.class);
    final DoubleMatrix2D m = (DoubleMatrix2D) covariance;
    assertEquals(m.getNumberOfRows(), 9);
    assertEquals(m.getNumberOfColumns(), 9);
    for (int i = 0; i < 9; i++) {
      assertArrayEquals(m.getData()[i], C2[i], EPS);
    }
  }

  @Test
  public void testSecondOrderDiagonalElementsOnlyNoDuplicateGreeks() {
    final ParametricVaRDataBundle result = CALC.evaluate(A_DATA_SECOND_ORDER_1).get(2);
    final List<String> names = result.getNames();
    assertEquals(names, Arrays.asList("A_SPOT_PRICE", "A_IMPLIED_VOLATILITY", "A_STRIKE"));
    final Matrix<?> sensitivities = result.getSensitivities();
    assertEquals(sensitivities.getClass(), DoubleMatrix2D.class);
    final DoubleMatrix2D m1 = (DoubleMatrix2D) sensitivities;
    assertEquals(m1.getNumberOfRows(), 3);
    assertEquals(m1.getNumberOfColumns(), 3);
    final double[][] s = new double[][] {new double[] {GAMMA_A_1, 0, 0}, new double[] {0, VOMMA_A_1, 0}, new double[] {0, 0, STRIKE_GAMMA_A_1}};
    assertArrayEquals(m1.getData()[0], s[0], EPS);
    assertArrayEquals(m1.getData()[1], s[1], EPS);
    assertArrayEquals(m1.getData()[2], s[2], EPS);
    final Matrix<?> covariance = result.getCovarianceMatrix();
    assertEquals(covariance.getClass(), DoubleMatrix2D.class);
    final DoubleMatrix2D m2 = (DoubleMatrix2D) covariance;
    assertEquals(m2.getNumberOfRows(), 3);
    assertEquals(m2.getNumberOfColumns(), 3);
    assertArrayEquals(m2.getData()[0], C3[0], EPS);
    assertArrayEquals(m2.getData()[1], C3[1], EPS);
    assertArrayEquals(m2.getData()[2], C3[2], EPS);
  }

  @Test
  public void testSecondOrderDiagonalElementsOnly() {
    final ParametricVaRDataBundle result = CALC.evaluate(A_DATA_SECOND_ORDER_2).get(2);
    final List<String> names = result.getNames();
    assertEquals(names, Arrays.asList("A_SPOT_PRICE", "A_IMPLIED_VOLATILITY", "A_STRIKE"));
    final Matrix<?> sensitivities = result.getSensitivities();
    assertEquals(sensitivities.getClass(), DoubleMatrix2D.class);
    final DoubleMatrix2D m1 = (DoubleMatrix2D) sensitivities;
    assertEquals(m1.getNumberOfRows(), 3);
    assertEquals(m1.getNumberOfColumns(), 3);
    final double[][] s = new double[][] {new double[] {GAMMA_A_1 + GAMMA_A_2, 0, 0}, new double[] {0, VOMMA_A_1 + VOMMA_A_2, 0}, new double[] {0, 0, STRIKE_GAMMA_A_1}};
    assertArrayEquals(m1.getData()[0], s[0], EPS);
    assertArrayEquals(m1.getData()[1], s[1], EPS);
    assertArrayEquals(m1.getData()[2], s[2], EPS);
    final Matrix<?> covariance = result.getCovarianceMatrix();
    assertEquals(covariance.getClass(), DoubleMatrix2D.class);
    final DoubleMatrix2D m2 = (DoubleMatrix2D) covariance;
    assertEquals(m2.getNumberOfRows(), 3);
    assertEquals(m2.getNumberOfColumns(), 3);
    assertArrayEquals(m2.getData()[0], C3[0], EPS);
    assertArrayEquals(m2.getData()[1], C3[1], EPS);
    assertArrayEquals(m2.getData()[2], C3[2], EPS);
  }

  @Test
  public void testSecondOrderOneTradeOnly() {
    final ParametricVaRDataBundle result = CALC.evaluate(A_DATA_SECOND_ORDER_3).get(2);
    final List<String> names = result.getNames();
    assertEquals(names, Arrays.asList("A_SPOT_PRICE", "A_IMPLIED_VOLATILITY", "A_STRIKE", "A_TIME"));
    final Matrix<?> sensitivities = result.getSensitivities();
    assertEquals(sensitivities.getClass(), DoubleMatrix2D.class);
    final DoubleMatrix2D m1 = (DoubleMatrix2D) sensitivities;
    assertEquals(m1.getNumberOfRows(), 4);
    assertEquals(m1.getNumberOfColumns(), 4);
    final double[][] s = new double[][] {new double[] {GAMMA_A_1 + GAMMA_A_2, VANNA_A, 0, DELTA_BLEED_A}, new double[] {VANNA_A, VOMMA_A_1 + VOMMA_A_2, 0, 0},
        new double[] {0, 0, STRIKE_GAMMA_A_1, 0}, new double[] {DELTA_BLEED_A, 0, 0, 0}};
    assertArrayEquals(m1.getData()[0], s[0], EPS);
    assertArrayEquals(m1.getData()[1], s[1], EPS);
    assertArrayEquals(m1.getData()[2], s[2], EPS);
    final Matrix<?> covariance = result.getCovarianceMatrix();
    assertEquals(covariance.getClass(), DoubleMatrix2D.class);
    final DoubleMatrix2D m2 = (DoubleMatrix2D) covariance;
    assertEquals(m2.getNumberOfRows(), 4);
    assertEquals(m2.getNumberOfColumns(), 4);
    for (int i = 0; i < 4; i++) {
      assertArrayEquals(m2.getData()[i], C4[i], EPS);
    }
  }

  @Test
  public void testSecondOrderOnly() {
    final ParametricVaRDataBundle result = CALC.evaluate(DATA_SECOND_ORDER).get(2);
    final List<String> names = result.getNames();
    assertEquals(names, Arrays.asList("A_SPOT_PRICE", "A_IMPLIED_VOLATILITY", "A_STRIKE", "A_TIME", "B_SPOT_PRICE", "B_IMPLIED_VOLATILITY", "C_SPOT_PRICE", "C_IMPLIED_VOLATILITY", "A_YIELD"));
    final Matrix<?> sensitivities = result.getSensitivities();
    assertEquals(sensitivities.getClass(), DoubleMatrix2D.class);
    final DoubleMatrix2D m1 = (DoubleMatrix2D) sensitivities;
    assertEquals(m1.getNumberOfRows(), 9);
    assertEquals(m1.getNumberOfColumns(), 9);
    final double[][] s = new double[][] {new double[] {GAMMA_A_1 + GAMMA_A_2, VANNA_A, 0, DELTA_BLEED_A, 0, 0, 0, 0, DUMMY_A}, new double[] {VANNA_A, VOMMA_A_1 + VOMMA_A_2, 0, 0, 0, 0, 0, 0, 0},
        new double[] {0, 0, STRIKE_GAMMA_A_1, 0, 0, 0, 0, 0, 0}, new double[] {DELTA_BLEED_A, 0, 0, 0, 0, 0, 0, 0, 0}, new double[] {0, 0, 0, 0, GAMMA_B, 0, 0, 0, 0},
        new double[] {0, 0, 0, 0, 0, VOMMA_B, 0, 0, 0}, new double[] {0, 0, 0, 0, 0, 0, GAMMA_C, VANNA_C, 0}, new double[] {0, 0, 0, 0, 0, 0, VANNA_C, 0, 0},
        new double[] {DUMMY_A, 0, 0, 0, 0, 0, 0, 0, 0}};
    for (int i = 0; i < 9; i++) {
      assertArrayEquals(m1.getData()[i], s[i], EPS);
    }
    final Matrix<?> covariance = result.getCovarianceMatrix();
    assertEquals(covariance.getClass(), DoubleMatrix2D.class);
    final DoubleMatrix2D m2 = (DoubleMatrix2D) covariance;
    assertEquals(m2.getNumberOfRows(), 9);
    assertEquals(m2.getNumberOfColumns(), 9);
    for (int i = 0; i < 9; i++) {
      assertArrayEquals(m2.getData()[i], C5[i], EPS);
    }
  }

  @Test
  public void test() {
    ParametricVaRDataBundle result = CALC.evaluate(DATA).get(1);
    List<String> names = result.getNames();
    assertEquals(names, Arrays.asList("A_IMPLIED_VOLATILITY", "A_SPOT_PRICE", "A_INTEREST_RATE", "B_INTEREST_RATE", "B_SPOT_PRICE", "B_IMPLIED_VOLATILITY", "C_SPOT_PRICE", "C_IMPLIED_VOLATILITY",
        "C_IMPLIED_VARIANCE"));
    Matrix<?> sensitivities = result.getSensitivities();
    assertEquals(sensitivities.getClass(), DoubleMatrix1D.class);
    final DoubleMatrix1D v = (DoubleMatrix1D) sensitivities;
    assertEquals(v.getNumberOfElements(), 9);
    assertArrayEquals(v.getData(), new double[] {VEGA_A_1, DELTA_A_1 + DELTA_A_2, RHO_A_1 + RHO_A_2, RHO_B, DELTA_B, VEGA_B, DELTA_C, VEGA_C, VARIANCE_VEGA_C}, EPS);
    Matrix<?> covariance = result.getCovarianceMatrix();
    assertEquals(covariance.getClass(), DoubleMatrix2D.class);
    final DoubleMatrix2D m = (DoubleMatrix2D) covariance;
    assertEquals(m.getNumberOfRows(), 9);
    assertEquals(m.getNumberOfColumns(), 9);
    for (int i = 0; i < 9; i++) {
      assertArrayEquals(m.getData()[i], C2[i], EPS);
    }
    result = CALC.evaluate(DATA).get(2);
    names = result.getNames();
    assertEquals(names, Arrays.asList("A_SPOT_PRICE", "A_IMPLIED_VOLATILITY", "A_STRIKE", "A_TIME", "B_SPOT_PRICE", "B_IMPLIED_VOLATILITY", "C_SPOT_PRICE", "C_IMPLIED_VOLATILITY", "A_YIELD"));
    sensitivities = result.getSensitivities();
    assertEquals(sensitivities.getClass(), DoubleMatrix2D.class);
    final DoubleMatrix2D m1 = (DoubleMatrix2D) sensitivities;
    assertEquals(m1.getNumberOfRows(), 9);
    assertEquals(m1.getNumberOfColumns(), 9);
    final double[][] s = new double[][] {new double[] {GAMMA_A_1 + GAMMA_A_2, VANNA_A, 0, DELTA_BLEED_A, 0, 0, 0, 0, DUMMY_A}, new double[] {VANNA_A, VOMMA_A_1 + VOMMA_A_2, 0, 0, 0, 0, 0, 0, 0},
        new double[] {0, 0, STRIKE_GAMMA_A_1, 0, 0, 0, 0, 0, 0}, new double[] {DELTA_BLEED_A, 0, 0, 0, 0, 0, 0, 0, 0}, new double[] {0, 0, 0, 0, GAMMA_B, 0, 0, 0, 0},
        new double[] {0, 0, 0, 0, 0, VOMMA_B, 0, 0, 0}, new double[] {0, 0, 0, 0, 0, 0, GAMMA_C, VANNA_C, 0}, new double[] {0, 0, 0, 0, 0, 0, VANNA_C, 0, 0},
        new double[] {DUMMY_A, 0, 0, 0, 0, 0, 0, 0, 0}};
    for (int i = 0; i < 9; i++) {
      assertArrayEquals(m1.getData()[i], s[i], EPS);
    }
    covariance = result.getCovarianceMatrix();
    assertEquals(covariance.getClass(), DoubleMatrix2D.class);
    final DoubleMatrix2D m2 = (DoubleMatrix2D) covariance;
    assertEquals(m2.getNumberOfRows(), 9);
    assertEquals(m2.getNumberOfColumns(), 9);
    for (int i = 0; i < 9; i++) {
      assertArrayEquals(m2.getData()[i], C5[i], EPS);
    }
  }

  private static class DummyGreek extends Greek {

    public DummyGreek(final Underlying underlying, final String name) {
      super(underlying, name);
    }

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return null;
    }

  }
}
