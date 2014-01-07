/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.fudgemsg;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFactory;
import org.fudgemsg.mapping.FudgeBuilderFactoryAdapter;
import org.fudgemsg.mapping.FudgeMessageBuilder;
import org.fudgemsg.mapping.FudgeObjectBuilder;
import org.fudgemsg.mapping.FudgeObjectDictionary;
import org.joda.beans.Bean;
import org.joda.beans.impl.flexi.FlexiBean;

/**
 * Utilities for converting Beans to Fudge and vice versa.
 */
public final class ExtendedFudgeBuilderFactory extends FudgeBuilderFactoryAdapter {

  /**
   * Map of bean class to builder.
   */
  private final ConcurrentMap<Class<?>, FudgeObjectBuilder<?>> _builders =
      new ConcurrentHashMap<Class<?>, FudgeObjectBuilder<?>>();

  /**
   * Initializes an instance of this factory.
   * This extracts the existing factory from the dictionary, wraps it, and updates it.
   * @param dictionary  the object dictionary to install into, not null
   */
  public static void init(final FudgeObjectDictionary dictionary) {
    FudgeBuilderFactory factory = new ExtendedFudgeBuilderFactory(dictionary.getDefaultBuilderFactory());
    dictionary.setDefaultBuilderFactory(factory);
  }

  /**
   * Constructor.
   * @param parent  the parent factory, not null
   */
  private ExtendedFudgeBuilderFactory(final FudgeBuilderFactory parent) {
    super(parent);
  }

  //-------------------------------------------------------------------------
  @Override
  public <T> FudgeMessageBuilder<T> createMessageBuilder(final Class<T> clazz) {
    if (Bean.class.isAssignableFrom(clazz) && clazz != FlexiBean.class) {
      FudgeMessageBuilder<T> bld = super.createMessageBuilder(clazz);
      if (bld == null || bld.getClass().getSimpleName().equals("JavaBeanBuilder")) {  // best we can do
        return createBeanBuilder(clazz);
      }
      return bld;
    }
    return super.createMessageBuilder(clazz);
  }

  @Override
  public <T> FudgeObjectBuilder<T> createObjectBuilder(final Class<T> clazz) {
    if (Bean.class.isAssignableFrom(clazz) && clazz != FlexiBean.class) {
      FudgeObjectBuilder<T> bld = super.createObjectBuilder(clazz);
      if (bld == null || bld.getClass().getSimpleName().equals("JavaBeanBuilder")) {  // best we can do
        return createBeanBuilder(clazz);
      }
      return bld;
    }
    return super.createObjectBuilder(clazz);
  }

  /**
   * Creates a builder for the specific type of bean.
   * @param <T> the bean type
   * @param cls  the class required, not null
   * @return the builder, not null
   */
  @SuppressWarnings({"unchecked", "rawtypes" })
  private <T> FudgeBuilder<T> createBeanBuilder(final Class<T> cls) {
    FudgeBuilder<T> builder = (FudgeBuilder<T>) _builders.get(cls);
    if (builder == null) {
      builder = DirectBeanFudgeBuilder.of((Class) cls);
      _builders.putIfAbsent(cls, builder);
    }
    return builder;
  }

}
