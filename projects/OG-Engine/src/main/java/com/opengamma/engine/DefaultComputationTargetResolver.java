/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.change.PassthroughChangeManager;
import com.opengamma.core.position.PositionSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.target.ComputationTargetResolverUtils;
import com.opengamma.engine.target.ComputationTargetSpecificationResolver;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.target.ComputationTargetTypeMap;
import com.opengamma.engine.target.ComputationTargetTypeProvider;
import com.opengamma.engine.target.ComputationTargetTypeVisitor;
import com.opengamma.engine.target.DefaultComputationTargetSpecificationResolver;
import com.opengamma.engine.target.PrimitiveComputationTargetType;
import com.opengamma.engine.target.lazy.LazyResolveContext;
import com.opengamma.engine.target.lazy.LazyResolver;
import com.opengamma.engine.target.resolver.ChainedResolver;
import com.opengamma.engine.target.resolver.IdentifierResolver;
import com.opengamma.engine.target.resolver.ObjectResolver;
import com.opengamma.engine.target.resolver.PositionSourceResolver;
import com.opengamma.engine.target.resolver.SecuritySourceResolver;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;

/**
 * A computation target resolver implementation that resolves using a security and position source.
 * <p>
 * This is the standard implementation that resolves from a target specification to a real target. It provides results using a security and position source.
 */
public class DefaultComputationTargetResolver implements ComputationTargetResolver, LazyResolver, ComputationTargetTypeProvider {

  // [PLAT-444]: move to com.opengamma.engine.target

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(DefaultComputationTargetResolver.class);

  /**
   * The security source.
   */
  private final SecuritySource _securitySource;

  /**
   * The position source.
   */
  private final PositionSource _positionSource;

  /**
   * The per-type resolvers.
   */
  private final ComputationTargetTypeMap<ObjectResolver<?>> _resolvers = new ComputationTargetTypeMap<ObjectResolver<?>>(ChainedResolver.CREATE);

  /**
   * The type-reductions used.
   */
  private final ComputationTargetTypeMap<ComputationTargetType> _baseTypes = new ComputationTargetTypeMap<ComputationTargetType>();

  private final DefaultComputationTargetSpecificationResolver _specificationResolver = new DefaultComputationTargetSpecificationResolver();

  private LazyResolveContext _lazyResolveContext;

  /**
   * Creates a resolver without access to a security source or a position source. This will only be able to resolve PRIMITIVE computation target types.
   */
  public DefaultComputationTargetResolver() {
    this(null, null);
  }

  /**
   * Creates a resolver using a security source only. This will not be able to resolve POSITION and PORTFOLIO_NODE computation target types.
   * 
   * @param securitySource the security source, null prevents some targets from resolving
   */
  public DefaultComputationTargetResolver(final SecuritySource securitySource) {
    this(securitySource, null);
  }

  /**
   * Creates a resolver using a security and position source This will be able to resolve any type of computation target.
   * 
   * @param securitySource the security source, null prevents some targets from resolving
   * @param positionSource the position source, null prevents some targets from resolving
   */
  public DefaultComputationTargetResolver(final SecuritySource securitySource, final PositionSource positionSource) {
    _securitySource = securitySource;
    if (securitySource != null) {
      addResolver(ComputationTargetType.SECURITY, new SecuritySourceResolver(securitySource));
    }
    _positionSource = positionSource;
    if (positionSource != null) {
      final PositionSourceResolver resolver = new PositionSourceResolver(positionSource);
      addResolver(ComputationTargetType.PORTFOLIO, new LazyResolver.LazyPortfolioResolver(this, resolver.portfolio()));
      addResolver(ComputationTargetType.PORTFOLIO_NODE, new LazyResolver.LazyPortfolioNodeResolver(this, resolver.portfolioNode()));
      addResolver(ComputationTargetType.POSITION, new LazyResolver.LazyPositionResolver(this, resolver.position()));
      addResolver(ComputationTargetType.TRADE, new LazyResolver.LazyTradeResolver(this, resolver.trade()));
    }
    addResolver(ComputationTargetType.CURRENCY);
    addResolver(ComputationTargetType.PRIMITIVE);
    addResolver(ComputationTargetType.UNORDERED_CURRENCY_PAIR);
    addResolver(ComputationTargetType.CREDIT_CURVE_IDENTIFIER);
    _lazyResolveContext = new LazyResolveContext(securitySource, null);
  }

