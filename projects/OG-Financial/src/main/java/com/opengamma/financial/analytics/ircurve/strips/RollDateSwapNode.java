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
public class RollDateSwapNode extends CurveNode {

  /** Serialization version */
  private static final long serialVersionUID = 1L;

  /**
   * The start tenor.
   */
  @PropertyDefinition(validate = "notNull")
  private Tenor _startTenor;

  /**
   * The roll date start number.
   */
  @PropertyDefinition(validate = "notNull")
  private int _rollDateStartNumber;

  /**
   * The roll date end number.
   */
  @PropertyDefinition(validate = "notNull")
  private int _rollDateEndNumber;

  /**
   * The roll date swap convention.
   */
  @PropertyDefinition(validate = "notNull")
  private ExternalId _rollDateSwapConvention;

  /**
   * Whether to use fixings when constructing the swap
   */
  @PropertyDefinition(validate = "notNull")
  private boolean _useFixings;

  /**
   * For the builder.
   */
  RollDateSwapNode() {
    super();
  }

  /**
   * Sets the useFixings field to true and the node name to null
   * @param startTenor The start tenor, not null
   * @param rollDateStartNumber The roll date start number, not negative or zero
   * @param rollDateEndNumber The roll date end number, not negative or zero
   * @param swapConvention The swap convention, not null
   * @param curveNodeIdMapperName The curve node id mapper name, not null
   */
  public RollDateSwapNode(final Tenor startTenor, final int rollDateStartNumber, final int rollDateEndNumber, final ExternalId swapConvention,
      final String curveNodeIdMapperName) {
    super(curveNodeIdMapperName);
    ArgumentChecker.notNegativeOrZero(rollDateStartNumber, "roll date start number");
    ArgumentChecker.notNegativeOrZero(rollDateEndNumber, "roll date end number");
    setStartTenor(startTenor);
    setRollDateStartNumber(rollDateStartNumber);
    setRollDateEndNumber(rollDateEndNumber);
    setRollDateSwapConvention(swapConvention);
    setUseFixings(true);
  }

  /**
   * Sets the node name to null
   * @param startTenor The start tenor, not null
   * @param rollDateStartNumber The IMM date start number, not negative or zero
   * @param rollDateEndNumber The IMM date end number, not negative or zero
   * @param swapConvention The swap convention, not null
   * @param useFixings Use fixings
   * @param curveNodeIdMapperName The curve node id mapper name
   */
  public RollDateSwapNode(final Tenor startTenor, final int rollDateStartNumber, final int rollDateEndNumber, final ExternalId swapConvention,
      final boolean useFixings, final String curveNodeIdMapperName) {
    super(curveNodeIdMapperName);
    ArgumentChecker.notNegativeOrZero(rollDateStartNumber, "roll date start number");
    ArgumentChecker.notNegativeOrZero(rollDateEndNumber, "roll date end number");
    setStartTenor(startTenor);
    setRollDateStartNumber(rollDateStartNumber);
    setRollDateEndNumber(rollDateEndNumber);
    setRollDateSwapConvention(swapConvention);
    setUseFixings(useFixings);
  }

  /**
   * Sets the useFixings field to true and the node name to null
   * @param startTenor The start tenor, not null
   * @param rollDateStartNumber The IMM date start number, not negative or zero
   * @param rollDateEndNumber The IMM date end number, not negative or zero
   * @param swapConvention The swap convention, not null
   * @param curveNodeIdMapperName The curve node id mapper name, not null
   * @param name The curve node name
   */
  public RollDateSwapNode(final Tenor startTenor, final int rollDateStartNumber, final int rollDateEndNumber, final ExternalId swapConvention,
      final String curveNodeIdMapperName, final String name) {
    super(curveNodeIdMapperName, name);
    ArgumentChecker.notNegativeOrZero(rollDateStartNumber, "roll date start number");
    ArgumentChecker.notNegativeOrZero(rollDateEndNumber, "roll date end number");
    setStartTenor(startTenor);
    setRollDateStartNumber(rollDateStartNumber);
    setRollDateEndNumber(rollDateEndNumber);
    setRollDateSwapConvention(swapConvention);
    setUseFixings(true);
  }

