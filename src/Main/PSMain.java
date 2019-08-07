package Main;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.jdt.core.dom.CompilationUnit;

import Instrument.IfStatementInstrumentVisitor;
import Util.ExecuteCommand;
import Util.ParseFile;
import Util.Utils;
import Util.D4jInfo;
import Util.Constant;

public class PSMain {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
        String project = "Chart";
        int buggyStart = 1;
        int buggyEnd = 1;
        
        int i = 0;
		if (args.length == 3){
			project = args[0];
			buggyStart = Integer.parseInt(args[1]);
			buggyEnd = Integer.parseInt(args[2]);
			ExecutorService executor = Executors.newSingleThreadExecutor();  
			for (i = buggyStart; i <= buggyEnd; i++){
				System.out.println("--------------  instrument " + project  + "  " +  i + " -------------------");
				instrument(project,i);
				System.out.println("--------------  compile " + project  + "  " +  i + " -------------------");
				String prePath = Constant.PROJECT_PRE_PATH + project + "/" + project + "_" + i+ "_buggy";
				defects4jCompile(prePath);
				ExecuteCommand.deleteTraceFile(project, i);
				System.out.println("--------------  run fail test " + project  + "  " +  i + " -------------------");
				String failTestFile = Constant.FAIL_TEST_FILE +  project + "/" + i + ".txt";		
				runFailTest(project, i, prePath, failTestFile);
				System.out.println("--------------  recover file  -------------------");
				recoverFile(project,i);
				
				System.out.println("--------------  Switching " + project  + "  " +  i + " -------------------");
				switching(project,i, executor);
				System.out.println("------------------- program complete ------------------");
			}
			executor.shutdownNow();		
		}else {
			System.out.println("illegal argument !");
		}

	}


	private static void instrument(String project ,int i){
		
		String prePath = Constant.PROJECT_PRE_PATH + project + "/" + project + "_" + i+ "_";
		D4jInfo d4j = new Util.D4jInfo(project,i,prePath);	
		
		String sourceFilePath = d4j.getProSrc();
		String instrumentWritefile = Constant.IF_TRACE_FILE
                + project +  "/" + i + ".txt";      // record the running failing test result
		
		List<String> filelist = new ArrayList<String>();
		filelist = Utils.getAllFiles(filelist, ".java", sourceFilePath);
		
		System.out.println(filelist.size());
		for(String file: filelist){
			backupFile(file);
			CompilationUnit cu = Utils.constuctCompilationUnit(file);
			IfStatementInstrumentVisitor ifvisitor = new IfStatementInstrumentVisitor(instrumentWritefile);
			ifvisitor.traverse(cu);
			if(ifvisitor.IsSuccess()){
				Utils.writeFile(file, cu.toString(), false);
			}
		}
	}
	private static void backupFile(String filename){
		//System.out.println("--------------  backup all .java file  -------------------");
		ExecuteCommand.backupFile(filename);
	}
	private static void defects4jCompile(String projectPath){
		try {
			ExecuteCommand.defects4jCompile(projectPath);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void runFailTest(String project, int i, String projectPath, String failTestFile) {
		try {
			List<String> failTestList = ParseFile.getFailTestlist(failTestFile);
			for (String failTest: failTestList ){
				System.out.println("--------------  run fail test " + failTest +  " -------------------");
				ExecuteCommand.runFailTest(project, i, projectPath, failTest);
			}		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static void recoverFile(String project ,int i){
		String prePath = Constant.PROJECT_PRE_PATH + project + "/" + project + "_" + i+ "_";
		D4jInfo d4j = new Util.D4jInfo(project,i,prePath);	
		
		String sourceFilePath = d4j.getProSrc();

		List<String> filelist = new ArrayList<String>();
		filelist = Utils.getAllFiles(filelist, ".java", sourceFilePath);
		
		System.out.println(filelist.size());
		for(String file: filelist){
		ExecuteCommand.recoverFile(file);
		}
	}
	public static void switching(String project, int i, ExecutorService executor) {
		String prePath = Constant.PROJECT_PRE_PATH + project + "/" + project + "_" + i+ "_";
		D4jInfo d4j = new Util.D4jInfo(project,i,prePath);	
		
		String sourceFilePath = d4j.getProSrc();
		String writefile = Constant.SUSPICIOUS_FILE + project + "/" +  i+ ".txt";
		SwitchingProcess oneProcess = new SwitchingProcess(project ,i, sourceFilePath);
		try {
			if(oneProcess.process(executor)){
				//System.out.println("process complete");
				HashMap<String, Integer> susipiciousList = oneProcess.getSuspicious();
				System.out.println(susipiciousList.entrySet().size());
				String content = "";
				for(Entry<String, Integer> entry: susipiciousList.entrySet()){
					String stmt = entry.getKey();
					int count = entry.getValue();
					content = content + stmt + ":" + count + "\n";
				}
				System.out.println(content);
				if(! content.equals("")){
					System.out.println("write to file ");
					Utils.writeFile(writefile, content, false);
				}
			}
		} catch (ClassNotFoundException | IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
