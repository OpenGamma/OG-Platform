/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.comparison;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.mapping.FudgeSerializer;
import org.fudgemsg.wire.types.FudgeWireType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.core.security.Security;
import com.opengamma.master.security.ManageableSecurityFudgeBuilder;

/* package */final class SecurityInfo extends AbstractInfo<Security> {

  private static final Logger s_logger = LoggerFactory.getLogger(SecurityInfo.class);

  private final Map<Object, Object> _info = new HashMap<Object, Object>();

  public SecurityInfo(final ComparisonContext context, final Security security) {
    super(security);
    s_logger.debug("Extracting core information from {}", security);
    final FudgeSerializer serializer = context.getFudgeSerializer();
    serializer.reset();
    final FudgeMsg rawMsg = serializer.objectToFudgeMsg(security);
    s_logger.debug("Raw message = {}", rawMsg);
    final Iterator<FudgeField> itr = rawMsg.iterator();
    while (itr.hasNext()) {
      final FudgeField field = itr.next();
      if (ManageableSecurityFudgeBuilder.UNIQUE_ID_FIELD_NAME.equals(field.getName()) || ManageableSecurityFudgeBuilder.IDENTIFIERS_FIELD_NAME.equals(field.getName())) {
        continue;
      }
      addFieldToMap(field, _info);
    }
    s_logger.debug("Info = {}", _info);
  }

  @SuppressWarnings({"unchecked", "rawtypes" })
  private static void addValueToMap(final Object key, final Object value, final Map<Object, Object> info) {
    final Object existing = info.get(key);
    if (existing != null) {
      if (existing instanceof List) {
        ((List) existing).add(value);
      } else {
        final List list = new ArrayList();
        list.add(existing);
        list.add(value);
      }
    } else {
      info.put(key, value);
    }
  }

  private static void addFieldValueToMap(final FudgeField field, final Object value, final Map<Object, Object> info) {
    if (field.getName() != null) {
      addValueToMap(field.getName(), value, info);
    } else {
      addValueToMap(field.getOrdinal(), value, info);
    }
  }

  private static void addFieldToMap(final FudgeField field, final Map<Object, Object> info) {
    final Object value;
    switch (field.getType().getTypeId()) {
      case FudgeWireType.BYTE_ARRAY_TYPE_ID:
      case FudgeWireType.BYTE_ARRAY_4_TYPE_ID:
      case FudgeWireType.BYTE_ARRAY_8_TYPE_ID:
      case FudgeWireType.BYTE_ARRAY_16_TYPE_ID:
      case FudgeWireType.BYTE_ARRAY_20_TYPE_ID:
      case FudgeWireType.BYTE_ARRAY_32_TYPE_ID:
      case FudgeWireType.BYTE_ARRAY_64_TYPE_ID:
      case FudgeWireType.BYTE_ARRAY_128_TYPE_ID:
      case FudgeWireType.BYTE_ARRAY_256_TYPE_ID:
      case FudgeWireType.BYTE_ARRAY_512_TYPE_ID:
        value = new ComparableByteArray((byte[]) field.getValue());
        break;
      case FudgeWireType.DOUBLE_ARRAY_TYPE_ID:
        value = new ComparableDoubleArray((double[]) field.getValue());
        break;
      case FudgeWireType.FLOAT_ARRAY_TYPE_ID:
        value = new ComparableFloatArray((float[]) field.getValue());
        break;
      case FudgeWireType.INT_ARRAY_TYPE_ID:
        value = new ComparableIntArray((int[]) field.getValue());
        break;
      case FudgeWireType.LONG_ARRAY_TYPE_ID:
        value = new ComparableLongArray((long[]) field.getValue());
        break;
      case FudgeWireType.SHORT_ARRAY_TYPE_ID:
        value = new ComparableShortArray((short[]) field.getValue());
        break;
      case FudgeWireType.SUB_MESSAGE_TYPE_ID: {
        final Map<Object, Object> subMsg = new HashMap<Object, Object>();
        for (FudgeField subField : (FudgeMsg) field.getValue()) {
          addFieldToMap(subField, subMsg);
        }
        value = subMsg;
        break;
      }
      default:
        value = field.getValue();
        break;
    }
    addFieldValueToMap(field, value, info);
  }

  private abstract static class ComparableArray<T> {

    private final T _data;
    private final int _hashCode;

    protected ComparableArray(final int hashCode, final T data) {
      _hashCode = hashCode;
      _data = data;
    }

    protected T getData() {
      return _data;
    }

    @Override
    public final int hashCode() {
      return _hashCode;
    }

    protected abstract boolean equalsImpl(final ComparableArray<?> o);

    @Override
    public final boolean equals(final Object o) {
      if (o == this) {
        return true;
      }
      if (!(o instanceof ComparableArray<?>)) {
        return false;
      }
      final ComparableArray<?> other = (ComparableArray<?>) o;
      if (other.hashCode() != hashCode()) {
        return false;
      }
      return equalsImpl(other);
    }

    protected abstract String toString(final T data);

    @Override
    public String toString() {
      return toString(getData());
    }

  }

  private static final class ComparableByteArray extends ComparableArray<byte[]> {

    private ComparableByteArray(final byte[] data) {
      super(Arrays.hashCode(data), data);
    }

    @Override
    protected boolean equalsImpl(final ComparableArray<?> o) {
      if (o instanceof ComparableByteArray) {
        return Arrays.equals(((ComparableByteArray) o).getData(), getData());
      } else {
        return false;
      }
    }

    @Override
    protected String toString(final byte[] data) {
      return Arrays.toString(data);
    }

  }

  private static final class ComparableDoubleArray extends ComparableArray<double[]> {

    private ComparableDoubleArray(final double[] data) {
      super(Arrays.hashCode(data), data);
    }

    @Override
    protected boolean equalsImpl(final ComparableArray<?> o) {
      if (o instanceof ComparableDoubleArray) {
        return Arrays.equals(((ComparableDoubleArray) o).getData(), getData());
      } else {
        return false;
      }
    }

    @Override
    protected String toString(final double[] data) {
      return Arrays.toString(data);
    }

  }

  private static final class ComparableFloatArray extends ComparableArray<float[]> {

    private ComparableFloatArray(final float[] data) {
      super(Arrays.hashCode(data), data);
    }

    @Override
    protected boolean equalsImpl(final ComparableArray<?> o) {
      if (o instanceof ComparableFloatArray) {
        return Arrays.equals(((ComparableFloatArray) o).getData(), getData());
      } else {
        return false;
      }
    }

    @Override
    protected String toString(final float[] data) {
      return Arrays.toString(data);
    }

  }

  private static final class ComparableIntArray extends ComparableArray<int[]> {

    private ComparableIntArray(final int[] data) {
      super(Arrays.hashCode(data), data);
    }

    @Override
    protected boolean equalsImpl(final ComparableArray<?> o) {
      if (o instanceof ComparableIntArray) {
        return Arrays.equals(((ComparableIntArray) o).getData(), getData());
      } else {
        return false;
      }
    }

    @Override
    protected String toString(final int[] data) {
      return Arrays.toString(data);
    }

  }

  private static final class ComparableLongArray extends ComparableArray<long[]> {

    private ComparableLongArray(final long[] data) {
      super(Arrays.hashCode(data), data);
    }

    @Override
    protected boolean equalsImpl(final ComparableArray<?> o) {
      if (o instanceof ComparableLongArray) {
        return Arrays.equals(((ComparableLongArray) o).getData(), getData());
      } else {
        return false;
      }
    }

    @Override
    protected String toString(final long[] data) {
      return Arrays.toString(data);
    }

  }

  private static final class ComparableShortArray extends ComparableArray<short[]> {

    private ComparableShortArray(final short[] data) {
      super(Arrays.hashCode(data), data);
    }

    @Override
    protected boolean equalsImpl(final ComparableArray<?> o) {
      if (o instanceof ComparableShortArray) {
        return Arrays.equals(((ComparableShortArray) o).getData(), getData());
      } else {
        return false;
      }
    }

    @Override
    protected String toString(final short[] data) {
      return Arrays.toString(data);
    }

  }

  public String getName() {
    return getUnderlying().getName();
  }

  public String getSecurityType() {
    return getUnderlying().getSecurityType();
  }

  private Map<Object, Object> getInfo() {
    return _info;
  }

  @Override
  public boolean equals(final Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof SecurityInfo)) {
      return false;
    }
    final SecurityInfo other = (SecurityInfo) o;
    // Info message contains all fields necessary for equality
    return getInfo().equals(other.getInfo());
  }

  @Override
  public int hashCode() {
    return getInfo().hashCode();
  }

}
