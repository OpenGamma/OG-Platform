/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.blotter;

import org.joda.beans.MetaBean;
import org.joda.beans.MetaProperty;

import com.opengamma.util.ArgumentChecker;

/**
 * TODO do I need this?
 */
/* package */ abstract class DelegatingVisitor<T> implements BeanVisitor<T> {

  private final BeanVisitor<T> _delegate;

  /* package */ DelegatingVisitor(BeanVisitor<T> delegate) {
    ArgumentChecker.notNull(delegate, "delegate");
    _delegate = delegate;
  }

  @Override
  public void visitBean(MetaBean metaBean) {
    _delegate.visitBean(metaBean);
  }

  @Override
  public void visitBeanProperty(MetaProperty<?> property, BeanTraverser traverser) {
    _delegate.visitBeanProperty(property, traverser);
  }

  @Override
  public void visitCollectionProperty(MetaProperty<?> property) {
    _delegate.visitCollectionProperty(property);
  }

  @Override
  public void visitSetProperty(MetaProperty<?> property) {
    _delegate.visitSetProperty(property);
  }

  @Override
  public void visitListProperty(MetaProperty<?> property) {
    _delegate.visitListProperty(property);
  }

  @Override
  public void visitMapProperty(MetaProperty<?> property) {
    _delegate.visitMapProperty(property);
  }

  @Override
  public void visitProperty(MetaProperty<?> property) {
    _delegate.visitProperty(property);
  }

  @Override
  public T finish() {
    return _delegate.finish();
  }
}
