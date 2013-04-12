package at.ac.tuwien.infosys.www.pixy.conversion;

import java.io.File;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import at.ac.tuwien.infosys.www.phpparser.ParseNode;
import at.ac.tuwien.infosys.www.phpparser.PhpSymbols;
import at.ac.tuwien.infosys.www.pixy.Utils;
import at.ac.tuwien.infosys.www.pixy.analysis.literal.LiteralAnalysis;
import at.ac.tuwien.infosys.www.pixy.conversion.nodes.CfgNodeInclude;

public class ParseNodeHeuristics {

    private static LiteralAnalysis literalAnalysis;
    private static CfgNodeInclude includeNode;

    // tries to find the name of an included file even if literal analysis
    // computed "top" by matching the known parts of the file name against
    // the files in the subdirectories; returns:
    // - null if there is more than one possibility
    // - an empty list if there is no possibility
    // - a one-element list if there is exactly one possibility
    public static List<String> getPossibleIncludeTargets(
        CfgNodeInclude includeNode, LiteralAnalysis literalAnalysis,
        Map<CfgNodeInclude, String> include2String,
        String workingDirectory) {

        ParseNodeHeuristics.literalAnalysis = literalAnalysis;
        ParseNodeHeuristics.includeNode = includeNode;

        ParseNode parseNode = includeNode.getParseNode();

        // just a few checks to make sure we are dealing with
        // an expected node type
        if (parseNode.getSymbol() != PhpSymbols.internal_functions_in_yacc) {
            throw new RuntimeException("SNH");
        }
        ParseNode firstChild = parseNode.getChild(0);
        switch (firstChild.getSymbol()) {
            case PhpSymbols.T_INCLUDE:
            case PhpSymbols.T_INCLUDE_ONCE:
            case PhpSymbols.T_REQUIRE:
            case PhpSymbols.T_REQUIRE_ONCE:
                // ok!
                break;
            default:
                throw new RuntimeException("SNH");
        }
        ParseNode secondChild = parseNode.getChild(1);
        if (secondChild.getSymbol() != PhpSymbols.expr) {
            throw new RuntimeException("SNH");
        }

        // results in a list of strings and null references;
        // null references represent ".*"
        LinkedList<String> stringList = expr(secondChild);

        // transform the list into a pattern...

        // flag indicating whether there is something literal
        boolean somethingLiteral = false;

        // flag indicating whether we have put a ".*" in front of the
        // current step; important for perfomance: if you have several
        // dot-stars in a row, matching is VERY slow
        boolean precedingDotStar = false;

        StringBuilder pattern = new StringBuilder();
        for (String s : stringList) {

            if (s == null) {
                if (precedingDotStar) {
                    // do nothing
                } else {
                    pattern.append(".*");
                    precedingDotStar = true;
                }
            } else {
                // quote special characters (most notably: the dot .)
                pattern.append(Pattern.quote(s));
                somethingLiteral = true;
                precedingDotStar = false;
            }
        }
        pattern.append("$");

        normalizePath(pattern);

        // if there is nothing literal in there, we don't even have to try
        if (!somethingLiteral) {
            return null;
        }

        Pattern patternObj = Pattern.compile(pattern.toString());

        // here is what we do now:
        // - collect all files below the current working directory
        // - try to find a match
        // - if we already have ambiguity (more than one match): return null
        // - if we have exactly one match: be happy and return it
        // - the only remaining possibility is: no match yet,
        //   so try the same with all files below the directory of the
        //   current script
        //   (but don't retry those files that you have tried before,
        //   since this would be a waste of time)

        List<File> candidates1 = Utils.fileListFromDir(workingDirectory);
        System.out.println("inclusion matching against " + candidates1.size() + " candidates");
        List<String> winners = matchCandidates(patternObj, candidates1);
        if (winners == null) {
            // we already have ambiguity
            return null;
        }
        if (winners.size() == 1) {
            // exact match
            return winners;
        }

        // no match, so try relative to script directory
        // (but don't retry previous candidates)
        List<File> candidates2 = Utils.fileListFromFile(parseNode.getFileName());
        candidates2.removeAll(candidates1);
        winners = matchCandidates(patternObj, candidates2);
        if (winners == null) {
            return null;
        }

        // if we haven't found anything...
        if (winners.isEmpty()) {
            include2String.put(includeNode, patternObj.pattern());
        }

        return winners;
    }

