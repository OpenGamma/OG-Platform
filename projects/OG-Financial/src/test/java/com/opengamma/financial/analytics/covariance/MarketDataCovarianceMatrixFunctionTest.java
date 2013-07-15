/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.covariance;

import static org.testng.Assert.assertEquals;

import java.util.Collections;
import java.util.Set;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.function.FunctionInputsImpl;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.DoubleLabelledMatrix2D;
import com.opengamma.financial.view.HistoricalViewEvaluationMarketData;
import com.opengamma.id.UniqueId;
import com.opengamma.timeseries.date.localdate.ImmutableLocalDateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.ImmutableLocalDateObjectTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateObjectTimeSeries;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link MarketDataCovarianceMatrixFunction} class.
 */
@Test(groups = TestGroup.UNIT)
public class MarketDataCovarianceMatrixFunctionTest {

  private final MarketDataCovarianceMatrixFunction FUNCTION = new MarketDataCovarianceMatrixFunction();
  private final ValueProperties PROPERTIES = ValueProperties.with(ValuePropertyNames.FUNCTION, "Test").get();
  private final ValueSpecification INPUT_VALUE = new ValueSpecification(ValueRequirementNames.HISTORICAL_TIME_SERIES, ComputationTargetSpecification.NULL, PROPERTIES);
  private final ValueRequirement DESIRED_VALUE = new ValueRequirement(ValueRequirementNames.COVARIANCE_MATRIX, ComputationTargetSpecification.NULL, PROPERTIES);

  private ValueSpecification timeSeriesSpecification(final int index) {
    return new ValueSpecification(ValueRequirementNames.VALUE, new ComputationTargetSpecification(ComputationTargetType.PRIMITIVE, UniqueId.of("Test", Integer.toString(index))), PROPERTIES);
  }

  private LocalDateDoubleTimeSeries localDateDoubleTimeSeries(LocalDate date, final int length, int skip, final int skipB) {
    final LocalDate[] d = new LocalDate[length];
    final double[] v = new double[length];
    for (int i = 0; i < length; i++) {
      d[i] = date;
      v[i] = (double) i;
      date = date.plusDays(1);
      if (((skip++) % skipB) == 0) {
        date = date.plusDays(1);
      }
    }
    return ImmutableLocalDateDoubleTimeSeries.of(d, v);
  }

  private LocalDateDoubleTimeSeries localDateDoubleTimeSeries(final LocalDate date, final int length) {
    return localDateDoubleTimeSeries(date, length, 0, 5);
  }

  private LocalDateDoubleTimeSeries localDateDoubleTimeSeries(final int length, final int skip, final int skipB) {
    return localDateDoubleTimeSeries(LocalDate.of(2013, 1, 1), length, skip, skipB);
  }

  private LocalDateDoubleTimeSeries localDateDoubleTimeSeries(final int length) {
    return localDateDoubleTimeSeries(length, 0, 5);
  }

  private LocalDateObjectTimeSeries<?> localDateObjectTimeSeries(final int length) {
    final LocalDate[] d = new LocalDate[length];
    final Object[] v = new Object[length];
    final LocalDate start = LocalDate.of(2013, 1, 1);
    for (int i = 0; i < length; i++) {
      d[i] = start.plusDays(i);
      v[i] = Integer.toString(i);
    }
    return ImmutableLocalDateObjectTimeSeries.of(d, v);
  }

