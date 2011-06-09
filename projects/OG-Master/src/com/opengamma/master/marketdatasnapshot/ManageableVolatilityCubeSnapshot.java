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
import org.joda.beans.BeanDefinition;

import com.opengamma.core.marketdatasnapshot.ValueSnapshot;
import com.opengamma.core.marketdatasnapshot.VolatilityCubeSnapshot;
import com.opengamma.core.marketdatasnapshot.VolatilityPoint;

/**
 * 
 */
@BeanDefinition
public class ManageableVolatilityCubeSnapshot implements VolatilityCubeSnapshot {
  
  /**
   * The values in the snapshot.  values are possibly null, representing a point which was requested but unavailable
   */
  private Map<VolatilityPoint, ValueSnapshot> _values;
  
  

  @Override
  public Map<VolatilityPoint, ValueSnapshot> getValues() {
    return _values;
  }

  private void setValues(Map<VolatilityPoint, ValueSnapshot> values) {
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

    ManageableVolatilityCubeSnapshot ret = new ManageableVolatilityCubeSnapshot();
    ret.setValues(values);
    return ret;
  }
}
