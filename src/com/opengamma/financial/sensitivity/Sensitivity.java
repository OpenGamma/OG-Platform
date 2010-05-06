package com.opengamma.financial.sensitivity;

import com.opengamma.financial.greeks.Order;

public abstract class Sensitivity<T> {
  private final T _underlying;
  private final Order _order;

  public Sensitivity(final T underlying, final Order order) {
    _underlying = underlying;
    _order = order;
  }

  public T getUnderlying() {
    return _underlying;
  }

  public Order getOrder() {
    return _order;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_order == null) ? 0 : _order.hashCode());
    result = prime * result + ((_underlying == null) ? 0 : _underlying.hashCode());
    return result;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(final Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    final Sensitivity<?> other = (Sensitivity<?>) obj;
    if (_order == null) {
      if (other._order != null)
        return false;
    } else if (!_order.equals(other._order))
      return false;
    if (_underlying == null) {
      if (other._underlying != null)
        return false;
    } else if (!_underlying.equals(other._underlying))
      return false;
    return true;
  }

}