  public void testExecuteFullData() {
    final HistoricalViewEvaluationMarketData input = new HistoricalViewEvaluationMarketData();
    input.addTimeSeries(timeSeriesSpecification(0), localDateDoubleTimeSeries(10));
    input.addTimeSeries(timeSeriesSpecification(1), localDateDoubleTimeSeries(10));
    input.addTimeSeries(timeSeriesSpecification(2), localDateDoubleTimeSeries(10));
    input.addTimeSeries(timeSeriesSpecification(3), localDateDoubleTimeSeries(10));
    input.addTimeSeries(timeSeriesSpecification(4), localDateDoubleTimeSeries(10));
    final FunctionInputs inputs = new FunctionInputsImpl(null, new ComputedValue(INPUT_VALUE, input));
    final Set<ComputedValue> result = FUNCTION.execute(new FunctionExecutionContext(), inputs, ComputationTarget.NULL, Collections.singleton(DESIRED_VALUE));
    assertEquals(result.size(), 1);
    final ComputedValue value = result.iterator().next();
    assertEquals(value.getValue().getClass(), DoubleLabelledMatrix2D.class);
    final DoubleLabelledMatrix2D matrix = (DoubleLabelledMatrix2D) value.getValue();
    assertEquals(matrix.getXLabels().length, 5);
    assertEquals(matrix.getYLabels().length, 5);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testExecuteNoData() {
    final HistoricalViewEvaluationMarketData input = new HistoricalViewEvaluationMarketData();
    final FunctionInputs inputs = new FunctionInputsImpl(null, new ComputedValue(INPUT_VALUE, input));
    FUNCTION.execute(new FunctionExecutionContext(), inputs, ComputationTarget.NULL, Collections.singleton(DESIRED_VALUE));
  }

  public void testExecuteMissingData() {
    final HistoricalViewEvaluationMarketData input = new HistoricalViewEvaluationMarketData();
    input.addTimeSeries(timeSeriesSpecification(0), localDateDoubleTimeSeries(10, 0, 5));
    input.addTimeSeries(timeSeriesSpecification(1), localDateObjectTimeSeries(10));
    input.addTimeSeries(timeSeriesSpecification(2), localDateDoubleTimeSeries(10, 0, 5));
    input.addTimeSeries(timeSeriesSpecification(3), localDateObjectTimeSeries(10));
    input.addTimeSeries(timeSeriesSpecification(4), localDateDoubleTimeSeries(10, 0, 5));
    input.addTimeSeries(timeSeriesSpecification(5), localDateDoubleTimeSeries(0, 0, 5));
    final FunctionInputs inputs = new FunctionInputsImpl(null, new ComputedValue(INPUT_VALUE, input));
    final Set<ComputedValue> result = FUNCTION.execute(new FunctionExecutionContext(), inputs, ComputationTarget.NULL, Collections.singleton(DESIRED_VALUE));
    assertEquals(result.size(), 1);
    final ComputedValue value = result.iterator().next();
    assertEquals(value.getValue().getClass(), DoubleLabelledMatrix2D.class);
    final DoubleLabelledMatrix2D matrix = (DoubleLabelledMatrix2D) value.getValue();
    assertEquals(matrix.getXLabels().length, 3);
    assertEquals(matrix.getYLabels().length, 3);
  }

  public void testExecuteSlightMisalignedData() {
    final HistoricalViewEvaluationMarketData input = new HistoricalViewEvaluationMarketData();
    input.addTimeSeries(timeSeriesSpecification(0), localDateDoubleTimeSeries(10, 0, 5));
    input.addTimeSeries(timeSeriesSpecification(1), localDateDoubleTimeSeries(15, 3, 7));
    input.addTimeSeries(timeSeriesSpecification(2), localDateDoubleTimeSeries(10, 1, 5));
    input.addTimeSeries(timeSeriesSpecification(3), localDateDoubleTimeSeries(15, 0, 7));
    input.addTimeSeries(timeSeriesSpecification(4), localDateDoubleTimeSeries(10, 2, 5));
    final FunctionInputs inputs = new FunctionInputsImpl(null, new ComputedValue(INPUT_VALUE, input));
    final Set<ComputedValue> result = FUNCTION.execute(new FunctionExecutionContext(), inputs, ComputationTarget.NULL, Collections.singleton(DESIRED_VALUE));
    assertEquals(result.size(), 1);
    final ComputedValue value = result.iterator().next();
    assertEquals(value.getValue().getClass(), DoubleLabelledMatrix2D.class);
    final DoubleLabelledMatrix2D matrix = (DoubleLabelledMatrix2D) value.getValue();
    assertEquals(matrix.getXLabels().length, 5);
    assertEquals(matrix.getYLabels().length, 5);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testExecuteTotalMisalignedData() {
    final HistoricalViewEvaluationMarketData input = new HistoricalViewEvaluationMarketData();
    input.addTimeSeries(timeSeriesSpecification(0), localDateDoubleTimeSeries(LocalDate.of(2013, 1, 1), 10));
    input.addTimeSeries(timeSeriesSpecification(1), localDateDoubleTimeSeries(LocalDate.of(2013, 1, 1), 15));
    input.addTimeSeries(timeSeriesSpecification(2), localDateDoubleTimeSeries(LocalDate.of(2013, 2, 1), 10));
    input.addTimeSeries(timeSeriesSpecification(3), localDateDoubleTimeSeries(LocalDate.of(2013, 2, 1), 15));
    final FunctionInputs inputs = new FunctionInputsImpl(null, new ComputedValue(INPUT_VALUE, input));
    FUNCTION.execute(new FunctionExecutionContext(), inputs, ComputationTarget.NULL, Collections.singleton(DESIRED_VALUE));
  }

}
