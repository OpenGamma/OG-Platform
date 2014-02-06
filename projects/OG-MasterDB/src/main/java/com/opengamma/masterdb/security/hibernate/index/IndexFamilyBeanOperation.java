/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */

package com.opengamma.masterdb.security.hibernate.index;

import static com.opengamma.masterdb.security.hibernate.Converters.externalIdBeanToExternalId;
import static com.opengamma.masterdb.security.hibernate.Converters.externalIdToExternalIdBean;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import com.opengamma.financial.security.index.IndexFamily;
import com.opengamma.id.ExternalId;
import com.opengamma.masterdb.security.hibernate.AbstractSecurityBeanOperation;
import com.opengamma.masterdb.security.hibernate.HibernateSecurityMasterDao;
import com.opengamma.masterdb.security.hibernate.OperationContext;
import com.opengamma.util.time.Tenor;

/**
 * Hibernate bean/security conversion operations.
 */
public final class IndexFamilyBeanOperation extends AbstractSecurityBeanOperation<IndexFamily, IndexFamilyBean> {

  /**
   * Singleton instance.
   */
  public static final IndexFamilyBeanOperation INSTANCE = new IndexFamilyBeanOperation();

  private IndexFamilyBeanOperation() {
    super(IndexFamily.METADATA_TYPE, IndexFamily.class, IndexFamilyBean.class);
  }

  @Override
  public IndexFamilyBean createBean(final OperationContext context, HibernateSecurityMasterDao secMasterSession, IndexFamily index) {
    final IndexFamilyBean bean = new IndexFamilyBean();
    SortedMap<Tenor, ExternalId> members = index.getMembers();
    Set<IndexFamilyEntryBean> entries = new HashSet<>();
    for (Map.Entry<Tenor, ExternalId> entry : members.entrySet()) {
      IndexFamilyEntryBean indexFamilyEntryBean = new IndexFamilyEntryBean();
      indexFamilyEntryBean.setTenor(secMasterSession.getOrCreateTenorBean(entry.getKey().toFormattedString()));
      indexFamilyEntryBean.setIdentifier(externalIdToExternalIdBean(entry.getValue()));
      entries.add(indexFamilyEntryBean);      
    }
    bean.setEntries(entries);
    return bean;
  }

  @Override
  public IndexFamily createSecurity(final OperationContext context, IndexFamilyBean bean) {
    Set<IndexFamilyEntryBean> entries = bean.getEntries();
    IndexFamily indexFamily = new IndexFamily();
    SortedMap<Tenor, ExternalId> map = new TreeMap<>(); // these get ordered when inserting to the indexFamily
    for (IndexFamilyEntryBean entry : entries) {
      Tenor tenor = Tenor.parse(entry.getTenor().getName());
      map.put(tenor, externalIdBeanToExternalId(entry.getIdentifier()));
    }
    indexFamily.setMembers(map);
    return indexFamily;
  }

}
