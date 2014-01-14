/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fudgemsg;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.financial.analytics.DoubleCurrencyLabelledMatrix2D;
import com.opengamma.financial.analytics.DoubleLabelledMatrix2D;
import com.opengamma.util.ClassUtils;
import com.opengamma.util.money.Currency;

/**
 * 
 */
final class LabelledMatrix2DBuilder {
  private static final String MATRIX_FIELD = "matrix";
  private static final String X_TITLE_FIELD = "xTitle";
  private static final String Y_TITLE_FIELD = "yTitle";
  private static final String VALUES_TITLE_FIELD = "valuesTitle";
  private static final int X_LABEL_TYPE_ORDINAL = 0;
  private static final int X_KEY_ORDINAL = 1;
  private static final int X_LABEL_ORDINAL = 2;
  private static final int Y_LABEL_TYPE_ORDINAL = 3;
  private static final int Y_KEY_ORDINAL = 4;
  private static final int Y_LABEL_ORDINAL = 5;
  private static final int VALUE_ORDINAL = 6;

  private LabelledMatrix2DBuilder() {
  }

  @FudgeBuilderFor(DoubleLabelledMatrix2D.class)
  public static final class DoubleLabelledMatrix2DBuilder extends AbstractFudgeBuilder<DoubleLabelledMatrix2D> {

    @Override
    protected void buildMessage(FudgeSerializer serializer, MutableFudgeMsg message, DoubleLabelledMatrix2D object) {
      MutableFudgeMsg msg = serializer.newMessage();

      Double[] xKeys = object.getXKeys();
      Object[] xLabels = object.getXLabels();
      Double[] yKeys = object.getYKeys();
      Object[] yLabels = object.getYLabels();
      double[][] values = object.getValues();
      int n = yKeys.length;
      int m = xKeys.length;
      for (int i = 0; i < n; i++) {
        msg.add(Y_LABEL_TYPE_ORDINAL, yLabels[i].getClass().getName());
        msg.add(Y_KEY_ORDINAL, yKeys[i]);
        serializer.addToMessage(msg, null, Y_LABEL_ORDINAL, yLabels[i]);
        for (int j = 0; j < m; j++) {
          if (i == 0) {
            msg.add(X_LABEL_TYPE_ORDINAL, xLabels[j].getClass().getName());
            msg.add(X_KEY_ORDINAL, xKeys[j]);
            serializer.addToMessage(msg, null, X_LABEL_ORDINAL, xLabels[j]);
          }
          msg.add(VALUE_ORDINAL, values[i][j]);
        }
      }
      message.add(MATRIX_FIELD, msg);
      if (object.getXTitle() != null) {
        message.add(X_TITLE_FIELD, object.getXTitle());
      }
      if (object.getYTitle() != null) {
        message.add(Y_TITLE_FIELD, object.getYTitle());
      }
      if (object.getValuesTitle() != null) {
        message.add(VALUES_TITLE_FIELD, object.getValuesTitle());
      }
    }

