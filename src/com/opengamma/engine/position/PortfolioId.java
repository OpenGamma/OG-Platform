/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.position;

import java.io.Serializable;

import com.opengamma.util.ArgumentChecker;

/**
 * An immutable identifier for a portfolio.
 * <p>
 * This identifier is used as a handle within the system to refer to a portfolio.
 * It will typically refer to a specific portfolio provided by a persistent data source.
 * When the position master is queried with an identifier it previously returned, it
 * must return the same {@code Position}.
 */
public final class PortfolioId implements Comparable<PortfolioId>, Serializable {

  /**
   * The identifier.
   */
  private final String _id;

  /**
   * Obtains an identifier.
   * @param identifier  the identifier of the portfolio, not empty, not null
   * @return the portfolio id, never null
   */
  public static PortfolioId of(String identifier) {
    return new PortfolioId(identifier);
  }

  /**
   * Constructor.
   * @param identifier  the identifier of the portfolio, not empty, not null
   */
  private PortfolioId(String identifier) {
    ArgumentChecker.notEmpty(identifier, "identifier");
    _id = identifier;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the identifier.
   * @return the identifier, not empty, never null
   */
  public String getId() {
    return _id;
  }

  //-------------------------------------------------------------------------
  /**
   * Compares the identifiers based on {@code String} comparison.
   * @param other  the other identifier, not null
   * @return negative if this is less, zero if equal, positive if greater
   */
  @Override
  public int compareTo(PortfolioId other) {
    return _id.compareTo(other._id);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof PortfolioId) {
      PortfolioId other = (PortfolioId) obj;
      return _id.equals(other._id);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return _id.hashCode() + 234; 
  }

  /**
   * Returns the identifier.
   * @return the identifier, not empty, never null
   */
  @Override
  public String toString() {
    return _id;
  }

}
