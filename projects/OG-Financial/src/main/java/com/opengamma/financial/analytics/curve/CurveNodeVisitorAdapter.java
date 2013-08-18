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
import com.opengamma.financial.analytics.ircurve.strips.CurveNodeVisitor;
import com.opengamma.financial.analytics.ircurve.strips.DeliverableSwapFutureNode;
import com.opengamma.financial.analytics.ircurve.strips.DiscountFactorNode;
import com.opengamma.financial.analytics.ircurve.strips.FRANode;
import com.opengamma.financial.analytics.ircurve.strips.FXForwardNode;
import com.opengamma.financial.analytics.ircurve.strips.RateFutureNode;
import com.opengamma.financial.analytics.ircurve.strips.SwapNode;
import com.opengamma.financial.analytics.ircurve.strips.ZeroCouponInflationNode;
import com.opengamma.util.ArgumentChecker;

/**
 * Adapter for visiting all concrete curve node types.
 *
 * @param <T> The return type for this visitor.
 */
public class CurveNodeVisitorAdapter<T> implements CurveNodeVisitor<T> {

  /**
   * Creates builder for a {@link CurveNodeVisitor}. The underlying visitor
   * has no implemented methods.
   * @param <T> The return type of the visitor
   * @return A builder
   */
  public static <T> Builder<T> builder() {
    return new Builder<>();
  }

  /**
   * Creates builder for a {@link CurveNodeVisitor} that uses the supplied
   * visitor as the initial underlying
   * @param <T> The return type of the visitor
   * @param visitor The underlying visitor, not null
   * @return A builder
   */
  public static <T> Builder<T> builder(final CurveNodeVisitor<T> visitor) {
    return new Builder<>(visitor);
  }

  @Override
  public T visitCashNode(final CashNode node) {
    throw new UnsupportedOperationException(getUnsupportedOperationMessage(getClass(), node));
  }

  @Override
  public T visitContinuouslyCompoundedRateNode(final ContinuouslyCompoundedRateNode node) {
    throw new UnsupportedOperationException(getUnsupportedOperationMessage(getClass(), node));
  }

  @Override
  public T visitCreditSpreadNode(final CreditSpreadNode node) {
    throw new UnsupportedOperationException(getUnsupportedOperationMessage(getClass(), node));
  }

  @Override
  public T visitDeliverableSwapFutureNode(final DeliverableSwapFutureNode node) {
    throw new UnsupportedOperationException(getUnsupportedOperationMessage(getClass(), node));
  }

  @Override
  public T visitDiscountFactorNode(final DiscountFactorNode node) {
    throw new UnsupportedOperationException(getUnsupportedOperationMessage(getClass(), node));
  }

  @Override
  public T visitFRANode(final FRANode node) {
    throw new UnsupportedOperationException(getUnsupportedOperationMessage(getClass(), node));
  }

  @Override
  public T visitFXForwardNode(final FXForwardNode node) {
    throw new UnsupportedOperationException(getUnsupportedOperationMessage(getClass(), node));
  }

  @Override
  public T visitRateFutureNode(final RateFutureNode node) {
    throw new UnsupportedOperationException(getUnsupportedOperationMessage(getClass(), node));
  }

  @Override
  public T visitSwapNode(final SwapNode node) {
    throw new UnsupportedOperationException(getUnsupportedOperationMessage(getClass(), node));
  }

  @Override
  public T visitZeroCouponInflationNode(final ZeroCouponInflationNode node) {
    throw new UnsupportedOperationException(getUnsupportedOperationMessage(getClass(), node));
  }

  /**
   * Generic message for unsupported methods in {@link CurveNodeVisitor} implementations
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
    private CurveNodeVisitor<T> _visitor;

    /**
     * Constructs a visitor with no methods implemented.
     */
    protected Builder() {
      _visitor = new CurveNodeVisitorAdapter<>();
    }

    /**
     * Accepts a visitor.
     * @param visitor The visitor, not null
     */
    protected Builder(final CurveNodeVisitor<T> visitor) {
      ArgumentChecker.notNull(visitor, "visitor");
      _visitor = visitor;
    }

    public Builder<T> cashNodeVisitor(final CurveNodeVisitor<T> visitor) {
      _visitor = new CurveNodeVisitorDelegate<T>(_visitor) {

        @Override
        public T visitCashNode(final CashNode node) {
          return visitor.visitCashNode(node);
        }
      };
      return this;
    }

    public Builder<T> continuouslyCompoundedRateNode(final CurveNodeVisitor<T> visitor) {
      _visitor = new CurveNodeVisitorDelegate<T>(_visitor) {

        @Override
        public T visitContinuouslyCompoundedRateNode(final ContinuouslyCompoundedRateNode node) {
          return visitor.visitContinuouslyCompoundedRateNode(node);
        }
      };
      return this;
    }

    public Builder<T> creditSpreadNode(final CurveNodeVisitor<T> visitor) {
      _visitor = new CurveNodeVisitorDelegate<T>(_visitor) {

        @Override
        public T visitCreditSpreadNode(final CreditSpreadNode node) {
          return visitor.visitCreditSpreadNode(node);
        }
      };
      return this;
    }

    public Builder<T> deliverableSwapFutureNode(final CurveNodeVisitor<T> visitor) {
      _visitor = new CurveNodeVisitorDelegate<T>(_visitor) {

        @Override
        public T visitDeliverableSwapFutureNode(final DeliverableSwapFutureNode node) {
          return visitor.visitDeliverableSwapFutureNode(node);
        }
      };
      return this;
    }

    public Builder<T> discountFactorNode(final CurveNodeVisitor<T> visitor) {
      _visitor = new CurveNodeVisitorDelegate<T>(_visitor) {

        @Override
        public T visitDiscountFactorNode(final DiscountFactorNode node) {
          return visitor.visitDiscountFactorNode(node);
        }
      };
      return this;
    }

    public Builder<T> fraNode(final CurveNodeVisitor<T> visitor) {
      _visitor = new CurveNodeVisitorDelegate<T>(_visitor) {

        @Override
        public T visitFRANode(final FRANode node) {
          return visitor.visitFRANode(node);
        }
      };
      return this;
    }

    public Builder<T> fxForwardNode(final CurveNodeVisitor<T> visitor) {
      _visitor = new CurveNodeVisitorDelegate<T>(_visitor) {

        @Override
        public T visitFXForwardNode(final FXForwardNode node) {
          return visitor.visitFXForwardNode(node);
        }
      };
      return this;
    }

    public Builder<T> rateFutureNode(final CurveNodeVisitor<T> visitor) {
      _visitor = new CurveNodeVisitorDelegate<T>(_visitor) {

        @Override
        public T visitRateFutureNode(final RateFutureNode node) {
          return visitor.visitRateFutureNode(node);
        }
      };
      return this;
    }

    public Builder<T> swapNode(final CurveNodeVisitor<T> visitor) {
      _visitor = new CurveNodeVisitorDelegate<T>(_visitor) {

        @Override
        public T visitSwapNode(final SwapNode node) {
          return visitor.visitSwapNode(node);
        }
      };
      return this;
    }

    public Builder<T> zeroCouponInflationNode(final CurveNodeVisitor<T> visitor) {
      _visitor = new CurveNodeVisitorDelegate<T>(_visitor) {

        @Override
        public T visitZeroCouponInflationNode(final ZeroCouponInflationNode node) {
          return visitor.visitZeroCouponInflationNode(node);
        }
      };
      return this;
    }

    public CurveNodeVisitor<T> create() {
      return new CurveNodeVisitorDelegate<>(_visitor);
    }
  }
}
