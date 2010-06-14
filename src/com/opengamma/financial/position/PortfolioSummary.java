/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.position;

import javax.time.Instant;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.ArgumentChecker;

/**
 * A summary of a portfolio.
 */
public final class PortfolioSummary {

  /**
   * The unique identifier.
   */
  private UniqueIdentifier _uid;
  /**
   * The instant at which this version became valid.
   */
  private Instant _startInstant;
  /**
   * The instant at which this version is invalid.
   */
  private Instant _endInstant;
  /**
   * The portfolio name.
   */
  private String _name;
  /**
   * The portfolio name.
   */
  private int _totalPositions;
  /**
   * The status, true if active, false if deleted.
   */
  private boolean _active = true;

  /**
   * Creates an instance.
   */
  public PortfolioSummary() {
  }

  /**
   * Creates an instance.
   * @param uid  the unique identifier, not null
   */
  public PortfolioSummary(UniqueIdentifier uid) {
    setUniqueIdentifier(uid);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the unique identifier.
   * @return the unique identifier, not null
   */
  public UniqueIdentifier getUniqueIdentifier() {
    return _uid;
  }

  /**
   * Sets the unique identifier.
   * @param uid  the unique identifier, not null
   */
  public void setUniqueIdentifier(UniqueIdentifier uid) {
    ArgumentChecker.notNull(uid, "UniqueIdentifier");
    _uid = uid;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the name.
   * @return the name
   */
  public String getName() {
    return _name;
  }

  /**
   * Sets the name.
   * @param name  the name
   */
  public void setName(String name) {
    _name = name;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the start instant.
   * @return the start instant
   */
  public Instant getStartInstant() {
    return _startInstant;
  }

  /**
   * Sets the start instant.
   * @param instant  the start instant
   */
  public void setStartInstant(Instant instant) {
    _startInstant = instant;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the end instant.
   * @return the end instant
   */
  public Instant getEndInstant() {
    return _endInstant;
  }

  /**
   * Sets the end instant.
   * @param instant  the end instant
   */
  public void setEndInstant(Instant instant) {
    _endInstant = instant;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the total number of positions.
   * @return the total number of positions
   */
  public int getTotalPositions() {
    return _totalPositions;
  }

  /**
   * Sets the total number of positions.
   * @param totalPositions  the total number of positions
   */
  public void setTotalPositions(int totalPositions) {
    _totalPositions = totalPositions;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the status.
   * @return the status, true if active, false if deleted
   */
  public boolean isActive() {
    return _active;
  }

  /**
   * Sets the status.
   * @param active  the status, true if active, false if deleted
   */
  public void setActive(boolean active) {
    _active = active;
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }

}
