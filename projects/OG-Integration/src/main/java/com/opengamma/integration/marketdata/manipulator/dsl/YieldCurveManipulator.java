/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.joda.beans.Bean;
import org.joda.beans.BeanDefinition;
import org.joda.beans.ImmutableBean;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectFieldsBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaBean;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.analytics.financial.legalentity.LegalEntityFilter;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.marketdata.manipulator.function.StructureManipulator;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;

/**
 * Abstract superclass for manipulators that operate on yield curves.
 * <p>
 * The target type is {@code Object} because the curves can be an individual {@link YieldCurve} or
 * part of a multicurve bundle ({@link MulticurveProviderDiscount} or {@link IssuerProviderDiscount}).
 */
@BeanDefinition
public abstract class YieldCurveManipulator implements ImmutableBean, StructureManipulator<Object> {

  private static final Logger s_logger = LoggerFactory.getLogger(YieldCurveManipulator.class);

  /**
   * The selector which selects the curves to be shifted
   */
  @PropertyDefinition(validate = "notNull")
  protected final YieldCurveSelector _selector;

  /* package */ YieldCurveManipulator(YieldCurveSelector selector) {
    _selector = ArgumentChecker.notNull(selector, "selector");
  }

  @Override
  public Object execute(Object target, ValueSpecification valueSpec, FunctionExecutionContext executionContext) {
    if (target instanceof YieldCurve) {
      return shiftCurve((YieldCurve) target);
    }
    if (target instanceof MulticurveProviderDiscount) {
      return shiftCurves((MulticurveProviderDiscount) target, valueSpec);
    }
    if (target instanceof IssuerProviderDiscount) {
      return shiftCurves((IssuerProviderDiscount) target, valueSpec);
    }
    s_logger.warn("Unexpected target type {}", target.getClass().getName());
    return target;
  }

  /**
   * Returns a {@link MulticurveProviderDiscount} with a shift applied to the named curve.
   * If there is no curve with the specified name the input provider is returned.
   *
   * @param multicurveProvider  the multicurve provider
   * @param valueSpec  the value specification of the curve or curve bundle
   * @return  a multicurve provider with a shifted curve
   */
  private MulticurveProviderDiscount shiftCurves(MulticurveProviderDiscount multicurveProvider,
                                                 ValueSpecification valueSpec) {
    FXMatrix fxMatrix = multicurveProvider.getFxRates();
    Map<Currency, YieldAndDiscountCurve> curvesByCurrency = new HashMap<>(multicurveProvider.getDiscountingCurves());
    Map<IborIndex, YieldAndDiscountCurve> curvesByIborIndex = new HashMap<>(multicurveProvider.getForwardIborCurves());
    Map<IndexON, YieldAndDiscountCurve> curvesByOnIndex = new HashMap<>(multicurveProvider.getForwardONCurves());

    for (String curveName : _selector.matchingCurveNames(valueSpec)) {
      YieldAndDiscountCurve yieldAndDiscountCurve = multicurveProvider.getCurve(curveName);

      if (yieldAndDiscountCurve == null) {
        continue;
      }
      // we're relying on YieldCurveUtils to do the shift which only supports yield curves
      if (!(yieldAndDiscountCurve instanceof YieldCurve)) {
        s_logger.warn("Unexpected curve type, unable to shift {}", yieldAndDiscountCurve.getClass().getName());
        continue;
      }
      YieldCurve curve = (YieldCurve) yieldAndDiscountCurve;
      YieldAndDiscountCurve shiftedCurve = shiftCurve(curve);

      Currency currency = multicurveProvider.getCurrencyForName(curveName);
      if (currency != null) {
        curvesByCurrency.put(currency, shiftedCurve);
      }
      IborIndex iborIndex = multicurveProvider.getIborIndexForName(curveName);
      if (iborIndex != null) {
        curvesByIborIndex.put(iborIndex, shiftedCurve);
      }
      IndexON indexON = multicurveProvider.getOvernightIndexForName(curveName);
      if (indexON != null) {
        curvesByOnIndex.put(indexON, shiftedCurve);
      }
    }
    return new MulticurveProviderDiscount(curvesByCurrency, curvesByIborIndex, curvesByOnIndex, fxMatrix);
  }

  private IssuerProviderDiscount shiftCurves(IssuerProviderDiscount issuerProvider, ValueSpecification valueSpec) {
    Map<Pair<Object, LegalEntityFilter<LegalEntity>>, YieldAndDiscountCurve> curves =
        new HashMap<>(issuerProvider.getIssuerCurves());

    for (String curveName : _selector.matchingCurveNames(valueSpec)) {
      YieldAndDiscountCurve yieldAndDiscountCurve = issuerProvider.getCurve(curveName);

      // we're relying on YieldCurveUtils to do the shift which only supports yield curves
      if (!(yieldAndDiscountCurve instanceof YieldCurve)) {
        s_logger.warn("Unexpected curve type, unable to shift {}", yieldAndDiscountCurve.getClass().getName());
        continue;
      }
      YieldCurve curve = (YieldCurve) yieldAndDiscountCurve;
      YieldAndDiscountCurve shiftedCurve = shiftCurve(curve);

      // this is nasty. we need to find the curve in the map
      // but we don't have any way to derive the map name from the curve or its name
      // the only option is to iterate over the map values until we find the same curve
      for (Map.Entry<Pair<Object, LegalEntityFilter<LegalEntity>>, YieldAndDiscountCurve> entry :
          issuerProvider.getIssuerCurves().entrySet()) {

        Pair<Object, LegalEntityFilter<LegalEntity>> key = entry.getKey();
        YieldAndDiscountCurve curveFromMap = entry.getValue();

        // reference equality, the curves retrieved by name are the same instances in the map
        if (curve == curveFromMap) {
          curves.put(key, shiftedCurve);
        }
      }
    }
    MulticurveProviderDiscount multicurveProvider = shiftCurves(issuerProvider.getMulticurveProvider(), valueSpec);
    return new IssuerProviderDiscount(multicurveProvider, curves);
  }

