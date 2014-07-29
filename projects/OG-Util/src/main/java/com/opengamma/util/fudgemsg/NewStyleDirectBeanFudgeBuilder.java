/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.fudgemsg;

import static org.fudgemsg.mapping.FudgeSerializer.addClassHeader;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeRuntimeException;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.joda.beans.Bean;
import org.joda.beans.BeanBuilder;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaBean;
import org.joda.beans.MetaProperty;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

/**
 * Builder to convert DirectBean to and from Fudge.
 * 
 * @param <T> the bean type
 */
public final class NewStyleDirectBeanFudgeBuilder<T extends Bean> implements FudgeBuilder<T> {

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
  public static <R extends Bean> NewStyleDirectBeanFudgeBuilder<R> of(final Class<R> cls) {
    MetaBean meta = JodaBeanUtils.metaBean(cls);
    return new NewStyleDirectBeanFudgeBuilder<R>(meta);
  }

  /**
   * Constructor.
   * @param metaBean  the meta-bean, not null
   */
  public NewStyleDirectBeanFudgeBuilder(MetaBean metaBean) {
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
          serializer.addToMessageWithClassHeaders(msg, prop.name(), null, obj, prop.propertyType()); // ignores null
        }
      }
      addClassHeader(msg, bean.getClass(), Bean.class);
      return msg;
    } catch (RuntimeException ex) {
      throw new FudgeRuntimeException("Unable to serialize: " + _metaBean.beanName(), ex);
    }
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
            Object value;
            try {
              //lets try first use type information included in fudge field itself
              value = deserializer.fieldValueToObject(field);
              if (!mp.propertyType().isAssignableFrom(value.getClass())) {
                // the automatically resolved type is not compatible with the bean expected property type.
                // let's see if we can convert the value to desired type
                if (mp.propertyType().equals(ImmutableSet.class) && value instanceof Set) {
                  value = ImmutableSet.copyOf((Set<?>) value);
                } else if (mp.propertyType().equals(ImmutableList.class) && value instanceof List) {
                  value = ImmutableList.copyOf((List<?>) value);
                } else if (mp.propertyType().equals(ImmutableMap.class) && value instanceof Map) {
                  value = ImmutableMap.copyOf((Map<?, ?>) value);
                }

                if (!mp.propertyType().isAssignableFrom(value.getClass())) {
                  // second check of type compatibility
                  // Now we try to deserialise the filed using type hinting.
                  value = deserializer.fieldValueToObject(mp.propertyType(), field);
                }
              }
            } catch (IllegalArgumentException ex) {
              if (field.getValue() instanceof String == false) {
                throw ex;
              }
              value = JodaBeanUtils.stringConverter().convertFromString(mp.propertyType(), (String) field.getValue());
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

}
