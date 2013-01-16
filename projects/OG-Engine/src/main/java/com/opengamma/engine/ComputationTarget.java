/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine;

import java.io.Serializable;
import java.util.Iterator;
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
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.PublicAPI;
import com.opengamma.util.functional.Function1;

/**
 * A fully resolved target, sufficient for computation invocation.
 */
@PublicAPI
public class ComputationTarget implements Serializable {

  // [PLAT-444] move to com.opengamma.engine.target

  /**
   * The null target. This has a type of {@link ComputationTargetType#NULL} and a null value.
   */
  public static final ComputationTarget NULL = new ComputationTarget(ComputationTargetType.NULL, null);

  private static final long serialVersionUID = 1L;

  /**
   * The type of the target.
   */
  private final ComputationTargetType _type;
  /**
   * The actual target.
   */
  private final UniqueIdentifiable _value;
  /**
   * The parent target object identifiers (if any)
   */
  private final List<UniqueId> _context;

  /**
   * The cached hash code.
   */
  private transient volatile int _hashCode;

  /**
   * Creates a target for computation.
   * 
   * @param type the type of the target, not null
   * @param value the target itself, may be null
   * @throws IllegalArgumentException if the value is invalid for the type
   */
  public ComputationTarget(final ComputationTargetType type, final UniqueIdentifiable value) {
    this(type, null, value);
  }

  public ComputationTarget(final ComputationTargetType type, final List<UniqueId> context, final UniqueIdentifiable value) {
    assert type != null;
    assert type.isCompatible(value);
    assert (context == null) ? (ComputationTargetReference.getTypeDepth(type) <= 1) : (ComputationTargetReference.getTypeDepth(type) == context.size() + 1);
    _type = type;
    _value = value;
    _context = context;
  }

  /**
   * Gets the type of the target.
   * 
   * @return the type, not null
   */
  public ComputationTargetType getType() {
    return _type;
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
    final UniqueIdentifiable value = getValue();
    if (value != null) {
      return value.getUniqueId();
    }
    return null;
  }

  /**
   * Returns the identifiers of target that put this target into context. The first identifier in the list is the outermost object.
   * 
   * @return the target context, or null if there is none
   */
  public List<UniqueId> getContextIdentifiers() {
    return _context;
  }

  private ComputationTargetSpecification getContextSpecification(final List<ComputationTargetType> types) {
    final List<UniqueId> uids = getContextIdentifiers();
    assert types.size() == uids.size() + 1;
    ComputationTargetSpecification result = null;
    final Iterator<ComputationTargetType> itrType = types.iterator();
    for (UniqueId uid : uids) {
      final ComputationTargetType type = itrType.next();
      if (result == null) {
        result = new ComputationTargetSpecification(type, uid);
      } else {
        result = result.containing(type, uid);
      }
    }
    return result;
  }

  private static final ComputationTargetTypeVisitor<ComputationTarget, ComputationTargetSpecification> s_getContextSpecification =
      new ComputationTargetTypeVisitor<ComputationTarget, ComputationTargetSpecification>() {

        @Override
        public ComputationTargetSpecification visitMultipleComputationTargetTypes(final Set<ComputationTargetType> types, final ComputationTarget self) {
          throw new IllegalStateException();
        }

        @Override
        public ComputationTargetSpecification visitNestedComputationTargetTypes(final List<ComputationTargetType> types, final ComputationTarget self) {
          return self.getContextSpecification(types);
        }

        @Override
        public ComputationTargetSpecification visitNullComputationTargetType(final ComputationTarget self) {
          return null;
        }

        @Override
        public ComputationTargetSpecification visitClassComputationTargetType(final Class<? extends UniqueIdentifiable> type, final ComputationTarget self) {
          return null;
        }

      };

  private static final ComputationTargetTypeVisitor<ComputationTarget, ComputationTargetSpecification> s_getLeafSpecification =
      new ComputationTargetTypeVisitor<ComputationTarget, ComputationTargetSpecification>() {

        @Override
        public ComputationTargetSpecification visitMultipleComputationTargetTypes(final Set<ComputationTargetType> types, final ComputationTarget self) {
          throw new IllegalStateException();
        }

        @Override
        public ComputationTargetSpecification visitNestedComputationTargetTypes(final List<ComputationTargetType> types, final ComputationTarget self) {
          return new ComputationTargetSpecification(types.get(types.size() - 1), self.getValue().getUniqueId());
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
   * @return the specification of the containing object, or null if there is none
   */
  public ComputationTargetSpecification getContextSpecification() {
    return getType().accept(s_getContextSpecification, this);
  }

  /**
   * Returns the target specification of the leaf target object. The specification of the whole target (as returned by {@link #toSpecification}) is equivalent to
   * {@code getContextSpecification().containing(getLeafSpecification())}.
   * 
   * @return the specification of the leaf object, never null
   */
  public ComputationTargetSpecification getLeafSpecification() {
    return getType().accept(s_getLeafSpecification, this);
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

  private static final ComputationTargetTypeVisitor<ComputationTarget, ComputationTargetSpecification> s_toSpecification =
      new ComputationTargetTypeVisitor<ComputationTarget, ComputationTargetSpecification>() {

        @Override
        public ComputationTargetSpecification visitMultipleComputationTargetTypes(final Set<ComputationTargetType> types, final ComputationTarget self) {
          throw new IllegalStateException();
        }

        @Override
        public ComputationTargetSpecification visitNestedComputationTargetTypes(final List<ComputationTargetType> types, final ComputationTarget self) {
          return self.getContextSpecification().containing(types.get(types.size() - 1), self.getUniqueId());
        }

        @Override
        public ComputationTargetSpecification visitNullComputationTargetType(final ComputationTarget self) {
          return ComputationTargetSpecification.NULL;
        }

        @Override
        public ComputationTargetSpecification visitClassComputationTargetType(final Class<? extends UniqueIdentifiable> type, final ComputationTarget self) {
          return new ComputationTargetSpecification(self.getType(), self.getUniqueId());
        }
      };

  /**
   * Returns a specification that is equivalent to this target.
   * 
   * @return the specification equivalent to this target, not null
   */
  public ComputationTargetSpecification toSpecification() {
    return getType().accept(s_toSpecification, this);
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof ComputationTarget) {
      final ComputationTarget other = (ComputationTarget) obj;
      return _type.equals(other._type)
          && ObjectUtils.equals(_context, other._context)
          && ObjectUtils.equals(_value, other._value);
    }
    return false;
  }

  @Override
  public int hashCode() {
    if (_hashCode == 0) {
      int hc = 1;
      hc += (hc << 4) + _type.hashCode();
      hc += (hc << 4) + ObjectUtils.hashCode(_context);
      hc += (hc << 4) + ObjectUtils.hashCode(_value);
      _hashCode = hc;
    }
    return _hashCode;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append("CT[").append(getType()).append(", ");
    if (getContextIdentifiers() != null) {
      sb.append(getContextIdentifiers()).append(", ");
    }
    return sb.append(getValue()).append(']').toString();
  }

}
