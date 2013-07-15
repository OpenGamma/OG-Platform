/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.view;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.timeseries.TimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateObjectTimeSeries;

/**
 * Result object holding the time series bundles produced by {@link ViewEvaluationFunction} containing the input market data used.
 */
@SuppressWarnings("rawtypes")
public final class HistoricalViewEvaluationMarketData implements Serializable {

  private static final long serialVersionUID = 1L;

  private final Map<ValueSpecification, TimeSeries> _data = new HashMap<ValueSpecification, TimeSeries>();

  public void addTimeSeries(final ValueSpecification specification, final TimeSeries timeSeries) {
    _data.put(specification, timeSeries);
  }

  public Set<ValueSpecification> getValueSpecifications() {
    return _data.keySet();
  }

  public TimeSeries getTimeSeries(final ValueSpecification specification) {
    return _data.get(specification);
  }

  public LocalDateDoubleTimeSeries getDoubleTimeSeries(final ValueSpecification specification) {
    final TimeSeries ts = getTimeSeries(specification);
    if (ts instanceof LocalDateDoubleTimeSeries) {
      return (LocalDateDoubleTimeSeries) ts;
    } else {
      return null;
    }
  }

  public LocalDateObjectTimeSeries getObjectTimeSeries(final ValueSpecification specification) {
    final TimeSeries ts = getTimeSeries(specification);
    if (ts instanceof LocalDateObjectTimeSeries) {
      return (LocalDateObjectTimeSeries) ts;
    } else {
      return null;
    }
  }

  @Override
  public String toString() {
    int count = 0;
    int points = 0;
    for (final TimeSeries ts : _data.values()) {
      points += ts.size();
      count++;
    }
    return "ViewEvaluationMarketData[" + points + " TS points for " + count + " specifications]";
  }

  public FudgeMsg toFudgeMsg(final FudgeSerializer serializer) {
    final MutableFudgeMsg msg = serializer.newMessage();
    for (final Map.Entry<ValueSpecification, TimeSeries> data : _data.entrySet()) {
      serializer.addToMessage(msg, null, 1, data.getKey());
      serializer.addToMessage(msg, null, 2, data.getValue()); // time-series Fudge builders add their own class details
    }
    return msg;
  }

  public static HistoricalViewEvaluationMarketData fromFudgeMsg(final FudgeDeserializer deserializer, final FudgeMsg msg) {
    final HistoricalViewEvaluationMarketData result = new HistoricalViewEvaluationMarketData();
    final Queue<Object> queue = new LinkedList<Object>();
    boolean requirements = false;
    for (final FudgeField field : msg) {
      final Integer ord = field.getOrdinal();
      if (ord != null) {
        if (ord.intValue() == 1) {
          final ValueSpecification specification = deserializer.fieldValueToObject(ValueSpecification.class, field);
          if (requirements) {
            queue.add(specification);
          } else if (queue.isEmpty()) {
            queue.add(specification);
            requirements = true;
          } else {
            result.addTimeSeries(specification, (TimeSeries) queue.remove());
          }
        } else if (ord.intValue() == 2) {
          final TimeSeries timeSeries = deserializer.fieldValueToObject(TimeSeries.class, field);
          if (!requirements) {
            queue.add(timeSeries);
          } else if (queue.isEmpty()) {
            queue.add(timeSeries);
            requirements = false;
          } else {
            result.addTimeSeries((ValueSpecification) queue.remove(), timeSeries);
          }
        }
      }
    }
    return result;
  }

}
