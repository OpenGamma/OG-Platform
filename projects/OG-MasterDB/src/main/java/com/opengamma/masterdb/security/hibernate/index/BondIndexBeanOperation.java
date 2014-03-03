/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */

package com.opengamma.masterdb.security.hibernate.index;

import static com.opengamma.masterdb.security.hibernate.Converters.externalIdBeanToExternalId;
import static com.opengamma.masterdb.security.hibernate.Converters.externalIdToExternalIdBean;
import static com.opengamma.masterdb.security.hibernate.Converters.indexWeightingTypeBeanToIndexWeightingType;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.security.index.BondIndex;
import com.opengamma.financial.security.index.BondIndexComponent;
import com.opengamma.financial.security.index.IndexWeightingType;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.masterdb.security.hibernate.AbstractSecurityBeanOperation;
import com.opengamma.masterdb.security.hibernate.ExternalIdBean;
import com.opengamma.masterdb.security.hibernate.HibernateSecurityMasterDao;
import com.opengamma.masterdb.security.hibernate.IndexWeightingTypeBean;
import com.opengamma.masterdb.security.hibernate.OperationContext;

/**
 * Hibernate bean/security conversion operations.
 */
public final class BondIndexBeanOperation extends AbstractSecurityBeanOperation<BondIndex, BondIndexBean> {

  /**
   * Singleton instance.
   */
  public static final BondIndexBeanOperation INSTANCE = new BondIndexBeanOperation();

  private BondIndexBeanOperation() {
    super(BondIndex.INDEX_TYPE, BondIndex.class, BondIndexBean.class);
  }

  @Override
  public BondIndexBean createBean(final OperationContext context, HibernateSecurityMasterDao secMasterSession, BondIndex index) {
    final BondIndexBean bean = new BondIndexBean();
    bean.setDescription(index.getDescription());
    List<BondIndexComponent> bondComponents = index.getBondComponents();
    List<BondIndexComponentBean> bondComponentBeans = new ArrayList<>();
    long i = 0;
    for (BondIndexComponent bondComponent : bondComponents) {
      BondIndexComponentBean bondComponentBean = new BondIndexComponentBean();
      bondComponentBean.setWeight(bondComponent.getWeight());
      Set<ExternalIdBean> idBundle = new HashSet<>();
      for (ExternalId id : bondComponent.getBondIdentifier().getExternalIds()) {
        idBundle.add(externalIdToExternalIdBean(id));
      }
      bondComponentBean.setIdentifiers(idBundle);   
      bondComponentBean.setPosition(i);
      i++;
      bondComponentBeans.add(bondComponentBean);
    }
    bean.setBondComponents(bondComponentBeans);
    IndexWeightingTypeBean indexWeightingTypeBean = secMasterSession.getOrCreateIndexWeightingTypeBean(index.getWeightingType().name());
    bean.setWeightingType(indexWeightingTypeBean);
    if (index.getIndexFamilyId() != null) {
      bean.setIndexFamilyId(externalIdToExternalIdBean(index.getIndexFamilyId()));
    }
    return bean;
  }
  
  @Override
  public BondIndexBean resolve(final OperationContext context,
                                 final HibernateSecurityMasterDao secMasterSession, final Date now,
                                 final BondIndexBean bean) {
    final List<BondIndexComponentBean> indexComponents = secMasterSession.getBondIndexComponentBeans(bean);
    bean.setBondComponents(new ArrayList<BondIndexComponentBean>(indexComponents));
    return bean;
  }
  
  @Override
  public void postPersistBean(final OperationContext context,
      final HibernateSecurityMasterDao secMasterSession, final Date now,
      final BondIndexBean bean) {
    secMasterSession.persistBondIndexComponentBeans(bean);
  }

  @Override
  public BondIndex createSecurity(final OperationContext context, BondIndexBean bean) {
    String description = bean.getDescription();
    IndexWeightingType weightingType = indexWeightingTypeBeanToIndexWeightingType(bean.getWeightingType());
    List<BondIndexComponentBean> bondComponents = bean.getBondComponents();
    if (bondComponents == null) {
      throw new OpenGammaRuntimeException("null returned by getBondComponents, which breaks contract.");
    }
    List<BondIndexComponent> components = new ArrayList<>();
    for (BondIndexComponentBean component : bondComponents) {
      Set<ExternalIdBean> identifiers = component.getIdentifiers();
      List<ExternalId> ids = new ArrayList<>();
      for (ExternalIdBean idBean : identifiers) {
        ExternalId externalId = externalIdBeanToExternalId(idBean);
        ids.add(externalId);
      }
      ExternalIdBundle externalIdBundle = ExternalIdBundle.of(ids);
      BigDecimal weight = component.getWeight().stripTrailingZeros();
      BondIndexComponent bondIndexComponent = new BondIndexComponent(externalIdBundle, weight);
      components.add(bondIndexComponent);
    }
    BondIndex bondIndex = new BondIndex("", description, components, weightingType);
    if (bean.getIndexFamilyId() != null) {
      bondIndex.setIndexFamilyId(externalIdBeanToExternalId(bean.getIndexFamilyId()));
    }
    return bondIndex;
  }

}
