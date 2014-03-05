/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import java.io.Serializable;
import java.util.Map;

import org.joda.beans.Bean;
import org.joda.beans.BeanBuilder;
import org.joda.beans.BeanDefinition;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectBean;
import org.joda.beans.impl.direct.DirectBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaBean;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.target.ComputationTargetTypeMap;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.lambdava.functions.Function1;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.PublicAPI;

/**
 * Encapsulates view-level configuration to describe the types of values required in the calculation results. This configuration could lead to fewer calculations taking place by allowing the
 * dependency graphs to be trimmed, although values will still be calculated if they are required as inputs for other calculations.
 * <p>
 * This configuration acts as a filter on the outputs that have been requested through {@link ViewCalculationConfiguration}. In a sense, it is a view-view.
 * <p>
 * This class is a mutable bean.
 */
@PublicAPI
@BeanDefinition
public class ResultModelDefinition extends DirectBean implements Serializable {

  private static final long serialVersionUID = 1L;

  private static ComputationTargetTypeMap<Function1<ResultModelDefinition, ResultOutputMode>> s_getOutputMode = getOutputMode();

  /**
   * The aggregate position output mode (portfolio nodes).
   * <p>
   * For example, the referenced portfolio could have a deep structure with many nodes at which aggregate portfolio outputs would be calculated. If these are not required then disabling them could
   * speed up the computation cycle significantly.
   */
  @PropertyDefinition
  private ResultOutputMode _aggregatePositionOutputMode;
  /**
   * The individual position output mode.
   * <p>
   * If only aggregate position calculations are required, with respect to the hierarchy of the reference portfolio, then disabling outputs for individual positions through this method could speed up
   * the computation cycle significantly. This is beneficial for calculations, such as VaR, which can be performed at the aggregate level without requiring the complete result of the same calculation
   * on its children. Aggregate calculations where this is not the case will be unaffected, although disabling the individual position outputs will still hide them from the user even though they will
   * be calculated.
   */
  @PropertyDefinition
  private ResultOutputMode _positionOutputMode;
  /**
   * The trade output mode.
   * <p>
   * If only aggregate position calculations are required, with respect to the hierarchy of the reference portfolio, then disabling outputs for individual trades through this method could speed up the
   * computation cycle significantly. This is beneficial for calculations, such as VaR, which can be performed at the aggregate level without requiring the complete result of the same calculation on
   * its children. Aggregate calculations where this is not the case will be unaffected, although disabling the individual trade outputs will still hide them from the user even though they will be
   * calculated.
   */
  @PropertyDefinition
  private ResultOutputMode _tradeOutputMode;
  /**
   * The security output mode.
   * <p>
   * These are values which relate generally to a security and apply to every position in that security. For example, market data on a security would be a security output.
   */
  @PropertyDefinition
  private ResultOutputMode _securityOutputMode;
  /**
   * The primitive output mode.
   * <p>
   * These are values which may be used in calculations for many securities. For example, the USD discount curve would be a primitive.
   */
  @PropertyDefinition
  private ResultOutputMode _primitiveOutputMode;

  /**
   * Creates an instance using the default output mode for each computation target type.
   * <p>
   * The {@link ResultOutputMode#TERMINAL_OUTPUTS} mode is used for all target types.
   */
  public ResultModelDefinition() {
    this(ResultOutputMode.TERMINAL_OUTPUTS);
  }

  /**
   * Creates an instance using the specified output mode for every computation target type.
   * 
   * @param defaultMode the default result output mode, not null
   */
  public ResultModelDefinition(ResultOutputMode defaultMode) {
    this(defaultMode, defaultMode, defaultMode, defaultMode, defaultMode);
  }

  /**
   * Creates an instance using the specified output modes for each computation target type.
   * 
   * @param aggregatePositionOutputMode the result output mode for aggregate position targets, not null
   * @param positionOutputMode the result output mode for individual position targets, not null
   * @param tradeOutputMode the result output mode for trade targets, not null
   * @param securityOutputMode the result output mode for security targets, not null
   * @param primitiveOutputMode the result output mode for primitive targets, not null
   */
  public ResultModelDefinition(
      ResultOutputMode aggregatePositionOutputMode, ResultOutputMode positionOutputMode, ResultOutputMode tradeOutputMode,
      ResultOutputMode securityOutputMode, ResultOutputMode primitiveOutputMode) {
    ArgumentChecker.notNull(aggregatePositionOutputMode, "aggregatePositionOutputMode");
    ArgumentChecker.notNull(positionOutputMode, "positionOutputMode");
    ArgumentChecker.notNull(tradeOutputMode, "tradeOutputMode");
    ArgumentChecker.notNull(securityOutputMode, "securityOutputMode");
    ArgumentChecker.notNull(primitiveOutputMode, "primitiveOutputMode");
    _aggregatePositionOutputMode = aggregatePositionOutputMode;
    _positionOutputMode = positionOutputMode;
    _tradeOutputMode = tradeOutputMode;
    _securityOutputMode = securityOutputMode;
    _primitiveOutputMode = primitiveOutputMode;
  }

