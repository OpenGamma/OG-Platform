/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.id;

import java.io.Serializable;

import javax.time.calendar.LocalDate;
import javax.time.calendar.Year;

import org.apache.commons.lang.ObjectUtils;
import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeMsgFactory;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;

import com.google.common.base.Objects;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

/**
 * An immutable external identifier with validity dates.
 * <p>
 * This class is used to restrict the validity of an {@link ExternalId external identifier}.
 * <p>
 * This class is immutable and thread-safe.
 */
public final class ExternalIdWithDates
    implements ExternalIdentifiable, Comparable<ExternalIdWithDates>, Serializable {
  static {
    OpenGammaFudgeContext.getInstance().getTypeDictionary().registerClassRename("com.opengamma.id.IdentifierWithDates", ExternalIdWithDates.class);
  }

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * Fudge message key for the valid_from.
   */
  public static final String VALID_FROM_FUDGE_FIELD_NAME = "ValidFrom";
  /**
   * Fudge message key for the valid_to.
   */
  public static final String VALID_TO_FUDGE_FIELD_NAME = "ValidTo";

  /**
   * The identifier.
   */
  private final ExternalId _identifier;
  /**
   * The valid start date, inclusive.
   */
  private final LocalDate _validFrom;
  /**
   * The valid end date, inclusive.
   */
  private final LocalDate _validTo;

  /**
   * Obtains an {@code ExternalIdWithDates} from an identifier and dates.
   * 
   * @param identifier  the identifier, not empty, not null
   * @param validFrom  the valid from date, inclusive, may be null
   * @param validTo  the valid to date, inclusive, may be null
   * @return the identifier, not null
   */
  public static ExternalIdWithDates of(ExternalId identifier, LocalDate validFrom, LocalDate validTo) {
    return new ExternalIdWithDates(identifier, validFrom, validTo);
  }

  /**
   * Parses an {@code ExternalIdWithDates} from a formatted scheme and value.
   * <p>
   * This parses the identifier from the form produced by {@code toString()}
   * which is {@code <SCHEME>~<VALUE>~S~<VALID_FROM>~E~<VALID_TO>}.
   * 
   * @param str  the identifier to parse, not null
   * @return the identifier, not null
   * @throws IllegalArgumentException if the identifier cannot be parsed
   */
  public static ExternalIdWithDates parse(String str) {
    ArgumentChecker.notNull(str, "parse string");
    ExternalId identifier = null;
    LocalDate validFrom = null;
    LocalDate validTo = null;
    int startPos = str.indexOf("~S~");
    int endPos = str.indexOf("~E~");
    if (startPos > 0) {
      identifier = ExternalId.parse(str.substring(0, startPos));
      if (endPos > 0) {
        validFrom = LocalDate.parse(str.substring(startPos + 3, endPos));
        validTo = LocalDate.parse(str.substring(endPos + 3));
      } else {
        validFrom = LocalDate.parse(str.substring(startPos + 3));
      }
    } else if (endPos > 0) {
      identifier = ExternalId.parse(str.substring(0, endPos));
      validTo = LocalDate.parse(str.substring(endPos + 3));
    } else {
      identifier = ExternalId.parse(str);
    }
    return new ExternalIdWithDates(identifier, validFrom, validTo);
  }

  /**
   * Creates an instance.
   * 
   * @param identifier  the identifier, not null
   * @param validFrom  the valid from date, may be null
   * @param validTo  the valid to date, may be null
   */
  private ExternalIdWithDates(ExternalId identifier, LocalDate validFrom, LocalDate validTo) {
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
  public ExternalId getExternalId() {
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
   * Checks if the identifier is valid on the specified date.
   * 
   * @param date  the date to check for validity on, null returns true
   * @return true if valid on the specified date
   */
  public boolean isValidOn(LocalDate date) {
    if (date == null) {
      return true;
    }
    LocalDate from = Objects.firstNonNull(getValidFrom(), LocalDate.of(Year.MIN_YEAR, 1, 1));  // TODO: JSR-310 far past/future
    LocalDate to = Objects.firstNonNull(getValidTo(), LocalDate.of(Year.MAX_YEAR, 12, 31));
    return date.isBefore(from) == false && date.isAfter(to) == false;
  }

  //-------------------------------------------------------------------------
  /**
   * Returns the identifier without dates.
   * 
   * @return the identifier without dates, not null
   */
  public ExternalId toExternalId() {
    return _identifier;
  }

  //-------------------------------------------------------------------------
  /**
   * Compares the external identifiers ignoring the dates.
   * This ordering is inconsistent with equals.
   * 
   * @param other  the other external identifier, not null
   * @return negative if this is less, zero if equal, positive if greater
   */
  @Override
  public int compareTo(ExternalIdWithDates other) {
    return _identifier.compareTo(other._identifier);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof ExternalIdWithDates) {
      ExternalIdWithDates other = (ExternalIdWithDates) obj;
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

  /**
   * Returns the identifier in the form {@code <SCHEME>~<VALUE>~S~<VALID_FROM>~E~<VALID_TO>}.
   * 
   * @return the identifier, not null
   */
  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(_identifier.toString());
    if (_validFrom != null) {
      buf.append("~S~").append(_validFrom.toString());
    }
    if (_validTo != null) {
      buf.append("~E~").append(_validTo.toString());
    }
    return buf.toString();
  }

  //-------------------------------------------------------------------------
  public MutableFudgeMsg toFudgeMsg(final FudgeMsgFactory factory, final MutableFudgeMsg message) {
    ArgumentChecker.notNull(factory, "factory");
    ArgumentChecker.notNull(message, "message");
    MutableFudgeMsg fudgeMsg = _identifier.toFudgeMsg(factory, message);
    if (_validFrom != null) {
      fudgeMsg.add(VALID_FROM_FUDGE_FIELD_NAME, _validFrom);
    }
    if (_validTo != null) {
      fudgeMsg.add(VALID_TO_FUDGE_FIELD_NAME, _validTo);
    }
    return fudgeMsg;
  }

  /**
   * Serializes to a Fudge message.
   * 
   * @param factory  the Fudge context, not null
   * @return the Fudge message, not null
   */
  public FudgeMsg toFudgeMsg(FudgeMsgFactory factory) {
    return toFudgeMsg(factory, factory.newMessage());
  }

  /**
   * Deserializes from a Fudge message.
   * 
   * @param fudgeContext  the Fudge context
   * @param msg  the Fudge message, not null
   * @return the pair, not null
   */
  public static ExternalIdWithDates fromFudgeMsg(FudgeDeserializer fudgeContext, FudgeMsg msg) {
    ExternalId identifier = ExternalId.fromFudgeMsg(fudgeContext, msg);
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
    return ExternalIdWithDates.of(identifier, validFrom, validTo);
  }

}
