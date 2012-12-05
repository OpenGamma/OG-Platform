/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.blotter;

import java.util.Collection;
import java.util.Map;

import org.joda.beans.Bean;
import org.joda.beans.MetaBean;
import org.joda.beans.MetaProperty;

import com.opengamma.util.ArgumentChecker;

/**
 * {@link BeanVisitor} that builds an object by pushing data from a bean into a {@link BeanDataSink sink}.
 * TODO a MUCH better name is required, this and BeanBuildingVisitor are too similar
 * TODO is this class worth the complication or would it be better for sinks to imlement BeanVisitor directly?
 */
@SuppressWarnings("unchecked")
/* package */ class BuildingBeanVisitor<T> implements BeanVisitor<T> {

  private final Bean _bean;
  private final BeanDataSink<T> _sink;

  /* package */ BuildingBeanVisitor(Bean bean, BeanDataSink<T> sink) {
    ArgumentChecker.notNull(bean, "bean");
    ArgumentChecker.notNull(sink, "sink");
    _bean = bean;
    _sink = sink;
  }

  @Override
  public void visitBean(MetaBean metaBean) {
    if (!_bean.getClass().equals(metaBean.beanType())) {
      throw new IllegalArgumentException("Bean type " + _bean.getClass().getName() + " is not the same as " +
                                             "MetaBean type " + metaBean.beanType().getName());
    }
    _sink.setBeanData(metaBean, _bean);
  }

  @Override
  public void visitBeanProperty(MetaProperty<?> property, BeanTraverser traverser) {
    _sink.setBeanValue(property.name(), (Bean) property.get(_bean), traverser);
  }

  @Override
  public void visitCollectionProperty(MetaProperty<?> property) {
    _sink.setCollectionValues(property.name(), (Collection<String>) property.get(_bean));
  }

  @Override
  public void visitSetProperty(MetaProperty<?> property) {
    _sink.setCollectionValues(property.name(), (Collection<String>) property.get(_bean));
  }

  @Override
  public void visitListProperty(MetaProperty<?> property) {
    _sink.setCollectionValues(property.name(), (Collection<String>) property.get(_bean));
  }

  @Override
  public void visitMapProperty(MetaProperty<?> property) {
    _sink.setMapValues(property.name(), (Map<?, ?>) property.get(_bean));
  }

  @Override
  public void visitProperty(MetaProperty<?> property) {
    _sink.setValue(property.name(), property.getString(_bean));
  }

  @Override
  public T finish() {
    return _sink.finish();
  }
}