  //-------------------------------------------------------------------------
  //  /**
  //   * Gets the output mode that applies to aggregate position values. This is independent of individual position outputs.
  //   * 
  //   * @return  the output mode that applies to aggregate position values
  //   */
  //  public ResultOutputMode getAggregatePositionOutputMode() {
  //    return _aggregatePositionOutputMode;
  //  }
  //
  //  /**
  //   * Sets the output mode that applies to aggregate position outputs. For example, the referenced portfolio could have
  //   * a deep structure with many nodes at which aggregate portfolio outputs would be calculated. If these are not
  //   * required then disabling them could speed up the computation cycle significantly.
  //   * 
  //   * @param aggregatePositionOutputMode  the output mode to apply to aggregate position values
  //   */
  //  public void setAggregatePositionOutputMode(ResultOutputMode aggregatePositionOutputMode) {
  //    _aggregatePositionOutputMode = aggregatePositionOutputMode;
  //  }
  //
  //  /**
  //   * Gets the output mode that applies to individual position values. This is independent of aggregate position
  //   * outputs. 
  //   * 
  //   * @return  the output mode that applies to position values
  //   */
  //  public ResultOutputMode getPositionOutputMode() {
  //    return _positionOutputMode;
  //  }
  //  
  //  /**
  //   * Sets the output mode that applies to individual position outputs. If only aggregate position calculations are
  //   * required, with respect to the hierarchy of the reference portfolio, then disabling outputs for individual
  //   * positions through this method could speed up the computation cycle significantly. This is beneficial for
  //   * calculations, such as VaR, which can be performed at the aggregate level without requiring the complete result of
  //   * the same calculation on its children. Aggregate calculations where this is not the case will be unaffected,
  //   * although disabling the individual position outputs will still hide them from the user even though they will be
  //   * calculated.
  //   * 
  //   * @param positionOutputMode  the output mode to apply to position values
  //   */
  //  public void setPositionOutputMode(ResultOutputMode positionOutputMode) {
  //    _positionOutputMode = positionOutputMode;
  //  }
  //  
  //  /**
  //   * Sets the output mode that applies to individual trade outputs. If only aggregate position calculations are
  //   * required, with respect to the hierarchy of the reference portfolio, then disabling outputs for individual
  //   * trades through this method could speed up the computation cycle significantly. This is beneficial for
  //   * calculations, such as VaR, which can be performed at the aggregate level without requiring the complete result of
  //   * the same calculation on its children. Aggregate calculations where this is not the case will be unaffected,
  //   * although disabling the individual trade outputs will still hide them from the user even though they will be
  //   * calculated.
  //   * 
  //   * @param tradeOutputMode  the output mode to apply to trade values
  //   */
  //  public void setTradeOutputMode(ResultOutputMode tradeOutputMode) {
  //    _tradeOutputMode = tradeOutputMode;
  //  }
  //
  //  /**
  //   * Gets the output mode that applies to individual trade values. 
  //   * 
  //   * @return  the output mode that applies to trade values
  //   */
  //  public ResultOutputMode getTradeOutputMode() {
  //    return _tradeOutputMode;
  //  }
  //
  //  /**
  //   * Gets the output mode that applies to security values.
  //   * 
  //   * @return  the output mode that applies to security values
  //   */
  //  public ResultOutputMode getSecurityOutputMode() {
  //    return _securityOutputMode;
  //  }
  //  
  //  /**
  //   * Sets the output mode to apply to security values. These are values which relate generally to a security and apply
  //   *  to every position in that security. For example, market data on a security would be a security output.
  //   * 
  //   * @param securityOutputMode  the output mode to apply to security values
  //   */
  //  public void setSecurityOutputMode(ResultOutputMode securityOutputMode) {
  //    _securityOutputMode = securityOutputMode;
  //  }
  //  
  //  /**
  //   * Gets the output mode that applies to primitive outputs.
  //   * 
  //   * @return  the output mode that applies to primitive values
  //   */
  //  public ResultOutputMode getPrimitiveOutputMode() {
  //    return _primitiveOutputMode;
  //  }
  //  
  //  /**
  //   * Sets the output mode that applies to primitive outputs. These are values which may be used in calculations for
  //   * many securities. For example, the USD discount curve would be a primitive.
  //   * 
  //   * @param primitiveOutputMode  the output mode to apply to primitive values
  //   */
  //  public void setPrimitiveOutputMode(ResultOutputMode primitiveOutputMode) {
  //    _primitiveOutputMode = primitiveOutputMode;
  //  }

