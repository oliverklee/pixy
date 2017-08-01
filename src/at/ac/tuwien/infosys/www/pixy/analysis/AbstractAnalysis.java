package at.ac.tuwien.infosys.www.pixy.analysis;

import java.util.*;

import at.ac.tuwien.infosys.www.pixy.conversion.ControlFlowGraph;
import at.ac.tuwien.infosys.www.pixy.conversion.TacFunction;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AbstractCfgNode;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AssignArray;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AssignBinary;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AssignReference;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AssignSimple;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AssignUnary;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.BasicBlock;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.CallBuiltinFunction;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.CallPreparation;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.CallReturn;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.CallUnknownFunction;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.CfgEntry;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.Define;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.Echo;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.Global;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.Include;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.IncludeEnd;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.IncludeStart;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.Isset;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.Static;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.Tester;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.Unset;

public abstract class AbstractAnalysis {

	protected List<TacFunction> functions;

	protected AbstractAnalysisInformation genericAnalysisInfo;

	protected AbstractLattice lattice;

	protected AbstractLatticeElement startValue;

	protected AbstractLatticeElement initialValue;

	protected abstract void initLattice();

	protected AbstractTransferFunction createTf(AbstractCfgNode cfgNodeX, TacFunction traversedFunction,
			AbstractCfgNode enclosingNode) {

		if (cfgNodeX instanceof BasicBlock) {

			BasicBlock cfgNode = (BasicBlock) cfgNodeX;
			return this.makeBasicBlockTf(cfgNode, traversedFunction);

		} else if (cfgNodeX instanceof AssignSimple) {

			return this.assignSimple(cfgNodeX, enclosingNode);

		} else if (cfgNodeX instanceof AssignUnary) {

			return this.assignUnary(cfgNodeX, enclosingNode);

		} else if (cfgNodeX instanceof AssignBinary) {

			return this.assignBinary(cfgNodeX, enclosingNode);

		} else if (cfgNodeX instanceof AssignReference) {

			return this.assignRef(cfgNodeX);

		} else if (cfgNodeX instanceof Unset) {

			return this.unset(cfgNodeX);

		} else if (cfgNodeX instanceof AssignArray) {

			return this.assignArray(cfgNodeX);

		} else if (cfgNodeX instanceof Isset) {

			return this.isset(cfgNodeX);

		} else if (cfgNodeX instanceof CallPreparation) {

			return this.callPrep(cfgNodeX, traversedFunction);

		} else if (cfgNodeX instanceof CfgEntry) {

			return this.entry(traversedFunction);

		} else if (cfgNodeX instanceof CallReturn) {

			return this.callRet(cfgNodeX, traversedFunction);

		} else if (cfgNodeX instanceof CallBuiltinFunction) {

			return this.callBuiltin(cfgNodeX, traversedFunction);

		} else if (cfgNodeX instanceof CallUnknownFunction) {

			return this.callUnknown(cfgNodeX, traversedFunction);

		} else if (cfgNodeX instanceof Global) {

			return this.global(cfgNodeX);

		} else if (cfgNodeX instanceof Define) {

			return this.define(cfgNodeX);

		} else if (cfgNodeX instanceof Tester) {

			return this.tester(cfgNodeX);

		} else if (cfgNodeX instanceof Echo) {

			return this.echo(cfgNodeX, traversedFunction);

		} else if (cfgNodeX instanceof Static) {

			return this.staticNode();

		} else if (cfgNodeX instanceof Include) {

			return this.include(cfgNodeX);

		} else if (cfgNodeX instanceof IncludeStart) {

			return this.includeStart(cfgNodeX);

		} else if (cfgNodeX instanceof IncludeEnd) {

			return this.includeEnd(cfgNodeX);

		} else {
			return TransferFunctionId.INSTANCE;
		}
	}

	protected void traverseCfg(ControlFlowGraph cfg, TacFunction traversedFunction) {

		for (Iterator<?> iter = cfg.dfPreOrder().iterator(); iter.hasNext();) {

			AbstractCfgNode cfgNodeX = (AbstractCfgNode) iter.next();
			AbstractTransferFunction tf = this.createTf(cfgNodeX, traversedFunction, cfgNodeX);
			if (tf == null) {
				System.out.println(cfgNodeX.getLoc());
				throw new RuntimeException("SNH");
			}
			this.genericAnalysisInfo.add(cfgNodeX, this.makeAnalysisNode(cfgNodeX, tf));
		}
	}

