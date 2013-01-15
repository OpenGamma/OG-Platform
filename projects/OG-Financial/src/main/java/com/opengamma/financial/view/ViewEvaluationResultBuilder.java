/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.view;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.ViewResultEntry;
import com.opengamma.engine.view.calcnode.MissingInput;
import com.opengamma.engine.view.compilation.CompiledViewCalculationConfiguration;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.util.timeseries.TimeSeries;
import com.opengamma.util.timeseries.fast.DateTimeNumericEncoding;
import com.opengamma.util.timeseries.fast.integer.FastArrayIntDoubleTimeSeries;
import com.opengamma.util.timeseries.fast.integer.object.FastArrayIntObjectTimeSeries;
import com.opengamma.util.timeseries.localdate.ArrayLocalDateDoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.ArrayLocalDateObjectTimeSeries;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.LocalDateEpochDaysConverter;
import com.opengamma.util.timeseries.localdate.LocalDateObjectTimeSeries;

/* package */class ViewEvaluationResultBuilder {

  private static final Logger s_logger = LoggerFactory.getLogger(ViewEvaluationResultBuilder.class);

  private static class AbstractTimeSeriesBuilder {

    private final int[] _dates;
    private int _next;

    public AbstractTimeSeriesBuilder(final int[] dates, final int next) {
      _dates = dates;
      _next = next;
    }

    public AbstractTimeSeriesBuilder(final AbstractTimeSeriesBuilder copyFrom) {
      _dates = copyFrom._dates;
      _next = copyFrom._next;
    }

    public AbstractTimeSeriesBuilder addPoint(final int date, final Object value) {
      if (value instanceof Number) {
        return new DoubleTimeSeriesBuilder(_dates, date, ((Number) value).doubleValue());
      } else {
        return new ObjectTimeSeriesBuilder(_dates, date, value);
      }
    }

    protected int index(final int date) {
      final int i = _next++;
      _dates[i] = date;
      return i;
    }

    @SuppressWarnings("rawtypes")
    protected TimeSeries makeTimeSeries(final TimeZone tz, final int[] dates) {
      throw new IllegalStateException();
    }

    @SuppressWarnings("rawtypes")
    public TimeSeries makeTimeSeries(final TimeZone tz) {
      if (_next == 0) {
        return ArrayLocalDateDoubleTimeSeries.EMPTY_SERIES;
      } else {
        final int[] dates;
        if (_dates.length != _next) {
          dates = new int[_next];
          System.arraycopy(_dates, 0, dates, 0, _next);
        } else {
          dates = _dates;
        }
        return makeTimeSeries(tz, dates);
      }
    }

  }

  // TODO: If the duration of the time series is high, or there are simply a large quantity then we could modify these builders to
  // write directly to the time series database instead (or batch up the points in internal arrays) and the result bundle will be
  // the identifiers of the time series that were written to the database

  private static class DoubleTimeSeriesBuilder extends AbstractTimeSeriesBuilder {

    private final double[] _values;

    public DoubleTimeSeriesBuilder(final int[] dates, final int date, final double initial) {
      super(dates, 0);
      _values = new double[dates.length];
      _values[index(date)] = initial;
    }

    @Override
    public AbstractTimeSeriesBuilder addPoint(final int date, final Object value) {
      if (value instanceof Number) {
        _values[index(date)] = ((Number) value).doubleValue();
        return this;
      } else {
        return new ObjectTimeSeriesBuilder(this, date, value);
      }
    }

    @Override
    protected LocalDateDoubleTimeSeries makeTimeSeries(final TimeZone tz, final int[] dates) {
      final double[] values;
      if (_values.length != dates.length) {
        values = new double[dates.length];
        System.arraycopy(_values, 0, values, 0, dates.length);
      } else {
        values = _values;
      }
      return new ArrayLocalDateDoubleTimeSeries(tz, new FastArrayIntDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS, dates, values));
    }

  }

  private static class ObjectTimeSeriesBuilder extends AbstractTimeSeriesBuilder {

    private final Object[] _values;

    public ObjectTimeSeriesBuilder(final int[] dates, final int date, final Object initial) {
      super(dates, 0);
      _values = new Object[dates.length];
      _values[index(date)] = initial;
    }

    public ObjectTimeSeriesBuilder(final DoubleTimeSeriesBuilder copyFrom, final int date, final Object next) {
      super(copyFrom);
      _values = new Object[copyFrom._values.length];
      final int count = index(date);
      _values[count] = next;
      for (int i = 0; i < count; i++) {
        _values[i] = copyFrom._values[i];
      }
    }

    @Override
    public AbstractTimeSeriesBuilder addPoint(final int date, final Object value) {
      _values[index(date)] = value;
      return this;
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked" })
    protected LocalDateObjectTimeSeries makeTimeSeries(final TimeZone tz, final int[] dates) {
      final Object[] values;
      if (_values.length != dates.length) {
        values = new Object[dates.length];
        System.arraycopy(_values, 0, values, 0, dates.length);
      } else {
        values = _values;
      }
      return new ArrayLocalDateObjectTimeSeries(tz, new FastArrayIntObjectTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS, dates, values));
    }

  }

  private static class ConfigurationResults {

    private Map<ValueSpecification, Set<ValueRequirement>> _requirements;
    private final Map<ValueRequirement, AbstractTimeSeriesBuilder> _results = new HashMap<ValueRequirement, AbstractTimeSeriesBuilder>();

    public ConfigurationResults(final int cycles, final Collection<ValueRequirement> requirements) {
      for (final ValueRequirement requirement : requirements) {
        _results.put(requirement, new AbstractTimeSeriesBuilder(new int[cycles], 0));
      }
    }

    public void setRequirements(final Map<ValueSpecification, Set<ValueRequirement>> requirements) {
      _requirements = requirements;
    }

    public void store(final int date, final ValueSpecification specification, final Object value) {
      final Set<ValueRequirement> requirements = _requirements.get(specification);
      if (requirements != null) {
        for (final ValueRequirement requirement : requirements) {
          final AbstractTimeSeriesBuilder builder = _results.get(requirement);
          if (builder == null) {
            s_logger.warn("View produced value {} for unrequested requirement {}", specification, requirement);
          } else {
            final AbstractTimeSeriesBuilder newBuilder = builder.addPoint(date, value);
            if (newBuilder != builder) {
              _results.put(requirement, newBuilder);
            }
          }
        }
      } else {
        s_logger.warn("View produced unrequested value {}", specification);
      }
    }

    public ViewEvaluationResult makeResult(final TimeZone tz) {
      final ViewEvaluationResult results = new ViewEvaluationResult();
      for (final Map.Entry<ValueRequirement, AbstractTimeSeriesBuilder> result : _results.entrySet()) {
        results.addTimeSeries(result.getKey(), result.getValue().makeTimeSeries(tz));
      }
      return results;
    }

  }

  private final TimeZone _timeZone;
  private final LocalDateEpochDaysConverter _date;
  private final Map<String, ConfigurationResults> _results = new HashMap<String, ConfigurationResults>();

  public ViewEvaluationResultBuilder(final TimeZone timeZone, final int cycles, final ViewDefinition viewDefinition) {
    _timeZone = timeZone;
    _date = new LocalDateEpochDaysConverter(timeZone);
    for (final ViewCalculationConfiguration calcConfig : viewDefinition.getAllCalculationConfigurations()) {
      final ConfigurationResults configResults = new ConfigurationResults(cycles, calcConfig.getSpecificRequirements());
      _results.put(calcConfig.getName(), configResults);
    }
  }

  public void store(final CompiledViewDefinition viewDefinition) {
    for (final CompiledViewCalculationConfiguration calcConfig : viewDefinition.getCompiledCalculationConfigurations()) {
      final ConfigurationResults configResults = _results.get(calcConfig.getName());
      configResults.setRequirements(calcConfig.getTerminalOutputSpecifications());
    }
  }

  public void store(final ViewComputationResultModel results) {
    final int date = _date.convertToInt(ZonedDateTime.ofInstant(results.getValuationTime(), _timeZone).toLocalDate());
    for (final ViewResultEntry viewResult : results.getAllResults()) {
      final ComputedValue computedValue = viewResult.getComputedValue();
      final Object value = computedValue.getValue();
      if ((value != null) && !(value instanceof MissingInput)) {
        final ConfigurationResults configResults = _results.get(viewResult.getCalculationConfiguration());
        configResults.store(date, computedValue.getSpecification(), value);
      }
    }
  }

  public Map<String, ViewEvaluationResult> getResults() {
    final Map<String, ViewEvaluationResult> results = Maps.newHashMapWithExpectedSize(_results.size());
    for (final Map.Entry<String, ConfigurationResults> result : _results.entrySet()) {
      results.put(result.getKey(), result.getValue().makeResult(_timeZone));
    }
    return results;
  }

}
