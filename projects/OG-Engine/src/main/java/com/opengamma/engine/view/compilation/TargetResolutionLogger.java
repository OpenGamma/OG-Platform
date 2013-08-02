/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.compilation;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.LocalDate;
import org.threeten.bp.OffsetTime;

import com.opengamma.core.position.Counterparty;
import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.Trade;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecurityLink;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetResolver;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.MemoryUtils;
import com.opengamma.engine.target.ComputationTargetReference;
import com.opengamma.engine.target.ComputationTargetReferenceVisitor;
import com.opengamma.engine.target.ComputationTargetRequirement;
import com.opengamma.engine.target.ComputationTargetSpecificationResolver;
import com.opengamma.engine.target.ComputationTargetSpecificationResolver.AtVersionCorrection;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.target.ComputationTargetTypeMap;
import com.opengamma.engine.target.ComputationTargetTypeVisitor;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.id.VersionCorrection;
import com.opengamma.lambdava.functions.Function2;
import com.opengamma.util.money.Currency;

/**
 * Wraps an existing specification resolver to log all of the resolutions calls. This allows any values that were considered during the compilation to be recorded and returned as part of the compiled
 * context. If the resolution of any of these would be different for a different version/correction then this compilation is no longer valid.
 */
/* package */final class TargetResolutionLogger implements ComputationTargetResolver.AtVersionCorrection {

  private static final Logger s_logger = LoggerFactory.getLogger(TargetResolutionLogger.class);

  private static final class LoggingSpecificationResolver implements ComputationTargetSpecificationResolver.AtVersionCorrection, ComputationTargetReferenceVisitor<ComputationTargetReference> {

    private final ComputationTargetSpecificationResolver.AtVersionCorrection _underlying;
    private final ConcurrentMap<ComputationTargetReference, UniqueId> _resolutions;
    private final Set<UniqueId> _expiredResolutions;

    private LoggingSpecificationResolver(final ComputationTargetSpecificationResolver.AtVersionCorrection underlying, final ConcurrentMap<ComputationTargetReference, UniqueId> resolutions,
        final Set<UniqueId> expiredResolutions) {
      _underlying = underlying;
      _resolutions = resolutions;
      _expiredResolutions = expiredResolutions;
    }

    private static final ComputationTargetTypeVisitor<Void, ComputationTargetType> s_getLeafType = new ComputationTargetTypeVisitor<Void, ComputationTargetType>() {

      @Override
      public ComputationTargetType visitMultipleComputationTargetTypes(final Set<ComputationTargetType> types, final Void data) {
        final ComputationTargetType[] result = new ComputationTargetType[types.size()];
        int i = 0;
        boolean different = false;
        for (final ComputationTargetType type : types) {
          final ComputationTargetType leafType = type.accept(this, null);
          if (leafType != null) {
            result[i++] = leafType;
            different = true;
          } else {
            result[i++] = type;
          }
        }
        if (different) {
          return ComputationTargetType.multiple(result);
        } else {
          return null;
        }
      }

      @Override
      public ComputationTargetType visitNestedComputationTargetTypes(final List<ComputationTargetType> types, final Void data) {
        final ComputationTargetType leafType = types.get(types.size() - 1);
        final ComputationTargetType newLeafType = leafType.accept(this, null);
        if (newLeafType != null) {
          return newLeafType;
        } else {
          return leafType;
        }
      }

      @Override
      public ComputationTargetType visitNullComputationTargetType(final Void data) {
        return null;
      }

      @Override
      public ComputationTargetType visitClassComputationTargetType(final Class<? extends UniqueIdentifiable> type, final Void data) {
        return null;
      }

    };

    private ComputationTargetType getLeafType(final ComputationTargetType type) {
      // TODO: Ought to reduce the type to its simplest form. This hasn't been a problem with the views & function repository I've been testing
      // with but might be necessary in the general case as there will be duplicate values in the resolver cache.
      return type.accept(s_getLeafType, null);
    }

    private void storeResolution(final ComputationTargetReference reference, final ComputationTargetSpecification resolved) {
      final ComputationTargetReference key = reference.accept(this);
      if (key != null) {
        final UniqueId resolvedId = resolved.getUniqueId();
        if (resolvedId != null) {
          final UniqueId previousId = _resolutions.put(key, resolvedId);
          if ((previousId != null) && !resolvedId.equals(previousId)) {
            s_logger.info("Direct resolution of {} to {} has expired", key, previousId);
            _expiredResolutions.add(previousId);
          }
        }
      }
    }

    private void storeResolution(final ComputationTargetType type, final UniqueIdentifiable resolved) {
      final UniqueId resolvedId = resolved.getUniqueId();
      storeResolution(new ComputationTargetSpecification(type, resolvedId.toLatest()), resolvedId);
    }

    private void storeResolution(final ComputationTargetReference reference, final UniqueId resolvedId) {
      final ComputationTargetReference key = MemoryUtils.instance(reference);
      final UniqueId previousId = _resolutions.put(key, resolvedId);
      if ((previousId != null) && !resolvedId.equals(previousId)) {
        s_logger.info("Transitive resolution of {} to {} has expired", previousId);
        _expiredResolutions.add(previousId);
      }
    }

    // ComputationTargetSpecificationResolver.AtVersionCorrection

    @Override
    public ComputationTargetSpecification getTargetSpecification(final ComputationTargetReference reference) {
      final ComputationTargetSpecification resolved = _underlying.getTargetSpecification(reference);
      if (resolved != null) {
        storeResolution(reference, resolved);
      }
      return resolved;
    }

    @Override
    public Map<ComputationTargetReference, ComputationTargetSpecification> getTargetSpecifications(final Set<ComputationTargetReference> references) {
      final Map<ComputationTargetReference, ComputationTargetSpecification> resolveds = _underlying.getTargetSpecifications(references);
      for (final Map.Entry<ComputationTargetReference, ComputationTargetSpecification> resolved : resolveds.entrySet()) {
        storeResolution(resolved.getKey(), resolved.getValue());
      }
      return resolveds;
    }

    // ComputationTargetReferenceVisitor

    @Override
    public ComputationTargetReference visitComputationTargetRequirement(final ComputationTargetRequirement requirement) {
      final ComputationTargetType leafType = getLeafType(requirement.getType());
      if (leafType != null) {
        return MemoryUtils.instance(new ComputationTargetRequirement(leafType, requirement.getIdentifiers()));
      } else {
        return requirement;
      }
    }

    @Override
    public ComputationTargetReference visitComputationTargetSpecification(final ComputationTargetSpecification specification) {
      if ((specification.getUniqueId() != null) && specification.getUniqueId().isLatest()) {
        final ComputationTargetType leafType = getLeafType(specification.getType());
        if (leafType != null) {
          return MemoryUtils.instance(new ComputationTargetSpecification(leafType, specification.getUniqueId()));
        } else {
          return specification;
        }
      } else {
        return null;
      }
    }

  }

  private static final ComputationTargetTypeMap<Function2<LoggingSpecificationResolver, ComputationTarget, ComputationTarget>> s_resolvers;

  private static final class LoggingPortfolio implements Portfolio {

    private final LoggingSpecificationResolver _logger;
    private final Portfolio _raw;

    public LoggingPortfolio(final LoggingSpecificationResolver logger, final Portfolio raw) {
      _logger = logger;
      _raw = raw;
    }

    // Portfolio

    @Override
    public Map<String, String> getAttributes() {
      return _raw.getAttributes();
    }

    @Override
    public void setAttributes(Map<String, String> attributes) {
      _raw.setAttributes(attributes);
    }

    @Override
    public void addAttribute(String key, String value) {
      _raw.addAttribute(key, value);
    }

    @Override
    public UniqueId getUniqueId() {
      return _raw.getUniqueId();
    }

    @Override
    public PortfolioNode getRootNode() {
      final PortfolioNode rootNode = _raw.getRootNode();
      _logger.storeResolution(ComputationTargetType.PORTFOLIO_NODE, rootNode);
      return new LoggingPortfolioNode(_logger, rootNode);
    }

    @Override
    public String getName() {
      return _raw.getName();
    }

  }

  private static final class LoggingPortfolioNode implements PortfolioNode {

    private final LoggingSpecificationResolver _logger;
    private final PortfolioNode _raw;

    public LoggingPortfolioNode(final LoggingSpecificationResolver logger, final PortfolioNode raw) {
      _logger = logger;
      _raw = raw;
    }

    // PortfolioNode

    @Override
    public UniqueId getUniqueId() {
      return _raw.getUniqueId();
    }

    @Override
    public UniqueId getParentNodeId() {
      return _raw.getParentNodeId();
    }

    @Override
    public int size() {
      return _raw.size();
    }

    @Override
    public List<PortfolioNode> getChildNodes() {
      final List<PortfolioNode> childNodes = _raw.getChildNodes();
      final List<PortfolioNode> result = new ArrayList<PortfolioNode>(childNodes.size());
      for (PortfolioNode childNode : childNodes) {
        _logger.storeResolution(ComputationTargetType.PORTFOLIO_NODE, childNode);
        result.add(new LoggingPortfolioNode(_logger, childNode));
      }
      return result;
    }

    @Override
    public List<Position> getPositions() {
      final List<Position> positions = _raw.getPositions();
      final List<Position> result = new ArrayList<Position>(positions.size());
      for (Position position : positions) {
        _logger.storeResolution(ComputationTargetType.POSITION, position);
        result.add(new LoggingPosition(_logger, position));
      }
      return result;
    }

    @Override
    public String getName() {
      return _raw.getName();
    }

  }

  private static final class LoggingPosition implements Position {

    private final LoggingSpecificationResolver _logger;
    private final Position _raw;

    public LoggingPosition(final LoggingSpecificationResolver logger, final Position raw) {
      _logger = logger;
      _raw = raw;
    }

    // Position

    @Override
    public UniqueId getUniqueId() {
      return _raw.getUniqueId();
    }

    @Override
    public BigDecimal getQuantity() {
      return _raw.getQuantity();
    }

    @Override
    public SecurityLink getSecurityLink() {
      return _raw.getSecurityLink();
    }

    @Override
    public Security getSecurity() {
      final Security security = _raw.getSecurity();
      final SecurityLink link = getSecurityLink();
      if (link.getExternalId() != null) {
        _logger.storeResolution(new ComputationTargetRequirement(ComputationTargetType.SECURITY, link.getExternalId()), security.getUniqueId());
      }
      if (link.getObjectId() != null) {
        _logger.storeResolution(new ComputationTargetSpecification(ComputationTargetType.SECURITY, link.getObjectId().atLatestVersion()), security.getUniqueId());
      }
      return security;
    }

    @Override
    public Collection<Trade> getTrades() {
      final Collection<Trade> trades = _raw.getTrades();
      final Collection<Trade> result = new ArrayList<Trade>(trades.size());
      for (Trade trade : trades) {
        _logger.storeResolution(ComputationTargetType.TRADE, trade);
        result.add(new LoggingTrade(_logger, trade));
      }
      return result;
    }

    @Override
    public Map<String, String> getAttributes() {
      return _raw.getAttributes();
    }

  }

  private static final class LoggingTrade implements Trade {

    private final LoggingSpecificationResolver _logger;
    private final Trade _raw;

    public LoggingTrade(final LoggingSpecificationResolver logger, final Trade raw) {
      _logger = logger;
      _raw = raw;
    }

    // Trade

    @Override
    public UniqueId getUniqueId() {
      return _raw.getUniqueId();
    }

    @Override
    public BigDecimal getQuantity() {
      return _raw.getQuantity();
    }

    @Override
    public SecurityLink getSecurityLink() {
      return _raw.getSecurityLink();
    }

    @Override
    public Security getSecurity() {
      final Security security = _raw.getSecurity();
      final SecurityLink link = getSecurityLink();
      if (link.getExternalId() != null) {
        _logger.storeResolution(new ComputationTargetRequirement(ComputationTargetType.SECURITY, link.getExternalId()), security.getUniqueId());
      }
      if (link.getObjectId() != null) {
        _logger.storeResolution(new ComputationTargetSpecification(ComputationTargetType.SECURITY, link.getObjectId().atLatestVersion()), security.getUniqueId());
      }
      return security;
    }

    @Override
    public Map<String, String> getAttributes() {
      return _raw.getAttributes();
    }

    @Override
    public void setAttributes(Map<String, String> attributes) {
      _raw.setAttributes(attributes);
    }

    @Override
    public void addAttribute(String key, String value) {
      _raw.addAttribute(key, value);
    }

    @Override
    public Counterparty getCounterparty() {
      return _raw.getCounterparty();
    }

    @Override
    public LocalDate getTradeDate() {
      return _raw.getTradeDate();
    }

    @Override
    public OffsetTime getTradeTime() {
      return _raw.getTradeTime();
    }

    @Override
    public Double getPremium() {
      return _raw.getPremium();
    }

    @Override
    public Currency getPremiumCurrency() {
      return _raw.getPremiumCurrency();
    }

    @Override
    public LocalDate getPremiumDate() {
      return _raw.getPremiumDate();
    }

    @Override
    public OffsetTime getPremiumTime() {
      return _raw.getPremiumTime();
    }

  }

  static {
    s_resolvers = new ComputationTargetTypeMap<Function2<LoggingSpecificationResolver, ComputationTarget, ComputationTarget>>();
    s_resolvers.put(ComputationTargetType.PORTFOLIO, new Function2<LoggingSpecificationResolver, ComputationTarget, ComputationTarget>() {
      @Override
      public ComputationTarget execute(final LoggingSpecificationResolver logger, final ComputationTarget raw) {
        return new ComputationTarget(raw.toSpecification(), new LoggingPortfolio(logger, (Portfolio) raw.getValue()));
      }
    });
    s_resolvers.put(ComputationTargetType.PORTFOLIO_NODE, new Function2<LoggingSpecificationResolver, ComputationTarget, ComputationTarget>() {
      @Override
      public ComputationTarget execute(final LoggingSpecificationResolver logger, final ComputationTarget raw) {
        return new ComputationTarget(raw.toSpecification(), new LoggingPortfolioNode(logger, raw.getPortfolioNode()));
      }
    });
    s_resolvers.put(ComputationTargetType.POSITION, new Function2<LoggingSpecificationResolver, ComputationTarget, ComputationTarget>() {
      @Override
      public ComputationTarget execute(final LoggingSpecificationResolver logger, final ComputationTarget raw) {
        return new ComputationTarget(raw.toSpecification(), new LoggingPosition(logger, raw.getPosition()));
      }
    });
    s_resolvers.put(ComputationTargetType.TRADE, new Function2<LoggingSpecificationResolver, ComputationTarget, ComputationTarget>() {
      @Override
      public ComputationTarget execute(final LoggingSpecificationResolver logger, final ComputationTarget raw) {
        return new ComputationTarget(raw.toSpecification(), new LoggingTrade(logger, raw.getTrade()));
      }
    });
  }

  // TODO: A wrapper like this isn't manageable in the long term. A logging hook needs to be part of the target resolver which
  // already understands the concept of deep resolution.

  private final ComputationTargetResolver.AtVersionCorrection _underlying;
  private final LoggingSpecificationResolver _specificationResolver;

  public static TargetResolutionLogger of(final ComputationTargetResolver.AtVersionCorrection underlying, final ConcurrentMap<ComputationTargetReference, UniqueId> resolutions,
      final Set<UniqueId> expiredResolutions) {
    return new TargetResolutionLogger(underlying, new LoggingSpecificationResolver(underlying.getSpecificationResolver(), resolutions, expiredResolutions));
  }

  private TargetResolutionLogger(final ComputationTargetResolver.AtVersionCorrection underlying, final LoggingSpecificationResolver specificationResolver) {
    _underlying = underlying;
    _specificationResolver = specificationResolver;
  }

  @Override
  public ComputationTarget resolve(final ComputationTargetSpecification specification) {
    return _underlying.resolve(specification);
  }

  @Override
  public AtVersionCorrection getSpecificationResolver() {
    return _specificationResolver;
  }

  @Override
  public ComputationTargetType simplifyType(final ComputationTargetType type) {
    return _underlying.simplifyType(type);
  }

  @Override
  public VersionCorrection getVersionCorrection() {
    return _underlying.getVersionCorrection();
  }

}
