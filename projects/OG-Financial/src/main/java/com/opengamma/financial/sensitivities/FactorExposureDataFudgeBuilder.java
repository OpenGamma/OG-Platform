/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.sensitivities;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.util.fudgemsg.AbstractFudgeBuilder;

/**
 * A Fudge builder for {@code BondFutureSecurity}.
 */
@FudgeBuilderFor(FactorExposureData.class)
public class FactorExposureDataFudgeBuilder extends AbstractFudgeBuilder implements FudgeBuilder<FactorExposureData> {

  /** Field name. */
  public static final String FACTOR_SET_ID_FIELD_NAME = "factorSetId";
  /** Field name. */
  public static final String FACTOR_TYPE_FIELD_NAME = "factorType";
  /** Field name. */
  private static final String FACTOR_NAME_FIELD_NAME = "factorName";
  /** Field name. */
  private static final String NODE_FIELD_NAME = "node";

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, FactorExposureData object) {
    final MutableFudgeMsg msg = serializer.newMessage();
    serializer.addToMessage(msg, FACTOR_SET_ID_FIELD_NAME, null, object.getFactorSetId());
    serializer.addToMessage(msg, FACTOR_TYPE_FIELD_NAME, null, object.getFactorType().getFactorType());
    serializer.addToMessage(msg, FACTOR_NAME_FIELD_NAME, null, object.getFactorName());
    serializer.addToMessage(msg, NODE_FIELD_NAME, null, object.getNode());
    return msg;
  }

  @Override
  public FactorExposureData buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
    Long factorSetId = msg.getLong(FACTOR_SET_ID_FIELD_NAME);
    String factorTypeStr = msg.getString(FACTOR_TYPE_FIELD_NAME);
    FactorType factorType = FactorType.of(factorTypeStr);
    String factorName = msg.getString(FACTOR_NAME_FIELD_NAME);
    String node = msg.getString(NODE_FIELD_NAME);
    FactorExposureData exposureData = new FactorExposureData(factorSetId, factorType, factorName, node);
    return exposureData;
  }

}
