/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fudgemsg;

import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.financial.analytics.DoubleLabelledMatrix1D;

/**
 * 
 */
@FudgeBuilderFor(DoubleLabelledMatrix1D.class)
public class DoubleLabelledMatrix1DBuilder extends FudgeBuilderBase<DoubleLabelledMatrix1D> {
  private static final String KEYS_NAME = "keys";
  private static final String LABELS_NAME = "labels";
  private static final String VALUES_NAME = "values";

  @Override
  protected void buildMessage(final FudgeSerializationContext context, final MutableFudgeFieldContainer message, final DoubleLabelledMatrix1D object) {
    context.objectToFudgeMsg(message, KEYS_NAME, null, object.getKeys());
    context.objectToFudgeMsg(message, LABELS_NAME, null, object.getLabels());
    context.objectToFudgeMsg(message, VALUES_NAME, null, object.getValues());
  }

  @Override
  public DoubleLabelledMatrix1D buildObject(final FudgeDeserializationContext context, final FudgeFieldContainer message) {
    final Double[] keys = context.fieldValueToObject(Double[].class, message.getByName(KEYS_NAME));
    final Object[] labels = context.fieldValueToObject(Object[].class, message.getByName(LABELS_NAME));
    final double[] values = context.fieldValueToObject(double[].class, message.getByName(VALUES_NAME));
    return new DoubleLabelledMatrix1D(keys, labels, values);
  }

}
