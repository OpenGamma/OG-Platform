/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.target.lazy;

import java.util.Map;

import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.impl.SimplePortfolio;
import com.opengamma.engine.ComputationTargetResolver;
import com.opengamma.id.UniqueId;

/**
 * A portfolio implementation that may not be fully resolved at construction but will appear fully resolved when used.
 */
public final class LazyResolvedPortfolio extends LazyResolvedObject<Portfolio> implements Portfolio {

  private static final long serialVersionUID = 1L;

  private volatile PortfolioNode _rootNode;

  public LazyResolvedPortfolio(final LazyResolveContext.AtVersionCorrection context, final Portfolio underlying) {
    super(context, underlying);
  }

  @Override
  public Map<String, String> getAttributes() {
    return getUnderlying().getAttributes();
  }

  @Override
  public void setAttributes(Map<String, String> attributes) {
    getUnderlying().setAttributes(attributes);
  }

  @Override
  public void addAttribute(String key, String value) {
    getUnderlying().addAttribute(key, value);
  }

  @Override
  public UniqueId getUniqueId() {
    return getUnderlying().getUniqueId();
  }

  @Override
  public PortfolioNode getRootNode() {
    if (_rootNode == null) {
      PortfolioNode rootNode = null;
      synchronized (this) {
        if (_rootNode == null) {
          rootNode = new LazyResolvedPortfolioNode(getLazyResolveContext(), getUnderlying().getRootNode());
          _rootNode = rootNode;
        }
      }
      if (rootNode != null) {
        getLazyResolveContext().cachePortfolioNode(rootNode);
      }
    }
    return _rootNode;
  }

  @Override
  public String getName() {
    return getUnderlying().getName();
  }

  @Override
  protected TargetResolverPortfolio targetResolverObject(final ComputationTargetResolver.AtVersionCorrection resolver) {
    return new TargetResolverPortfolio(resolver, this);
  }

  @Override
  protected SimplePortfolio simpleObject() {
    return new SimplePortfolio(this);
  }

}
