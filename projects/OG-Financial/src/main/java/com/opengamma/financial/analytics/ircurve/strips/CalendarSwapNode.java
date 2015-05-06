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

import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.time.Tenor;

/**
 * A swap curve node that uses calendar dates to set the start and maturity dates.
 * By calendar we mean a list of dates, e.g. ECB dates stored in a {@see com.opengamma.core.DateSet}
 * configuration object this is unrelated to the holiday calendar implementation.
 */
@BeanDefinition
public class CalendarSwapNode extends CurveNode {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * The set of dates (i.e. calendar) name, not null. Used to determine swap start and end dates using the offsets below.
   * Refers to the name of the {@see com.opengamma.core.DateSet} configuration object holding the dates.
   */
  @PropertyDefinition(validate = "notNull")
  private String _dateSetName;
  
  /**
   * The start tenor.
   */
  @PropertyDefinition(validate = "notNull")
  private Tenor _startTenor;

  /**
   * The calendar date number for the swap start (effective) date.
   */
  @PropertyDefinition
  private int _startDateNumber;

  /**
   * The calendar date number for the swap end (maturity) date.
   */
  @PropertyDefinition
  private int _endDateNumber;

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
   * @param dateSetName The calendar name, not null
   * @param startTenor The start tenor, not null
   * @param startDateNumber The start date number, greater than zero
   * @param endDateNumber The maturity date number, greater than the start date number
   * @param swapConvention The swap convention, not null
   * @param curveNodeIdMapperName The curve node id mapper name, not null
   */
  public CalendarSwapNode(final String dateSetName, final Tenor startTenor, final int startDateNumber, final int endDateNumber, final ExternalId swapConvention,
      final String curveNodeIdMapperName) {
    super(curveNodeIdMapperName);
    ArgumentChecker.notNegativeOrZero(startDateNumber, "start date number");
    ArgumentChecker.isTrue(endDateNumber > startDateNumber, "Maturity date number {} must be greater than the start date number {}",
                           endDateNumber,
                           startDateNumber);
    setDateSetName(dateSetName);
    setStartTenor(startTenor);
    setStartDateNumber(startDateNumber);
    setEndDateNumber(endDateNumber);
    setSwapConvention(swapConvention);
    setUseFixings(true);
  }

  /**
   * @param dateSetName The calendar id, not null
   * @param startTenor The start tenor, not null
   * @param startDateNumber The start date number, greater than zero
   * @param endDateNumber The end date number, greater than the start date number
   * @param swapConvention The swap convention, not null
   * @param useFixings The use fixings parameter
   * @param curveNodeIdMapperName The curve node id mapper name, not null
   */
  public CalendarSwapNode(final String dateSetName, final Tenor startTenor, final int startDateNumber, final int endDateNumber, final ExternalId swapConvention,
      final boolean useFixings, final String curveNodeIdMapperName) {
    super(curveNodeIdMapperName);
    ArgumentChecker.notNegativeOrZero(startDateNumber, "start date number");
    ArgumentChecker.isTrue(endDateNumber > startDateNumber, "Maturity date number {} must be greater than the start date number {}",
                           endDateNumber,
                           startDateNumber);
    setDateSetName(dateSetName);
    setStartTenor(startTenor);
    setStartDateNumber(startDateNumber);
    setEndDateNumber(endDateNumber);
    setSwapConvention(swapConvention);
    setUseFixings(useFixings);
  }

  /**
   * Sets the useFixings parameter to true.
   * @param dateSetName The calendar id, not null
   * @param startTenor The start tenor, not null
   * @param startDateNumber The start date number, greater than zero
   * @param endDateNumber The end date number, greater than the start date number
   * @param swapConvention The swap convention, not null
   * @param curveNodeIdMapperName The curve node id mapper name, not null
   * @param name The curve node name
   */
  public CalendarSwapNode(final String dateSetName, final Tenor startTenor, final int startDateNumber, final int endDateNumber, final ExternalId swapConvention,
      final String curveNodeIdMapperName, final String name) {
    super(curveNodeIdMapperName, name);
    ArgumentChecker.notNegativeOrZero(startDateNumber, "start date number");
    ArgumentChecker.isTrue(endDateNumber > startDateNumber, "Maturity date number {} must be greater than the start date number {}",
                           endDateNumber,
                           startDateNumber);
    setDateSetName(dateSetName);
    setStartTenor(startTenor);
    setStartDateNumber(startDateNumber);
    setEndDateNumber(endDateNumber);
    setSwapConvention(swapConvention);
    setUseFixings(true);
  }

