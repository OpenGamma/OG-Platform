/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve.strips;

import java.util.Map;

import org.joda.beans.Bean;
import org.joda.beans.BeanBuilder;
import org.joda.beans.BeanDefinition;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;
import org.threeten.bp.LocalDate;

import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.time.Tenor;

/**
 * A swap curve node that uses calendar dates to set the start and maturity dates.
 */
@BeanDefinition
public class CalendarSwapNode extends CurveNode {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * The calendar external id.
   */
  @PropertyDefinition(validate = "notNull")
  private ExternalId _calendarId;

  /**
   * The date from which to start counting.
   */
  @PropertyDefinition(validate = "notNull")
  private LocalDate _initialDate;

  /**
   * The start calendar date number.
   */
  @PropertyDefinition
  private int _startDateNumber;

  /**
   * The maturity calendar date number.
   */
  @PropertyDefinition
  private int _maturityDateNumber;

  /**
   * The swap convention.
   */
  @PropertyDefinition(validate = "notNull")
  private ExternalId _swapConvention;

  /**
   * Whether to use fixings when constructing the swap.
   */
  @PropertyDefinition
  private boolean _useFixings;

  /**
   * For the builder.
   */
  /* package */CalendarSwapNode() {
    super();
  }

  /**
   * Sets the useFixings parameter to true.
   * @param calendarId The calendar id, not null
   * @param initialDate The date from which to start counting, not null
   * @param startDateNumber The start date number, greater than zero
   * @param maturityDateNumber The maturity date number, greater than the start date number
   * @param swapConvention The swap convention, not null
   * @param curveNodeIdMapperName The curve node id mapper name, not null
   */
  public CalendarSwapNode(final ExternalId calendarId, final LocalDate initialDate, final int startDateNumber, final int maturityDateNumber, final ExternalId swapConvention,
      final String curveNodeIdMapperName) {
    super(curveNodeIdMapperName);
    ArgumentChecker.notNegativeOrZero(startDateNumber, "start date number");
    ArgumentChecker.isTrue(maturityDateNumber > startDateNumber, "Maturity date number {} must be greater than the start date number {}", maturityDateNumber, startDateNumber);
    setCalendarId(calendarId);
    setStartDateNumber(startDateNumber);
    setMaturityDateNumber(maturityDateNumber);
    setSwapConvention(swapConvention);
    setUseFixings(true);
  }

  /**
   * @param calendarId The calendar id, not null
   * @param initialDate The date from which to start counting, not null
   * @param startDateNumber The start date number, greater than zero
   * @param maturityDateNumber The maturity date number, greater than the start date number
   * @param swapConvention The swap convention, not null
   * @param useFixings The use fixings parameter
   * @param curveNodeIdMapperName The curve node id mapper name, not null
   */
  public CalendarSwapNode(final ExternalId calendarId, final LocalDate initialDate, final int startDateNumber, final int maturityDateNumber, final ExternalId swapConvention,
      final boolean useFixings, final String curveNodeIdMapperName) {
    super(curveNodeIdMapperName);
    ArgumentChecker.notNegativeOrZero(startDateNumber, "start date number");
    ArgumentChecker.isTrue(maturityDateNumber > startDateNumber, "Maturity date number {} must be greater than the start date number {}", maturityDateNumber, startDateNumber);
    setCalendarId(calendarId);
    setStartDateNumber(startDateNumber);
    setMaturityDateNumber(maturityDateNumber);
    setSwapConvention(swapConvention);
    setUseFixings(useFixings);
  }

  /**
   * Sets the useFixings parameter to true.
   * @param calendarId The calendar id, not null
   * @param initialDate The date from which to start counting, not null
   * @param startDateNumber The start date number, greater than zero
   * @param maturityDateNumber The maturity date number, greater than the start date number
   * @param swapConvention The swap convention, not null
   * @param curveNodeIdMapperName The curve node id mapper name, not null
   * @param name The curve node name
   */
  public CalendarSwapNode(final ExternalId calendarId, final LocalDate initialDate, final int startDateNumber, final int maturityDateNumber, final ExternalId swapConvention,
      final String curveNodeIdMapperName, final String name) {
    super(curveNodeIdMapperName, name);
    ArgumentChecker.notNegativeOrZero(startDateNumber, "start date number");
    ArgumentChecker.isTrue(maturityDateNumber > startDateNumber, "Maturity date number {} must be greater than the start date number {}", maturityDateNumber, startDateNumber);
    setCalendarId(calendarId);
    setStartDateNumber(startDateNumber);
    setMaturityDateNumber(maturityDateNumber);
    setSwapConvention(swapConvention);
    setUseFixings(true);
  }

