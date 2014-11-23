package com.opengamma.sesame.engine;

import static com.opengamma.sesame.config.ConfigBuilder.argument;
import static com.opengamma.sesame.config.ConfigBuilder.arguments;
import static com.opengamma.sesame.config.ConfigBuilder.column;
import static com.opengamma.sesame.config.ConfigBuilder.config;
import static com.opengamma.sesame.config.ConfigBuilder.configureView;
import static com.opengamma.sesame.config.ConfigBuilder.function;
import static com.opengamma.sesame.config.ConfigBuilder.implementations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNotSame;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertSame;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.Callable;

import org.testng.annotations.Test;

import com.codahale.metrics.MetricRegistry;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.opengamma.core.position.Trade;
import com.opengamma.engine.marketdata.spec.LiveMarketDataSpecification;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.service.ServiceContext;
import com.opengamma.service.ThreadLocalServiceContext;
import com.opengamma.sesame.DirectExecutorService;
import com.opengamma.sesame.EngineTestUtils;
import com.opengamma.sesame.cache.CacheKey;
import com.opengamma.sesame.cache.Cacheable;
import com.opengamma.sesame.cache.FunctionCache;
import com.opengamma.sesame.cache.NoOpCacheInvalidator;
import com.opengamma.sesame.config.FunctionModelConfig;
import com.opengamma.sesame.config.ViewConfig;
import com.opengamma.sesame.function.AvailableImplementations;
import com.opengamma.sesame.function.AvailableImplementationsImpl;
import com.opengamma.sesame.function.AvailableOutputs;
import com.opengamma.sesame.function.AvailableOutputsImpl;
import com.opengamma.sesame.function.Output;
import com.opengamma.sesame.marketdata.CycleMarketDataFactory;
import com.opengamma.sesame.marketdata.DefaultStrategyAwareMarketDataSource;
import com.opengamma.sesame.marketdata.MapMarketDataSource;
import com.opengamma.sesame.marketdata.MarketDataSource;
import com.opengamma.sesame.marketdata.StrategyAwareMarketDataSource;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the behaviour of {@link ViewFactory} WRT cache behaviour.
 */
@Test(groups = TestGroup.UNIT)
public class ViewFactoryCacheTest {

  /**
   * checks that cached values created by a view are available next time it's run.
   */
  private void cacheIsSharedBetweenRuns(Class<?> implClass) {
    ViewConfig viewConfig =
        configureView(
            "view name",
            column(
                "Foo",
                config(
                    implementations(TestFn.class, implClass),
                    arguments(function(implClass, argument("s", "s"))))));
    ViewFactory viewFactory = createViewFactory(TestFn.class);
    View view = viewFactory.createView(viewConfig, String.class);
    CycleMarketDataFactory cycleMarketDataFactory = mock(CycleMarketDataFactory.class);
    when(cycleMarketDataFactory.getPrimaryMarketDataSource()).thenReturn(mock(StrategyAwareMarketDataSource.class));
    CycleArguments cycleArguments = CycleArguments.builder(cycleMarketDataFactory).build();
    Results results1 = view.run(cycleArguments, ImmutableList.of("bar"));
    Results results2 = view.run(cycleArguments, ImmutableList.of("bar"));
    assertSame(results1.get(0, 0).getResult().getValue(), results2.get(0, 0).getResult().getValue());
  }

  /**
   * checks that cached values created by a view are available next time it's run.
   */
  @Test
  public void cacheIsSharedBetweenRuns() {
    cacheIsSharedBetweenRuns(AutomaticCaching.class);
  }

  /**
   * checks that values cached by user code in a {@link FunctionCache} are available next time it's run.
   */
  @Test
  public void functionCacheIsSharedBetweenRuns() {
    cacheIsSharedBetweenRuns(ExplicitCaching.class);
  }

  /**
   * checks that no caching is done when the function services don't include caching.
   */
  private void noCachingWhenCacheServiceNotIncluded(Class<?> implClass) {
    ViewConfig viewConfig =
        configureView(
            "view name",
            column(
                "Foo",
                config(
                    implementations(TestFn.class, implClass),
                    arguments(function(implClass, argument("s", "s"))))));
    ViewFactory viewFactory = createViewFactory(TestFn.class);
    View view = viewFactory.createView(viewConfig, FunctionService.NONE, String.class);
    CycleMarketDataFactory cycleMarketDataFactory = mock(CycleMarketDataFactory.class);
    when(cycleMarketDataFactory.getPrimaryMarketDataSource()).thenReturn(mock(StrategyAwareMarketDataSource.class));
    CycleArguments cycleArguments = CycleArguments.builder(cycleMarketDataFactory).build();
    Results results1 = view.run(cycleArguments, ImmutableList.of("bar"));
    Results results2 = view.run(cycleArguments, ImmutableList.of("bar"));
    assertNotSame(results1.get(0, 0).getResult().getValue(), results2.get(0, 0).getResult().getValue());
  }

