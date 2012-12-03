/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.blotter;

import java.util.List;
import java.util.Map;

import org.joda.beans.MetaBean;

import com.google.common.collect.Maps;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
/* package */ class MapMetaBeanFactory implements MetaBeanFactory {

  private final Map<String, MetaBean> _metaBeans = Maps.newHashMap();

  /* package */ MapMetaBeanFactory(List<MetaBean> metaBeans) {
    ArgumentChecker.notNull(metaBeans, "metaBeans");
    for (MetaBean metaBean : metaBeans) {
      _metaBeans.put(metaBean.beanType().getSimpleName(), metaBean);
    }
  }

  @Override
  public MetaBean beanFor(BeanDataSource beanData) {
    MetaBean metaBean = _metaBeans.get(beanData.getBeanTypeName());
    if (metaBean == null) {
      throw new OpenGammaRuntimeException("No meta bean for type " + beanData.getBeanTypeName());
    }
    return metaBean;
  }
}
