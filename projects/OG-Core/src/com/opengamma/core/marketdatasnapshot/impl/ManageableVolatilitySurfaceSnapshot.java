/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.marketdatasnapshot.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.fudgemsg.types.IndicatorType;

import com.opengamma.core.marketdatasnapshot.ValueSnapshot;
import com.opengamma.core.marketdatasnapshot.VolatilitySurfaceSnapshot;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public class ManageableVolatilitySurfaceSnapshot implements VolatilitySurfaceSnapshot {
  
  /**
   * The values in the snapshot.
   */
  private Map<Pair<Object, Object>, ValueSnapshot> _values;
  
  
  /**
   * Sets the values field.
   * @param values  the values
   */
  public void setValues(Map<Pair<Object, Object>, ValueSnapshot> values) {
    _values = values;
  }


  @Override
  public Map<Pair<Object, Object>, ValueSnapshot> getValues() {
    return _values;
  }

  /**
   * Creates a Fudge representation of the snapshot:
   * <pre>
   *   message {
   *     message { // map
   *       repeated Pair key = 1;
   *       repeated ValueSnapshot value = 2;
   *     } values;
   *   }
   * </pre>
   * 
   * @param serializer Fudge serialization context, not null
   * @return the message representation of this snapshot
   */
  public FudgeMsg toFudgeMsg(final FudgeSerializer serializer) {
    MutableFudgeMsg ret = serializer.newMessage();
    // TODO: this should not be adding it's own class header; the caller should be doing that, or this be registered as a generic builder for VolatilitySurfaceSnapshot and that class name be added
    FudgeSerializer.addClassHeader(ret, ManageableVolatilitySurfaceSnapshot.class);
    MutableFudgeMsg valuesMsg = serializer.newMessage();
    for (Entry<Pair<Object, Object>, ValueSnapshot> entry : _values.entrySet()) {
      serializer.addToMessage(valuesMsg, null, 1, entry.getKey());
      if (entry.getValue() == null) {
        valuesMsg.add(2, IndicatorType.INSTANCE);
      } else {
        serializer.addToMessage(valuesMsg, null, 2, entry.getValue());
      }
    }
    ret.add("values", valuesMsg);
    return ret;
  }

  // TODO: externalize the message representation to a Fudge builder

  /**
   * Creates a snapshot object from a Fudge message representation. See {@link #toFudgeMsg}
   * for the message format.
   * 
   * @param deserializer the Fudge deserialization context, not null
   * @param msg message containing the snapshot representation, not null
   * @return a snapshot object
   */
  @SuppressWarnings("unchecked")
  public static ManageableVolatilitySurfaceSnapshot fromFudgeMsg(final FudgeDeserializer deserializer, final FudgeMsg msg) {
    final HashMap<Pair<Object, Object>, ValueSnapshot> values = new HashMap<Pair<Object, Object>, ValueSnapshot>();
    Pair<Object, Object> key = null;
    for (FudgeField fudgeField : msg.getMessage("values")) {
      Integer ordinal = fudgeField.getOrdinal();
      if (ordinal == null) {
        continue;
      }
      final int intValue = ordinal.intValue();
      if (intValue == 1) {
        key = deserializer.fieldValueToObject(Pair.class, fudgeField);
      } else if (intValue == 2) {
        ValueSnapshot value = deserializer.fieldValueToObject(ValueSnapshot.class, fudgeField);
        values.put(key, value);
        key = null;
      }
    }
    final ManageableVolatilitySurfaceSnapshot ret = new ManageableVolatilitySurfaceSnapshot();
    ret.setValues(values);
    return ret;
  }
}
