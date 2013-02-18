/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.core.obligor.definition;

import com.opengamma.core.obligor.CreditRating;
import com.opengamma.core.obligor.CreditRatingFitch;
import com.opengamma.core.obligor.CreditRatingMoodys;
import com.opengamma.core.obligor.CreditRatingStandardAndPoors;
import com.opengamma.core.obligor.Region;
import com.opengamma.core.obligor.Sector;
import com.opengamma.util.ArgumentChecker;
import org.joda.beans.BeanDefinition;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectBean;
import java.util.Map;
import org.joda.beans.BeanBuilder;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.impl.direct.DirectBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaBean;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

/**
 * Class for defining the characteristics of an obligor in a derivative contract
 * In the credit derivative context obligors can be protection buyers, protection sellers or the reference entity
 * More generally an obligor is someone to whom one has counterparty risk
 */
@BeanDefinition
public class Obligor extends DirectBean {

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // TODO : Need to be able to allow the user to add user-defined fields to the definition of an obligor on an ad-hoc basis (each user will have different ways of representing an obligor)
  // TODO : Should we include the recovery rate model as part of the obligors composition (private final RecoveryRateModel _recoveryRateModel;)?

  // NOTE : There should be no market data within this objects definition (should only have the obligor characteristics)

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Private member variables

  // The obligor identifiers
  @PropertyDefinition(set = "", validate = "notNull")
  private  String _obligorTicker;
  @PropertyDefinition(set = "", validate = "notNull")
  private  String _obligorShortName;
  @PropertyDefinition(set = "", validate = "notNull")
  private  String _obligorREDCode;

  // The obligor credit rating (MarkIt fields)
  @PropertyDefinition(set = "", validate = "notNull")
  private  CreditRating _compositeRating;
  @PropertyDefinition(set = "", validate = "notNull")
  private  CreditRating _impliedRating;

  // The obligor credit rating (Moodys, S&P and Fitch classifications)
  @PropertyDefinition(set = "", validate = "notNull")
  private  CreditRatingMoodys _moodysCreditRating;
  @PropertyDefinition(set = "", validate = "notNull")
  private  CreditRatingStandardAndPoors _standardAndPoorsCreditRating;
  @PropertyDefinition(set = "", validate = "notNull")
  private  CreditRatingFitch _fitchCreditRating;

  // Explicit flag to determine if the obligor has already defaulted prior to the current time
  @PropertyDefinition(set = "", validate = "notNull")
  private  boolean _hasDefaulted;

  // The obligor industrial sector classification
  @PropertyDefinition(set = "", validate = "notNull")
  private  Sector _sector;

  // The regional domicile of the obligor
  @PropertyDefinition(set = "", validate = "notNull")
  private  Region _region;

  // The country of domicile of the obligor
  @PropertyDefinition(set = "", validate = "notNull")
  private  String _country;

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Obligor constructor

  private Obligor(){    
  }
  