  @Override
  public Class<Object> getExpectedType() {
    return Object.class;
  }

  /**
   * Returns a new curve with a shift applied.
   *
   * @param curve  the curve
   * @return  a copy of the curve with a shift applied
   */
  protected abstract YieldCurve shiftCurve(YieldCurve curve);

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code YieldCurveManipulator}.
   * @return the meta-bean, not null
   */
  public static YieldCurveManipulator.Meta meta() {
    return YieldCurveManipulator.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(YieldCurveManipulator.Meta.INSTANCE);
  }

  /**
   * Restricted constructor.
   * @param builder  the builder to copy from, not null
   */
  protected YieldCurveManipulator(YieldCurveManipulator.Builder builder) {
    JodaBeanUtils.notNull(builder._selector, "selector");
    this._selector = builder._selector;
  }

  @Override
  public YieldCurveManipulator.Meta metaBean() {
    return YieldCurveManipulator.Meta.INSTANCE;
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
   * Gets the selector which selects the curves to be shifted
   * @return the value of the property, not null
   */
  public YieldCurveSelector getSelector() {
    return _selector;
  }

  //-----------------------------------------------------------------------
  /**
   * Returns a builder that allows this bean to be mutated.
   * @return the mutable builder, not null
   */
  public abstract Builder toBuilder();

  @Override
  public YieldCurveManipulator clone() {
    return this;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      YieldCurveManipulator other = (YieldCurveManipulator) obj;
      return JodaBeanUtils.equal(getSelector(), other.getSelector());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash += hash * 31 + JodaBeanUtils.hashCode(getSelector());
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(64);
    buf.append("YieldCurveManipulator{");
    int len = buf.length();
    toString(buf);
    if (buf.length() > len) {
      buf.setLength(buf.length() - 2);
    }
    buf.append('}');
    return buf.toString();
  }

  protected void toString(StringBuilder buf) {
    buf.append("selector").append('=').append(JodaBeanUtils.toString(getSelector())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code YieldCurveManipulator}.
   */
  public static class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code selector} property.
     */
    private final MetaProperty<YieldCurveSelector> _selector = DirectMetaProperty.ofImmutable(
        this, "selector", YieldCurveManipulator.class, YieldCurveSelector.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "selector");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case 1191572447:  // selector
          return _selector;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public YieldCurveManipulator.Builder builder() {
      throw new UnsupportedOperationException("YieldCurveManipulator is an abstract class");
    }

    @Override
    public Class<? extends YieldCurveManipulator> beanType() {
      return YieldCurveManipulator.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code selector} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<YieldCurveSelector> selector() {
      return _selector;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 1191572447:  // selector
          return ((YieldCurveManipulator) bean).getSelector();
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
   * The bean-builder for {@code YieldCurveManipulator}.
   */
  public abstract static class Builder extends DirectFieldsBeanBuilder<YieldCurveManipulator> {

    private YieldCurveSelector _selector;

    /**
     * Restricted constructor.
     */
    protected Builder() {
    }

    /**
     * Restricted copy constructor.
     * @param beanToCopy  the bean to copy from, not null
     */
    protected Builder(YieldCurveManipulator beanToCopy) {
      this._selector = beanToCopy.getSelector();
    }

    //-----------------------------------------------------------------------
    @Override
    public Object get(String propertyName) {
      switch (propertyName.hashCode()) {
        case 1191572447:  // selector
          return _selector;
        default:
          throw new NoSuchElementException("Unknown property: " + propertyName);
      }
    }

    @Override
    public Builder set(String propertyName, Object newValue) {
      switch (propertyName.hashCode()) {
        case 1191572447:  // selector
          this._selector = (YieldCurveSelector) newValue;
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

    //-----------------------------------------------------------------------
    /**
     * Sets the {@code selector} property in the builder.
     * @param selector  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder selector(YieldCurveSelector selector) {
      JodaBeanUtils.notNull(selector, "selector");
      this._selector = selector;
      return this;
    }

    //-----------------------------------------------------------------------
    @Override
    public String toString() {
      StringBuilder buf = new StringBuilder(64);
      buf.append("YieldCurveManipulator.Builder{");
      int len = buf.length();
      toString(buf);
      if (buf.length() > len) {
        buf.setLength(buf.length() - 2);
      }
      buf.append('}');
      return buf.toString();
    }

    protected void toString(StringBuilder buf) {
      buf.append("selector").append('=').append(JodaBeanUtils.toString(_selector)).append(',').append(' ');
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