  /**
   * Adds a resolver for use with targets of the given type. If the resolver also implements the {@link IdentifierResolver} interface then it will be registered for target specification resolution as
   * well as object resolution.
   * 
   * @param <T> the target type
   * @param type the type(s) to use this resolver for, never null
   * @param resolver the resolver to use, not null
   */
  public <T extends UniqueIdentifiable> void addResolver(final ComputationTargetType type, final ObjectResolver<T> resolver) {
    _resolvers.put(type, resolver, _resolvers.getFoldFunction());
    _baseTypes.put(type, type);
    if (resolver instanceof IdentifierResolver) {
      _specificationResolver.addResolver(type, (IdentifierResolver) resolver);
    }
  }

  /**
   * Registers a primitive type with the resolver using the primitive type's default resolution strategy to convert {@link UniqueId} instances to the target object.
   * 
   * @param type the primitive type to resolve, never null
   */
  public void addResolver(final PrimitiveComputationTargetType<?> type) {
    addResolver(type, type);
  }

  @Override
  public void setLazyResolveContext(final LazyResolveContext context) {
    ArgumentChecker.notNull(context, "context");
    _lazyResolveContext = context;
  }

  @Override
  public LazyResolveContext getLazyResolveContext() {
    return _lazyResolveContext;
  }

  /**
   * Gets the security source which provides access to the securities.
   * 
   * @return the security source, may be null
   */
  @Override
  public SecuritySource getSecuritySource() {
    return _securitySource;
  }

  /**
   * Gets the position source which provides access to the positions.
   * 
   * @return the position source, may be null
   */
  protected PositionSource getPositionSource() {
    return _positionSource;
  }

  /**
   * Resolves the specification using the security and position sources.
   * <p>
   * The key method of this class, implementing {@code ComputationTargetResolver}. It examines the specification and resolves the most appropriate target.
   * 
   * @param specification the specification to resolve, not null
   * @param versionCorrection the version/correction timestamp to use for the resolution, not null
   * @return the resolved target, null if not found
   */
  @Override
  public ComputationTarget resolve(final ComputationTargetSpecification specification, final VersionCorrection versionCorrection) {
    final ComputationTargetType type = specification.getType();
    if (ComputationTargetType.NULL == type) {
      return ComputationTarget.NULL;
    } else {
      final ObjectResolver<?> resolver = _resolvers.get(type);
      if (resolver != null) {
        final UniqueIdentifiable resolved = resolver.resolveObject(specification.getUniqueId(), versionCorrection);
        if (resolved != null) {
          return ComputationTargetResolverUtils.createResolvedTarget(specification, resolved);
        } else {
          s_logger.info("Unable to resolve {}", specification);
          return null;
        }
      } else {
        throw new OpenGammaRuntimeException("Unhandled computation target type " + specification.getType());
      }
    }
  }

  @Override
  public ObjectResolver<?> getResolver(final ComputationTargetSpecification specification) {
    final ComputationTargetType type = specification.getType();
    if (ComputationTargetType.NULL == type) {
      return (ObjectResolver<?>) ComputationTargetType.NULL;
    } else {
      return _resolvers.get(type);
    }
  }

