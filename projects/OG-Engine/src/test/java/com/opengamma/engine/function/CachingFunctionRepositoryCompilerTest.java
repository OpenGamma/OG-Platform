/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.function;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotSame;
import static org.testng.AssertJUnit.assertSame;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.DefaultComputationTargetResolver;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.test.TestLifecycle;

@Test(groups = TestGroup.UNIT)
public class CachingFunctionRepositoryCompilerTest {

  private static class MockFunction extends AbstractFunction {

    private final AtomicInteger _compileCount = new AtomicInteger();
    private final Long _validBefore;
    private final Long _validAfter;

    private MockFunction(final String name, final Long validBefore, final Long validAfter) {
      setUniqueId(name);
      _validBefore = validBefore;
      _validAfter = validAfter;
    }

    @Override
    public CompiledFunctionDefinition compile(FunctionCompilationContext context, Instant atInstant) {
      _compileCount.incrementAndGet();
      final AbstractFunction.AbstractCompiledFunction compiled = new AbstractFunction.AbstractCompiledFunction() {

        @Override
        public ComputationTargetType getTargetType() {
          return null;
        }

        @Override
        public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target) {
          return null;
        }

        @Override
        public Set<ValueRequirement> getRequirements(FunctionCompilationContext context, ComputationTarget target, final ValueRequirement desiredValue) {
          return null;
        }

        @Override
        public FunctionInvoker getFunctionInvoker() {
          return null;
        }

        @Override
        public boolean canApplyTo(FunctionCompilationContext context, ComputationTarget target) {
          return false;
        }

      };
      if (_validBefore != null) {
        compiled.setEarliestInvocationTime(atInstant.minusMillis(_validBefore));
      }
      if (_validAfter != null) {
        compiled.setLatestInvocationTime(atInstant.plusMillis(_validAfter));
      }
      return compiled;
    }

