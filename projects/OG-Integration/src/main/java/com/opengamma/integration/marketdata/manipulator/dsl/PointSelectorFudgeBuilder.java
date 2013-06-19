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
    if (selector.getCalculationConfigurationNames() != null) {
      for (String calcConfigName : selector.getCalculationConfigurationNames()) {
        serializer.addToMessage(calcConfigsMsg, null, null, calcConfigName);
      }
      serializer.addToMessage(msg, CALC_CONFIGS, null, calcConfigsMsg);
    }
    if (selector.getIds() != null) {
      MutableFudgeMsg idsMsg = serializer.newMessage();
      for (ExternalId id : selector.getIds()) {
        serializer.addToMessage(idsMsg, null, null, id);
      }
      serializer.addToMessage(msg, IDS, null, idsMsg);
    }
    if (selector.getIdMatchScheme() != null) {
      serializer.addToMessage(msg, ID_MATCH_SCHEME, null, selector.getIdMatchScheme().toString());
    }
    if (selector.getIdValuePattern() != null) {
      serializer.addToMessage(msg, ID_MATCH_REGEX, null, selector.getIdValuePattern().pattern());
    }
    return msg;
  }

  @Override
  public PointSelector buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
    Set<ExternalId> ids;
    if (msg.hasField(IDS)) {
      ids = Sets.newHashSet();
      FudgeMsg idsMsg = msg.getMessage(IDS);
      for (FudgeField field : idsMsg) {
        ExternalId id = deserializer.fieldValueToObject(ExternalId.class, field);
        ids.add(id);
      }
    } else {
      ids = null;
    }

    ExternalScheme idMatchScheme;
    if (msg.hasField(ID_MATCH_SCHEME)) {
      String idMatchSchemeStr = deserializer.fieldValueToObject(String.class, msg.getByName(ID_MATCH_SCHEME));
      idMatchScheme = ExternalScheme.of(idMatchSchemeStr);
    } else {
      idMatchScheme = null;
    }

    Pattern idValuePattern;
    if (msg.hasField(ID_MATCH_REGEX)) {
      String idValuePatternStr = deserializer.fieldValueToObject(String.class, msg.getByName(ID_MATCH_REGEX));
      idValuePattern = Pattern.compile(idValuePatternStr);
    } else {
      idValuePattern = null;
    }

    Set<String> calcConfigNames;
    if (msg.hasField(CALC_CONFIGS)) {
      calcConfigNames = Sets.newHashSet();
      FudgeMsg calcConfigsMsg = msg.getMessage(CALC_CONFIGS);
      for (FudgeField field : calcConfigsMsg) {
        calcConfigNames.add(deserializer.fieldValueToObject(String.class, field));
      }
    } else {
      calcConfigNames = null;
    }
    return new PointSelector(calcConfigNames, ids, idMatchScheme, idValuePattern);
  }
}
