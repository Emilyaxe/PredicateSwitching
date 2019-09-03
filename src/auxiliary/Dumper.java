package auxiliary;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Dumper {
	

	private String writeContent = "";
	
	Dumper(){
		
	}
	public static void write(String writeFilePath, String message){
		writeFile(writeFilePath, message, true);
	}

	private static void writeFile(String fileName, String content, boolean isAppend) {
		
		try {
			File file = new File(fileName);
			File fileParent = file.getParentFile();
			if(!fileParent.exists()){
				fileParent.mkdirs();
			}
			FileWriter writer = new FileWriter(fileName, isAppend); // append to the file
			writer.write(content + "\n");
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	public static void main(String arg[]){
		 int count95 = 1;
		 auxiliary.Dumper.write("E:\\test.txt","org.jfree.chart.BufferedImageRenderingSource thenblock #95:" + count95);
	}

}
