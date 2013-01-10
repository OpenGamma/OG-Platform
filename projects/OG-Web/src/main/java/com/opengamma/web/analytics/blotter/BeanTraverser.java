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
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaBean;
import org.joda.beans.MetaProperty;

import com.google.common.collect.Lists;
import com.opengamma.util.ArgumentChecker;

/**
 * TODO is there a generally useful way to have pluggable handlers to override default behaviour for specific properties?
 * or would that have to be done in the visitors?
 * could also handle it by property name instead of using the metaproperty
 */
/* package */ class BeanTraverser {

  private final List<BeanVisitorDecorator> _decorators;

  /* package */ BeanTraverser() {
    _decorators = Collections.emptyList();
  }

  /* package */ BeanTraverser(BeanVisitorDecorator... decorators) {
    _decorators = Arrays.asList(decorators);
    // first decorator in the list should be on the outside, need to reverse before wrapping
    Collections.reverse(_decorators);
  }

  /* package */ Object traverse(MetaBean bean, BeanVisitor<?> visitor) {
    BeanVisitor<?> decoratedVisitor = decorate(visitor);
    decoratedVisitor.visitBean(bean);
    List<TraversalFailure> failures = Lists.newArrayList();
    for (MetaProperty<?> property : bean.metaPropertyIterable()) {
      Class<?> propertyType = property.propertyType();
      try {
        if (isConvertible(propertyType)) {
          decoratedVisitor.visitProperty(property);
        } else if (Bean.class.isAssignableFrom(propertyType)) {
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
      } catch (Exception e) {
        failures.add(new TraversalFailure(e, property));
        // TODO temporary. need to make the stack traces from traversal failures nice and obvious
        throw new RuntimeException(e);
      }
    }
    if (failures.isEmpty()) {
      return decoratedVisitor.finish();
    } else {
      throw new TraversalException(failures);
    }
  }

  private static boolean isConvertible(Class<?> type) {
    boolean canConvert;
    try {
      JodaBeanUtils.stringConverter().findConverter(type);
      canConvert = true;
    } catch (Exception e) {
      canConvert = false;
    }
    return canConvert;
  }

  private BeanVisitor<?> decorate(BeanVisitor<?> visitor) {
    BeanVisitor<?> decoratedVisitor = visitor;
    for (BeanVisitorDecorator decorator : _decorators) {
      decoratedVisitor = decorator.decorate(decoratedVisitor);
    }
    return decoratedVisitor;
  }

  /* package */ static final class TraversalFailure {

    private final Exception _exception;
    private final MetaProperty<?> _property;

    private TraversalFailure(Exception exception, MetaProperty<?> property) {
      ArgumentChecker.notNull(exception, "exception");
      ArgumentChecker.notNull(property, "property");
      _exception = exception;
      _property = property;
    }

    /* package */ Exception getException() {
      return _exception;
    }

    /* package */ MetaProperty<?> getProperty() {
      return _property;
    }
  }

  /* package */ static final class TraversalException extends RuntimeException {

    private final List<TraversalFailure> _failures;

    /* package */ TraversalException(List<TraversalFailure> failures) {
      super("Bean traversal failed");
      ArgumentChecker.notEmpty(failures, "failures");
      _failures = failures;
    }

    /* package */ List<TraversalFailure> getFailures() {
      return _failures;
    }
  }
}
