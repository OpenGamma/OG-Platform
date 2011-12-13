// @export "imports"
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintStream;
import java.lang.Exception;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

// @export "classDefinition"
public class OpenGammaExample {

    // @export "runMethod"
    public static void runMethod(Method method, PrintStream out, ByteArrayOutputStream byteStream, JSONObject outputStreams) throws Exception {
        System.out.println("Running method " + method.getName());
        method.invoke(null, out);
        out.flush();
        outputStreams.put(method.getName(), byteStream.toString());
        byteStream.reset();
    }

    // @export "main"
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

        // @export "outputStreams"
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(byteStream);
        JSONObject outputStreams = new JSONObject();

        // @export "findAndRunMethods"
        Class thisClass = Class.forName(className);
        Method[] methods = thisClass.getMethods();

        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            Class[] params = method.getParameterTypes();
            if (params.length == 1 && params[0] == PrintStream.class) {
                runMethod(method, out, byteStream, outputStreams);
            }
        }

        // @export "saveOutput"
        File fl = new File(outputFilename);
        FileWriter file = new FileWriter(fl);
        outputStreams.writeJSONString(file);
        file.close();

        // @export "saveFields"
        if (args.length == 3) {
            String fieldOutputFilename = args[2];
            File f2 = new File(fieldOutputFilename);
            file = new FileWriter(f2);
            JSONObject fieldInfo = new JSONObject();

            Field[] fields = thisClass.getDeclaredFields();
            for (int i = 0; i < fields.length; i++) {
                Field field = fields[i];

                // Handle any special data types that cause problems for the JSON conversion...
                if (field.getType().equals(double[].class)) {
                    double[] values = (double[])field.get(null);
                    JSONArray array = new JSONArray();
                    for (int j = 0; j < values.length; j++) {
                        array.add(values[j]);
                    }
                    fieldInfo.put(field.getName(), array);
                } else {
                    // default case
                    Object value = field.get(null);

                    // check class, if not a directly supported type, then explicitly call toString() or else we get invalid JSON
                    Class fieldClass = field.getType();

                    // TODO Finish this, need to recognize all classes that should not be converted to strings.
                    // The values can be any of these types: Boolean, JSONArray, JSONObject, Number, String, or the JSONObject.NULL object.
                    if (fieldClass.equals(double.class)) {
                        // ok
                    } else if (fieldClass.equals(int.class)) {
                        // ok
                    } else if (fieldClass.equals(long.class)) {
                        // ok
                    } else if (fieldClass.equals(boolean.class)) {
                        // ok
                    } else if (java.lang.Number.class.isAssignableFrom(fieldClass)) {
                        // ok
                    } else {
                        value = value.toString();
                    }
                    fieldInfo.put(field.getName(), value);
                }
            }
            fieldInfo.writeJSONString(file);
            file.close();
        }
        // @end
    }
}
