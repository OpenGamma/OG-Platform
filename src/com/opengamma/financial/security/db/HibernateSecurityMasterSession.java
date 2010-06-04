package com.opengamma.financial.security.db;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.security.Security;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.monitor.OperationTimer;

/**
 * 
 * 
 */
public class HibernateSecurityMasterSession {
  private static final Logger s_logger = LoggerFactory.getLogger(HibernateSecurityMasterSession.class);
  private Session _session;

  protected Session getSession() {
    return _session;
  }

  public HibernateSecurityMasterSession(Session session) {
    _session = session;
  }

  // UTILITY METHODS

  private Set<String> getListOfSchemes(
      Collection<Identifier> identifiers) {
    Set<String> schemes = new HashSet<String>();
    for (Identifier id : identifiers) {
      schemes.add(id.getScheme().getName());
    }
    return schemes;
  }

  private Set<String> getListOfValuesForScheme(String scheme,
      Collection<Identifier> identifiers) {
    Set<String> values = new HashSet<String>();
    for (Identifier id : identifiers) {
      if (id.getScheme().getName().equals(scheme)) {
        values.add(id.getValue());
      }
    }
    return values;
  }
  
  private <T extends EnumBean> T persistBean(T bean) {
    Long id = (Long) getSession().save(bean);
    getSession().flush();
    bean.setId(id);
    return bean;
  }
  
  // SESSION LEVEL METHODS

  // Exchanges
  protected ExchangeBean getOrCreateExchangeBean(String name,
      String description) {
    Query query = getSession().getNamedQuery("ExchangeBean.one");
    query.setString("name", name);
    ExchangeBean exchange = (ExchangeBean) query.uniqueResult();
    if (exchange == null) {
      exchange = persistBean(new ExchangeBean(name, description));
    } else {
      if (description != null) {
        if (exchange.getDescription() == null) {
          exchange.setDescription(description);
          getSession().saveOrUpdate(exchange);
          getSession().flush();
        }
      }
    }
    return exchange;
  }

  @SuppressWarnings("unchecked")
  protected List<ExchangeBean> getExchangeBeans() {
    Query query = getSession().getNamedQuery("ExchangeBean.all");
    return query.list();
  }

  // Currencies
  protected CurrencyBean getOrCreateCurrencyBean(String name) {
    Query query = getSession().getNamedQuery("CurrencyBean.one");
    query.setString("name", name);
    CurrencyBean currency = (CurrencyBean) query.uniqueResult();
    if (currency == null) {
      currency = persistBean(new CurrencyBean(name));
    }
    return currency;
  }

  @SuppressWarnings("unchecked")
  protected List<CurrencyBean> getCurrencyBeans() {
    Query query = getSession().getNamedQuery("CurrencyBean.all");
    return query.list();
  }

  // GICS codes
  protected GICSCodeBean getOrCreateGICSCodeBean(final String name,
      final String description) {
    Query query = getSession().getNamedQuery("GICSCodeBean.one");
    query.setString("name", name);
    GICSCodeBean gicsCode = (GICSCodeBean) query.uniqueResult();
    if (gicsCode == null) {
      gicsCode = persistBean(new GICSCodeBean(name, description));
    } else {
      if (description != null) {
        if (gicsCode.getDescription() == null) {
          gicsCode.setDescription(description);
          getSession().saveOrUpdate(gicsCode);
          getSession().flush();
        }
      }
    }
    return gicsCode;
  }

  @SuppressWarnings("unchecked")
  protected List<GICSCodeBean> getGICSCodeBeans() {
    Query query = getSession().getNamedQuery("GICSCodeBean.all");
    return query.list();
  }
  
  // Daycount conventions
  protected DayCountBean getOrCreateDayCountBean(final String convention) {
    final Query query = getSession().getNamedQuery("DayCountBean.one");
    query.setString("name", convention);
    DayCountBean bean = (DayCountBean) query.uniqueResult();
    if (bean == null) {
      bean = persistBean(new DayCountBean(convention));
    }
    return bean;
  }
  
  @SuppressWarnings ("unchecked")
  protected List<DayCountBean> getDayCountBeans() {
    final Query query = getSession().getNamedQuery("DayCountBean.all");
    return query.list();
  }
  
