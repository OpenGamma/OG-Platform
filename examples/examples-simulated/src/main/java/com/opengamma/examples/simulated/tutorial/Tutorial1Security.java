/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.simulated.tutorial;

import java.util.Map;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.joda.beans.BeanBuilder;
import org.joda.beans.BeanDefinition;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

import com.opengamma.core.security.Security;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.master.security.RawSecurity;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.money.Currency;
import org.joda.beans.Bean;

/**
 * Implementation of the custom security type used in the tutorial. This shows how to extend the asset class support available from the OpenGamma instance without having to alter the database backed
 * {@link SecurityMaster} by transforming the security to/from a {@link RawSecurity} for storage and transport.
 * <p>
 * Our tutorial asset class has the common fields from {@link Security} plus a currency and a reference to another underlying or component security.
 * <p>
 * The additional fields specific to this new asset type are held in attributes defined on this class. The default OpenGamma security master database will not be able to support our custom class so
 * these fields are stored in a Fudge message and this is converted to a {@code byte[]} which can be held in a {@code RawSecurity} instance which is fully supported.
 */
@BeanDefinition
public class Tutorial1Security extends ManageableSecurity {

  private static final long serialVersionUID = 1L;

  /**
   * Type string used to identify this class of security.
   */
  public static final String TYPE = "TUTORIAL_1";

  /**
   * The currency the security is in.
   */
  @PropertyDefinition(validate = "notNull")
  private Currency _currency;

  /**
   * An identifier to the underlying or component security.
   */
  @PropertyDefinition(validate = "notNull")
  private ExternalId _underlying;

  public Tutorial1Security() {
    super(TYPE);
  }

  /**
   * Creates a new security instance.
   *
   * @param name the display name or label for the security, not null
   * @param identifiers the identifiers that reference this security in other systems, not null
   * @param currency the currency, not null
   * @param underlying an identifier of the underlying of component security, not null
   */
  public Tutorial1Security(final String name, final ExternalIdBundle identifiers, final Currency currency, final ExternalId underlying) {
    super(null, name, TYPE, identifiers);
    ArgumentChecker.notNull(currency, "currency");
    ArgumentChecker.notNull(underlying, "underlying");
    setCurrency(currency);
    setUnderlying(underlying);
  }

  /**
   * Creates a security instance copying common properties from the {@code RawSecurity}, and filling other attributes from the supplied Fudge message. This will be called by {@link #fromRawSecurity}
   * or by a sub-class being converted from a {@code RawSecurity}.
   *
   * @param copyFrom the {@code RawSecurity} containing the common properties, not null
   * @param deserializer the Fudge deserializer instance to decode the message with, not null
   * @param msg the Fudge message containing this asset's properties
   */
  protected Tutorial1Security(final Security copyFrom, final FudgeDeserializer deserializer, final FudgeMsg msg) {
    this(copyFrom.getName(), copyFrom.getExternalIdBundle(), deserializer.fieldValueToObject(Currency.class, msg.getByName(Meta.INSTANCE.currency().name())), deserializer.fieldValueToObject(
        ExternalId.class, msg.getByName(Meta.INSTANCE.underlying().name())));
    setAttributes(copyFrom.getAttributes());
  }

  /**
   * Creates a {@code Tutorial1Security} instance from a {@code RawSecurity} instance. Note that the instance returned is not coupled to the original security in any way - changes made to the returned
   * instance will not affect the original raw security instance, and changes to the raw security will not affect the returned instance.
   * <p>
   * A sub-class will implement it's own form of this, performing a suitable {@link #isInstance} check and then calling the constructor.
   *
   * @param raw the raw security instance, not null
   * @return the equivalent tutorial security, not null
   * @throws IllegalArgumentException if the raw security is not an encoding of a {@code Tutorial1Security}
   */
  public static Tutorial1Security fromRawSecurity(final RawSecurity raw) {
    ArgumentChecker.isTrue(isInstance(raw), "raw");
    final FudgeContext context = OpenGammaFudgeContext.getInstance();
    final FudgeMsg fudgeMsg = context.deserialize(raw.getRawData()).getMessage();
    final FudgeDeserializer deserializer = new FudgeDeserializer(context);
    return new Tutorial1Security(raw, deserializer, fudgeMsg);
  }

  /**
   * Tests whether a {@code RawSecurity} instance contains a {@code Tutorial1Security1}. A valid raw security can be used to obtain a {@code Tutorial1Security} instance by calling
   * {@link #fromRawSecurity}.
   *
   * @param raw the raw security instance, not null
   * @return true if the instance can be converted to a {@code Tutorial1Security}, false otherwise
   */
  public static boolean isInstance(final RawSecurity raw) {
    ArgumentChecker.notNull(raw, "raw");
    return TYPE.equals(raw.getSecurityType());
  }

