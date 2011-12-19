import org.apache.bcel.Repository;
import org.apache.bcel.classfile.*;

class ParseExamples {
    static String CLASS_NAME = "examples.MatrixExample";

    public static void main(String[] args) throws java.lang.Exception {
        JavaClass clazz = Repository.lookupClass(CLASS_NAME);
        System.out.println(clazz);

        Method[] methods = clazz.getMethods();
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            System.out.println(method.getName());
            System.out.println(method.getCode());
            ConstantPool pool = method.getConstantPool();
            System.out.println("\nConstant Pool\n");
            System.out.println(pool);
            System.out.println("\nConstants\n");
            for (int j = 0; j < pool.getLength(); j++) {
                Constant constant = pool.getConstant(j);
                try{
                    ConstantClass constantObject = (ConstantClass)constant;
                    Object constantValue = constantObject.getConstantValue(pool);
                    System.out.format("%d) %s%n", j, constantValue);
                } catch (java.lang.NullPointerException e) {
                } catch (java.lang.ClassCastException e) {
                }
            }
            System.out.println("\n\n");
        }
    }
}
