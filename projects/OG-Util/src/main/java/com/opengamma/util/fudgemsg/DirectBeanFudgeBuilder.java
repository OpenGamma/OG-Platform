/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.fudgemsg;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeRuntimeException;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.fudgemsg.types.IndicatorType;
import org.fudgemsg.wire.types.FudgeWireType;
import org.joda.beans.Bean;
import org.joda.beans.BeanBuilder;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaBean;
import org.joda.beans.MetaProperty;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.SortedSetMultimap;
import com.google.common.collect.TreeMultimap;

/**
 * Builder to convert DirectBean to and from Fudge.
 *
 * @param <T> the bean type
 */
public final class DirectBeanFudgeBuilder<T extends Bean> implements FudgeBuilder<T> {

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
  public static <R extends Bean> DirectBeanFudgeBuilder<R> of(final Class<R> cls) {
    MetaBean meta = JodaBeanUtils.metaBean(cls);
    return new DirectBeanFudgeBuilder<R>(meta);
  }

  /**
   * Constructor.
   * @param metaBean  the meta-bean, not null
   */
  public DirectBeanFudgeBuilder(MetaBean metaBean) {
    _metaBean = metaBean;
  }

  //-------------------------------------------------------------------------
  // TODO: FudgeFieldName and Ordinal annotations

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, T bean) {
    try {
      MutableFudgeMsg msg = serializer.newMessage();
      for (MetaProperty<?> prop : bean.metaBean().metaPropertyIterable()) {
        if (prop.style().isReadable()) {
          Object obj = prop.get(bean);
          if (obj instanceof List<?>) {
            MutableFudgeMsg subMsg = buildMessageCollection(serializer, prop, bean.getClass(), (List<?>) obj);
            msg.add(prop.name(), null, FudgeWireType.SUB_MESSAGE, subMsg);
          } else if (obj instanceof Set<?>) {
            MutableFudgeMsg subMsg = buildMessageCollection(
                serializer, prop, bean.getClass(), new ArrayList<Object>((Set<?>) obj));
            msg.add(prop.name(), null, FudgeWireType.SUB_MESSAGE, subMsg);
          } else if (obj instanceof Map<?, ?>) {
            MutableFudgeMsg subMsg = buildMessageMap(serializer, bean.getClass(), prop, (Map<?, ?>) obj);
            msg.add(prop.name(), null, FudgeWireType.SUB_MESSAGE, subMsg);
          } else if (obj instanceof Multimap<?, ?>) {
            MutableFudgeMsg subMsg = buildMessageMultimap(serializer, bean.getClass(), prop, (Multimap<?, ?>) obj);
            msg.add(prop.name(), null, FudgeWireType.SUB_MESSAGE, subMsg);
          } else {
            serializer.addToMessageWithClassHeaders(msg, prop.name(), null, obj, prop.propertyType()); // ignores null
          }
        }
      }
      return msg;
    } catch (RuntimeException ex) {
      throw new FudgeRuntimeException("Unable to serialize: " + _metaBean.beanName(), ex);
    }
  }

  private MutableFudgeMsg buildMessageCollection(
      FudgeSerializer serializer, MetaProperty<?> prop, Class<?> beanType, List<?> list) {
    
    Class<?> contentType = JodaBeanUtils.collectionType(prop, beanType);
    MutableFudgeMsg msg = serializer.newMessage();
    for (Object entry : list) {
      if (entry == null) {
        msg.add(null, null, FudgeWireType.INDICATOR, IndicatorType.INSTANCE);
      } else if (contentType == null) {
        serializer.addToMessage(msg, null, null, entry);
      } else {
        serializer.addToMessageWithClassHeaders(msg, null, null, entry, contentType);
      }
    }
    return msg;
  }

  private MutableFudgeMsg buildMessageMap(FudgeSerializer serializer, Class<?> beanType, MetaProperty<?> prop,
                                          Map<?, ?> map) {
    return buildMessageMapFromEntries(map.entrySet(), serializer, beanType, prop);
  }

  private MutableFudgeMsg buildMessageMultimap(FudgeSerializer serializer, Class<?> beanType, MetaProperty<?> prop,
                                               Multimap<?, ?> multimap) {
    return buildMessageMapFromEntries(multimap.entries(), serializer, beanType, prop);
  }

  private MutableFudgeMsg buildMessageMapFromEntries(Collection<? extends Map.Entry<?, ?>> entries,
                                                     FudgeSerializer serializer,
                                                     Class<?> beanType,
                                                     MetaProperty<?> prop) {
    Class<?> keyType = JodaBeanUtils.mapKeyType(prop, beanType);
    Class<?> valueType = JodaBeanUtils.mapValueType(prop, beanType);
    MutableFudgeMsg msg = serializer.newMessage();
    for (Map.Entry<?, ?> entry : entries) {
      if (entry.getKey() == null) {
        msg.add(null, 1, FudgeWireType.INDICATOR, IndicatorType.INSTANCE);
      } else if (keyType == null) {
        serializer.addToMessage(msg, null, 1, entry.getKey());
      } else {
        serializer.addToMessageWithClassHeaders(msg, null, 1, entry.getKey(), keyType);
      }
      if (entry.getValue() == null) {
        msg.add(null, 2, FudgeWireType.INDICATOR, IndicatorType.INSTANCE);
      } else if (valueType == null) {
        serializer.addToMessage(msg, null, 2, entry.getValue());
      } else {
        serializer.addToMessageWithClassHeaders(msg, null, 2, entry.getValue(), valueType);
      }
    }
    return msg;
  }

  //-------------------------------------------------------------------------
  @SuppressWarnings("unchecked")
  @Override
  public T buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
    try {
      BeanBuilder<T> builder = (BeanBuilder<T>) _metaBean.builder();
      for (MetaProperty<?> mp : _metaBean.metaPropertyIterable()) {
        if (mp.style().isBuildable()) {
          final FudgeField field = msg.getByName(mp.name());
          if (field != null) {
            Object value = null;
            if (List.class.isAssignableFrom(mp.propertyType())) {
              value = field.getValue();
              if (value instanceof FudgeMsg) {
                value = buildObjectList(
                    deserializer, mp, _metaBean.beanType(), (FudgeMsg) value);
              }
            } else if (SortedSet.class.isAssignableFrom(mp.propertyType())) {
              value = field.getValue();
              if (value instanceof FudgeMsg) {
                value = buildObjectSet(
                    deserializer, mp, _metaBean.beanType(), (FudgeMsg) value, new TreeSet<>());
              }
            } else if (Set.class.isAssignableFrom(mp.propertyType())) {
              value = field.getValue();
              if (value instanceof FudgeMsg) {
                value = buildObjectSet(
                    deserializer, mp, _metaBean.beanType(), (FudgeMsg) value, new LinkedHashSet<>());
              }
            } else if (Map.class.isAssignableFrom(mp.propertyType())) {
              value = field.getValue();
              if (value instanceof FudgeMsg) {
                value = buildObjectMap(
                    deserializer, mp, _metaBean.beanType(), (FudgeMsg) value);
              }
            } else if (ListMultimap.class.isAssignableFrom(mp.propertyType())) {
              value = field.getValue();
              if (value instanceof FudgeMsg) {
                value = buildObjectMultimap(
                    deserializer, mp, _metaBean.beanType(), (FudgeMsg) value, ArrayListMultimap.create());
              }
            } else if (SortedSetMultimap.class.isAssignableFrom(mp.propertyType())) {
              value = field.getValue();
              if (value instanceof FudgeMsg) {
                value = buildObjectMultimap(
                    deserializer, mp, _metaBean.beanType(), (FudgeMsg) value, TreeMultimap.create());
              }
            } else if (Multimap.class.isAssignableFrom(mp.propertyType())) {
              // In the absence of other information we'll create a hash multimap
              value = field.getValue();
              if (value instanceof FudgeMsg) {
                value = buildObjectMultimap(
                    deserializer, mp, _metaBean.beanType(), (FudgeMsg) value, HashMultimap.create());
              }
            }
            if (value == null) {
              try {
                if (mp.propertyType() == Object.class) {
                  value = deserializer.fieldValueToObject(field);
                } else {
                  value = deserializer.fieldValueToObject(mp.propertyType(), field);
                }
              } catch (IllegalArgumentException ex) {
                if (field.getValue() instanceof String == false) {
                  throw ex;
                }
                value = JodaBeanUtils.stringConverter().convertFromString(mp.propertyType(), (String) field.getValue());
              }
            }
            if (value != null || mp.propertyType().isPrimitive() == false) {
              builder.set(mp.name(), value);
            }
          }
        }
      }
      return builder.build();
    } catch (RuntimeException ex) {
      throw new FudgeRuntimeException("Unable to deserialize: " + _metaBean.beanName(), ex);
    }
  }

  private List<Object> buildObjectList(FudgeDeserializer deserializer, MetaProperty<?> prop, Class<?> type, FudgeMsg msg) {
    Class<?> contentType = JodaBeanUtils.collectionType(prop, type);
    List<Object> list = new ArrayList<Object>();  // should be List<contentType>
    for (FudgeField field : msg) {
      if (field.getOrdinal() != null && field.getOrdinal() != 1) {
        throw new IllegalArgumentException("Sub-message doesn't contain a list (bad field " + field + ")");
      }
      Object obj = buildField(deserializer, contentType, field);
      list.add((obj instanceof IndicatorType) ? null : obj);
    }
    return list;
  }

  private Set<Object> buildObjectSet(FudgeDeserializer deserializer, MetaProperty<?> prop, Class<?> type, FudgeMsg msg, Set<Object> set) {
    Class<?> contentType = JodaBeanUtils.collectionType(prop, type);
    for (FudgeField field : msg) {
      if (field.getOrdinal() != null && field.getOrdinal() != 1) {
        throw new IllegalArgumentException("Sub-message doesn't contain a set (bad field " + field + ")");
      }
      Object obj = buildField(deserializer, contentType, field);
      set.add((obj instanceof IndicatorType) ? null : obj);
    }
    return set;
  }

  private Map<Object, Object> buildObjectMap(FudgeDeserializer deserializer, MetaProperty<?> prop, Class<?> type, FudgeMsg msg) {
    Class<?> keyType = JodaBeanUtils.mapKeyType(prop, type);
    Class<?> valueType = JodaBeanUtils.mapValueType(prop, type);
    Map<Object, Object> map; // should be Map<keyType,contentType>
    if (SortedMap.class.isAssignableFrom(prop.propertyType())) {
      map = new TreeMap<>();
    } else {
      map = Maps.newHashMap();  
    }
    Queue<Object> keys = new LinkedList<>();
    Queue<Object> values = new LinkedList<>();
    for (FudgeField field : msg) {
      if (field.getOrdinal() == 1) {
        Object fieldValue = buildField(deserializer, keyType, field);
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
        Object fieldValue = buildField(deserializer, valueType, field);
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

  @SuppressWarnings({"unchecked", "rawtypes" })
  private Multimap<Object, Object> buildObjectMultimap(FudgeDeserializer deserializer,
                                                       MetaProperty<?> prop,
                                                       Class<?> type,
                                                       FudgeMsg msg,
                                                       Multimap multimap) {

    Class<?> keyType = JodaBeanUtils.mapKeyType(prop, type);
    Class<?> valueType = JodaBeanUtils.mapValueType(prop, type);
    Queue<Object> keys = new LinkedList<>();
    Queue<Object> values = new LinkedList<>();
    for (FudgeField field : msg) {
      if (field.getOrdinal() == 1) {
        Object fieldValue = buildField(deserializer, keyType, field);
        if (fieldValue instanceof IndicatorType) {
          fieldValue = null;
        }
        if (values.isEmpty()) {
          // no values ready, so store the key till next time
          keys.add(fieldValue);
        } else {
          // store key along with next value
          multimap.put(fieldValue, values.remove());
        }
      } else if (field.getOrdinal() == 2) {
        Object fieldValue = buildField(deserializer, valueType, field);
        if (fieldValue instanceof IndicatorType) {
          fieldValue = null;
        }
        if (keys.isEmpty()) {
          // no keys ready, so store the value till next time
          values.add(fieldValue);
        } else {
          // store value along with next key
          multimap.put(keys.remove(), fieldValue);
        }
      } else {
        throw new IllegalArgumentException("Sub-message doesn't contain a map (bad field " + field + ")");
      }
    }
    return multimap;
  }

  private Object buildField(FudgeDeserializer deserializer, Class<?> type, FudgeField field) {
    if (isAbstractLike(type)) {
      return deserializer.fieldValueToObject(field);
    }
    return deserializer.fieldValueToObject(type, field);
  }

  private boolean isAbstractLike(Class<?> type) {
    return type == null ||
        type.isInterface() ||
        (Modifier.isAbstract(type.getModifiers()) && type.isPrimitive() == false) ||
        type == Object.class;
  }

}
