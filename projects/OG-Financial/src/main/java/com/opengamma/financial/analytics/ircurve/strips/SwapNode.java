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
 *
 */
@BeanDefinition
public class SwapNode extends CurveNode {

  /** Serialization version */
  private static final long serialVersionUID = 1L;

  /**
   * The start tenor.
   */
  @PropertyDefinition(validate = "notNull")
  private Tenor _startTenor;

  /**
   * The maturity tenor.
   */
  @PropertyDefinition(validate = "notNull")
  private Tenor _maturityTenor;

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
  @PropertyDefinition
  private boolean _useFixings;

  /**
   * For the builder.
   */
  /* package */SwapNode() {
    super();
  }

  /**
   * @param startTenor The start tenor, not null
   * @param maturityTenor The maturity tenor, not null
   * @param payLegConvention The pay leg convention, not null
   * @param receiveLegConvention The receive leg convention, not null
   * @param curveNodeIdMapperName The curve node id mapper name, not null
   */
  public SwapNode(final Tenor startTenor, final Tenor maturityTenor, final ExternalId payLegConvention, final ExternalId receiveLegConvention,
      final String curveNodeIdMapperName) {
    super(curveNodeIdMapperName);
    setStartTenor(startTenor);
    setMaturityTenor(maturityTenor);
    setPayLegConvention(payLegConvention);
    setReceiveLegConvention(receiveLegConvention);
    setUseFixings(true);
  }

  /**
   * @param startTenor The start tenor, not null
   * @param maturityTenor The maturity tenor, not null
   * @param payLegConvention The pay leg convention, not null
   * @param receiveLegConvention The receive leg convention, not null
   * @param useFixings True if fixings are to be used in curve construction
   * @param curveNodeIdMapperName The curve node id mapper name, not null
   */
  public SwapNode(final Tenor startTenor, final Tenor maturityTenor, final ExternalId payLegConvention, final ExternalId receiveLegConvention,
      final boolean useFixings, final String curveNodeIdMapperName) {
    super(curveNodeIdMapperName);
    setStartTenor(startTenor);
    setMaturityTenor(maturityTenor);
    setPayLegConvention(payLegConvention);
    setReceiveLegConvention(receiveLegConvention);
    setUseFixings(useFixings);
  }

  /**
   * @param startTenor The start tenor, not null
   * @param maturityTenor The maturity tenor, not null
   * @param payLegConvention The pay leg convention, not null
   * @param receiveLegConvention The receive leg convention, not null
   * @param curveNodeIdMapperName The curve node id mapper name, not null
   * @param name The name
   */
  public SwapNode(final Tenor startTenor, final Tenor maturityTenor, final ExternalId payLegConvention, final ExternalId receiveLegConvention,
      final String curveNodeIdMapperName, final String name) {
    super(curveNodeIdMapperName, name);
    setStartTenor(startTenor);
    setMaturityTenor(maturityTenor);
    setPayLegConvention(payLegConvention);
    setReceiveLegConvention(receiveLegConvention);
    setUseFixings(true);
  }

  /**
   * @param startTenor The start tenor, not null
   * @param maturityTenor The maturity tenor, not null
   * @param payLegConvention The pay leg convention, not null
   * @param receiveLegConvention The receive leg convention, not null
   * @param useFixings True if fixings are to be used in curve construction
   * @param curveNodeIdMapperName The curve node id mapper name, not null
   * @param name The name
   */
  public SwapNode(final Tenor startTenor, final Tenor maturityTenor, final ExternalId payLegConvention, final ExternalId receiveLegConvention,
      final boolean useFixings, final String curveNodeIdMapperName, final String name) {
    super(curveNodeIdMapperName, name);
    setStartTenor(startTenor);
    setMaturityTenor(maturityTenor);
    setPayLegConvention(payLegConvention);
    setReceiveLegConvention(receiveLegConvention);
    setUseFixings(useFixings);
  }

  @Override
  public Tenor getResolvedMaturity() {
    return Tenor.of(_startTenor.getPeriod().plus(_maturityTenor.getPeriod()));
  }

  @Override
  public <T> T accept(final CurveNodeVisitor<T> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitSwapNode(this);
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code SwapNode}.
   * @return the meta-bean, not null
   */
  public static SwapNode.Meta meta() {
    return SwapNode.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(SwapNode.Meta.INSTANCE);
  }

