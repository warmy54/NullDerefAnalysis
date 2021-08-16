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
import soot.util.IterableNumberer;



public class NullAnalysisTransformer extends SceneTransformer {

	NullAnalysisProperties prop;
	public NullAnalysisTransformer(NullAnalysisProperties prop) {
		this.prop = prop;//keep properties
	}
	@Override
	protected void internalTransform(String arg0, Map arg1) {
		//we analyse each methods so we have to get them from the scene
		SootClass studiedClass = Scene.v().getSootClass(prop.MainClass);
		List<SootMethod> studiedMethods = studiedClass.getMethods();
		System.out.println("Class studied = "+prop.MainClass);
		
		Iterator<SootMethod> iterMeth = studiedMethods.iterator();
		
		//we iterate over every methode of the class
		int numMeth = 0;
		int numMethDer = 0;
		while(iterMeth.hasNext()){
			
			SootMethod sMethod = iterMeth.next();
			if(sMethod.isConcrete()) {
				numMeth++;
			}
			System.out.println("Method number = " + numMeth  + " Num of deref = " + numMethDer);
			
			System.out.println(sMethod.getDeclaration());
			
			//we generate the graph from the main methnumber
			UnitGraph graph;
			try {
			 graph = new EnhancedUnitGraph(sMethod.getActiveBody());
			} catch  (Exception e) {
				System.out.println();
				continue;
			}
			//use the graph and do the anlysis
			nullFinderAnalysis analysis = new nullFinderAnalysis(graph,prop);
			Iterator<Unit> unitIt = graph.iterator();
			//then we analyse the results
			while (unitIt.hasNext()) {
				
				//System.out.println("next unit :");
				Unit s = unitIt.next();
	
				//System.out.println(s);
	
				NullableValueSet i = analysis.getFlowBefore(s);
				//i.print(); //Si besoin est on peut print le flow before
				//System.out.println("\n line number found = " + s.getJavaSourceStartLineNumber());
				Stmt stm = (Stmt) s;
				if (stm.containsInvokeExpr()) {
					InvokeExpr inv = stm.getInvokeExpr();
					if(inv instanceof InstanceInvokeExpr) {
						numMethDer++;
						String stat = i.get(((InstanceInvokeExpr) inv).getBase());
						if(stat.equals("Null")) {
							System.out.println("WARNING NULL DEREF on " + ((InstanceInvokeExpr) inv).getBase() + " at " + s.getJavaSourceStartLineNumber());
						}
						if(stat.equals("NCP")) {
							if (this.prop.ShowNCPWarning) {
								System.out.println("hWARNING NCP DEREF on " + ((InstanceInvokeExpr) inv).getBase() + " at " + s.getJavaSourceStartLineNumber());
							}
						}
						if(stat.equals("NSP")) {
							System.out.println("WARNING NSP DEREF on " + ((InstanceInvokeExpr) inv).getBase() + " at " + s.getJavaSourceStartLineNumber());
						}
					}
				}
				//there is no  direct way to check "deref" in soot as far as i know so 
				// so we have to check each case manually 
				if (stm.containsFieldRef()) {
					FieldRef inv = stm.getFieldRef();
					if(inv instanceof InstanceFieldRef) {
						numMethDer++;
						String stat = i.get(((InstanceFieldRef) inv).getBase());
						if(stat.equals("Null")) { 
							System.out.println("WARNING NULL DEREF on " + ((InstanceFieldRef) inv).getBase() + " at " + s.getJavaSourceStartLineNumber());
						}
						if (this.prop.ShowNCPWarning && stat.equals("NCP")) {
							System.out.println("WARNING NCP DEREF on " + ((InstanceFieldRef) inv).getBase() + " at " + s.getJavaSourceStartLineNumber());
						}
						if(stat.equals("NSP")) {
							System.out.println("WARNING NSP DEREF on " + ((InstanceFieldRef) inv).getBase() + " at " + s.getJavaSourceStartLineNumber());
						}
					}
					
				}
				if (stm.containsArrayRef()) {
					ArrayRef inv = stm.getArrayRef();
					String stat = i.get(inv.getBase());
					numMethDer++;
					if(stat.equals("Null")) {
						System.out.println("WARNING NULL DEREF on " + inv.getBase() + " at " + s.getJavaSourceStartLineNumber());
					}
					if (this.prop.ShowNCPWarning && stat.equals("NCP")) {
						System.out.println("WARNING NCP DEREF on " + inv.getBase() + " at " + s.getJavaSourceStartLineNumber());
					}
					if(stat.equals("NSP")) {
						System.out.println("WARNING NSP DEREF on " + inv.getBase() + " at " + s.getJavaSourceStartLineNumber());
					}
				}
				
				
			}
			System.out.println();//aestetics
		}
	
	}
}