	public List<TacFunction> getFunctions() {
		return this.functions;
	}

	public int size() {
		return this.genericAnalysisInfo.size();
	}

	public AbstractLatticeElement getStartValue() {
		return this.startValue;
	}

	public AbstractLattice getLattice() {
		return this.lattice;
	}

	protected AbstractTransferFunction makeBasicBlockTf(BasicBlock basicBlock, TacFunction traversedFunction) {

		CompositeTransferFunction ctf = new CompositeTransferFunction();

		for (Iterator<?> iter = basicBlock.getContainedNodes().iterator(); iter.hasNext();) {
			AbstractCfgNode cfgNodeX = (AbstractCfgNode) iter.next();
			ctf.add(this.createTf(cfgNodeX, traversedFunction, basicBlock));
		}
		return ctf;
	}

	protected AbstractTransferFunction assignSimple(AbstractCfgNode cfgNodeX, AbstractCfgNode aliasInNode) {
		return TransferFunctionId.INSTANCE;
	}

	protected AbstractTransferFunction assignUnary(AbstractCfgNode cfgNodeX, AbstractCfgNode aliasInNode) {
		return TransferFunctionId.INSTANCE;
	}

	protected AbstractTransferFunction assignBinary(AbstractCfgNode cfgNodeX, AbstractCfgNode aliasInNode) {
		return TransferFunctionId.INSTANCE;
	}

	protected AbstractTransferFunction assignRef(AbstractCfgNode cfgNodeX) {
		return TransferFunctionId.INSTANCE;
	}

	protected AbstractTransferFunction unset(AbstractCfgNode cfgNodeX) {
		return TransferFunctionId.INSTANCE;
	}

	protected AbstractTransferFunction assignArray(AbstractCfgNode cfgNodeX) {
		return TransferFunctionId.INSTANCE;
	}

	protected AbstractTransferFunction callPrep(AbstractCfgNode cfgNodeX, TacFunction traversedFunction) {
		return TransferFunctionId.INSTANCE;
	}

	protected AbstractTransferFunction entry(TacFunction traversedFunction) {
		return TransferFunctionId.INSTANCE;
	}

	protected AbstractTransferFunction callRet(AbstractCfgNode cfgNodeX, TacFunction traversedFunction) {
		return TransferFunctionId.INSTANCE;
	}

	protected AbstractTransferFunction callBuiltin(AbstractCfgNode cfgNodeX, TacFunction traversedFunction) {
		return TransferFunctionId.INSTANCE;
	}

	protected AbstractTransferFunction callUnknown(AbstractCfgNode cfgNodeX, TacFunction traversedFunction) {
		return TransferFunctionId.INSTANCE;
	}

	protected AbstractTransferFunction global(AbstractCfgNode cfgNodeX) {
		return TransferFunctionId.INSTANCE;
	}

	protected AbstractTransferFunction isset(AbstractCfgNode cfgNodeX) {
		return TransferFunctionId.INSTANCE;
	}

	protected AbstractTransferFunction define(AbstractCfgNode cfgNodeX) {
		return TransferFunctionId.INSTANCE;
	}

	protected AbstractTransferFunction tester(AbstractCfgNode cfgNodeX) {
		return TransferFunctionId.INSTANCE;
	}

	protected AbstractTransferFunction echo(AbstractCfgNode cfgNodeX, TacFunction traversedFunction) {
		return TransferFunctionId.INSTANCE;
	}

	protected AbstractTransferFunction staticNode() {
		return TransferFunctionId.INSTANCE;
	}

	protected AbstractTransferFunction include(AbstractCfgNode cfgNodeX) {
		return TransferFunctionId.INSTANCE;
	}

	protected AbstractTransferFunction includeStart(AbstractCfgNode cfgNodeX) {
		return TransferFunctionId.INSTANCE;
	}

	protected AbstractTransferFunction includeEnd(AbstractCfgNode cfgNodeX) {
		return TransferFunctionId.INSTANCE;
	}

	protected abstract AbstractAnalysisNode makeAnalysisNode(AbstractCfgNode cfgNode, AbstractTransferFunction tf);

	public abstract AbstractLatticeElement recycle(AbstractLatticeElement recycleMe);
}
