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
		
		boolean SettingDefault;
		boolean ShowNCPWarning =true; //default behavior
		String user;
		String password;
		
		public NullAnalysisProperties() {SettingDefault = true;}
		public void decode(String path) throws IOException {
	     
			try (InputStream input = new FileInputStream(path)) {

	            Properties prop = new Properties();
	            prop.load(input);
	            String NCPString = prop.getProperty("ShowNCPWarning");

	            if (NCPString.contentEquals("Yes") || NCPString.contentEquals("No")) {
	            	ShowNCPWarning = YesNoToBoolean(prop.getProperty("ShowNCPWarning"));
	            }
	            user = prop.getProperty("db.user");

	            password = prop.getProperty("db.password");
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
			System.out.println(prop.ShowNCPWarning);
		}
}