  private static final ComputationTargetTypeVisitor<ComputationTargetTypeMap<ComputationTargetType>, ComputationTargetType> s_simplifyType =
      new ComputationTargetTypeVisitor<ComputationTargetTypeMap<ComputationTargetType>, ComputationTargetType>() {

        @Override
        public ComputationTargetType visitMultipleComputationTargetTypes(final Set<ComputationTargetType> types, final ComputationTargetTypeMap<ComputationTargetType> data) {
          final ComputationTargetType[] result = new ComputationTargetType[types.size()];
          int i = 0;
          boolean changed = false;
          for (final ComputationTargetType type : types) {
            final ComputationTargetType newType = type.accept(this, data);
            if ((newType != null) && (newType != type)) {
              result[i++] = newType;
              changed = true;
            } else {
              result[i++] = type;
            }
          }
          return changed ? ComputationTargetType.multiple(result) : null;
        }

        @Override
        public ComputationTargetType visitNestedComputationTargetTypes(final List<ComputationTargetType> types, final ComputationTargetTypeMap<ComputationTargetType> data) {
          final int length = types.size();
          for (int i = 0; i < length; i++) {
            ComputationTargetType type = types.get(i);
            ComputationTargetType newType = type.accept(this, data);
            if ((newType != null) && (newType != type)) {
              ComputationTargetType result;
              if (i > 0) {
                result = types.get(0);
                for (int j = 1; j < i; j++) {
                  result = result.containing(types.get(j));
                }
                result = result.containing(newType);
              } else {
                result = newType;
              }
              for (int j = i + 1; j < length; j++) {
                type = types.get(j);
                newType = type.accept(this, data);
                result = result.containing((newType == null) ? type : newType);
              }
              return result;
            }
          }
          return null;
        }

        @Override
        public ComputationTargetType visitNullComputationTargetType(final ComputationTargetTypeMap<ComputationTargetType> data) {
          return null;
        }

        @Override
        public ComputationTargetType visitClassComputationTargetType(final Class<? extends UniqueIdentifiable> type, final ComputationTargetTypeMap<ComputationTargetType> data) {
          return data.get(type);
        }

      };

  @Override
  public ComputationTargetType simplifyType(final ComputationTargetType type) {
    final ComputationTargetType newType = type.accept(s_simplifyType, _baseTypes);
    if (newType != null) {
      return newType;
    } else {
      return type;
    }
  }

  @Override
  public ComputationTargetSpecificationResolver getSpecificationResolver() {
    return _specificationResolver;
  }

  @Override
  public ComputationTargetResolver.AtVersionCorrection atVersionCorrection(final VersionCorrection versionCorrection) {
    final ComputationTargetSpecificationResolver.AtVersionCorrection specificationResolver = getSpecificationResolver().atVersionCorrection(versionCorrection);
    return new ComputationTargetResolver.AtVersionCorrection() {

      @Override
      public ComputationTargetType simplifyType(final ComputationTargetType type) {
        return DefaultComputationTargetResolver.this.simplifyType(type);
      }

      @Override
      public ComputationTarget resolve(final ComputationTargetSpecification specification) {
        return DefaultComputationTargetResolver.this.resolve(specification, versionCorrection);
      }

      @Override
      public ObjectResolver<?> getResolver(final ComputationTargetSpecification specification) {
        return DefaultComputationTargetResolver.this.getResolver(specification);
      }

      @Override
      public ComputationTargetSpecificationResolver.AtVersionCorrection getSpecificationResolver() {
        return specificationResolver;
      }

      @Override
      public VersionCorrection getVersionCorrection() {
        return versionCorrection;
      }

    };
  }

  @Override
  public ChangeManager changeManager() {
    return new PassthroughChangeManager(_resolvers.values());
  }

  /**
   * Returns a string suitable for debugging.
   * 
   * @return the string, not null
   */
  @Override
  public String toString() {
    return getClass().getSimpleName() + "[securitySource=" + getSecuritySource() + ",positionSource=" + getPositionSource() + "]";
  }

  // ComputationTargetTypeProvider

  @Override
  public Collection<ComputationTargetType> getSimpleTypes() {
    final Set<ComputationTargetType> types = new HashSet<ComputationTargetType>();
    for (final ComputationTargetType baseType : _baseTypes.values()) {
      types.add(baseType);
    }
    return types;
  }

  @Override
  public Collection<ComputationTargetType> getAdditionalTypes() {
    return Collections.emptySet();
  }

  @Override
  public Collection<ComputationTargetType> getAllTypes() {
    return getSimpleTypes();
  }

}