  // Business day conventions
  protected BusinessDayConventionBean getOrCreateBusinessDayConventionBean(final String convention) {
    final Query query = getSession().getNamedQuery("BusinessDayConventionBean.one");
    query.setString("name", convention);
    BusinessDayConventionBean bean = (BusinessDayConventionBean) query.uniqueResult();
    if (bean == null) {
      bean = persistBean(new BusinessDayConventionBean(convention));
    }
    return bean;
  }

  @SuppressWarnings ("unchecked")
  protected List<BusinessDayConventionBean> getBusinessDayConventionBeans() {
    final Query query = getSession().getNamedQuery("BusinessDayConventionBean.all");
    return query.list();
  }

  // Frequencies
  protected FrequencyBean getOrCreateFrequencyBean(final String convention) {
    final Query query = getSession().getNamedQuery("FrequencyBean.one");
    query.setString("name", convention);
    FrequencyBean bean = (FrequencyBean) query.uniqueResult();
    if (bean == null) {
      bean = persistBean(new FrequencyBean(convention));
    }
    return bean;
  }

  @SuppressWarnings ("unchecked")
  protected List<FrequencyBean> getFrequencyBeans() {
    final Query query = getSession().getNamedQuery("FrequencyBean.all");
    return query.list();
  }
  
  // CommodityFutureTypes
  
  protected CommodityFutureTypeBean getOrCreateCommodityFutureTypeBean(final String type) {
    final Query query = getSession().getNamedQuery("CommodityFutureTypeBean.one");
    query.setString("name", type);
    CommodityFutureTypeBean bean = (CommodityFutureTypeBean) query.uniqueResult();
    if (bean == null) {
      bean = persistBean(new CommodityFutureTypeBean(type));
    }
    return bean;
  }
  
  // BondFutureType
  
  protected BondFutureTypeBean getOrCreateBondFutureTypeBean(final String type) {
    final Query query = getSession().getNamedQuery("BondFutureTypeBean.one");
    query.setString("name", type);
    BondFutureTypeBean bean = (BondFutureTypeBean) query.uniqueResult();
    if (bean == null) {
      bean = persistBean(new BondFutureTypeBean(type));
    }
    return bean;
  }
  
  // UnitName
  
  protected UnitBean getOrCreateUnitNameBean(final String unitName) {
    final Query query = getSession().getNamedQuery("UnitBean.one");
    query.setString("name", unitName);
    UnitBean bean = (UnitBean) query.uniqueResult();
    if (bean == null) {
      bean = persistBean(new UnitBean(unitName));
    }
    return bean;
  }
  
  // CashRateType
  
  protected CashRateTypeBean getOrCreateCashRateTypeBean(final String type) {
    final Query query = getSession().getNamedQuery("CashRateTypeBean.one");
    query.setString("name", type);
    CashRateTypeBean bean = (CashRateTypeBean) query.uniqueResult();
    if (bean == null) {
      bean = persistBean(new CashRateTypeBean(type));
    }
    return bean;
  }
  
  // IssuerTypeBean
  
  protected IssuerTypeBean getOrCreateIssuerTypeBean(final String type) {
    final Query query = getSession().getNamedQuery("IssuerTypeBean.one");
    query.setString("name", type);
    IssuerTypeBean bean = (IssuerTypeBean) query.uniqueResult();
    if (bean == null) {
      bean = persistBean(new IssuerTypeBean(type));
    }
    return bean;
  }
  
  // MarketBean
  
  protected MarketBean getOrCreateMarketBean(final String market) {
    final Query query = getSession().getNamedQuery("MarketBean.one");
    query.setString("name", market);
    MarketBean bean = (MarketBean) query.uniqueResult();
    if (bean == null) {
      bean = persistBean(new MarketBean(market));
    }
    return bean;
  }
  
  // YieldConventionBean
  
  protected YieldConventionBean getOrCreateYieldConventionBean(final String convention) {
    final Query query = getSession().getNamedQuery("YieldConventionBean.one");
    query.setString("name", convention);
    YieldConventionBean bean = (YieldConventionBean) query.uniqueResult();
    if (bean == null) {
      bean = persistBean(new YieldConventionBean(convention));
    }
    return bean;
  }
  
  // GuaranteeTypeBean
  
