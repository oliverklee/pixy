package at.ac.tuwien.infosys.www.pixy.analysis.literal.tf;

import java.io.*;
import java.util.*;

import at.ac.tuwien.infosys.www.pixy.MyOptions;
import at.ac.tuwien.infosys.www.pixy.analysis.LatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.TransferFunction;
import at.ac.tuwien.infosys.www.pixy.analysis.literal.LiteralLatticeElement;
import at.ac.tuwien.infosys.www.pixy.conversion.Literal;
import at.ac.tuwien.infosys.www.pixy.conversion.TacActualParam;
import at.ac.tuwien.infosys.www.pixy.conversion.TacPlace;
import at.ac.tuwien.infosys.www.pixy.conversion.nodes.CfgNodeCallBuiltin;

public class LiteralTfCallBuiltin
extends TransferFunction {

    private CfgNodeCallBuiltin cfgNode;

// *********************************************************************************
// CONSTRUCTORS ********************************************************************
// *********************************************************************************

    public LiteralTfCallBuiltin(CfgNodeCallBuiltin cfgNode) {
        this.cfgNode = cfgNode;
    }

// *********************************************************************************
// OTHER ***************************************************************************
// *********************************************************************************

    public LatticeElement transfer(LatticeElement inX) {

        LiteralLatticeElement in = (LiteralLatticeElement) inX;
        LiteralLatticeElement out = new LiteralLatticeElement(in);


        // SIMULATION OF BUILTIN FUNCTIONS
        // ...by letting the php binary do the work


        String functionName = this.cfgNode.getFunctionName();

        if (MyOptions.phpBin == null) {

            // don't simulate
            out.handleReturnValueBuiltin(this.cfgNode.getTempVar());

        } else {

            if (functionName.equals("realpath")) {

                Literal resultLit = this.simulate(in, functionName);
                out.handleReturnValue(this.cfgNode.getTempVar(), resultLit);

            } else if (functionName.equals("dirname")) {

                Literal resultLit = this.simulate(in, functionName);
                out.handleReturnValue(this.cfgNode.getTempVar(), resultLit);

            } else {
                // this function is not explicitly modelled
                out.handleReturnValueBuiltin(this.cfgNode.getTempVar());
            }
        }

        return out;

    }

    private Literal simulate(LiteralLatticeElement in, String functionName) {

        // retrieve the necessary parameters
        List<TacActualParam> paramList = this.cfgNode.getParamList();
        if (paramList.size() < 1) {
            // wrong number of params
            return Literal.TOP;
        }
        TacPlace param0 = paramList.get(0).getPlace();

        // check if we can resolve the necessary params
        Literal lit0 = in.getLiteral(param0);
        if (lit0 == Literal.TOP) {
            return Literal.TOP;
        }

        Runtime runtime = Runtime.getRuntime();

        // assemble command.....

        StringBuilder command = new StringBuilder("<? ");

        // change working directory
        command.append("chdir('");
        command.append(MyOptions.entryFile.getParent());
        command.append("');");

        // use "var_dump" to retrieve information about the output
        command.append("var_dump(");

        // funcname plus opening bracket
        command.append(functionName);
        command.append("(");

        // params
        command.append("'");
        command.append(lit0);
        command.append("'");

        // finish
        command.append(")); ?>");

        //System.out.println("command: " + command.toString());

        Literal resultLit = null;
        try {

            Process p = runtime.exec(MyOptions.phpBin);
            OutputStream outstream = p.getOutputStream();   // input for php
            InputStream instream = p.getInputStream();      // output of php

            Writer outwriter = new OutputStreamWriter(outstream);
            outwriter.write(command.toString());
            outwriter.flush();
            outwriter.close();
            p.waitFor();

            resultLit = this.parseOutput(instream);

        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        } catch (InterruptedException e) {
            throw new RuntimeException(e.getMessage());
        }

        return resultLit;

    }

    // parses the output returned by the php invocation into a literal
    private Literal parseOutput(InputStream instream) {

        String resultString = null;
        try {
            BufferedReader inreader = new BufferedReader(new InputStreamReader(instream));
            resultString = inreader.readLine();
            inreader.close();
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());        }

        if (resultString.startsWith("bool(")) {
            if (resultString.equals("bool(true)")) {
                return Literal.TRUE;
            } else {
                return Literal.FALSE;
            }
        } else if (resultString.startsWith("string(")) {
            return new Literal(resultString.substring(
                    resultString.indexOf(')') + 3,
                    resultString.length() - 1));
        } else {
            return Literal.TOP;
        }
    }
}