import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintStream;
import java.lang.Exception;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class RunExamples {
    /*
     * Method which runs a static method in the example class. Assumes that
     * method accepts a single parameter of type PrintStream, and saves
     * contents that are written to this stream under the method's name in a
     * JSONObject (i.e. a HashMap)
     */
    public static void runMethod(Method method, PrintStream out, ByteArrayOutputStream byteStream, JSONObject outputStreams) throws Exception {
        System.out.println("Running method " + method.getName());

        // null because this is a static method, so it's not being invoked on any particular instance
        method.invoke(null, out);

        // save output and reset streams for next time
        out.flush();
        outputStreams.put(method.getName(), byteStream.toString());
        byteStream.reset();
    }

    public static String[] classNames() {
        // TODO get list of all example classes automatically, by parsing jsondoc data or otherwise
        return new String[] {
            "com.opengamma.example.curveconstruction.CashExample",
            "com.opengamma.example.curveconstruction.MatrixExample",
            "com.opengamma.example.curveconstruction.FunctionExample"
        };
    }

    public static void runExamples() throws Exception {
        String[] classNames = classNames();
        String outputFilename = "../dexy--example-output.json";
        String fieldsFilename = "../dexy--example-fields.json";

        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(byteStream);
        JSONObject outputStreams = new JSONObject();
        JSONObject fieldInfo = new JSONObject();

        for (int k = 0; k < classNames.length; k++) {
            String className = classNames[k];
            Class thisClass = Class.forName(className);

            JSONObject classOutputStreams = new JSONObject();
            Method[] methods = thisClass.getMethods();
            for (int i = 0; i < methods.length; i++) {
                Method method = methods[i];
                Class[] params = method.getParameterTypes();
                if (params.length == 1 && params[0] == PrintStream.class) {
                    runMethod(method, out, byteStream, classOutputStreams);
                }
            }
            outputStreams.put(className, classOutputStreams);


            JSONObject classFieldInfo = new JSONObject();
            Field[] fields = thisClass.getDeclaredFields();
            for (int i = 0; i < fields.length; i++) {
                Field field = fields[i];

                try {
                    Object value = field.get(null);

                    // Handle any special data types that cause problems for the JSON conversion...
                    if (field.getType().equals(double[].class)) {
                        double[] values = (double[])value;
                        JSONArray array = new JSONArray();
                        for (int j = 0; j < values.length; j++) {
                            array.add(values[j]);
                        }
                        classFieldInfo.put(field.getName(), array);
                    } else {
                        // default case

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
                        classFieldInfo.put(field.getName(), value);
                    }
                } catch (java.lang.IllegalAccessException e) {
                    // Skip any fields that aren't accessible.
                    System.out.println("Not allowed to see value of " + field.getName() + " in " + className);
                }
            }
            fieldInfo.put(className, classFieldInfo);
        }

        File f;
        FileWriter file;

        f = new File(outputFilename);
        file = new FileWriter(f);
        outputStreams.writeJSONString(file);
        file.close();

        f = new File(fieldsFilename);
        file = new FileWriter(f);
        fieldInfo.writeJSONString(file);
        file.close();
    }

    public static void main(String[] args) throws Exception {
        runExamples();
    }
}