  protected GuaranteeTypeBean getOrCreateGuaranteeTypeBean(final String type) {
    final Query query = getSession().getNamedQuery("GuaranteeTypeBean.one");
    query.setString("name", type);
    GuaranteeTypeBean bean = (GuaranteeTypeBean) query.uniqueResult();
    if (bean == null) {
      bean = persistBean(new GuaranteeTypeBean(type));
    }
    return bean;
  }
  
  // CouponTypeBean
  
  protected CouponTypeBean getOrCreateCouponTypeBean(final String type) {
    final Query query = getSession().getNamedQuery("CouponTypeBean.one");
    query.setString("name", type);
    CouponTypeBean bean = (CouponTypeBean) query.uniqueResult();
    if (bean == null) {
      bean = persistBean(new CouponTypeBean(type));
    }
    return bean;
  }
  
  // Identifiers
  
  private IdentifierAssociationBean createIdentifierAssociationBean(Date now, String scheme, String identifier, SecurityBean security) {
    final IdentifierAssociationBean association = new IdentifierAssociationBean(security, new IdentifierBean(scheme, identifier));
    final Transaction transaction = getSession().beginTransaction();
    try {
      transaction.begin();
      Query query = getSession().getNamedQuery("IdentifierAssociationBean.one.previousAssociation");
      query.setString("scheme", scheme);
      query.setString("identifier", identifier);
      query.setDate("now", now);
      IdentifierAssociationBean other = (IdentifierAssociationBean) query.uniqueResult();
      if (other != null) {
        association.setValidStartDate(other.getValidEndDate());
      }
      query = getSession().getNamedQuery("IdentifierAssociationBean.one.nextAssociation");
      query.setString("scheme", scheme);
      query.setString("identifier", identifier);
      query.setDate("now", now);
      other = (IdentifierAssociationBean) query.uniqueResult();
      if (other != null) {
        association.setValidEndDate(other.getValidEndDate());
      }
      Long id = (Long) getSession().save(association);
      association.setId(id);
      transaction.commit();
    } catch (Exception e) {
      transaction.rollback();
      throw new OpenGammaRuntimeException("transaction rolled back", e);
    }
    getSession().flush();
    return association;
  }
  
  protected IdentifierAssociationBean getCreateOrUpdateIdentifierAssociationBean(
      Date now, String scheme, String identifier, SecurityBean security) {
    Query query = getSession().getNamedQuery(
        "IdentifierAssociationBean.one.byDateIdentifier");
    query.setString("scheme", scheme);
    query.setString("identifier", identifier);
    query.setDate("now", now);
    IdentifierAssociationBean association = (IdentifierAssociationBean) query.uniqueResult();
    if (association == null) {
      association = createIdentifierAssociationBean(now, scheme, identifier, security);
    } else {
      if (!association.getSecurity().getId().equals(security.getId())) {
        // terminate the previous record, and create a new one
        association.setValidEndDate(now);
        getSession().update(association);
        getSession().flush();
        association = createIdentifierAssociationBean(now, scheme, identifier, security);
      }
    }
    return association;
  }

  // only for testing.
  @SuppressWarnings ("unchecked")
  protected List<IdentifierAssociationBean> getAllAssociations() {
    Query query = getSession().createQuery(
        "from IdentifierAssociationBean as d");
    return query.list();
  }

  protected void associateOrUpdateIdentifierWithSecurity(Date now,
      Identifier identifier, SecurityBean security) {
    getCreateOrUpdateIdentifierAssociationBean(now, identifier
        .getScheme().getName(), identifier.getValue(), security
        .getFirstVersion());
  }
  
  // Generic Securities

  protected SecurityBean getSecurityBean(final UniqueIdentifier uid) {
    if (uid.isLatest()) {
      return getSecurityBean(new Date(), uid);
    }
    Query query = getSession().getNamedQuery("SecurityBean.one.byUid");
    query.setLong("securityUid", Long.valueOf(uid.getVersion()));
    SecurityBean security = (SecurityBean) query.uniqueResult();
    return security;
  }

  protected SecurityBean getSecurityBean(Date now, final UniqueIdentifier uid) {
    Query query = getSession().getNamedQuery("SecurityBean.one.byDateOid");
    query.setLong("securityOid", Long.valueOf(uid.getValue()));
    query.setDate("now", now);
    SecurityBean security = (SecurityBean) query.uniqueResult();
    return security;
  }

