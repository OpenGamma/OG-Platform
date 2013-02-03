/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.blotter;

import java.util.Arrays;
import java.util.Set;

import org.joda.beans.MetaBean;
import org.joda.beans.MetaProperty;

import com.google.common.collect.Sets;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
/* package */ class PropertyFilter implements BeanVisitorDecorator {

  /** The properties this will be filtered out. */
  private final Set<MetaProperty<?>> _properties;

  /* package */ PropertyFilter(MetaProperty<?>... properties) {
    ArgumentChecker.notNull(properties, "properties");
    _properties = Sets.newHashSet(Arrays.asList(properties));
  }

  @Override
  public BeanVisitor<?> decorate(final BeanVisitor<?> visitor) {
    return new BeanVisitor<Object>() {
      public void visitMetaBean(MetaBean metaBean) {
        visitor.visitMetaBean(metaBean);
      }

      public void visitBeanProperty(MetaProperty<?> property, BeanTraverser traverser) {
        if (!_properties.contains(property)) {
          visitor.visitBeanProperty(property, traverser);
        }
      }

      public void visitCollectionProperty(MetaProperty<?> property, BeanTraverser traverser) {
        if (!_properties.contains(property)) {
          visitor.visitCollectionProperty(property, traverser);
        }
      }

      public void visitSetProperty(MetaProperty<?> property, BeanTraverser traverser) {
        if (!_properties.contains(property)) {
          visitor.visitSetProperty(property, traverser);
        }
      }

      public void visitListProperty(MetaProperty<?> property, BeanTraverser traverser) {
        if (!_properties.contains(property)) {
          visitor.visitListProperty(property, traverser);
        }
      }

      public void visitMapProperty(MetaProperty<?> property, BeanTraverser traverser) {
        if (!_properties.contains(property)) {
          visitor.visitMapProperty(property, traverser);
        }
      }

      public void visitProperty(MetaProperty<?> property, BeanTraverser traverser) {
        if (!_properties.contains(property)) {
          visitor.visitProperty(property, traverser);
        }
      }

      public Object finish() {
        return visitor.finish();
      }
    };
  }
}
