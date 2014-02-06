/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */

package com.opengamma.masterdb.security.hibernate.index;

import static com.opengamma.masterdb.security.hibernate.Converters.externalIdBeanToExternalId;
import static com.opengamma.masterdb.security.hibernate.Converters.externalIdToExternalIdBean;
import static com.opengamma.masterdb.security.hibernate.Converters.tenorBeanToTenor;

import com.opengamma.financial.security.index.IborIndex;
import com.opengamma.id.ExternalId;
import com.opengamma.masterdb.security.hibernate.AbstractSecurityBeanOperation;
import com.opengamma.masterdb.security.hibernate.HibernateSecurityMasterDao;
import com.opengamma.masterdb.security.hibernate.OperationContext;
import com.opengamma.util.time.Tenor;

/**
 * Hibernate bean/security conversion operations.
 */
public final class IborIndexBeanOperation extends AbstractSecurityBeanOperation<IborIndex, IborIndexBean> {

  /**
   * Singleton instance.
   */
  public static final IborIndexBeanOperation INSTANCE = new IborIndexBeanOperation();

  private IborIndexBeanOperation() {
    super(IborIndex.INDEX_TYPE, IborIndex.class, IborIndexBean.class);
  }

  @Override
  public IborIndexBean createBean(final OperationContext context, HibernateSecurityMasterDao secMasterSession, IborIndex index) {
    final IborIndexBean bean = new IborIndexBean();
    bean.setDescription(index.getDescription());
    bean.setConventionId(externalIdToExternalIdBean(index.getConventionId()));
    bean.setTenor(secMasterSession.getOrCreateTenorBean(index.getTenor().toFormattedString()));
    if (index.getIndexFamilyId() != null) {
      bean.setIndexFamilyId(externalIdToExternalIdBean(index.getIndexFamilyId()));
    }
    return bean;
  }

  @Override
  public IborIndex createSecurity(final OperationContext context, IborIndexBean bean) {
    String description = bean.getDescription();
    Tenor tenor = tenorBeanToTenor(bean.getTenor());
    ExternalId conventionId = externalIdBeanToExternalId(bean.getConventionId());
    IborIndex iborIndex = new IborIndex("", description, tenor, conventionId);
    if (bean.getIndexFamilyId() != null) {
      ExternalId indexFamilyId = externalIdBeanToExternalId(bean.getIndexFamilyId());
      iborIndex.setIndexFamilyId(indexFamilyId);
    }
    return iborIndex;
  }

}
