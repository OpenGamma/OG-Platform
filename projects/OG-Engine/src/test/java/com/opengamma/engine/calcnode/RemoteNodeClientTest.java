/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.calcnode;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Arrays;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.testng.annotations.Test;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.cache.AbstractIdentifierMap;
import com.opengamma.engine.cache.CacheSelectHint;
import com.opengamma.engine.cache.IdentifierMap;
import com.opengamma.engine.cache.InMemoryIdentifierMap;
import com.opengamma.engine.calcnode.msg.Execute;
import com.opengamma.engine.calcnode.msg.Failure;
import com.opengamma.engine.calcnode.msg.Init;
import com.opengamma.engine.calcnode.msg.IsAlive;
import com.opengamma.engine.calcnode.msg.Ready;
import com.opengamma.engine.calcnode.msg.RemoteCalcNodeMessage;
import com.opengamma.engine.calcnode.msg.Result;
import com.opengamma.engine.calcnode.stats.FunctionInvocationStatisticsSender;
import com.opengamma.engine.function.CachingFunctionRepositoryCompiler;
import com.opengamma.engine.function.CompiledFunctionService;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.InMemoryFunctionRepository;
import com.opengamma.engine.test.TestCalculationNode;
import com.opengamma.id.VersionCorrection;
import com.opengamma.transport.CollectingFudgeMessageReceiver;
import com.opengamma.transport.DirectFudgeConnection;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.test.TestLifecycle;
import com.opengamma.util.test.Timeout;

/**
 * Tests RemoteNodeClient
 */
@Test(groups = TestGroup.UNIT)
public class RemoteNodeClientTest {

  private static final FudgeContext s_fudgeContext = OpenGammaFudgeContext.getInstance();
  private static final long TIMEOUT = Timeout.standardTimeoutMillis();

  protected static CalculationJob createTestCalculationJob() {
    return new CalculationJob(JobDispatcherTest.createTestJobSpec(), 0L, VersionCorrection.LATEST, null, JobDispatcherTest.createTestJobItems(), CacheSelectHint.allShared());
  }

  public void simpleInvocation() {
    TestLifecycle.begin();
    try {
      final IdentifierMap identifierMap = new InMemoryIdentifierMap();
      final DirectFudgeConnection conduit = new DirectFudgeConnection(s_fudgeContext);
      final CollectingFudgeMessageReceiver messages = new CollectingFudgeMessageReceiver();
      conduit.getEnd2().setFudgeMessageReceiver(messages);
      final CompiledFunctionService cfs = new CompiledFunctionService(new InMemoryFunctionRepository(), new CachingFunctionRepositoryCompiler(), new FunctionCompilationContext());
      TestLifecycle.register(cfs);
      final RemoteNodeClient client = new RemoteNodeClient(conduit.getEnd1(), cfs, new InMemoryIdentifierMap(), new FunctionInvocationStatisticsSender());
      final TestCalculationNode node = new TestCalculationNode();
      TestLifecycle.register(node);
      assertEquals(0, messages.getMessages().size());
      client.addNode(node);
      assertEquals(0, messages.getMessages().size());
      client.start();
      TestLifecycle.register(client);
      assertEquals(1, messages.getMessages().size());
      final FudgeMsgEnvelope readyMsgEnvelope = messages.getMessages().get(0);
      messages.clear();
      final FudgeDeserializer dcontext = new FudgeDeserializer(s_fudgeContext);
      final FudgeSerializer scontext = new FudgeSerializer(s_fudgeContext);
      final RemoteCalcNodeMessage readyMessage = dcontext.fudgeMsgToObject(RemoteCalcNodeMessage.class, readyMsgEnvelope.getMessage());
      assertTrue(readyMessage instanceof Ready);
      final Ready ready = (Ready) readyMessage;
      assertEquals(1, ready.getCapacity());
      conduit.getEnd2().getFudgeMessageSender().send(FudgeSerializer.addClassHeader(scontext.objectToFudgeMsg(new Init(0)), Init.class, RemoteCalcNodeMessage.class));
      final CalculationJob job = createTestCalculationJob();
      AbstractIdentifierMap.convertIdentifiers(identifierMap, job);
      conduit.getEnd2().getFudgeMessageSender().send(FudgeSerializer.addClassHeader(scontext.objectToFudgeMsg(new Execute(job)), Execute.class, RemoteCalcNodeMessage.class));
      final FudgeMsgEnvelope resultMsgEnvelope = messages.waitForMessage(TIMEOUT);
      assertNotNull(resultMsgEnvelope);
      final RemoteCalcNodeMessage resultMessage = dcontext.fudgeMsgToObject(RemoteCalcNodeMessage.class, resultMsgEnvelope.getMessage());
      assertTrue(resultMessage instanceof Result);
      final Result result = (Result) resultMessage;
      assertEquals(job.getSpecification(), result.getResult().getSpecification());
    } finally {
      TestLifecycle.end();
    }
  }