  private static ComputationTargetTypeMap<Function1<ResultModelDefinition, ResultOutputMode>> getOutputMode() {
    final ComputationTargetTypeMap<Function1<ResultModelDefinition, ResultOutputMode>> map = new ComputationTargetTypeMap<Function1<ResultModelDefinition, ResultOutputMode>>();
    map.put(ComputationTargetType.ANYTHING, new Function1<ResultModelDefinition, ResultOutputMode>() {
      @Override
      public ResultOutputMode execute(final ResultModelDefinition definition) {
        return definition.getPrimitiveOutputMode();
      }
    });
    map.put(ComputationTargetType.NULL, new Function1<ResultModelDefinition, ResultOutputMode>() {
      @Override
      public ResultOutputMode execute(final ResultModelDefinition definition) {
        return definition.getPrimitiveOutputMode();
      }
    });
    map.put(ComputationTargetType.SECURITY, new Function1<ResultModelDefinition, ResultOutputMode>() {
      @Override
      public ResultOutputMode execute(final ResultModelDefinition definition) {
        return definition.getSecurityOutputMode();
      }
    });
    map.put(ComputationTargetType.POSITION, new Function1<ResultModelDefinition, ResultOutputMode>() {
      @Override
      public ResultOutputMode execute(final ResultModelDefinition definition) {
        return definition.getPositionOutputMode();
      }
    });
    map.put(ComputationTargetType.TRADE, new Function1<ResultModelDefinition, ResultOutputMode>() {
      @Override
      public ResultOutputMode execute(final ResultModelDefinition definition) {
        return definition.getTradeOutputMode();
      }
    });
    map.put(ComputationTargetType.PORTFOLIO_NODE, new Function1<ResultModelDefinition, ResultOutputMode>() {
      @Override
      public ResultOutputMode execute(final ResultModelDefinition definition) {
        return definition.getAggregatePositionOutputMode();
      }
    });
    return map;
  }

  /**
   * Gets the output mode that applies to values of the given computation target type.
   * 
   * @param computationTargetType the target type, not null
   * @return the output mode that applies to values of the give type
   */
  public ResultOutputMode getOutputMode(final ComputationTargetType computationTargetType) {
    ArgumentChecker.notNull(computationTargetType, "computationTargetType");
    final Function1<ResultModelDefinition, ResultOutputMode> operation = s_getOutputMode.get(computationTargetType);
    if (operation != null) {
      return operation.execute(this);
    } else {
      throw new IllegalArgumentException("Unknown target type " + computationTargetType);
    }
  }

  /**
   * Indicates whether an output with the given specification should be included in the results.
   * 
   * @param outputSpecification the specification of the output value, not null
   * @param dependencyGraph the dependency graph to which the output value belongs, not null
   * @return true if the output value should be included in the results
   */
  public boolean shouldOutputResult(ValueSpecification outputSpecification, DependencyGraph dependencyGraph) {
    ArgumentChecker.notNull(outputSpecification, "outputSpecification");
    ArgumentChecker.notNull(dependencyGraph, "dependencyGraph");
    ComputationTargetType targetType = outputSpecification.getTargetSpecification().getType();
    return getOutputMode(targetType).shouldOutputResult(outputSpecification, dependencyGraph);
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code ResultModelDefinition}.
   * @return the meta-bean, not null
   */
  public static ResultModelDefinition.Meta meta() {
    return ResultModelDefinition.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(ResultModelDefinition.Meta.INSTANCE);
  }

