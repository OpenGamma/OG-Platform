/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.util.fudge;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.FudgeRuntimeException;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;
import org.fudgemsg.types.StringFieldType;
import org.joda.beans.Bean;
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
          context.objectToFudgeMsg(msg, prop.name(), null, obj);  // ignores null
        }
      }
      msg.add(null, 0, StringFieldType.INSTANCE, _metaBean.beanType().getName());
      return msg;
    } catch (RuntimeException ex) {
      throw new FudgeRuntimeException("Unable to serialize: " + _metaBean.beanName(), ex);
    }
  }

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
            Object value = context.fieldValueToObject(prop.propertyType(), field);
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

}
