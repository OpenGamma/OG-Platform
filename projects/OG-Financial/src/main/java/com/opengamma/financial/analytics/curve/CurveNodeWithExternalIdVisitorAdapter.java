/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.curve;

import com.opengamma.financial.analytics.ircurve.strips.CashNode;
import com.opengamma.financial.analytics.ircurve.strips.ContinuouslyCompoundedRateNode;
import com.opengamma.financial.analytics.ircurve.strips.CreditSpreadNode;
import com.opengamma.financial.analytics.ircurve.strips.CurveNode;
import com.opengamma.financial.analytics.ircurve.strips.CurveNodeWithExternalIdVisitor;
import com.opengamma.financial.analytics.ircurve.strips.DeliverableSwapFutureNode;
import com.opengamma.financial.analytics.ircurve.strips.DiscountFactorNode;
import com.opengamma.financial.analytics.ircurve.strips.FRANode;
import com.opengamma.financial.analytics.ircurve.strips.FXForwardNode;
import com.opengamma.financial.analytics.ircurve.strips.RateFutureNode;
import com.opengamma.financial.analytics.ircurve.strips.SwapNode;
import com.opengamma.financial.analytics.ircurve.strips.ZeroCouponInflationNode;
import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;

/**
 * Adapter for visiting all concrete curve node types.
 *
 * @param <T> The return type for this visitor.
 */
public class CurveNodeWithExternalIdVisitorAdapter<T> implements CurveNodeWithExternalIdVisitor<T> {

  /**
   * Creates builder for a {@link CurveNodeWithExternalIdVisitor}. The underlying visitor
   * has no implemented methods.
   *
   * @param <T> The return type of the visitor
   * @return A builder
   */
  public static <T> Builder<T> builder() {
    return new Builder<>();
  }

  /**
   * Creates builder for a {@link CurveNodeWithExternalIdVisitor} that uses the supplied
   * visitor as the initial underlying
   *
   * @param <T> The return type of the visitor
   * @param visitor The underlying visitor, not null
   * @return A builder
   */
  public static <T> Builder<T> builder(final CurveNodeWithExternalIdVisitor<T> visitor) {
    return new Builder<>(visitor);
  }

  @Override
  public T visitCashNode(final CashNode node, ExternalId externalId) {
    throw new UnsupportedOperationException(getUnsupportedOperationMessage(getClass(), node));
  }

  @Override
  public T visitContinuouslyCompoundedRateNode(final ContinuouslyCompoundedRateNode node, ExternalId externalId) {
    throw new UnsupportedOperationException(getUnsupportedOperationMessage(getClass(), node));
  }

  @Override
  public T visitCreditSpreadNode(final CreditSpreadNode node, ExternalId externalId) {
    throw new UnsupportedOperationException(getUnsupportedOperationMessage(getClass(), node));
  }

  @Override
  public T visitDeliverableSwapFutureNode(final DeliverableSwapFutureNode node, ExternalId externalId) {
    throw new UnsupportedOperationException(getUnsupportedOperationMessage(getClass(), node));
  }

  @Override
  public T visitDiscountFactorNode(final DiscountFactorNode node, ExternalId externalId) {
    throw new UnsupportedOperationException(getUnsupportedOperationMessage(getClass(), node));
  }

  @Override
  public T visitFRANode(final FRANode node, ExternalId externalId) {
    throw new UnsupportedOperationException(getUnsupportedOperationMessage(getClass(), node));
  }

  @Override
  public T visitFXForwardNode(final FXForwardNode node, ExternalId externalId) {
    throw new UnsupportedOperationException(getUnsupportedOperationMessage(getClass(), node));
  }

  @Override
  public T visitRateFutureNode(final RateFutureNode node, ExternalId externalId) {
    throw new UnsupportedOperationException(getUnsupportedOperationMessage(getClass(), node));
  }

  @Override
  public T visitSwapNode(final SwapNode node, ExternalId externalId) {
    throw new UnsupportedOperationException(getUnsupportedOperationMessage(getClass(), node));
  }

  @Override
  public T visitZeroCouponInflationNode(final ZeroCouponInflationNode node, ExternalId externalId) {
    throw new UnsupportedOperationException(getUnsupportedOperationMessage(getClass(), node));
  }

  /**
   * Generic message for unsupported methods in {@link CurveNodeWithExternalIdVisitor} implementations
   *
   * @param clazz the implementation class, not null
   * @param node the curve node, not null
   * @return the message, not null;
   */
  public static String getUnsupportedOperationMessage(final Class<?> clazz, final CurveNode node) {
    ArgumentChecker.notNull(clazz, "implementation class");
    ArgumentChecker.notNull(node, "financial security");
    return "This visitor (" + clazz.getName() + ") does not support " + node.getClass().getName() + " nodes.";
  }

  /**
   * Builder class for this visitor adapter.
   *
   * @param <T> The return type of the visitor.
   */
  public static class Builder<T> {

    /** The visitor */
    private CurveNodeWithExternalIdVisitor<T> _visitor;

