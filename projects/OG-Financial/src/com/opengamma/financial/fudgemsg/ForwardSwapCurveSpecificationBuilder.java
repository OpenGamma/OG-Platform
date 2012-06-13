/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.financial.analytics.forwardcurve.ForwardSwapCurveSpecification;

/**
 * 
 */
@FudgeBuilderFor(ForwardSwapCurveSpecification.class)
public class ForwardSwapCurveSpecificationBuilder implements FudgeBuilder<ForwardSwapCurveSpecification> {

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final ForwardSwapCurveSpecification object) {
    return null;
  }

  @Override
  public ForwardSwapCurveSpecification buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
    return null;
  }

}