  protected SecurityBean getSecurityBean(Date now, IdentifierBundle bundle) {
    Collection<Identifier> identifiers = bundle.getIdentifiers();
    Set<String> schemes = getListOfSchemes(identifiers);
    for (String scheme : schemes) {
      final Set<String> ids = getListOfValuesForScheme(scheme, identifiers);
      Query query = getSession().getNamedQuery("SecurityBean.one.byDateIdentifiers");
      query.setString("scheme", scheme);
      query.setParameterList("identifiers", ids);
      query.setDate("now", now);
      SecurityBean security = (SecurityBean) query.uniqueResult();
      if (security != null) {
        return security;
      }
    }
    return null; // none of the dsid's matched.
  }
  
  // Specific securities through BeanOperation
  
  protected <S extends Security, SBean extends SecurityBean> SBean createSecurityBean(
      final BeanOperation<S, SBean> beanOperation,
      final Date effectiveDateTime,
      final boolean deleted,
      final Date lastModified,
      final String modifiedBy,
      final SBean firstVersion,
      final S security) {
    final SBean bean = beanOperation.createBean(this, security);
    persistSecurityBean(effectiveDateTime, deleted, lastModified, modifiedBy, firstVersion, security.getName(), bean);
    beanOperation.postPersistBean(this, effectiveDateTime, bean);
    return bean;
  }
  
  protected void persistSecurityBean(
      final Date effectiveDateTime,
      final boolean deleted,
      final Date lastModified,
      final String modifiedBy,
      final SecurityBean firstVersion,
      final String displayName,
      final SecurityBean bean) {
    // base properties
    bean.setEffectiveDateTime(effectiveDateTime);
    bean.setDeleted(deleted);
    bean.setLastModifiedDateTime(lastModified);
    bean.setLastModifiedBy(modifiedBy);
    bean.setDisplayName(displayName);
    // first version
    bean.setFirstVersion(firstVersion);
    if (firstVersion == null) {
      // link to itself as a parent
      final Transaction transaction = getSession().beginTransaction();
      try {
        transaction.begin();
        final Long id = (Long) getSession().save(bean);
        bean.setId(id);
        bean.setFirstVersion(bean);
        getSession().update(bean);
        transaction.commit();
      } catch (Exception e) {
        transaction.rollback();
        throw new OpenGammaRuntimeException("transaction rolled back", e);
      }
    } else {
      final Long id = (Long) getSession().save(bean);
      bean.setId(id);
    }    
    getSession().flush();
  }

  // Equities

  // Internal query methods for equities
  @SuppressWarnings("unchecked")
  protected List<EquitySecurityBean> getEquitySecurityBeans() {
    Query query = getSession().getNamedQuery("EquitySecurityBean.all");
    return query.list();
  }

  @SuppressWarnings("unchecked")
  protected List<EquitySecurityBean> getAllVersionsOfEquitySecurityBean(
      EquitySecurityBean firstVersion) {
    Query query = getSession().getNamedQuery(
        "EquitySecurityBean.many.allVersionsByFirstVersion");
    query.setParameter("firstVersion", firstVersion);
    return query.list();
  }

  protected EquitySecurityBean getCurrentEquitySecurityBean(Date now,
      ExchangeBean exchange, String companyName, CurrencyBean currency) {
    Query query = getSession().getNamedQuery(
        "EquitySecurityBean.one.byExchangeCompanyNameCurrencyDate");
    query.setDate("now", now);
    query.setParameter("exchange", exchange);
    query.setString("companyName", companyName);
    query.setParameter("currency", currency);
    return (EquitySecurityBean) query.uniqueResult();
  }

  protected EquitySecurityBean getCurrentEquitySecurityBean(Date now,
      EquitySecurityBean firstVersion) {
    Query query = getSession().getNamedQuery(
        "EquitySecurityBean.one.byFirstVersionDate");
    query.setParameter("firstVersion", firstVersion);
    query.setDate("now", now);
    return (EquitySecurityBean) query.uniqueResult();
  }

  protected EquitySecurityBean getCurrentLiveEquitySecurityBean(Date now,
      ExchangeBean exchange, String companyName, CurrencyBean currency) {
    Query query = getSession().getNamedQuery(
        "EquitySecurityBean.one.liveByExchangeCompanyNameCurrencyDate");
    query.setParameter("exchange", exchange);
    query.setString("companyName", companyName);
    query.setParameter("currency", currency);
    query.setDate("now", now);
    return (EquitySecurityBean) query.uniqueResult();
  }

