/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.fudge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.FudgeRuntimeException;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;
import org.fudgemsg.types.FudgeMsgFieldType;
import org.fudgemsg.types.IndicatorFieldType;
import org.fudgemsg.types.IndicatorType;
import org.joda.beans.Bean;
import org.joda.beans.BeanUtils;
import org.joda.beans.MetaBean;
import org.joda.beans.MetaProperty;

/**
 * Builder to convert DirectBean to and from Fudge.
 * 
 * @param <T> the bean type
 */
public final class DirectBeanBuilder<T extends Bean> implements FudgeBuilder<T> {

  /**
   * The meta bean for this instance.
   */
  private final MetaBean _metaBean;

  /**
   * Creates a builder from a class, using reflection to find the meta-bean.
   * @param <R> the bean type
   * @param cls  the class to get the builder for, not null
   * @return the bean builder, not null
   */
  public static <R extends Bean> DirectBeanBuilder<R> of(final Class<R> cls) {
    MetaBean meta;
    try {
      meta = (MetaBean) cls.getMethod("meta").invoke(null);
    } catch (RuntimeException ex) {
      throw ex;
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
    return new DirectBeanBuilder<R>(meta);
  }

  /**
   * Constructor.
   * @param metaBean  the meta-bean, not null
   */
  public DirectBeanBuilder(MetaBean metaBean) {
    _metaBean = metaBean;
  }

  //-------------------------------------------------------------------------
  // TODO: FudgeFieldName and Ordinal annotations

  @Override
  public MutableFudgeFieldContainer buildMessage(FudgeSerializationContext context, T bean) {
    try {
      MutableFudgeFieldContainer msg = context.newMessage();
      for (MetaProperty<Object> prop : bean.metaBean().metaPropertyIterable()) {
        if (prop.readWrite().isReadable()) {
          Object obj = prop.get(bean);
          if (obj instanceof List<?>) {
            MutableFudgeFieldContainer subMsg = buildMessageList(context, prop, (List<?>) obj);
            msg.add(prop.name(), null, FudgeMsgFieldType.INSTANCE, subMsg);
          } else if (obj instanceof Map<?, ?>) {
            MutableFudgeFieldContainer subMsg = buildMessageMap(context, prop, (Map<?, ?>) obj);
            msg.add(prop.name(), null, FudgeMsgFieldType.INSTANCE, subMsg);
          } else {
            context.objectToFudgeMsgWithClassHeaders(msg, prop.name(), null, obj, prop.propertyType()); // ignores null
          }
        }
      }
      return msg;
    } catch (RuntimeException ex) {
      throw new FudgeRuntimeException("Unable to serialize: " + _metaBean.beanName(), ex);
    }
  }

  private MutableFudgeFieldContainer buildMessageList(FudgeSerializationContext context, MetaProperty<Object> prop, List<?> list) {
    Class<?> contentType = BeanUtils.listType(prop);
    MutableFudgeFieldContainer msg = context.newMessage();
    for (Object entry : list) {
      if (entry == null) {
        msg.add(null, null, IndicatorFieldType.INSTANCE, IndicatorType.INSTANCE);
      } else if (contentType != null) {
        context.objectToFudgeMsg(msg, null, null, entry);
      } else {
        context.objectToFudgeMsgWithClassHeaders(msg, null, null, entry);
      }
    }
    return msg;
  }

  private MutableFudgeFieldContainer buildMessageMap(FudgeSerializationContext context, MetaProperty<Object> prop, Map<?, ?> map) {
    Class<?> keyType = BeanUtils.mapKeyType(prop);
    Class<?> valueType = BeanUtils.mapValueType(prop);
    MutableFudgeFieldContainer msg = context.newMessage();
    for (Map.Entry<?, ?> entry : map.entrySet()) {
      if (entry.getKey() == null) {
        msg.add(null, 1, IndicatorFieldType.INSTANCE, IndicatorType.INSTANCE);
      } else if (keyType != null) {
        context.objectToFudgeMsg(msg, null, 1, entry.getKey());
      } else {
        context.objectToFudgeMsgWithClassHeaders(msg, null, 1, entry.getKey());
      }
      if (entry.getValue() == null) {
        msg.add(null, 2, IndicatorFieldType.INSTANCE, IndicatorType.INSTANCE);
      } else if (valueType != null) {
        context.objectToFudgeMsg(msg, null, 2, entry.getValue());
      } else {
        context.objectToFudgeMsgWithClassHeaders(msg, null, 2, entry.getValue());
      }
    }
    return msg;
  }

  //-------------------------------------------------------------------------
  @SuppressWarnings("unchecked")
  @Override
  public T buildObject(FudgeDeserializationContext context, FudgeFieldContainer msg) {
    final T bean;
    try {
      bean = (T) _metaBean.createBean();
      for (MetaProperty<Object> prop : bean.metaBean().metaPropertyIterable()) {
        if (prop.readWrite().isWritable()) {
          final FudgeField field = msg.getByName(prop.name());
          if (field != null) {
            Object value = null;
            if (List.class.isAssignableFrom(prop.propertyType())) {
              value = field.getValue();
              if (value instanceof FudgeFieldContainer) {
                value = buildObjectList(context, prop, (FudgeFieldContainer) value);
              }
            } else if (Map.class.isAssignableFrom(prop.propertyType())) {
              value = field.getValue();
              if (value instanceof FudgeFieldContainer) {
                value = buildObjectMap(context, prop, (FudgeFieldContainer) value);
              }
            }
            if (value == null) {
              value = context.fieldValueToObject(prop.propertyType(), field);
            }
            if (value != null || prop.propertyType().isPrimitive() == false) {
              prop.set(bean, value);
            }
          }
        }
      }
    } catch (RuntimeException ex) {
      throw new FudgeRuntimeException("Unable to deserialize: " + _metaBean.beanName(), ex);
    }
    return bean;
  }

  private Object buildObjectList(FudgeDeserializationContext context, MetaProperty<Object> prop, FudgeFieldContainer msg) {
    Class<?> contentType = BeanUtils.listType(prop);
    List<Object> list = new ArrayList<Object>();
    for (FudgeField field : msg) {
      if (field.getOrdinal() != null && field.getOrdinal() != 1) {
        throw new IllegalArgumentException("Sub-message doesn't contain a list (bad field " + field + ")");
      }
      Object obj = (contentType == null ? context.fieldValueToObject(field) : context.fieldValueToObject(contentType, field));
      list.add((obj instanceof IndicatorType) ? null : obj);
    }
    return list;
  }

  private Object buildObjectMap(FudgeDeserializationContext context, MetaProperty<Object> prop, FudgeFieldContainer msg) {
    Class<?> keyType = BeanUtils.mapKeyType(prop);
    Class<?> valueType = BeanUtils.mapValueType(prop);
    Map<Object, Object> map = new HashMap<Object, Object>();
    Queue<Object> keys = new LinkedList<Object>();
    Queue<Object> values = new LinkedList<Object>();
    for (FudgeField field : msg) {
      if (field.getOrdinal() == 1) {
        Object fieldValue = (keyType == null ? context.fieldValueToObject(field) : context.fieldValueToObject(keyType, field));
        if (fieldValue instanceof IndicatorType) {
          fieldValue = null;
        }
        if (values.isEmpty()) {
          // no values ready, so store the key till next time
          keys.add(fieldValue);
        } else {
          // store key along with next value
          map.put(fieldValue, values.remove());
        }
      } else if (field.getOrdinal() == 2) {
        Object fieldValue = (valueType == null ? context.fieldValueToObject(field) : context.fieldValueToObject(valueType, field));
        if (fieldValue instanceof IndicatorType) {
          fieldValue = null;
        }
        if (keys.isEmpty()) {
          // no keys ready, so store the value till next time
          values.add(fieldValue);
        } else {
          // store value along with next key
          map.put(keys.remove(), fieldValue);
        }
      } else {
        throw new IllegalArgumentException("Sub-message doesn't contain a map (bad field " + field + ")");
      }
    }
    return map;
  }

}
