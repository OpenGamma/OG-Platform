/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fudgemsg;

import java.lang.reflect.Array;
import java.util.Iterator;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeFieldType;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.fudgemsg.types.SecondaryFieldType;
import org.fudgemsg.wire.types.FudgeWireType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.financial.analytics.DoubleLabelledMatrix3D;
import com.opengamma.financial.analytics.LabelledMatrix3D;
import com.opengamma.util.ClassUtils;

/**
 * Base class for builders of {@link LabelledMatrix3D} subclasses. Basic message structure is:
 * <dl>
 * <dd>[X|Y|Z]_KEYS</dd>
 * <dt>The values of the keys. The default encoding is a sub-message containing the keys as anonymous fields, but if the keys are primitive types it may use one of the built-in Fudge array types.</dt>
 * <dd>[X|Y|Z]_LABELS</dd>
 * <dt>The values of the labels as a sub-message containing an anonymous field for each value.</dt>
 * <dd>[X|Y|Z]_TYPES</dd>
 * <dt>Additional type information for the labels as a sub-message containing an anonymous field for each value in <code>?_LABELS</code>. If a numeric value, this is the Fudge type identifier of the
 * original value. If a string value, this is a class name of the original type. This is an optional field. If omitted, default types will be inferred from <code>?_LABELS</code>. It will only be
 * generated if the inferred types differ from the actual types.</dt>
 * <dd>VALUES</dd>
 * <dt>A flattened array of matrix data, e.g. <code>{ { { 1, 2 }, { 3, 4 } }, { { 5, 6 }, { 7, 8 } } }</code> becomes <code>{ 1, 2, 3, 4, 5, 6, 7, 8 }</code>.</dt>
 * 
 * @param <KX> X key type
 * @param <KY> Y key type
 * @param <KZ> Z key type
 * @param <T> sub-type of matrix to build
 */
public abstract class LabelledMatrix3DFudgeBuilder<KX, KY, KZ, T extends LabelledMatrix3D<KX, KY, KZ, ?, ?, ?, T>> extends AbstractFudgeBuilder<T> {

  private static final Logger s_logger = LoggerFactory.getLogger(LabelledMatrix3DFudgeBuilder.class);

  /** Field name. */
  public static final String X_KEYS_KEY = "X_KEYS";
  /** Field name. */
  public static final String Y_KEYS_KEY = "Y_KEYS";
  /** Field name. */
  public static final String Z_KEYS_KEY = "Z_KEYS";
  /** Field name. */
  public static final String X_LABELS_KEY = "X_LABELS";
  /** Field name. */
  public static final String X_LABEL_TYPES_KEY = "X_LABEL_TYPES";
  /** Field name. */
  public static final String Y_LABELS_KEY = "Y_LABELS";
  /** Field name. */
  public static final String Y_LABEL_TYPES_KEY = "Y_LABEL_TYPES";
  /** Field name. */
  public static final String Z_LABELS_KEY = "Z_LABELS";
  /** Field name. */
  public static final String Z_LABEL_TYPES_KEY = "Z_LABEL_TYPES";
  /** Field name. */
  public static final String VALUES_KEY = "VALUES";

  private final Class<KX> _xKeyClass;
  private final Class<KY> _yKeyClass;
  private final Class<KZ> _zKeyClass;

  protected LabelledMatrix3DFudgeBuilder(final Class<KX> xKey, final Class<KY> yKey, final Class<KZ> zKey) {
    _xKeyClass = xKey;
    _yKeyClass = yKey;
    _zKeyClass = zKey;
  }

  protected Class<KX> getXKeyClass() {
    return _xKeyClass;
  }

  protected Class<KY> getYKeyClass() {
    return _yKeyClass;
  }

  protected Class<KZ> getZKeyClass() {
    return _zKeyClass;
  }

  @Override
  protected final void buildMessage(final FudgeSerializer serializer, final MutableFudgeMsg message, final T object) {
    s_logger.debug("Building message from {}", object);
    writeLabels(serializer, message, object.getXLabels(), X_LABELS_KEY, X_LABEL_TYPES_KEY);
    writeXKeys(serializer, message, object.getXKeys());
    writeLabels(serializer, message, object.getYLabels(), Y_LABELS_KEY, Y_LABEL_TYPES_KEY);
    writeYKeys(serializer, message, object.getYKeys());
    writeLabels(serializer, message, object.getZLabels(), Z_LABELS_KEY, Z_LABEL_TYPES_KEY);
    writeZKeys(serializer, message, object.getZKeys());
    writeValues(message, object.getValues());
    s_logger.debug("Built {}", message);
  }

