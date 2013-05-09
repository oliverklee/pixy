package at.ac.tuwien.infosys.www.pixy.analysis.type;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class Type {

//  static stuff *******************************************************************

    private static Map<String, Type> classTypes;

    public static void initTypes(Collection<String> classNames) {
        classTypes = new HashMap<String, Type>();
        for (String className : classNames) {
            classTypes.put(className, new Type(className));
        }
    }

    public static Type getTypeForClass(String className) {
        Type type = classTypes.get(className);
        if (type == null) {
            throw new RuntimeException("SNH");
        }
        return type;
    }

//  ********************************************************************************

    private String className;

    private Type(String className) {
        this.className = className;
    }

    public String getClassName() {
        return this.className;
    }

    public String toString() {
        return this.className;
    }
}