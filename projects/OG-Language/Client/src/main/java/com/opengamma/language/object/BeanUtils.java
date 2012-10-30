/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.language.object;

import org.joda.beans.MetaBean;
import org.joda.beans.MetaProperty;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

/**
 *
 */
public final class BeanUtils {

  private BeanUtils() {
  }

  public static Iterable<MetaProperty<?>> writableMetaProperties(MetaBean metaBean) {
    return Iterables.filter(metaBean.metaPropertyIterable(), new Predicate<MetaProperty<?>>() {
      @Override
      public boolean apply(MetaProperty<?> property) {
        return property.readWrite().isWritable();
      }
    });
  }

}
