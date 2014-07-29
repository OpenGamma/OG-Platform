/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.id;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
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

import com.google.common.collect.Maps;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalScheme;

/**
 * Class to hold configuration of custom display ordering of external ids.
 */
@SuppressWarnings("deprecation")
@BeanDefinition
public class ExternalIdOrderConfig extends DirectBean {
  @PropertyDefinition(validate = "notNull")
  private Map<ExternalScheme, Integer> _rateMap;
  /**
   * Default config object. Do not use directly.
   */
  public static final ExternalIdOrderConfig DEFAULT_CONFIG = new ExternalIdOrderConfig();

  private static Map<ExternalScheme, Integer> s_defaultScoreMap = Maps.newHashMap();

  static {
    s_defaultScoreMap.put(ExternalSchemes.BLOOMBERG_TCM, 20); // Because if there's both ticker and TCM, you want to see TCM.
    s_defaultScoreMap.put(ExternalSchemes.BLOOMBERG_TICKER, 19);
    s_defaultScoreMap.put(ExternalSchemes.RIC, 17);
    s_defaultScoreMap.put(ExternalSchemes.BLOOMBERG_TICKER_WEAK, 16);
    s_defaultScoreMap.put(ExternalSchemes.ACTIVFEED_TICKER, 15);
    s_defaultScoreMap.put(ExternalSchemes.SURF, 14);
    s_defaultScoreMap.put(ExternalSchemes.ISIN, 13);
    s_defaultScoreMap.put(ExternalSchemes.CUSIP, 12);
    s_defaultScoreMap.put(ExternalSchemes.SEDOL1, 11);
    s_defaultScoreMap.put(ExternalSchemes.OG_SYNTHETIC_TICKER, 10);
    s_defaultScoreMap.put(ExternalSchemes.BLOOMBERG_BUID, 5);
    s_defaultScoreMap.put(ExternalSchemes.BLOOMBERG_BUID_WEAK, 4);
    DEFAULT_CONFIG.setRateMap(s_defaultScoreMap);
  }

  /**
   * Apply the ordering to obtain the most preferred identifier from a bundle.
   * 
   * @param identifiers the bundle of identifiers to query
   * @return the preferred identifier from the bundle, or null if it is empty
   */
  public ExternalId getPreferred(final ExternalIdBundle identifiers) {
    if (identifiers.isEmpty()) {
      return null;
    } else if (identifiers.size() == 1) {
      return identifiers.iterator().next();
    } else {
      final Map<ExternalScheme, Integer> rates = getRateMap();
      ExternalId preferred = null;
      int preferredScore = Integer.MIN_VALUE;
      for (final ExternalId id : identifiers) {
        final Integer score = rates.get(id.getScheme());
        if (preferred == null) {
          preferred = id;
          if (score != null) {
            preferredScore = score;
          }
        } else if (score != null) {
          if (score > preferredScore) {
            preferred = id;
            preferredScore = score;
          } else if (score == preferredScore) {
            // same score, so use natural ordering of the schemes
            if (id.getScheme().compareTo(preferred.getScheme()) < 0) {
              preferred = id;
            }
          }
        } else {
          if (preferredScore == Integer.MIN_VALUE) {
            // same score, so use natural ordering of the schemes
            if (id.getScheme().compareTo(preferred.getScheme()) < 0) {
              preferred = id;
            }
          }
        }
      }
      return preferred;
    }
  }

  /**
   * Returns a {@link Comparator} that will order schemes from highest to lowest rank.
   * 
   * @return the comparator
   */
  public Comparator<ExternalScheme> schemeComparator() {
    return new Comparator<ExternalScheme>() {
      @Override
      public int compare(final ExternalScheme o1, final ExternalScheme o2) {
        final Integer r1 = getRateMap().get(o1);
        final Integer r2 = getRateMap().get(o2);
        if (r1 == null) {
          if (r2 == null) {
            // neither have a rank, use the natural ordering
            return o1.compareTo(o2);
          } else {
            // o2 has a rank, use that
            return 1;
          }
        } else {
          if (r2 == null) {
            // o1 has a rank, use that
            return -1;
          } else {
            final int r = r2 - r1;
            if (r != 0) {
              return r;
            } else {
              // both have the same rank, use the natural ordering
              return o1.compareTo(o2);
            }
          }
        }
      }
    };
  }

