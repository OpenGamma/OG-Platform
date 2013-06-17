/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import java.util.regex.Pattern;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalScheme;

/**
 *
 */
@FudgeBuilderFor(PointSelector.class)
public class PointSelectorFudgeBuilder implements FudgeBuilder<PointSelector> {

  /** Field name for Fudge message. */
  private static final String CALC_CONFIG = "calculationConfigurationName";
  /** Field name for Fudge message. */
  private static final String ID = "id";
  /** Field name for Fudge message. */
  private static final String ID_MATCH_SCHEME = "idMatchScheme";
  /** Field name for Fudge message. */
  private static final String ID_MATCH_REGEX = "idMatchRegex";


  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, PointSelector selector) {
    MutableFudgeMsg msg = serializer.newMessage();
    serializer.addToMessage(msg, CALC_CONFIG, null, selector.getCalculationConfigurationName());
    serializer.addToMessage(msg, ID, null, selector.getId().toString());
    serializer.addToMessage(msg, ID_MATCH_SCHEME, null, selector.getIdMatchScheme().toString());
    serializer.addToMessage(msg, ID_MATCH_REGEX, null, selector.getIdValuePattern().pattern());
    return msg;
  }

  @Override
  public PointSelector buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
    String calcConfigName = deserializer.fieldValueToObject(String.class, msg.getByName(CALC_CONFIG));
    String idStr = deserializer.fieldValueToObject(String.class, msg.getByName(ID));
    String idMatchScheme = deserializer.fieldValueToObject(String.class, msg.getByName(ID_MATCH_SCHEME));
    String idValuePattern = deserializer.fieldValueToObject(String.class, msg.getByName(ID_MATCH_REGEX));
    return new PointSelector(calcConfigName, ExternalId.parse(idStr), ExternalScheme.of(idMatchScheme), Pattern.compile(idValuePattern));
  }
}
