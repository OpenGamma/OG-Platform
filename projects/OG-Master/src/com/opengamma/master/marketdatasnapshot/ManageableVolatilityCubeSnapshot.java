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

import com.opengamma.core.marketdatasnapshot.UnstructuredMarketDataSnapshot;
import com.opengamma.core.marketdatasnapshot.ValueSnapshot;
import com.opengamma.core.marketdatasnapshot.VolatilityCubeSnapshot;
import com.opengamma.core.marketdatasnapshot.VolatilityPoint;

/**
 * 
 */
public class ManageableVolatilityCubeSnapshot implements VolatilityCubeSnapshot {
  
  /**
   * The values in the snapshot.
   */
  private Map<VolatilityPoint, ValueSnapshot> _values;
  
  private UnstructuredMarketDataSnapshot _otherValues;

  @Override
  public Map<VolatilityPoint, ValueSnapshot> getValues() {
    return _values;
  }

  public void setValues(Map<VolatilityPoint, ValueSnapshot> values) {
    _values = values;
  }


  public org.fudgemsg.FudgeMsg toFudgeMsg(FudgeSerializationContext context) {
    MutableFudgeMsg ret = context.newMessage();
    FudgeSerializationContext.addClassHeader(ret, ManageableVolatilityCubeSnapshot.class);
    MutableFudgeMsg valuesMsg = context.newMessage();
    for (Entry<VolatilityPoint, ValueSnapshot> entry : _values.entrySet()) {
      context.addToMessage(valuesMsg, null, 1, entry.getKey());
      if (entry.getValue() == null) {
        valuesMsg.add(2, IndicatorType.INSTANCE);
      } else {
        context.addToMessage(valuesMsg, null, 2, entry.getValue());
      }
    }
    ret.add("values", valuesMsg);
    ret.add("otherValues", context.objectToFudgeMsg(_otherValues));
    return ret;
  }

  public static ManageableVolatilityCubeSnapshot fromFudgeMsg(FudgeDeserializationContext context, FudgeMsg msg) {

    HashMap<VolatilityPoint, ValueSnapshot> values = new HashMap<VolatilityPoint, ValueSnapshot>();

    VolatilityPoint key = null;
    for (FudgeField fudgeField : msg.getMessage("values")) {
      Integer ordinal = fudgeField.getOrdinal();
      if (ordinal == null) {
        continue;
      }

      int intValue = ordinal.intValue();
      if (intValue == 1) {
        key = context.fieldValueToObject(VolatilityPoint.class, fudgeField);
      } else if (intValue == 2) {
        ValueSnapshot value = context.fieldValueToObject(ValueSnapshot.class, fudgeField);
        values.put(key, value);
        key = null;
      }
    }

    UnstructuredMarketDataSnapshot otherValues = context.fieldValueToObject(ManageableUnstructuredMarketDataSnapshot.class, msg.getByName("otherValues"));
    ManageableVolatilityCubeSnapshot ret = new ManageableVolatilityCubeSnapshot();
    ret.setValues(values);
    ret.setOtherValues(otherValues);
    return ret;
  }

  public void setOtherValues(UnstructuredMarketDataSnapshot otherValues) {
    _otherValues = otherValues;
  }

  public UnstructuredMarketDataSnapshot getOtherValues() {
    return _otherValues;
  }
}
