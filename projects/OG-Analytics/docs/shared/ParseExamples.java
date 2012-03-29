import java.io.File;
import java.io.FileWriter;
import org.apache.bcel.Repository;
import org.apache.bcel.classfile.*;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;

class ParseExamples {
    public static void main(String[] args) throws java.lang.Exception {
        String[] classNames = RunExamples.classNames();

        // e.g. com.opengamma.Class used in com.opengamma.example.ExampleClass:MethodName
        JSONObject classesUsed = new JSONObject();

        for (int k = 0; k < classNames.length; k++) {
            String className = classNames[k];
            JavaClass clazz = Repository.lookupClass(className);
            ConstantPool pool = clazz.getConstantPool();

            // Check each method in this class to see which OpenGamma classes it uses
            Method[] methods = clazz.getMethods(); for (int i = 0; i < methods.length; i++) {
                Method method = methods[i];
                Code code = method.getCode();

                // code.toString calls Utilities.codeToString which does lots
                // of useful parsing but then stores everything in a String
                // instead of a more accessible format, so we need to parse the string
                String[] codeLines = code.toString().split("\n");
                for (int m = 0; m < codeLines.length; m++) {
                    String line = codeLines[m];
                    int x = line.indexOf("invokespecial");
                    int y = line.indexOf(".<init>");
                    if (x > 0 && y > 0) {
                        String usedClassName = line.substring(x+13, y).trim();
                        if (usedClassName.startsWith("com.opengamma")) {
                            // This is one we are interested in!
                            JSONArray methodsUsedIn;

                            if (classesUsed.containsKey(usedClassName)) {
                                // Get existing array...
                                methodsUsedIn = (JSONArray)classesUsed.get(usedClassName);
                            } else {
                                // Init new array...
                                methodsUsedIn = new JSONArray();
                            }
                            String methodKey = className + ":" + method.getName();
                            methodsUsedIn.add(methodKey);
                            classesUsed.put(usedClassName, methodsUsedIn);
                        }
                    }
                }

                File f;
                FileWriter file;

                f = new File("../dexy--depends.json");
                file = new FileWriter(f);
                classesUsed.writeJSONString(file);
                file.close();
            }
        }
    }
}
