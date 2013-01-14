/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.target.lazy;

import java.util.Map;

import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.engine.ComputationTargetResolver;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.id.UniqueId;

/**
 * A portfolio implementation that defers to a target resolver for the component parts.
 */
public final class TargetResolverPortfolio extends TargetResolverObject implements Portfolio {

  private static final long serialVersionUID = 1L;

  private final Map<String, String> _attributes;
  private final UniqueId _uniqueId;
  private final ComputationTargetSpecification _rootNodeSpec;
  private transient volatile PortfolioNode _rootNode;
  private final String _name;

  public TargetResolverPortfolio(final ComputationTargetResolver.AtVersionCorrection targetResolver, final Portfolio copyFrom) {
    super(targetResolver);
    _attributes = copyFrom.getAttributes();
    _uniqueId = copyFrom.getUniqueId();
    _rootNodeSpec = ComputationTargetSpecification.of(copyFrom.getRootNode());
    _name = copyFrom.getName();
  }

  @Override
  public Map<String, String> getAttributes() {
    return _attributes;
  }

  @Override
  public void setAttributes(Map<String, String> attributes) {
    _attributes.clear();
    _attributes.putAll(attributes);
  }

  @Override
  public void addAttribute(String key, String value) {
    _attributes.put(key, value);
  }

  @Override
  public UniqueId getUniqueId() {
    return _uniqueId;
  }

  @Override
  public PortfolioNode getRootNode() {
    if (_rootNode == null) {
      synchronized (this) {
        if (_rootNode == null) {
          _rootNode = new LazyTargetResolverPortfolioNode(getTargetResolver(), _rootNodeSpec);
        }
      }
    }
    return _rootNode;
  }

  @Override
  public String getName() {
    return _name;
  }

}