    /** Constructs a visitor with no methods implemented. */
    protected Builder() {
      _visitor = new CurveNodeWithExternalIdVisitorAdapter<>();
    }

    /**
     * Accepts a visitor.
     *
     * @param visitor The visitor, not null
     */
    protected Builder(final CurveNodeWithExternalIdVisitor<T> visitor) {
      ArgumentChecker.notNull(visitor, "visitor");
      _visitor = visitor;
    }

    public Builder<T> cashNodeVisitor(final CurveNodeWithExternalIdVisitor<T> visitor, ExternalId externalId) {
      _visitor = new CurveNodeWithExternalIdVisitorDelegate<T>(_visitor) {

        @Override
        public T visitCashNode(final CashNode node, final ExternalId externalId) {
          return visitor.visitCashNode(node, externalId);
        }
      };
      return this;
    }

    public Builder<T> continuouslyCompoundedRateNode(final CurveNodeWithExternalIdVisitor<T> visitor,
                                                     ExternalId externalId) {
      _visitor = new CurveNodeWithExternalIdVisitorDelegate<T>(_visitor) {

        @Override
        public T visitContinuouslyCompoundedRateNode(final ContinuouslyCompoundedRateNode node,
                                                     final ExternalId externalId) {
          return visitor.visitContinuouslyCompoundedRateNode(node, externalId);
        }
      };
      return this;
    }

    public Builder<T> creditSpreadNode(final CurveNodeWithExternalIdVisitor<T> visitor, ExternalId externalId) {
      _visitor = new CurveNodeWithExternalIdVisitorDelegate<T>(_visitor) {

        @Override
        public T visitCreditSpreadNode(final CreditSpreadNode node, ExternalId externalId) {
          return visitor.visitCreditSpreadNode(node, externalId);
        }
      };
      return this;
    }

    public Builder<T> deliverableSwapFutureNode(final CurveNodeWithExternalIdVisitor<T> visitor,
                                                ExternalId externalId) {
      _visitor = new CurveNodeWithExternalIdVisitorDelegate<T>(_visitor) {

        @Override
        public T visitDeliverableSwapFutureNode(final DeliverableSwapFutureNode node, ExternalId externalId) {
          return visitor.visitDeliverableSwapFutureNode(node, externalId);
        }
      };
      return this;
    }

    public Builder<T> discountFactorNode(final CurveNodeWithExternalIdVisitor<T> visitor, ExternalId externalId) {
      _visitor = new CurveNodeWithExternalIdVisitorDelegate<T>(_visitor) {

        @Override
        public T visitDiscountFactorNode(final DiscountFactorNode node, ExternalId externalId) {
          return visitor.visitDiscountFactorNode(node, externalId);
        }
      };
      return this;
    }

    public Builder<T> fraNode(final CurveNodeWithExternalIdVisitor<T> visitor, ExternalId externalId) {
      _visitor = new CurveNodeWithExternalIdVisitorDelegate<T>(_visitor) {

        @Override
        public T visitFRANode(final FRANode node, ExternalId externalId) {
          return visitor.visitFRANode(node, externalId);
        }
      };
      return this;
    }

    public Builder<T> fxForwardNode(final CurveNodeWithExternalIdVisitor<T> visitor, ExternalId externalId) {
      _visitor = new CurveNodeWithExternalIdVisitorDelegate<T>(_visitor) {

        @Override
        public T visitFXForwardNode(final FXForwardNode node, ExternalId externalId) {
          return visitor.visitFXForwardNode(node, externalId);
        }
      };
      return this;
    }

    public Builder<T> rateFutureNode(final CurveNodeWithExternalIdVisitor<T> visitor, ExternalId externalId) {
      _visitor = new CurveNodeWithExternalIdVisitorDelegate<T>(_visitor) {

        @Override
        public T visitRateFutureNode(final RateFutureNode node, ExternalId externalId) {
          return visitor.visitRateFutureNode(node, externalId);
        }
      };
      return this;
    }

    public Builder<T> swapNode(final CurveNodeWithExternalIdVisitor<T> visitor, ExternalId externalId) {
      _visitor = new CurveNodeWithExternalIdVisitorDelegate<T>(_visitor) {

        @Override
        public T visitSwapNode(final SwapNode node, ExternalId externalId) {
          return visitor.visitSwapNode(node, externalId);
        }
      };
      return this;
    }

    public Builder<T> zeroCouponInflationNode(final CurveNodeWithExternalIdVisitor<T> visitor, ExternalId externalId) {
      _visitor = new CurveNodeWithExternalIdVisitorDelegate<T>(_visitor) {

        @Override
        public T visitZeroCouponInflationNode(final ZeroCouponInflationNode node, ExternalId externalId) {
          return visitor.visitZeroCouponInflationNode(node, externalId);
        }
      };
      return this;
    }

    public CurveNodeWithExternalIdVisitor<T> create() {
      return new CurveNodeWithExternalIdVisitorDelegate<>(_visitor);
    }
  }
}
