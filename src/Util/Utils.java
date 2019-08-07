package Util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;


public class Utils {

	public static String readFile(String absolutePath) {
		
		

		File file = new File(absolutePath);
		String content = "";
		if(! file.exists()){
			System.out.println(absolutePath + " doesn't exists !");
			return content;
		}
			
		try {
			FileInputStream in = new FileInputStream(file);
			int size = in.available();
			byte[] buffer = new byte[size];
			in.read(buffer);
			in.close();
			content = new String(buffer, "GB2312");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			return null;
		}
		

		return content;
	}
	public static void writeFile(String fileName, String content, boolean isAppend) {
		
		try {
			File file = new File(fileName);
			File fileParent = file.getParentFile();
			if(!fileParent.exists()){
				fileParent.mkdirs();
			}
			FileWriter writer = new FileWriter(fileName, isAppend); // append to the file
			writer.write(content);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	//obtain all java files in path
	public static List<String> getAllFiles(List<String> filePath,

	String suffixs, String path) {

		File fileT = new File(path);
		if (fileT.exists()) {
			if (fileT.isDirectory() && ! fileT.getName().contains(".svn")) {
				for (File f : fileT.listFiles()) {
					filePath = getAllFiles( filePath,suffixs, f.getAbsolutePath());
				}
			} else {
				if ( (fileT.getName().endsWith(suffixs))) {
				//	System.out.println(fileT.getPath().toString());
					filePath.add(fileT.getPath().toString());
					}
			}
		}else{
			System.out.println(path + " doesn't exist!");
		}
		return filePath;
	}
	
	public static CompilationUnit constuctCompilationUnit(String filePath) {
		String icu = Utils.
				readFile(filePath);
		ASTParser astParser = ASTParser.newParser(AST.JLS8);
		Map<?, ?> options = JavaCore.getOptions();
		JavaCore.setComplianceOptions(JavaCore.VERSION_1_7, options);
		astParser.setCompilerOptions(options);
		astParser.setSource(icu.toCharArray());
		astParser.setKind(ASTParser.K_COMPILATION_UNIT);
		astParser.setResolveBindings(true);
		return (CompilationUnit) astParser.createAST(null);
	}
}
