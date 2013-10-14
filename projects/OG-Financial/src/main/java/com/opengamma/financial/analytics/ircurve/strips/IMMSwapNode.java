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
 * An IMM swap curve node.
 */
@BeanDefinition
public class IMMSwapNode extends CurveNode {

  /** Serialization version */
  private static final long serialVersionUID = 1L;

  /**
   * The start tenor.
   */
  @PropertyDefinition(validate = "notNull")
  private Tenor _startTenor;

  /**
   * The IMM tenor.
   */
  @PropertyDefinition(validate = "notNull")
  private Tenor _immTenor;

  /**
   * The IMM date start number.
   */
  @PropertyDefinition(validate = "notNull")
  private int _immDateStartNumber;

  /**
   * The IMM date end number.
   */
  @PropertyDefinition(validate = "notNull")
  private int _immDateEndNumber;

  /**
   * The swap convention.
   */
  @PropertyDefinition(validate = "notNull")
  private ExternalId _swapConvention;

  /**
   * Whether to use fixings when constructing the swap
   */
  @PropertyDefinition(validate = "notNull")
  private boolean _useFixings;

  /**
   * For the builder.
   */
  /* package */ IMMSwapNode() {
    super();
  }

  /**
   * Sets the useFixings field to true and the node name to null
   * @param startTenor The start tenor, not null
   * @param immTenor The IMM tenor, not null
   * @param immDateStartNumber The IMM date start number, not negative or zero
   * @param immDateEndNumber The IMM date end number, not negative or zero
   * @param swapConvention The swap convention, not null
   * @param curveNodeIdMapperName The curve node id mapper name, not null
   */
  public IMMSwapNode(final Tenor startTenor, final Tenor immTenor, final int immDateStartNumber, final int immDateEndNumber, final ExternalId swapConvention,
      final String curveNodeIdMapperName) {
    super(curveNodeIdMapperName);
    ArgumentChecker.notNegativeOrZero(immDateStartNumber, "IMM date start number");
    ArgumentChecker.notNegativeOrZero(immDateEndNumber, "IMM date end number");
    setStartTenor(startTenor);
    setImmTenor(immTenor);
    setImmDateStartNumber(immDateStartNumber);
    setImmDateEndNumber(immDateEndNumber);
    setSwapConvention(swapConvention);
    setUseFixings(true);
  }

  /**
   * Sets the node name to null
   * @param startTenor The start tenor, not null
   * @param immTenor The IMM tenor, not null
   * @param immDateStartNumber The IMM date start number, not negative or zero
   * @param immDateEndNumber The IMM date end number, not negative or zero
   * @param swapConvention The swap convention, not null
   * @param useFixings Use fixings
   * @param curveNodeIdMapperName The curve node id mapper name
   */
  public IMMSwapNode(final Tenor startTenor, final Tenor immTenor, final int immDateStartNumber, final int immDateEndNumber, final ExternalId swapConvention,
      final boolean useFixings, final String curveNodeIdMapperName) {
    super(curveNodeIdMapperName);
    ArgumentChecker.notNegativeOrZero(immDateStartNumber, "IMM date start number");
    ArgumentChecker.notNegativeOrZero(immDateEndNumber, "IMM date end number");
    setStartTenor(startTenor);
    setImmTenor(immTenor);
    setImmDateStartNumber(immDateStartNumber);
    setImmDateEndNumber(immDateEndNumber);
    setSwapConvention(swapConvention);
    setUseFixings(useFixings);
  }

