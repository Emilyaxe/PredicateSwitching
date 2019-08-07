package Util;

import java.util.ArrayList;
import java.util.List;



public class ParseFile {

	public static List<String> getFailTestlist(String failTestFile){
		List<String> failTestList = new ArrayList<String>();
		String content = Utils.readFile(failTestFile);
		for(String line: content.split("\n")){
			if (line.trim() == "")
				break;
			failTestList.add(line);
		}
		return failTestList;
	}
}
