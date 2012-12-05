/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.blotter;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joda.beans.Bean;
import org.joda.beans.MetaBean;
import org.joda.beans.MetaProperty;

import com.opengamma.util.ArgumentChecker;

/**
 * TODO static traversal method(s)?
 */
/* package */ class BeanTraverser {

  private final Set<String> _ignorePropertyNames;
  private final Set<MetaProperty<?>> _ignoreProperties;

  /* package */ BeanTraverser() {
    this(Collections.<MetaProperty<?>>emptySet(), Collections.<String>emptySet());
  }

  // TODO ignoring properties could be implemented as a decorating visitor
  /* package */ BeanTraverser(Set<MetaProperty<?>> ignoreProperties, Set<String> ignorePropertyNames) {
    ArgumentChecker.notNull(ignoreProperties, "ignoreProperties");
    ArgumentChecker.notNull(ignorePropertyNames, "ignorePropertyNames");
    _ignoreProperties = ignoreProperties;
    _ignorePropertyNames = ignorePropertyNames;
  }

  /* package */ <T> T traverse(MetaBean bean, BeanVisitor<T> visitor) {
    visitor.visitBean(bean);
    for (MetaProperty<?> property : bean.metaPropertyIterable()) {
      if (_ignoreProperties.contains(property)) {
        continue;
      }
      String propertyName = property.name();
      if (_ignorePropertyNames.contains(propertyName)) {
        continue;
      }
      Class<?> propertyType = property.propertyType();
      if (Bean.class.isAssignableFrom(propertyType)) {
        visitor.visitBeanProperty(property, this);
      } else if (Set.class.isAssignableFrom(propertyType)) {
        visitor.visitSetProperty(property);
      } else if (List.class.isAssignableFrom(propertyType)) {
        visitor.visitListProperty(property);
      } else if (Collection.class.isAssignableFrom(propertyType)) {
        visitor.visitCollectionProperty(property);
      } else if (Map.class.isAssignableFrom(propertyType)) {
        visitor.visitMapProperty(property);
      } else {
        visitor.visitProperty(property);
      }
    }
    return visitor.finish();
  }
}