  /**
   * Returns a {@link Comparator} that will order identifiers from highest to lowest ranked scheme.
   * 
   * @return the comparator
   */
  public Comparator<ExternalId> identifierComparator() {
    final Comparator<ExternalScheme> scheme = schemeComparator();
    return new Comparator<ExternalId>() {
      @Override
      public int compare(final ExternalId o1, final ExternalId o2) {
        final int c = scheme.compare(o1.getScheme(), o2.getScheme());
        if (c != 0) {
          return c;
        } else {
          // Same scheme, order by identifier value
          return o1.getValue().compareTo(o2.getValue());
        }
      }
    };
  }

  /**
   * Sorts the identifiers from a bundle from highest to lowest ranked scheme. Identifiers with the same scheme will be sorted naturally.
   * 
   * @param bundle the identifiers to sort
   * @return the identifiers in descending tank order
   */
  public List<ExternalId> sort(final ExternalIdBundle bundle) {
    final List<ExternalId> identifiers = new ArrayList<ExternalId>(bundle.getExternalIds());
    Collections.sort(identifiers, identifierComparator());
    return identifiers;
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code ExternalIdOrderConfig}.
   * @return the meta-bean, not null
   */
  public static ExternalIdOrderConfig.Meta meta() {
    return ExternalIdOrderConfig.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(ExternalIdOrderConfig.Meta.INSTANCE);
  }

  @Override
  public ExternalIdOrderConfig.Meta metaBean() {
    return ExternalIdOrderConfig.Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the rateMap.
   * @return the value of the property, not null
   */
  public Map<ExternalScheme, Integer> getRateMap() {
    return _rateMap;
  }

  /**
   * Sets the rateMap.
   * @param rateMap  the new value of the property, not null
   */
  public void setRateMap(Map<ExternalScheme, Integer> rateMap) {
    JodaBeanUtils.notNull(rateMap, "rateMap");
    this._rateMap = rateMap;
  }

  /**
   * Gets the the {@code rateMap} property.
   * @return the property, not null
   */
  public final Property<Map<ExternalScheme, Integer>> rateMap() {
    return metaBean().rateMap().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public ExternalIdOrderConfig clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      ExternalIdOrderConfig other = (ExternalIdOrderConfig) obj;
      return JodaBeanUtils.equal(getRateMap(), other.getRateMap());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash += hash * 31 + JodaBeanUtils.hashCode(getRateMap());
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(64);
    buf.append("ExternalIdOrderConfig{");
    int len = buf.length();
    toString(buf);
    if (buf.length() > len) {
      buf.setLength(buf.length() - 2);
    }
    buf.append('}');
    return buf.toString();
  }

  protected void toString(StringBuilder buf) {
    buf.append("rateMap").append('=').append(JodaBeanUtils.toString(getRateMap())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code ExternalIdOrderConfig}.
   */
  public static class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code rateMap} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<Map<ExternalScheme, Integer>> _rateMap = DirectMetaProperty.ofReadWrite(
        this, "rateMap", ExternalIdOrderConfig.class, (Class) Map.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "rateMap");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case 983446620:  // rateMap
          return _rateMap;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends ExternalIdOrderConfig> builder() {
      return new DirectBeanBuilder<ExternalIdOrderConfig>(new ExternalIdOrderConfig());
    }

    @Override
    public Class<? extends ExternalIdOrderConfig> beanType() {
      return ExternalIdOrderConfig.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code rateMap} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Map<ExternalScheme, Integer>> rateMap() {
      return _rateMap;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 983446620:  // rateMap
          return ((ExternalIdOrderConfig) bean).getRateMap();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 983446620:  // rateMap
          ((ExternalIdOrderConfig) bean).setRateMap((Map<ExternalScheme, Integer>) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

    @Override
    protected void validate(Bean bean) {
      JodaBeanUtils.notNull(((ExternalIdOrderConfig) bean)._rateMap, "rateMap");
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
