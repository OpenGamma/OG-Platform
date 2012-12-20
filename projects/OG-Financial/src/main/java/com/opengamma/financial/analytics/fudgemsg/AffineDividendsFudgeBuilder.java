/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fudgemsg;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.analytics.financial.equity.variance.pricing.AffineDividends;

/**
 *
 */
@FudgeBuilderFor(AffineDividends.class)
public class AffineDividendsFudgeBuilder extends AbstractFudgeBuilder<AffineDividends> {
  private static final String TAU_FIELD = "tau";
  private static final String ALPHA_FIELD = "alpha";
  private static final String BETA_FIELD = "beta";

  @Override
  public AffineDividends buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
    final double[] tau = deserializer.fieldValueToObject(double[].class, message.getByName(TAU_FIELD));
    final double[] alpha = deserializer.fieldValueToObject(double[].class, message.getByName(ALPHA_FIELD));
    final double[] beta = deserializer.fieldValueToObject(double[].class, message.getByName(BETA_FIELD));
    return new AffineDividends(tau, alpha, beta);
  }

  @Override
  protected void buildMessage(final FudgeSerializer serializer, final MutableFudgeMsg message, final AffineDividends object) {
    serializer.addToMessage(message, TAU_FIELD, null, object.getTau());
    serializer.addToMessage(message, ALPHA_FIELD, null, object.getAlpha());
    serializer.addToMessage(message, BETA_FIELD, null, object.getBeta());
  }

}