  /**
   * @param calendarId The calendar id, not null
   * @param initialDate The date from which to start counting, not null
   * @param startDateNumber The start date number, greater than zero
   * @param maturityDateNumber The maturity date number, greater than the start date number
   * @param swapConvention The swap convention, not null
   * @param useFixings The use fixings parameter
   * @param curveNodeIdMapperName The curve node id mapper name, not null
   * @param name The curve node name
   */
  public CalendarSwapNode(final ExternalId calendarId, final LocalDate initialDate, final int startDateNumber, final int maturityDateNumber, final ExternalId swapConvention,
      final boolean useFixings, final String curveNodeIdMapperName, final String name) {
    super(curveNodeIdMapperName, name);
    ArgumentChecker.notNegativeOrZero(startDateNumber, "start date number");
    ArgumentChecker.isTrue(maturityDateNumber > startDateNumber, "Maturity date number {} must be greater than the start date number {}", maturityDateNumber, startDateNumber);
    setCalendarId(calendarId);
    setStartDateNumber(startDateNumber);
    setMaturityDateNumber(maturityDateNumber);
    setSwapConvention(swapConvention);
    setUseFixings(useFixings);
  }

  @Override
  public Tenor getResolvedMaturity() {
    return null;
  }

  @Override
  public <T> T accept(final CurveNodeVisitor<T> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return null; //visitor.visitCalendarSwapNode(this);
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code CalendarSwapNode}.
   * @return the meta-bean, not null
   */
  public static CalendarSwapNode.Meta meta() {
    return CalendarSwapNode.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(CalendarSwapNode.Meta.INSTANCE);
  }

