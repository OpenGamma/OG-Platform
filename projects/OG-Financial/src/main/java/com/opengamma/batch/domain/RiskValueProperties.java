/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.batch.domain;

import static com.google.common.collect.Lists.newArrayList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.opengamma.engine.value.ValueProperties;

/**
 * Data model for a set of risk value properties.
 */
@BeanDefinition
public class RiskValueProperties extends DirectBean {

  private static final Pattern ESCAPE_PATTERN = Pattern.compile("[=\\?\\[\\],\\\\]");

  @PropertyDefinition
  private int _id;

  @PropertyDefinition
  private String _syntheticForm;

  public RiskValueProperties() {
  }

  public RiskValueProperties(ValueProperties requirement) {
    setSyntheticForm(synthesize(requirement));
  }

  private static String escape(Pattern p, String s) {
    return p.matcher(s).replaceAll("\\\\$0");
  }

  private static String unescape(String s) {
    return s.replaceAll("\\\\", "");
  }

  public static String synthesize(ValueProperties requirement) {
    try {
      JSONObject json = new JSONObject();
      if (ValueProperties.all() == requirement) {
        json.put("infinity", true).toString();
      } else if (ValueProperties.isNearInfiniteProperties(requirement)) {
        //ValueProperties.NearlyInfinitePropertiesImpl nearlyInifite = (ValueProperties.NearlyInfinitePropertiesImpl) requirement;
        final List<String> nearlyInfinite = new ArrayList<String>(ValueProperties.all().getUnsatisfied(requirement));
        Collections.sort(nearlyInfinite);
        JSONArray without = new JSONArray();
        for (String value : nearlyInfinite) {
          without.put(escape(ESCAPE_PATTERN, value));
        }
        json.put("without", without);
      } else {
        JSONArray properties = new JSONArray();
        if (requirement.getProperties() != null) {
          List<String> sorted = new ArrayList<>(requirement.getProperties());
          Collections.sort(sorted);
          for (String property : sorted) {
            JSONObject propertyJson = new JSONObject();

            propertyJson.put("name", property);
            if (requirement.isOptional(property)) {
              propertyJson.put("optional", true);
            }

            JSONArray values = new JSONArray();
            List<String> sorted2 = new ArrayList<>(requirement.getValues(property));
            Collections.sort(sorted2);
            for (String value : sorted2) {
              values.put(escape(ESCAPE_PATTERN, value));
            }
            propertyJson.put("values", values);
            properties.put(propertyJson);
          }
        }
        json.put("properties", properties);
      }
      return json.toString();
    } catch (JSONException e) {
      throw new RuntimeException(e);
    }
  }

  public static ValueProperties parseJson(String source) {
    try {
      JSONObject json = new JSONObject(source);

      if (json.has("infinity") && json.getBoolean("infinity")) {
        return ValueProperties.all();
      } else if (json.has("without")) {
        JSONArray without = json.getJSONArray("without");
        ValueProperties.Builder builder = ValueProperties.all().copy();
        for (int i = 0; i < without.length(); i++) {
          String value = unescape(without.getString(i));
          builder.withoutAny(value);
        }
        return builder.get();
      } else if (json.has("properties")) {
        ValueProperties.Builder builder = ValueProperties.builder();
        JSONArray properties = json.getJSONArray("properties");
        for (int i = 0; i < properties.length(); i++) {
          JSONObject property = properties.getJSONObject(i);
          String propertyName = property.getString("name");
          if (property.has("optional") && property.getBoolean("optional")) {
            builder.withOptional(propertyName);
          }
          if (property.has("values")) {
            JSONArray valueArray = property.getJSONArray("values");
            Collection<String> values = newArrayList();
            for (int j = 0; j < valueArray.length(); j++) {
              values.add(unescape(valueArray.getString(j)));
            }
            builder.with(propertyName, values);
          }
        }
        return builder.get();
      } else {
        return ValueProperties.none();
      }
    } catch (JSONException e) {
      throw new RuntimeException(e);
    }
  }

