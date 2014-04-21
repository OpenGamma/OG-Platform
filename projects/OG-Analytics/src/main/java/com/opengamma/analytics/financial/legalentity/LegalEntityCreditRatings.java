/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.legalentity;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.joda.beans.Bean;
import org.joda.beans.BeanBuilder;
import org.joda.beans.BeanDefinition;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaBean;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;
import com.opengamma.util.types.ParameterizedTypeImpl;

/**
 * Gets the credit ratings of an {@link LegalEntity}.
 */
@BeanDefinition
public class LegalEntityCreditRatings implements LegalEntityFilter<LegalEntity>, Bean {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * True if the rating is to be used as a filter.
   */
  @PropertyDefinition
  private boolean _useRating;

  /**
   * A set of agencies to be used to filter by rating.
   */
  @PropertyDefinition(validate = "notNull", set = "manual")
  private Set<String> _perAgencyRatings;

  /**
   * True if the rating description is to be used as a filter.
   */
  @PropertyDefinition
  private boolean _useRatingDescription;

  /**
   * A set of agencies to be used to filter by rating description.
   */
  @PropertyDefinition(validate = "notNull", set = "manual")
  private Set<String> _perAgencyRatingDescriptions;

  /**
   * For the builder.
   */
  public LegalEntityCreditRatings() {
    setUseRating(false);
    setPerAgencyRatings(Collections.<String>emptySet());
    setUseRatingDescription(false);
    setPerAgencyRatingDescriptions(Collections.<String>emptySet());
  }

  /**
   * @param useRating True if the rating is to be used as a filter
   * @param ratings A set of agencies to be used to filter by rating, not null. Can be empty
   * @param useRatingDescription True if the rating description is to be used as a filter
   * @param ratingDescriptions A set of agencies to be used to filter by rating description, not null. Can be empty
   */
  public LegalEntityCreditRatings(final boolean useRating, final Set<String> ratings, final boolean useRatingDescription, final Set<String> ratingDescriptions) {
    setUseRating(useRating);
    setPerAgencyRatings(ratings);
    setUseRatingDescription(useRatingDescription);
    setPerAgencyRatingDescriptions(ratingDescriptions);
  }

  @Override
  public Object getFilteredData(final LegalEntity legalEntity) {
    ArgumentChecker.notNull(legalEntity, "legal entity");
    final Set<CreditRating> creditRatings = legalEntity.getCreditRatings();
    if (creditRatings == null) {
      throw new IllegalStateException("Credit ratings for this legal entity " + legalEntity + " were null");
    }
    if (!(_useRating || _useRatingDescription)) {
      return creditRatings;
    }
    int ratingCount = 0;
    int ratingDescriptionCount = 0;
    final Set<Object> selections = new HashSet<>();
    for (final CreditRating creditRating : creditRatings) {
      final String agencyName = creditRating.getAgencyName();
      if (_useRating) {
        final Pair<String, String> agencyRatingPair = Pairs.of(agencyName, creditRating.getRating());
        if (_perAgencyRatings.isEmpty()) {
          selections.add(agencyRatingPair);
        }
        if (_perAgencyRatings.contains(agencyName)) {
          selections.add(agencyRatingPair);
          ratingCount++;
        }
      }
      if (_useRatingDescription) {
        if (creditRating.getRatingDescription() == null) {
          throw new IllegalStateException("Credit rating " + creditRating + " does not contain rating description for " + agencyName);
        }
        final Pair<String, String> agencyRatingDescription = Pairs.of(agencyName, creditRating.getRatingDescription());
        if (_perAgencyRatingDescriptions.isEmpty()) {
          selections.add(agencyRatingDescription);
        }
        if (_perAgencyRatingDescriptions.contains(agencyName)) {
          selections.add(agencyRatingDescription);
          ratingDescriptionCount++;
        }
      }
    }
    if (_useRating && ratingCount != _perAgencyRatings.size()) {
      throw new IllegalStateException("Credit ratings " + creditRatings + " do not contain matches for " + _perAgencyRatings);
    }
    if (_useRatingDescription && ratingDescriptionCount != _perAgencyRatingDescriptions.size()) {
      throw new IllegalStateException("Credit ratings " + creditRatings + " do not contain matches for " + _perAgencyRatingDescriptions);
    }
    return selections;
  }

  @Override
  public Type getFilteredDataType() {
    if (!(_useRating || _useRatingDescription)) {
      return LegalEntity.meta().creditRatings().propertyGenericType();
    }
    return ParameterizedTypeImpl.of(Set.class, ParameterizedTypeImpl.of(Pair.class, String.class, String.class));
  }

