/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.id;

import java.io.Serializable;

import javax.time.calendar.LocalDate;

import org.apache.commons.lang.ObjectUtils;
import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.FudgeMessageFactory;
import org.fudgemsg.MutableFudgeFieldContainer;

import com.opengamma.util.ArgumentChecker;

/**
 * An immutable identifier with valid dates for an item.
 * <p>
 * It is made up of an {@link Identifier identifier} with valid start and end date
 * <p>
 * This class is immutable and thread-safe.
 */
public final class IdentifierWithDates implements Identifiable, Comparable<IdentifierWithDates>, Serializable {

  /**
   * Fudge message key for the valid_from.
   */
  public static final String VALID_FROM_FUDGE_FIELD_NAME = "ValidFrom";
  /**
   * Fudge message key for the valid_to.
   */
  public static final String VALID_TO_FUDGE_FIELD_NAME = "ValidTo";
  /**
   * The identifier
   */
  private final Identifier _identifier;
  /**
   * The valid start date
   */
  private final LocalDate _validFrom;
  /**
   * The valid end date
   */
  private final LocalDate _validTo;

  /**
   * Obtains an identifier with dates from an identifier and dates.
   * 
   * @param identifier  the identifier, not empty, not null
   * @param validFrom  the valid from date, may be null
   * @param validTo  the valid to date, may be null
   * @return the identifier, not null
   */
  public static IdentifierWithDates of(Identifier identifier, LocalDate validFrom, LocalDate validTo) {
    return new IdentifierWithDates(identifier, validFrom, validTo);
  }

  /**
   * Obtains an identifier with dates from a formatted scheme and value.
   * <p>
   * This parses the identifier from the form produced by {@code toString()}
   * which is {@code <SCHEME>::<VALUE>:S:<VALID_FROM>:E:<VALID_TO>}.
   * 
   * @param str  the identifier to parse, not null
   * @return the identifier, not null
   * @throws IllegalArgumentException if the identifier cannot be parsed
   */
  public static IdentifierWithDates parse(String str) {
    ArgumentChecker.notNull(str, "parse string");
    Identifier identifier = null;
    LocalDate validFrom = null;
    LocalDate validTo = null;
    int startPos = str.indexOf(":S:");
    int endPos = str.indexOf(":E");
    if (startPos > 0) {
      identifier = Identifier.parse(str.substring(0, startPos));
      if (endPos > 0) {
        validFrom = LocalDate.parse(str.substring(startPos + 3, endPos));
        validTo = LocalDate.parse(str.substring(endPos + 3));
      } else {
        validFrom = LocalDate.parse(str.substring(startPos + 3));
      }
    } else if (endPos > 0) {
      identifier = Identifier.parse(str.substring(0, endPos));
      validTo = LocalDate.parse(str.substring(endPos + 3));
    } else {
      identifier = Identifier.parse(str);
    }
    return new IdentifierWithDates(identifier, validFrom, validTo);
  }

  /**
   * Creates an instance.
   * 
   * @param identifier  the identifier, not null
   * @param validFrom  the valid from date, may be null
   * @param validTo  the valid to date, may be null
   */
  private IdentifierWithDates(Identifier identifier, LocalDate validFrom, LocalDate validTo) {
    ArgumentChecker.notNull(identifier, "identifier");
    if (validFrom != null && validTo != null) {
      ArgumentChecker.isTrue(validTo.isAfter(validFrom) || validTo.equals(validFrom), "ValidTo must be after or eqauls to ValidFrom");
    }
    _identifier = identifier;
    _validFrom = validFrom;
    _validTo = validTo;
  }

  //-------------------------------------------------------------------------
  @Override
  public Identifier getIdentityKey() {
    return _identifier;
  }

  /**
   * Gets the valid from date.
   * 
   * @return the valid from date, may be null
   */
  public LocalDate getValidFrom() {
    return _validFrom;
  }

  /**
   * Gets the valid to date.
   * 
   * @return the valid to date, may be null
   */
  public LocalDate getValidTo() {
    return _validTo;
  }

  //-------------------------------------------------------------------------
  /**
   * Returns the identifier without dates.
   * 
   * @return the identifier without dates, not null
   */
  public Identifier asIdentifier() {
    return _identifier;
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof IdentifierWithDates) {
      IdentifierWithDates other = (IdentifierWithDates) obj;
      return ObjectUtils.equals(_identifier, other._identifier) &&
              ObjectUtils.equals(_validFrom, other._validFrom) &&
              ObjectUtils.equals(_validTo, other._validTo);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return _identifier.hashCode() ^ ObjectUtils.hashCode(_validFrom) ^ ObjectUtils.hashCode(_validTo);
  }

  @Override
  public int compareTo(IdentifierWithDates other) {
    return _identifier.compareTo(other._identifier);
  }

  /**
   * Returns the identifier in the form {@code <SCHEME>::<VALUE>:S:<VALID_FROM>:E:<VALID_TO>}.
   * 
   * @return the identifier, not null
   */
  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(_identifier.toString());
    if (_validFrom != null) {
      buf.append(":S:").append(_validFrom.toString()); 
    }
    if (_validTo != null) {
      buf.append(":E:").append(_validTo.toString());
    }
    return buf.toString();
  }

  //-------------------------------------------------------------------------
  public MutableFudgeFieldContainer toFudgeMsg(final FudgeMessageFactory factory, final MutableFudgeFieldContainer message) {
    ArgumentChecker.notNull(factory, "factory");
    ArgumentChecker.notNull(message, "message");
    MutableFudgeFieldContainer fudgeMsg = _identifier.toFudgeMsg(factory, message);
    if (_validFrom != null) {
      fudgeMsg.add(VALID_FROM_FUDGE_FIELD_NAME, _validFrom);
    }
    if (_validTo != null) {
      fudgeMsg.add(VALID_TO_FUDGE_FIELD_NAME, _validTo);
    }
    return fudgeMsg;
  }

  /**
   * Serializes this pair to a Fudge message.
   * @param factory  the Fudge context, not null
   * @return the Fudge message, not null
   */
  public FudgeFieldContainer toFudgeMsg(FudgeMessageFactory factory) {
    return toFudgeMsg(factory, factory.newMessage());
  }

  /**
   * Deserializes this pair from a Fudge message.
   * @param msg  the Fudge message, not null
   * @return the pair, not null
   */
  public static IdentifierWithDates fromFudgeMsg(FudgeFieldContainer msg) {
    Identifier identifier = Identifier.fromFudgeMsg(msg);
    FudgeField field = msg.getByName(VALID_FROM_FUDGE_FIELD_NAME);
    LocalDate validFrom = null;
    if (field != null) {
      validFrom = (LocalDate) field.getValue();
    }
    field = msg.getByName(VALID_TO_FUDGE_FIELD_NAME);
    LocalDate validTo = null;
    if (field != null) {
      validTo = (LocalDate) field.getValue();
    }
    return IdentifierWithDates.of(identifier, validFrom, validTo);
  }

}
