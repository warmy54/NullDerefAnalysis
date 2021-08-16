package dfa;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Properties;

public class NullAnalysisProperties {
		
		boolean SettingDefault; //default behavior
		boolean ShowNCPWarning =true; 
		boolean TrustInvocationsReturn = false;
		boolean LooseInfoOnInvocations = false;
		boolean TrustFieldRefReturn = false;
		boolean TrustArrayRefReturn = false;
		boolean TrustParameterRefReturn = false;
		String MainClass = "";
		
		public NullAnalysisProperties() {SettingDefault = true;}
		public void decode(String path) throws IOException {
	     
			try (InputStream input = new FileInputStream(path)) {

	            Properties prop = new Properties();
	            prop.load(input);
	            
	            
	            String NCPString = prop.getProperty("ShowNCPWarning");
	            if(NCPString == null) {} else {
		            if (NCPString.contentEquals("Yes") || NCPString.contentEquals("No")) {
		            	ShowNCPWarning = YesNoToBoolean(NCPString);
		            }
	            }
	            
	            
	            String TrustInvocationsString = prop.getProperty("TrustInvocations");
	            if(TrustInvocationsString == null) {} else {
	            	if (TrustInvocationsString.contentEquals("Yes") || TrustInvocationsString.contentEquals("No")) {
		            	TrustInvocationsReturn = YesNoToBoolean(TrustInvocationsString);
		            }
	            }
	            
	            
	            String LooseInfoOnInvocationsString = prop.getProperty("LooseInfoOnInvocations");
	            if(LooseInfoOnInvocationsString == null) {} else {
	            	if (LooseInfoOnInvocationsString.contentEquals("Yes") || LooseInfoOnInvocationsString.contentEquals("No")) {
	            		LooseInfoOnInvocations = YesNoToBoolean(LooseInfoOnInvocationsString);
		            }
	            }
	            
	            String TrustFieldRefString = prop.getProperty("TrustFieldRef");
	            if(TrustFieldRefString == null) {} else {
	            	if (TrustFieldRefString.contentEquals("Yes") || TrustFieldRefString.contentEquals("No")) {
		            	TrustFieldRefReturn = YesNoToBoolean(TrustFieldRefString);
		            }
	            }
	            
	            String TrustArrayRefString = prop.getProperty("TrustArrayRef");
	            if(TrustArrayRefString == null) {} else {
	            	if (TrustArrayRefString.contentEquals("Yes") || TrustArrayRefString.contentEquals("No")) {
		            	TrustArrayRefReturn = YesNoToBoolean(TrustArrayRefString);
		            }
	            }
	            
	            String TrustParameterRefString = prop.getProperty("TrustParameterRef");
	            if(TrustParameterRefString == null) {} else {
	            	if (TrustParameterRefString.contentEquals("Yes") || TrustParameterRefString.contentEquals("No")) {
	            		TrustParameterRefReturn = YesNoToBoolean(TrustParameterRefString);
		            }
	            }
	            
	            
	            SettingDefault = false;
			} catch (IOException io) {
	            io.printStackTrace();
	        }
			
		}
		public boolean YesNoToBoolean(String s) {
			if (s.contentEquals("Yes")) {
				return true;
			} else if (s.contentEquals("No")) {
				return false;
			} else {
				throw new IllegalArgumentException("neither yes or no given");
			}
		}
		
		public static void main(String[] argv) throws IOException {
			NullAnalysisProperties prop = new NullAnalysisProperties();
			prop.decode("config.properties");
			System.out.println(prop.LooseInfoOnInvocations);
		}
}