  @Test
  public void noCachingWhenCacheServiceNotIncluded() {
    noCachingWhenCacheServiceNotIncluded(AutomaticCaching.class);
  }

  @Test
  public void noFunctionCachingWhenCacheServiceNotIncluded() {
    noCachingWhenCacheServiceNotIncluded(ExplicitCaching.class);
  }

  /**
   * Checks that whe we are capturing the cycle we do not use cached
   * values. (If we did we would not be able to intercept calls to the
   * sources as they would not be called.)
   */
  private void cacheIsNotSharedBetweenRunsWhenCapturingCycle(Class<?> implClass) {
    ThreadLocalServiceContext.init(ServiceContext.of(ImmutableMap.<Class<?>, Object>of()));
    ViewConfig viewConfig =
        configureView(
            "view name",
            column(
                "Foo",
                config(
                    implementations(TestFn.class, implClass),
                    arguments(function(implClass, argument("s", "s"))))));
    ViewFactory viewFactory = createViewFactory(TestFn.class);
    View view = viewFactory.createView(viewConfig, String.class);
    CycleMarketDataFactory cycleMarketDataFactory = mock(CycleMarketDataFactory.class);
    when(cycleMarketDataFactory.getPrimaryMarketDataSource()).thenReturn(
        new DefaultStrategyAwareMarketDataSource(LiveMarketDataSpecification.LIVE_SPEC, MapMarketDataSource.of()));
    CycleArguments cycleArguments = CycleArguments.builder(cycleMarketDataFactory).captureInputs(true).build();
    Results results1 = view.run(cycleArguments, ImmutableList.of("bar"));
    Results results2 = view.run(cycleArguments, ImmutableList.of("bar"));
    assertNotSame(results1.get(0, 0).getResult().getValue(), results2.get(0, 0).getResult().getValue());
    assertNotNull(results2.getViewInputs());
    assertNotNull(results1.getViewInputs());
  }

  /**
   * Checks that whe we are capturing the cycle we do not use cached
   * values. (If we did we would not be able to intercept calls to the
   * sources as they would not be called.)
   */
  @Test
  public void cacheIsNotSharedBetweenRunsWhenCapturingCycle() {
    cacheIsNotSharedBetweenRunsWhenCapturingCycle(AutomaticCaching.class);
  }

  /**
   * Checks that whe we are capturing the cycle we do not use cached
   * values. (If we did we would not be able to intercept calls to the
   * sources as they would not be called.)
   */
  @Test
  public void functionCacheIsNotSharedBetweenRunsWhenCapturingCycle() {
    cacheIsNotSharedBetweenRunsWhenCapturingCycle(ExplicitCaching.class);
  }

  /**
   * checks that cached values created by a view are available to other views built by the same view factory.
   */
  private void cacheIsSharedBetweenViews(Class<?> implClass) {
    ViewConfig viewConfig =
        configureView(
            "view name",
            column(
                "Foo",
                config(
                    implementations(TestFn.class, implClass),
                    arguments(function(implClass, argument("s", "s"))))));
    ViewFactory viewFactory = createViewFactory(TestFn.class);
    View view1 = viewFactory.createView(viewConfig, String.class);
    View view2 = viewFactory.createView(viewConfig, String.class);
    CycleMarketDataFactory cycleMarketDataFactory = mock(CycleMarketDataFactory.class);
    when(cycleMarketDataFactory.getPrimaryMarketDataSource()).thenReturn(mock(StrategyAwareMarketDataSource.class));
    CycleArguments cycleArguments = CycleArguments.builder(cycleMarketDataFactory).build();
    Results results1 = view1.run(cycleArguments, ImmutableList.of("bar"));
    Results results2 = view2.run(cycleArguments, ImmutableList.of("bar"));
    assertSame(results1.get(0, 0).getResult().getValue(), results2.get(0, 0).getResult().getValue());
  }

