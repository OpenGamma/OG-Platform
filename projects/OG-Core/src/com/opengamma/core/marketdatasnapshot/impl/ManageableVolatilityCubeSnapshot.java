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

import com.opengamma.core.marketdatasnapshot.UnstructuredMarketDataSnapshot;
import com.opengamma.core.marketdatasnapshot.ValueSnapshot;
import com.opengamma.core.marketdatasnapshot.VolatilityCubeSnapshot;
import com.opengamma.core.marketdatasnapshot.VolatilityPoint;
import com.opengamma.util.time.Tenor;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public class ManageableVolatilityCubeSnapshot implements VolatilityCubeSnapshot {
  
  /**
   * The values in the snapshot.
   */
  private Map<VolatilityPoint, ValueSnapshot> _values;
  
  private UnstructuredMarketDataSnapshot _otherValues;
  
  private Map<Pair<Tenor, Tenor>, ValueSnapshot> _strikes;

  @Override
  public Map<VolatilityPoint, ValueSnapshot> getValues() {
    return _values;
  }

  public void setValues(Map<VolatilityPoint, ValueSnapshot> values) {
    _values = values;
  }


  public org.fudgemsg.FudgeMsg toFudgeMsg(FudgeSerializer serializer) {
    MutableFudgeMsg ret = serializer.newMessage();
    FudgeSerializer.addClassHeader(ret, ManageableVolatilityCubeSnapshot.class);
    MutableFudgeMsg valuesMsg = getValuesMessage(serializer);
    MutableFudgeMsg strikesMsg = getStrikesMessage(serializer);
    
    ret.add("values", valuesMsg);
    ret.add("otherValues", serializer.objectToFudgeMsg(_otherValues));
    ret.add("strikes", strikesMsg);
    
    return ret;
  }

  private MutableFudgeMsg getValuesMessage(FudgeSerializer serializer) {
    MutableFudgeMsg valuesMsg = serializer.newMessage();
    for (Entry<VolatilityPoint, ValueSnapshot> entry : _values.entrySet()) {
      serializer.addToMessage(valuesMsg, null, 1, entry.getKey());
      if (entry.getValue() == null) {
        valuesMsg.add(2, IndicatorType.INSTANCE);
      } else {
        serializer.addToMessage(valuesMsg, null, 2, entry.getValue());
      }
    }
    return valuesMsg;
  }
  
  private MutableFudgeMsg getStrikesMessage(FudgeSerializer serializer) {
    MutableFudgeMsg msg = serializer.newMessage();
    for (Entry<Pair<Tenor, Tenor>, ValueSnapshot> entry : _strikes.entrySet()) {
      serializer.addToMessage(msg, null, 1, entry.getKey());
      if (entry.getValue() == null) {
        msg.add(2, IndicatorType.INSTANCE);
      } else {
        serializer.addToMessage(msg, null, 2, entry.getValue());
      }
    }
    return msg;
  }

  public static ManageableVolatilityCubeSnapshot fromFudgeMsg(FudgeDeserializer deserializer, FudgeMsg msg) {

    HashMap<VolatilityPoint, ValueSnapshot> values = readValues(deserializer, msg);
    UnstructuredMarketDataSnapshot otherValues = deserializer.fieldValueToObject(ManageableUnstructuredMarketDataSnapshot.class, msg.getByName("otherValues"));
    HashMap<Pair<Tenor, Tenor>, ValueSnapshot> strikes = readStrikes(deserializer, msg);
        
    ManageableVolatilityCubeSnapshot ret = new ManageableVolatilityCubeSnapshot();
    ret.setValues(values);
    ret.setOtherValues(otherValues);
    ret.setStrikes(strikes);
    return ret;
  }

  @SuppressWarnings("unchecked")
  private static HashMap<Pair<Tenor, Tenor>, ValueSnapshot> readStrikes(FudgeDeserializer deserializer, FudgeMsg msg) {
    HashMap<Pair<Tenor, Tenor>, ValueSnapshot> values = new HashMap<Pair<Tenor, Tenor>, ValueSnapshot>();

    FudgeMsg valuesMessage = msg.getMessage("strikes");
    if (valuesMessage == null) {
      return values;
    }
    Pair<Tenor, Tenor> key = null;
    for (FudgeField fudgeField : valuesMessage) {
      Integer ordinal = fudgeField.getOrdinal();
      if (ordinal == null) {
        continue;
      }

      int intValue = ordinal.intValue();
      if (intValue == 1) {
        key = deserializer.fieldValueToObject(Pair.class, fudgeField);
      } else if (intValue == 2) {
        ValueSnapshot value = deserializer.fieldValueToObject(ValueSnapshot.class, fudgeField);
        values.put(key, value);
        key = null;
      }
    }
    return values;
  }

  private static HashMap<VolatilityPoint, ValueSnapshot> readValues(FudgeDeserializer deserializer, FudgeMsg msg) {
    HashMap<VolatilityPoint, ValueSnapshot> values = new HashMap<VolatilityPoint, ValueSnapshot>();

    VolatilityPoint key = null;
    FudgeMsg valuesMessage = msg.getMessage("values");
    for (FudgeField fudgeField : valuesMessage) {
      Integer ordinal = fudgeField.getOrdinal();
      if (ordinal == null) {
        continue;
      }

      int intValue = ordinal.intValue();
      if (intValue == 1) {
        key = deserializer.fieldValueToObject(VolatilityPoint.class, fudgeField);
      } else if (intValue == 2) {
        ValueSnapshot value = deserializer.fieldValueToObject(ValueSnapshot.class, fudgeField);
        values.put(key, value);
        key = null;
      }
    }
    return values;
  }

  public void setOtherValues(UnstructuredMarketDataSnapshot otherValues) {
    _otherValues = otherValues;
  }

  public UnstructuredMarketDataSnapshot getOtherValues() {
    return _otherValues;
  }

  public Map<Pair<Tenor, Tenor>, ValueSnapshot> getStrikes() {
    return _strikes;
  }

  public void setStrikes(Map<Pair<Tenor, Tenor>, ValueSnapshot> strikes) {
    _strikes = strikes;
  }
}
