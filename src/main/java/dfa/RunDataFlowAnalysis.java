package dfa;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;


import soot.*;
import soot.Body;
import soot.NormalUnitPrinter;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.UnitPrinter;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;
import soot.jimple.internal.*;

public class RunDataFlowAnalysis
{
	public static void main(String[] args) {
		
		String mainClass = "testCLASS";

		
		/*** *** YOU SHOULD EDIT THIS BEFORE RUNNING *** ***/
		String classPath = "/home/frederic/soot"; 			//TODO CHANGE TO ARG PATH

		


		//Set up arguments for Soot
		String[] sootArgs = {
			"-cp", classPath, "-pp", 	// sets the class path for Soot
			"-w", 						// Whole program analysis, necessary for using Transformer
			 "-p", "jb", "use-original-names:true", //use original name
			 "-p", "jb.ule", "off",		//stop jimple from removing unused locals
			 "-p", "jb.tr", "ignore-nullpointer-dereferences:true", //we want to show null deref not delete them
			 "-p", "jb.dae", "off", 	//stop jimple from removing useless asignment
			 "-p", "jj.a", "enabled:false",
			 "-p", "jb.lns", "off",
			"-src-prec", "java",		// Specify type of source file
			"-main-class", mainClass,	// Specify the main class 
			"-f", "J", 					// Specify type of output file
			"-keep-line-number",		//used to retrieve error location
			mainClass 
		};
		 // transformer for analysis
		NullAnalysisProperties prop = new NullAnalysisProperties();

		try {
			prop.decode("config.properties");
		} catch (Exception e) {
			System.out.println("Configuration file decoding failed");
			e.printStackTrace();
		}
		NullAnalysisTransformer analysisTransformer = new NullAnalysisTransformer(prop);
		// Add transformer to appropriate Pack in PackManager. PackManager will run all Packs when main function of Soot is called
		PackManager.v().getPack("wjtp").add(new Transform("wjtp.dfa", analysisTransformer));

		// Call main function with arguments
		soot.Main.main(sootArgs);

	}
}