    @Override
    public DoubleLabelledMatrix2D buildObject(FudgeDeserializer deserializer, FudgeMsg message) {
      FudgeMsg msg = message.getMessage(MATRIX_FIELD);
      Queue<String> xLabelTypes = new LinkedList<String>();
      Queue<FudgeField> xLabelValues = new LinkedList<FudgeField>();
      Queue<String> yLabelTypes = new LinkedList<String>();
      Queue<FudgeField> yLabelValues = new LinkedList<FudgeField>();

      final List<Double> xKeys = new LinkedList<Double>();
      final List<Object> xLabels = new LinkedList<Object>();
      final List<Double> yKeys = new LinkedList<Double>();
      final List<Object> yLabels = new LinkedList<Object>();
      final List<List<Double>> values = new LinkedList<List<Double>>();

      boolean newRow = true;
      int count = -1;
      for (final FudgeField field : msg) {
        switch (field.getOrdinal()) {
          case X_LABEL_TYPE_ORDINAL:
            xLabelTypes.add((String) field.getValue());
            break;
          case X_KEY_ORDINAL:
            xKeys.add((Double) field.getValue());
            break;
          case X_LABEL_ORDINAL:
            xLabelValues.add(field);
            break;
          case Y_LABEL_TYPE_ORDINAL:
            newRow = true;
            count++;
            yLabelTypes.add((String) field.getValue());
            break;
          case Y_KEY_ORDINAL:
            yKeys.add((Double) field.getValue());
            break;
          case Y_LABEL_ORDINAL:
            yLabelValues.add(field);
            break;
          case VALUE_ORDINAL:
            Double value = (Double) field.getValue();
            if (newRow) {
              List<Double> row = new ArrayList<Double>();
              row.add(value);
              values.add(row);
              newRow = false;
            } else {
              values.get(count).add(value);
            }
            break;
        }

        if (!xLabelTypes.isEmpty() && !xLabelValues.isEmpty()) {
          // Have a type and a value, which can be consumed
          final String labelType = xLabelTypes.remove();
          Class<?> labelClass = ClassUtils.loadClassRuntime(labelType);
          final FudgeField labelValue = xLabelValues.remove();
          final Object label = deserializer.fieldValueToObject(labelClass, labelValue);
          xLabels.add(label);
        }
        if (!yLabelTypes.isEmpty() && !yLabelValues.isEmpty()) {
          // Have a type and a value, which can be consumed
          final String labelType = yLabelTypes.remove();
          Class<?> labelClass = ClassUtils.loadClassRuntime(labelType);
          final FudgeField labelValue = yLabelValues.remove();
          final Object label = deserializer.fieldValueToObject(labelClass, labelValue);
          yLabels.add(label);
        }
      }

      final String xTitle = message.getString(X_TITLE_FIELD);
      final String yTitle = message.getString(Y_TITLE_FIELD);
      final String valuesTitle = message.getString(VALUES_TITLE_FIELD);

      final int matrixRowSize = yKeys.size();
      final int matrixColumnSize = xKeys.size();
      final Double[] xKeysArray = new Double[matrixColumnSize];
      final Object[] xLabelsArray = new Object[matrixColumnSize];
      final Double[] yKeysArray = new Double[matrixRowSize];
      final Object[] yLabelsArray = new Object[matrixRowSize];
      final double[][] valuesArray = new double[matrixRowSize][matrixColumnSize];
      for (int i = 0; i < matrixRowSize; i++) {
        yKeysArray[i] = yKeys.get(i);
        yLabelsArray[i] = yLabels.get(i);
        for (int j = 0; j < matrixColumnSize; j++) {
          if (i == 0) {
            xKeysArray[j] = xKeys.get(j);
            xLabelsArray[j] = xLabels.get(j);
          }
          valuesArray[i][j] = values.get(i).get(j);
        }
      }
      return new DoubleLabelledMatrix2D(xKeysArray, xLabelsArray, xTitle, yKeysArray, yLabelsArray, yTitle, valuesArray, valuesTitle);
    }

  }
  
  @FudgeBuilderFor(DoubleCurrencyLabelledMatrix2D.class)
  public static final class DoubleCurrencyLabelledMatrix2DBuilder extends AbstractFudgeBuilder<DoubleCurrencyLabelledMatrix2D> {
    
    @Override
    protected void buildMessage(FudgeSerializer serializer, MutableFudgeMsg message, DoubleCurrencyLabelledMatrix2D object) {
      MutableFudgeMsg msg = serializer.newMessage();

      Double[] xKeys = object.getXKeys();
      Object[] xLabels = object.getXLabels();
      Currency[] yKeys = object.getYKeys();
      Object[] yLabels = object.getYLabels();
      double[][] values = object.getValues();
      int n = yKeys.length;
      int m = xKeys.length;
      for (int i = 0; i < n; i++) {
        msg.add(Y_LABEL_TYPE_ORDINAL, yLabels[i].getClass().getName());
        msg.add(Y_KEY_ORDINAL, yKeys[i]);
        serializer.addToMessage(msg, null, Y_LABEL_ORDINAL, yLabels[i]);
        for (int j = 0; j < m; j++) {
          if (i == 0) {
            msg.add(X_LABEL_TYPE_ORDINAL, xLabels[j].getClass().getName());
            msg.add(X_KEY_ORDINAL, xKeys[j]);
            serializer.addToMessage(msg, null, X_LABEL_ORDINAL, xLabels[j]);
          }
          msg.add(VALUE_ORDINAL, values[i][j]);
        }
      }
      message.add(MATRIX_FIELD, msg);
      if (object.getXTitle() != null) {
        message.add(X_TITLE_FIELD, object.getXTitle());
      }
      if (object.getYTitle() != null) {
        message.add(Y_TITLE_FIELD, object.getYTitle());
      }
      if (object.getValuesTitle() != null) {
        message.add(VALUES_TITLE_FIELD, object.getValuesTitle());
      }
    }

