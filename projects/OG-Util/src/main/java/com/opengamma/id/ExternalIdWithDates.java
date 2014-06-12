/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.id;

import java.io.Serializable;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;
import org.joda.beans.Bean;
import org.joda.beans.BeanDefinition;
import org.joda.beans.ImmutableBean;
import org.joda.beans.ImmutableConstructor;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectFieldsBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaBean;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;
import org.threeten.bp.LocalDate;

import com.google.common.base.Objects;
import com.opengamma.util.ArgumentChecker;

/**
 * An immutable external identifier with validity dates.
 * <p>
 * This class is used to restrict the validity of an {@link ExternalId external identifier}.
 * <p>
 * This class is immutable and thread-safe.
 */
@BeanDefinition(builderScope = "private")
public final class ExternalIdWithDates implements ImmutableBean,
    ExternalIdentifiable, Comparable<ExternalIdWithDates>, Serializable {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * The external identifier.
   */
  @PropertyDefinition(validate = "notNull")
  private final ExternalId _externalId;
  /**
   * The valid from date, inclusive, null means far past.
   */
  @PropertyDefinition
  private final LocalDate _validFrom;
  /**
   * The valid to date, inclusive, null means far future.
   */
  @PropertyDefinition
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
   * Obtains an {@code ExternalIdWithDates} from an {@code ExternalId}.
   * @param identifier the identifier, not empty, not null
   * @return the identifier, not null
   */
  public static ExternalIdWithDates of(ExternalId identifier) {
    return ExternalIdWithDates.of(identifier, null, null);
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
   * @param externalId  the identifier, not null
   * @param validFrom  the valid from date, may be null
   * @param validTo  the valid to date, may be null
   */
  @ImmutableConstructor
  private ExternalIdWithDates(ExternalId externalId, LocalDate validFrom, LocalDate validTo) {
    ArgumentChecker.notNull(externalId, "externalId");
    if (validFrom != null && validTo != null) {
      ArgumentChecker.isTrue(validTo.isAfter(validFrom) || validTo.equals(validFrom), "validTo (" + validTo + ") is before validFrom (" + validFrom + ")");
    }
    _externalId = externalId;
    _validFrom = validFrom;
    _validTo = validTo;
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
    LocalDate from = Objects.firstNonNull(getValidFrom(), LocalDate.MIN);
    LocalDate to = Objects.firstNonNull(getValidTo(), LocalDate.MAX);
    return date.isBefore(from) == false && date.isAfter(to) == false;
  }

  //-------------------------------------------------------------------------
  /**
   * Returns the identifier without dates.
   * 
   * @return the identifier without dates, not null
   */
  public ExternalId toExternalId() {
    return _externalId;
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
    return _externalId.compareTo(other._externalId);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof ExternalIdWithDates) {
      ExternalIdWithDates other = (ExternalIdWithDates) obj;
      return ObjectUtils.equals(_externalId, other._externalId) &&
          ObjectUtils.equals(_validFrom, other._validFrom) &&
          ObjectUtils.equals(_validTo, other._validTo);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return _externalId.hashCode() ^ ObjectUtils.hashCode(_validFrom) ^ ObjectUtils.hashCode(_validTo);
  }

  /**
   * Returns the identifier in the form {@code <SCHEME>~<VALUE>~S~<VALID_FROM>~E~<VALID_TO>}.
   * 
   * @return the identifier, not null
   */
  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(_externalId.toString());
    if (_validFrom != null) {
      buf.append("~S~").append(_validFrom.toString());
    }
    if (_validTo != null) {
      buf.append("~E~").append(_validTo.toString());
    }
    return buf.toString();
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code ExternalIdWithDates}.
   * @return the meta-bean, not null
   */
  public static ExternalIdWithDates.Meta meta() {
    return ExternalIdWithDates.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(ExternalIdWithDates.Meta.INSTANCE);
  }

  @Override
  public ExternalIdWithDates.Meta metaBean() {
    return ExternalIdWithDates.Meta.INSTANCE;
  }

  @Override
  public <R> Property<R> property(String propertyName) {
    return metaBean().<R>metaProperty(propertyName).createProperty(this);
  }

  @Override
  public Set<String> propertyNames() {
    return metaBean().metaPropertyMap().keySet();
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the external identifier.
   * @return the value of the property, not null
   */
  public ExternalId getExternalId() {
    return _externalId;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the valid from date, inclusive, null means far past.
   * @return the value of the property
   */
  public LocalDate getValidFrom() {
    return _validFrom;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the valid to date, inclusive, null means far future.
   * @return the value of the property
   */
  public LocalDate getValidTo() {
    return _validTo;
  }

  //-----------------------------------------------------------------------
  @Override
  public ExternalIdWithDates clone() {
    return this;
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code ExternalIdWithDates}.
   */
  public static final class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code externalId} property.
     */
    private final MetaProperty<ExternalId> _externalId = DirectMetaProperty.ofImmutable(
        this, "externalId", ExternalIdWithDates.class, ExternalId.class);
    /**
     * The meta-property for the {@code validFrom} property.
     */
    private final MetaProperty<LocalDate> _validFrom = DirectMetaProperty.ofImmutable(
        this, "validFrom", ExternalIdWithDates.class, LocalDate.class);
    /**
     * The meta-property for the {@code validTo} property.
     */
    private final MetaProperty<LocalDate> _validTo = DirectMetaProperty.ofImmutable(
        this, "validTo", ExternalIdWithDates.class, LocalDate.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "externalId",
        "validFrom",
        "validTo");

    /**
     * Restricted constructor.
     */
    private Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case -1699764666:  // externalId
          return _externalId;
        case -1110590010:  // validFrom
          return _validFrom;
        case 231246743:  // validTo
          return _validTo;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public ExternalIdWithDates.Builder builder() {
      return new ExternalIdWithDates.Builder();
    }

    @Override
    public Class<? extends ExternalIdWithDates> beanType() {
      return ExternalIdWithDates.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code externalId} property.
     * @return the meta-property, not null
     */
    public MetaProperty<ExternalId> externalId() {
      return _externalId;
    }

    /**
     * The meta-property for the {@code validFrom} property.
     * @return the meta-property, not null
     */
    public MetaProperty<LocalDate> validFrom() {
      return _validFrom;
    }

    /**
     * The meta-property for the {@code validTo} property.
     * @return the meta-property, not null
     */
    public MetaProperty<LocalDate> validTo() {
      return _validTo;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -1699764666:  // externalId
          return ((ExternalIdWithDates) bean).getExternalId();
        case -1110590010:  // validFrom
          return ((ExternalIdWithDates) bean).getValidFrom();
        case 231246743:  // validTo
          return ((ExternalIdWithDates) bean).getValidTo();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      metaProperty(propertyName);
      if (quiet) {
        return;
      }
      throw new UnsupportedOperationException("Property cannot be written: " + propertyName);
    }

  }

  //-----------------------------------------------------------------------
  /**
   * The bean-builder for {@code ExternalIdWithDates}.
   */
  private static final class Builder extends DirectFieldsBeanBuilder<ExternalIdWithDates> {

    private ExternalId _externalId;
    private LocalDate _validFrom;
    private LocalDate _validTo;

    /**
     * Restricted constructor.
     */
    private Builder() {
    }

    //-----------------------------------------------------------------------
    @Override
    public Object get(String propertyName) {
      switch (propertyName.hashCode()) {
        case -1699764666:  // externalId
          return _externalId;
        case -1110590010:  // validFrom
          return _validFrom;
        case 231246743:  // validTo
          return _validTo;
        default:
          throw new NoSuchElementException("Unknown property: " + propertyName);
      }
    }

    @Override
    public Builder set(String propertyName, Object newValue) {
      switch (propertyName.hashCode()) {
        case -1699764666:  // externalId
          this._externalId = (ExternalId) newValue;
          break;
        case -1110590010:  // validFrom
          this._validFrom = (LocalDate) newValue;
          break;
        case 231246743:  // validTo
          this._validTo = (LocalDate) newValue;
          break;
        default:
          throw new NoSuchElementException("Unknown property: " + propertyName);
      }
      return this;
    }

    @Override
    public Builder set(MetaProperty<?> property, Object value) {
      super.set(property, value);
      return this;
    }

    @Override
    public Builder setString(String propertyName, String value) {
      setString(meta().metaProperty(propertyName), value);
      return this;
    }

    @Override
    public Builder setString(MetaProperty<?> property, String value) {
      super.set(property, value);
      return this;
    }

    @Override
    public Builder setAll(Map<String, ? extends Object> propertyValueMap) {
      super.setAll(propertyValueMap);
      return this;
    }

    @Override
    public ExternalIdWithDates build() {
      return new ExternalIdWithDates(
          _externalId,
          _validFrom,
          _validTo);
    }

    //-----------------------------------------------------------------------
    @Override
    public String toString() {
      StringBuilder buf = new StringBuilder(128);
      buf.append("ExternalIdWithDates.Builder{");
      buf.append("externalId").append('=').append(JodaBeanUtils.toString(_externalId)).append(',').append(' ');
      buf.append("validFrom").append('=').append(JodaBeanUtils.toString(_validFrom)).append(',').append(' ');
      buf.append("validTo").append('=').append(JodaBeanUtils.toString(_validTo));
      buf.append('}');
      return buf.toString();
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
