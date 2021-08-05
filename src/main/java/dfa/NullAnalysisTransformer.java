package dfa;

import java.util.*;
import soot.*;
import soot.baf.IfNonNullInst;
import soot.baf.Inst;
import soot.baf.TargetArgInst;
import soot.jimple.*;
import soot.shimple.PhiExpr;
import soot.toolkits.graph.*;
import soot.toolkits.graph.pdg.EnhancedUnitGraph;
import soot.toolkits.scalar.BackwardFlowAnalysis;
import soot.toolkits.scalar.FlowSet;



public class NullAnalysisTransformer extends SceneTransformer {

	NullAnalysisProperties prop;
	public NullAnalysisTransformer(NullAnalysisProperties prop) {
		this.prop = prop;
	}
	@Override
	protected void internalTransform(String arg0, Map arg1) {

		SootMethod sMethod = Scene.v().getMainMethod();

		UnitGraph graph = new EnhancedUnitGraph(sMethod.getActiveBody());
		nullFinderAnalysis analysis = new nullFinderAnalysis(graph,prop);
		Iterator<Unit> unitIt = graph.iterator();
		
		while (unitIt.hasNext()) {
			
			System.out.println("");
			Unit s = unitIt.next();

			System.out.println(s);

			int d = 50 - s.toString().length();
			while (d > 0) {
				System.out.print(" ");
				d--;
			}
			NullableValueSet i = analysis.getFlowBefore(s);
			System.out.println("A");
			i.print();
			System.out.println("\n" + s.getJavaSourceStartLineNumber());
			Stmt stm = (Stmt) s;
			if (stm.containsInvokeExpr()) {
				InvokeExpr inv = stm.getInvokeExpr();
				if(inv instanceof InstanceInvokeExpr) {
					String stat = i.get(((InstanceInvokeExpr) inv).getBase());
					if(stat.equals("Null")) {
						System.out.println("WARNING NULL DEREF");
					}
					if(stat.equals("NCP")) {
						if (this.prop.ShowNCPWarning) {
							System.out.println("WARNING NCP DEREF");
						}
					}
					if(stat.equals("NSP")) {
						System.out.println("WARNING NSP DEREF");
					}
				}
			}
			if (stm.containsFieldRef()) {
				FieldRef inv = stm.getFieldRef();
				if(inv instanceof InstanceFieldRef) {
					String stat = i.get(((InstanceFieldRef) inv).getBase());
					if(stat.equals("Null")) {
						System.out.println("WARNING NULL DEREF");
					}
					if (this.prop.ShowNCPWarning) {
						System.out.println("WARNING NCP DEREF");
					}
					if(stat.equals("NSP")) {
						System.out.println("WARNING NSP DEREF");
					}
				}
				
			}
			if (stm.containsArrayRef()) {
				ArrayRef inv = stm.getArrayRef();
				String stat = i.get(inv.getBase());
				if(stat.equals("Null")) {
					System.out.println("WARNING NULL DEREF");
				}
				if (this.prop.ShowNCPWarning) {
					System.out.println("WARNING NCP DEREF");
				}
				if(stat.equals("NSP")) {
					System.out.println("WARNING NSP DEREF");
				}
			}
			
			
		}
		
	}
}
