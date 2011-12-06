import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.File;
import java.io.FileWriter;
import org.json.simple.JSONObject;
import java.util.Arrays;
import java.lang.reflect.Method;
import java.lang.Exception;
import java.lang.Math;

public class OpenGammaExample {
    public static void runMethod(Method method, PrintStream out, ByteArrayOutputStream byteStream, JSONObject outputStreams) throws Exception {
        System.out.println("Running method " + method.getName());
        method.invoke(null, out);
        out.flush();
        outputStreams.put(method.getName(), byteStream.toString());
        byteStream.reset();
    }

    public static void main(String[] args) throws Exception {
        String className;
        String outputFilename;

        if (args.length == 0) {
            className = "OpenGammaExample";
            outputFilename = "output.json";
        } else {
            className = args[0];
            outputFilename = args[1];
        }

        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(byteStream);
        JSONObject outputStreams = new JSONObject();

        Class thisClass = Class.forName(className);
        System.out.println("this class is" + thisClass.getName());
        Method[] methods = thisClass.getMethods();
        System.out.println("found methods" + methods.toString());

        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            Class[] params = method.getParameterTypes();
            if (params.length == 1 && params[0] == PrintStream.class) {
                runMethod(method, out, byteStream, outputStreams);
            }
        }

        File fl = new File(outputFilename);
        FileWriter file = new FileWriter(fl);
        outputStreams.writeJSONString(file);
        file.close();
    }
}
