/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.blotter;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joda.beans.Bean;
import org.joda.beans.MetaBean;
import org.joda.beans.MetaProperty;

/**
 *
 */
/* package */ class BeanTraverser {

  private final List<BeanVisitorDecorator> _decorators;

  /* package */ BeanTraverser() {
    _decorators = Collections.emptyList();
  }

  /* package */ BeanTraverser(BeanVisitorDecorator... decorators) {
    _decorators = Arrays.asList(decorators);
  }

  /* package */ <T> T traverse(MetaBean bean, BeanVisitor<T> visitor) {
    BeanVisitor<T> decoratedVisitor = decorate(visitor);
    decoratedVisitor.visitBean(bean);
    for (MetaProperty<?> property : bean.metaPropertyIterable()) {
      Class<?> propertyType = property.propertyType();
      if (Bean.class.isAssignableFrom(propertyType)) {
        decoratedVisitor.visitBeanProperty(property, this);
      } else if (Set.class.isAssignableFrom(propertyType)) {
        decoratedVisitor.visitSetProperty(property);
      } else if (List.class.isAssignableFrom(propertyType)) {
        decoratedVisitor.visitListProperty(property);
      } else if (Collection.class.isAssignableFrom(propertyType)) {
        decoratedVisitor.visitCollectionProperty(property);
      } else if (Map.class.isAssignableFrom(propertyType)) {
        decoratedVisitor.visitMapProperty(property);
      } else {
        decoratedVisitor.visitProperty(property);
      }
    }
    return decoratedVisitor.finish();
  }

  private <T> BeanVisitor<T> decorate(BeanVisitor<T> visitor) {
    BeanVisitor<T> decoratedVisitor = visitor;
    for (BeanVisitorDecorator decorator : _decorators) {
      decoratedVisitor = decorator.decorate(decoratedVisitor);
    }
    return decoratedVisitor;
  }
}
