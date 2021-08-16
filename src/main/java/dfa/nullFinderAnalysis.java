package dfa;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

import soot.Local;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.AnyNewExpr;
import soot.jimple.ArrayRef;
import soot.jimple.CaughtExceptionRef;
import soot.jimple.ClassConstant;
import soot.jimple.DefinitionStmt;
import soot.jimple.EqExpr;
import soot.jimple.FieldRef;
import soot.jimple.InstanceFieldRef;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InstanceOfExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.MonitorStmt;
import soot.jimple.NeExpr;
import soot.jimple.NullConstant;
import soot.jimple.ParameterRef;
import soot.jimple.Ref;
import soot.jimple.Stmt;
import soot.jimple.StringConstant;
import soot.jimple.ThisRef;
import soot.jimple.internal.AbstractBinopExpr;
import soot.jimple.internal.JCastExpr;
import soot.jimple.internal.JIfStmt;
import soot.shimple.PhiExpr;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.BackwardFlowAnalysis;
import soot.toolkits.scalar.FlowSet;
import soot.toolkits.scalar.ForwardBranchedFlowAnalysis;
import soot.toolkits.scalar.ArraySparseSet;



public class nullFinderAnalysis extends ForwardBranchedFlowAnalysis<NullableValueSet>{
	public int cmptp = 0;

	
	UnitGraph graph;
	NullAnalysisProperties prop;
	
	public nullFinderAnalysis(UnitGraph graph,NullAnalysisProperties prop) {
		super(graph);
		this.prop = prop;
		this.graph = graph;
		doAnalysis();
	}

	@Override
	protected void flowThrough(NullableValueSet in, Unit s, List<NullableValueSet> fallOut,
		List<NullableValueSet> branchOuts) {
		//System.out.println("flow + " + s.toString());
		//System.out.println("in of size" + in.valueSet.size()+ " = ["+ in + " ] ");
		NullableValueSet out = new NullableValueSet();
		NullableValueSet outBranch = new NullableValueSet(); 
		copy(in, out);
		copy(in,outBranch); //on crée des set out et outbranch qui deviendrons les contenus de fallout et branchouts
		//System.out.println("predecesor with size = " + graph.getPredsOf(s).size() + " " +graph.getPredsOf(s));
		Stmt stm = (Stmt) s;
		if (stm.containsInvokeExpr()) {
			HandleInvokeExpr(stm,out,outBranch);
		}
		if (stm.containsFieldRef()) {
			HandleFieldExpr(stm,out,outBranch);

		}
		if (stm.containsArrayRef()) {
			HandleArrayRef(stm,out,outBranch);
		}
		if (stm instanceof DefinitionStmt){
			Value left = ((DefinitionStmt) stm).getLeftOp();
			Value right = ((DefinitionStmt) stm).getRightOp();
			HandleLDef(out,left,right);
		}
		
		if (stm instanceof JIfStmt) {
		      handleIfStmt(stm, out, outBranch);
		    } else if (stm instanceof MonitorStmt) {
		      // in case of a monitor statement, we know that if it succeeds, we have a non-null value
		      out.add(((MonitorStmt) stm).getOp(), "NonNull");
		    }
		
		for	(NullableValueSet next: fallOut) {
			next.replace(out);
		}
		for	(NullableValueSet next: branchOuts) {
			next.replace(outBranch);
		}
		//System.out.println("Descendant = " +  graph.getSuccsOf(s));
		//System.out.println("out après = " + fallOut);
		//System.out.println("out branch apres = " + branchOuts +" size = " + branchOuts.size() + "\n\n\n");
	}

	@Override
	protected NullableValueSet newInitialFlow() {
		return new NullableValueSet();
	}

	@Override
	protected void merge(NullableValueSet in1, NullableValueSet in2, NullableValueSet out) {
		NullableValueSet newout = NullableValueSet.merge(in1, in2);
		out.valueSet = newout.valueSet;
		//System.out.println("merge anal = " + out.valueSet.size());
	}

	@Override
	protected void copy(NullableValueSet source, NullableValueSet dest) {
		//System.out.println("COPY");
		//source.print();
		dest.replace(source);	
		//dest.print();
		//System.out.println("\n\n\n");
	}
	