  /**
   * Sets the agencies with which to filter ratings. This also sets the {@link LegalEntityCreditRatings#_useRating} field to true.
   * 
   * @param perAgencyRatings The new value of the property, not null
   */
  public void setPerAgencyRatings(final Set<String> perAgencyRatings) {
    JodaBeanUtils.notNull(perAgencyRatings, "perAgencyRatings");
    if (!perAgencyRatings.isEmpty()) {
      setUseRating(true);
    }
    this._perAgencyRatings = perAgencyRatings;
  }

  /**
   * Sets the agencies with which to filter rating descriptions. This also sets the {@link LegalEntityCreditRatings#_useRatingDescription} field to true.
   * 
   * @param perAgencyRatingDescriptions The new value of the property, not null
   */
  public void setPerAgencyRatingDescriptions(final Set<String> perAgencyRatingDescriptions) {
    JodaBeanUtils.notNull(perAgencyRatingDescriptions, "perAgencyRatingDescriptions");
    if (!perAgencyRatingDescriptions.isEmpty()) {
      setUseRatingDescription(true);
    }
    this._perAgencyRatingDescriptions = perAgencyRatingDescriptions;
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code LegalEntityCreditRatings}.
   * @return the meta-bean, not null
   */
  public static LegalEntityCreditRatings.Meta meta() {
    return LegalEntityCreditRatings.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(LegalEntityCreditRatings.Meta.INSTANCE);
  }

