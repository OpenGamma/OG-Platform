/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.marketdatasnapshot;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;
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

  public org.fudgemsg.FudgeMsg toFudgeMsg(FudgeSerializationContext context) {
    MutableFudgeMsg ret = context.newMessage();
    FudgeSerializationContext.addClassHeader(ret, ManageableVolatilitySurfaceSnapshot.class);
    MutableFudgeMsg valuesMsg = context.newMessage();
    for (Entry<Pair<Object, Object>, ValueSnapshot> entry : _values.entrySet()) {
      context.addToMessage(valuesMsg, null, 1, entry.getKey());
      if (entry.getValue() == null) {
        valuesMsg.add(2, IndicatorType.INSTANCE);
      } else {
        context.addToMessage(valuesMsg, null, 2, entry.getValue());
      }
    }
    ret.add("values", valuesMsg);
    return ret;
  }

  @SuppressWarnings("unchecked")
  public static ManageableVolatilitySurfaceSnapshot fromFudgeMsg(FudgeDeserializationContext context, FudgeMsg msg) {

    HashMap<Pair<Object, Object>, ValueSnapshot> values = new HashMap<Pair<Object, Object>, ValueSnapshot>();

    Pair<Object, Object> key = null;
    for (FudgeField fudgeField : msg.getMessage("values")) {
      Integer ordinal = fudgeField.getOrdinal();
      if (ordinal == null) {
        continue;
      }

      int intValue = ordinal.intValue();
      if (intValue == 1) {
        key = context.fieldValueToObject(Pair.class, fudgeField);
      } else if (intValue == 2) {
        ValueSnapshot value = context.fieldValueToObject(ValueSnapshot.class, fudgeField);
        values.put(key, value);
        key = null;
      }
    }

    ManageableVolatilitySurfaceSnapshot ret = new ManageableVolatilitySurfaceSnapshot();
    ret.setValues(values);
    return ret;
  }
}
