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
  public void visitMetaBean(MetaBean metaBean) {
    _delegate.visitMetaBean(metaBean);
  }

  @Override
  public void visitBeanProperty(MetaProperty<?> property, BeanTraverser traverser) {
    _delegate.visitBeanProperty(property, traverser);
  }

  @Override
  public void visitCollectionProperty(MetaProperty<?> property, BeanTraverser traverser) {
    _delegate.visitCollectionProperty(property, traverser);
  }

  @Override
  public void visitSetProperty(MetaProperty<?> property, BeanTraverser traverser) {
    _delegate.visitSetProperty(property, traverser);
  }

  @Override
  public void visitListProperty(MetaProperty<?> property, BeanTraverser traverser) {
    _delegate.visitListProperty(property, traverser);
  }

  @Override
  public void visitMapProperty(MetaProperty<?> property, BeanTraverser traverser) {
    _delegate.visitMapProperty(property, traverser);
  }

  @Override
  public void visitProperty(MetaProperty<?> property, BeanTraverser traverser) {
    _delegate.visitProperty(property, traverser);
  }

  @Override
  public T finish() {
    return _delegate.finish();
  }
}
