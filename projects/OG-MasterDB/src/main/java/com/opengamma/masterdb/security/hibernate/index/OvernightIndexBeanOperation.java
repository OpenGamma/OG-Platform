/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */

package com.opengamma.masterdb.security.hibernate.index;

import static com.opengamma.masterdb.security.hibernate.Converters.externalIdBeanToExternalId;
import static com.opengamma.masterdb.security.hibernate.Converters.externalIdToExternalIdBean;

import com.opengamma.financial.security.index.OvernightIndex;
import com.opengamma.id.ExternalId;
import com.opengamma.masterdb.security.hibernate.AbstractSecurityBeanOperation;
import com.opengamma.masterdb.security.hibernate.HibernateSecurityMasterDao;
import com.opengamma.masterdb.security.hibernate.OperationContext;

/**
 * Hibernate bean/security conversion operations.
 */
public final class OvernightIndexBeanOperation extends AbstractSecurityBeanOperation<OvernightIndex, OvernightIndexBean> {

  /**
   * Singleton instance.
   */
  public static final OvernightIndexBeanOperation INSTANCE = new OvernightIndexBeanOperation();

  private OvernightIndexBeanOperation() {
    super(OvernightIndex.INDEX_TYPE, OvernightIndex.class, OvernightIndexBean.class);
  }

  @Override
  public OvernightIndexBean createBean(final OperationContext context, HibernateSecurityMasterDao secMasterSession, OvernightIndex index) {
    final OvernightIndexBean bean = new OvernightIndexBean();
    bean.setDescription(index.getDescription());
    bean.setConventionId(externalIdToExternalIdBean(index.getConventionId()));
    if (index.getIndexFamilyId() != null) {
      bean.setIndexFamilyId(externalIdToExternalIdBean(index.getIndexFamilyId()));
    }
    return bean;
  }

  @Override
  public OvernightIndex createSecurity(final OperationContext context, OvernightIndexBean bean) {
    String description = bean.getDescription();
    ExternalId conventionId = externalIdBeanToExternalId(bean.getConventionId());
    OvernightIndex overnightIndex = new OvernightIndex("", description, conventionId);
    if (bean.getIndexFamilyId() != null) {
      overnightIndex.setIndexFamilyId(externalIdBeanToExternalId(bean.getIndexFamilyId()));
    }
    return overnightIndex;
  }

}
