/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fudgemsg;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.ArrayUtils;
import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.fudgemsg.types.FudgeDate;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;

import com.google.common.primitives.Doubles;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.analytics.CurrencyLabelledMatrix1D;
import com.opengamma.financial.analytics.DoubleLabelledMatrix1D;
import com.opengamma.financial.analytics.LocalDateLabelledMatrix1D;
import com.opengamma.financial.analytics.StringLabelledMatrix1D;
import com.opengamma.financial.analytics.TenorLabelledLocalDateDoubleTimeSeriesMatrix1D;
import com.opengamma.financial.analytics.TenorLabelledMatrix1D;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.ClassUtils;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;

/**
 * 
 */
final class LabelledMatrix1DBuilder {

  /** Field name. */
  private static final String MATRIX_FIELD_NAME = "matrix";
  private static final String LABELS_TITLE_FIELD_NAME = "labelsTitle";
  private static final String VALUES_TITLE_FIELD_NAME = "valuesTitle";

  private static final int LABEL_TYPE_ORDINAL = 0;
  private static final int KEY_ORDINAL = 1;
  private static final int LABEL_ORDINAL = 2;
  private static final int VALUE_ORDINAL = 3;

  private LabelledMatrix1DBuilder() {
  }

  private static Class<?> getLabelClass(final String labelType, final Map<String, Class<?>> loadedClasses) throws ClassNotFoundException {
    Class<?> labelClass;
    labelClass = loadedClasses.get(labelType);
    if (labelClass == null) {
      labelClass = ClassUtils.loadClass(labelType);
      loadedClasses.put(labelType, labelClass);
    }
    return labelClass;
  }

  @FudgeBuilderFor(DoubleLabelledMatrix1D.class)
  public static final class DoubleLabelledMatrix1DFudgeBuilder extends AbstractFudgeBuilder<DoubleLabelledMatrix1D> {

    @Override
    protected void buildMessage(final FudgeSerializer serializer, final MutableFudgeMsg message, final DoubleLabelledMatrix1D object) {
      final MutableFudgeMsg msg = serializer.newMessage();
      final Double[] keys = object.getKeys();
      final Object[] labels = object.getLabels();
      final double[] values = object.getValues();
      for (int i = 0; i < object.size(); i++) {
        msg.add(LABEL_TYPE_ORDINAL, labels[i].getClass().getName());
        msg.add(KEY_ORDINAL, keys[i]);
        serializer.addToMessage(msg, null, LABEL_ORDINAL, labels[i]);
        msg.add(VALUE_ORDINAL, values[i]);
      }
      message.add(MATRIX_FIELD_NAME, msg);
      if (object.getLabelsTitle() != null) {
        message.add(LABELS_TITLE_FIELD_NAME, object.getLabelsTitle());
      }
      if (object.getValuesTitle() != null) {
        message.add(VALUES_TITLE_FIELD_NAME, object.getValuesTitle());
      }
    }

    @SuppressWarnings("synthetic-access")
    @Override
    public DoubleLabelledMatrix1D buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final FudgeMsg msg = message.getMessage(MATRIX_FIELD_NAME);

      final Queue<String> labelTypes = new LinkedList<>();
      final Queue<FudgeField> labelValues = new LinkedList<>();

