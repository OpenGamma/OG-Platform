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
import com.opengamma.util.tuple.ObjectsPairFudgeBuilder;
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

  /**
   * Creates a Fudge representation of the snapshot:
   * <pre>
   *   message {
   *     message { // map
   *       repeated VolatilityPoint key = 1;
   *       repeated ValueSnapshot|indicator value = 2;
   *     } values;
   *     UnstructuredMarketDataSnapshot otherValues;
   *     message { // map
   *       repeated message { // pair
   *         Tenor first;
   *         Tenor second;
   *       } key = 1;
   *       repeated ValueSnapshot|indicator value = 2;
   *     } strikes;
   *   }
   * </pre>
   * 
   * @param serializer Fudge serialization context, not null
   * @return the message representation of this snapshot
   */
  public org.fudgemsg.FudgeMsg toFudgeMsg(FudgeSerializer serializer) {
    MutableFudgeMsg ret = serializer.newMessage();
    // TODO: this should not be adding it's own class header; the caller should be doing that, or this be registered as a generic builder for VolatilityCubeSnapshot and that class name be added
    FudgeSerializer.addClassHeader(ret, ManageableVolatilityCubeSnapshot.class);
    MutableFudgeMsg valuesMsg = getValuesMessage(serializer);
    MutableFudgeMsg strikesMsg = getStrikesMessage(serializer);
    ret.add("values", valuesMsg);
    if (_otherValues != null) {
      ret.add("otherValues", serializer.objectToFudgeMsg(_otherValues));
    }
    ret.add("strikes", strikesMsg);
    return ret;
  }

  private MutableFudgeMsg getValuesMessage(FudgeSerializer serializer) {
    MutableFudgeMsg valuesMsg = serializer.newMessage();
    if (_values != null) {
      for (Entry<VolatilityPoint, ValueSnapshot> entry : _values.entrySet()) {
        serializer.addToMessage(valuesMsg, null, 1, entry.getKey());
        if (entry.getValue() == null) {
          valuesMsg.add(2, IndicatorType.INSTANCE);
        } else {
          serializer.addToMessage(valuesMsg, null, 2, entry.getValue());
        }
      }
    }
    return valuesMsg;
  }
  
  private MutableFudgeMsg getStrikesMessage(FudgeSerializer serializer) {
    MutableFudgeMsg msg = serializer.newMessage();
    if (_strikes != null) {
      // TODO: is this the best encoding for this message; would a 3-tuple be better (key-x = ordinal 1, key-y = ordinal 2, value = ordinal 3)?
      for (Entry<Pair<Tenor, Tenor>, ValueSnapshot> entry : _strikes.entrySet()) {
        serializer.addToMessage(msg, null, 1, ObjectsPairFudgeBuilder.buildMessage(serializer, entry.getKey(), Tenor.class, Tenor.class));
        if (entry.getValue() == null) {
          msg.add(2, IndicatorType.INSTANCE);
        } else {
          serializer.addToMessage(msg, null, 2, entry.getValue());
        }
      }
    }
    return msg;
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
  public static ManageableVolatilityCubeSnapshot fromFudgeMsg(FudgeDeserializer deserializer, FudgeMsg msg) {
    final ManageableVolatilityCubeSnapshot ret = new ManageableVolatilityCubeSnapshot();
    ret.setValues(readValues(deserializer, msg));
    FudgeField otherValues = msg.getByName("otherValues");
    if (otherValues != null) {
      ret.setOtherValues(deserializer.fieldValueToObject(ManageableUnstructuredMarketDataSnapshot.class, otherValues));
    }
    ret.setStrikes(readStrikes(deserializer, msg));
    return ret;
  }

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
        key = ObjectsPairFudgeBuilder.buildObject(deserializer, (FudgeMsg) fudgeField.getValue(), Tenor.class, Tenor.class);
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
    FudgeMsg valuesMessage = msg.getMessage("values");
    if (valuesMessage == null) {
      return values;
    }
    VolatilityPoint key = null;
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