  public ValueProperties toProperties() {
    try {
      JSONObject jsonObject = new JSONObject(_syntheticForm);
      if (jsonObject.has("infinity") && jsonObject.getBoolean("infinity")) {
        return ValueProperties.all();
      } else if (jsonObject.has("without")) {
        JSONArray withoutProperties = jsonObject.getJSONArray("without");
        ValueProperties requirement = ValueProperties.all();
        for (int i = 0; i < withoutProperties.length(); i++) {
          String without = (String) withoutProperties.get(i);
          requirement = requirement.withoutAny(without);
        }
        return requirement;
      } else if (jsonObject.has("properties") && jsonObject.getJSONArray("properties") != null) {
        JSONArray withProperties = jsonObject.getJSONArray("properties");
        final ValueProperties.Builder builder = ValueProperties.builder();
        for (int i = 0; i < withProperties.length(); i++) {
          JSONObject property = (JSONObject) withProperties.get(i);
          String name = property.getString("name");
          if (jsonObject.has("optional") && property.getBoolean("optional")) {
            builder.withOptional(name);
          }
          JSONArray values = (JSONArray) property.get("values");
          for (int j = 0; j < values.length(); j++) {
            String value = (String) values.get(j);
            builder.with(name, value);
          }
        }
        return builder.get();
      } else {
        return null;
      }
    } catch (JSONException e) {
      throw new RuntimeException(e);
    }
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code RiskValueProperties}.
   * @return the meta-bean, not null
   */
  public static RiskValueProperties.Meta meta() {
    return RiskValueProperties.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(RiskValueProperties.Meta.INSTANCE);
  }

  @Override
  public RiskValueProperties.Meta metaBean() {
    return RiskValueProperties.Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the id.
   * @return the value of the property
   */
  public int getId() {
    return _id;
  }

  /**
   * Sets the id.
   * @param id  the new value of the property
   */
  public void setId(int id) {
    this._id = id;
  }

  /**
   * Gets the the {@code id} property.
   * @return the property, not null
   */
  public final Property<Integer> id() {
    return metaBean().id().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the syntheticForm.
   * @return the value of the property
   */
  public String getSyntheticForm() {
    return _syntheticForm;
  }

  /**
   * Sets the syntheticForm.
   * @param syntheticForm  the new value of the property
   */
  public void setSyntheticForm(String syntheticForm) {
    this._syntheticForm = syntheticForm;
  }

  /**
   * Gets the the {@code syntheticForm} property.
   * @return the property, not null
   */
  public final Property<String> syntheticForm() {
    return metaBean().syntheticForm().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public RiskValueProperties clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      RiskValueProperties other = (RiskValueProperties) obj;
      return (getId() == other.getId()) &&
          JodaBeanUtils.equal(getSyntheticForm(), other.getSyntheticForm());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash += hash * 31 + JodaBeanUtils.hashCode(getId());
    hash += hash * 31 + JodaBeanUtils.hashCode(getSyntheticForm());
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(96);
    buf.append("RiskValueProperties{");
    int len = buf.length();
    toString(buf);
    if (buf.length() > len) {
      buf.setLength(buf.length() - 2);
    }
    buf.append('}');
    return buf.toString();
  }

  protected void toString(StringBuilder buf) {
    buf.append("id").append('=').append(JodaBeanUtils.toString(getId())).append(',').append(' ');
    buf.append("syntheticForm").append('=').append(JodaBeanUtils.toString(getSyntheticForm())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code RiskValueProperties}.
   */
  public static class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code id} property.
     */
    private final MetaProperty<Integer> _id = DirectMetaProperty.ofReadWrite(
        this, "id", RiskValueProperties.class, Integer.TYPE);
    /**
     * The meta-property for the {@code syntheticForm} property.
     */
    private final MetaProperty<String> _syntheticForm = DirectMetaProperty.ofReadWrite(
        this, "syntheticForm", RiskValueProperties.class, String.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "id",
        "syntheticForm");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case 3355:  // id
          return _id;
        case 1545026985:  // syntheticForm
          return _syntheticForm;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends RiskValueProperties> builder() {
      return new DirectBeanBuilder<RiskValueProperties>(new RiskValueProperties());
    }

    @Override
    public Class<? extends RiskValueProperties> beanType() {
      return RiskValueProperties.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code id} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Integer> id() {
      return _id;
    }

    /**
     * The meta-property for the {@code syntheticForm} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> syntheticForm() {
      return _syntheticForm;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 3355:  // id
          return ((RiskValueProperties) bean).getId();
        case 1545026985:  // syntheticForm
          return ((RiskValueProperties) bean).getSyntheticForm();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 3355:  // id
          ((RiskValueProperties) bean).setId((Integer) newValue);
          return;
        case 1545026985:  // syntheticForm
          ((RiskValueProperties) bean).setSyntheticForm((String) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
