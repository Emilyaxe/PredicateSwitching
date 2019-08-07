package Main;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.core.dom.CompilationUnit;

import Instrument.SwitchingInstrumentVisitor;
import Util.Constant;
import Util.ExecuteCommand;
import Util.ParseTFwoTC;
import Util.ParseTraceFile;
import Util.Utils;

public class SwitchingProcess {
	private String project = "";
	private int buggyId = 0;
	private String sourceFilePath = "";
	private String projectPath = "";
	private String failTestFile = "";
	private String traceFile = "";
	/*
	 * for without test case
	 */
	// private HashMap<String, List<String>> testTraceMap = new HashMap<String,
	// List<String>>();
	private List<String> testTraceList = new ArrayList<String>();
	private List<String> slicingList = new ArrayList<String>();
	private HashMap<String, Integer> suspiciousMap = new HashMap<String, Integer>();

	public SwitchingProcess() {

	}

	public SwitchingProcess(String project, int i, String sourceFilePath) {
		this.project = project;
		this.buggyId = i;
		this.sourceFilePath = sourceFilePath; // buggy/source/
		this.projectPath = Constant.PROJECT_PRE_PATH + project + "/" + project
				+ "_" + buggyId + "_buggy";
		this.failTestFile = Constant.FAIL_TEST_FILE + project + "/" + buggyId
				+ ".txt";
		this.traceFile = Constant.IF_TRACE_FILE + project + "/" + buggyId
				+ ".txt";
	}

	private boolean getTestResult() {
		String content = Utils.readFile(traceFile);
		if (content.equals("")) {
			return false;
		}
		// System.out.println(content);
		/*
		 * for without testcase ParseTraceFile parse = new
		 * ParseTraceFile(content); parse.processTraceFile(); testTraceMap =
		 * parse.getTestTraceMap();
		 */
		ParseTFwoTC parse = new ParseTFwoTC(content);
		parse.processTraceFile();
		testTraceList = parse.getTestTraceList();
		return true;
	}

	private String findFile(String line) {

		// /home/emily/WorkSpace/Data/Defects4J_Copy/projects/"
		// + project + "/" + project + "_" + i + "_buggy/source";
		// line : org.jfree.data.general.DatasetUtilities#1283:1@thenblock
		String filename = sourceFilePath;
		line = line.split("#")[0].replace(".", "/");
		filename = filename + "/" + line + ".java";

		return filename;
	}

	private List<String> getFailTestlist() {
		List<String> failTestList = new ArrayList<String>();
		String content = Utils.readFile(failTestFile);
		for (String line : content.split("\n")) {
			if (line.trim() == "")
				break;
			failTestList.add(line);
		}
		return failTestList;
	}

	public HashMap<String, Integer> getSuspicious() {
		return this.suspiciousMap;
	}

	// process trace if without testccase
	public boolean process(ExecutorService executor) throws IOException,
			InterruptedException, ClassNotFoundException {
		// init TraceHashMap<testcase, iflist> and init slicingList

		if (!getTestResult()) {
			return false;
		}

		Timestamp startTime = new Timestamp(System.currentTimeMillis());
		// testcaseListCount++;
		// line:org.jfree.chart.util.ResourceBundleWrapper#122:1@elseblock
		int countline = testTraceList.size();
		for (int i = 1; i <= countline; i++) {
			String line = testTraceList.get(i - 1);

			Timestamp nowTime = new Timestamp(System.currentTimeMillis());
			long runtime = nowTime.getTime() - startTime.getTime();
			if (runtime >= 1800000) {
				System.out.println("time out! " + runtime);
				break;
			}
			System.out.println("-------------  test " + i + "/" + countline
					+ "   " + line + "  ---------------------");

			if (this.suspiciousMap.containsKey(line.split(":")[0]))
				continue;

			String filePath = findFile(line);
			System.out
					.println("-------------  copy file  ---------------------");
			ExecuteCommand.backupFile(filePath);// copy filePath to filePath.bak

			System.out
					.println("-------------  Switching  ---------------------");
			CompilationUnit cu = Utils.constuctCompilationUnit(filePath);
			SwitchingInstrumentVisitor switchingvisitor = new SwitchingInstrumentVisitor(
					line);
			switchingvisitor.traverse(cu);
			if (switchingvisitor.isSuccessful()) {
				Utils.writeFile(filePath, cu.toString(), false);
				// System.out.println(cu.toString());
			} else {
				continue;
			}
			System.out.println("-------------  compile  ---------------------");
			ExecuteCommand.defects4jCompile(projectPath); // compile
			List<String> failTestList = this.getFailTestlist();
			int count = 0;
			int failTestCount = 1;
 
			for (String failTest : failTestList) {

				System.out
						.println("-------------  test " + failTestCount + "  ---------------------" );
				failTestCount++;
				// maybe get into timeout
				String result = "";
				RunFailTestTask task = new RunFailTestTask(this.project, this.buggyId, projectPath, failTest);
				Future<?> future = executor.submit(task);
				try {
					result = (String) future.get(60, TimeUnit.SECONDS);
					/*
					if (executor.awaitTermination(60, TimeUnit.SECONDS)) {
						System.out.println("task finished");
					} else {
						System.out.println("task time out,will terminate");					
						
							if (!future.isDone()) {
								future.cancel(true);
							}						
					}
					*/
				} catch (InterruptedException e) {
					System.out.println(failTest + " executor is interrupted");
					future.cancel(true);
				} catch (TimeoutException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					System.out.print("task time out");
					future.cancel(true);
				} catch (ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					future.cancel(true);
				} finally {
					
				}

				System.out.println(result);
				if (result.equals("Success.")) {
					System.out
							.println("-------------  test success  ---------------------");
					count++;
				} else {
					System.out
							.println("-------------  test failure ---------------------");
				}
			}
			if (count != 0) {
				line = line.split(":")[0];
				this.suspiciousMap.put(line, count);
			}

			System.out.println("-------------  delete  ---------------------");
			ExecuteCommand.recoverFile(filePath);
		}
		System.out.println("proccess Complete");
		return true;

	}