  /**
   * Sets the useFixings field to true and the node name to null
   * @param startTenor The start tenor, not null
   * @param immTenor The IMM tenor, not null
   * @param immDateStartNumber The IMM date start number, not negative or zero
   * @param immDateEndNumber The IMM date end number, not negative or zero
   * @param swapConvention The swap convention, not null
   * @param curveNodeIdMapperName The curve node id mapper name, not null
   * @param name The curve node name
   */
  public IMMSwapNode(final Tenor startTenor, final Tenor immTenor, final int immDateStartNumber, final int immDateEndNumber, final ExternalId swapConvention,
      final String curveNodeIdMapperName, final String name) {
    super(curveNodeIdMapperName, name);
    ArgumentChecker.notNegativeOrZero(immDateStartNumber, "IMM date start number");
    ArgumentChecker.notNegativeOrZero(immDateEndNumber, "IMM date end number");
    setStartTenor(startTenor);
    setImmTenor(immTenor);
    setImmDateStartNumber(immDateStartNumber);
    setImmDateEndNumber(immDateEndNumber);
    setSwapConvention(swapConvention);
    setUseFixings(true);
  }

  /**
   * Sets the node name to null
   * @param startTenor The start tenor, not null
   * @param immTenor The IMM tenor, not null
   * @param immDateStartNumber The IMM date start number, not negative or zero
   * @param immDateEndNumber The IMM date end number, not negative or zero
   * @param swapConvention The swap convention, not null
   * @param useFixings Use fixings
   * @param curveNodeIdMapperName The curve node id mapper name
   * @param name The curve node name
   */
  public IMMSwapNode(final Tenor startTenor, final Tenor immTenor, final int immDateStartNumber, final int immDateEndNumber, final ExternalId swapConvention,
      final boolean useFixings, final String curveNodeIdMapperName, final String name) {
    super(curveNodeIdMapperName, name);
    ArgumentChecker.notNegativeOrZero(immDateStartNumber, "IMM date start number");
    ArgumentChecker.notNegativeOrZero(immDateEndNumber, "IMM date end number");
    setStartTenor(startTenor);
    setImmTenor(immTenor);
    setImmDateStartNumber(immDateStartNumber);
    setImmDateEndNumber(immDateEndNumber);
    setSwapConvention(swapConvention);
    setUseFixings(useFixings);
  }

  @Override
  public Tenor getResolvedMaturity() {
    final int m = getImmTenor().getPeriod().getMonths();
    return Tenor.of(getStartTenor().getPeriod().plusMonths(m * getImmDateEndNumber()));
  }

  @Override
  public <T> T accept(final CurveNodeVisitor<T> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitIMMSwapNode(this);
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code IMMSwapNode}.
   * @return the meta-bean, not null
   */
  public static IMMSwapNode.Meta meta() {
    return IMMSwapNode.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(IMMSwapNode.Meta.INSTANCE);
  }