  public Obligor(final String obligorTicker,
      final String obligorShortName,
      final String obligorREDCode,
      final CreditRating compositeRating,
      final CreditRating impliedRating,
      final CreditRatingMoodys moodysCreditRating,
      final CreditRatingStandardAndPoors standardAndPoorsCreditRating,
      final CreditRatingFitch fitchCreditRating,
      final boolean hasDefaulted,
      final Sector sector,
      final Region region,
      final String country) {

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Check the validity of the input arguments

    ArgumentChecker.notNull(obligorTicker, "Obligor ticker");
    ArgumentChecker.isFalse(obligorTicker.isEmpty(), "Obligor ticker");

    ArgumentChecker.notNull(obligorShortName, "Obligor short name");
    ArgumentChecker.isFalse(obligorShortName.isEmpty(), "Obligor short name");

    ArgumentChecker.notNull(obligorREDCode, "Obligor RED code");
    ArgumentChecker.isFalse(obligorREDCode.isEmpty(), "Obligor RED code");

    ArgumentChecker.notNull(compositeRating, "Composite rating field is null");
    ArgumentChecker.notNull(impliedRating, "Implied rating field is null");

    ArgumentChecker.notNull(moodysCreditRating, "Moodys credit rating");
    ArgumentChecker.notNull(standardAndPoorsCreditRating, "S&P credit rating");
    ArgumentChecker.notNull(fitchCreditRating, "Fitch credit rating");

    ArgumentChecker.notNull(sector, "Sector field");
    ArgumentChecker.notNull(region, "Region field");

    ArgumentChecker.notNull(country, "Country field");
    ArgumentChecker.isFalse(country.isEmpty(), "Country field");

    // Assign the member variables for the obligor object

    _obligorTicker = obligorTicker;
    _obligorShortName = obligorShortName;
    _obligorREDCode = obligorREDCode;

    _compositeRating = compositeRating;
    _impliedRating = impliedRating;

    _moodysCreditRating = moodysCreditRating;
    _standardAndPoorsCreditRating = standardAndPoorsCreditRating;
    _fitchCreditRating = fitchCreditRating;

    _hasDefaulted = hasDefaulted;

    _sector = sector;
    _region = region;
    _country = country;

  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code Obligor}.
   * @return the meta-bean, not null
   */
  public static Obligor.Meta meta() {
    return Obligor.Meta.INSTANCE;
  }
  static {
    JodaBeanUtils.registerMetaBean(Obligor.Meta.INSTANCE);
  }

  @Override
  public Obligor.Meta metaBean() {
    return Obligor.Meta.INSTANCE;
  }

  @Override
  protected Object propertyGet(String propertyName, boolean quiet) {
    switch (propertyName.hashCode()) {
      case 896190372:  // obligorTicker
        return getObligorTicker();
      case -1066272179:  // obligorShortName
        return getObligorShortName();
      case -823370556:  // obligorREDCode
        return getObligorREDCode();
      case 957861636:  // compositeRating
        return getCompositeRating();
      case 1421672549:  // impliedRating
        return getImpliedRating();
      case 1016935655:  // moodysCreditRating
        return getMoodysCreditRating();
      case 1963211373:  // standardAndPoorsCreditRating
        return getStandardAndPoorsCreditRating();
      case 1612838220:  // fitchCreditRating
        return getFitchCreditRating();
      case 1706701094:  // hasDefaulted
        return isHasDefaulted();
      case -906274970:  // sector
        return getSector();
      case -934795532:  // region
        return getRegion();
      case 957831062:  // country
        return getCountry();
    }
    return super.propertyGet(propertyName, quiet);
  }

