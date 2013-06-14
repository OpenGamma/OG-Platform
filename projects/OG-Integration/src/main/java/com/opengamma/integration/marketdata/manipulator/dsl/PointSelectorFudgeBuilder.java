/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.id.ExternalId;

/**
 *
 */
@FudgeBuilderFor(PointSelector.class)
public class PointSelectorFudgeBuilder implements FudgeBuilder<PointSelector> {

  private static final String CALC_CONFIG = "calculationConfigurationName";
  private static final String ID = "id";


  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, PointSelector object) {
    MutableFudgeMsg msg = serializer.newMessage();
    serializer.addToMessage(msg, CALC_CONFIG, null, object.getCalculationConfigurationName());
    serializer.addToMessage(msg, ID, null, object.getId().toString());
    return msg;
  }

  @Override
  public PointSelector buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
    String calcConfigName = deserializer.fieldValueToObject(String.class, msg.getByName(CALC_CONFIG));
    String idStr = deserializer.fieldValueToObject(String.class, msg.getByName(ID));
    return new PointSelector(ExternalId.parse(idStr), calcConfigName);
  }
}
