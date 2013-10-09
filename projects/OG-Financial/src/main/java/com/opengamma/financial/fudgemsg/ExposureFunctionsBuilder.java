/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.financial.analytics.curve.exposure.ExposureFunctions;
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdFudgeBuilder;

/**
 * Fudge builder for {@link ExposureFunctions}
 */
@FudgeBuilderFor(ExposureFunctions.class)
public class ExposureFunctionsBuilder implements FudgeBuilder<ExposureFunctions> {
  /** The name field */
  private static final String NAME_FIELD = "name";
  /** The exposure function name field */
  private static final String EXPOSURE_FUNCTION_FIELD = "exposureFunction";
  /** The external id field */
  private static final String EXTERNAL_ID_FIELD = "id";
  /** The curve construction configuration name field */
  private static final String CONFIGURATION_FIELD = "configuration";
  /** The unique id field */
  private static final String UNIQUE_ID_FIELD = "uniqueId";

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final ExposureFunctions object) {
    final MutableFudgeMsg message = serializer.newMessage();
    message.add(null, 0, object.getClass().getName());
    message.add(NAME_FIELD, object.getName());
    for (final String exposureFunction : object.getExposureFunctions()) {
      message.add(EXPOSURE_FUNCTION_FIELD, exposureFunction);
    }
    for (final Map.Entry<ExternalId, String> entry : object.getIdsToNames().entrySet()) {
      serializer.addToMessage(message, EXTERNAL_ID_FIELD, null, entry.getKey());
      message.add(CONFIGURATION_FIELD, entry.getValue());
    }
    if (object.getUniqueId() != null) {
      message.add(UNIQUE_ID_FIELD, null, UniqueIdFudgeBuilder.toFudgeMsg(serializer, object.getUniqueId()));
    }
    return message;
  }

  @Override
  public ExposureFunctions buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
    final String name = message.getString(NAME_FIELD);
    final List<String> exposureFunctions = new ArrayList<>();
    final List<FudgeField> exposureFunctionsFields = message.getAllByName(EXPOSURE_FUNCTION_FIELD);
    for (final FudgeField field : exposureFunctionsFields) {
      exposureFunctions.add((String) field.getValue());
    }
    final Map<ExternalId, String> idsToNames = new HashMap<>();
    final List<FudgeField> idsFields = message.getAllByName(EXTERNAL_ID_FIELD);
    final List<FudgeField> namesFields = message.getAllByName(CONFIGURATION_FIELD);
    final int n = idsFields.size();
    if (namesFields.size() != n) {
      throw new IllegalStateException("Should have one configuration name per external id");
    }
    for (int i = 0; i < n; i++) {
      final ExternalId id = deserializer.fieldValueToObject(ExternalId.class, idsFields.get(i));
      final String configuration = (String) namesFields.get(i).getValue();
      idsToNames.put(id, configuration);
    }
    final ExposureFunctions functions = new ExposureFunctions(name, exposureFunctions, idsToNames);
    final FudgeField uniqueId = message.getByName(UNIQUE_ID_FIELD);
    if (uniqueId != null) {
      functions.setUniqueId(deserializer.fieldValueToObject(UniqueId.class, uniqueId));
    }
    return functions;
  }

}
