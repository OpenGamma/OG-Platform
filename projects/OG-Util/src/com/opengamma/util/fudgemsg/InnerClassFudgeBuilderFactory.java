/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.fudgemsg;

import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFactory;
import org.fudgemsg.mapping.FudgeBuilderFactoryAdapter;
import org.fudgemsg.mapping.FudgeMessageBuilder;
import org.fudgemsg.mapping.FudgeObjectBuilder;
import org.fudgemsg.mapping.FudgeObjectDictionary;

/**
 * Utilities for converting Beans to Fudge and vice versa.
 */
public final class InnerClassFudgeBuilderFactory extends FudgeBuilderFactoryAdapter {

  /**
   * Map of bean class to builder.
   */
  @SuppressWarnings("rawtypes")
  private final FudgeBuilder _innerClassFudgeBuilder = new InnerClassFudgeBuilder();

  /**
   * Initializes an instance of this factory.
   * This extracts the existing factory from the dictionary, wraps it, and updates it.
   * @param dictionary  the object dictionary to install into, not null
   */
  public static void init(final FudgeObjectDictionary dictionary) {
    FudgeBuilderFactory factory = new InnerClassFudgeBuilderFactory(dictionary.getDefaultBuilderFactory());
    dictionary.setDefaultBuilderFactory(factory);
  }

  /**
   * Constructor.
   * @param parent  the parent factory, not null
   */
  private InnerClassFudgeBuilderFactory(final FudgeBuilderFactory parent) {
    super(parent);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> FudgeMessageBuilder<T> createMessageBuilder(final Class<T> clazz) {
    if (AutoFudgable.class.isAssignableFrom(clazz)) {
      return _innerClassFudgeBuilder;
    } else {
      return super.createMessageBuilder(clazz);
    }
  }


  @SuppressWarnings("unchecked")
  @Override
  public <T> FudgeObjectBuilder<T> createObjectBuilder(final Class<T> clazz) {
    if (AutoFudgable.class.isAssignableFrom(clazz)) {
      return _innerClassFudgeBuilder;
    } else {
      return super.createObjectBuilder(clazz);
    }
  }

}
