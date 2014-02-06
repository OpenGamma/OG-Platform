/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.index;

import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.id.ExternalId;
import com.opengamma.master.security.ManageableSecurityFudgeBuilder;
import com.opengamma.util.time.Tenor;

/**
 * Fudge builder for {@link IndexFamily}
 */
@FudgeBuilderFor(IndexFamily.class)
public class IndexFamilyBuilder implements FudgeBuilder<IndexFamily> {
  /** The exposure function name field */
  private static final String TENOR_FIELD = "tenor";
  /** The external id field */
  private static final String EXTERNAL_ID_FIELD = "id";

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final IndexFamily object) {
    final MutableFudgeMsg message = serializer.newMessage();
    ManageableSecurityFudgeBuilder.toFudgeMsg(serializer, object, message);
    //message.add(null, 0, object.getClass().getName());
    for (final Map.Entry<Tenor, ExternalId> entry : object.getMembers().entrySet()) {
      serializer.addToMessage(message, TENOR_FIELD, null, entry.getKey());
      serializer.addToMessage(message, EXTERNAL_ID_FIELD, null, entry.getValue());
    }
    return message;
  }

  @Override
  public IndexFamily buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
    IndexFamily indexFamily = new IndexFamily();
    ManageableSecurityFudgeBuilder.fromFudgeMsg(deserializer, message, indexFamily);
    final SortedMap<Tenor, ExternalId> entries = new TreeMap<>();
    final List<FudgeField> tenorFields = message.getAllByName(TENOR_FIELD);
    final List<FudgeField> idsFields = message.getAllByName(EXTERNAL_ID_FIELD);
    final int n = idsFields.size();
    if (tenorFields.size() != n) {
      throw new IllegalStateException("Should have one tenor per external id");
    }
    for (int i = 0; i < n; i++) {
      final Tenor tenor = deserializer.fieldValueToObject(Tenor.class, tenorFields.get(i));
      final ExternalId id = deserializer.fieldValueToObject(ExternalId.class, idsFields.get(i));
      entries.put(tenor, id);
    }
    indexFamily.setMembers(entries);
    return indexFamily;
  }

}