  /**
   * @param dateSetName The calendar id, not null
   * @param startTenor The start tenor, not null
   * @param startDateNumber The start date number, greater than zero
   * @param endDateNumber The end date number, greater than the start date number
   * @param swapConvention The swap convention, not null
   * @param useFixings The use fixings parameter
   * @param curveNodeIdMapperName The curve node id mapper name, not null
   * @param name The curve node name
   */
  public CalendarSwapNode(final String dateSetName, final Tenor startTenor, final int startDateNumber, final int endDateNumber, final ExternalId swapConvention,
      final boolean useFixings, final String curveNodeIdMapperName, final String name) {
    super(curveNodeIdMapperName, name);
    ArgumentChecker.notNegativeOrZero(startDateNumber, "start date number");
    ArgumentChecker.isTrue(endDateNumber > startDateNumber, "Maturity date number {} must be greater than the start date number {}",
                           endDateNumber,
                           startDateNumber);
    setDateSetName(dateSetName);
    setStartTenor(startTenor);
    setStartDateNumber(startDateNumber);
    setEndDateNumber(endDateNumber);
    setSwapConvention(swapConvention);
    setUseFixings(useFixings);
  }

  @Override
  public Tenor getResolvedMaturity() {
    final int m = 1; // TODO [PLAT-6313]: Review: How to get the Roll date adjuster period?
    return Tenor.of(getStartTenor().getPeriod().plusMonths(m * getEndDateNumber()));
  }

