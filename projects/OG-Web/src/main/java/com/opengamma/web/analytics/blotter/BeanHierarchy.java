/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.blotter;

import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.joda.beans.Bean;
import org.joda.beans.MetaBean;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 *
 */
// TODO do I need to worry about properties whose type is an interface with implementations that are beans?
/* package */ class BeanHierarchy {

  private final Map<Class<?>, Node> _nodes = Maps.newHashMap();
  private final Set<Class<?>> _classesWithMetaBean = Sets.newHashSet();

  /* package */ BeanHierarchy(Set<MetaBean> metaBeans) {
    for (MetaBean metaBean : metaBeans) {
      _classesWithMetaBean.add(metaBean.beanType());
    }
    for (MetaBean metaBean : metaBeans) {
      getOrAddNode(metaBean.beanType());
    }
  }

  private Node getOrAddNode(Class<?> type) {
    Node node = _nodes.get(type);
    if (node != null) {
      return node;
    }
    Node newNode = new Node(type);
    _nodes.put(type, newNode);
    Class<?> superclass = type.getSuperclass();
    if (superclass != null) {
      getOrAddNode(superclass).addSubclass(newNode);
    }
    return newNode;
  }

  /* package */ Set<Class<? extends Bean>> subtypes(Class<?> type) {
    Node node = _nodes.get(type);
    if (node == null) {
      return Collections.emptySet();
    }
    Set<Class<? extends Bean>> types = Sets.newHashSet();
    addSubtypes(node, types);
    return types;
  }

  @SuppressWarnings("unchecked")
  private void addSubtypes(Node node, Set<Class<? extends Bean>> types) {
    Class<?> type = node.getType();
    if (isConcreteBeanWithMetaBean(type)) {
      types.add((Class<? extends Bean>) type);
    }
    for (Node subclassNode : node.getSubclasses()) {
      addSubtypes(subclassNode, types);
    }
  }

  /**
   * @param type A type
   * @return true if type is a concrete subclass of {@link Bean} whose {@link MetaBean} was in the set passed to
   * the constructor.
   */
  private boolean isConcreteBeanWithMetaBean(Class<?> type) {
    return Bean.class.isAssignableFrom(type) &&
        (type.getModifiers() & Modifier.ABSTRACT) != Modifier.ABSTRACT &&
        _classesWithMetaBean.contains(type);
  }

  private static final class Node {

    private final Class<?> _type;
    private final Set<Node> _subclasses = Sets.newHashSet();

    private Node(Class<?> type) {
      _type = type;
    }

    private void addSubclass(Node subclassNode) {
      _subclasses.add(subclassNode);
    }

    private Class<?> getType() {
      return _type;
    }

    private Set<Node> getSubclasses() {
      return _subclasses;
    }
  }
}
