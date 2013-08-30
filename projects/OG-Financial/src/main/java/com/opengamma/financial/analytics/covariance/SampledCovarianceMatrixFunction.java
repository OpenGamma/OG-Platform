/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.covariance;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.threeten.bp.Period;

import com.opengamma.analytics.financial.covariance.CovarianceMatrixCalculator;
import com.opengamma.analytics.financial.covariance.HistoricalCovarianceCalculator;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.analytics.DoubleLabelledMatrix2D;
import com.opengamma.financial.analytics.timeseries.DateConstraint;
import com.opengamma.financial.temptarget.TempTarget;
import com.opengamma.financial.temptarget.TempTargetRepository;
import com.opengamma.financial.view.HistoricalViewEvaluationMarketDataMode;
import com.opengamma.financial.view.HistoricalViewEvaluationTarget;
import com.opengamma.financial.view.ViewEvaluationFunction;
import com.opengamma.id.UniqueId;
import com.opengamma.timeseries.DoubleTimeSeries;

/**
 * Iterates a view client over a window of historical data to get time series of values from which a covariance matrix can be constructed. The target will identify the item(s) for which data should be
 * gathered to build the matrix from.
 */
public abstract class SampledCovarianceMatrixFunction extends AbstractFunction.NonCompiledInvoker {

  /**
   * When used in "permissive" mode, will use this period as a default sampling duration.
   */
  private static final Period DEFAULT_SAMPLING_PERIOD = Period.ofMonths(1);

  /**
   * The type supported by this class.
   */
  protected static final ComputationTargetType TYPE = ComputationTargetType.PORTFOLIO.or(ComputationTargetType.PORTFOLIO_NODE).or(ComputationTargetType.POSITION);

  /**
   * Returns the type of data used to construct the matrix, and distinguish between different sub-class implementations. For example, this might be market data, risk factors or something else.
   * 
   * @return the type, not null
   */
  protected abstract String getDataType();

  protected Set<ValueRequirement> createRequirements(final ComputationTargetSpecification tempTargetSpec) {
    return Collections.singleton(new ValueRequirement(ValueRequirementNames.HISTORICAL_TIME_SERIES, tempTargetSpec, ValueProperties.withAny(ViewEvaluationFunction.PROPERTY_CALC_CONFIG).get()));
  }

  protected void addValueRequirements(final FunctionCompilationContext context, final Portfolio portfolio, final ViewCalculationConfiguration calcConfig) {
    addValueRequirements(context, portfolio.getRootNode(), calcConfig);
  }

  protected void addValueRequirements(final FunctionCompilationContext context, final PortfolioNode node, final ViewCalculationConfiguration calcConfig) {
    for (PortfolioNode child : node.getChildNodes()) {
      addValueRequirements(context, child, calcConfig);
    }
    for (Position child : node.getPositions()) {
      addValueRequirements(context, child, calcConfig);
    }
  }

  protected abstract void addValueRequirements(FunctionCompilationContext context, Position position, ViewCalculationConfiguration calcConfig);

