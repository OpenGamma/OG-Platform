/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.position;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.opengamma.engine.position.Portfolio;
import com.opengamma.engine.position.PortfolioNode;
import com.opengamma.engine.position.PortfolioNodeImpl;
import com.opengamma.util.ArgumentChecker;

/**
 * A request to add a portfolio.
 */
public final class AddPortfolioRequest {

  /**
   * The portfolio name.
   */
  private String _name;
  /**
   * The portfolio root node.
   */
  private PortfolioNode _rootNode;

  /**
   * Creates an instance.
   */
  public AddPortfolioRequest() {
  }

  /**
   * Creates an instance.
   * @param portfolio  the portfolio to copy, not null
   */
  public AddPortfolioRequest(Portfolio portfolio) {
    setName(portfolio.getName());
    setRootNode(portfolio.getRootNode());
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the name to change to.
   * @return the name
   */
  public String getName() {
    return _name;
  }

  /**
   * Sets the name to change to.
   * @param name  the name
   */
  public void setName(String name) {
    _name = StringUtils.trimToNull(name);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the root node.
   * @return the root node, not null
   */
  public PortfolioNode getRootNode() {
    return _rootNode;
  }

  /**
   * Sets the portfolio unique identifier.
   * @param node  the root node, not null
   */
  public void setRootNode(PortfolioNode node) {
    ArgumentChecker.notNull(node, "PortfolioNode");
    _rootNode = node;
  }

  //-------------------------------------------------------------------------
  /**
   * Validates this request throwing an exception if not.
   */
  public void checkValid() {
    Validate.notEmpty(getName(), "Name must not be null");
    if (getRootNode() == null) {
      setRootNode(new PortfolioNodeImpl(getName()));
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }

}
