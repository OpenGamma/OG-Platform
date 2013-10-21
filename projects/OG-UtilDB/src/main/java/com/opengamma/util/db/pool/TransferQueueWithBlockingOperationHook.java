/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.db.pool;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import jsr166y.TransferQueue;

import com.opengamma.util.async.BlockingOperation;

/**
 * Wrapper around a {@link TransferQueue} instance that will make calls to {@link BlockingOperation#wouldBlock} before any potentially blocking operations.
 * 
 * @param <T> the type of elements in the queue
 */
public final class TransferQueueWithBlockingOperationHook<T> implements TransferQueue<T> {

  private final TransferQueue<T> _underlying;

  public TransferQueueWithBlockingOperationHook(final TransferQueue<T> underlying) {
    _underlying = underlying;
  }

  protected TransferQueue<T> getUnderlying() {
    return _underlying;
  }

  @Override
  public boolean add(final T e) {
    return getUnderlying().add(e);
  }

  @Override
  public boolean offer(final T e) {
    return getUnderlying().offer(e);
  }

  @Override
  public void put(final T e) throws InterruptedException {
    if (!offer(e)) {
      BlockingOperation.wouldBlock();
      getUnderlying().put(e);
    }
  }

  @Override
  public boolean offer(final T e, final long timeout, final TimeUnit unit) throws InterruptedException {
    if (offer(e)) {
      return true;
    } else {
      BlockingOperation.wouldBlock();
      return getUnderlying().offer(e, timeout, unit);
    }
  }

  @Override
  public T take() throws InterruptedException {
    final T result = poll();
    if (result != null) {
      return result;
    } else {
      BlockingOperation.wouldBlock();
      return getUnderlying().take();
    }
  }

  @Override
  public T poll(final long timeout, final TimeUnit unit) throws InterruptedException {
    final T result = poll();
    if (result != null) {
      return result;
    } else {
      BlockingOperation.wouldBlock();
      return getUnderlying().poll(timeout, unit);
    }
  }

  @Override
  public int remainingCapacity() {
    return getUnderlying().remainingCapacity();
  }

  @Override
  public boolean remove(final Object o) {
    return getUnderlying().remove(o);
  }

  @Override
  public boolean contains(final Object o) {
    return getUnderlying().contains(o);
  }

  @Override
  public int drainTo(final Collection<? super T> c) {
    return getUnderlying().drainTo(c);
  }

  @Override
  public int drainTo(final Collection<? super T> c, int maxElements) {
    return getUnderlying().drainTo(c, maxElements);
  }

  @Override
  public T remove() {
    return getUnderlying().remove();
  }

  @Override
  public T poll() {
    return getUnderlying().poll();
  }

  @Override
  public T element() {
    return getUnderlying().element();
  }

  @Override
  public T peek() {
    return getUnderlying().peek();
  }

  @Override
  public int size() {
    return getUnderlying().size();
  }

  @Override
  public boolean isEmpty() {
    return getUnderlying().isEmpty();
  }

  @Override
  public Iterator<T> iterator() {
    return getUnderlying().iterator();
  }

  @Override
  public Object[] toArray() {
    return getUnderlying().toArray();
  }

  @Override
  public <X> X[] toArray(final X[] a) {
    return getUnderlying().toArray(a);
  }

  @Override
  public boolean containsAll(final Collection<?> c) {
    return getUnderlying().containsAll(c);
  }

  @Override
  public boolean addAll(final Collection<? extends T> c) {
    return getUnderlying().addAll(c);
  }

  @Override
  public boolean removeAll(final Collection<?> c) {
    return getUnderlying().removeAll(c);
  }

  @Override
  public boolean retainAll(final Collection<?> c) {
    return getUnderlying().retainAll(c);
  }

  @Override
  public void clear() {
    getUnderlying().clear();
  }

  @Override
  public boolean tryTransfer(final T e) {
    return getUnderlying().tryTransfer(e);
  }

  @Override
  public void transfer(final T e) throws InterruptedException {
    BlockingOperation.wouldBlock();
    getUnderlying().transfer(e);
  }

  @Override
  public boolean tryTransfer(final T e, final long timeout, final TimeUnit unit) throws InterruptedException {
    if (tryTransfer(e)) {
      return true;
    } else {
      BlockingOperation.wouldBlock();
      return getUnderlying().tryTransfer(e, timeout, unit);
    }
  }

  @Override
  public boolean hasWaitingConsumer() {
    return getUnderlying().hasWaitingConsumer();
  }

  @Override
  public int getWaitingConsumerCount() {
    return getUnderlying().getWaitingConsumerCount();
  }
}