  /**
   * Sets the node name to null
   * @param startTenor The start tenor, not null
   * @param rollDateStartNumber The IMM date start number, not negative or zero
   * @param rollDateEndNumber The IMM date end number, not negative or zero
   * @param swapConvention The swap convention, not null
   * @param useFixings Use fixings
   * @param curveNodeIdMapperName The curve node id mapper name
   * @param name The curve node name
   */
  public RollDateSwapNode(final Tenor startTenor, final int rollDateStartNumber, final int rollDateEndNumber, final ExternalId swapConvention,
      final boolean useFixings, final String curveNodeIdMapperName, final String name) {
    super(curveNodeIdMapperName, name);
    ArgumentChecker.notNegativeOrZero(rollDateStartNumber, "roll date start number");
    ArgumentChecker.notNegativeOrZero(rollDateEndNumber, "roll date end number");
    setStartTenor(startTenor);
    setRollDateStartNumber(rollDateStartNumber);
    setRollDateEndNumber(rollDateEndNumber);
    setRollDateSwapConvention(swapConvention);
    setUseFixings(useFixings);
  }

  @Override
  public Tenor getResolvedMaturity() {
    final int m = 3; // TODO [PLAT-6313]: Review: How to get the Roll date adjuster period?
    return Tenor.of(getStartTenor().getPeriod().plusMonths(m * getRollDateEndNumber()));
  }

  @Override
  public <T> T accept(final CurveNodeVisitor<T> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitRollDateSwapNode(this);
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code RollDateSwapNode}.
   * @return the meta-bean, not null
   */
  public static RollDateSwapNode.Meta meta() {
    return RollDateSwapNode.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(RollDateSwapNode.Meta.INSTANCE);
  }

