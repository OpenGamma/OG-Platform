/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.blotter;

import java.util.Set;

import org.joda.beans.MetaBean;

import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.util.ArgumentChecker;

/**
 * TODO should this be called TradeBuilder if it's going to build the trade, security, underlying and possibly position
 * TODO how should portfolio data be handled. BeanDataSource doesn't seem like the right thing, it's not a full bean
 */
/* package */ abstract class SecurityBuilder {

  private final BeanDataSource _securityData;
  private final BeanDataSource _underlyingData;
  private final SecurityMaster _securityMaster;
  private final MetaBeanFactory _metaBeanFactory;

  /* package */ SecurityBuilder(BeanDataSource securityData,
                                BeanDataSource underlyingData,
                                SecurityMaster securityMaster,
                                Set<MetaBean> metaBeans) {
    ArgumentChecker.notNull(securityData, "securityData");
    ArgumentChecker.notNull(securityMaster, "securityManager");
    ArgumentChecker.notEmpty(metaBeans, "metaBeans");
    _securityData = securityData;
    _underlyingData = underlyingData;
    _securityMaster = securityMaster;
    _metaBeanFactory = new MapMetaBeanFactory(metaBeans);
  }

  /**
   * Saves a security to the security master.
   * @param document The security document
   * @return A document containing the saved security
   */
  /* package */ abstract SecurityDocument save(SecurityDocument document);

  private ExternalId buildUnderlying() {
    if (_underlyingData == null) {
      return null;
    }
    ManageableSecurity underlyingSecurity = build(_underlyingData);
    if (!(underlyingSecurity instanceof FinancialSecurity)) {
      throw new IllegalArgumentException("Can only create underlying securities that extend FinancialSecurity");
    }
    FinancialSecurity underlying = (FinancialSecurity) underlyingSecurity;
    save(new SecurityDocument(underlying));
    ExternalId underlyingId = underlying.accept(new ExternalIdVisitor(_securityMaster));
    if (underlyingId == null) {
      throw new IllegalArgumentException("Unable to get external ID of underlying security " + underlying);
    }
    return underlyingId;
  }

  /* package */ UniqueId buildSecurity() {
    ExternalId underlyingId = buildUnderlying();
    BeanDataSource dataSource;
    if (underlyingId != null) {
      dataSource = new PropertyReplacingDataSource(_securityData, "underlyingId", underlyingId.toString());
    } else {
      dataSource = _securityData;
    }
    ManageableSecurity security = build(dataSource);
    SecurityDocument document = save(new SecurityDocument(security));
    return document.getUniqueId();
  }

  private ManageableSecurity build(BeanDataSource data) {
    // TODO custom converters for dates
    // default timezone based on OpenGammaClock
    // default times for effectiveDate and maturityDate properties on SwapSecurity, probably others
    BeanVisitor<ManageableSecurity> visitor = new BeanBuildingVisitor<ManageableSecurity>(data, _metaBeanFactory);
    return new BeanTraverser().traverse(_metaBeanFactory.beanFor(data), visitor);
  }

  /* package */  SecurityMaster getSecurityMaster() {
    return _securityMaster;
  }
}

/**
 * Builds a security and saves it as a new version of an existing security. The security data must contain a
 * unique ID.
 */
/* package */ class ExistingSecurityBuilder extends SecurityBuilder {

  /**
   * Saves a security using {@link SecurityMaster#update}. The security data must contain a unique ID.
   * @param document The security document
   * @return A document containing the updated security
   */
  @Override
  SecurityDocument save(SecurityDocument document) {
    return getSecurityMaster().update(document);
  }

  /* package */ ExistingSecurityBuilder(BeanDataSource securityData,
                                        BeanDataSource underlyingData,
                                        SecurityMaster securityMaster,
                                        Set<MetaBean> metaBeans) {
    super(securityData, underlyingData, securityMaster, metaBeans);
  }
}

/**
 * Builds a security and adds it to the security master as a new security. The security data must not contain a
 * unique ID.
 */
/* package */ class NewSecurityBuilder extends SecurityBuilder {

  /**
   * Saves a security using {@link SecurityMaster#add}. The security data must not contain a unique ID.
   * @param document The security document
   * @return A document containing the saved security.
   */
  @Override
  SecurityDocument save(SecurityDocument document) {
    return getSecurityMaster().add(document);
  }

  /* package */ NewSecurityBuilder(BeanDataSource securityData,
                                   BeanDataSource underlyingData,
                                   SecurityMaster securityMaster,
                                   Set<MetaBean> metaBeans) {
    super(securityData, underlyingData, securityMaster, metaBeans);
  }
}
