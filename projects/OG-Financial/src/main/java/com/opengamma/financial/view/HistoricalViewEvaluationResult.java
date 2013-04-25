/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
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

import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.timeseries.TimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateObjectTimeSeries;

/**
 * Result object holding the time series bundles produced by {@link ViewEvaluationFunction}.
 */
@SuppressWarnings("rawtypes")
public final class HistoricalViewEvaluationResult implements Serializable {

  private static final long serialVersionUID = 1L;

  private final Map<ValueRequirement, TimeSeries> _data = new HashMap<ValueRequirement, TimeSeries>();

  public void addTimeSeries(final ValueRequirement requirement, final TimeSeries timeSeries) {
    _data.put(requirement, timeSeries);
  }

  public Set<ValueRequirement> getValueRequirements() {
    return _data.keySet();
  }

  public TimeSeries getTimeSeries(final ValueRequirement requirement) {
    return _data.get(requirement);
  }

  public LocalDateDoubleTimeSeries getDoubleTimeSeries(final ValueRequirement requirement) {
    final TimeSeries ts = getTimeSeries(requirement);
    if (ts instanceof LocalDateDoubleTimeSeries) {
      return (LocalDateDoubleTimeSeries) ts;
    } else {
      return null;
    }
  }

  public LocalDateObjectTimeSeries getObjectTimeSeries(final ValueRequirement requirement) {
    final TimeSeries ts = getTimeSeries(requirement);
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
    return "ViewEvaluationResult[" + points + " TS points for " + count + " requirements]";
  }

  public FudgeMsg toFudgeMsg(final FudgeSerializer serializer) {
    final MutableFudgeMsg msg = serializer.newMessage();
    for (final Map.Entry<ValueRequirement, TimeSeries> data : _data.entrySet()) {
      serializer.addToMessage(msg, null, 1, data.getKey());
      serializer.addToMessage(msg, null, 2, data.getValue()); // time-series Fudge builders add their own class details
    }
    return msg;
  }

  public static HistoricalViewEvaluationResult fromFudgeMsg(final FudgeDeserializer deserializer, final FudgeMsg msg) {
    final HistoricalViewEvaluationResult result = new HistoricalViewEvaluationResult();
    final Queue<Object> queue = new LinkedList<Object>();
    boolean requirements = false;
    for (final FudgeField field : msg) {
      final Integer ord = field.getOrdinal();
      if (ord != null) {
        if (ord.intValue() == 1) {
          final ValueRequirement requirement = deserializer.fieldValueToObject(ValueRequirement.class, field);
          if (requirements) {
            queue.add(requirement);
          } else if (queue.isEmpty()) {
            queue.add(requirement);
            requirements = true;
          } else {
            result.addTimeSeries(requirement, (TimeSeries) queue.remove());
          }
        } else if (ord.intValue() == 2) {
          final TimeSeries timeSeries = deserializer.fieldValueToObject(TimeSeries.class, field);
          if (!requirements) {
            queue.add(timeSeries);
          } else if (queue.isEmpty()) {
            queue.add(timeSeries);
            requirements = false;
          } else {
            result.addTimeSeries((ValueRequirement) queue.remove(), timeSeries);
          }
        }
      }
    }
    return result;
  }

}