  @Override
  public SwapNode.Meta metaBean() {
    return SwapNode.Meta.INSTANCE;
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
   * Gets the maturity tenor.
   * @return the value of the property, not null
   */
  public Tenor getMaturityTenor() {
    return _maturityTenor;
  }

  /**
   * Sets the maturity tenor.
   * @param maturityTenor  the new value of the property, not null
   */
  public void setMaturityTenor(Tenor maturityTenor) {
    JodaBeanUtils.notNull(maturityTenor, "maturityTenor");
    this._maturityTenor = maturityTenor;
  }

  /**
   * Gets the the {@code maturityTenor} property.
   * @return the property, not null
   */
  public final Property<Tenor> maturityTenor() {
    return metaBean().maturityTenor().createProperty(this);
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
   * @return the value of the property
   */
  public boolean isUseFixings() {
    return _useFixings;
  }

  /**
   * Sets whether to use fixings when constructing the swap
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
  public SwapNode clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      SwapNode other = (SwapNode) obj;
      return JodaBeanUtils.equal(getStartTenor(), other.getStartTenor()) &&
          JodaBeanUtils.equal(getMaturityTenor(), other.getMaturityTenor()) &&
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
    hash += hash * 31 + JodaBeanUtils.hashCode(getMaturityTenor());
    hash += hash * 31 + JodaBeanUtils.hashCode(getPayLegConvention());
    hash += hash * 31 + JodaBeanUtils.hashCode(getReceiveLegConvention());
    hash += hash * 31 + JodaBeanUtils.hashCode(isUseFixings());
    return hash ^ super.hashCode();
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(192);
    buf.append("SwapNode{");
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
    buf.append("maturityTenor").append('=').append(JodaBeanUtils.toString(getMaturityTenor())).append(',').append(' ');
    buf.append("payLegConvention").append('=').append(JodaBeanUtils.toString(getPayLegConvention())).append(',').append(' ');
    buf.append("receiveLegConvention").append('=').append(JodaBeanUtils.toString(getReceiveLegConvention())).append(',').append(' ');
    buf.append("useFixings").append('=').append(JodaBeanUtils.toString(isUseFixings())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code SwapNode}.
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
        this, "startTenor", SwapNode.class, Tenor.class);
    /**
     * The meta-property for the {@code maturityTenor} property.
     */
    private final MetaProperty<Tenor> _maturityTenor = DirectMetaProperty.ofReadWrite(
        this, "maturityTenor", SwapNode.class, Tenor.class);
    /**
     * The meta-property for the {@code payLegConvention} property.
     */
    private final MetaProperty<ExternalId> _payLegConvention = DirectMetaProperty.ofReadWrite(
        this, "payLegConvention", SwapNode.class, ExternalId.class);
    /**
     * The meta-property for the {@code receiveLegConvention} property.
     */
    private final MetaProperty<ExternalId> _receiveLegConvention = DirectMetaProperty.ofReadWrite(
        this, "receiveLegConvention", SwapNode.class, ExternalId.class);
    /**
     * The meta-property for the {@code useFixings} property.
     */
    private final MetaProperty<Boolean> _useFixings = DirectMetaProperty.ofReadWrite(
        this, "useFixings", SwapNode.class, Boolean.TYPE);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "startTenor",
        "maturityTenor",
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
        case 45907375:  // maturityTenor
          return _maturityTenor;
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
    public BeanBuilder<? extends SwapNode> builder() {
      return new DirectBeanBuilder<SwapNode>(new SwapNode());
    }

    @Override
    public Class<? extends SwapNode> beanType() {
      return SwapNode.class;
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
     * The meta-property for the {@code maturityTenor} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Tenor> maturityTenor() {
      return _maturityTenor;
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
          return ((SwapNode) bean).getStartTenor();
        case 45907375:  // maturityTenor
          return ((SwapNode) bean).getMaturityTenor();
        case 774631511:  // payLegConvention
          return ((SwapNode) bean).getPayLegConvention();
        case -560732676:  // receiveLegConvention
          return ((SwapNode) bean).getReceiveLegConvention();
        case 1829944031:  // useFixings
          return ((SwapNode) bean).isUseFixings();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -1583746178:  // startTenor
          ((SwapNode) bean).setStartTenor((Tenor) newValue);
          return;
        case 45907375:  // maturityTenor
          ((SwapNode) bean).setMaturityTenor((Tenor) newValue);
          return;
        case 774631511:  // payLegConvention
          ((SwapNode) bean).setPayLegConvention((ExternalId) newValue);
          return;
        case -560732676:  // receiveLegConvention
          ((SwapNode) bean).setReceiveLegConvention((ExternalId) newValue);
          return;
        case 1829944031:  // useFixings
          ((SwapNode) bean).setUseFixings((Boolean) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

    @Override
    protected void validate(Bean bean) {
      JodaBeanUtils.notNull(((SwapNode) bean)._startTenor, "startTenor");
      JodaBeanUtils.notNull(((SwapNode) bean)._maturityTenor, "maturityTenor");
      JodaBeanUtils.notNull(((SwapNode) bean)._payLegConvention, "payLegConvention");
      JodaBeanUtils.notNull(((SwapNode) bean)._receiveLegConvention, "receiveLegConvention");
      super.validate(bean);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
