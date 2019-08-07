package Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

public class ParseTraceFile {
	
	private HashMap<String, List<String>> testTraceMap = new HashMap<String, List<String>>();
	private String content;
	public ParseTraceFile(){
		
	}
	public ParseTraceFile(String content){
		this.content = content;
	}
	public HashMap<String, List<String>> getTestTraceMap(){
		return this.testTraceMap;
	}
	public void processTraceFile(){
		
		String perTestlist[] = this.content.trim().split("Failure\\.");
		for(String perTest: perTestlist){
			if(perTest.trim().equals(""))
				break;
			//System.out.println(perTest);
			String trace = perTest.split("Trace:")[0].trim();
			if(trace.equals("")){
				break;
			}
			//System.out.println(perTest);
			String testcaseName = perTest.split("TestHeader:")[1].split("RunTime:")[0].trim();
		    testcaseName = processTestCaseName(testcaseName);
			List<String> traceList = processTrace(trace);
			this.testTraceMap.put(testcaseName, traceList);
		}
	}
	private String processTestCaseName(String testcaseName){
		// testBug2849731_2(org.jfree.data.general.junit.DatasetUtilitiesTests)  ==>
		// org.jfree.data.general.junit.DatasetUtilitiesTests::testBug2849731_2
		
		if (testcaseName == "")
			return "";
		String newName = "";
		String methodName = testcaseName.split("\\(")[0];
		String className = testcaseName.split("\\(")[1];
		className = className.substring(0, className.length()-1);
		newName = className + "::" + methodName;
		
		return newName;
	}
	
	private List<String> processTrace(String trace){
		if (trace == "")
			return null ;
		//System.out.println(trace);
		List<String> traceList = new ArrayList<String>();
		List<String> reverseTraceList = new ArrayList<String>();
		for(String oneStmt: trace.split("\n")){
			// org.jfree.chart.util.ResourceBundleWrapper elseblock #122:1
            // org.jfree.chart.util.ResourceBundleWrapper#122:1@elseblock
			
			if (oneStmt == "")
				break;
			String className = oneStmt.split(" ")[0];
			String thenOrelse = oneStmt.split(" ")[1];
			String lineNumber = oneStmt.split(" ")[2];
			String newStmt = className + lineNumber + "@" + thenOrelse;
			if(! traceList.contains(newStmt)){
				traceList.add(newStmt);
			}
			
		}
		for(int i = traceList.size()-1; i >= 0; i--){
			reverseTraceList.add(traceList.get(i));
		}
		return reverseTraceList;
	}
	public static void main(String[] args) {
		
	
		// TODO Auto-generated method stub
		String traceFile = "E:\\28.txt";
		String content = Utils.readFile(traceFile);
		//System.out.println(content);
		ParseTraceFile parse = new ParseTraceFile(content);
		parse.processTraceFile();
		HashMap<String, List<String>> testTraceMap = parse.getTestTraceMap();
		for(Entry<String, List<String>> entry: testTraceMap.entrySet()){
			System.out.println("testcase: " + entry.getKey());
			System.out.println("trace: ");
			for(String line: entry.getValue()){
				System.out.println(line);
			}
		}
		
		
	}

}