  /**
   * Adds the fields for this asset class to the Fudge message. This is called by {@link #toRawSecurity}.
   * <p>
   * A sub-class should overload this to add its own fields to the message, calling this version to populate the fields from this class.
   *
   * @param serializer the Fudge serializer to use for encoding complex values, not null
   * @param msg the message to populate, not null
   */
  protected void populateFudgeMsg(final FudgeSerializer serializer, final MutableFudgeMsg msg) {
    serializer.addToMessage(msg, currency().name(), null, getCurrency());
    serializer.addToMessage(msg, underlying().name(), null, getUnderlying());
  }

  /**
   * Converts this instance to a {@code RawSecurity} containing the equivalent data so that it can be transported or persisted to the OpenGamma {@link SecurityMaster}.
   * <p>
   * A sub-class should not need to overload this method, overloading {@link #populateFudgeMsg} will correctly populate the {@code RawSecurity}.
   *
   * @return the raw security instance, not null
   */
  public RawSecurity toRawSecurity() {
    final FudgeContext context = OpenGammaFudgeContext.getInstance();
    final MutableFudgeMsg fudgeMsg = context.newMessage();
    final FudgeSerializer serializer = new FudgeSerializer(context);
    populateFudgeMsg(serializer, fudgeMsg);
    final RawSecurity security = new RawSecurity(getUniqueId(), getName(), getSecurityType(), getExternalIdBundle(), context.toByteArray(fudgeMsg));
    security.setAttributes(getAttributes());
    return security;
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code Tutorial1Security}.
   * @return the meta-bean, not null
   */
  public static Tutorial1Security.Meta meta() {
    return Tutorial1Security.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(Tutorial1Security.Meta.INSTANCE);
  }

  @Override
  public Tutorial1Security.Meta metaBean() {
    return Tutorial1Security.Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the currency the security is in.
   * @return the value of the property, not null
   */
  public Currency getCurrency() {
    return _currency;
  }

  /**
   * Sets the currency the security is in.
   * @param currency  the new value of the property, not null
   */
  public void setCurrency(Currency currency) {
    JodaBeanUtils.notNull(currency, "currency");
    this._currency = currency;
  }

  /**
   * Gets the the {@code currency} property.
   * @return the property, not null
   */
  public final Property<Currency> currency() {
    return metaBean().currency().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets an identifier to the underlying or component security.
   * @return the value of the property, not null
   */
  public ExternalId getUnderlying() {
    return _underlying;
  }

  /**
   * Sets an identifier to the underlying or component security.
   * @param underlying  the new value of the property, not null
   */
  public void setUnderlying(ExternalId underlying) {
    JodaBeanUtils.notNull(underlying, "underlying");
    this._underlying = underlying;
  }

  /**
   * Gets the the {@code underlying} property.
   * @return the property, not null
   */
  public final Property<ExternalId> underlying() {
    return metaBean().underlying().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public Tutorial1Security clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      Tutorial1Security other = (Tutorial1Security) obj;
      return JodaBeanUtils.equal(getCurrency(), other.getCurrency()) &&
          JodaBeanUtils.equal(getUnderlying(), other.getUnderlying()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash += hash * 31 + JodaBeanUtils.hashCode(getCurrency());
    hash += hash * 31 + JodaBeanUtils.hashCode(getUnderlying());
    return hash ^ super.hashCode();
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(96);
    buf.append("Tutorial1Security{");
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
    buf.append("currency").append('=').append(JodaBeanUtils.toString(getCurrency())).append(',').append(' ');
    buf.append("underlying").append('=').append(JodaBeanUtils.toString(getUnderlying())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code Tutorial1Security}.
   */
  public static class Meta extends ManageableSecurity.Meta {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code currency} property.
     */
    private final MetaProperty<Currency> _currency = DirectMetaProperty.ofReadWrite(
        this, "currency", Tutorial1Security.class, Currency.class);
    /**
     * The meta-property for the {@code underlying} property.
     */
    private final MetaProperty<ExternalId> _underlying = DirectMetaProperty.ofReadWrite(
        this, "underlying", Tutorial1Security.class, ExternalId.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "currency",
        "underlying");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case 575402001:  // currency
          return _currency;
        case -1770633379:  // underlying
          return _underlying;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends Tutorial1Security> builder() {
      return new DirectBeanBuilder<Tutorial1Security>(new Tutorial1Security());
    }

    @Override
    public Class<? extends Tutorial1Security> beanType() {
      return Tutorial1Security.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code currency} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Currency> currency() {
      return _currency;
    }

    /**
     * The meta-property for the {@code underlying} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ExternalId> underlying() {
      return _underlying;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 575402001:  // currency
          return ((Tutorial1Security) bean).getCurrency();
        case -1770633379:  // underlying
          return ((Tutorial1Security) bean).getUnderlying();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 575402001:  // currency
          ((Tutorial1Security) bean).setCurrency((Currency) newValue);
          return;
        case -1770633379:  // underlying
          ((Tutorial1Security) bean).setUnderlying((ExternalId) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

    @Override
    protected void validate(Bean bean) {
      JodaBeanUtils.notNull(((Tutorial1Security) bean)._currency, "currency");
      JodaBeanUtils.notNull(((Tutorial1Security) bean)._underlying, "underlying");
      super.validate(bean);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
