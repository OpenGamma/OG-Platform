/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security.hibernate.cds;

import static com.opengamma.masterdb.security.hibernate.Converters.cdsIndexComponentBeanToCDSIndexComponent;
import static com.opengamma.masterdb.security.hibernate.Converters.currencyBeanToCurrency;
import static com.opengamma.masterdb.security.hibernate.Converters.externalIdToExternalIdBean;
import static com.opengamma.masterdb.security.hibernate.Converters.tenorBeanToTenor;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.opengamma.financial.security.cds.CDSIndexComponentBundle;
import com.opengamma.financial.security.cds.CDSIndexTerms;
import com.opengamma.financial.security.cds.CreditDefaultSwapIndexComponent;
import com.opengamma.financial.security.cds.CreditDefaultSwapIndexSecurity;
import com.opengamma.masterdb.security.hibernate.AbstractSecurityBeanOperation;
import com.opengamma.masterdb.security.hibernate.HibernateSecurityMasterDao;
import com.opengamma.masterdb.security.hibernate.OperationContext;
import com.opengamma.masterdb.security.hibernate.TenorBean;
import com.opengamma.util.time.Tenor;

/**
 * 
 */
public final class CreditDefaultSwapIndexSecurityBeanOperation extends AbstractSecurityBeanOperation<CreditDefaultSwapIndexSecurity, CreditDefaultSwapIndexSecurityBean> {

  /**
   * Singleton
   */
  public static final CreditDefaultSwapIndexSecurityBeanOperation INSTANCE = new CreditDefaultSwapIndexSecurityBeanOperation();
  
  private CreditDefaultSwapIndexSecurityBeanOperation() {
    super(CreditDefaultSwapIndexSecurity.SECURITY_TYPE, CreditDefaultSwapIndexSecurity.class, CreditDefaultSwapIndexSecurityBean.class);
  }
  
  @Override
  public CreditDefaultSwapIndexSecurityBean createBean(OperationContext context, HibernateSecurityMasterDao secMasterSession, CreditDefaultSwapIndexSecurity security) {

    CreditDefaultSwapIndexSecurityBean bean = new CreditDefaultSwapIndexSecurityBean();
    bean.setVersion(security.getVersion());
    bean.setSeries(security.getSeries());
    bean.setCurrency(secMasterSession.getOrCreateCurrencyBean(security.getCurrency().getCode()));
    bean.setFamily(secMasterSession.getOrCreateCDSIFamilyBean(security.getFamily()));
    
    final Set<TenorBean> tenors = bean.getTenors();
    for (Tenor tenor : security.getTerms()) {
      tenors.add(secMasterSession.getOrCreateTenorBean(tenor.getPeriod().toString()));
    }

    final Set<CDSIndexComponentBean> componentBeans = bean.getComponents();
    for (CreditDefaultSwapIndexComponent cdsiComponent : security.getComponents()) {
      CDSIndexComponentBean componentBean = new CDSIndexComponentBean();
      componentBean.setWeight(cdsiComponent.getWeight());
      componentBean.setBondId(externalIdToExternalIdBean(cdsiComponent.getBondId()));
      componentBean.setObligor(externalIdToExternalIdBean(cdsiComponent.getObligorRedCode()));
      componentBean.setName(cdsiComponent.getName());
      
      componentBeans.add(componentBean);
    }
    
    return bean;
  }

  @Override
  public CreditDefaultSwapIndexSecurity createSecurity(OperationContext context, CreditDefaultSwapIndexSecurityBean bean) {
    List<Tenor> tenors = Lists.newArrayList();
    for (TenorBean tenorBean : bean.getTenors()) {
      tenors.add(tenorBeanToTenor(tenorBean));
    }
    
    final List<CreditDefaultSwapIndexComponent> components = Lists.newArrayList();
    for (CDSIndexComponentBean cdsIndexComponentBean : bean.getComponents()) {
      components.add(cdsIndexComponentBeanToCDSIndexComponent(cdsIndexComponentBean));
    }
    
    final CreditDefaultSwapIndexSecurity security = new CreditDefaultSwapIndexSecurity(bean.getVersion(), 
        bean.getSeries(), 
        bean.getFamily().getName(), 
        currencyBeanToCurrency(bean.getCurrency()), 
        CDSIndexTerms.of(tenors), 
        CDSIndexComponentBundle.of(components));
    
    return security;
  }

}