  /**
   * General purpose label writer.
   * 
   * @param serializer serialization context
   * @param message message to add the key to
   * @param labels labels to add
   * @param valueKey name of the field to hold label value information
   * @param typeKey name of the optional field to hold additional type information
   */
  protected void writeLabels(final FudgeSerializer serializer, final MutableFudgeMsg message, final Object[] labels, final String valueKey, final String typeKey) {
    final MutableFudgeMsg valueMsg = serializer.newMessage();
    final MutableFudgeMsg typeMsg = serializer.newMessage();
    boolean needsTypeInfo = false;
    for (Object label : labels) {
      final FudgeFieldType type = serializer.getFudgeContext().getTypeDictionary().getByJavaType(label.getClass());
      if (type == null) {
        serializer.addToMessage(valueMsg, null, null, label);
        valueMsg.add(null, null, FudgeWireType.SUB_MESSAGE, serializer.objectToFudgeMsg(label));
        typeMsg.add(null, null, label.getClass().getName());
        needsTypeInfo = true;
      } else {
        valueMsg.add(null, null, type, label);
        if (type instanceof SecondaryFieldType<?, ?>) {
          typeMsg.add(null, null, type.getJavaType().getName());
          needsTypeInfo = true;
        } else {
          typeMsg.add(null, null, type.getTypeId());
        }
      }
    }
    if (!needsTypeInfo) {
      // Message contains only primitive types; only put the type info in if there have been type reductions
      final Iterator<FudgeField> itrValueMsg = valueMsg.iterator();
      final Iterator<FudgeField> itrTypeMsg = typeMsg.iterator();
      while (itrValueMsg.hasNext()) {
        if (itrValueMsg.next().getType().getTypeId() != typeMsg.getFieldValue(Integer.class, itrTypeMsg.next())) {
          needsTypeInfo = true;
          break;
        }
      }
    }
    message.add(valueKey, valueMsg);
    if (needsTypeInfo) {
      message.add(typeKey, typeMsg);
    }
  }

  /**
   * General purpose key writer; defaults to {@link #writeKeys} - override in a subclass for more efficient handling.
   * 
   * @param serializer serialization context
   * @param message message to add the key to
   * @param keys keys to add
   */
  protected void writeXKeys(final FudgeSerializer serializer, final MutableFudgeMsg message, final KX[] keys) {
    writeKeys(serializer, message, keys, getXKeyClass(), X_KEYS_KEY);
  }

  /**
   * General purpose key writer; defaults to {@link #writeKeys} - override in a subclass for more efficient handling.
   * 
   * @param serializer serialization context
   * @param message message to add the key to
   * @param keys keys to add
   */
  protected void writeYKeys(final FudgeSerializer serializer, final MutableFudgeMsg message, final KY[] keys) {
    writeKeys(serializer, message, keys, getYKeyClass(), Y_KEYS_KEY);
  }

  /**
   * General purpose key writer; defaults to {@link #writeKeys} - override in a subclass for more efficient handling.
   * 
   * @param serializer serialization context
   * @param message message to add the key to
   * @param keys keys to add
   */
  protected void writeZKeys(final FudgeSerializer serializer, final MutableFudgeMsg message, final KZ[] keys) {
    writeKeys(serializer, message, keys, getZKeyClass(), Z_KEYS_KEY);
  }

  /**
   * General purposes key writer; add each key as a field to a sub-message.
   * 
   * @param <K> type of key
   * @param serializer serialization context
   * @param message message to add the keys to
   * @param keys keys to add
   * @param keyClass common base type of the keys (as known to the receiver)
   * @param keysKey name of the field to add the keys as
   */
  protected <K> void writeKeys(final FudgeSerializer serializer, final MutableFudgeMsg message, final K[] keys, final Class<K> keyClass, final String keysKey) {
    final MutableFudgeMsg submsg = serializer.newMessage();
    for (K key : keys) {
      serializer.addToMessageWithClassHeaders(submsg, null, null, key, keyClass);
    }
    message.add(keysKey, submsg);
  }

  protected void writeDoubleKeys(final MutableFudgeMsg message, final Double[] keys, final String keysKey) {
    final double[] arr = new double[keys.length];
    for (int i = 0; i < keys.length; i++) {
      arr[i] = keys[i];
    }
    message.add(keysKey, arr);
  }