  @Override
  public ResultModelDefinition.Meta metaBean() {
    return ResultModelDefinition.Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the aggregate position output mode (portfolio nodes).
   * <p>
   * For example, the referenced portfolio could have a deep structure with many nodes at which aggregate portfolio outputs would be calculated. If these are not required then disabling them could
   * speed up the computation cycle significantly.
   * @return the value of the property
   */
  public ResultOutputMode getAggregatePositionOutputMode() {
    return _aggregatePositionOutputMode;
  }

  /**
   * Sets the aggregate position output mode (portfolio nodes).
   * <p>
   * For example, the referenced portfolio could have a deep structure with many nodes at which aggregate portfolio outputs would be calculated. If these are not required then disabling them could
   * speed up the computation cycle significantly.
   * @param aggregatePositionOutputMode  the new value of the property
   */
  public void setAggregatePositionOutputMode(ResultOutputMode aggregatePositionOutputMode) {
    this._aggregatePositionOutputMode = aggregatePositionOutputMode;
  }

  /**
   * Gets the the {@code aggregatePositionOutputMode} property.
   * <p>
   * For example, the referenced portfolio could have a deep structure with many nodes at which aggregate portfolio outputs would be calculated. If these are not required then disabling them could
   * speed up the computation cycle significantly.
   * @return the property, not null
   */
  public final Property<ResultOutputMode> aggregatePositionOutputMode() {
    return metaBean().aggregatePositionOutputMode().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the individual position output mode.
   * <p>
   * If only aggregate position calculations are required, with respect to the hierarchy of the reference portfolio, then disabling outputs for individual positions through this method could speed up
   * the computation cycle significantly. This is beneficial for calculations, such as VaR, which can be performed at the aggregate level without requiring the complete result of the same calculation
   * on its children. Aggregate calculations where this is not the case will be unaffected, although disabling the individual position outputs will still hide them from the user even though they will
   * be calculated.
   * @return the value of the property
   */
  public ResultOutputMode getPositionOutputMode() {
    return _positionOutputMode;
  }

  /**
   * Sets the individual position output mode.
   * <p>
   * If only aggregate position calculations are required, with respect to the hierarchy of the reference portfolio, then disabling outputs for individual positions through this method could speed up
   * the computation cycle significantly. This is beneficial for calculations, such as VaR, which can be performed at the aggregate level without requiring the complete result of the same calculation
   * on its children. Aggregate calculations where this is not the case will be unaffected, although disabling the individual position outputs will still hide them from the user even though they will
   * be calculated.
   * @param positionOutputMode  the new value of the property
   */
  public void setPositionOutputMode(ResultOutputMode positionOutputMode) {
    this._positionOutputMode = positionOutputMode;
  }

  /**
   * Gets the the {@code positionOutputMode} property.
   * <p>
   * If only aggregate position calculations are required, with respect to the hierarchy of the reference portfolio, then disabling outputs for individual positions through this method could speed up
   * the computation cycle significantly. This is beneficial for calculations, such as VaR, which can be performed at the aggregate level without requiring the complete result of the same calculation
   * on its children. Aggregate calculations where this is not the case will be unaffected, although disabling the individual position outputs will still hide them from the user even though they will
   * be calculated.
   * @return the property, not null
   */
  public final Property<ResultOutputMode> positionOutputMode() {
    return metaBean().positionOutputMode().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the trade output mode.
   * <p>
   * If only aggregate position calculations are required, with respect to the hierarchy of the reference portfolio, then disabling outputs for individual trades through this method could speed up the
   * computation cycle significantly. This is beneficial for calculations, such as VaR, which can be performed at the aggregate level without requiring the complete result of the same calculation on
   * its children. Aggregate calculations where this is not the case will be unaffected, although disabling the individual trade outputs will still hide them from the user even though they will be
   * calculated.
   * @return the value of the property
   */
  public ResultOutputMode getTradeOutputMode() {
    return _tradeOutputMode;
  }

  /**
   * Sets the trade output mode.
   * <p>
   * If only aggregate position calculations are required, with respect to the hierarchy of the reference portfolio, then disabling outputs for individual trades through this method could speed up the
   * computation cycle significantly. This is beneficial for calculations, such as VaR, which can be performed at the aggregate level without requiring the complete result of the same calculation on
   * its children. Aggregate calculations where this is not the case will be unaffected, although disabling the individual trade outputs will still hide them from the user even though they will be
   * calculated.
   * @param tradeOutputMode  the new value of the property
   */
  public void setTradeOutputMode(ResultOutputMode tradeOutputMode) {
    this._tradeOutputMode = tradeOutputMode;
  }

  /**
   * Gets the the {@code tradeOutputMode} property.
   * <p>
   * If only aggregate position calculations are required, with respect to the hierarchy of the reference portfolio, then disabling outputs for individual trades through this method could speed up the
   * computation cycle significantly. This is beneficial for calculations, such as VaR, which can be performed at the aggregate level without requiring the complete result of the same calculation on
   * its children. Aggregate calculations where this is not the case will be unaffected, although disabling the individual trade outputs will still hide them from the user even though they will be
   * calculated.
   * @return the property, not null
   */
  public final Property<ResultOutputMode> tradeOutputMode() {
    return metaBean().tradeOutputMode().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the security output mode.
   * <p>
   * These are values which relate generally to a security and apply to every position in that security. For example, market data on a security would be a security output.
   * @return the value of the property
   */
  public ResultOutputMode getSecurityOutputMode() {
    return _securityOutputMode;
  }

  /**
   * Sets the security output mode.
   * <p>
   * These are values which relate generally to a security and apply to every position in that security. For example, market data on a security would be a security output.
   * @param securityOutputMode  the new value of the property
   */
  public void setSecurityOutputMode(ResultOutputMode securityOutputMode) {
    this._securityOutputMode = securityOutputMode;
  }

  /**
   * Gets the the {@code securityOutputMode} property.
   * <p>
   * These are values which relate generally to a security and apply to every position in that security. For example, market data on a security would be a security output.
   * @return the property, not null
   */
  public final Property<ResultOutputMode> securityOutputMode() {
    return metaBean().securityOutputMode().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the primitive output mode.
   * <p>
   * These are values which may be used in calculations for many securities. For example, the USD discount curve would be a primitive.
   * @return the value of the property
   */
  public ResultOutputMode getPrimitiveOutputMode() {
    return _primitiveOutputMode;
  }

  /**
   * Sets the primitive output mode.
   * <p>
   * These are values which may be used in calculations for many securities. For example, the USD discount curve would be a primitive.
   * @param primitiveOutputMode  the new value of the property
   */
  public void setPrimitiveOutputMode(ResultOutputMode primitiveOutputMode) {
    this._primitiveOutputMode = primitiveOutputMode;
  }

  /**
   * Gets the the {@code primitiveOutputMode} property.
   * <p>
   * These are values which may be used in calculations for many securities. For example, the USD discount curve would be a primitive.
   * @return the property, not null
   */
  public final Property<ResultOutputMode> primitiveOutputMode() {
    return metaBean().primitiveOutputMode().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public ResultModelDefinition clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      ResultModelDefinition other = (ResultModelDefinition) obj;
      return JodaBeanUtils.equal(getAggregatePositionOutputMode(), other.getAggregatePositionOutputMode()) &&
          JodaBeanUtils.equal(getPositionOutputMode(), other.getPositionOutputMode()) &&
          JodaBeanUtils.equal(getTradeOutputMode(), other.getTradeOutputMode()) &&
          JodaBeanUtils.equal(getSecurityOutputMode(), other.getSecurityOutputMode()) &&
          JodaBeanUtils.equal(getPrimitiveOutputMode(), other.getPrimitiveOutputMode());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash += hash * 31 + JodaBeanUtils.hashCode(getAggregatePositionOutputMode());
    hash += hash * 31 + JodaBeanUtils.hashCode(getPositionOutputMode());
    hash += hash * 31 + JodaBeanUtils.hashCode(getTradeOutputMode());
    hash += hash * 31 + JodaBeanUtils.hashCode(getSecurityOutputMode());
    hash += hash * 31 + JodaBeanUtils.hashCode(getPrimitiveOutputMode());
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(192);
    buf.append("ResultModelDefinition{");
    int len = buf.length();
    toString(buf);
    if (buf.length() > len) {
      buf.setLength(buf.length() - 2);
    }
    buf.append('}');
    return buf.toString();
  }

  protected void toString(StringBuilder buf) {
    buf.append("aggregatePositionOutputMode").append('=').append(JodaBeanUtils.toString(getAggregatePositionOutputMode())).append(',').append(' ');
    buf.append("positionOutputMode").append('=').append(JodaBeanUtils.toString(getPositionOutputMode())).append(',').append(' ');
    buf.append("tradeOutputMode").append('=').append(JodaBeanUtils.toString(getTradeOutputMode())).append(',').append(' ');
    buf.append("securityOutputMode").append('=').append(JodaBeanUtils.toString(getSecurityOutputMode())).append(',').append(' ');
    buf.append("primitiveOutputMode").append('=').append(JodaBeanUtils.toString(getPrimitiveOutputMode())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code ResultModelDefinition}.
   */
  public static class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code aggregatePositionOutputMode} property.
     */
    private final MetaProperty<ResultOutputMode> _aggregatePositionOutputMode = DirectMetaProperty.ofReadWrite(
        this, "aggregatePositionOutputMode", ResultModelDefinition.class, ResultOutputMode.class);
    /**
     * The meta-property for the {@code positionOutputMode} property.
     */
    private final MetaProperty<ResultOutputMode> _positionOutputMode = DirectMetaProperty.ofReadWrite(
        this, "positionOutputMode", ResultModelDefinition.class, ResultOutputMode.class);
    /**
     * The meta-property for the {@code tradeOutputMode} property.
     */
    private final MetaProperty<ResultOutputMode> _tradeOutputMode = DirectMetaProperty.ofReadWrite(
        this, "tradeOutputMode", ResultModelDefinition.class, ResultOutputMode.class);
    /**
     * The meta-property for the {@code securityOutputMode} property.
     */
    private final MetaProperty<ResultOutputMode> _securityOutputMode = DirectMetaProperty.ofReadWrite(
        this, "securityOutputMode", ResultModelDefinition.class, ResultOutputMode.class);
    /**
     * The meta-property for the {@code primitiveOutputMode} property.
     */
    private final MetaProperty<ResultOutputMode> _primitiveOutputMode = DirectMetaProperty.ofReadWrite(
        this, "primitiveOutputMode", ResultModelDefinition.class, ResultOutputMode.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "aggregatePositionOutputMode",
        "positionOutputMode",
        "tradeOutputMode",
        "securityOutputMode",
        "primitiveOutputMode");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case 1633131628:  // aggregatePositionOutputMode
          return _aggregatePositionOutputMode;
        case -798411699:  // positionOutputMode
          return _positionOutputMode;
        case -1772765496:  // tradeOutputMode
          return _tradeOutputMode;
        case -583556700:  // securityOutputMode
          return _securityOutputMode;
        case 545428107:  // primitiveOutputMode
          return _primitiveOutputMode;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends ResultModelDefinition> builder() {
      return new DirectBeanBuilder<ResultModelDefinition>(new ResultModelDefinition());
    }

    @Override
    public Class<? extends ResultModelDefinition> beanType() {
      return ResultModelDefinition.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code aggregatePositionOutputMode} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ResultOutputMode> aggregatePositionOutputMode() {
      return _aggregatePositionOutputMode;
    }

    /**
     * The meta-property for the {@code positionOutputMode} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ResultOutputMode> positionOutputMode() {
      return _positionOutputMode;
    }

    /**
     * The meta-property for the {@code tradeOutputMode} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ResultOutputMode> tradeOutputMode() {
      return _tradeOutputMode;
    }

    /**
     * The meta-property for the {@code securityOutputMode} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ResultOutputMode> securityOutputMode() {
      return _securityOutputMode;
    }

    /**
     * The meta-property for the {@code primitiveOutputMode} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ResultOutputMode> primitiveOutputMode() {
      return _primitiveOutputMode;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 1633131628:  // aggregatePositionOutputMode
          return ((ResultModelDefinition) bean).getAggregatePositionOutputMode();
        case -798411699:  // positionOutputMode
          return ((ResultModelDefinition) bean).getPositionOutputMode();
        case -1772765496:  // tradeOutputMode
          return ((ResultModelDefinition) bean).getTradeOutputMode();
        case -583556700:  // securityOutputMode
          return ((ResultModelDefinition) bean).getSecurityOutputMode();
        case 545428107:  // primitiveOutputMode
          return ((ResultModelDefinition) bean).getPrimitiveOutputMode();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 1633131628:  // aggregatePositionOutputMode
          ((ResultModelDefinition) bean).setAggregatePositionOutputMode((ResultOutputMode) newValue);
          return;
        case -798411699:  // positionOutputMode
          ((ResultModelDefinition) bean).setPositionOutputMode((ResultOutputMode) newValue);
          return;
        case -1772765496:  // tradeOutputMode
          ((ResultModelDefinition) bean).setTradeOutputMode((ResultOutputMode) newValue);
          return;
        case -583556700:  // securityOutputMode
          ((ResultModelDefinition) bean).setSecurityOutputMode((ResultOutputMode) newValue);
          return;
        case 545428107:  // primitiveOutputMode
          ((ResultModelDefinition) bean).setPrimitiveOutputMode((ResultOutputMode) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
