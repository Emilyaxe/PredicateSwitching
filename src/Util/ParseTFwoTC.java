package Util;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
public class ParseTFwoTC {
	//private HashMap<String, List<String>> testTraceMap = new HashMap<String, List<String>>();
	private List<String> testTraceList = new ArrayList<String>();
	private String content;
	public ParseTFwoTC(){
		
	}
	public ParseTFwoTC(String content){
		this.content = content;
	}
	public List<String> getTestTraceList(){
		return this.testTraceList;
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
			processTrace(trace);
			//this.testTraceMap.put(testcaseName, traceList);
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
	
	private void processTrace(String trace){
		if (trace == "")
			return ;
		//System.out.println(trace);
		List<String> traceList = new ArrayList<String>();
		//List<String> reverseTraceList = new ArrayList<String>();
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
			if(! this.testTraceList.contains(traceList.get(i))){
				this.testTraceList.add(traceList.get(i));
			}
			
		}
	
	}
}
