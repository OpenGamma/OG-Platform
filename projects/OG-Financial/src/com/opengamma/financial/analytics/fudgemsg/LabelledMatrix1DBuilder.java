/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fudgemsg;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.google.common.primitives.Doubles;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.analytics.DoubleLabelledMatrix1D;

/**
 * 
 */
final class LabelledMatrix1DBuilder {
  private static final String MATRIX_FIELD = "matrix";
  private static final int LABEL_TYPE_ORDINAL = 0;
  private static final int KEY_ORDINAL = 1;
  private static final int LABEL_ORDINAL = 2;
  private static final int VALUE_ORDINAL = 3;

  private LabelledMatrix1DBuilder() {
  }

  @FudgeBuilderFor(DoubleLabelledMatrix1D.class)
  public static final class DoubleLabelledMatrix1DBuilder extends FudgeBuilderBase<DoubleLabelledMatrix1D> {

    @Override
    protected void buildMessage(final FudgeSerializationContext context, final MutableFudgeFieldContainer message, final DoubleLabelledMatrix1D object) {
      MutableFudgeFieldContainer msg = context.newMessage();
      
      Double[] keys = object.getKeys();
      Object[] labels = object.getLabels();
      double[] values = object.getValues();
      for (int i = 0; i < object.size(); i++) {
        msg.add(LABEL_TYPE_ORDINAL, labels[i].getClass().getName());
        msg.add(KEY_ORDINAL, keys[i]);
        context.objectToFudgeMsg(msg, null, LABEL_ORDINAL, labels[i]);
        msg.add(VALUE_ORDINAL, values[i]);
      }
      
      message.add(MATRIX_FIELD, msg);
    }

    @Override
    public DoubleLabelledMatrix1D buildObject(final FudgeDeserializationContext context, final FudgeFieldContainer message) {
      FudgeFieldContainer msg = message.getMessage(MATRIX_FIELD);
      
      final Queue<String> labelTypes = new LinkedList<String>();
      final Queue<FudgeField> labelValues = new LinkedList<FudgeField>();
      
      final List<Double> keys = new LinkedList<Double>();
      final List<Object> labels = new LinkedList<Object>();
      final List<Double> values = new LinkedList<Double>();
      
      for (FudgeField field : msg) {
        switch (field.getOrdinal()) {
          case LABEL_TYPE_ORDINAL:
            labelTypes.add((String) field.getValue());
            break;
          case KEY_ORDINAL:
            keys.add((Double) field.getValue());
            break;
          case LABEL_ORDINAL:
            labelValues.add(field);
            break;
          case VALUE_ORDINAL:
            values.add((Double) field.getValue());
            break;
        }
        
        if (!labelTypes.isEmpty() && !labelValues.isEmpty()) {
          // Have a type and a value, which can be consumed
          String labelType = labelTypes.remove();
          Class<?> labelClass;
          try {
            labelClass = Class.forName(labelType);
          } catch (ClassNotFoundException ex) {
            throw new OpenGammaRuntimeException("Could not deserialize label of type " + labelType, ex);
          }
          FudgeField labelValue = labelValues.remove();
          Object label = context.fieldValueToObject(labelClass, labelValue);
          labels.add(label);
        }
      }
      
      final int matrixSize = keys.size();
      final Double[] keysArray = new Double[matrixSize];
      keys.toArray(keysArray);
      final Object[] labelsArray = new Object[matrixSize];
      labels.toArray(labelsArray);
      final double[] valuesArray = Doubles.toArray(values);
      return new DoubleLabelledMatrix1D(keysArray, labelsArray, valuesArray);
    }
  }

  //TODO add LocalDate and ZonedDateTime versions
}