  @Override
  public <T> T accept(final CurveNodeVisitor<T> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitCalendarSwapNode(this);
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
   * Gets the set of dates (i.e. calendar) name, not null. Used to determine swap start and end dates using the offsets below.
   * Refers to the name of the {@see com.opengamma.core.DateSet} configuration object holding the dates.
   * @return the value of the property, not null
   */
  public String getDateSetName() {
    return _dateSetName;
  }

  /**
   * Sets the set of dates (i.e. calendar) name, not null. Used to determine swap start and end dates using the offsets below.
   * Refers to the name of the {@see com.opengamma.core.DateSet} configuration object holding the dates.
   * @param dateSetName  the new value of the property, not null
   */
  public void setDateSetName(String dateSetName) {
    JodaBeanUtils.notNull(dateSetName, "dateSetName");
    this._dateSetName = dateSetName;
  }

  /**
   * Gets the the {@code dateSetName} property.
   * Refers to the name of the {@see com.opengamma.core.DateSet} configuration object holding the dates.
   * @return the property, not null
   */
  public final Property<String> dateSetName() {
    return metaBean().dateSetName().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the start tenor.
   * @return the value of the property, not null
   */
  public Tenor getStartTenor() {
    return _startTenor;
  }

  /**
   * Sets the start tenor.
   * @param startTenor  the new value of the property, not null
   */
  public void setStartTenor(Tenor startTenor) {
    JodaBeanUtils.notNull(startTenor, "startTenor");
    this._startTenor = startTenor;
  }

  /**
   * Gets the the {@code startTenor} property.
   * @return the property, not null
   */
  public final Property<Tenor> startTenor() {
    return metaBean().startTenor().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the calendar date number for the swap start (effective) date.
   * @return the value of the property
   */
  public int getStartDateNumber() {
    return _startDateNumber;
  }

  /**
   * Sets the calendar date number for the swap start (effective) date.
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
   * Gets the calendar date number for the swap end (maturity) date.
   * @return the value of the property
   */
  public int getEndDateNumber() {
    return _endDateNumber;
  }

  /**
   * Sets the calendar date number for the swap end (maturity) date.
   * @param endDateNumber  the new value of the property
   */
  public void setEndDateNumber(int endDateNumber) {
    this._endDateNumber = endDateNumber;
  }

  /**
   * Gets the the {@code endDateNumber} property.
   * @return the property, not null
   */
  public final Property<Integer> endDateNumber() {
    return metaBean().endDateNumber().createProperty(this);
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
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      CalendarSwapNode other = (CalendarSwapNode) obj;
      return JodaBeanUtils.equal(getDateSetName(), other.getDateSetName()) &&
          JodaBeanUtils.equal(getStartTenor(), other.getStartTenor()) &&
          (getStartDateNumber() == other.getStartDateNumber()) &&
          (getEndDateNumber() == other.getEndDateNumber()) &&
          JodaBeanUtils.equal(getSwapConvention(), other.getSwapConvention()) &&
          (isUseFixings() == other.isUseFixings()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = hash * 31 + JodaBeanUtils.hashCode(getDateSetName());
    hash = hash * 31 + JodaBeanUtils.hashCode(getStartTenor());
    hash = hash * 31 + JodaBeanUtils.hashCode(getStartDateNumber());
    hash = hash * 31 + JodaBeanUtils.hashCode(getEndDateNumber());
    hash = hash * 31 + JodaBeanUtils.hashCode(getSwapConvention());
    hash = hash * 31 + JodaBeanUtils.hashCode(isUseFixings());
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
    buf.append("dateSetName").append('=').append(JodaBeanUtils.toString(getDateSetName())).append(',').append(' ');
    buf.append("startTenor").append('=').append(JodaBeanUtils.toString(getStartTenor())).append(',').append(' ');
    buf.append("startDateNumber").append('=').append(JodaBeanUtils.toString(getStartDateNumber())).append(',').append(' ');
    buf.append("endDateNumber").append('=').append(JodaBeanUtils.toString(getEndDateNumber())).append(',').append(' ');
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
     * The meta-property for the {@code dateSetName} property.
     */
    private final MetaProperty<String> _dateSetName = DirectMetaProperty.ofReadWrite(
        this, "dateSetName", CalendarSwapNode.class, String.class);
    /**
     * The meta-property for the {@code startTenor} property.
     */
    private final MetaProperty<Tenor> _startTenor = DirectMetaProperty.ofReadWrite(
        this, "startTenor", CalendarSwapNode.class, Tenor.class);
    /**
     * The meta-property for the {@code startDateNumber} property.
     */
    private final MetaProperty<Integer> _startDateNumber = DirectMetaProperty.ofReadWrite(
        this, "startDateNumber", CalendarSwapNode.class, Integer.TYPE);
    /**
     * The meta-property for the {@code endDateNumber} property.
     */
    private final MetaProperty<Integer> _endDateNumber = DirectMetaProperty.ofReadWrite(
        this, "endDateNumber", CalendarSwapNode.class, Integer.TYPE);
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
        "dateSetName",
        "startTenor",
        "startDateNumber",
        "endDateNumber",
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
        case -1115098497:  // dateSetName
          return _dateSetName;
        case -1583746178:  // startTenor
          return _startTenor;
        case -2005022055:  // startDateNumber
          return _startDateNumber;
        case 526912466:  // endDateNumber
          return _endDateNumber;
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
     * The meta-property for the {@code dateSetName} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> dateSetName() {
      return _dateSetName;
    }

    /**
     * The meta-property for the {@code startTenor} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Tenor> startTenor() {
      return _startTenor;
    }

    /**
     * The meta-property for the {@code startDateNumber} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Integer> startDateNumber() {
      return _startDateNumber;
    }

    /**
     * The meta-property for the {@code endDateNumber} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Integer> endDateNumber() {
      return _endDateNumber;
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
        case -1115098497:  // dateSetName
          return ((CalendarSwapNode) bean).getDateSetName();
        case -1583746178:  // startTenor
          return ((CalendarSwapNode) bean).getStartTenor();
        case -2005022055:  // startDateNumber
          return ((CalendarSwapNode) bean).getStartDateNumber();
        case 526912466:  // endDateNumber
          return ((CalendarSwapNode) bean).getEndDateNumber();
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
        case -1115098497:  // dateSetName
          ((CalendarSwapNode) bean).setDateSetName((String) newValue);
          return;
        case -1583746178:  // startTenor
          ((CalendarSwapNode) bean).setStartTenor((Tenor) newValue);
          return;
        case -2005022055:  // startDateNumber
          ((CalendarSwapNode) bean).setStartDateNumber((Integer) newValue);
          return;
        case 526912466:  // endDateNumber
          ((CalendarSwapNode) bean).setEndDateNumber((Integer) newValue);
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
      JodaBeanUtils.notNull(((CalendarSwapNode) bean)._dateSetName, "dateSetName");
      JodaBeanUtils.notNull(((CalendarSwapNode) bean)._startTenor, "startTenor");
      JodaBeanUtils.notNull(((CalendarSwapNode) bean)._swapConvention, "swapConvention");
      super.validate(bean);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