    // matches the pattern against the candidate files; returns
    // - null if more than one candidate matches the pattern
    // - an empty list of no candidates match
    // - a one-element list if exactly one candidate matches
    private static List<String> matchCandidates(Pattern patternObj, Collection<File> candidates) {
        List<String> winners = new LinkedList<String>();
        for (File candidate : candidates) {
            // no need to go on if we already have ambiguity
            if (winners.size() > 1) {
                return null;
            }

            String candidatePath = candidate.getPath();

            // EFF: the matching step is rather slow
            Matcher matcher = patternObj.matcher(candidatePath);
            if (matcher.find()) {
                winners.add(candidatePath);
            }
        }
        return winners;
    }

    private static void normalizePath(StringBuilder pattern) {
        // here, you can do various things to increase the pattern match rate
        // (e.g., remove unnecessary "./")
    }

//  ********************************************************************************
//  MINI-PARSER ********************************************************************
//  ********************************************************************************

    private static LinkedList<String> expr(ParseNode node) {

        LinkedList<String> myList = null;

        ParseNode firstChild = node.getChild(0);
        switch (firstChild.getSymbol()) {

            // -> r_cvar
            case PhpSymbols.r_cvar: {
                myList = r_cvar(firstChild);
                break;
            }

            // -> expr_without_variable
            case PhpSymbols.expr_without_variable: {
                myList = expr_without_variable(firstChild);
                break;
            }
        }

        return myList;
    }

    private static LinkedList<String> expr_without_variable(ParseNode node) {

        LinkedList<String> myList = null;

        ParseNode firstChild = node.getChild(0);
        switch (firstChild.getSymbol()) {

            // -> expr ...
            case PhpSymbols.expr: {
                switch (node.getChild(1).getSymbol()) {

                    // -> expr . expr
                    case PhpSymbols.T_POINT: {
                        LinkedList<String> list0 = expr(node.getChild(0));
                        List<String> list2 = expr(node.getChild(2));
                        myList = list0;
                        myList.addAll(list2);
                        break;
                    }

                    default: {
                        myList = new LinkedList<String>();
                        myList.add(null);
                    }
                }
                break;
            }

            // -> ( expr )
            case PhpSymbols.T_OPEN_BRACES: {
                myList = expr(node.getChild(1));
                break;
            }

            // -> scalar
            case PhpSymbols.scalar: {
                myList = scalar(firstChild);
                break;
            }

            default: {
                myList = new LinkedList<String>();
                myList.add(null);
            }
        }

        return myList;
    }

    private static LinkedList<String> scalar(ParseNode node) {
        LinkedList<String> myList = null;

        ParseNode firstChild = node.getChild(0);
        switch (firstChild.getSymbol()) {

            // -> common_scalar
            case PhpSymbols.common_scalar: {
                myList = common_scalar(firstChild);
                break;
            }

            // -> " encaps_list "
            case PhpSymbols.T_DOUBLE_QUOTE: {
                myList = encaps_list(node.getChild(1));
                break;
            }

            default: {
                myList = new LinkedList<String>();
                myList.add(null);
            }
        }

        return myList;
    }

    private static LinkedList<String> common_scalar(ParseNode node) {
        LinkedList<String> myList = null;

        ParseNode firstChild = node.getChild(0);
        switch (firstChild.getSymbol()) {

            case PhpSymbols.T_CONSTANT_ENCAPSED_STRING: {
                myList = new LinkedList<String>();

                // use Literal() to peel off quotes,
                myList.add(new Literal(firstChild.getLexeme()).toString());
                break;
            }

            default: {
                myList = new LinkedList<String>();
                myList.add(null);
            }
        }

        return myList;
    }

    private static LinkedList<String> r_cvar(ParseNode node) {
        return cvar(node.getChild(0));
    }

    private static LinkedList<String> cvar(ParseNode node) {
        LinkedList<String> myList = null;

        if (node.getNumChildren() == 1) {
            // -> cvar_without_objects
            myList = cvar_without_objects(node.getChild(0));
        } else {
            // -> cvar_without_objects T_OBJECT_OPERATOR ref_list
            myList = new LinkedList<String>();
            myList.add(null);
        }

        return myList;
    }

    private static LinkedList<String> cvar_without_objects(ParseNode node) {
        LinkedList<String> myList = null;

        ParseNode firstChild = node.getChild(0);
        switch (firstChild.getSymbol()) {

            // -> reference_variable
            case PhpSymbols.reference_variable: {
                myList = reference_variable(firstChild);
                break;
            }

            // -> simple_indirect_reference reference_variable
            case PhpSymbols.simple_indirect_reference: {
                myList = new LinkedList<String>();
                myList.add(null);
                break;
            }

            default: {
                throw new RuntimeException("SNH");
            }
        }

        return myList;
    }

