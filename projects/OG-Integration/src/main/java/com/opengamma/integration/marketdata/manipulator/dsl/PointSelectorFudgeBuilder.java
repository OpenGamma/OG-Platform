/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import java.util.Set;
import java.util.regex.Pattern;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.google.common.collect.Sets;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalScheme;

@FudgeBuilderFor(PointSelector.class)
public class PointSelectorFudgeBuilder implements FudgeBuilder<PointSelector> {

  /** Field name for Fudge message. */
  private static final String CALC_CONFIGS = "calculationConfigurationNames";
  /** Field name for Fudge message. */
  private static final String IDS = "ids";
  /** Field name for Fudge message. */
  private static final String ID_MATCH_SCHEME = "idMatchScheme";
  /** Field name for Fudge message. */
  private static final String ID_MATCH_REGEX = "idMatchRegex";

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, PointSelector selector) {
    MutableFudgeMsg msg = serializer.newMessage();
    MutableFudgeMsg calcConfigsMsg = serializer.newMessage();
    for (String calcConfigName : selector.getCalculationConfigurationNames()) {
      serializer.addToMessage(calcConfigsMsg, null, null, calcConfigName);
    }
    serializer.addToMessage(msg, CALC_CONFIGS, null, calcConfigsMsg);
    MutableFudgeMsg idsMsg = serializer.newMessage();
    for (ExternalId id : selector.getIds()) {
      serializer.addToMessage(idsMsg, null, null, id);
    }
    serializer.addToMessage(msg, IDS, null, idsMsg);
    serializer.addToMessage(msg, ID_MATCH_SCHEME, null, selector.getIdMatchScheme().toString());
    serializer.addToMessage(msg, ID_MATCH_REGEX, null, selector.getIdValuePattern().pattern());
    return msg;
  }

  @Override
  public PointSelector buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
    FudgeMsg idsMsg = msg.getMessage(IDS);
    Set<ExternalId> ids = Sets.newHashSet();
    for (FudgeField field : idsMsg) {
      ExternalId id = deserializer.fieldValueToObject(ExternalId.class, field);
      ids.add(id);
    }
    String idMatchScheme = deserializer.fieldValueToObject(String.class, msg.getByName(ID_MATCH_SCHEME));
    String idValuePattern = deserializer.fieldValueToObject(String.class, msg.getByName(ID_MATCH_REGEX));
    FudgeMsg calcConfigsMsg = msg.getMessage(CALC_CONFIGS);
    Set<String> calcConfigNames = Sets.newHashSet();
    for (FudgeField field : calcConfigsMsg) {
      calcConfigNames.add(deserializer.fieldValueToObject(String.class, field));
    }
    return new PointSelector(calcConfigNames, ids, ExternalScheme.of(idMatchScheme), Pattern.compile(idValuePattern));
  }
}
