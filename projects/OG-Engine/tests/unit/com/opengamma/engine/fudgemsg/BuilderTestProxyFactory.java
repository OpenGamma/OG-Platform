package com.opengamma.engine.fudgemsg;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeDataInputStreamReader;
import org.fudgemsg.FudgeDataOutputStreamWriter;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.FudgeMsgReader;
import org.fudgemsg.FudgeMsgWriter;

import com.opengamma.util.fudge.OpenGammaFudgeContext;

/**
 * This class allows you to test serializers from other platforms by using any {@link AbstractBuilderTestCase}s you have.
 */
public class BuilderTestProxyFactory {

  public interface BuilderTestProxy {
    public FudgeFieldContainer proxy(FudgeFieldContainer orig);
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
    public FudgeFieldContainer proxy(FudgeFieldContainer orig) {
      return orig;
    }
  }

  private static class ExecBuilderTestProxy implements BuilderTestProxy {
    private final ProcessBuilder processBuilder;

    public ExecBuilderTestProxy(String execPath) {
      this.processBuilder = new ProcessBuilder(execPath);
      processBuilder.directory(new File(execPath).getParentFile());
    }

    @Override
    public FudgeFieldContainer proxy(FudgeFieldContainer orig) {

      FudgeContext context = OpenGammaFudgeContext.getInstance();

      try {
        Process proc = processBuilder.start();
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
              FudgeMsgReader fudgeMsgReader = new FudgeMsgReader(fudgeDataInputStreamReader);
          
                           
              FudgeFieldContainer retMsg = fudgeMsgReader.nextMessage();
              
            
              String string = orig.toString();
              String string2 = retMsg.toString();
              if (!string.equals(string2)){
                
              }
            
              int ret = proc.waitFor();
              if (ret!=0)
              {
                throw new IOException("Exit code not expected: "+ret);
              }
              
              return retMsg;
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
