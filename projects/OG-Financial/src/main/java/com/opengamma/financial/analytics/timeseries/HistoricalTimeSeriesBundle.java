/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.timeseries;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.google.common.collect.Iterators;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.function.Function;
import com.opengamma.util.tuple.Triple;

/**
 * A collection of historical time-series objects. The time-series are keyed by the originally requested data field
 * name and the external ids associated with them. The original field is not generally necessary but is to allow
 * override operations to identify the nature of the data points in the time series they are being applied to. The
 * time-series are maintained in the order in which they were added, for example tenor order, and are iterable in this
 * order.
 */
public final class HistoricalTimeSeriesBundle {

  private static final class Entry implements Iterable<HistoricalTimeSeries> {

    // Maintain insertion order, so that the bundle can be ordered e.g. by tenor
    private final Map<ExternalIdBundle, HistoricalTimeSeries> _timeSeries = new LinkedHashMap<ExternalIdBundle, HistoricalTimeSeries>();
    private Map<ExternalId, HistoricalTimeSeries> _lookup;

    private HistoricalTimeSeries getImpl(final ExternalId id) {
      return _lookup.get(id);
    }

    private void addImpl(final ExternalId id, final HistoricalTimeSeries hts) {
      _lookup.put(id, hts);
    }

    private void addImpl(final ExternalIdBundle ids, final HistoricalTimeSeries hts) {
      for (ExternalId id : ids) {
        addImpl(id, hts);
      }
    }
    
    /*
     * Added synchronized here because the hashmap was getting corrupted by concurrent access. 
     */
    private synchronized void lookup() {
      if (_lookup != null) {
        return;
      }
      _lookup = new HashMap<ExternalId, HistoricalTimeSeries>();
      for (Map.Entry<ExternalIdBundle, HistoricalTimeSeries> e : _timeSeries.entrySet()) {
        addImpl(e.getKey(), e.getValue());
      }
    }

    public HistoricalTimeSeries get(final ExternalIdBundle bundle) {
      lookup();
      for (ExternalId id : bundle) {
        final HistoricalTimeSeries hts = getImpl(id);
        if (hts != null) {
          return hts;
        }
      }
      return null;
    }

    public HistoricalTimeSeries get(final ExternalId id) {
      lookup();
      return getImpl(id);
    }

    /*
     * Added synchronized here because the hashmap was getting corrupted by concurrent access. 
     */
    public synchronized void add(final ExternalIdBundle bundle, final HistoricalTimeSeries timeSeries) {
      _timeSeries.put(bundle, timeSeries);
      if (_lookup != null) {
        addImpl(bundle, timeSeries);
      }
    }
    
    public int size() {
      return _timeSeries.size();
    }
    
    @Override
    public Iterator<HistoricalTimeSeries> iterator() {
      return _timeSeries.values().iterator();
    }

    public MutableFudgeMsg toFudgeMsg(final FudgeSerializer context) {
      final MutableFudgeMsg msg = context.newMessage();
      for (Map.Entry<ExternalIdBundle, HistoricalTimeSeries> data : _timeSeries.entrySet()) {
        context.addToMessageWithClassHeaders(msg, null, 1, data.getKey(), ExternalIdBundle.class);
        context.addToMessageWithClassHeaders(msg, null, 2, data.getValue(), HistoricalTimeSeries.class);
      }
      return msg;
    }

    public static Entry fromFudgeMsg(final FudgeDeserializer context, final FudgeMsg msg) {
      final Entry e = new Entry();
      final Iterator<FudgeField> keys = msg.getAllByOrdinal(1).iterator();
      final Iterator<FudgeField> values = msg.getAllByOrdinal(2).iterator();
      while (keys.hasNext() && values.hasNext()) {
        final FudgeField key = keys.next();
        final FudgeField value = values.next();
        e.add(context.fieldValueToObject(ExternalIdBundle.class, key), context.fieldValueToObject(HistoricalTimeSeries.class, value));
      }
      return e;
    }

    @Override
    public String toString() {
      return _timeSeries.keySet().toString();
    }

  }

  private final Map<String, Entry> _data = new HashMap<String, Entry>();

  public void add(final String field, final ExternalIdBundle idBundle, final HistoricalTimeSeries timeSeries) {
    ArgumentChecker.notNull(field, "field");
    ArgumentChecker.notNull(idBundle, "idBundle");
    ArgumentChecker.notNull(timeSeries, "timeSeries");
    Entry e = _data.get(field);
    if (e == null) {
      e = new Entry();
      _data.put(field, e);
    }
    e.add(idBundle, timeSeries);
  }

  public HistoricalTimeSeries get(final String field, final ExternalId id) {
    final Entry e = _data.get(field);
    if (e == null) {
      return null;
    }
    return e.get(id);
  }

  public HistoricalTimeSeries get(final String field, final ExternalIdBundle ids) {
    final Entry e = _data.get(field);
    if (e == null) {
      return null;
    }
    return e.get(ids);
  }
  
  public int size(final String field) {
    ArgumentChecker.notNull(field, "field");
    final Entry e = _data.get(field);
    if (e == null) {
      return 0;
    }
    return e.size();
  }
  
  /**
   * Gets an iterator for the time-series stored under the given field name. This iterates over the time-series in the
   * same order as they were added.
   * 
   * @param field  the data field, not null
   * @return an iterator, not null
   */
  public Iterator<HistoricalTimeSeries> iterator(final String field) {
    ArgumentChecker.notNull(field, "field");
    final Entry e = _data.get(field);
    if (e == null) {
      return Iterators.emptyIterator();
    }
    return e.iterator();
  }

  protected HistoricalTimeSeriesBundle apply(final Function<Triple<String, ExternalIdBundle, HistoricalTimeSeries>, HistoricalTimeSeries> function) {
    final HistoricalTimeSeriesBundle result = new HistoricalTimeSeriesBundle();
    for (Map.Entry<String, Entry> fieldTimeSeries : _data.entrySet()) {
      final Entry newEntry = new Entry();
      for (Map.Entry<ExternalIdBundle, HistoricalTimeSeries> timeSeries : fieldTimeSeries.getValue()._timeSeries.entrySet()) {
        newEntry._timeSeries.put(timeSeries.getKey(), function.apply(
            Triple.of(fieldTimeSeries.getKey(), timeSeries.getKey(), timeSeries.getValue())));
      }
      result._data.put(fieldTimeSeries.getKey(), newEntry);
    }
    return result;
  }

  public MutableFudgeMsg toFudgeMsg(final FudgeSerializer context) {
    final MutableFudgeMsg msg = context.newMessage();
    for (Map.Entry<String, Entry> entry : _data.entrySet()) {
      msg.add(entry.getKey(), entry.getValue().toFudgeMsg(context));
    }
    return msg;
  }

  public static HistoricalTimeSeriesBundle fromFudgeMsg(final FudgeDeserializer context, final FudgeMsg msg) {
    final HistoricalTimeSeriesBundle bundle = new HistoricalTimeSeriesBundle();
    for (FudgeField field : msg) {
      if (field.getValue() instanceof FudgeMsg) {
        bundle._data.put(field.getName(), Entry.fromFudgeMsg(context, (FudgeMsg) field.getValue()));
      }
    }
    return bundle;
  }

  @Override
  public String toString() {
    return "HistoricalTimeSeriesBundle" + _data;
  }

}
