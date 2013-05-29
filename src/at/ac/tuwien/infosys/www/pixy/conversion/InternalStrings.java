package at.ac.tuwien.infosys.www.pixy.conversion;

/**
 * Contains a number of internal names, prefixes, and suffixes.
 *
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class InternalStrings {
    /** suffix for g-shadows */
    public static final String gShadowSuffix = "_gs";

    /** suffix for f-shadows */
    public static final String fShadowSuffix = "_fs";

    /** prefix for function return variables */
    public static final String returnPrefix = "ret_";

    /** name of the "main" function; don't use uppercase characters */
    public static final String mainFunctionName = "_main";

    /** internal name for member variables */
    public static final String memberName = "_member";

    /** suffix for methods */
    public static final String methodSuffix = "<m>";

    /**
     * The name of Pixy's suppression function.
     *
     * Use it to get rid of false positives during the analysis.
     *
     * For reverting your application to production mode, simply run a search-replace over it and remove (or comment
     * out) all lines that contain a call to this function.
     */
    public static final String suppression = "pixy_sanit";

    // CONFIG.TXT ****************************
    public static final String INCLUDE_PATH = "includePath";
    public static final String PHP_BIN = "phpBin";
    public static final String FSA_HOME = "fsaHome";
}