    @Override
    public DoubleCurrencyLabelledMatrix2D buildObject(FudgeDeserializer deserializer, FudgeMsg message) {
      FudgeMsg msg = message.getMessage(MATRIX_FIELD);
      Queue<String> xLabelTypes = new LinkedList<String>();
      Queue<FudgeField> xLabelValues = new LinkedList<FudgeField>();
      Queue<String> yLabelTypes = new LinkedList<String>();
      Queue<FudgeField> yLabelValues = new LinkedList<FudgeField>();

      final List<Double> xKeys = new LinkedList<Double>();
      final List<Object> xLabels = new LinkedList<Object>();
      final List<Currency> yKeys = new LinkedList<Currency>();
      final List<Object> yLabels = new LinkedList<Object>();
      final List<List<Double>> values = new LinkedList<List<Double>>();

      boolean newRow = true;
      int count = -1;
      for (final FudgeField field : msg) {
        switch (field.getOrdinal()) {
          case X_LABEL_TYPE_ORDINAL:
            xLabelTypes.add((String) field.getValue());
            break;
          case X_KEY_ORDINAL:
            xKeys.add((Double) field.getValue());
            break;
          case X_LABEL_ORDINAL:
            xLabelValues.add(field);
            break;
          case Y_LABEL_TYPE_ORDINAL:
            newRow = true;
            count++;
            yLabelTypes.add((String) field.getValue());
            break;
          case Y_KEY_ORDINAL:
            yKeys.add(deserializer.fieldValueToObject(Currency.class, field));
            break;
          case Y_LABEL_ORDINAL:
            yLabelValues.add(field);
            break;
          case VALUE_ORDINAL:
            Double value = (Double) field.getValue();
            if (newRow) {
              List<Double> row = new ArrayList<Double>();
              row.add(value);
              values.add(row);
              newRow = false;
            } else {
              values.get(count).add(value);
            }
            break;
        }

        if (!xLabelTypes.isEmpty() && !xLabelValues.isEmpty()) {
          // Have a type and a value, which can be consumed
          final String labelType = xLabelTypes.remove();
          Class<?> labelClass = ClassUtils.loadClassRuntime(labelType);
          final FudgeField labelValue = xLabelValues.remove();
          final Object label = deserializer.fieldValueToObject(labelClass, labelValue);
          xLabels.add(label);
        }
        if (!yLabelTypes.isEmpty() && !yLabelValues.isEmpty()) {
          // Have a type and a value, which can be consumed
          final String labelType = yLabelTypes.remove();
          Class<?> labelClass = ClassUtils.loadClassRuntime(labelType);
          final FudgeField labelValue = yLabelValues.remove();
          final Object label = deserializer.fieldValueToObject(labelClass, labelValue);
          yLabels.add(label);
        }
      }

      final String xTitle = message.getString(X_TITLE_FIELD);
      final String yTitle = message.getString(Y_TITLE_FIELD);
      final String valuesTitle = message.getString(VALUES_TITLE_FIELD);

      final int matrixRowSize = yKeys.size();
      final int matrixColumnSize = xKeys.size();
      final Double[] xKeysArray = new Double[matrixColumnSize];
      final Object[] xLabelsArray = new Object[matrixColumnSize];
      final Currency[] yKeysArray = new Currency[matrixRowSize];
      final Object[] yLabelsArray = new Object[matrixRowSize];
      final double[][] valuesArray = new double[matrixRowSize][matrixColumnSize];
      for (int i = 0; i < matrixRowSize; i++) {
        yKeysArray[i] = yKeys.get(i);
        yLabelsArray[i] = yLabels.get(i);
        for (int j = 0; j < matrixColumnSize; j++) {
          if (i == 0) {
            xKeysArray[j] = xKeys.get(j);
            xLabelsArray[j] = xLabels.get(j);
          }
          valuesArray[i][j] = values.get(i).get(j);
        }
      }
      return new DoubleCurrencyLabelledMatrix2D(xKeysArray, xLabelsArray, xTitle, yKeysArray, yLabelsArray, yTitle, valuesArray, valuesTitle);
    }
    
  }
  
}