  public void isAlive() {
    TestLifecycle.begin();
    try {
      final IdentifierMap identifierMap = new InMemoryIdentifierMap();
      final DirectFudgeConnection conduit = new DirectFudgeConnection(s_fudgeContext);
      final CollectingFudgeMessageReceiver messages = new CollectingFudgeMessageReceiver();
      conduit.getEnd2().setFudgeMessageReceiver(messages);
      final CompiledFunctionService cfs = new CompiledFunctionService(new InMemoryFunctionRepository(), new CachingFunctionRepositoryCompiler(), new FunctionCompilationContext());
      TestLifecycle.register(cfs);
      final RemoteNodeClient client = new RemoteNodeClient(conduit.getEnd1(), cfs, new InMemoryIdentifierMap(), new FunctionInvocationStatisticsSender());
      client.start();
      TestLifecycle.register(client);
      assertEquals(1, messages.getMessages().size());
      final FudgeMsgEnvelope readyMsgEnvelope = messages.getMessages().get(0);
      messages.clear();
      final FudgeDeserializer dcontext = new FudgeDeserializer(s_fudgeContext);
      final FudgeSerializer scontext = new FudgeSerializer(s_fudgeContext);
      final RemoteCalcNodeMessage readyMessage = dcontext.fudgeMsgToObject(RemoteCalcNodeMessage.class, readyMsgEnvelope.getMessage());
      assertTrue(readyMessage instanceof Ready);
      conduit.getEnd2().getFudgeMessageSender().send(FudgeSerializer.addClassHeader(scontext.objectToFudgeMsg(new Init(0)), Init.class, RemoteCalcNodeMessage.class));
      final CalculationJob job1 = createTestCalculationJob();
      final CalculationJob job2 = createTestCalculationJob();
      AbstractIdentifierMap.convertIdentifiers(identifierMap, job1);
      conduit.getEnd2().getFudgeMessageSender().send(FudgeSerializer.addClassHeader(scontext.objectToFudgeMsg(new Execute(job1)), Execute.class, RemoteCalcNodeMessage.class));
      conduit
          .getEnd2()
          .getFudgeMessageSender()
          .send(
              FudgeSerializer.addClassHeader(scontext.objectToFudgeMsg(new IsAlive(Arrays.asList(job1.getSpecification(), job2.getSpecification()))), IsAlive.class,
                  RemoteCalcNodeMessage.class));
      final FudgeMsgEnvelope resultMsgEnvelope = messages.waitForMessage(TIMEOUT);
      assertNotNull(resultMsgEnvelope);
      final RemoteCalcNodeMessage failureMessage = dcontext.fudgeMsgToObject(RemoteCalcNodeMessage.class, resultMsgEnvelope.getMessage());
      assertTrue(failureMessage instanceof Failure);
      final Failure failure = (Failure) failureMessage;
      assertEquals(job2.getSpecification(), failure.getJob());
      // No more messages - job1 is alive
      assertNull(messages.waitForMessage(TIMEOUT));
    } finally {
      TestLifecycle.end();
    }
  }

  public void errorInvocation() {
    TestLifecycle.begin();
    try {
      final IdentifierMap identifierMap = new InMemoryIdentifierMap();
      final DirectFudgeConnection conduit = new DirectFudgeConnection(s_fudgeContext);
      final CollectingFudgeMessageReceiver messages = new CollectingFudgeMessageReceiver();
      conduit.getEnd2().setFudgeMessageReceiver(messages);
      final CompiledFunctionService cfs = new CompiledFunctionService(new InMemoryFunctionRepository(), new CachingFunctionRepositoryCompiler(), new FunctionCompilationContext());
      TestLifecycle.register(cfs);
      final RemoteNodeClient client = new RemoteNodeClient(conduit.getEnd1(), cfs, new InMemoryIdentifierMap(), new FunctionInvocationStatisticsSender());
      final TestCalculationNode failingNode = new TestCalculationNode() {

        @Override
        public CalculationJobResult executeJob(CalculationJob job) {
          throw new OpenGammaRuntimeException("Remote node not working");
        }

      };
      TestLifecycle.register(failingNode);
      assertEquals(0, messages.getMessages().size());
      client.addNode(failingNode);
      assertEquals(0, messages.getMessages().size());
      client.start();
      TestLifecycle.register(client);
      assertEquals(1, messages.getMessages().size());
      final FudgeMsgEnvelope readyMsgEnvelope = messages.getMessages().get(0);
      messages.clear();
      final FudgeDeserializer dcontext = new FudgeDeserializer(s_fudgeContext);
      final FudgeSerializer scontext = new FudgeSerializer(s_fudgeContext);
      final RemoteCalcNodeMessage readyMessage = dcontext.fudgeMsgToObject(RemoteCalcNodeMessage.class, readyMsgEnvelope.getMessage());
      assertTrue(readyMessage instanceof Ready);
      final Ready ready = (Ready) readyMessage;
      assertEquals(1, ready.getCapacity());
      conduit.getEnd2().getFudgeMessageSender().send(FudgeSerializer.addClassHeader(scontext.objectToFudgeMsg(new Init(0)), Init.class, RemoteCalcNodeMessage.class));
      final CalculationJob job = createTestCalculationJob();
      AbstractIdentifierMap.convertIdentifiers(identifierMap, job);
      conduit.getEnd2().getFudgeMessageSender().send(FudgeSerializer.addClassHeader(scontext.objectToFudgeMsg(new Execute(job)), Execute.class, RemoteCalcNodeMessage.class));
      final FudgeMsgEnvelope resultMsgEnvelope = messages.waitForMessage(TIMEOUT);
      assertNotNull(resultMsgEnvelope);
      final RemoteCalcNodeMessage resultMessage = dcontext.fudgeMsgToObject(RemoteCalcNodeMessage.class, resultMsgEnvelope.getMessage());
      assertTrue(resultMessage instanceof Failure);
      final Failure failure = (Failure) resultMessage;
      assertEquals(job.getSpecification(), failure.getJob());
    } finally {
      TestLifecycle.end();
    }
  }

}
