/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.blotter;

import java.util.Map;
import java.util.Set;

import org.joda.beans.MetaBean;

import com.google.common.collect.Maps;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.ArgumentChecker;

/**
 * {@link MetaBeanFactory} where the {@link MetaBean} instances are keyed on the
 * {@link BeanDataSource#getBeanTypeName() type name} of the bean.
 */
/* package */ class MapMetaBeanFactory implements MetaBeanFactory {

  private final Map<String, MetaBean> _metaBeans = Maps.newHashMap();

  /* package */ MapMetaBeanFactory(Set<MetaBean> metaBeans) {
    ArgumentChecker.notNull(metaBeans, "metaBeans");
    for (MetaBean metaBean : metaBeans) {
      _metaBeans.put(metaBean.beanType().getSimpleName(), metaBean);
    }
  }

  /**
   * @param beanData The bean data
   * @return The {@link MetaBean} keyed on the bean data's {@link BeanDataSource#getBeanTypeName() type name}, not null
   * @throws OpenGammaRuntimeException If there's no {@link MetaBean} keyed on the type name of the data source
   */
  @Override
  public MetaBean beanFor(BeanDataSource beanData) {
    MetaBean metaBean = _metaBeans.get(beanData.getBeanTypeName());
    if (metaBean == null) {
      throw new OpenGammaRuntimeException("No meta bean for type " + beanData.getBeanTypeName());
    }
    return metaBean;
  }
}
