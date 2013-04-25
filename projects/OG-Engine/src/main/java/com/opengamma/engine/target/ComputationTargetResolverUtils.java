/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.target;

import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetResolver;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.id.UniqueIdentifiable;

/**
 * Helper methods for writing {@link ComputationTargetResolver} instances.
 */
public class ComputationTargetResolverUtils {

  private static final Logger s_logger = LoggerFactory.getLogger(ComputationTargetResolverUtils.class);

  /**
   * Visitor to remove any union selections from the leaf. Returns null if the type did not match the resolved object, {@link ComputationTargetType#NULL} if the object did match, or the correct type
   * selection if it was modified.
   */
  private static final ComputationTargetTypeVisitor<UniqueIdentifiable, ComputationTargetType> s_resolveType = new ComputationTargetTypeVisitor<UniqueIdentifiable, ComputationTargetType>() {

    @Override
    public ComputationTargetType visitMultipleComputationTargetTypes(final Set<ComputationTargetType> types, final UniqueIdentifiable resolved) {
      for (final ComputationTargetType type : types) {
        final ComputationTargetType newType = type.accept(this, resolved);
        if (newType == ComputationTargetType.NULL) {
          // Use the original type
          return type;
        } else if (newType != null) {
          // Use the replaced type
          return newType;
        }
      }
      // Nothing in the union matched
      return null;
    }

    @Override
    public ComputationTargetType visitNestedComputationTargetTypes(final List<ComputationTargetType> types, final UniqueIdentifiable resolved) {
      final ComputationTargetType leafType = types.get(types.size() - 1).accept(this, resolved);
      if (leafType == ComputationTargetType.NULL) {
        // Unchanged
        return leafType;
      } else if (leafType != null) {
        // Replace the leaf type
        ComputationTargetType newType = types.get(0);
        for (int i = 1; i < types.size() - 1; i++) {
          newType = newType.containing(types.get(i));
        }
        return newType.containing(leafType);
      } else {
        return null;
      }
    }

    @Override
    public ComputationTargetType visitNullComputationTargetType(final UniqueIdentifiable resolved) {
      // Invalid
      return null;
    }

    @Override
    public ComputationTargetType visitClassComputationTargetType(final Class<? extends UniqueIdentifiable> type, final UniqueIdentifiable resolved) {
      final Class<? extends UniqueIdentifiable> clazz = resolved.getClass();
      if (type.equals(clazz)) {
        // Unchanged
        return ComputationTargetType.NULL;
      } else if (type.isAssignableFrom(clazz)) {
        // Have a more specific type
        return ComputationTargetType.of(clazz);
      } else {
        // Class doesn't match
        return null;
      }
    }

  };

  /**
   * Creates a {@link ComputationTarget} instance that describes the resolved object. The type in the target will accurately describe the target type to the scoping level of the requested
   * specification. For example a target of type {@code FooSecurity} resolved from a specification of type {@code POSITION/SECURITY} will end up as type {@code POSITION/FooSecurity}.
   * 
   * @param requestedSpecification the original specification as passed to the {@link ComputationTargetResolver#resolve} method, not null
   * @param target the resolved object, never null
   * @return the target object instance with the correct logical type, not null
   */
  public static ComputationTarget createResolvedTarget(final ComputationTargetSpecification requestedSpecification, final UniqueIdentifiable target) {
    ComputationTargetSpecification resolvedSpecification;
    final ComputationTargetType requestedType = requestedSpecification.getType();
    final ComputationTargetType resolvedType = requestedType.accept(s_resolveType, target);
    if (resolvedType == null) {
      // Error
      if (s_logger.isWarnEnabled()) {
        s_logger.warn("Resolved {} to {}, not instanceof {}", new Object[] {requestedSpecification.getUniqueId(), target, requestedType });
      }
      resolvedSpecification = requestedSpecification;
    } else if (resolvedType == ComputationTargetType.NULL) {
      // No-change
      resolvedSpecification = requestedSpecification;
    } else {
      // Different type
      resolvedSpecification = (ComputationTargetSpecification) requestedSpecification.replaceType(resolvedType);
    }
    if (requestedSpecification.getUniqueId().isLatest() && target.getUniqueId().isVersioned()) {
      resolvedSpecification = resolvedSpecification.replaceIdentifier(target.getUniqueId());
    }
    return new ComputationTarget(resolvedSpecification, target);
  }

  @SuppressWarnings("unchecked")
  private static <T extends ComputationTargetReference> T simplifyType(final T reference, final ComputationTargetType oldType, final ComputationTargetType newType) {
    if ((newType == null) || (newType == oldType)) {
      return reference;
    } else {
      return (T) reference.replaceType(newType);
    }
  }

  /**
   * Simplifies the type within a reference to the simplest form that the resolver will recognize. For example {@code CTSpec[FooSecurity, Sec~1]} might be simplified to {@code CTSpec[SECURITY, Sec~1]}
   * if the same resolution will take place regardless of whether the type is a security or a sub-class of it. If no simplification is possible, the original reference may be returned.
   * 
   * @param <T> the object type, either {@link ComputationTargetSpecification} or {@link ComputationTargetReference}
   * @param reference the reference to simplify, not null
   * @param resolver the resolver to simplify against, not null
   * @return the simplified reference, not null
   */
  public static <T extends ComputationTargetReference> T simplifyType(final T reference, final ComputationTargetResolver resolver) {
    return simplifyType(reference, reference.getType(), resolver.simplifyType(reference.getType()));
  }

  public static <T extends ComputationTargetReference> T simplifyType(final T reference, final ComputationTargetResolver.AtVersionCorrection resolver) {
    return simplifyType(reference, reference.getType(), resolver.simplifyType(reference.getType()));
  }

}
