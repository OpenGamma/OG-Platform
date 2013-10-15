/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.PositionOrTrade;
import com.opengamma.core.position.Trade;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecurityLink;
import com.opengamma.engine.target.ComputationTargetReference;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.target.ComputationTargetTypeMap;
import com.opengamma.engine.target.ComputationTargetTypeVisitor;
import com.opengamma.engine.target.ObjectComputationTargetType;
import com.opengamma.engine.target.PrimitiveComputationTargetType;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.lambdava.functions.Function1;
import com.opengamma.util.PublicAPI;

/**
 * A fully resolved target, sufficient for computation invocation.
 */
@PublicAPI
public class ComputationTarget implements Serializable {

  // [PLAT-444] move to com.opengamma.engine.target

  /**
   * The null target. This has a type of {@link ComputationTargetType#NULL} and a null value.
   */
  public static final ComputationTarget NULL = new ComputationTarget(ComputationTargetSpecification.NULL, null);

  private static final long serialVersionUID = 1L;

  /**
   * The specification that was resolved into this target
   */
  private final ComputationTargetSpecification _specification;
  /**
   * The actual target.
   */
  private final UniqueIdentifiable _value;

  /**
   * The cached hash code.
   */
  private transient int _hashCode;  // safe via racy single check idiom

  /**
   * Creates a target for computation. The type is a primitive type that is capable of converting the unique identifier form of the value to its {@link UniqueIdentifiable} form without any resolution
   * services. This is intended for creating test cases only. Code requiring resolution of a type/unique identifier pair to a target should use a {@link ComputationTargetResolver} instance.
   * 
   * @param type the type of the target, not null
   * @param value the target itself, not null
   * @throws IllegalArgumentException if the value is invalid for the type
   */
  public ComputationTarget(final PrimitiveComputationTargetType<?> type, final UniqueId value) {
    this(new ComputationTargetSpecification(type, value), type.resolve(value));
  }

  /**
   * Creates a target for computation. This is intended for creating test cases only. Code which is resolving a target specification should use the constructor which takes that specification.
   * 
   * @param type the type of the target, not null
   * @param value the target itself, not null
   * @throws IllegalArgumentException if the value is invalid for the type
   */
  public ComputationTarget(final ComputationTargetType type, final UniqueIdentifiable value) {
    this(new ComputationTargetSpecification(type, (value != null) ? value.getUniqueId() : null), value);
  }

  /**
   * Creates a target for computation.
   * 
   * @param specification the target specification, not null
   * @param value the target itself, may be null if the specification of {@link ComputationTargetSpecification#NULL}
   */
  public ComputationTarget(final ComputationTargetSpecification specification, final UniqueIdentifiable value) {
    assert specification != null;
    assert specification.getType().isCompatible(value);
    assert (value != null) ? specification.getUniqueId().equals(value.getUniqueId()) : (specification.getUniqueId() == null);
    _specification = specification;
    _value = value;
  }

  /**
   * Gets the type of the target.
   * 
   * @return the type, not null
   */
  public ComputationTargetType getType() {
    return _specification.getType();
  }

  /**
   * Gets the actual target.
   * 
   * @return the target, may be null
   */
  public UniqueIdentifiable getValue() {
    return _value;
  }

  /**
   * Gets the unique identifier of the target object
   * 
   * @return the unique identifier, may be null
   */
  public UniqueId getUniqueId() {
    return _specification.getUniqueId();
  }

  private static final ComputationTargetTypeVisitor<ComputationTarget, ComputationTargetSpecification> s_getLeafSpecification =
      new ComputationTargetTypeVisitor<ComputationTarget, ComputationTargetSpecification>() {

        @Override
        public ComputationTargetSpecification visitMultipleComputationTargetTypes(final Set<ComputationTargetType> types, final ComputationTarget self) {
          throw new IllegalStateException();
        }

        @Override
        public ComputationTargetSpecification visitNestedComputationTargetTypes(final List<ComputationTargetType> types, final ComputationTarget self) {
          return new ComputationTargetSpecification(types.get(types.size() - 1), self._specification.getUniqueId());
        }

        @Override
        public ComputationTargetSpecification visitNullComputationTargetType(final ComputationTarget self) {
          return ComputationTargetSpecification.NULL;
        }

        @Override
        public ComputationTargetSpecification visitClassComputationTargetType(final Class<? extends UniqueIdentifiable> type, final ComputationTarget self) {
          return self.toSpecification();
        }

      };

  /**
   * Returns the target specification of the context this target is part of. If the actual target object is required a {@link ComputationTargetResolver} can be used to obtain it.
   * 
   * @return the reference of the containing object, or null if there is none
   */
  public ComputationTargetReference getContextSpecification() {
    return _specification.getParent();
  }

  /**
   * Returns the target specification of the leaf target object. The specification of the whole target (as returned by {@link #toSpecification}) is equivalent to
   * {@code getContextSpecification().containing(getLeafSpecification())}.
   * 
   * @return the specification of the leaf object, never null
   */
  public ComputationTargetSpecification getLeafSpecification() {
    return _specification.getType().accept(s_getLeafSpecification, this);
  }

  /**
   * Safely converts the target to a {@code PortfolioNode}.
   * 
   * @return the portfolio node, not null
   * @throws IllegalStateException if the type is not PORTFOLIO_NODE
   */
  public PortfolioNode getPortfolioNode() {
    if (getValue() instanceof PortfolioNode) {
      return (PortfolioNode) getValue();
    } else {
      throw new IllegalStateException("Requested a PortfolioNode for a target of type " + getType());
    }
  }

