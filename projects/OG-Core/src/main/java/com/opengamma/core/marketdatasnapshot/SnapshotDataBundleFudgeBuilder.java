/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.marketdatasnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;

/**
 * Encodes/decodes a {@link SnapshotDataBundle} to/from its Fudge representation.
 * 
 * <pre>
 * message SnapshotDataBundle {
 *   message {
 *     required repeated ExternalId identifier;
 *     required double value;
 *   } data[];
 * }
 * </pre>
 */
@FudgeBuilderFor(SnapshotDataBundle.class)
public class SnapshotDataBundleFudgeBuilder implements FudgeBuilder<SnapshotDataBundle> {

  private static final String DATA_FIELD = "data";
  private static final String IDENTIFIER_FIELD = "identifier";
  private static final String VALUE_FIELD = "value";

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final SnapshotDataBundle object) {
    final MutableFudgeMsg msg = serializer.newMessage();
    final MutableFudgeMsg data = msg.addSubMessage(DATA_FIELD, null);
    for (final Map.Entry<ExternalIdBundle, Double> dataValue : object.getDataPointSet()) {
      final MutableFudgeMsg dataMsg = data.addSubMessage(null, null);
      for (final ExternalId identifier : dataValue.getKey()) {
        serializer.addToMessage(dataMsg, IDENTIFIER_FIELD, null, identifier);
      }
      dataMsg.add(VALUE_FIELD, dataValue.getValue());
    }
    return msg;
  }

  @Override
  public SnapshotDataBundle buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
    final SnapshotDataBundle object = new SnapshotDataBundle();
    for (final FudgeField dataField : message.getMessage(DATA_FIELD)) {
      if (dataField.getValue() instanceof FudgeMsg) {
        final FudgeMsg dataMsg = (FudgeMsg) dataField.getValue();
        final List<FudgeField> identifierFields = dataMsg.getAllByName(IDENTIFIER_FIELD);
        if (identifierFields.size() == 1) {
          object.setDataPoint(deserializer.fieldValueToObject(ExternalId.class, identifierFields.get(0)), dataMsg.getDouble(VALUE_FIELD));
        } else {
          final List<ExternalId> identifiers = new ArrayList<ExternalId>(identifierFields.size());
          for (final FudgeField identifier : identifierFields) {
            identifiers.add(deserializer.fieldValueToObject(ExternalId.class, identifier));
          }
          object.setDataPoint(ExternalIdBundle.of(identifiers), dataMsg.getDouble(VALUE_FIELD));
        }
      }
    }
    return object;
  }

}
