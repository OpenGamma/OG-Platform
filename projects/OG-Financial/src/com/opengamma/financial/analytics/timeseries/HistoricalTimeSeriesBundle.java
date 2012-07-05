/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.timeseries;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.ArgumentChecker;

/**
 * A collection of historical time series objects.
 */
public class HistoricalTimeSeriesBundle {

  private final Map<ExternalId, HistoricalTimeSeries> _data = new HashMap<ExternalId, HistoricalTimeSeries>();

  public void add(final ExternalId id, final HistoricalTimeSeries timeSeries) {
    ArgumentChecker.notNull(id, "id");
    ArgumentChecker.notNull(timeSeries, "timeSeries");
    _data.put(id, timeSeries);
  }

  public void add(final ExternalIdBundle ids, final HistoricalTimeSeries timeSeries) {
    ArgumentChecker.notNull(ids, "ids");
    ArgumentChecker.notNull(timeSeries, "timeSeries");
    for (ExternalId id : ids) {
      _data.put(id, timeSeries);
    }
  }

  public HistoricalTimeSeries get(final ExternalId id) {
    return _data.get(id);
  }

  public HistoricalTimeSeries get(final ExternalIdBundle ids) {
    for (ExternalId id : ids) {
      final HistoricalTimeSeries ts = get(id);
      if (ts != null) {
        return ts;
      }
    }
    return null;
  }

  public Collection<ExternalId> getAllExternalIds() {
    return _data.keySet();
  }

  public Collection<HistoricalTimeSeries> getAllTimeSeries() {
    return _data.values();
  }

  public MutableFudgeMsg toFudgeMsg(final FudgeSerializer context) {
    final MutableFudgeMsg msg = context.newMessage();
    for (Map.Entry<ExternalId, HistoricalTimeSeries> data : _data.entrySet()) {
      context.addToMessageWithClassHeaders(msg, null, 1, data.getKey(), ExternalId.class);
      context.addToMessageWithClassHeaders(msg, null, 2, data.getValue(), HistoricalTimeSeries.class);
    }
    return msg;
  }

  public static HistoricalTimeSeriesBundle fromFudgeMsg(final FudgeDeserializer context, final FudgeMsg msg) {
    final HistoricalTimeSeriesBundle bundle = new HistoricalTimeSeriesBundle();
    final Iterator<FudgeField> keys = msg.getAllByOrdinal(1).iterator();
    final Iterator<FudgeField> values = msg.getAllByOrdinal(2).iterator();
    while (keys.hasNext() && values.hasNext()) {
      final FudgeField key = keys.next();
      final FudgeField value = values.next();
      bundle.add(context.fieldValueToObject(ExternalId.class, key), context.fieldValueToObject(HistoricalTimeSeries.class, value));
    }
    return bundle;
  }

  @Override
  public String toString() {
    return "HistoricalTimeSeriesBundle" + _data.keySet();
  }

}