  @Override
  public IMMSwapNode.Meta metaBean() {
    return IMMSwapNode.Meta.INSTANCE;
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
   * Gets the IMM tenor.
   * @return the value of the property, not null
   */
  public Tenor getImmTenor() {
    return _immTenor;
  }

  /**
   * Sets the IMM tenor.
   * @param immTenor  the new value of the property, not null
   */
  public void setImmTenor(Tenor immTenor) {
    JodaBeanUtils.notNull(immTenor, "immTenor");
    this._immTenor = immTenor;
  }

  /**
   * Gets the the {@code immTenor} property.
   * @return the property, not null
   */
  public final Property<Tenor> immTenor() {
    return metaBean().immTenor().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the IMM date start number.
   * @return the value of the property, not null
   */
  public int getImmDateStartNumber() {
    return _immDateStartNumber;
  }

  /**
   * Sets the IMM date start number.
   * @param immDateStartNumber  the new value of the property, not null
   */
  public void setImmDateStartNumber(int immDateStartNumber) {
    JodaBeanUtils.notNull(immDateStartNumber, "immDateStartNumber");
    this._immDateStartNumber = immDateStartNumber;
  }

  /**
   * Gets the the {@code immDateStartNumber} property.
   * @return the property, not null
   */
  public final Property<Integer> immDateStartNumber() {
    return metaBean().immDateStartNumber().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the IMM date end number.
   * @return the value of the property, not null
   */
  public int getImmDateEndNumber() {
    return _immDateEndNumber;
  }

  /**
   * Sets the IMM date end number.
   * @param immDateEndNumber  the new value of the property, not null
   */
  public void setImmDateEndNumber(int immDateEndNumber) {
    JodaBeanUtils.notNull(immDateEndNumber, "immDateEndNumber");
    this._immDateEndNumber = immDateEndNumber;
  }

  /**
   * Gets the the {@code immDateEndNumber} property.
   * @return the property, not null
   */
  public final Property<Integer> immDateEndNumber() {
    return metaBean().immDateEndNumber().createProperty(this);
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
   * Gets whether to use fixings when constructing the swap
   * @return the value of the property, not null
   */
  public boolean isUseFixings() {
    return _useFixings;
  }

  /**
   * Sets whether to use fixings when constructing the swap
   * @param useFixings  the new value of the property, not null
   */
  public void setUseFixings(boolean useFixings) {
    JodaBeanUtils.notNull(useFixings, "useFixings");
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
  public IMMSwapNode clone() {
    return (IMMSwapNode) super.clone();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      IMMSwapNode other = (IMMSwapNode) obj;
      return JodaBeanUtils.equal(getStartTenor(), other.getStartTenor()) &&
          JodaBeanUtils.equal(getImmTenor(), other.getImmTenor()) &&
          (getImmDateStartNumber() == other.getImmDateStartNumber()) &&
          (getImmDateEndNumber() == other.getImmDateEndNumber()) &&
          JodaBeanUtils.equal(getSwapConvention(), other.getSwapConvention()) &&
          (isUseFixings() == other.isUseFixings()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash += hash * 31 + JodaBeanUtils.hashCode(getStartTenor());
    hash += hash * 31 + JodaBeanUtils.hashCode(getImmTenor());
    hash += hash * 31 + JodaBeanUtils.hashCode(getImmDateStartNumber());
    hash += hash * 31 + JodaBeanUtils.hashCode(getImmDateEndNumber());
    hash += hash * 31 + JodaBeanUtils.hashCode(getSwapConvention());
    hash += hash * 31 + JodaBeanUtils.hashCode(isUseFixings());
    return hash ^ super.hashCode();
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(224);
    buf.append("IMMSwapNode{");
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
    buf.append("startTenor").append('=').append(JodaBeanUtils.toString(getStartTenor())).append(',').append(' ');
    buf.append("immTenor").append('=').append(JodaBeanUtils.toString(getImmTenor())).append(',').append(' ');
    buf.append("immDateStartNumber").append('=').append(JodaBeanUtils.toString(getImmDateStartNumber())).append(',').append(' ');
    buf.append("immDateEndNumber").append('=').append(JodaBeanUtils.toString(getImmDateEndNumber())).append(',').append(' ');
    buf.append("swapConvention").append('=').append(JodaBeanUtils.toString(getSwapConvention())).append(',').append(' ');
    buf.append("useFixings").append('=').append(JodaBeanUtils.toString(isUseFixings())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code IMMSwapNode}.
   */
  public static class Meta extends CurveNode.Meta {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code startTenor} property.
     */
    private final MetaProperty<Tenor> _startTenor = DirectMetaProperty.ofReadWrite(
        this, "startTenor", IMMSwapNode.class, Tenor.class);
    /**
     * The meta-property for the {@code immTenor} property.
     */
    private final MetaProperty<Tenor> _immTenor = DirectMetaProperty.ofReadWrite(
        this, "immTenor", IMMSwapNode.class, Tenor.class);
    /**
     * The meta-property for the {@code immDateStartNumber} property.
     */
    private final MetaProperty<Integer> _immDateStartNumber = DirectMetaProperty.ofReadWrite(
        this, "immDateStartNumber", IMMSwapNode.class, Integer.TYPE);
    /**
     * The meta-property for the {@code immDateEndNumber} property.
     */
    private final MetaProperty<Integer> _immDateEndNumber = DirectMetaProperty.ofReadWrite(
        this, "immDateEndNumber", IMMSwapNode.class, Integer.TYPE);
    /**
     * The meta-property for the {@code swapConvention} property.
     */
    private final MetaProperty<ExternalId> _swapConvention = DirectMetaProperty.ofReadWrite(
        this, "swapConvention", IMMSwapNode.class, ExternalId.class);
    /**
     * The meta-property for the {@code useFixings} property.
     */
    private final MetaProperty<Boolean> _useFixings = DirectMetaProperty.ofReadWrite(
        this, "useFixings", IMMSwapNode.class, Boolean.TYPE);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "startTenor",
        "immTenor",
        "immDateStartNumber",
        "immDateEndNumber",
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
        case -1583746178:  // startTenor
          return _startTenor;
        case -533583753:  // immTenor
          return _immTenor;
        case 2126343860:  // immDateStartNumber
          return _immDateStartNumber;
        case -548980051:  // immDateEndNumber
          return _immDateEndNumber;
        case 1414180196:  // swapConvention
          return _swapConvention;
        case 1829944031:  // useFixings
          return _useFixings;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends IMMSwapNode> builder() {
      return new DirectBeanBuilder<IMMSwapNode>(new IMMSwapNode());
    }

    @Override
    public Class<? extends IMMSwapNode> beanType() {
      return IMMSwapNode.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code startTenor} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Tenor> startTenor() {
      return _startTenor;
    }

    /**
     * The meta-property for the {@code immTenor} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Tenor> immTenor() {
      return _immTenor;
    }

    /**
     * The meta-property for the {@code immDateStartNumber} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Integer> immDateStartNumber() {
      return _immDateStartNumber;
    }

    /**
     * The meta-property for the {@code immDateEndNumber} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Integer> immDateEndNumber() {
      return _immDateEndNumber;
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
        case -1583746178:  // startTenor
          return ((IMMSwapNode) bean).getStartTenor();
        case -533583753:  // immTenor
          return ((IMMSwapNode) bean).getImmTenor();
        case 2126343860:  // immDateStartNumber
          return ((IMMSwapNode) bean).getImmDateStartNumber();
        case -548980051:  // immDateEndNumber
          return ((IMMSwapNode) bean).getImmDateEndNumber();
        case 1414180196:  // swapConvention
          return ((IMMSwapNode) bean).getSwapConvention();
        case 1829944031:  // useFixings
          return ((IMMSwapNode) bean).isUseFixings();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -1583746178:  // startTenor
          ((IMMSwapNode) bean).setStartTenor((Tenor) newValue);
          return;
        case -533583753:  // immTenor
          ((IMMSwapNode) bean).setImmTenor((Tenor) newValue);
          return;
        case 2126343860:  // immDateStartNumber
          ((IMMSwapNode) bean).setImmDateStartNumber((Integer) newValue);
          return;
        case -548980051:  // immDateEndNumber
          ((IMMSwapNode) bean).setImmDateEndNumber((Integer) newValue);
          return;
        case 1414180196:  // swapConvention
          ((IMMSwapNode) bean).setSwapConvention((ExternalId) newValue);
          return;
        case 1829944031:  // useFixings
          ((IMMSwapNode) bean).setUseFixings((Boolean) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

    @Override
    protected void validate(Bean bean) {
      JodaBeanUtils.notNull(((IMMSwapNode) bean)._startTenor, "startTenor");
      JodaBeanUtils.notNull(((IMMSwapNode) bean)._immTenor, "immTenor");
      JodaBeanUtils.notNull(((IMMSwapNode) bean)._immDateStartNumber, "immDateStartNumber");
      JodaBeanUtils.notNull(((IMMSwapNode) bean)._immDateEndNumber, "immDateEndNumber");
      JodaBeanUtils.notNull(((IMMSwapNode) bean)._swapConvention, "swapConvention");
      JodaBeanUtils.notNull(((IMMSwapNode) bean)._useFixings, "useFixings");
      super.validate(bean);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