  @Override
  public RollDateSwapNode.Meta metaBean() {
    return RollDateSwapNode.Meta.INSTANCE;
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
   * Gets the roll date start number.
   * @return the value of the property, not null
   */
  public int getRollDateStartNumber() {
    return _rollDateStartNumber;
  }

  /**
   * Sets the roll date start number.
   * @param rollDateStartNumber  the new value of the property, not null
   */
  public void setRollDateStartNumber(int rollDateStartNumber) {
    JodaBeanUtils.notNull(rollDateStartNumber, "rollDateStartNumber");
    this._rollDateStartNumber = rollDateStartNumber;
  }

  /**
   * Gets the the {@code rollDateStartNumber} property.
   * @return the property, not null
   */
  public final Property<Integer> rollDateStartNumber() {
    return metaBean().rollDateStartNumber().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the roll date end number.
   * @return the value of the property, not null
   */
  public int getRollDateEndNumber() {
    return _rollDateEndNumber;
  }

  /**
   * Sets the roll date end number.
   * @param rollDateEndNumber  the new value of the property, not null
   */
  public void setRollDateEndNumber(int rollDateEndNumber) {
    JodaBeanUtils.notNull(rollDateEndNumber, "rollDateEndNumber");
    this._rollDateEndNumber = rollDateEndNumber;
  }

  /**
   * Gets the the {@code rollDateEndNumber} property.
   * @return the property, not null
   */
  public final Property<Integer> rollDateEndNumber() {
    return metaBean().rollDateEndNumber().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the roll date swap convention.
   * @return the value of the property, not null
   */
  public ExternalId getRollDateSwapConvention() {
    return _rollDateSwapConvention;
  }

  /**
   * Sets the roll date swap convention.
   * @param rollDateSwapConvention  the new value of the property, not null
   */
  public void setRollDateSwapConvention(ExternalId rollDateSwapConvention) {
    JodaBeanUtils.notNull(rollDateSwapConvention, "rollDateSwapConvention");
    this._rollDateSwapConvention = rollDateSwapConvention;
  }

  /**
   * Gets the the {@code rollDateSwapConvention} property.
   * @return the property, not null
   */
  public final Property<ExternalId> rollDateSwapConvention() {
    return metaBean().rollDateSwapConvention().createProperty(this);
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
  public RollDateSwapNode clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      RollDateSwapNode other = (RollDateSwapNode) obj;
      return JodaBeanUtils.equal(getStartTenor(), other.getStartTenor()) &&
          (getRollDateStartNumber() == other.getRollDateStartNumber()) &&
          (getRollDateEndNumber() == other.getRollDateEndNumber()) &&
          JodaBeanUtils.equal(getRollDateSwapConvention(), other.getRollDateSwapConvention()) &&
          (isUseFixings() == other.isUseFixings()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = hash * 31 + JodaBeanUtils.hashCode(getStartTenor());
    hash = hash * 31 + JodaBeanUtils.hashCode(getRollDateStartNumber());
    hash = hash * 31 + JodaBeanUtils.hashCode(getRollDateEndNumber());
    hash = hash * 31 + JodaBeanUtils.hashCode(getRollDateSwapConvention());
    hash = hash * 31 + JodaBeanUtils.hashCode(isUseFixings());
    return hash ^ super.hashCode();
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(192);
    buf.append("RollDateSwapNode{");
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
    buf.append("rollDateStartNumber").append('=').append(JodaBeanUtils.toString(getRollDateStartNumber())).append(',').append(' ');
    buf.append("rollDateEndNumber").append('=').append(JodaBeanUtils.toString(getRollDateEndNumber())).append(',').append(' ');
    buf.append("rollDateSwapConvention").append('=').append(JodaBeanUtils.toString(getRollDateSwapConvention())).append(',').append(' ');
    buf.append("useFixings").append('=').append(JodaBeanUtils.toString(isUseFixings())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code RollDateSwapNode}.
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
        this, "startTenor", RollDateSwapNode.class, Tenor.class);
    /**
     * The meta-property for the {@code rollDateStartNumber} property.
     */
    private final MetaProperty<Integer> _rollDateStartNumber = DirectMetaProperty.ofReadWrite(
        this, "rollDateStartNumber", RollDateSwapNode.class, Integer.TYPE);
    /**
     * The meta-property for the {@code rollDateEndNumber} property.
     */
    private final MetaProperty<Integer> _rollDateEndNumber = DirectMetaProperty.ofReadWrite(
        this, "rollDateEndNumber", RollDateSwapNode.class, Integer.TYPE);
    /**
     * The meta-property for the {@code rollDateSwapConvention} property.
     */
    private final MetaProperty<ExternalId> _rollDateSwapConvention = DirectMetaProperty.ofReadWrite(
        this, "rollDateSwapConvention", RollDateSwapNode.class, ExternalId.class);
    /**
     * The meta-property for the {@code useFixings} property.
     */
    private final MetaProperty<Boolean> _useFixings = DirectMetaProperty.ofReadWrite(
        this, "useFixings", RollDateSwapNode.class, Boolean.TYPE);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "startTenor",
        "rollDateStartNumber",
        "rollDateEndNumber",
        "rollDateSwapConvention",
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
        case 2110556032:  // rollDateStartNumber
          return _rollDateStartNumber;
        case -660728199:  // rollDateEndNumber
          return _rollDateEndNumber;
        case 1667192847:  // rollDateSwapConvention
          return _rollDateSwapConvention;
        case 1829944031:  // useFixings
          return _useFixings;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends RollDateSwapNode> builder() {
      return new DirectBeanBuilder<RollDateSwapNode>(new RollDateSwapNode());
    }

    @Override
    public Class<? extends RollDateSwapNode> beanType() {
      return RollDateSwapNode.class;
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
     * The meta-property for the {@code rollDateStartNumber} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Integer> rollDateStartNumber() {
      return _rollDateStartNumber;
    }

    /**
     * The meta-property for the {@code rollDateEndNumber} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Integer> rollDateEndNumber() {
      return _rollDateEndNumber;
    }

    /**
     * The meta-property for the {@code rollDateSwapConvention} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ExternalId> rollDateSwapConvention() {
      return _rollDateSwapConvention;
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
          return ((RollDateSwapNode) bean).getStartTenor();
        case 2110556032:  // rollDateStartNumber
          return ((RollDateSwapNode) bean).getRollDateStartNumber();
        case -660728199:  // rollDateEndNumber
          return ((RollDateSwapNode) bean).getRollDateEndNumber();
        case 1667192847:  // rollDateSwapConvention
          return ((RollDateSwapNode) bean).getRollDateSwapConvention();
        case 1829944031:  // useFixings
          return ((RollDateSwapNode) bean).isUseFixings();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -1583746178:  // startTenor
          ((RollDateSwapNode) bean).setStartTenor((Tenor) newValue);
          return;
        case 2110556032:  // rollDateStartNumber
          ((RollDateSwapNode) bean).setRollDateStartNumber((Integer) newValue);
          return;
        case -660728199:  // rollDateEndNumber
          ((RollDateSwapNode) bean).setRollDateEndNumber((Integer) newValue);
          return;
        case 1667192847:  // rollDateSwapConvention
          ((RollDateSwapNode) bean).setRollDateSwapConvention((ExternalId) newValue);
          return;
        case 1829944031:  // useFixings
          ((RollDateSwapNode) bean).setUseFixings((Boolean) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

    @Override
    protected void validate(Bean bean) {
      JodaBeanUtils.notNull(((RollDateSwapNode) bean)._startTenor, "startTenor");
      JodaBeanUtils.notNull(((RollDateSwapNode) bean)._rollDateStartNumber, "rollDateStartNumber");
      JodaBeanUtils.notNull(((RollDateSwapNode) bean)._rollDateEndNumber, "rollDateEndNumber");
      JodaBeanUtils.notNull(((RollDateSwapNode) bean)._rollDateSwapConvention, "rollDateSwapConvention");
      JodaBeanUtils.notNull(((RollDateSwapNode) bean)._useFixings, "useFixings");
      super.validate(bean);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