    private static LinkedList<String> reference_variable(ParseNode node) {
        LinkedList<String> myList = null;

        ParseNode firstChild = node.getChild(0);
        switch (firstChild.getSymbol()) {

            // -> reference_variable ...
            case PhpSymbols.reference_variable: {
                myList = new LinkedList<String>();
                myList.add(null);
                break;
            }

            // -> compound_variable
            case PhpSymbols.compound_variable: {
                myList = compound_variable(firstChild);
                break;
            }

            default: {
                throw new RuntimeException("SNH");
            }
        }

        return myList;
    }

    private static LinkedList<String> compound_variable(ParseNode node) {
        LinkedList<String> myList = null;

        ParseNode firstChild = node.getChild(0);
        switch (firstChild.getSymbol()) {

            // -> T_VARIABLE
            case PhpSymbols.T_VARIABLE: {
                myList = new LinkedList<String>();

                // try to resolve with literal analysis!
                Literal lit = literalAnalysis.getLiteral(firstChild.getLexeme(), includeNode);
                if (lit == Literal.TOP) {
                    myList.add(null);
                } else {
                    myList.add(lit.toString());
                }

                break;
            }

            // -> $ { expr }
            case PhpSymbols.T_DOLLAR: {
                myList = new LinkedList<String>();
                myList.add(null);
                break;
            }

            default: {
                throw new RuntimeException("SNH");
            }
        }

        return myList;
    }

    private static LinkedList<String> encaps_list(ParseNode node) {
        LinkedList<String> myList = null;

        ParseNode firstChild = node.getChild(0);
        if (firstChild.getSymbol() == PhpSymbols.T_EPSILON) {
            // -> empty

            myList = new LinkedList<String>();
            return myList;
        }

        ParseNode secondChild = node.getChild(1);
        switch (secondChild.getSymbol()) {

            // -> encaps_list encaps_var
            case PhpSymbols.encaps_var: {
                LinkedList<String> list0 = encaps_list(firstChild);
                LinkedList<String> list1 = encaps_var(secondChild);
                myList = list0;
                myList.addAll(list1);
                break;
            }

            // -> encaps_list T_STRING
            case PhpSymbols.T_STRING: {
                myList = encapsListHelper(node);
                break;
            }

            // -> encaps_list T_NUM_STRING
            case PhpSymbols.T_NUM_STRING: {
                myList = encapsListHelper(node);
                break;
            }

            // -> encaps_list T_ENCAPSED_AND_WHITESPACE
            case PhpSymbols.T_ENCAPSED_AND_WHITESPACE: {
                myList = encapsListHelper(node);
                break;
            }

            // -> encaps_list T_CHARACTER
            // escaped character?
            case PhpSymbols.T_CHARACTER: {
                myList = encapsListHelper(node);
                break;
            }

            // -> encaps_list [
            case PhpSymbols.T_OPEN_RECT_BRACES: {
                myList = encapsListHelper(node);
                break;
            }

            // -> encaps_list ]
            case PhpSymbols.T_CLOSE_RECT_BRACES: {
                myList = encapsListHelper(node);
                break;
            }

            // -> encaps_list {
            case PhpSymbols.T_OPEN_CURLY_BRACES: {
                myList = encapsListHelper(node);
                break;
            }

            // -> encaps_list }
            case PhpSymbols.T_CLOSE_CURLY_BRACES: {
                myList = encapsListHelper(node);
                break;
            }

            // -> encaps_list T_OBJECT_OPERATOR
            case PhpSymbols.T_OBJECT_OPERATOR: {
                myList = new LinkedList<String>();
                myList.add(null);
                break;
            }

            default: {
                throw new RuntimeException("SNH");
            }
        }

        return myList;
    }

    // encaps_list -> encaps_list, <some token>
    private static LinkedList<String> encapsListHelper(ParseNode node) {
        LinkedList<String> myList = encaps_list(node.getChild(0));
        myList.add(node.getChild(1).getLexeme());
        return myList;
    }

    private static LinkedList<String> encaps_var(ParseNode node) {
        LinkedList<String> myList = null;

        if (node.getNumChildren() == 1) {
            // -> T_VARIABLE

            myList = new LinkedList<String>();

            // try to resolve with literal analysis!
            Literal lit = literalAnalysis.getLiteral(node.getChild(0).getLexeme(), includeNode);
            if (lit == Literal.TOP) {
                myList.add(null);
            } else {
                myList.add(lit.toString());
            }
            return myList;
        }

        myList = new LinkedList<String>();
        myList.add(null);

        return myList;
    }
}