  @Override
  protected void propertySet(String propertyName, Object newValue, boolean quiet) {
    switch (propertyName.hashCode()) {
      case 896190372:  // obligorTicker
        if (quiet) {
          return;
        }
        throw new UnsupportedOperationException("Property cannot be written: obligorTicker");
      case -1066272179:  // obligorShortName
        if (quiet) {
          return;
        }
        throw new UnsupportedOperationException("Property cannot be written: obligorShortName");
      case -823370556:  // obligorREDCode
        if (quiet) {
          return;
        }
        throw new UnsupportedOperationException("Property cannot be written: obligorREDCode");
      case 957861636:  // compositeRating
        if (quiet) {
          return;
        }
        throw new UnsupportedOperationException("Property cannot be written: compositeRating");
      case 1421672549:  // impliedRating
        if (quiet) {
          return;
        }
        throw new UnsupportedOperationException("Property cannot be written: impliedRating");
      case 1016935655:  // moodysCreditRating
        if (quiet) {
          return;
        }
        throw new UnsupportedOperationException("Property cannot be written: moodysCreditRating");
      case 1963211373:  // standardAndPoorsCreditRating
        if (quiet) {
          return;
        }
        throw new UnsupportedOperationException("Property cannot be written: standardAndPoorsCreditRating");
      case 1612838220:  // fitchCreditRating
        if (quiet) {
          return;
        }
        throw new UnsupportedOperationException("Property cannot be written: fitchCreditRating");
      case 1706701094:  // hasDefaulted
        if (quiet) {
          return;
        }
        throw new UnsupportedOperationException("Property cannot be written: hasDefaulted");
      case -906274970:  // sector
        if (quiet) {
          return;
        }
        throw new UnsupportedOperationException("Property cannot be written: sector");
      case -934795532:  // region
        if (quiet) {
          return;
        }
        throw new UnsupportedOperationException("Property cannot be written: region");
      case 957831062:  // country
        if (quiet) {
          return;
        }
        throw new UnsupportedOperationException("Property cannot be written: country");
    }
    super.propertySet(propertyName, newValue, quiet);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      Obligor other = (Obligor) obj;
      return JodaBeanUtils.equal(getObligorTicker(), other.getObligorTicker()) &&
          JodaBeanUtils.equal(getObligorShortName(), other.getObligorShortName()) &&
          JodaBeanUtils.equal(getObligorREDCode(), other.getObligorREDCode()) &&
          JodaBeanUtils.equal(getCompositeRating(), other.getCompositeRating()) &&
          JodaBeanUtils.equal(getImpliedRating(), other.getImpliedRating()) &&
          JodaBeanUtils.equal(getMoodysCreditRating(), other.getMoodysCreditRating()) &&
          JodaBeanUtils.equal(getStandardAndPoorsCreditRating(), other.getStandardAndPoorsCreditRating()) &&
          JodaBeanUtils.equal(getFitchCreditRating(), other.getFitchCreditRating()) &&
          JodaBeanUtils.equal(isHasDefaulted(), other.isHasDefaulted()) &&
          JodaBeanUtils.equal(getSector(), other.getSector()) &&
          JodaBeanUtils.equal(getRegion(), other.getRegion()) &&
          JodaBeanUtils.equal(getCountry(), other.getCountry());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash += hash * 31 + JodaBeanUtils.hashCode(getObligorTicker());
    hash += hash * 31 + JodaBeanUtils.hashCode(getObligorShortName());
    hash += hash * 31 + JodaBeanUtils.hashCode(getObligorREDCode());
    hash += hash * 31 + JodaBeanUtils.hashCode(getCompositeRating());
    hash += hash * 31 + JodaBeanUtils.hashCode(getImpliedRating());
    hash += hash * 31 + JodaBeanUtils.hashCode(getMoodysCreditRating());
    hash += hash * 31 + JodaBeanUtils.hashCode(getStandardAndPoorsCreditRating());
    hash += hash * 31 + JodaBeanUtils.hashCode(getFitchCreditRating());
    hash += hash * 31 + JodaBeanUtils.hashCode(isHasDefaulted());
    hash += hash * 31 + JodaBeanUtils.hashCode(getSector());
    hash += hash * 31 + JodaBeanUtils.hashCode(getRegion());
    hash += hash * 31 + JodaBeanUtils.hashCode(getCountry());
    return hash;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the obligorTicker.
   * @return the value of the property
   */
  public String getObligorTicker() {
    return _obligorTicker;
  }

  /**
   * Gets the the {@code obligorTicker} property.
   * @return the property, not null
   */
  public final Property<String> obligorTicker() {
    return metaBean().obligorTicker().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the obligorShortName.
   * @return the value of the property
   */
  public String getObligorShortName() {
    return _obligorShortName;
  }

  /**
   * Gets the the {@code obligorShortName} property.
   * @return the property, not null
   */
  public final Property<String> obligorShortName() {
    return metaBean().obligorShortName().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the obligorREDCode.
   * @return the value of the property
   */
  public String getObligorREDCode() {
    return _obligorREDCode;
  }

  /**
   * Gets the the {@code obligorREDCode} property.
   * @return the property, not null
   */
  public final Property<String> obligorREDCode() {
    return metaBean().obligorREDCode().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the compositeRating.
   * @return the value of the property
   */
  public CreditRating getCompositeRating() {
    return _compositeRating;
  }

  /**
   * Gets the the {@code compositeRating} property.
   * @return the property, not null
   */
  public final Property<CreditRating> compositeRating() {
    return metaBean().compositeRating().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the impliedRating.
   * @return the value of the property
   */
  public CreditRating getImpliedRating() {
    return _impliedRating;
  }

  /**
   * Gets the the {@code impliedRating} property.
   * @return the property, not null
   */
  public final Property<CreditRating> impliedRating() {
    return metaBean().impliedRating().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the moodysCreditRating.
   * @return the value of the property
   */
  public CreditRatingMoodys getMoodysCreditRating() {
    return _moodysCreditRating;
  }

  /**
   * Gets the the {@code moodysCreditRating} property.
   * @return the property, not null
   */
  public final Property<CreditRatingMoodys> moodysCreditRating() {
    return metaBean().moodysCreditRating().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the standardAndPoorsCreditRating.
   * @return the value of the property
   */
  public CreditRatingStandardAndPoors getStandardAndPoorsCreditRating() {
    return _standardAndPoorsCreditRating;
  }

  /**
   * Gets the the {@code standardAndPoorsCreditRating} property.
   * @return the property, not null
   */
  public final Property<CreditRatingStandardAndPoors> standardAndPoorsCreditRating() {
    return metaBean().standardAndPoorsCreditRating().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the fitchCreditRating.
   * @return the value of the property
   */
  public CreditRatingFitch getFitchCreditRating() {
    return _fitchCreditRating;
  }

  /**
   * Gets the the {@code fitchCreditRating} property.
   * @return the property, not null
   */
  public final Property<CreditRatingFitch> fitchCreditRating() {
    return metaBean().fitchCreditRating().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the hasDefaulted.
   * @return the value of the property
   */
  public boolean isHasDefaulted() {
    return _hasDefaulted;
  }

  /**
   * Gets the the {@code hasDefaulted} property.
   * @return the property, not null
   */
  public final Property<Boolean> hasDefaulted() {
    return metaBean().hasDefaulted().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the sector.
   * @return the value of the property
   */
  public Sector getSector() {
    return _sector;
  }

  /**
   * Gets the the {@code sector} property.
   * @return the property, not null
   */
  public final Property<Sector> sector() {
    return metaBean().sector().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the region.
   * @return the value of the property
   */
  public Region getRegion() {
    return _region;
  }

  /**
   * Gets the the {@code region} property.
   * @return the property, not null
   */
  public final Property<Region> region() {
    return metaBean().region().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the country.
   * @return the value of the property
   */
  public String getCountry() {
    return _country;
  }

  /**
   * Gets the the {@code country} property.
   * @return the property, not null
   */
  public final Property<String> country() {
    return metaBean().country().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code Obligor}.
   */
  public static class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code obligorTicker} property.
     */
    private final MetaProperty<String> _obligorTicker = DirectMetaProperty.ofReadOnly(
        this, "obligorTicker", Obligor.class, String.class);
    /**
     * The meta-property for the {@code obligorShortName} property.
     */
    private final MetaProperty<String> _obligorShortName = DirectMetaProperty.ofReadOnly(
        this, "obligorShortName", Obligor.class, String.class);
    /**
     * The meta-property for the {@code obligorREDCode} property.
     */
    private final MetaProperty<String> _obligorREDCode = DirectMetaProperty.ofReadOnly(
        this, "obligorREDCode", Obligor.class, String.class);
    /**
     * The meta-property for the {@code compositeRating} property.
     */
    private final MetaProperty<CreditRating> _compositeRating = DirectMetaProperty.ofReadOnly(
        this, "compositeRating", Obligor.class, CreditRating.class);
    /**
     * The meta-property for the {@code impliedRating} property.
     */
    private final MetaProperty<CreditRating> _impliedRating = DirectMetaProperty.ofReadOnly(
        this, "impliedRating", Obligor.class, CreditRating.class);
    /**
     * The meta-property for the {@code moodysCreditRating} property.
     */
    private final MetaProperty<CreditRatingMoodys> _moodysCreditRating = DirectMetaProperty.ofReadOnly(
        this, "moodysCreditRating", Obligor.class, CreditRatingMoodys.class);
    /**
     * The meta-property for the {@code standardAndPoorsCreditRating} property.
     */
    private final MetaProperty<CreditRatingStandardAndPoors> _standardAndPoorsCreditRating = DirectMetaProperty.ofReadOnly(
        this, "standardAndPoorsCreditRating", Obligor.class, CreditRatingStandardAndPoors.class);
    /**
     * The meta-property for the {@code fitchCreditRating} property.
     */
    private final MetaProperty<CreditRatingFitch> _fitchCreditRating = DirectMetaProperty.ofReadOnly(
        this, "fitchCreditRating", Obligor.class, CreditRatingFitch.class);
    /**
     * The meta-property for the {@code hasDefaulted} property.
     */
    private final MetaProperty<Boolean> _hasDefaulted = DirectMetaProperty.ofReadOnly(
        this, "hasDefaulted", Obligor.class, Boolean.TYPE);
    /**
     * The meta-property for the {@code sector} property.
     */
    private final MetaProperty<Sector> _sector = DirectMetaProperty.ofReadOnly(
        this, "sector", Obligor.class, Sector.class);
    /**
     * The meta-property for the {@code region} property.
     */
    private final MetaProperty<Region> _region = DirectMetaProperty.ofReadOnly(
        this, "region", Obligor.class, Region.class);
    /**
     * The meta-property for the {@code country} property.
     */
    private final MetaProperty<String> _country = DirectMetaProperty.ofReadOnly(
        this, "country", Obligor.class, String.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "obligorTicker",
        "obligorShortName",
        "obligorREDCode",
        "compositeRating",
        "impliedRating",
        "moodysCreditRating",
        "standardAndPoorsCreditRating",
        "fitchCreditRating",
        "hasDefaulted",
        "sector",
        "region",
        "country");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case 896190372:  // obligorTicker
          return _obligorTicker;
        case -1066272179:  // obligorShortName
          return _obligorShortName;
        case -823370556:  // obligorREDCode
          return _obligorREDCode;
        case 957861636:  // compositeRating
          return _compositeRating;
        case 1421672549:  // impliedRating
          return _impliedRating;
        case 1016935655:  // moodysCreditRating
          return _moodysCreditRating;
        case 1963211373:  // standardAndPoorsCreditRating
          return _standardAndPoorsCreditRating;
        case 1612838220:  // fitchCreditRating
          return _fitchCreditRating;
        case 1706701094:  // hasDefaulted
          return _hasDefaulted;
        case -906274970:  // sector
          return _sector;
        case -934795532:  // region
          return _region;
        case 957831062:  // country
          return _country;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends Obligor> builder() {
      return new DirectBeanBuilder<Obligor>(new Obligor());
    }

    @Override
    public Class<? extends Obligor> beanType() {
      return Obligor.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code obligorTicker} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> obligorTicker() {
      return _obligorTicker;
    }

    /**
     * The meta-property for the {@code obligorShortName} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> obligorShortName() {
      return _obligorShortName;
    }

    /**
     * The meta-property for the {@code obligorREDCode} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> obligorREDCode() {
      return _obligorREDCode;
    }

    /**
     * The meta-property for the {@code compositeRating} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<CreditRating> compositeRating() {
      return _compositeRating;
    }

    /**
     * The meta-property for the {@code impliedRating} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<CreditRating> impliedRating() {
      return _impliedRating;
    }

    /**
     * The meta-property for the {@code moodysCreditRating} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<CreditRatingMoodys> moodysCreditRating() {
      return _moodysCreditRating;
    }

    /**
     * The meta-property for the {@code standardAndPoorsCreditRating} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<CreditRatingStandardAndPoors> standardAndPoorsCreditRating() {
      return _standardAndPoorsCreditRating;
    }

    /**
     * The meta-property for the {@code fitchCreditRating} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<CreditRatingFitch> fitchCreditRating() {
      return _fitchCreditRating;
    }

    /**
     * The meta-property for the {@code hasDefaulted} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Boolean> hasDefaulted() {
      return _hasDefaulted;
    }

    /**
     * The meta-property for the {@code sector} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Sector> sector() {
      return _sector;
    }

    /**
     * The meta-property for the {@code region} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Region> region() {
      return _region;
    }

    /**
     * The meta-property for the {@code country} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> country() {
      return _country;
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
