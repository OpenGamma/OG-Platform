/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.blotter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.joda.beans.Bean;
import org.joda.beans.BeanBuilder;
import org.joda.beans.MetaBean;
import org.joda.beans.MetaProperty;

import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalScheme;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.master.security.ManageableSecurityLink;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
/* package */ abstract class OtcTradeBuilder {

  // TODO where should these live?
  private static final ExternalScheme CPTY_SCHEME = ExternalScheme.of("Cpty");
  private static final String COUNTERPARTY = "counterparty";

  private final SecurityMaster _securityMaster;
  private final PositionMaster _positionMaster;
  private final MetaBeanFactory _metaBeanFactory;

  /* package */ OtcTradeBuilder(SecurityMaster securityMaster, PositionMaster positionMaster, Set<MetaBean> metaBeans) {
    ArgumentChecker.notNull(securityMaster, "securityManager");
    ArgumentChecker.notNull(positionMaster, "positionMaster");
    ArgumentChecker.notEmpty(metaBeans, "metaBeans");
    _positionMaster = positionMaster;
    _securityMaster = securityMaster;
    _metaBeanFactory = new MapMetaBeanFactory(metaBeans);
  }

  /* package */ UniqueId buildTrade(BeanDataSource tradeData, BeanDataSource securityData, BeanDataSource underlyingData) {
    ObjectId securityId = buildSecurity(securityData, underlyingData).getObjectId();
    ManageableTrade.Meta meta = ManageableTrade.meta();
    BeanBuilder<? extends ManageableTrade> tradeBuilder =
        tradeBuilder(tradeData,
                     meta.tradeDate(),
                     meta.tradeTime(),
                     meta.premium(),
                     meta.premiumCurrency(),
                     meta.premiumDate(),
                     meta.premiumTime());
    tradeBuilder.set(meta.attributes(), tradeData.getMapValues(meta.attributes().name()));
    tradeBuilder.set(meta.quantity(), BigDecimal.ONE);
    tradeBuilder.set(meta.securityLink(), new ManageableSecurityLink(securityId));
    // TODO set counterparty on trade, scheme Cpty
    String counterparty = tradeData.getValue(COUNTERPARTY);
    if (!StringUtils.isEmpty(counterparty)) {
      tradeBuilder.set(meta.counterpartyExternalId(), ExternalId.of(CPTY_SCHEME, counterparty));
    }
    ManageableTrade trade = tradeBuilder.build();
    // TODO need the node ID so we can add the position to it
    ManageablePosition position = new ManageablePosition();
    position.setSecurityLink(new ManageableSecurityLink(securityId));
    position.addTrade(trade);
    position.setQuantity(trade.getQuantity());
    ManageablePosition savedPosition = savePosition(position);
    List<ManageableTrade> trades = savedPosition.getTrades();
    ManageableTrade savedTrade = trades.get(0);
    return savedTrade.getUniqueId();
  }

  private BeanBuilder<? extends ManageableTrade> tradeBuilder(BeanDataSource tradeData, MetaProperty<?>... properties) {
    BeanBuilder<? extends ManageableTrade> builder = ManageableTrade.meta().builder();
    for (MetaProperty<?> property : properties) {
      // TODO custom converters needed for some properties? OffsetDate?
      builder.setString(property, tradeData.getValue(property.name()));
    }
    return builder;
  }

  /**
   * Saves a security to the security master.
   * @param security The security
   * @return The saved security
   */
  /* package */ abstract ManageableSecurity saveSecurity(ManageableSecurity security);

  /**
   * Saves a position to the position master.
   * @param position The position
   * @return The saved position
   */
  /* package */ abstract ManageablePosition savePosition(ManageablePosition position);

  private UniqueId buildSecurity(BeanDataSource securityData, BeanDataSource underlyingData) {
    ExternalId underlyingId = buildUnderlying(underlyingData);
    BeanDataSource dataSource;
    if (underlyingId != null) {
      dataSource = new PropertyReplacingDataSource(securityData, "underlyingId", underlyingId.toString());
    } else {
      dataSource = securityData;
    }
    FinancialSecurity security = build(dataSource, FinancialSecurity.class);
    ManageableSecurity savedSecurity = saveSecurity(security);
    return savedSecurity.getUniqueId();
  }

  private ExternalId buildUnderlying(BeanDataSource underlyingData) {
    if (underlyingData == null) {
      return null;
    }
    FinancialSecurity underlying = build(underlyingData, FinancialSecurity.class);
    saveSecurity(underlying);
    ExternalId underlyingId = underlying.accept(new ExternalIdVisitor(_securityMaster));
    if (underlyingId == null) {
      throw new IllegalArgumentException("Unable to get external ID of underlying security " + underlying);
    }
    return underlyingId;
  }

  @SuppressWarnings("unchecked")
  private <T extends Bean> T build(BeanDataSource data, Class<T> expectedType) {
    // TODO custom converters for dates
    // default timezone based on OpenGammaClock
    // default times for effectiveDate and maturityDate properties on SwapSecurity, probably others
    // TODO decorator to filter out trade properties
    BeanVisitor<Bean> visitor = new BeanBuildingVisitor<Bean>(data, _metaBeanFactory);
    MetaBean metaBean = _metaBeanFactory.beanFor(data);
    Object bean = new BeanTraverser().traverse(metaBean, visitor);
    if (!expectedType.isAssignableFrom(bean.getClass())) {
      throw new IllegalArgumentException("object type " + bean.getClass().getName() + " doesn't conform to " +
                                             "the expected type " + expectedType.getName());
    }
    return expectedType.cast(bean);
  }

  /* package */  SecurityMaster getSecurityMaster() {
    return _securityMaster;
  }

  /* package */ PositionMaster getPositionMaster() {
    return _positionMaster;
  }
}