	private void HandleLDef(NullableValueSet out,Value left,Value right) {
		
		while (right instanceof JCastExpr) {
		      right = ((JCastExpr) right).getOp();
		    } //if right has a cast we remove it.
		
		if(right instanceof NullConstant) {//if right is null constant we asigne left null status
			out.add(left, "Null");
		} else //If right is something that we know can't be Null we give it nonnull status
		if(right instanceof AnyNewExpr || right instanceof ThisRef 
				|| right instanceof ClassConstant || right instanceof CaughtExceptionRef) {
			out.add(left, "NonNull");
		} else //if right is a known value we give its status to left
		if (out.contains(right)){
			out.add(left, NullableValueSet.getStatus(out, right));
		} else{ //if non of those things we check if user gave instruction on what to trust
			if (right instanceof InvokeExpr) {
				if(prop.TrustInvocationsReturn) {
					out.add(left, "NonNull");
					return;
				} 
			} else if (right instanceof FieldRef) {
				if (prop.TrustFieldRefReturn) {
					out.add(left, "NonNull");
					return;
				}
			} else if (right instanceof ArrayRef) {
				if (prop.TrustArrayRefReturn) {
					out.add(left, "NonNull");
					return;
				}
			} else if (right instanceof ParameterRef){
				if (prop.TrustParameterRefReturn) {
					out.add(left, "NonNull");
					return;
				}
			} else {//if not the case we just give it NSP because we don't know
					out.add(left, "NSP");
				}
			}
		}
	
	
	
	private void HandleFieldExpr(Stmt stm,NullableValueSet out,NullableValueSet outBranch) {
		FieldRef e = stm.getFieldRef();
		if(e instanceof InstanceFieldRef) {//si field ref a une base qui est déréferencé, elle n'est pas nulle
			if(stm.branches()) {
				outBranch.add(((InstanceFieldRef) e).getBase(), "NonNull");
			} 
			if(stm.fallsThrough()) {
				out.add(((InstanceFieldRef) e).getBase(), "NonNull");
			}

			//System.out.println("FieldRef");
		}
	}
	
	private void HandleArrayRef(Stmt stm,NullableValueSet out,NullableValueSet outBranch) {
		Value base = stm.getArrayRef().getBase();
		//si on optient un objet de l'array, c'est que l'array n'est pas null
		if(stm.branches()) {
			outBranch.add(base, "NonNull");
		} 
		if(stm.fallsThrough()) {
			out.add(base, "NonNull");
		}
		//System.out.println("ArrayRef");
	}
	
	
	private void HandleInvokeExpr(Stmt stm,NullableValueSet out,NullableValueSet outBranch) {
		InvokeExpr e  = stm.getInvokeExpr();

		//System.out.println("InvokeExpr");
		if(prop.LooseInfoOnInvocations) {
			for(NullableValue v:out) {
				if(v.status.contentEquals("NonNull")) {
					v.status = "NCP";
				}
			}
			for(NullableValue v:outBranch) {
				if(v.status.contentEquals("NonNull")) {
					v.status = "NCP";
				}
			}
		}
		//Si le statement déreférence qqch alors on met la valeure déréferencé nn-null
		if (e instanceof InstanceInvokeExpr) {
			if(stm.branches()) {
				outBranch.add(((InstanceInvokeExpr) e).getBase(), "NonNull");
			} 
			if(stm.fallsThrough()) {
				out.add(((InstanceInvokeExpr) e).getBase(), "NonNull");
			}
		}
		
	}

	private void handleIfStmt(Stmt stm,NullableValueSet out,NullableValueSet outBranch) {
		JIfStmt sif = (JIfStmt) stm;
		AbstractBinopExpr cond = (AbstractBinopExpr) sif.getCondition();
		if(cond instanceof EqExpr || cond instanceof NeExpr) {
			HandleEquality(stm,cond,out,outBranch);
		} else if(cond instanceof InstanceOfExpr) {//if instanceof is true then the thing tested is not null
			outBranch.add(((InstanceOfExpr) cond).getOp(), "NonNull");
		}
	}
	private void HandleEquality(Stmt stm, AbstractBinopExpr cond,NullableValueSet out,NullableValueSet outBranch){
		Value left = cond.getOp1();
	    Value right = cond.getOp2();
		//System.out.println("\n\n\nENTERING EQUALITY left = " + left + " right = "+ right);
		//System.out.println("left is ref " + (left instanceof PhiExpr) + "  "+ left.getClass());
		//System.out.println("left is ref " + (right instanceof PhiExpr)+ "  "+ right.getClass());
		Value val = null;
	    if (left == NullConstant.v()) {
	      if (right != NullConstant.v()) {
	        val = right;
	        
	      }
	    } else if (right == NullConstant.v()) {
	      if (left != NullConstant.v()) {
	        val = left;
	      }
	    }
	    if (val != null && val instanceof Local) {
	        if (cond instanceof EqExpr) {
	          // a==null then  fall non null and branch null
	         out.add(val, "NonNull");
	         outBranch.add(val, "Null");
	        } else {
	          // a!=null then the opposite
	        	out.add(val, "Null");
		         outBranch.add(val, "NonNull");
	        } 
	      }
	    //System.out.println(val);
	    //System.out.println( "\n\n\n");
	}
}
