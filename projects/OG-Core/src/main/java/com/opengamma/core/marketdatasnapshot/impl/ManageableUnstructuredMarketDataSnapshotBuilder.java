/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.marketdatasnapshot.impl;

import java.util.HashMap;
import java.util.Map;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.google.common.collect.Maps;
import com.opengamma.core.marketdatasnapshot.ValueSnapshot;
import com.opengamma.id.ExternalIdBundle;

/**
 * Fudge message builder for {@link ManageableUnstructuredMarketDataSnapshot}
 * 
 * <pre>
 *   message {
 *     repeated message { // set
 *       ExternalIdBundle identifiers;
 *       string valueName;
 *       ValueSnapshot value;
 *     } value = 1;
 *   }
 * </pre>
 */
@FudgeBuilderFor(ManageableUnstructuredMarketDataSnapshot.class)
public class ManageableUnstructuredMarketDataSnapshotBuilder implements FudgeBuilder<ManageableUnstructuredMarketDataSnapshot> {

  /** Field name. */
  public static final String IDENTIFIERS_FIELD_NAME = "identifiers";
  /** Field name. */
  public static final String VALUE_NAME_FIELD_NAME = "valueName";
  /** Field name. */
  public static final String VALUE_FIELD_NAME = "value";

  // TODO: This is not the most efficient representation. Either keep the repeat and use a 3-tuple approach rather
  // than a set for the outer message, or use a map of value specs to a map of names and values. Which is the more
  // concise depends on how likely there are to be multiple named values for each given specification.

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final ManageableUnstructuredMarketDataSnapshot object) {
    final MutableFudgeMsg ret = serializer.newMessage();

    for (final Map.Entry<ExternalIdBundle, Map<String, ValueSnapshot>> subMap : object.getValues().entrySet()) {

      for (final Map.Entry<String, ValueSnapshot> subMapEntry : subMap.getValue().entrySet()) {
        final MutableFudgeMsg msg = serializer.newMessage();

        serializer.addToMessage(msg, IDENTIFIERS_FIELD_NAME, null, subMap.getKey());
        serializer.addToMessage(msg, VALUE_NAME_FIELD_NAME, null, subMapEntry.getKey());
        serializer.addToMessage(msg, VALUE_FIELD_NAME, null, subMapEntry.getValue());

        ret.add(1, msg);
      }
    }
    return ret;
  }

  @Override
  public ManageableUnstructuredMarketDataSnapshot buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
    final Map<ExternalIdBundle, Map<String, ValueSnapshot>> values = Maps.newHashMap();

    for (final FudgeField fudgeField : message.getAllByOrdinal(1)) {
      final FudgeMsg innerValue = (FudgeMsg) fudgeField.getValue();
      final ExternalIdBundle spec = deserializer.fieldValueToObject(ExternalIdBundle.class, innerValue.getByName(IDENTIFIERS_FIELD_NAME));
      final String valueName = innerValue.getFieldValue(String.class, innerValue.getByName(VALUE_NAME_FIELD_NAME));
      final ValueSnapshot value = deserializer.fieldValueToObject(ValueSnapshot.class, innerValue.getByName(VALUE_FIELD_NAME));
      if (!values.containsKey(spec)) {
        values.put(spec, new HashMap<String, ValueSnapshot>());
      }
      values.get(spec).put(valueName, value);
    }

    final ManageableUnstructuredMarketDataSnapshot ret = new ManageableUnstructuredMarketDataSnapshot();
    ret.setValues(values);
    return ret;
  }

}