  protected EquitySecurityBean getCurrentLiveEquitySecurityBean(Date now,
      EquitySecurityBean firstVersion) {
    Query query = getSession().getNamedQuery(
        "EquitySecurityBean.one.liveByFirstVersionDate");
    query.setParameter("firstVersion", firstVersion);
    query.setDate("now", now);
    return (EquitySecurityBean) query.uniqueResult();
  }

  // Equity options

  @SuppressWarnings("unchecked")
  protected List<OptionSecurityBean> getEquityOptionSecurityBeans() {
    Query query = getSession().getNamedQuery("EquityOptionSecurityBean.all");
    return query.list();
  }
  
  // Futures
  
  @SuppressWarnings("unchecked")
  protected List<FutureBundleBean> getFutureBundleBeans(Date now, FutureSecurityBean future) {
    Query query;
    if (now != null) {
      query = getSession().getNamedQuery("FutureBundleBean.many.byDateFuture");
      query.setDate("now", now);
    } else {
      query = getSession().getNamedQuery("FutureBundleBean.many.byFuture");
    }
    query.setParameter("future", future);
    return query.list();
  }
  
  protected FutureBundleBean nextFutureBundleBean(Date now, FutureSecurityBean future) {
    Query query = getSession().getNamedQuery("FutureBundleBean.one.nextBundle");
    query.setDate("now", now);
    query.setParameter("future", future);
    return (FutureBundleBean) query.uniqueResult();
  }
  
  protected void persistFutureBundleBeans(final Date now, final FutureSecurityBean future) {
    OperationTimer timer = new OperationTimer(s_logger, "persistFutureBundleBeans");
    final Set<FutureBundleBean> beanBasket = future.getBasket();
    final List<FutureBundleBean> dbBasket = getFutureBundleBeans(now, future);
    if (now != null) {
      // anything in the database (at this timestamp), but not in the basket must be "terminated" at this timestamp
      boolean beansUpdated = false;
      for (FutureBundleBean dbBundle : dbBasket) {
        if (!beanBasket.contains(dbBundle)) {
          dbBundle.setEndDate(now);
          getSession().update(dbBundle);
          beansUpdated = true;
        }
      }
      if (beansUpdated) {
        getSession().flush();
        beansUpdated = false;
      }
      // anything not in the database (at this timestamp), but in the basket must be added:
      for (FutureBundleBean beanBundle : beanBasket) {
        if (!dbBasket.contains(beanBundle)) {
          final FutureBundleBean next = nextFutureBundleBean(now, future);
          if (next != null) {
            beanBundle.setId(next.getId());
            beanBundle.setEndDate(next.getEndDate());
            next.setStartDate(now);
            getSession().update(next);
          } else {
            beanBundle.setStartDate(now);
            beanBundle.setEndDate(null);
            if (beanBundle.getId() != null) {
              getSession().update(beanBundle);
            } else {
              Long id = (Long) getSession().save(beanBundle);
              beanBundle.setId(id);
            }
          }
          beansUpdated = true;
        }
      }
      if (beansUpdated) {
        getSession().flush();
      }
    } else {
      // anything in the database with any timestamp that isn't null/null must be deleted
      // anything in the database, but not in the basket, must be deleted
      boolean beansUpdated = false;
      for (FutureBundleBean dbBundle : dbBasket) {
        if (!beanBasket.contains(dbBundle)) {
          getSession().delete(dbBundle);
          beansUpdated = true;
        } else if ((dbBundle.getStartDate() != null) || (dbBundle.getEndDate() != null)) {
          dbBundle.setStartDate(null);
          dbBundle.setEndDate(null);
          getSession().update(dbBundle);
          beansUpdated = true;
        }
      }
      // anything not in the database, but in the basket, must be added (null/null)
      for (FutureBundleBean beanBundle : beanBasket) {
        if (!dbBasket.contains(beanBundle)) {
          beanBundle.setStartDate(null);
          beanBundle.setEndDate(null);
          if (beanBundle.getId() != null) {
            getSession().update(beanBundle);
          } else {
            Long id = (Long) getSession().save(beanBundle);
            beanBundle.setId(id);
          }
          beansUpdated = true;
        }
      }
      if (beansUpdated) {
        getSession().flush();
      }
    }
    timer.finished();
  }
  
}
