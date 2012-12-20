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
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

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
      ArgumentChecker.isTrue(validTo.isAfter(validFrom) || validTo.equals(validFrom), "validTo (" + validTo + ") is before validFrom (" + validFrom + ")");
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
  /**
   * This is for more efficient code within the .proto representations of securities, allowing this class
   * to be used directly as a message type instead of through the serialization framework.
   * 
   * @param serializer  the serializer, not null
   * @param msg  the message to populate, not null
   * @deprecated Use builder
   */
  @Deprecated
  public void toFudgeMsg(final FudgeSerializer serializer, final MutableFudgeMsg msg) {
    ExternalIdWithDatesFudgeBuilder.toFudgeMsg(serializer, this, msg);
  }

  /**
   * This is for more efficient code within the .proto representations of securities, allowing this class
   * to be used directly as a message type instead of through the serialization framework.
   * 
   * @param deserializer  the deserializer, not null
   * @param msg  the message to decode, not null
   * @return the created object, not null
   * @deprecated Use builder
   */
  @Deprecated
  public static ExternalIdWithDates fromFudgeMsg(final FudgeDeserializer deserializer, final FudgeMsg msg) {
    return ExternalIdWithDatesFudgeBuilder.fromFudgeMsg(deserializer, msg);
  }

}
