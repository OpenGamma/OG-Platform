/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalIdBundleFudgeBuilder;

/**
 * Fudge message builder for {@code LiveDataSpecification}.
 */
@FudgeBuilderFor(LiveDataSpecification.class)
public class LiveDataSpecificationFudgeBuilder implements FudgeBuilder<LiveDataSpecification> {

  /** Field name. */
  public static final String NORMALIZATION_RULE_SET_ID_FIELD_NAME = "NormalizationRuleSetId";
  /** Field name. */
  public static final String DOMAIN_SPECIFIC_IDS_FIELD_NAME = "DomainSpecificIdentifiers";

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, LiveDataSpecification object) {
    return LiveDataSpecificationFudgeBuilder.toFudgeMsg(serializer, object);
  }

  public static MutableFudgeMsg toFudgeMsg(FudgeSerializer serializer, LiveDataSpecification object) {
    final MutableFudgeMsg msg = serializer.newMessage();
    LiveDataSpecificationFudgeBuilder.toFudgeMsg(serializer, object, msg);
    return msg;
  }

  public static void toFudgeMsg(FudgeSerializer serializer, LiveDataSpecification object, final MutableFudgeMsg msg) {
    msg.add(NORMALIZATION_RULE_SET_ID_FIELD_NAME, object.getNormalizationRuleSetId());
    msg.add(DOMAIN_SPECIFIC_IDS_FIELD_NAME, ExternalIdBundleFudgeBuilder.toFudgeMsg(serializer, object.getIdentifiers()));
  }

  @Override
  public LiveDataSpecification buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
    return LiveDataSpecificationFudgeBuilder.fromFudgeMsg(deserializer, msg);
  }

  public static LiveDataSpecification fromFudgeMsg(FudgeDeserializer deserializer, FudgeMsg msg) {
    String normalizationRuleSetId = msg.getString(NORMALIZATION_RULE_SET_ID_FIELD_NAME);
    ExternalIdBundle ids = ExternalIdBundleFudgeBuilder.fromFudgeMsg(deserializer, msg.getMessage(DOMAIN_SPECIFIC_IDS_FIELD_NAME));
    return new LiveDataSpecification(normalizationRuleSetId, ids);
  }

}
