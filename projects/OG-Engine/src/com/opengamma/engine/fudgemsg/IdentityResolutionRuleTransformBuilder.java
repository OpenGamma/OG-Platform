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
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.engine.function.resolver.IdentityResolutionRuleTransform;

/**
 * Fudge message builder for {@code IdentityResolutionRuleTransform}.
 */
@FudgeBuilderFor(IdentityResolutionRuleTransform.class)
public class IdentityResolutionRuleTransformBuilder implements FudgeBuilder<IdentityResolutionRuleTransform> {

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializationContext context, final IdentityResolutionRuleTransform object) {
    final MutableFudgeMsg message = context.newMessage();
    message.add(0, IdentityResolutionRuleTransform.class.getName());
    return message;
  }

  @Override
  public IdentityResolutionRuleTransform buildObject(final FudgeDeserializationContext context, final FudgeMsg message) {
    return IdentityResolutionRuleTransform.INSTANCE;
  }
}