  protected void addValueRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ViewCalculationConfiguration calcConfig) {
    if (target.getValue() instanceof Portfolio) {
      addValueRequirements(context, (Portfolio) target.getValue(), calcConfig);
    } else if (target.getValue() instanceof PortfolioNode) {
      addValueRequirements(context, (PortfolioNode) target.getValue(), calcConfig);
    } else if (target.getValue() instanceof Position) {
      addValueRequirements(context, (Position) target.getValue(), calcConfig);
    }
  }

  protected ViewCalculationConfiguration createViewCalculationConfiguration(final ViewDefinition viewDefinition, final String calcConfigName) {
    return new ViewCalculationConfiguration(viewDefinition, calcConfigName);
  }

  protected <T extends Comparable<? super T>> DoubleLabelledMatrix2D createCovarianceMatrix(DoubleTimeSeries<T>[] timeSeries, Object[] labels) {
    final CovarianceMatrixCalculator calculator = new CovarianceMatrixCalculator(new HistoricalCovarianceCalculator());
    int len = timeSeries.length;
    // Any nulls or empty time series (missing data) will upset the calculator, so we'll remove them and produce a best efforts matrix with what is left
    for (int i = 0; i < len; i++) {
      if ((timeSeries[i] == null) || timeSeries[i].isEmpty()) {
        len--;
        timeSeries[i] = timeSeries[len];
        labels[i] = labels[len];
        i--;
      }
    }
    if (len == 0) {
      throw new IllegalArgumentException("No time series");
    }
    if (len != timeSeries.length) {
      timeSeries = Arrays.copyOf(timeSeries, len);
      labels = Arrays.copyOf(labels, len);
    }
    // The time-series must all have corresponding dates - delete any points which are not common to all time-series
    final Comparable<? super T>[][] times = new Comparable[len][];
    final double[][] values = new double[len][];
    for (int i = 0; i < len; i++) {
      times[i] = timeSeries[i].timesArray();
      values[i] = timeSeries[i].valuesArrayFast();
    }
    boolean ended = false;
    int timeIndex = 0;
    do {
      Comparable<? super T> earliest = times[0][timeIndex];
      boolean mismatch = false;
      for (int i = 1; i < len; i++) {
        int c = earliest.compareTo((T) times[i][timeIndex]);
        if (c != 0) {
          mismatch = true;
          if (c > 0) {
            earliest = times[i][timeIndex];
          }
        }
      }
      if (mismatch) {
        for (int i = 0; i < len; i++) {
          if (earliest.equals(times[i][timeIndex])) {
            System.arraycopy(times[i], timeIndex + 1, times[i], timeIndex, times[i].length - (timeIndex + 1));
            System.arraycopy(values[i], timeIndex + 1, values[i], timeIndex, values[i].length - (timeIndex + 1));
            times[i][times[i].length - 1] = null;
            if (times[i][timeIndex] == null) {
              ended = true;
            }
          }
        }
      } else {
        timeIndex++;
        for (int i = 0; i < len; i++) {
          if ((timeIndex >= times[i].length) || (times[i][timeIndex] == null)) {
            ended = true;
          }
        }
      }
    } while (!ended);
    if (timeIndex < 1) {
      throw new IllegalArgumentException("Time series union is empty");
    }
    DoubleTimeSeries<T>[] newTimeSeries = null;
    for (int i = 0; i < len; i++) {
      if (times[i].length > timeIndex) {
        if (newTimeSeries == null) {
          newTimeSeries = new DoubleTimeSeries[len];
          System.arraycopy(timeSeries, 0, newTimeSeries, 0, len);
        }
        final Double[] value = new Double[timeIndex];
        for (int j = 0; j < timeIndex; j++) {
          value[j] = values[i][j];
        }
        newTimeSeries[i] = timeSeries[i].newInstance(Arrays.copyOf((T[]) times[i], timeIndex), value);
      }
    }
    if (newTimeSeries != null) {
      timeSeries = newTimeSeries;
    }
    // Keys will just be sequential numbers
    final Double[] keys = new Double[len];
    for (int i = 0; i < timeSeries.length; i++) {
      keys[i] = (double) i;
    }
    // Calculate the co-variance matrix
    final DoubleMatrix2D unlabelled = calculator.evaluate(timeSeries);
    // Label it
    return new DoubleLabelledMatrix2D(keys, labels, keys, labels, unlabelled.getData());
  }

  // CompiledFunctionDefinition

  @Override
  public ComputationTargetType getTargetType() {
    return TYPE;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    return context.getViewCalculationConfiguration() != null;
  }

  @Override
  protected ValueProperties.Builder createValueProperties() {
    final ValueProperties.Builder properties = super.createValueProperties();
    properties.with("Type", getDataType());
    return properties;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.COVARIANCE_MATRIX, target.toSpecification(), createValueProperties().withAny(ValuePropertyNames.SAMPLING_PERIOD).get()));
  }

  private String anyConstraintOrNull(final ValueProperties constraints, final String name) {
    final Set<String> values = constraints.getValues(name);
    if ((values == null) || values.isEmpty()) {
      return null;
    } else {
      return values.iterator().next();
    }
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final ValueProperties constraints = desiredValue.getConstraints();
    final String lookbackPeriodString = anyConstraintOrNull(constraints, ValuePropertyNames.SAMPLING_PERIOD);
    final DateConstraint startDate;
    if (lookbackPeriodString == null) {
      if (!OpenGammaCompilationContext.isPermissive(context)) {
        return null;
      }
      startDate = DateConstraint.VALUATION_TIME.minus(DEFAULT_SAMPLING_PERIOD);
    } else {
      startDate = DateConstraint.VALUATION_TIME.minus(Period.parse(lookbackPeriodString));
    }
    final ViewDefinition viewDefinition = context.getViewCalculationConfiguration().getViewDefinition();
    final HistoricalViewEvaluationTarget tempTarget = new HistoricalViewEvaluationTarget(viewDefinition.getMarketDataUser(), startDate.toString(), true, DateConstraint.VALUATION_TIME.toString(),
        false, null, HistoricalViewEvaluationMarketDataMode.HISTORICAL);
    final ViewCalculationConfiguration calcConfig = createViewCalculationConfiguration(tempTarget.getViewDefinition(), context.getViewCalculationConfiguration().getName());
    addValueRequirements(context, target, calcConfig);
    tempTarget.getViewDefinition().addViewCalculationConfiguration(calcConfig);
    final TempTargetRepository targets = OpenGammaCompilationContext.getTempTargets(context);
    final UniqueId tempTargetId = targets.locateOrStore(tempTarget);
    final ComputationTargetSpecification targetSpec = new ComputationTargetSpecification(TempTarget.TYPE, tempTargetId);
    return createRequirements(targetSpec);
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    final TempTarget tempTargetObject = OpenGammaCompilationContext.getTempTargets(context).get(inputs.keySet().iterator().next().getTargetSpecification().getUniqueId());
    if (!(tempTargetObject instanceof HistoricalViewEvaluationTarget)) {
      return null;
    }
    final HistoricalViewEvaluationTarget historicalTarget = (HistoricalViewEvaluationTarget) tempTargetObject;
    final DateConstraint startDate = DateConstraint.parse(historicalTarget.getStartDate());
    final DateConstraint endDate = DateConstraint.parse(historicalTarget.getEndDate());
    final Period samplingPeriod = startDate.periodUntil(endDate);
    return Collections
        .singleton(new ValueSpecification(ValueRequirementNames.COVARIANCE_MATRIX, target.toSpecification(), createValueProperties()
            .with(ValuePropertyNames.SAMPLING_PERIOD, samplingPeriod.toString()).get()));
  }

}