  /**
   * Safely converts the target to a {@code Position}.
   * 
   * @return the position, not null
   * @throws IllegalStateException if the type is not POSITION
   */
  public Position getPosition() {
    if (getValue() instanceof Position) {
      return (Position) getValue();
    } else {
      throw new IllegalStateException("Requested a Position for a target of type " + getType());
    }
  }

  /**
   * Safely converts the target to a {@code Trade}.
   * 
   * @return the trade, not null
   * @throws IllegalStateException if the type is not TRADE
   */
  public Trade getTrade() {
    if (getValue() instanceof Trade) {
      return (Trade) getValue();
    } else {
      throw new IllegalStateException("Requested a Trade for a target of type " + getType());
    }
  }

  /**
   * Safely converts the target to a {@code Position} or {@code Trade}.
   * 
   * @return the position or trade, not null
   * @throws IllegalStateException if the type is not a POSITION or TRADE
   */
  public PositionOrTrade getPositionOrTrade() {
    if (getValue() instanceof PositionOrTrade) {
      return (PositionOrTrade) getValue();
    } else {
      throw new IllegalStateException("Requested a Position or Trade for a target of type " + getType());
    }
  }

  /**
   * Safely converts the target to a {@code Security}.
   * 
   * @return the security, not null
   * @throws IllegalStateException if the type is not SECURITY
   */
  public Security getSecurity() {
    if (getValue() instanceof Security) {
      return (Security) getValue();
    } else {
      throw new IllegalStateException("Requested a Security for a target of type " + getType());
    }
  }

  @SuppressWarnings("unchecked")
  public <T extends UniqueIdentifiable> T getValue(final ObjectComputationTargetType<T> type) {
    if (type.getObjectClass().isAssignableFrom(getValue().getClass())) {
      return (T) getValue();
    } else {
      throw new IllegalStateException("Requested a " + type + " for a target of type " + getType());
    }
  }

  private String getNameImpl(final String name) {
    if (name != null) {
      return name;
    } else {
      final UniqueId uid = getUniqueId();
      if (uid != null) {
        return uid.toString();
      } else {
        return null;
      }
    }
  }

  private static final ComputationTargetTypeMap<Function1<ComputationTarget, String>> s_getName = createGetName();

  private static ComputationTargetTypeMap<Function1<ComputationTarget, String>> createGetName() {
    final ComputationTargetTypeMap<Function1<ComputationTarget, String>> getName = new ComputationTargetTypeMap<Function1<ComputationTarget, String>>();
    getName.put(ComputationTargetType.PORTFOLIO_NODE, new Function1<ComputationTarget, String>() {
      @Override
      public String execute(final ComputationTarget target) {
        return target.getPortfolioNode().getName();
      }
    });
    getName.put(ComputationTargetType.SECURITY, new Function1<ComputationTarget, String>() {
      @Override
      public String execute(final ComputationTarget target) {
        return target.getSecurity().getName();
      }
    });
    getName.put(ComputationTargetType.POSITION.or(ComputationTargetType.TRADE), new Function1<ComputationTarget, String>() {
      @Override
      public String execute(final ComputationTarget target) {
        final PositionOrTrade position = target.getPositionOrTrade();
        Security security = position.getSecurity();
        if (security != null) {
          return security.getName();
        }
        final SecurityLink link = position.getSecurityLink();
        if (link != null) {
          security = link.getTarget();
          if (security != null) {
            return security.getName();
          }
          final ExternalIdBundle identifiers = link.getExternalId();
          if (identifiers != null) {
            if (identifiers.size() > 0) {
              return position.getQuantity() + " x " + identifiers.iterator().next();
            } else {
              return position.getQuantity() + " x " + identifiers;
            }
          } else if (link.getObjectId() != null) {
            return position.getQuantity() + " x " + link.getObjectId();
          }
        }
        return null;
      }
    });
    return getName;
  }

  /**
   * Gets the name of the computation target.
   * <p>
   * This can the portfolio name, the security name, the name of the security underlying a position, or - for primitives - the unique identifier if available.
   * 
   * @return the name of the computation target, null if a primitive and no identifier is available
   */
  public String getName() {
    final Function1<ComputationTarget, String> getName = s_getName.get(getType());
    if (getName != null) {
      return getNameImpl(getName.execute(this));
    } else {
      return getNameImpl(null);
    }
  }

  /**
   * Returns a specification that is equivalent to this target.
   * 
   * @return the specification equivalent to this target, not null
   */
  public ComputationTargetSpecification toSpecification() {
    return _specification;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof ComputationTarget) {
      final ComputationTarget other = (ComputationTarget) obj;
      return _specification.equals(other._specification)
          && ObjectUtils.equals(_value, other._value);
    }
    return false;
  }

  @Override
  public int hashCode() {
    // racy single check idiom allows non-volatile variable
    // requires only one read and one write of non-volatile
    int hashCode = _hashCode;
    if (hashCode == 0) {
      int result = 1;
      result += (result << 4) + _specification.hashCode();
      result += (result << 4) + ObjectUtils.hashCode(_value);
      _hashCode = result;
    }
    return hashCode;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append("CT[").append(toSpecification()).append(", ");
    return sb.append(getValue()).append(']').toString();
  }

}
