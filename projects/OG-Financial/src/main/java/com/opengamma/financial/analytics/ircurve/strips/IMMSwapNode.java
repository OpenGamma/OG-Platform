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
   * The pay leg convention.
   */
  @PropertyDefinition(validate = "notNull")
  private ExternalId _payLegConvention;

  /**
   * The receive leg convention.
   */
  @PropertyDefinition(validate = "notNull")
  private ExternalId _receiveLegConvention;

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
   * @param immDateStartNumber The IMM date start number, not negative or zero
   * @param immDateEndNumber The IMM date end number, not negative or zero
   * @param payLegConvention The pay leg convention, not null
   * @param receiveLegConvention The receive leg convention, not null
   * @param curveNodeIdMapperName The curve node id mapper name, not null
   */
  public IMMSwapNode(final Tenor startTenor, final int immDateStartNumber, final int immDateEndNumber, final ExternalId payLegConvention,
      final ExternalId receiveLegConvention, final String curveNodeIdMapperName) {
    super(curveNodeIdMapperName);
    ArgumentChecker.notNegativeOrZero(immDateStartNumber, "IMM date start number");
    ArgumentChecker.notNegativeOrZero(immDateEndNumber, "IMM date end number");
    setStartTenor(startTenor);
    setImmDateStartNumber(immDateStartNumber);
    setImmDateEndNumber(immDateEndNumber);
    setPayLegConvention(payLegConvention);
    setReceiveLegConvention(receiveLegConvention);
    setUseFixings(true);
  }

  /**
   * Sets the node name to null
   * @param startTenor The start tenor, not null
   * @param immDateStartNumber The IMM date start number, not negative or zero
   * @param immDateEndNumber The IMM date end number, not negative or zero
   * @param payLegConvention The pay leg convention, not null
   * @param receiveLegConvention The receive leg convention, not null
   * @param useFixings Use fixings
   * @param curveNodeIdMapperName The curve node id mapper name
   */
  public IMMSwapNode(final Tenor startTenor, final int immDateStartNumber, final int immDateEndNumber, final ExternalId payLegConvention,
      final ExternalId receiveLegConvention, final boolean useFixings, final String curveNodeIdMapperName) {
    super(curveNodeIdMapperName);
    ArgumentChecker.notNegativeOrZero(immDateStartNumber, "IMM date start number");
    ArgumentChecker.notNegativeOrZero(immDateEndNumber, "IMM date end number");
    setStartTenor(startTenor);
    setImmDateStartNumber(immDateStartNumber);
    setImmDateEndNumber(immDateEndNumber);
    setPayLegConvention(payLegConvention);
    setReceiveLegConvention(receiveLegConvention);
    setUseFixings(useFixings);
  }

  /**
   * Sets the useFixings field to true and the node name to null
   * @param startTenor The start tenor, not null
   * @param immDateStartNumber The IMM date start number, not negative or zero
   * @param immDateEndNumber The IMM date end number, not negative or zero
   * @param payLegConvention The pay leg convention, not null
   * @param receiveLegConvention The receive leg convention, not null
   * @param curveNodeIdMapperName The curve node id mapper name, not null
   * @param name The curve node name
   */
  public IMMSwapNode(final Tenor startTenor, final int immDateStartNumber, final int immDateEndNumber, final ExternalId payLegConvention,
      final ExternalId receiveLegConvention, final String curveNodeIdMapperName, final String name) {
    super(curveNodeIdMapperName, name);
    ArgumentChecker.notNegativeOrZero(immDateStartNumber, "IMM date start number");
    ArgumentChecker.notNegativeOrZero(immDateEndNumber, "IMM date end number");
    setStartTenor(startTenor);
    setImmDateStartNumber(immDateStartNumber);
    setImmDateEndNumber(immDateEndNumber);
    setPayLegConvention(payLegConvention);
    setReceiveLegConvention(receiveLegConvention);
    setUseFixings(true);
  }

  /**
   * Sets the node name to null
   * @param startTenor The start tenor, not null
   * @param immDateStartNumber The IMM date start number, not negative or zero
   * @param immDateEndNumber The IMM date end number, not negative or zero
   * @param payLegConvention The pay leg convention, not null
   * @param receiveLegConvention The receive leg convention, not null
   * @param useFixings Use fixings
   * @param curveNodeIdMapperName The curve node id mapper name
   * @param name The curve node name
   */
  public IMMSwapNode(final Tenor startTenor, final int immDateStartNumber, final int immDateEndNumber, final ExternalId payLegConvention,
      final ExternalId receiveLegConvention, final boolean useFixings, final String curveNodeIdMapperName, final String name) {
    super(curveNodeIdMapperName, name);
    ArgumentChecker.notNegativeOrZero(immDateStartNumber, "IMM date start number");
    ArgumentChecker.notNegativeOrZero(immDateEndNumber, "IMM date end number");
    setStartTenor(startTenor);
    setImmDateStartNumber(immDateStartNumber);
    setImmDateEndNumber(immDateEndNumber);
    setPayLegConvention(payLegConvention);
    setReceiveLegConvention(receiveLegConvention);
    setUseFixings(useFixings);
  }

  @Override
  public Tenor getResolvedMaturity() {
    return Tenor.of(getStartTenor().getPeriod().plusMonths(_immDateEndNumber * 3));
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
   * Gets the pay leg convention.
   * @return the value of the property, not null
   */
  public ExternalId getPayLegConvention() {
    return _payLegConvention;
  }

  /**
   * Sets the pay leg convention.
   * @param payLegConvention  the new value of the property, not null
   */
  public void setPayLegConvention(ExternalId payLegConvention) {
    JodaBeanUtils.notNull(payLegConvention, "payLegConvention");
    this._payLegConvention = payLegConvention;
  }

  /**
   * Gets the the {@code payLegConvention} property.
   * @return the property, not null
   */
  public final Property<ExternalId> payLegConvention() {
    return metaBean().payLegConvention().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the receive leg convention.
   * @return the value of the property, not null
   */
  public ExternalId getReceiveLegConvention() {
    return _receiveLegConvention;
  }

  /**
   * Sets the receive leg convention.
   * @param receiveLegConvention  the new value of the property, not null
   */
  public void setReceiveLegConvention(ExternalId receiveLegConvention) {
    JodaBeanUtils.notNull(receiveLegConvention, "receiveLegConvention");
    this._receiveLegConvention = receiveLegConvention;
  }

  /**
   * Gets the the {@code receiveLegConvention} property.
   * @return the property, not null
   */
  public final Property<ExternalId> receiveLegConvention() {
    return metaBean().receiveLegConvention().createProperty(this);
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
          (getImmDateStartNumber() == other.getImmDateStartNumber()) &&
          (getImmDateEndNumber() == other.getImmDateEndNumber()) &&
          JodaBeanUtils.equal(getPayLegConvention(), other.getPayLegConvention()) &&
          JodaBeanUtils.equal(getReceiveLegConvention(), other.getReceiveLegConvention()) &&
          (isUseFixings() == other.isUseFixings()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash += hash * 31 + JodaBeanUtils.hashCode(getStartTenor());
    hash += hash * 31 + JodaBeanUtils.hashCode(getImmDateStartNumber());
    hash += hash * 31 + JodaBeanUtils.hashCode(getImmDateEndNumber());
    hash += hash * 31 + JodaBeanUtils.hashCode(getPayLegConvention());
    hash += hash * 31 + JodaBeanUtils.hashCode(getReceiveLegConvention());
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
    buf.append("startTenor").append('=').append(getStartTenor()).append(',').append(' ');
    buf.append("immDateStartNumber").append('=').append(getImmDateStartNumber()).append(',').append(' ');
    buf.append("immDateEndNumber").append('=').append(getImmDateEndNumber()).append(',').append(' ');
    buf.append("payLegConvention").append('=').append(getPayLegConvention()).append(',').append(' ');
    buf.append("receiveLegConvention").append('=').append(getReceiveLegConvention()).append(',').append(' ');
    buf.append("useFixings").append('=').append(isUseFixings()).append(',').append(' ');
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
     * The meta-property for the {@code payLegConvention} property.
     */
    private final MetaProperty<ExternalId> _payLegConvention = DirectMetaProperty.ofReadWrite(
        this, "payLegConvention", IMMSwapNode.class, ExternalId.class);
    /**
     * The meta-property for the {@code receiveLegConvention} property.
     */
    private final MetaProperty<ExternalId> _receiveLegConvention = DirectMetaProperty.ofReadWrite(
        this, "receiveLegConvention", IMMSwapNode.class, ExternalId.class);
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
        "immDateStartNumber",
        "immDateEndNumber",
        "payLegConvention",
        "receiveLegConvention",
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
        case 2126343860:  // immDateStartNumber
          return _immDateStartNumber;
        case -548980051:  // immDateEndNumber
          return _immDateEndNumber;
        case 774631511:  // payLegConvention
          return _payLegConvention;
        case -560732676:  // receiveLegConvention
          return _receiveLegConvention;
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
     * The meta-property for the {@code payLegConvention} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ExternalId> payLegConvention() {
      return _payLegConvention;
    }

    /**
     * The meta-property for the {@code receiveLegConvention} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ExternalId> receiveLegConvention() {
      return _receiveLegConvention;
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
        case 2126343860:  // immDateStartNumber
          return ((IMMSwapNode) bean).getImmDateStartNumber();
        case -548980051:  // immDateEndNumber
          return ((IMMSwapNode) bean).getImmDateEndNumber();
        case 774631511:  // payLegConvention
          return ((IMMSwapNode) bean).getPayLegConvention();
        case -560732676:  // receiveLegConvention
          return ((IMMSwapNode) bean).getReceiveLegConvention();
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
        case 2126343860:  // immDateStartNumber
          ((IMMSwapNode) bean).setImmDateStartNumber((Integer) newValue);
          return;
        case -548980051:  // immDateEndNumber
          ((IMMSwapNode) bean).setImmDateEndNumber((Integer) newValue);
          return;
        case 774631511:  // payLegConvention
          ((IMMSwapNode) bean).setPayLegConvention((ExternalId) newValue);
          return;
        case -560732676:  // receiveLegConvention
          ((IMMSwapNode) bean).setReceiveLegConvention((ExternalId) newValue);
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
      JodaBeanUtils.notNull(((IMMSwapNode) bean)._immDateStartNumber, "immDateStartNumber");
      JodaBeanUtils.notNull(((IMMSwapNode) bean)._immDateEndNumber, "immDateEndNumber");
      JodaBeanUtils.notNull(((IMMSwapNode) bean)._payLegConvention, "payLegConvention");
      JodaBeanUtils.notNull(((IMMSwapNode) bean)._receiveLegConvention, "receiveLegConvention");
      JodaBeanUtils.notNull(((IMMSwapNode) bean)._useFixings, "useFixings");
      super.validate(bean);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
