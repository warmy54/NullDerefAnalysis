package dfa;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.FileHandler;

import soot.*;

public class RunDataFlowAnalysis
{	
	public static void main(String[] args) throws SecurityException, IOException {
		
		File file = new File("ResultSoot");
		PrintStream out = new PrintStream(file);
		System.setOut(out);

		NullAnalysisProperties prop = new NullAnalysisProperties();
		AnaliseDir("/home/frederic/maven3.3.0",prop);
		//String[] i = {"Marker","/home/frederic/javacv/target/classes/org/bytedeco/javacv"};
		//Run(i,prop);
		System.out.println("Finihed");
		out.close();
	}
	public static void AnaliseDir(String path,NullAnalysisProperties prop) {
		try {
			Files.walk(Paths.get(path))
			.filter(w -> Files.isRegularFile(w) && w.toString().contains(".class"))
			.forEach(f -> WrapAndRun(f,prop));
			
		} catch (IOException e) {
			System.out.println("Erreur io in AnaliDir");
			e.printStackTrace();
		}
		
	}
	public static void WrapAndRun(Path f,NullAnalysisProperties prop) {
		//throw new RuntimeException(f.getFileName().toString() + "  " + f.getParent().toString());
		String[] i = {f.getFileName().toString().replace(".class", ""),f.getParent().toString()};
		Run(i,prop);
	}
	public static void Run(String[] args,NullAnalysisProperties prop) {
		soot.G.v();
		//we first need to reset everthing
		G.reset();
		
		//default argument if no argument given in path
		String mainClass = "ExpressionDocumenter";
		String classPath = "/home/frederic/maven3.3.0/maven/maven-compat/target/classes/org/apache/maven/usability/plugin";
		
		
		//if arguments are given use given in path
		if (args.length == 2) {
			mainClass = args[0];
			
			classPath = args[1];
			
		}
		System.out.println();
		System.out.println(mainClass);
		System.out.println(classPath);
		//Set up arguments for Soot
		String[] sootArgs = {
			"-cp", classPath, "-pp", 	// sets the class path for Soot
			"-allow-phantom-refs"
			,"-w", 						// Whole program analysis, necessary for using Transformer
			 "-p", "jb", "use-original-names:true", //use original name
			 "-p", "jb.ule", "off",		//stop jimple from removing unused locals
			 "-p", "jb.tr", "ignore-nullpointer-dereferences:true", //we want to show null deref not delete them
			 "-p", "jb.dae", "off", 	//stop jimple from removing useless asignment
			 
			"-src-prec", "java",		// Specify type of source file
										// Specify the main class 
			"-f", "J", 					// Specify type of output file
			"-keep-line-number",		//used to retrieve error location
			mainClass
		};
		         			
		// transformer for analysis
		prop.MainClass = mainClass;
		try {//configurations des propriétés
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
