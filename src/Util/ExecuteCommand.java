package Util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import  org.apache.commons.io.FileUtils;

public class ExecuteCommand {
	public static void backupFile(String filename){
		String bashcmd = "cp " + filename + " " + filename + ".bak";
		String[] cmd = new String[]{"/bin/bash", "-c", bashcmd};
		execute(cmd);
	}
	public  static  void copyAuxiliary(String path){
		try {
			FileUtils.copyFile(new File("./src/auxiliary/Dumper.java"), new File(path+"/auxiliary/Dumper.java"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public static void recoverFile(String filename){
		String rmcmd = "rm " + filename;
		String mvcmd = "mv " + filename + ".bak " + filename; 
		String[] cmd = new String[]{"/bin/bash", "-c", rmcmd};
		execute(cmd);
		String[] cmd2 = new String[]{"/bin/bash", "-c", mvcmd};
		execute(cmd2);		
	}
	public static void defects4jCompile(String projectPath) throws IOException{

	    String[] cmd = new String[]{"/bin/bash", "-c", "cd " + projectPath + " && " + Constant.COMMAND_D4J + " compile"};  
		String results = execute(cmd);	
		System.out.println(results);
	}
	public static void runFailTest(String project, int i, String projectPath, String failTest) throws IOException{
		String runTestCmd = getProjectTestCp(failTest, project, i);
		execute(new String[]{"/bin/bash", "-c", "cd " + projectPath + " && " + runTestCmd});
		System.out.println( runTestCmd);
	}
	public static String runFailTestforSwitching(String project, int i, String projectPath, String failTest) throws IOException{
		String runTestCmd = getProjectTestCpforSwitching(failTest, project, i);
		String result = execute(new String[]{"/bin/bash", "-c", "cd " + projectPath + " && " + runTestCmd});	
		return result;
	}
	public static void deleteTraceFile(String project, int i){
		String rmcmd = "rm " + Constant.IF_TRACE_FILE + project + "/" + i + ".txt";
		String[] cmd = new String[]{"/bin/bash", "-c", rmcmd};
		execute(cmd);	
	}
	
	private static String getProjectTestCp(String failTest, String project,int buggyId) throws IOException {
		// TODO Auto-generated method stub
		     String tempScript = "";
		     String root = Constant.ROOT;
	        if(project.equals("Chart")){
	        	tempScript = "java -cp \""+ Constant.D4J_Path + "/major/lib/junit-4.11.jar:"
	        			+  Constant.D4J_Path + "major/lib/auxiliary.jar:build:build-tests:"
	        			+ Constant.D4J_Path + "major/lib/SingleJUnitTestRunner\" SingleJUnitTestRunner " + failTest + " Chart " + buggyId + " " + root;
	        }else if(project.equals("Closure")){
	        	tempScript = "java -cp \""+ Constant.D4J_Path + "/major/lib/junit-4.11.jar:"
	        			+   Constant.D4J_Path + "/major/lib/auxiliary.jar:build/lib/rhino.jar:lib/*:build/classes:build/test:"
	        			+ Constant.D4J_Path + "major/lib/SingleJUnitTestRunner\" SingleJUnitTestRunner " +  failTest + " Closure " + buggyId+ " " + root;
	        }else if(project.equals("Lang")){
	        	tempScript = "java -cp \""+ Constant.D4J_Path + "/major/lib/junit-4.11.jar:"
	        			+   Constant.D4J_Path + "/major/lib/auxiliary.jar:target/test-classes:target/tests:target/classes:"
	        			+ Constant.D4J_Path + "major/lib/SingleJUnitTestRunner\" SingleJUnitTestRunner "+ failTest + " Lang " + buggyId+ " " + root;
	        }else if(project.equals("Math")){
	        	tempScript = "java -cp \""+ Constant.D4J_Path + "/major/lib/junit-4.11.jar:"
	        			+   Constant.D4J_Path + "/major/lib/auxiliary.jar:target/test-classes:target/classes:"
	        			+ Constant.D4J_Path + "major/lib/SingleJUnitTestRunner\" SingleJUnitTestRunner " + failTest + " Math " + buggyId+ " " + root;
	        }else if(project.equals("Time")){
	        	tempScript = "java -cp \""+ Constant.D4J_Path + "/major/lib/junit-4.11.jar:"
	        			+   Constant.D4J_Path + "/major/lib/auxiliary.jar:target/test-classes:target/classes:build/classes:build/tests:"
	        			+ Constant.D4J_Path + "major/lib/SingleJUnitTestRunner\" SingleJUnitTestRunner " + failTest + " Time " + buggyId+ " " + root;
	        }
	        return tempScript;
	}
	
	private static String getProjectTestCpforSwitching(String failTest, String project,int buggyId) throws IOException {
		// TODO Auto-generated method stub
		     String tempScript = "";	      
	        if(project.equals("Chart")){
	        	tempScript = "timeout 20s java -cp \""+ Constant.D4J_Path + "/major/lib/junit-4.11.jar:"
	        			+ "build:build-tests:"
	        			+ Constant.D4J_Path + "major/lib/SingleJUnitTestRunner\" SingleJUnitTestRunnerSwitch " + failTest + " Chart " + buggyId;
	        }else if(project.equals("Closure")){
	        	tempScript = "timeout 20s java -cp \""+ Constant.D4J_Path + "/major/lib/junit-4.11.jar:"
	        			+ "build/lib/rhino.jar:lib/*:build/classes:build/test:"
	        			+ Constant.D4J_Path + "major/lib/SingleJUnitTestRunner\" SingleJUnitTestRunnerSwitch " +  failTest + " Closure " + buggyId;
	        }else if(project.equals("Lang")){
	        	tempScript = "timeout 20s java -cp \""+ Constant.D4J_Path + "/major/lib/junit-4.11.jar:"
	        			+ "target/test-classes:target/tests:target/classes:"
	        			+ Constant.D4J_Path + "major/lib/SingleJUnitTestRunner\" SingleJUnitTestRunnerSwitch " + failTest + " Lang " + buggyId;
	        }else if(project.equals("Math")){
	        	tempScript = "timeout 20s java -cp \""+ Constant.D4J_Path + "/major/lib/junit-4.11.jar:"
	        			+ "target/test-classes:target/classes:"
	        			+ Constant.D4J_Path + "major/lib/SingleJUnitTestRunner\" SingleJUnitTestRunnerSwitch " + failTest + " Math " + buggyId;
	        }else if(project.equals("Time")){
	        	tempScript = "timeout 20s java -cp \""+ Constant.D4J_Path + "/major/lib/junit-4.11.jar:"
	        			+ "target/test-classes:target/classes:build/classes:build/tests:"
	        			+ Constant.D4J_Path + "major/lib/SingleJUnitTestRunner\" SingleJUnitTestRunnerSwitch "+ failTest + " Time " + buggyId ;
	        }
	        return tempScript;
	}
	private static String execute(String... command) {
		//System.out.println(command);
		Process process = null;
		final List<String> results = new ArrayList<String>();
		final List<String> errorInfo = new ArrayList<String>();
		try {
			ProcessBuilder builder = new ProcessBuilder(command);
			builder.redirectErrorStream(true);
			process = builder.start();
			final InputStream inputStream = process.getInputStream();
			//final InputStream errorStream = process.getErrorStream();
			
			Thread processReader = new Thread(){
				public void run() {
					BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
					String line;
					try {
						while((line = reader.readLine()) != null) {
							results.add(line + "\n");
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
					try {
						reader.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			};
	
					
			processReader.start();
			try {
				processReader.join();
				process.waitFor();
			} catch (InterruptedException e) {
				System.out.println("#execute Process interrupted !");
				return "";
			}
		} catch (IOException e) {
			System.out.println( "#execute Process output redirect exception !");
		} finally {
			if (process != null) {
				process.destroy();
			}
			process = null;
		}
		
		String result = "";
		for(String s: results) {
			result += s;
		}
		return result;
	}
}
