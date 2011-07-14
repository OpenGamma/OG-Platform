/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fudgemsg;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import javax.time.calendar.LocalDate;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;
import org.fudgemsg.types.FudgeDate;

import com.google.common.primitives.Doubles;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.analytics.CurrencyLabelledMatrix1D;
import com.opengamma.financial.analytics.DoubleLabelledMatrix1D;
import com.opengamma.financial.analytics.LocalDateLabelledMatrix1D;
import com.opengamma.util.money.Currency;

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
  public static final class DoubleLabelledMatrix1DBuilder extends AbstractFudgeBuilder<DoubleLabelledMatrix1D> {

    @Override
    protected void buildMessage(final FudgeSerializationContext context, final MutableFudgeMsg message, final DoubleLabelledMatrix1D object) {
      final MutableFudgeMsg msg = context.newMessage();

      final Double[] keys = object.getKeys();
      final Object[] labels = object.getLabels();
      final double[] values = object.getValues();
      for (int i = 0; i < object.size(); i++) {
        msg.add(LABEL_TYPE_ORDINAL, labels[i].getClass().getName());
        msg.add(KEY_ORDINAL, keys[i]);
        context.addToMessage(msg, null, LABEL_ORDINAL, labels[i]);
        msg.add(VALUE_ORDINAL, values[i]);
      }

      message.add(MATRIX_FIELD, msg);
    }

    @Override
    public DoubleLabelledMatrix1D buildObject(final FudgeDeserializationContext context, final FudgeMsg message) {
      final FudgeMsg msg = message.getMessage(MATRIX_FIELD);

      final Queue<String> labelTypes = new LinkedList<String>();
      final Queue<FudgeField> labelValues = new LinkedList<FudgeField>();

      final List<Double> keys = new LinkedList<Double>();
      final List<Object> labels = new LinkedList<Object>();
      final List<Double> values = new LinkedList<Double>();

      for (final FudgeField field : msg) {
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
          final String labelType = labelTypes.remove();
          Class<?> labelClass;
          try {
            labelClass = Class.forName(labelType);
          } catch (final ClassNotFoundException ex) {
            throw new OpenGammaRuntimeException("Could not deserialize label of type " + labelType, ex);
          }
          final FudgeField labelValue = labelValues.remove();
          final Object label = context.fieldValueToObject(labelClass, labelValue);
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

  @FudgeBuilderFor(LocalDateLabelledMatrix1D.class)
  public static final class LocalDateLabelledMatrix1DBuilder extends AbstractFudgeBuilder<LocalDateLabelledMatrix1D> {

    @Override
    protected void buildMessage(final FudgeSerializationContext context, final MutableFudgeMsg message, final LocalDateLabelledMatrix1D object) {
      final MutableFudgeMsg msg = context.newMessage();

      final LocalDate[] keys = object.getKeys();
      final Object[] labels = object.getLabels();
      final double[] values = object.getValues();
      for (int i = 0; i < object.size(); i++) {
        msg.add(LABEL_TYPE_ORDINAL, labels[i].getClass().getName());
        msg.add(KEY_ORDINAL, keys[i]);
        context.addToMessage(msg, null, LABEL_ORDINAL, labels[i]);
        msg.add(VALUE_ORDINAL, values[i]);
      }

      message.add(MATRIX_FIELD, msg);
    }

    @Override
    public LocalDateLabelledMatrix1D buildObject(final FudgeDeserializationContext context, final FudgeMsg message) {
      final FudgeMsg msg = message.getMessage(MATRIX_FIELD);

      final Queue<String> labelTypes = new LinkedList<String>();
      final Queue<FudgeField> labelValues = new LinkedList<FudgeField>();

      final List<LocalDate> keys = new LinkedList<LocalDate>();
      final List<Object> labels = new LinkedList<Object>();
      final List<Double> values = new LinkedList<Double>();

      for (final FudgeField field : msg) {
        switch (field.getOrdinal()) {
          case LABEL_TYPE_ORDINAL:
            labelTypes.add((String) field.getValue());
            break;
          case KEY_ORDINAL:
            keys.add(((FudgeDate) field.getValue()).toLocalDate());
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
          final String labelType = labelTypes.remove();
          Class<?> labelClass;
          try {
            labelClass = Class.forName(labelType);
          } catch (final ClassNotFoundException ex) {
            throw new OpenGammaRuntimeException("Could not deserialize label of type " + labelType, ex);
          }
          final FudgeField labelValue = labelValues.remove();
          final Object label = context.fieldValueToObject(labelClass, labelValue);
          labels.add(label);
        }
      }

      final int matrixSize = keys.size();
      final LocalDate[] keysArray = new LocalDate[matrixSize];
      keys.toArray(keysArray);
      final Object[] labelsArray = new Object[matrixSize];
      labels.toArray(labelsArray);
      final double[] valuesArray = Doubles.toArray(values);
      return new LocalDateLabelledMatrix1D(keysArray, labelsArray, valuesArray);
    }
  }

  //TODO add ZonedDateTime version

  @FudgeBuilderFor(CurrencyLabelledMatrix1D.class)
  public static final class CurrencyLabelledMatrix1DBuilder extends AbstractFudgeBuilder<CurrencyLabelledMatrix1D> {

    @Override
    protected void buildMessage(final FudgeSerializationContext context, final MutableFudgeMsg message, final CurrencyLabelledMatrix1D object) {
      final MutableFudgeMsg msg = context.newMessage();

      final Currency[] keys = object.getKeys();
      final Object[] labels = object.getLabels();
      final double[] values = object.getValues();
      for (int i = 0; i < object.size(); i++) {
        msg.add(LABEL_TYPE_ORDINAL, labels[i].getClass().getName());
        msg.add(KEY_ORDINAL, keys[i]);
        context.addToMessage(msg, null, LABEL_ORDINAL, labels[i]);
        msg.add(VALUE_ORDINAL, values[i]);
      }

      message.add(MATRIX_FIELD, msg);
    }

    @Override
    public CurrencyLabelledMatrix1D buildObject(final FudgeDeserializationContext context, final FudgeMsg message) {
      final FudgeMsg msg = message.getMessage(MATRIX_FIELD);

      final Queue<String> labelTypes = new LinkedList<String>();
      final Queue<FudgeField> labelValues = new LinkedList<FudgeField>();

      final List<Currency> keys = new LinkedList<Currency>();
      final List<Object> labels = new LinkedList<Object>();
      final List<Double> values = new LinkedList<Double>();

      for (final FudgeField field : msg) {
        switch (field.getOrdinal()) {
          case LABEL_TYPE_ORDINAL:
            labelTypes.add((String) field.getValue());
            break;
          case KEY_ORDINAL:
            keys.add(Currency.of((String) field.getValue()));
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
          final String labelType = labelTypes.remove();
          Class<?> labelClass;
          try {
            labelClass = Class.forName(labelType);
          } catch (final ClassNotFoundException ex) {
            throw new OpenGammaRuntimeException("Could not deserialize label of type " + labelType, ex);
          }
          final FudgeField labelValue = labelValues.remove();
          final Object label = context.fieldValueToObject(labelClass, labelValue);
          //          labels.add(Currency.of((String) label));
          labels.add(label);
        }
      }

      final int matrixSize = keys.size();
      final Currency[] keysArray = new Currency[matrixSize];
      keys.toArray(keysArray);
      final Object[] labelsArray = new Object[matrixSize];
      labels.toArray(labelsArray);
      final double[] valuesArray = Doubles.toArray(values);
      return new CurrencyLabelledMatrix1D(keysArray, labelsArray, valuesArray);
    }
  }
}
