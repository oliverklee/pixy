package at.ac.tuwien.infosys.www.pixy.analysis.literal.transferfunction;

import java.io.*;
import java.util.*;

import at.ac.tuwien.infosys.www.pixy.MyOptions;
import at.ac.tuwien.infosys.www.pixy.analysis.AbstractLatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.AbstractTransferFunction;
import at.ac.tuwien.infosys.www.pixy.analysis.literal.LiteralLatticeElement;
import at.ac.tuwien.infosys.www.pixy.conversion.AbstractTacPlace;
import at.ac.tuwien.infosys.www.pixy.conversion.Literal;
import at.ac.tuwien.infosys.www.pixy.conversion.TacActualParameter;

public class CallBuiltinFunction extends AbstractTransferFunction {

	private at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.CallBuiltinFunction cfgNode;

	public CallBuiltinFunction(at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.CallBuiltinFunction cfgNode) {
		this.cfgNode = cfgNode;
	}

	public AbstractLatticeElement transfer(AbstractLatticeElement inX) {

		LiteralLatticeElement in = (LiteralLatticeElement) inX;
		LiteralLatticeElement out = new LiteralLatticeElement(in);
		String functionName = this.cfgNode.getFunctionName();

		if (MyOptions.phpBin == null) {
			out.handleReturnValueBuiltin(this.cfgNode.getTempVar());
		} else {

			if (functionName.equals("realpath")) {
				Literal resultLit = this.simulate(in, functionName);
				out.handleReturnValue(this.cfgNode.getTempVar(), resultLit);
			} else if (functionName.equals("dirname")) {
				Literal resultLit = this.simulate(in, functionName);
				out.handleReturnValue(this.cfgNode.getTempVar(), resultLit);
			} else {
				out.handleReturnValueBuiltin(this.cfgNode.getTempVar());
			}
		}
		return out;
	}

	private Literal simulate(LiteralLatticeElement in, String functionName) {
		List<TacActualParameter> paramList = this.cfgNode.getParamList();
		if (paramList.size() < 1) {
			return Literal.TOP;
		}
		AbstractTacPlace param0 = paramList.get(0).getPlace();
		Literal lit0 = in.getLiteral(param0);
		if (lit0 == Literal.TOP) {
			return Literal.TOP;
		}
		Runtime runtime = Runtime.getRuntime();
		StringBuilder command = new StringBuilder("<? ");

		command.append("chdir('");
		command.append(MyOptions.entryFile.getParent());
		command.append("');");

		command.append("var_dump(");

		command.append(functionName);
		command.append("(");

		command.append("'");
		command.append(lit0);
		command.append("'");

		command.append(")); ?>");

		Literal resultLit = null;
		try {

			Process p = runtime.exec(MyOptions.phpBin);
			OutputStream outstream = p.getOutputStream();
			InputStream instream = p.getInputStream();

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

	private Literal parseOutput(InputStream instream) {

		String resultString = null;
		try {
			BufferedReader inreader = new BufferedReader(new InputStreamReader(instream));
			resultString = inreader.readLine();
			inreader.close();
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage());
		}

		if (resultString.startsWith("bool(")) {
			if (resultString.equals("bool(true)")) {
				return Literal.TRUE;
			} else {
				return Literal.FALSE;
			}
		} else if (resultString.startsWith("string(")) {
			return new Literal(resultString.substring(resultString.indexOf(')') + 3, resultString.length() - 1));
		} else {
			return Literal.TOP;
		}
	}
}