  /**
   * checks that cached values created by a view are available to other views built by the same view factory.
   */
  @Test
  public void cacheIsSharedBetweenViews() {
    cacheIsSharedBetweenViews(AutomaticCaching.class);
  }

  /**
   * checks that cached values created by a view are available to other views built by the same view factory.
   */
  @Test
  public void functionCacheIsSharedBetweenViews() {
    cacheIsSharedBetweenViews(ExplicitCaching.class);
  }

  /**
   * tests clearing the cache causes a value to be recalculated in the next cycle in a single view.
   */
  @Test
  public void clearCacheSameView() {
    ViewConfig viewConfig =
        configureView(
            "test view",
            config(implementations(CacheFn1.class, Impl1.class,
                                   CacheFn2.class, Impl2.class,
                                   RootFn.class, RootImpl.class)),
            column("Foo"));

    ViewFactory viewFactory = createViewFactory(RootFn.class);
    View view = viewFactory.createView(viewConfig, EquitySecurity.class);
    CycleMarketDataFactory cycleMarketDataFactory = mock(CycleMarketDataFactory.class);
    when(cycleMarketDataFactory.getPrimaryMarketDataSource()).thenReturn(mock(MarketDataSource.class));
    CycleArguments cycleArguments = CycleArguments.builder(cycleMarketDataFactory).build();
    Trade equityTrade = EngineTestUtils.createEquityTrade();

    Results results1 = view.run(cycleArguments, ImmutableList.of(equityTrade));
    Object value1 = results1.get(0, 0).getResult().getValue();

    Results results2 = view.run(cycleArguments, ImmutableList.of(equityTrade));
    Object value2 = results2.get(0, 0).getResult().getValue();

    assertEquals(value1, value2);

    viewFactory.clearCache();
    Results results3 = view.run(cycleArguments, ImmutableList.of(equityTrade));
    Object value3 = results3.get(0, 0).getResult().getValue();
    assertFalse(value1.equals(value3));
  }

  /**
   * tests clearing the cache causes a value to be recalculated in the next cycle when the value is shared
   * between two views.
   */
  @Test
  public void clearCacheDifferentView() {
    ViewConfig viewConfig =
        configureView(
            "test view",
            config(implementations(CacheFn1.class, Impl1.class,
                                   CacheFn2.class, Impl2.class,
                                   RootFn.class, RootImpl.class)),
            column("Foo"));

    ViewFactory viewFactory = createViewFactory(RootFn.class);
    View view1 = viewFactory.createView(viewConfig, EquitySecurity.class);
    View view2 = viewFactory.createView(viewConfig, EquitySecurity.class);

    CycleMarketDataFactory cycleMarketDataFactory = mock(CycleMarketDataFactory.class);
    when(cycleMarketDataFactory.getPrimaryMarketDataSource()).thenReturn(mock(MarketDataSource.class));
    CycleArguments cycleArguments = CycleArguments.builder(cycleMarketDataFactory).build();
    Trade equityTrade = EngineTestUtils.createEquityTrade();

    Results results1 = view1.run(cycleArguments, ImmutableList.of(equityTrade));
    Object value1 = results1.get(0, 0).getResult().getValue();

    Results results2 = view2.run(cycleArguments, ImmutableList.of(equityTrade));
    Object value2 = results2.get(0, 0).getResult().getValue();

    assertEquals(value1, value2);

    viewFactory.clearCache();

    Results results3 = view2.run(cycleArguments, ImmutableList.of(equityTrade));
    Object value3 = results3.get(0, 0).getResult().getValue();
    assertFalse(value1.equals(value3));
  }