  @Override
  public LegalEntityCreditRatings.Meta metaBean() {
    return LegalEntityCreditRatings.Meta.INSTANCE;
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
   * Gets true if the rating is to be used as a filter.
   * @return the value of the property
   */
  public boolean isUseRating() {
    return _useRating;
  }

  /**
   * Sets true if the rating is to be used as a filter.
   * @param useRating  the new value of the property
   */
  public void setUseRating(boolean useRating) {
    this._useRating = useRating;
  }

  /**
   * Gets the the {@code useRating} property.
   * @return the property, not null
   */
  public final Property<Boolean> useRating() {
    return metaBean().useRating().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets a set of agencies to be used to filter by rating.
   * @return the value of the property, not null
   */
  public Set<String> getPerAgencyRatings() {
    return _perAgencyRatings;
  }

  /**
   * Gets the the {@code perAgencyRatings} property.
   * @return the property, not null
   */
  public final Property<Set<String>> perAgencyRatings() {
    return metaBean().perAgencyRatings().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets true if the rating description is to be used as a filter.
   * @return the value of the property
   */
  public boolean isUseRatingDescription() {
    return _useRatingDescription;
  }

  /**
   * Sets true if the rating description is to be used as a filter.
   * @param useRatingDescription  the new value of the property
   */
  public void setUseRatingDescription(boolean useRatingDescription) {
    this._useRatingDescription = useRatingDescription;
  }

  /**
   * Gets the the {@code useRatingDescription} property.
   * @return the property, not null
   */
  public final Property<Boolean> useRatingDescription() {
    return metaBean().useRatingDescription().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets a set of agencies to be used to filter by rating description.
   * @return the value of the property, not null
   */
  public Set<String> getPerAgencyRatingDescriptions() {
    return _perAgencyRatingDescriptions;
  }

  /**
   * Gets the the {@code perAgencyRatingDescriptions} property.
   * @return the property, not null
   */
  public final Property<Set<String>> perAgencyRatingDescriptions() {
    return metaBean().perAgencyRatingDescriptions().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public LegalEntityCreditRatings clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      LegalEntityCreditRatings other = (LegalEntityCreditRatings) obj;
      return (isUseRating() == other.isUseRating()) &&
          JodaBeanUtils.equal(getPerAgencyRatings(), other.getPerAgencyRatings()) &&
          (isUseRatingDescription() == other.isUseRatingDescription()) &&
          JodaBeanUtils.equal(getPerAgencyRatingDescriptions(), other.getPerAgencyRatingDescriptions());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash += hash * 31 + JodaBeanUtils.hashCode(isUseRating());
    hash += hash * 31 + JodaBeanUtils.hashCode(getPerAgencyRatings());
    hash += hash * 31 + JodaBeanUtils.hashCode(isUseRatingDescription());
    hash += hash * 31 + JodaBeanUtils.hashCode(getPerAgencyRatingDescriptions());
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(160);
    buf.append("LegalEntityCreditRatings{");
    int len = buf.length();
    toString(buf);
    if (buf.length() > len) {
      buf.setLength(buf.length() - 2);
    }
    buf.append('}');
    return buf.toString();
  }

  protected void toString(StringBuilder buf) {
    buf.append("useRating").append('=').append(JodaBeanUtils.toString(isUseRating())).append(',').append(' ');
    buf.append("perAgencyRatings").append('=').append(JodaBeanUtils.toString(getPerAgencyRatings())).append(',').append(' ');
    buf.append("useRatingDescription").append('=').append(JodaBeanUtils.toString(isUseRatingDescription())).append(',').append(' ');
    buf.append("perAgencyRatingDescriptions").append('=').append(JodaBeanUtils.toString(getPerAgencyRatingDescriptions())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code LegalEntityCreditRatings}.
   */
  public static class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code useRating} property.
     */
    private final MetaProperty<Boolean> _useRating = DirectMetaProperty.ofReadWrite(
        this, "useRating", LegalEntityCreditRatings.class, Boolean.TYPE);
    /**
     * The meta-property for the {@code perAgencyRatings} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<Set<String>> _perAgencyRatings = DirectMetaProperty.ofReadWrite(
        this, "perAgencyRatings", LegalEntityCreditRatings.class, (Class) Set.class);
    /**
     * The meta-property for the {@code useRatingDescription} property.
     */
    private final MetaProperty<Boolean> _useRatingDescription = DirectMetaProperty.ofReadWrite(
        this, "useRatingDescription", LegalEntityCreditRatings.class, Boolean.TYPE);
    /**
     * The meta-property for the {@code perAgencyRatingDescriptions} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<Set<String>> _perAgencyRatingDescriptions = DirectMetaProperty.ofReadWrite(
        this, "perAgencyRatingDescriptions", LegalEntityCreditRatings.class, (Class) Set.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "useRating",
        "perAgencyRatings",
        "useRatingDescription",
        "perAgencyRatingDescriptions");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case -574758396:  // useRating
          return _useRating;
        case 594236564:  // perAgencyRatings
          return _perAgencyRatings;
        case 159817560:  // useRatingDescription
          return _useRatingDescription;
        case -81474218:  // perAgencyRatingDescriptions
          return _perAgencyRatingDescriptions;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends LegalEntityCreditRatings> builder() {
      return new DirectBeanBuilder<LegalEntityCreditRatings>(new LegalEntityCreditRatings());
    }

    @Override
    public Class<? extends LegalEntityCreditRatings> beanType() {
      return LegalEntityCreditRatings.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code useRating} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Boolean> useRating() {
      return _useRating;
    }

    /**
     * The meta-property for the {@code perAgencyRatings} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Set<String>> perAgencyRatings() {
      return _perAgencyRatings;
    }

    /**
     * The meta-property for the {@code useRatingDescription} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Boolean> useRatingDescription() {
      return _useRatingDescription;
    }

    /**
     * The meta-property for the {@code perAgencyRatingDescriptions} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Set<String>> perAgencyRatingDescriptions() {
      return _perAgencyRatingDescriptions;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -574758396:  // useRating
          return ((LegalEntityCreditRatings) bean).isUseRating();
        case 594236564:  // perAgencyRatings
          return ((LegalEntityCreditRatings) bean).getPerAgencyRatings();
        case 159817560:  // useRatingDescription
          return ((LegalEntityCreditRatings) bean).isUseRatingDescription();
        case -81474218:  // perAgencyRatingDescriptions
          return ((LegalEntityCreditRatings) bean).getPerAgencyRatingDescriptions();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -574758396:  // useRating
          ((LegalEntityCreditRatings) bean).setUseRating((Boolean) newValue);
          return;
        case 594236564:  // perAgencyRatings
          ((LegalEntityCreditRatings) bean).setPerAgencyRatings((Set<String>) newValue);
          return;
        case 159817560:  // useRatingDescription
          ((LegalEntityCreditRatings) bean).setUseRatingDescription((Boolean) newValue);
          return;
        case -81474218:  // perAgencyRatingDescriptions
          ((LegalEntityCreditRatings) bean).setPerAgencyRatingDescriptions((Set<String>) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

    @Override
    protected void validate(Bean bean) {
      JodaBeanUtils.notNull(((LegalEntityCreditRatings) bean)._perAgencyRatings, "perAgencyRatings");
      JodaBeanUtils.notNull(((LegalEntityCreditRatings) bean)._perAgencyRatingDescriptions, "perAgencyRatingDescriptions");
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
