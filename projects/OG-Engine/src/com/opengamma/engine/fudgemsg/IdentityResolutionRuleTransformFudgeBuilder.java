/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.fudgemsg;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.engine.function.resolver.IdentityResolutionRuleTransform;

/**
 * Fudge message builder for {@code IdentityResolutionRuleTransform}.
 */
@FudgeBuilderFor(IdentityResolutionRuleTransform.class)
public class IdentityResolutionRuleTransformFudgeBuilder implements FudgeBuilder<IdentityResolutionRuleTransform> {

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final IdentityResolutionRuleTransform object) {
    final MutableFudgeMsg message = serializer.newMessage();
    message.add(0, IdentityResolutionRuleTransform.class.getName());
    return message;
  }

  @Override
  public IdentityResolutionRuleTransform buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
    return IdentityResolutionRuleTransform.INSTANCE;
  }
}
