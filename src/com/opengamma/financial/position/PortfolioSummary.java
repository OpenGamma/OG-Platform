/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.position;

import javax.time.Instant;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

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
   * The total number of positions at any depth.
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
   * Gets the total number of positions at any depth.
   * @return the total number of positions
   */
  public int getTotalPositions() {
    return _totalPositions;
  }

  /**
   * Sets the total number of positions at any depth.
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

  //-------------------------------------------------------------------------
  /** Field name. */
  private static final String UID_FIELD_NAME = "uid";
  /** Field name. */
  private static final String NAME_FIELD_NAME = "name";
  /** Field name. */
  private static final String START_INSTANT_FIELD_NAME = "startInstant";
  /** Field name. */
  private static final String END_INSTANT_FIELD_NAME = "endInstant";
  /** Field name. */
  private static final String TOTAL_POSITIONS_FIELD_NAME = "totalPositions";
  /** Field name. */
  private static final String ACTIVE_FIELD_NAME = "active";

  /**
   * Serializes to a Fudge message.
   * @param context  the Fudge context, not null
   * @return the Fudge message, not null
   */
  public FudgeFieldContainer toFudgeMsg(final FudgeSerializationContext context) {
    MutableFudgeFieldContainer msg = context.newMessage();
    if (_uid != null) {
      msg.add(UID_FIELD_NAME, _uid.toFudgeMsg(context));
    }
    if (_name != null) {
      msg.add(NAME_FIELD_NAME, _name);
    }
    if (_startInstant != null) {
      msg.add(START_INSTANT_FIELD_NAME, _startInstant);
    }
    if (_endInstant != null) {
      msg.add(END_INSTANT_FIELD_NAME, _endInstant);
    }
    msg.add(TOTAL_POSITIONS_FIELD_NAME, _totalPositions);
    msg.add(ACTIVE_FIELD_NAME, _active);
    return msg;
  }

  /**
   * Deserializes from a Fudge message.
   * @param context  the Fudge context, not null
   * @param msg  the Fudge message, not null
   * @return the pair, not null
   */
  public static PortfolioSummary fromFudgeMsg(final FudgeDeserializationContext context, final FudgeFieldContainer msg) {
    PortfolioSummary summary = new PortfolioSummary();
    if (msg.hasField(UID_FIELD_NAME)) {
      summary.setUniqueIdentifier(UniqueIdentifier.fromFudgeMsg(msg.getMessage(UID_FIELD_NAME)));
    }
    if (msg.hasField(NAME_FIELD_NAME)) {
      summary.setName(msg.getString(NAME_FIELD_NAME));
    }
    if (msg.hasField(START_INSTANT_FIELD_NAME)) {
      summary.setStartInstant(msg.getValue(Instant.class, START_INSTANT_FIELD_NAME));
    }
    if (msg.hasField(END_INSTANT_FIELD_NAME)) {
      summary.setEndInstant(msg.getValue(Instant.class, END_INSTANT_FIELD_NAME));
    }
    summary.setTotalPositions(msg.getInt(TOTAL_POSITIONS_FIELD_NAME));
    summary.setActive(msg.getBoolean(ACTIVE_FIELD_NAME));
    return summary;
  }

}
