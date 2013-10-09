/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.marketdatasnapshot.impl;

import java.util.Map;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

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
    for (final ExternalIdBundle target : object.getTargets()) {
      for (final Map.Entry<String, ValueSnapshot> targetEntry : object.getTargetValues(target).entrySet()) {
        final MutableFudgeMsg msg = serializer.newMessage();
        serializer.addToMessage(msg, IDENTIFIERS_FIELD_NAME, null, target);
        serializer.addToMessage(msg, VALUE_NAME_FIELD_NAME, null, targetEntry.getKey());
        serializer.addToMessage(msg, VALUE_FIELD_NAME, null, targetEntry.getValue());
        ret.add(1, msg);
      }
    }
    return ret;
  }

  @Override
  public ManageableUnstructuredMarketDataSnapshot buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
    final ManageableUnstructuredMarketDataSnapshot object = new ManageableUnstructuredMarketDataSnapshot();
    for (final FudgeField fudgeField : message.getAllByOrdinal(1)) {
      final FudgeMsg innerValue = (FudgeMsg) fudgeField.getValue();
      final ExternalIdBundle identifiers = deserializer.fieldValueToObject(ExternalIdBundle.class, innerValue.getByName(IDENTIFIERS_FIELD_NAME));
      final String valueName = innerValue.getFieldValue(String.class, innerValue.getByName(VALUE_NAME_FIELD_NAME));
      final FudgeField valueField = innerValue.getByName(VALUE_FIELD_NAME);
      final ValueSnapshot value = valueField == null ? null : deserializer.fieldValueToObject(ValueSnapshot.class, valueField);
      object.putValue(identifiers, valueName, value);
    }
    return object;
  }

}