  /**
   * tests that clearing the cache doesn't affect a running calculation cycle
   */
  @Test
  public void clearCacheDuringCycle() {
    ViewFactory viewFactory = createViewFactory(CacheClearingFn.class);
    ViewConfig viewConfig =
        configureView(
            "test view",
            config(implementations(CacheFn1.class, Impl1.class,
                                   CacheFn2.class, Impl2.class,
                                   RootFn.class, RootImpl.class),
                   arguments(function(CacheClearingFn.class, argument("viewFactory", viewFactory)))),
            column("Bar"));
    View view = viewFactory.createView(viewConfig, EquitySecurity.class);
    CycleMarketDataFactory cycleMarketDataFactory = mock(CycleMarketDataFactory.class);
    when(cycleMarketDataFactory.getPrimaryMarketDataSource()).thenReturn(mock(MarketDataSource.class));
    CycleArguments cycleArguments = CycleArguments.builder(cycleMarketDataFactory).build();
    Trade equityTrade = EngineTestUtils.createEquityTrade();

    // check that the same result is return from 2 calls to TestFn.foo() even if the cache is cleared between
    Results results1 = view.run(cycleArguments, ImmutableList.of(equityTrade));
    List<?> values1 = (List<?>) results1.get(0, 0).getResult().getValue();
    assertEquals(values1.get(0), values1.get(1));

    // check that the result is different on the second run as a result of the cache being cleared on the first
    Results results2 = view.run(cycleArguments, ImmutableList.of(equityTrade));
    List<?> values2 = (List<?>) results2.get(0, 0).getResult().getValue();
    assertFalse(values1.get(0).equals(values2.get(0)));
  }

  private ViewFactory createViewFactory(Class<?> function) {
    AvailableOutputs availableOutputs = new AvailableOutputsImpl(String.class, EquitySecurity.class);
    AvailableImplementations availableImplementations = new AvailableImplementationsImpl();
    availableOutputs.register(function);
    availableImplementations.register(AutomaticCaching.class);
    return new ViewFactory(new DirectExecutorService(),
                           ComponentMap.EMPTY,
                           availableOutputs,
                           availableImplementations,
                           FunctionModelConfig.EMPTY,
                           EnumSet.of(FunctionService.CACHING),
                           EngineTestUtils.createCacheBuilder(),
                           new NoOpCacheInvalidator(),
                           Optional.<MetricRegistry>absent());
  }

  public interface TestFn {

    @Output("Foo")
    Object foo(String arg);
  }

  public static class AutomaticCaching implements TestFn {

    private final String _s;

    public AutomaticCaching(String s) {
      _s = s;
    }

    @Cacheable
    @Override
    public Object foo(String arg) {
      return _s + new Object();
    }
  }

  public static class ExplicitCaching implements TestFn {

    private final String _s;
    private final FunctionCache _cache;

    public ExplicitCaching(String s, FunctionCache cache) {
      _s = s;
      _cache = cache;
    }

    @Override
    public Object foo(String arg) {
      CacheKey key = CacheKey.of(this, "the cache key");
      return _cache.get(key, new Callable<Object>() {
        @Override
        public Object call() throws Exception {
          return _s + new Object();
        }
      });
    }
  }

  public interface CacheFn1 {

    @Cacheable
    int bar(EquitySecurity arg);
  }

  public static class Impl1 implements CacheFn1 {

    private final CacheFn2 _cacheFn2;

    public Impl1(CacheFn2 cacheFn2) {
      _cacheFn2 = cacheFn2;
    }

    @Override
    public int bar(EquitySecurity arg) {
      return _cacheFn2.bar(arg);
    }
  }

  public interface CacheFn2 {

    @Cacheable
    int bar(EquitySecurity arg);
  }

  public static class Impl2 implements CacheFn2 {

    private static int i = 0;

    @Override
    public int bar(EquitySecurity arg) {
      return i++;
    }
  }

  public static class CacheClearingFn {

    private final ViewFactory _viewFactory;
    private final RootFn _rootFn;

    public CacheClearingFn(ViewFactory viewFactory, RootFn rootFn) {
      _viewFactory = viewFactory;
      _rootFn = rootFn;
    }

    /**
     * Calls {@link CacheFn1#bar} twice, clearing the cache between the calls, and returns a list of the return values.
     * {@code foo()} returns a different value on each call, so the values will only be equal if the second one
     * is retrieved from the cache. This confirms that clearing the cache has no effect on a running cycle.
     */
    @Output("Bar")
    public List<Integer> getValues(EquitySecurity arg) {
      List<Integer> values = new ArrayList<>();
      values.add(_rootFn.foo(arg));
      _viewFactory.clearCache();
      values.add(_rootFn.foo(arg));
      return values;
    }
  }

  public interface RootFn {

    @Output("Foo")
    int foo(EquitySecurity arg);
  }

  public static class RootImpl implements RootFn {

    private final CacheFn1 _cacheFn1;

    public RootImpl(CacheFn1 cacheFn1) {
      _cacheFn1 = cacheFn1;
    }

    @Override
    public int foo(EquitySecurity arg) {
      return _cacheFn1.bar(arg);
    }
  }
}
