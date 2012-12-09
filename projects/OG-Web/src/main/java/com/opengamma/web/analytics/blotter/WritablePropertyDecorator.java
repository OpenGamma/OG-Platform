/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.blotter;

import org.joda.beans.MetaBean;
import org.joda.beans.MetaProperty;
import org.joda.beans.PropertyReadWrite;

import com.opengamma.util.ArgumentChecker;

/**
 * Decorator that filters out read-only properties.
 * TODO is this ever going to be used?
 */
/* package */ class WritablePropertyDecorator<T> implements BeanVisitorDecorator<T> {

  private final BeanVisitor<T> _delegate;

  /* package */ WritablePropertyDecorator(BeanVisitor<T> delegate) {
    ArgumentChecker.notNull(delegate, "delegate");
    _delegate = delegate;
  }

  @Override
  public BeanVisitor<T> decorate(BeanVisitor<T> visitor) {
    return new BeanVisitor<T>() {
      @Override
      public void visitBean(MetaBean metaBean) {
        _delegate.visitBean(metaBean);
      }

      @Override
      public void visitBeanProperty(MetaProperty<?> property, BeanTraverser traverser) {
        if (isWriteable(property)) {
          _delegate.visitBeanProperty(property, traverser);
        }
      }

      @Override
      public void visitCollectionProperty(MetaProperty<?> property) {
        if (isWriteable(property)) {
          _delegate.visitCollectionProperty(property);
        }
      }

      @Override
      public void visitSetProperty(MetaProperty<?> property) {
        if (isWriteable(property)) {
          _delegate.visitSetProperty(property);
        }
      }

      @Override
      public void visitListProperty(MetaProperty<?> property) {
        if (isWriteable(property)) {
          _delegate.visitListProperty(property);
        }
      }

      @Override
      public void visitMapProperty(MetaProperty<?> property) {
        if (isWriteable(property)) {
          _delegate.visitMapProperty(property);
        }
      }

      @Override
      public void visitProperty(MetaProperty<?> property) {
        if (isWriteable(property)) {
          _delegate.visitProperty(property);
        }
      }

      @Override
      public T finish() {
        return _delegate.finish();
      }
    };
  }

  private static boolean isWriteable(MetaProperty<?> property) {
    return property.readWrite() != PropertyReadWrite.READ_ONLY;
  }
}
