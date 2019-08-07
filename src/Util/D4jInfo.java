package Util;

public class D4jInfo {
	private String project = "";
	private int buggy;
	private  String commonPrePath ="";
	public D4jInfo(){
		
	}
	public D4jInfo(String pro,int n, String prePath){
		this.project = pro;
		this.buggy = n;
		this.commonPrePath = prePath;
		
	}
	public String getProSrc(){
		String src = "";
		   if(project.equals("Chart")){
				src = commonPrePath + "buggy/source/";
			}else if (project.equals("Closure")){
				src = commonPrePath + "buggy/src/";
			}else if (project.equals("Lang") && buggy <=35 ){
				src = commonPrePath + "buggy/src/main/java/";
			}else if (project.equals("Lang") && buggy <= 65){
				src = commonPrePath + "buggy/src/java/";
			}else if (project.equals("Math") && buggy <= 84){
				src = commonPrePath + "buggy/src/main/java/";
			}else if (project.equals("Math")&& buggy <= 106){
				src = commonPrePath + "buggy/src/java/";
			}else if (project.equals("Time")){
				src = commonPrePath + "buggy/src/main/java/";
			}
		return src;
		
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}