      final List<Double> keys = new LinkedList<>();
      final List<Object> labels = new LinkedList<>();
      final List<Double> values = new LinkedList<>();

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
            labelClass = getLabelClass(labelType, _loadedClasses);
          } catch (final ClassNotFoundException ex) {
            throw new OpenGammaRuntimeException("Could not deserialize label of type " + labelType, ex);
          }
          final FudgeField labelValue = labelValues.remove();
          final Object label = deserializer.fieldValueToObject(labelClass, labelValue);
          labels.add(label);
        }
      }

      final String labelsTitle = message.getString(LABELS_TITLE_FIELD_NAME);
      final String valuesTitle = message.getString(VALUES_TITLE_FIELD_NAME);

      final int matrixSize = keys.size();
      final Double[] keysArray = new Double[matrixSize];
      keys.toArray(keysArray);
      final Object[] labelsArray = new Object[matrixSize];
      labels.toArray(labelsArray);
      final double[] valuesArray = Doubles.toArray(values);
      return new DoubleLabelledMatrix1D(keysArray, labelsArray, labelsTitle, valuesArray, valuesTitle);
    }

    private final Map<String, Class<?>> _loadedClasses = new ConcurrentHashMap<>(); //TODO: This should be expired at some point, but it's an insignificant leak at the moment
  }

  @FudgeBuilderFor(LocalDateLabelledMatrix1D.class)
  public static final class LocalDateLabelledMatrix1DBuilder extends AbstractFudgeBuilder<LocalDateLabelledMatrix1D> {

    @Override
    protected void buildMessage(final FudgeSerializer serializer, final MutableFudgeMsg message, final LocalDateLabelledMatrix1D object) {
      final MutableFudgeMsg msg = serializer.newMessage();

      final LocalDate[] keys = object.getKeys();
      final Object[] labels = object.getLabels();
      final double[] values = object.getValues();
      for (int i = 0; i < object.size(); i++) {
        msg.add(LABEL_TYPE_ORDINAL, labels[i].getClass().getName());
        msg.add(KEY_ORDINAL, keys[i]);
        serializer.addToMessage(msg, null, LABEL_ORDINAL, labels[i]);
        msg.add(VALUE_ORDINAL, values[i]);
      }
      message.add(MATRIX_FIELD_NAME, msg);
      if (object.getLabelsTitle() != null) {
        message.add(LABELS_TITLE_FIELD_NAME, object.getLabelsTitle());
      }
      if (object.getValuesTitle() != null) {
        message.add(VALUES_TITLE_FIELD_NAME, object.getValuesTitle());
      }
    }

    @Override
    public LocalDateLabelledMatrix1D buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final FudgeMsg msg = message.getMessage(MATRIX_FIELD_NAME);

      final Queue<String> labelTypes = new LinkedList<>();
      final Queue<FudgeField> labelValues = new LinkedList<>();

      final List<LocalDate> keys = new LinkedList<>();
      final List<Object> labels = new LinkedList<>();
      final List<Double> values = new LinkedList<>();

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
          final Class<?> labelClass = getClass(labelType);
          final FudgeField labelValue = labelValues.remove();
          final Object label = deserializer.fieldValueToObject(labelClass, labelValue);
          labels.add(label);
        }
      }

      final String labelsTitle = message.getString(LABELS_TITLE_FIELD_NAME);
      final String valuesTitle = message.getString(VALUES_TITLE_FIELD_NAME);

      final int matrixSize = keys.size();
      final LocalDate[] keysArray = new LocalDate[matrixSize];
      keys.toArray(keysArray);
      final Object[] labelsArray = new Object[matrixSize];
      labels.toArray(labelsArray);
      final double[] valuesArray = Doubles.toArray(values);
      return new LocalDateLabelledMatrix1D(keysArray, labelsArray, labelsTitle, valuesArray, valuesTitle);
    }

    @SuppressWarnings("synthetic-access")
    private Class<?> getClass(final String labelType) {
      Class<?> labelClass;
      try {
        labelClass = LabelledMatrix1DBuilder.getLabelClass(labelType, _loadedClasses);
      } catch (final ClassNotFoundException ex) {
        throw new OpenGammaRuntimeException("Could not deserialize label of type " + labelType, ex);
      }
      return labelClass;
    }

    private final Map<String, Class<?>> _loadedClasses = new ConcurrentHashMap<>(); //TODO: This should be expired at some point, but it's an insignificant leak at the moment
  }

  @FudgeBuilderFor(TenorLabelledMatrix1D.class)
  public static final class TenorLabelledMatrix1DBuilder extends AbstractFudgeBuilder<TenorLabelledMatrix1D> {

    @Override
    protected void buildMessage(final FudgeSerializer serializer, final MutableFudgeMsg message, final TenorLabelledMatrix1D object) {
      final MutableFudgeMsg msg = serializer.newMessage();

      final Tenor[] keys = object.getKeys();
      final Object[] labels = object.getLabels();
      final double[] values = object.getValues();
      for (int i = 0; i < object.size(); i++) {
        msg.add(LABEL_TYPE_ORDINAL, labels[i].getClass().getName());
        msg.add(KEY_ORDINAL, keys[i]);
        serializer.addToMessage(msg, null, LABEL_ORDINAL, labels[i]);
        msg.add(VALUE_ORDINAL, values[i]);
      }
      message.add(MATRIX_FIELD_NAME, msg);
      if (object.getLabelsTitle() != null) {
        message.add(LABELS_TITLE_FIELD_NAME, object.getLabelsTitle());
      }
      if (object.getValuesTitle() != null) {
        message.add(VALUES_TITLE_FIELD_NAME, object.getValuesTitle());
      }
    }

    @Override
    public TenorLabelledMatrix1D buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final FudgeMsg msg = message.getMessage(MATRIX_FIELD_NAME);

      final Queue<String> labelTypes = new LinkedList<>();
      final Queue<FudgeField> labelValues = new LinkedList<>();

      final List<Tenor> keys = new LinkedList<>();
      final List<Object> labels = new LinkedList<>();
      final List<Double> values = new LinkedList<>();

      for (final FudgeField field : msg) {
        switch (field.getOrdinal()) {
          case LABEL_TYPE_ORDINAL:
            labelTypes.add((String) field.getValue());
            break;
          case KEY_ORDINAL:
            keys.add(Tenor.of(Period.parse((String) field.getValue())));
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
          final Class<?> labelClass = getClass(labelType);
          final FudgeField labelValue = labelValues.remove();
          final Object label = deserializer.fieldValueToObject(labelClass, labelValue);
          labels.add(label);
        }
      }

      final String labelsTitle = message.getString(LABELS_TITLE_FIELD_NAME);
      final String valuesTitle = message.getString(VALUES_TITLE_FIELD_NAME);

      final int matrixSize = keys.size();
      final Tenor[] keysArray = new Tenor[matrixSize];
      keys.toArray(keysArray);
      final Object[] labelsArray = new Object[matrixSize];
      labels.toArray(labelsArray);
      final double[] valuesArray = Doubles.toArray(values);
      return new TenorLabelledMatrix1D(keysArray, labelsArray, labelsTitle, valuesArray, valuesTitle);
    }

    @SuppressWarnings("synthetic-access")
    private Class<?> getClass(final String labelType) {
      Class<?> labelClass;
      try {
        labelClass = LabelledMatrix1DBuilder.getLabelClass(labelType, _loadedClasses);
      } catch (final ClassNotFoundException ex) {
        throw new OpenGammaRuntimeException("Could not deserialize label of type " + labelType, ex);
      }
      return labelClass;
    }

    private final Map<String, Class<?>> _loadedClasses = new ConcurrentHashMap<>(); //TODO: This should be expired at some point, but it's an insignificant leak at the moment
  }

  //TODO add ZonedDateTime version

  @FudgeBuilderFor(CurrencyLabelledMatrix1D.class)
  public static final class CurrencyLabelledMatrix1DBuilder extends AbstractFudgeBuilder<CurrencyLabelledMatrix1D> {

    @Override
    protected void buildMessage(final FudgeSerializer serializer, final MutableFudgeMsg message, final CurrencyLabelledMatrix1D object) {
      final MutableFudgeMsg msg = serializer.newMessage();

      final Currency[] keys = object.getKeys();
      final Object[] labels = object.getLabels();
      final double[] values = object.getValues();
      for (int i = 0; i < object.size(); i++) {
        msg.add(LABEL_TYPE_ORDINAL, labels[i].getClass().getName());
        msg.add(KEY_ORDINAL, keys[i]);
        serializer.addToMessage(msg, null, LABEL_ORDINAL, labels[i]);
        msg.add(VALUE_ORDINAL, values[i]);
      }
      message.add(MATRIX_FIELD_NAME, msg);
      if (object.getLabelsTitle() != null) {
        message.add(LABELS_TITLE_FIELD_NAME, object.getLabelsTitle());
      }
      if (object.getValuesTitle() != null) {
        message.add(VALUES_TITLE_FIELD_NAME, object.getValuesTitle());
      }
    }

    @SuppressWarnings("synthetic-access")
    @Override
    public CurrencyLabelledMatrix1D buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final FudgeMsg msg = message.getMessage(MATRIX_FIELD_NAME);

      final Queue<String> labelTypes = new LinkedList<>();
      final Queue<FudgeField> labelValues = new LinkedList<>();

      final List<Currency> keys = new LinkedList<>();
      final List<Object> labels = new LinkedList<>();
      final List<Double> values = new LinkedList<>();

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
            labelClass = LabelledMatrix1DBuilder.getLabelClass(labelType, _loadedClasses);
          } catch (final ClassNotFoundException ex) {
            throw new OpenGammaRuntimeException("Could not deserialize label of type " + labelType, ex);
          }
          final FudgeField labelValue = labelValues.remove();
          final Object label = deserializer.fieldValueToObject(labelClass, labelValue);
          //          labels.add(Currency.of((String) label));
          labels.add(label);
        }
      }

      final String labelsTitle = message.getString(LABELS_TITLE_FIELD_NAME);
      final String valuesTitle = message.getString(VALUES_TITLE_FIELD_NAME);

      final int matrixSize = keys.size();
      final Currency[] keysArray = new Currency[matrixSize];
      keys.toArray(keysArray);
      final Object[] labelsArray = new Object[matrixSize];
      labels.toArray(labelsArray);
      final double[] valuesArray = Doubles.toArray(values);
      return new CurrencyLabelledMatrix1D(keysArray, labelsArray, labelsTitle, valuesArray, valuesTitle);
    }

    private final Map<String, Class<?>> _loadedClasses = new ConcurrentHashMap<>(); //TODO: This should be expired at some point, but it's an insignificant leak at the moment
  }

  @FudgeBuilderFor(StringLabelledMatrix1D.class)
  public static final class StringLabelledMatrix1DBuilder extends AbstractFudgeBuilder<StringLabelledMatrix1D> {

    @Override
    protected void buildMessage(final FudgeSerializer serializer, final MutableFudgeMsg message, final StringLabelledMatrix1D object) {
      final MutableFudgeMsg msg = serializer.newMessage();
      final String[] keys = object.getKeys();
      final double[] values = object.getValues();
      for (int i = 0; i < object.size(); i++) {
        msg.add(KEY_ORDINAL, keys[i]);
        msg.add(VALUE_ORDINAL, values[i]);
      }
      message.add(MATRIX_FIELD_NAME, msg);
      if (object.getLabelsTitle() != null) {
        message.add(LABELS_TITLE_FIELD_NAME, object.getLabelsTitle());
      }
      if (object.getValuesTitle() != null) {
        message.add(VALUES_TITLE_FIELD_NAME, object.getValuesTitle());
      }
    }

    @Override
    public StringLabelledMatrix1D buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final FudgeMsg msg = message.getMessage(MATRIX_FIELD_NAME);
      final List<String> keys = new LinkedList<>();
      final List<Double> values = new LinkedList<>();
      for (final FudgeField field : msg) {
        switch (field.getOrdinal()) {
          case KEY_ORDINAL:
            keys.add((String) field.getValue());
            break;
          case VALUE_ORDINAL:
            values.add((Double) field.getValue());
            break;
        }
      }
      final String labelsTitle = message.getString(LABELS_TITLE_FIELD_NAME);
      final String valuesTitle = message.getString(VALUES_TITLE_FIELD_NAME);
      final String[] keysArray = keys.toArray(ArrayUtils.EMPTY_STRING_ARRAY);
      final double[] valuesArray = Doubles.toArray(values);
      return new StringLabelledMatrix1D(keysArray, keysArray, labelsTitle, valuesArray, valuesTitle);
    }
  }

  @FudgeBuilderFor(TenorLabelledLocalDateDoubleTimeSeriesMatrix1D.class)
  public static final class TenorLabelledLocalDateDoubleTimeSeriesMatrix1DFudgeBuilder extends AbstractFudgeBuilder<TenorLabelledLocalDateDoubleTimeSeriesMatrix1D> {

    @Override
    protected void buildMessage(final FudgeSerializer serializer, final MutableFudgeMsg message, final TenorLabelledLocalDateDoubleTimeSeriesMatrix1D object) {
      final MutableFudgeMsg msg = serializer.newMessage();

      final Tenor[] keys = object.getKeys();
      final Object[] labels = object.getLabels();
      final LocalDateDoubleTimeSeries[] values = object.getValues();
      for (int i = 0; i < object.size(); i++) {
        msg.add(LABEL_TYPE_ORDINAL, labels[i].getClass().getName());
        msg.add(KEY_ORDINAL, keys[i]);
        serializer.addToMessage(msg, null, LABEL_ORDINAL, labels[i]);
        serializer.addToMessage(msg, null, VALUE_ORDINAL, values[i]);
      }
      message.add(MATRIX_FIELD_NAME, msg);
      if (object.getLabelsTitle() != null) {
        message.add(LABELS_TITLE_FIELD_NAME, object.getLabelsTitle());
      }
      if (object.getValuesTitle() != null) {
        message.add(VALUES_TITLE_FIELD_NAME, object.getValuesTitle());
      }
    }

    @Override
    public TenorLabelledLocalDateDoubleTimeSeriesMatrix1D buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final FudgeMsg msg = message.getMessage(MATRIX_FIELD_NAME);

      final Queue<String> labelTypes = new LinkedList<>();
      final Queue<FudgeField> labelValues = new LinkedList<>();

      final List<Tenor> keys = new LinkedList<>();
      final List<Object> labels = new LinkedList<>();
      final List<LocalDateDoubleTimeSeries> values = new LinkedList<>();

      for (final FudgeField field : msg) {
        switch (field.getOrdinal()) {
          case LABEL_TYPE_ORDINAL:
            labelTypes.add((String) field.getValue());
            break;
          case KEY_ORDINAL:
            keys.add(Tenor.of(Period.parse((String) field.getValue())));
            break;
          case LABEL_ORDINAL:
            labelValues.add(field);
            break;
          case VALUE_ORDINAL:
            values.add(deserializer.fieldValueToObject(LocalDateDoubleTimeSeries.class, field));
            break;
        }

        if (!labelTypes.isEmpty() && !labelValues.isEmpty()) {
          // Have a type and a value, which can be consumed
          final String labelType = labelTypes.remove();
          final Class<?> labelClass = getClass(labelType);
          final FudgeField labelValue = labelValues.remove();
          final Object label = deserializer.fieldValueToObject(labelClass, labelValue);
          labels.add(label);
        }
      }

      final String labelsTitle = message.getString(LABELS_TITLE_FIELD_NAME);
      final String valuesTitle = message.getString(VALUES_TITLE_FIELD_NAME);

      final int matrixSize = keys.size();
      final Tenor[] keysArray = new Tenor[matrixSize];
      keys.toArray(keysArray);
      final Object[] labelsArray = new Object[matrixSize];
      labels.toArray(labelsArray);
      final LocalDateDoubleTimeSeries[] valuesArray = new LocalDateDoubleTimeSeries[matrixSize];
      values.toArray(valuesArray);
      return new TenorLabelledLocalDateDoubleTimeSeriesMatrix1D(keysArray, labelsArray, labelsTitle, valuesArray, valuesTitle);
    }

    @SuppressWarnings("synthetic-access")
    private Class<?> getClass(final String labelType) {
      Class<?> labelClass;
      try {
        labelClass = LabelledMatrix1DBuilder.getLabelClass(labelType, _loadedClasses);
      } catch (final ClassNotFoundException ex) {
        throw new OpenGammaRuntimeException("Could not deserialize label of type " + labelType, ex);
      }
      return labelClass;
    }

    private final Map<String, Class<?>> _loadedClasses = new ConcurrentHashMap<>(); //TODO: This should be expired at some point, but it's an insignificant leak at the moment
  }
  
}
