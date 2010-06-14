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

import com.opengamma.engine.position.PortfolioNode;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.ArgumentChecker;

/**
 * A request to add a portfolio node.
 */
public final class AddPortfolioNodeRequest {

  /**
   * The parent node unique identifier.
   */
  private UniqueIdentifier _parentUid;
  /**
   * The portfolio node name.
   */
  private String _name;

  /**
   * Creates an instance.
   */
  public AddPortfolioNodeRequest() {
  }

  /**
   * Creates an instance.
   * @param portfolioNode  the portfolio node to copy, not null
   */
  public AddPortfolioNodeRequest(PortfolioNode portfolioNode) {
    setName(portfolioNode.getName());
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the parent node unique identifier.
   * @return the unique identifier, not null
   */
  public UniqueIdentifier getParentNode() {
    return _parentUid;
  }

  /**
   * Sets the parent node unique identifier.
   * @param parentUid  the unique identifier, not null
   */
  public void setParentNode(UniqueIdentifier parentUid) {
    ArgumentChecker.notNull(parentUid, "UniqueIdentifier");
    _parentUid = parentUid;
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
    _name = StringUtils.trim(name);
  }

  //-------------------------------------------------------------------------
  /**
   * Validates this request throwing an exception if not.
   */
  public void checkValid() {
    Validate.notNull(getParentNode(), "Parent must not be null");
    Validate.notEmpty(getName(), "Name must not be empty");
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }

}