	static class RunFailTestTask implements Callable<String> {
		private String project = "";
		private int buggyId = 0;
		private String projectPath = "";
		private String failTest = "";

		public RunFailTestTask(String project, int buggyId, String projectPath,
				String failTest) {
			this.project = project;
			this.buggyId = buggyId;
			this.projectPath = projectPath;
			this.failTest = failTest;
		}

		public String runFailTest() throws IOException, InterruptedException {

			String result = "";
			result = ExecuteCommand.runFailTestforSwitching(this.project,
					this.buggyId, projectPath, failTest).trim();

			//System.out.println("task " + failTest + " finished successfully");
			return result;
		}

		@Override
		public String call() throws Exception {
			// TODO Auto-generated method stub

			return runFailTest() + "";
		}
	}

	// process trace if with testcase
	/*
	 * public boolean process() throws IOException, InterruptedException,
	 * ClassNotFoundException{ // init TraceHashMap<testcase, iflist> and init
	 * slicingList
	 * 
	 * if(! getTestResult()){ return false; }
	 * 
	 * int testcaseListCount = 0; int totalListCount =
	 * testTraceMap.entrySet().size(); for(Entry<String, List<String>> entry:
	 * testTraceMap.entrySet()){ Timestamp startTime = new
	 * Timestamp(System.currentTimeMillis()); testcaseListCount++; //
	 * line:org.jfree.chart.util.ResourceBundleWrapper#122:1@elseblock int i =
	 * 1; int countline = entry.getValue().size(); for(String line:
	 * entry.getValue()){
	 * 
	 * Timestamp nowTime = new Timestamp(System.currentTimeMillis()); long
	 * runtime = nowTime.getTime() - startTime.getTime(); if(runtime >= 300000){
	 * System.out.println("time out! " + runtime ); break; }
	 * System.out.println("-------------  test " + testcaseListCount + "/" +
	 * totalListCount + " - " + i + " " + line + "  ---------------------");
	 * i++;
	 * 
	 * if (this.suspiciousMap.containsKey(line.split(":")[0])) continue;
	 * 
	 * String filePath = findFile(line);
	 * System.out.println("-------------  copy file  ---------------------");
	 * ExecuteCommand.backupFile(filePath);// copy filePath to filePath.bak
	 * 
	 * System.out.println("-------------  Switching  ---------------------");
	 * CompilationUnit cu = Utils.constuctCompilationUnit(filePath);
	 * SwitchingInstrumentVisitor switchingvisitor = new
	 * SwitchingInstrumentVisitor(line); switchingvisitor.traverse(cu);
	 * if(switchingvisitor.isSuccessful()){ Utils.writeFile(filePath,
	 * cu.toString(), false); //System.out.println(cu.toString()); }else{
	 * continue; }
	 * System.out.println("-------------  compile  ---------------------");
	 * ExecuteCommand.defects4jCompile(projectPath); // compile List<String>
	 * failTestList = this.getFailTestlist(); int count = 0;
	 * 
	 * for(String failTest: failTestList){
	 * 
	 * // System.out.println("-------------  test  ---------------------");
	 * //FutureTaskTime futureTaskTime = new FutureTaskTime(failTest,
	 * projectPath, project, buggyId); //CmdLine.runFailTest(failTest,
	 * projectPath, project, buggyId+"") String result =
	 * ExecuteCommand.runFailTestforSwitching(this.project,this.buggyId,
	 * projectPath, failTest).trim(); System.out.println(result);
	 * if(result.equals("Success.")){
	 * System.out.println("-------------  test success  ---------------------");
	 * count++; }else{
	 * System.out.println("-------------  test failure ---------------------");
	 * } } if(count != 0){ line = line.split(":")[0];
	 * this.suspiciousMap.put(line, count); }
	 * 
	 * System.out.println("-------------  delete  ---------------------");
	 * ExecuteCommand.recoverFile(filePath); }
	 * System.out.println("one test case list complete");
	 * 
	 * } System.out.println("proccess preComplete"); return true;
	 * 
	 * }
	 */
}
