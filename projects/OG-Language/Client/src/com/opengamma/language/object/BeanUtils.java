package com.opengamma.language.object;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.joda.beans.MetaBean;
import org.joda.beans.MetaProperty;

/**
 *
 */
public class BeanUtils {

  private BeanUtils() {
  }

  public static Iterable<MetaProperty<Object>> writableMetaProperties(MetaBean metaBean) {
    return Iterables.filter(metaBean.metaPropertyIterable(), new Predicate<MetaProperty<Object>>() {
      @Override
      public boolean apply(MetaProperty<Object> property) {
        return property.readWrite().isWritable();
      }
    });
  }
}
