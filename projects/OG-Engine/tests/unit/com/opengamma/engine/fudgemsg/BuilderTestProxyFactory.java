package com.opengamma.engine.fudgemsg;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import org.apache.commons.io.IOUtils;
import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeDataInputStreamReader;
import org.fudgemsg.FudgeDataOutputStreamWriter;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.FudgeMsgReader;
import org.fudgemsg.FudgeMsgWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.util.fudge.OpenGammaFudgeContext;

/**
 * This class allows you to test serializers from other platforms by using any {@link AbstractBuilderTestCase}s you have.
 */
public class BuilderTestProxyFactory {

  public interface BuilderTestProxy {
    public FudgeFieldContainer proxy(final Class<?> clazz, FudgeFieldContainer orig);
  }

  public BuilderTestProxy getProxy() {
    String execPath = System.getProperty("com.opengamma.engine.fudgemsg.BuilderTestProxyFactory.ExecBuilderTestProxy.execPath");
    if (execPath!=null)
    {
      return new ExecBuilderTestProxy(execPath);
    }
    return new NullBuilderTestProxy();
  }

  private static class NullBuilderTestProxy implements BuilderTestProxy {
    @Override
    public FudgeFieldContainer proxy(final Class<?> clazz, FudgeFieldContainer orig) {
      return orig;
    }
  }

  private static class ExecBuilderTestProxy implements BuilderTestProxy {
    private static final Logger s_logger = LoggerFactory.getLogger (ExecBuilderTestProxy.class);
    
    final String _execPath;

    public ExecBuilderTestProxy(String execPath) {
      _execPath=execPath;
    }

    @Override
    public FudgeFieldContainer proxy(final Class<?> clazz, FudgeFieldContainer orig) {

      FudgeContext context = OpenGammaFudgeContext.getInstance();

      LinkedList<String> command = new LinkedList<String>();
      command.add(_execPath);
      command.add(clazz.getName());
      
      final ProcessBuilder processBuilder = new ProcessBuilder(command);
      
      
      try {
        final Process proc = processBuilder.start();
        try{
          OutputStream outputStream = proc.getOutputStream();
          try {
            FudgeDataOutputStreamWriter fudgeDataOutputStreamWriter = new FudgeDataOutputStreamWriter(context,                 outputStream);
            FudgeMsgWriter fudgeMsgWriter = new FudgeMsgWriter(fudgeDataOutputStreamWriter);
            fudgeMsgWriter.writeMessage(orig);
            fudgeMsgWriter.flush();
            
            InputStream inputStream = proc.getInputStream();
            try {
              
              FudgeDataInputStreamReader fudgeDataInputStreamReader = new FudgeDataInputStreamReader(context, inputStream);
              final FudgeMsgReader fudgeMsgReader = new FudgeMsgReader(fudgeDataInputStreamReader);
          
              ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(3);
              Future<FudgeFieldContainer> retMsgFuture = scheduledThreadPoolExecutor.submit(new Callable<FudgeFieldContainer>() {

                @Override
                public FudgeFieldContainer call() throws Exception {
                  return fudgeMsgReader.nextMessage();
                }
                
              });
              
              Future<List<String>> errFuture = scheduledThreadPoolExecutor.submit(new Callable<List<String>>() {

                @Override
                public List<String> call() throws Exception {
                  InputStream errorStream = proc.getErrorStream();
                  try
                  {
                    return IOUtils.readLines(errorStream);
                  }
                  finally
                  {
                    errorStream.close();
                  }
                }
              });
            
              for (String err : errFuture.get()) {
                s_logger.warn(err);
              }
              int ret = proc.waitFor();
              if (ret!=0)
              {
                throw new IOException("Exit code not expected: "+ret);
              }
              return retMsgFuture.get();
            } finally {
              inputStream.close();
            }
          } finally {
            outputStream.close();
          }
        }
        finally
        {
          proc.destroy();
        }
      } catch (Exception ex) {
        throw new AssertionError(ex);
      }
    }

  }

}
