package org.lamsfoundation.lams.contentrepository;
/**
 * LAMS property types.
 */
public final class PropertyType {
	/** String property type */
    public static final int STRING = 1;
	/** Long property type */
    public static final int LONG = 2;
	/** Double property type */
    public static final int DOUBLE = 3;
	/** Date property type */
    public static final int DATE = 4;
	/** Boolean property type */
    public static final int BOOLEAN = 5;

    /**
     * Undefined type.
     */
    public static final int UNDEFINED = 0;

    /**
     * The names of the supported property types,
     * as used in serialization.
     */
    public static final String TYPENAME_STRING = "String";
    public static final String TYPENAME_LONG = "Long";
    public static final String TYPENAME_DOUBLE = "Double";
    public static final String TYPENAME_DATE = "Date";
    public static final String TYPENAME_BOOLEAN = "Boolean";

    /**
     * Returns the name of the specified type.
     * @param type the property type
     * @return the name of the specified type
     * @throws RepostoryException if the type is not a valid property type.
     */
    public static String nameFromValue(int type) throws RepositoryCheckedException {
        switch (type) {
            case STRING:
	        return TYPENAME_STRING;
            case BOOLEAN:
	        return TYPENAME_BOOLEAN;
            case LONG:
	        return TYPENAME_LONG;
            case DOUBLE:
	        return TYPENAME_DOUBLE;
            case DATE:
	        return TYPENAME_DATE;
	    default:
	        throw new RepositoryCheckedException("unknown type: " + type);
        }
    }

    /**
     * Returns the numeric constant value of the type with the specified name.
     * @param name the name of the property type
     * @return the numeric constant value
     * @throws RepostoryException if name
     * is not a valid property type name.
     */
    public static int valueFromName(String name) throws RepositoryCheckedException {
        if (name.equals(TYPENAME_STRING)) {
            return STRING;
        } else if (name.equals(TYPENAME_BOOLEAN)) {
            return BOOLEAN;
        } else if (name.equals(TYPENAME_LONG)) {
            return LONG;
        } else if (name.equals(TYPENAME_DOUBLE)) {
            return DOUBLE;
        } else if (name.equals(TYPENAME_DATE)) {
            return DATE;
        } else {
            throw new RepositoryCheckedException("unknown type: " + name);
        }
    }

    /** private constructor to prevent instantiation */
    private PropertyType() {
    }
}