  @Override
  public CalendarSwapNode.Meta metaBean() {
    return CalendarSwapNode.Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the calendar external id.
   * @return the value of the property, not null
   */
  public ExternalId getCalendarId() {
    return _calendarId;
  }

  /**
   * Sets the calendar external id.
   * @param calendarId  the new value of the property, not null
   */
  public void setCalendarId(ExternalId calendarId) {
    JodaBeanUtils.notNull(calendarId, "calendarId");
    this._calendarId = calendarId;
  }

  /**
   * Gets the the {@code calendarId} property.
   * @return the property, not null
   */
  public final Property<ExternalId> calendarId() {
    return metaBean().calendarId().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the date from which to start counting.
   * @return the value of the property, not null
   */
  public LocalDate getInitialDate() {
    return _initialDate;
  }

  /**
   * Sets the date from which to start counting.
   * @param initialDate  the new value of the property, not null
   */
  public void setInitialDate(LocalDate initialDate) {
    JodaBeanUtils.notNull(initialDate, "initialDate");
    this._initialDate = initialDate;
  }

  /**
   * Gets the the {@code initialDate} property.
   * @return the property, not null
   */
  public final Property<LocalDate> initialDate() {
    return metaBean().initialDate().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the start calendar date number.
   * @return the value of the property
   */
  public int getStartDateNumber() {
    return _startDateNumber;
  }

  /**
   * Sets the start calendar date number.
   * @param startDateNumber  the new value of the property
   */
  public void setStartDateNumber(int startDateNumber) {
    this._startDateNumber = startDateNumber;
  }

  /**
   * Gets the the {@code startDateNumber} property.
   * @return the property, not null
   */
  public final Property<Integer> startDateNumber() {
    return metaBean().startDateNumber().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the maturity calendar date number.
   * @return the value of the property
   */
  public int getMaturityDateNumber() {
    return _maturityDateNumber;
  }

  /**
   * Sets the maturity calendar date number.
   * @param maturityDateNumber  the new value of the property
   */
  public void setMaturityDateNumber(int maturityDateNumber) {
    this._maturityDateNumber = maturityDateNumber;
  }

  /**
   * Gets the the {@code maturityDateNumber} property.
   * @return the property, not null
   */
  public final Property<Integer> maturityDateNumber() {
    return metaBean().maturityDateNumber().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the swap convention.
   * @return the value of the property, not null
   */
  public ExternalId getSwapConvention() {
    return _swapConvention;
  }

  /**
   * Sets the swap convention.
   * @param swapConvention  the new value of the property, not null
   */
  public void setSwapConvention(ExternalId swapConvention) {
    JodaBeanUtils.notNull(swapConvention, "swapConvention");
    this._swapConvention = swapConvention;
  }

  /**
   * Gets the the {@code swapConvention} property.
   * @return the property, not null
   */
  public final Property<ExternalId> swapConvention() {
    return metaBean().swapConvention().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets whether to use fixings when constructing the swap.
   * @return the value of the property
   */
  public boolean isUseFixings() {
    return _useFixings;
  }

  /**
   * Sets whether to use fixings when constructing the swap.
   * @param useFixings  the new value of the property
   */
  public void setUseFixings(boolean useFixings) {
    this._useFixings = useFixings;
  }

  /**
   * Gets the the {@code useFixings} property.
   * @return the property, not null
   */
  public final Property<Boolean> useFixings() {
    return metaBean().useFixings().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public CalendarSwapNode clone() {
    return (CalendarSwapNode) super.clone();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      CalendarSwapNode other = (CalendarSwapNode) obj;
      return JodaBeanUtils.equal(getCalendarId(), other.getCalendarId()) &&
          JodaBeanUtils.equal(getInitialDate(), other.getInitialDate()) &&
          (getStartDateNumber() == other.getStartDateNumber()) &&
          (getMaturityDateNumber() == other.getMaturityDateNumber()) &&
          JodaBeanUtils.equal(getSwapConvention(), other.getSwapConvention()) &&
          (isUseFixings() == other.isUseFixings()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash += hash * 31 + JodaBeanUtils.hashCode(getCalendarId());
    hash += hash * 31 + JodaBeanUtils.hashCode(getInitialDate());
    hash += hash * 31 + JodaBeanUtils.hashCode(getStartDateNumber());
    hash += hash * 31 + JodaBeanUtils.hashCode(getMaturityDateNumber());
    hash += hash * 31 + JodaBeanUtils.hashCode(getSwapConvention());
    hash += hash * 31 + JodaBeanUtils.hashCode(isUseFixings());
    return hash ^ super.hashCode();
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(224);
    buf.append("CalendarSwapNode{");
    int len = buf.length();
    toString(buf);
    if (buf.length() > len) {
      buf.setLength(buf.length() - 2);
    }
    buf.append('}');
    return buf.toString();
  }

  @Override
  protected void toString(StringBuilder buf) {
    super.toString(buf);
    buf.append("calendarId").append('=').append(JodaBeanUtils.toString(getCalendarId())).append(',').append(' ');
    buf.append("initialDate").append('=').append(JodaBeanUtils.toString(getInitialDate())).append(',').append(' ');
    buf.append("startDateNumber").append('=').append(JodaBeanUtils.toString(getStartDateNumber())).append(',').append(' ');
    buf.append("maturityDateNumber").append('=').append(JodaBeanUtils.toString(getMaturityDateNumber())).append(',').append(' ');
    buf.append("swapConvention").append('=').append(JodaBeanUtils.toString(getSwapConvention())).append(',').append(' ');
    buf.append("useFixings").append('=').append(JodaBeanUtils.toString(isUseFixings())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code CalendarSwapNode}.
   */
  public static class Meta extends CurveNode.Meta {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code calendarId} property.
     */
    private final MetaProperty<ExternalId> _calendarId = DirectMetaProperty.ofReadWrite(
        this, "calendarId", CalendarSwapNode.class, ExternalId.class);
    /**
     * The meta-property for the {@code initialDate} property.
     */
    private final MetaProperty<LocalDate> _initialDate = DirectMetaProperty.ofReadWrite(
        this, "initialDate", CalendarSwapNode.class, LocalDate.class);
    /**
     * The meta-property for the {@code startDateNumber} property.
     */
    private final MetaProperty<Integer> _startDateNumber = DirectMetaProperty.ofReadWrite(
        this, "startDateNumber", CalendarSwapNode.class, Integer.TYPE);
    /**
     * The meta-property for the {@code maturityDateNumber} property.
     */
    private final MetaProperty<Integer> _maturityDateNumber = DirectMetaProperty.ofReadWrite(
        this, "maturityDateNumber", CalendarSwapNode.class, Integer.TYPE);
    /**
     * The meta-property for the {@code swapConvention} property.
     */
    private final MetaProperty<ExternalId> _swapConvention = DirectMetaProperty.ofReadWrite(
        this, "swapConvention", CalendarSwapNode.class, ExternalId.class);
    /**
     * The meta-property for the {@code useFixings} property.
     */
    private final MetaProperty<Boolean> _useFixings = DirectMetaProperty.ofReadWrite(
        this, "useFixings", CalendarSwapNode.class, Boolean.TYPE);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "calendarId",
        "initialDate",
        "startDateNumber",
        "maturityDateNumber",
        "swapConvention",
        "useFixings");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case 428682489:  // calendarId
          return _calendarId;
        case 1232894226:  // initialDate
          return _initialDate;
        case -2005022055:  // startDateNumber
          return _startDateNumber;
        case 1560213256:  // maturityDateNumber
          return _maturityDateNumber;
        case 1414180196:  // swapConvention
          return _swapConvention;
        case 1829944031:  // useFixings
          return _useFixings;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends CalendarSwapNode> builder() {
      return new DirectBeanBuilder<CalendarSwapNode>(new CalendarSwapNode());
    }

    @Override
    public Class<? extends CalendarSwapNode> beanType() {
      return CalendarSwapNode.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code calendarId} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ExternalId> calendarId() {
      return _calendarId;
    }

    /**
     * The meta-property for the {@code initialDate} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<LocalDate> initialDate() {
      return _initialDate;
    }

    /**
     * The meta-property for the {@code startDateNumber} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Integer> startDateNumber() {
      return _startDateNumber;
    }

    /**
     * The meta-property for the {@code maturityDateNumber} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Integer> maturityDateNumber() {
      return _maturityDateNumber;
    }

    /**
     * The meta-property for the {@code swapConvention} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ExternalId> swapConvention() {
      return _swapConvention;
    }

    /**
     * The meta-property for the {@code useFixings} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Boolean> useFixings() {
      return _useFixings;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 428682489:  // calendarId
          return ((CalendarSwapNode) bean).getCalendarId();
        case 1232894226:  // initialDate
          return ((CalendarSwapNode) bean).getInitialDate();
        case -2005022055:  // startDateNumber
          return ((CalendarSwapNode) bean).getStartDateNumber();
        case 1560213256:  // maturityDateNumber
          return ((CalendarSwapNode) bean).getMaturityDateNumber();
        case 1414180196:  // swapConvention
          return ((CalendarSwapNode) bean).getSwapConvention();
        case 1829944031:  // useFixings
          return ((CalendarSwapNode) bean).isUseFixings();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 428682489:  // calendarId
          ((CalendarSwapNode) bean).setCalendarId((ExternalId) newValue);
          return;
        case 1232894226:  // initialDate
          ((CalendarSwapNode) bean).setInitialDate((LocalDate) newValue);
          return;
        case -2005022055:  // startDateNumber
          ((CalendarSwapNode) bean).setStartDateNumber((Integer) newValue);
          return;
        case 1560213256:  // maturityDateNumber
          ((CalendarSwapNode) bean).setMaturityDateNumber((Integer) newValue);
          return;
        case 1414180196:  // swapConvention
          ((CalendarSwapNode) bean).setSwapConvention((ExternalId) newValue);
          return;
        case 1829944031:  // useFixings
          ((CalendarSwapNode) bean).setUseFixings((Boolean) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

    @Override
    protected void validate(Bean bean) {
      JodaBeanUtils.notNull(((CalendarSwapNode) bean)._calendarId, "calendarId");
      JodaBeanUtils.notNull(((CalendarSwapNode) bean)._initialDate, "initialDate");
      JodaBeanUtils.notNull(((CalendarSwapNode) bean)._swapConvention, "swapConvention");
      super.validate(bean);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