    @Override
    public String getShortName() {
      return getUniqueId();
    }

  }

  public void testCompileFunction() {
    TestLifecycle.begin();
    try {
      final InMemoryFunctionRepository functions = new InMemoryFunctionRepository();
      final MockFunction alwaysValid = new MockFunction("always valid", null, null);
      final MockFunction validUntil = new MockFunction("valid until", null, 30L);
      final MockFunction validFrom = new MockFunction("valid from", 30L, null);
      final MockFunction validWithin = new MockFunction("valid within", 30L, 30L);
      functions.addFunction(alwaysValid);
      functions.addFunction(validUntil);
      functions.addFunction(validFrom);
      functions.addFunction(validWithin);
      final CachingFunctionRepositoryCompiler compiler = new CachingFunctionRepositoryCompiler();
      final FunctionCompilationContext context = new FunctionCompilationContext();
      context.setRawComputationTargetResolver(new DefaultComputationTargetResolver());
      final CompiledFunctionService cfs = new CompiledFunctionService(functions, compiler, context);
      TestLifecycle.register(cfs);
      cfs.initialize();
      final Instant timestamp = Instant.now();

      // Everything compiled once
      final CompiledFunctionRepository compiledFunctionsNow = cfs.compileFunctionRepository(timestamp);
      assertSame(alwaysValid, compiledFunctionsNow.getDefinition(alwaysValid.getUniqueId()).getFunctionDefinition());
      assertSame(validUntil, compiledFunctionsNow.getDefinition(validUntil.getUniqueId()).getFunctionDefinition());
      assertSame(validFrom, compiledFunctionsNow.getDefinition(validFrom.getUniqueId()).getFunctionDefinition());
      assertSame(validWithin, compiledFunctionsNow.getDefinition(validWithin.getUniqueId()).getFunctionDefinition());
      assertEquals(1, alwaysValid._compileCount.get());
      assertEquals(1, validUntil._compileCount.get());
      assertEquals(1, validFrom._compileCount.get());
      assertEquals(1, validWithin._compileCount.get());

      // All previously compiled ones still valid, so should use the "previous" cache
      final CompiledFunctionRepository compiledFunctionsAheadWithin = cfs.compileFunctionRepository(timestamp.plusMillis(29L));
      assertSame(compiledFunctionsNow, compiledFunctionsAheadWithin);
      assertEquals(1, alwaysValid._compileCount.get());
      assertEquals(1, validUntil._compileCount.get());
      assertEquals(1, validFrom._compileCount.get());
      assertEquals(1, validWithin._compileCount.get());

      // All previously compiled ones still valid, so should use the "previous" cache
      final CompiledFunctionRepository compiledFunctionsAheadLimit = cfs.compileFunctionRepository(timestamp.plusMillis(30L));
      assertSame(compiledFunctionsNow, compiledFunctionsAheadLimit);
      assertEquals(1, alwaysValid._compileCount.get());
      assertEquals(1, validUntil._compileCount.get());
      assertEquals(1, validFrom._compileCount.get());
      assertEquals(1, validWithin._compileCount.get());

      // Some functions to be recompiled, others from the "previous" cache
      final CompiledFunctionRepository compiledFunctionsAheadBeyond = cfs.compileFunctionRepository(timestamp.plusMillis(31L));
      assertNotSame(compiledFunctionsNow, compiledFunctionsAheadBeyond);
      assertSame(compiledFunctionsNow.getDefinition(alwaysValid.getUniqueId()), compiledFunctionsAheadBeyond.getDefinition(alwaysValid.getUniqueId()));
      assertNotSame(compiledFunctionsNow.getDefinition(validUntil.getUniqueId()), compiledFunctionsAheadBeyond.getDefinition(validUntil.getUniqueId()));
      assertSame(compiledFunctionsNow.getDefinition(validFrom.getUniqueId()), compiledFunctionsAheadBeyond.getDefinition(validFrom.getUniqueId()));
      assertNotSame(compiledFunctionsNow.getDefinition(validWithin.getUniqueId()), compiledFunctionsAheadBeyond.getDefinition(validWithin.getUniqueId()));
      assertEquals(1, alwaysValid._compileCount.get());
      assertEquals(2, validUntil._compileCount.get());
      assertEquals(1, validFrom._compileCount.get());
      assertEquals(2, validWithin._compileCount.get());

      // All previously compiled functions, so should use the "ahead" cache
      final CompiledFunctionRepository compiledFunctionsBeforeWithin = cfs.compileFunctionRepository(timestamp.minusMillis(30L));
      assertSame(compiledFunctionsNow, compiledFunctionsBeforeWithin);
      assertEquals(1, alwaysValid._compileCount.get());
      assertEquals(2, validUntil._compileCount.get());
      assertEquals(1, validFrom._compileCount.get());
      assertEquals(2, validWithin._compileCount.get());

      // Some functions to be recompiled, others from the "ahead" cache
      final CompiledFunctionRepository compiledFunctionsBeforeBeyond = cfs.compileFunctionRepository(timestamp.minusMillis(31L));
      assertNotSame(compiledFunctionsNow, compiledFunctionsBeforeBeyond);
      assertSame(compiledFunctionsNow.getDefinition(alwaysValid.getUniqueId()), compiledFunctionsBeforeBeyond.getDefinition(alwaysValid.getUniqueId()));
      assertSame(compiledFunctionsNow.getDefinition(validUntil.getUniqueId()), compiledFunctionsBeforeBeyond.getDefinition(validUntil.getUniqueId()));
      assertNotSame(compiledFunctionsNow.getDefinition(validFrom.getUniqueId()), compiledFunctionsBeforeBeyond.getDefinition(validFrom.getUniqueId()));
      assertNotSame(compiledFunctionsNow.getDefinition(validWithin.getUniqueId()), compiledFunctionsBeforeBeyond.getDefinition(validWithin.getUniqueId()));
      assertEquals(1, alwaysValid._compileCount.get());
      assertEquals(2, validUntil._compileCount.get());
      assertEquals(2, validFrom._compileCount.get());
      assertEquals(3, validWithin._compileCount.get());
    } finally {
      TestLifecycle.end();
    }
  }

}