  /**
   * General purpose value writer.
   * 
   * @param message message to add the values to
   * @param values values to add
   */
  protected void writeValues(final MutableFudgeMsg message, final double[][][] values) {
    final int z = values.length;
    final int y = values[0].length;
    final int x = values[0][0].length;
    final double[] flat = new double[z * y * x];
    int i = 0;
    for (double[][] slice : values) {
      for (double[] row : slice) {
        System.arraycopy(row, 0, flat, i, row.length);
        i += row.length;
      }
    }
    message.add(VALUES_KEY, flat);
  }

  @Override
  public final T buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
    s_logger.debug("Building object from {}", message);
    final KX[] xKeys = readXKeys(deserializer, message);
    final Object[] xLabels = readLabels(deserializer, message, X_LABELS_KEY, X_LABEL_TYPES_KEY);
    final KY[] yKeys = readYKeys(deserializer, message);
    final Object[] yLabels = readLabels(deserializer, message, Y_LABELS_KEY, Y_LABEL_TYPES_KEY);
    final KZ[] zKeys = readZKeys(deserializer, message);
    final Object[] zLabels = readLabels(deserializer, message, Z_LABELS_KEY, Z_LABEL_TYPES_KEY);
    final double[][][] values = readValues(message, xKeys.length, yKeys.length, zKeys.length);
    final T result = createMatrix(xKeys, xLabels, yKeys, yLabels, zKeys, zLabels, values);
    s_logger.debug("Built object {}", result);
    return result;
  }

  /**
   * Inverse of {@link #writeLabels}.
   * 
   * @param deserializer deserialization context
   * @param message message to read from
   * @param labelsKey name of the field containing label values
   * @param labelTypesKey name of the optional field containing additional type information
   * @return new labels array
   */
  protected Object[] readLabels(final FudgeDeserializer deserializer, final FudgeMsg message, final String labelsKey, final String labelTypesKey) {
    final FudgeMsg valueMsg = message.getMessage(labelsKey);
    if (valueMsg == null) {
      s_logger.warn("Message field {} not found in {}", labelsKey, message);
      throw new IllegalArgumentException("Message is not a LabelledMatrix3D - does not contain a " + labelsKey + " field");
    }
    final Object[] labels = new Object[valueMsg.getNumFields()];
    final FudgeMsg typeMsg = message.getMessage(labelTypesKey);
    final Iterator<FudgeField> itrValue = valueMsg.iterator();
    final Iterator<FudgeField> itrType = (typeMsg != null) ? typeMsg.iterator() : null;
    int i = 0;
    while (itrValue.hasNext()) {
      Class<?> type = null;
      if (itrType != null) {
        final FudgeField typeField = itrType.next();
        final Object val = typeField.getValue();
        if (val instanceof String) {
          try {
            type = ClassUtils.loadClass((String) val);
          } catch (ClassNotFoundException e) {
            s_logger.warn("Message field {} requires unknown class {}", i, val);
            type = Object.class;
          }
        } else {
          type = deserializer.getFudgeContext().getTypeDictionary().getByTypeId(((Number) val).intValue()).getJavaType();
        }
      } else {
        type = Object.class;
      }
      labels[i++] = deserializer.fieldValueToObject(type, itrValue.next());
    }
    return labels;
  }

  /**
   * Inverse of {@link #readXKeys}.
   * 
   * @param deserializer deserialization context
   * @param message message to read from
   * @return the keys
   */
  protected KX[] readXKeys(final FudgeDeserializer deserializer, final FudgeMsg message) {
    return readKeys(deserializer, message, getXKeyClass(), X_KEYS_KEY);
  }

  /**
   * Inverse of {@link #readXKeys}.
   * 
   * @param deserializer deserialization context
   * @param message message to read from
   * @return the keys
   */
  protected KY[] readYKeys(final FudgeDeserializer deserializer, final FudgeMsg message) {
    return readKeys(deserializer, message, getYKeyClass(), Y_KEYS_KEY);
  }

  /**
   * Inverse of {@link #readXKeys}.
   * 
   * @param deserializer deserialization context
   * @param message message to read from
   * @return the keys
   */
  protected KZ[] readZKeys(final FudgeDeserializer deserializer, final FudgeMsg message) {
    return readKeys(deserializer, message, getZKeyClass(), Z_KEYS_KEY);
  }

  /**
   * Inverse of {@link #writeKeys}.
   * 
   * @param <K> key type
   * @param deserializer deserialization context
   * @param message message to read from
   * @param keyClass common base type of the keys (as understood by the sender)
   * @param keysKey name of the field containing the keys
   * @return the keys
   */
  @SuppressWarnings("unchecked")
  protected <K> K[] readKeys(final FudgeDeserializer deserializer, final FudgeMsg message, final Class<K> keyClass, final String keysKey) {
    final FudgeMsg submsg = message.getMessage(keysKey);
    if (submsg == null) {
      s_logger.warn("Message field {} not found in {}", keysKey, message);
      throw new IllegalArgumentException("Message is not a LabelledMatrix3D - does not contain a " + keysKey + " field");
    }
    final K[] keys = (K[]) Array.newInstance(keyClass, submsg.getNumFields());
    int i = 0;
    for (FudgeField key : submsg) {
      keys[i++] = deserializer.fieldValueToObject(keyClass, key);
    }
    return keys;
  }

  protected Double[] readDoubleKeys(final FudgeMsg message, final String keysKey) {
    final FudgeField field = message.getByName(keysKey);
    if (field == null) {
      s_logger.warn("Message field {} not found in {}", keysKey, message);
      throw new IllegalArgumentException("Message is not a LabelledMatrix3D - does not contain a " + keysKey + " field");
    }
    final double[] keys = message.getFieldValue(double[].class, field);
    final Double[] arr = new Double[keys.length];
    for (int i = 0; i < keys.length; i++) {
      arr[i] = keys[i];
    }
    return arr;
  }

  /**
   * Inverse of {@link #writeValues}.
   * 
   * @param message message to read from
   * @param x number of values in the X dimension
   * @param y number of values in the Y dimension
   * @param z number of values in the Z dimension
   * @return the values
   */
  protected double[][][] readValues(final FudgeMsg message, final int x, final int y, final int z) {
    final FudgeField field = message.getByName(VALUES_KEY);
    if (field == null) {
      s_logger.warn("Message field {} not found in {}", VALUES_KEY, message);
      throw new IllegalArgumentException("Message is not a LabelledMatrix3D - does not contain a " + VALUES_KEY + " field");
    }
    final double[] flat = message.getFieldValue(double[].class, field);
    if (flat.length != x * y * z) {
      s_logger.warn("Invalid values length in {}", message);
      throw new IllegalArgumentException("Expected " + (x * y * z) + " matrix elements, got " + flat.length);
    }
    final double[][][] values = new double[z][y][x];
    int i = 0;
    for (double[][] slice : values) {
      for (double[] row : slice) {
        System.arraycopy(flat, i, row, 0, x);
        i += x;
      }
    }
    return values;
  }

  protected abstract T createMatrix(final KX[] xKeys, final Object[] xLabels, final KY[] yKeys, final Object[] yLabels, final KZ[] zKeys, final Object[] zLabels, final double[][][] values);

  /**
   * Builder for {@link DoubleLabelledMatrix3D}.
   */
  @FudgeBuilderFor(DoubleLabelledMatrix3D.class)
  public static final class DoubleBuilder extends LabelledMatrix3DFudgeBuilder<Double, Double, Double, DoubleLabelledMatrix3D> {

    public DoubleBuilder() {
      super(Double.class, Double.class, Double.class);
    }

    @Override
    protected void writeXKeys(final FudgeSerializer serializer, final MutableFudgeMsg message, final Double[] keys) {
      writeDoubleKeys(message, keys, X_KEYS_KEY);
    }

    @Override
    protected void writeYKeys(final FudgeSerializer serializer, final MutableFudgeMsg message, final Double[] keys) {
      writeDoubleKeys(message, keys, Y_KEYS_KEY);
    }

    @Override
    protected void writeZKeys(final FudgeSerializer serializer, final MutableFudgeMsg message, final Double[] keys) {
      writeDoubleKeys(message, keys, Z_KEYS_KEY);
    }

    @Override
    protected Double[] readXKeys(final FudgeDeserializer deserializer, final FudgeMsg message) {
      return readDoubleKeys(message, X_KEYS_KEY);
    }

    @Override
    protected Double[] readYKeys(final FudgeDeserializer deserializer, final FudgeMsg message) {
      return readDoubleKeys(message, Y_KEYS_KEY);
    }

    @Override
    protected Double[] readZKeys(final FudgeDeserializer deserializer, final FudgeMsg message) {
      return readDoubleKeys(message, Z_KEYS_KEY);
    }

    @Override
    protected DoubleLabelledMatrix3D createMatrix(final Double[] xKeys, final Object[] xLabels, final Double[] yKeys, final Object[] yLabels, final Double[] zKeys, final Object[] zLabels,
        final double[][][] values) {
      return new DoubleLabelledMatrix3D(xKeys, xLabels, yKeys, yLabels, zKeys, zLabels, values);
    }

  }

}
