/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.position.db;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.id.Identifier;

/**
 * A Hibernate bean for identifiers.
 */
public class IdentifierAssociationBean extends DateIdentifiableBean {

  private PositionBean _position;
  private String _scheme;

  /**
   * Creates an instance.
   */
  public IdentifierAssociationBean() {
  }

  /**
   * Creates an instance based on another.
   * @param other  the instance to copy, not null
   */
  public IdentifierAssociationBean(final IdentifierAssociationBean other) {
    super(other);
    setPosition(other.getPosition());
    setScheme(other.getScheme());
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the position.
   * @return the position
   */
  public PositionBean getPosition() {
    return _position;
  }

  /**
   * Sets the position.
   * @param position  the position to set
   */
  public void setPosition(PositionBean position) {
    _position = position;
  }

  /**
   * Gets the scheme.
   * @return the scheme
   */
  public String getScheme() {
    return _scheme;
  }

  /**
   * Sets the scheme.
   * @param scheme  the scheme to set
   */
  public void setScheme(String scheme) {
    _scheme = scheme;
  }

  //-------------------------------------------------------------------------
  /**
   * Converts this to an {@code Identifier}.
   * @return the identifier, not null
   */
  public Identifier getDomainSpecificIdentifier() {
    return new Identifier(getScheme(), getIdentifier());
  }

  /**
   * Sets the fields from an {@code Identifier}.
   * @param identifier  the identifier, not null
   */
  public void setDomainSpecificIdentifier(final Identifier identifier) {
    setScheme(identifier.getScheme().getName());
    setIdentifier(identifier.getValue());
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean equals(final Object obj) {
    if (obj == this) {
      return true;
    }
    if (super.equals(obj)) {
      final IdentifierAssociationBean other = (IdentifierAssociationBean) obj;
      return ObjectUtils.equals(getPosition(), other.getPosition()) && ObjectUtils.equals(getScheme(), other.getScheme());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hc = super.hashCode();
    hc = hc * 17 + ObjectUtils.hashCode(getPosition());
    hc = hc * 17 + ObjectUtils.hashCode(getScheme());
    return hc;
  }

}
