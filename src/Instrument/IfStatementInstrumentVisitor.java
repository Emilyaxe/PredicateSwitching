package Instrument;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.AssertStatement;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;

import Util.Utils;


public class IfStatementInstrumentVisitor extends TraversalVisitor{

	private String _clazzName = "";
	private String _clazzFileName = "";
	private CompilationUnit _cu;
	private MethodDeclaration _methodNode;
	private boolean isSuccess = true;
	private HashMap<MethodDeclaration, List<Integer>> methodCounterMap = new HashMap<MethodDeclaration, List<Integer>>();
	
	private String instrumentWritefile = "";
	
	public IfStatementInstrumentVisitor(){
		
	}
	
	public IfStatementInstrumentVisitor(String writefile){
		instrumentWritefile = writefile;
	}
	public boolean IsSuccess(){
		return this.isSuccess;
	}
	@Override
	public boolean visit(CompilationUnit node) {
		// TODO Auto-generated method stub
		if(node.getPackage() == null){
			System.out.println("couldn't find package!");    
			this.isSuccess = false;
			return false;
		}
		_clazzName = node.getPackage().getName().getFullyQualifiedName();
		_clazzFileName = _clazzName;
		_cu = node;
		return true;
	}
	

	
	@Override
	public boolean visit(TypeDeclaration node) {
		// TODO Auto-generated method stub
		if (_clazzName.equals(_clazzFileName) ){
			_clazzName += '.' + node.getName().getFullyQualifiedName();
		}
		return true;
	}


	@Override
	public boolean visit(MethodDeclaration node) {
		// TODO Auto-generated method stub
		
		_methodNode = node;
		Block methodBody = node.getBody();

		if (methodBody == null) {
			return true;
		}

	

		List<Statement> blockStatement = new ArrayList<>();

		for (Object object : methodBody.statements()) {
			if (object instanceof Statement) {
				Statement statement = (Statement) object;
				blockStatement.addAll(processStatement(statement));
			} 
		}
		methodBody.statements().clear();
		
		if (node.isConstructor()){
			
			if(blockStatement.size() > 0 && ( blockStatement.get(0) instanceof SuperConstructorInvocation || 
					blockStatement.get(0) instanceof ConstructorInvocation)){
				methodBody.statements().add(ASTNode.copySubtree(methodBody.getAST(), blockStatement.get(0)));
				blockStatement.remove(0);
			}
		}
		for(Entry<MethodDeclaration, List<Integer>> entry: methodCounterMap.entrySet()){
			if(entry.getKey().equals(_methodNode)){
				for(int lineNumber :entry.getValue()){
					Statement counterStatement = GenStatement.genCounterASTNode(lineNumber);
					methodBody.statements().add(ASTNode.copySubtree(methodBody.getAST(), counterStatement));
				}
			}
		}
		
		for (Statement statement : blockStatement) {
			methodBody.statements().add(ASTNode.copySubtree(methodBody.getAST(), statement));
		}

		return true;
	}

	private List<Statement> processStatement( Statement statement) {
		// TODO Auto-generated method stub
		List<Statement> result = new ArrayList<>();
		if(statement instanceof IfStatement){
			IfStatement ifStatement = (IfStatement) statement;
			result.addAll(processIf(ifStatement));			
		}else if (statement instanceof WhileStatement){
			WhileStatement whileStatement = (WhileStatement) statement;
			Statement whilebody = whileStatement.getBody();
			if (whilebody != null) {
				Block whileBlock = null;
				if (whilebody instanceof Block) {
					whileBlock = (Block) whilebody;
				} else {
					AST ast = AST.newAST(AST.JLS8);
					whileBlock = ast.newBlock();
					whileBlock.statements().add(ASTNode.copySubtree(whileBlock.getAST(), whilebody));
				}
				Block newWhileBlock = processBlock(whileBlock, "", 0, false);
				whileStatement.setBody((Statement) ASTNode.copySubtree(whileStatement.getAST(), newWhileBlock));
			}

			result.add(whileStatement);
		}else if(statement instanceof ForStatement){
			ForStatement forStatement = (ForStatement) statement;
			Statement forBody = forStatement.getBody();
			if (forBody != null) {
				Block forBlock = null;
				if (forBody instanceof Block) {
					forBlock = (Block) forBody;
				} else {
					AST ast = AST.newAST(AST.JLS8);
					forBlock = ast.newBlock();
					forBlock.statements().add(ASTNode.copySubtree(forBlock.getAST(), forBody));
				}

				Block newForBlock = processBlock( forBlock, "", 0, false);
				forStatement.setBody((Statement) ASTNode.copySubtree(forStatement.getAST(), newForBlock));
			}

			result.add(forStatement);
		} else if (statement instanceof DoStatement) {

			DoStatement doStatement = (DoStatement) statement;
			Statement doBody = doStatement.getBody();
			if (doBody != null) {
				Block doBlock = null;
				if (doBody instanceof Block) {
					doBlock = (Block) doBody;
				} else {
					AST ast = AST.newAST(AST.JLS8);
					doBlock = ast.newBlock();
					doBlock.statements().add(ASTNode.copySubtree(doBlock.getAST(), doBody));
				}

				Block newDoBlock = processBlock( doBlock, "", 0, false);
				doStatement.setBody((Statement) ASTNode.copySubtree(doStatement.getAST(), newDoBlock));
			}

			result.add(doStatement);
		} else if (statement instanceof Block) {
			Block block = (Block) statement;
			Block newBlock = processBlock( block, "", 0, false);
			result.add(newBlock);
		} else if (statement instanceof EnhancedForStatement) {

			EnhancedForStatement enhancedForStatement = (EnhancedForStatement) statement;
			Statement enhancedBody = enhancedForStatement.getBody();
			if (enhancedBody != null) {
				Block enhancedBlock = null;
				if (enhancedBody instanceof Block) {
					enhancedBlock = (Block) enhancedBody;
				} else {
					AST ast = AST.newAST(AST.JLS8);
					enhancedBlock = ast.newBlock();
					enhancedBlock.statements().add(ASTNode.copySubtree(enhancedBlock.getAST(), enhancedBody));
				}


				Block newEnhancedBlock = processBlock( enhancedBlock, "", 0, false);
				enhancedForStatement
						.setBody((Statement) ASTNode.copySubtree(enhancedForStatement.getAST(), newEnhancedBlock));
			}

			result.add(enhancedForStatement);
		} else if (statement instanceof SwitchStatement) {

			SwitchStatement switchStatement = (SwitchStatement) statement;
			List<ASTNode> statements = new ArrayList<>();
			AST ast = AST.newAST(AST.JLS8);
			for (Object object : switchStatement.statements()) {
				ASTNode astNode = (ASTNode) object;
				statements.add(ASTNode.copySubtree(ast, astNode));
			}

			switchStatement.statements().clear();

			for (ASTNode astNode : statements) {
				if (astNode instanceof Statement) {
					Statement s = (Statement) astNode;
					for (Statement statement2 : processStatement( s)) {
						switchStatement.statements().add(ASTNode.copySubtree(switchStatement.getAST(), statement2));
					}
				} else {
					switchStatement.statements().add(ASTNode.copySubtree(switchStatement.getAST(), astNode));
				}
			}
			result.add(switchStatement);
		} else if (statement instanceof TryStatement) {

			TryStatement tryStatement = (TryStatement) statement;

			Block tryBlock = tryStatement.getBody();
			if (tryBlock != null) {
				Block newTryBlock = processBlock( tryBlock, "", 0, false);
				tryStatement.setBody((Block) ASTNode.copySubtree(tryStatement.getAST(), newTryBlock));
			}

			List catchList = tryStatement.catchClauses();
			if(catchList != null){
				for (Object object : catchList) {
					if (object instanceof CatchClause) {
						CatchClause catchClause = (CatchClause) object;
						Block catchBlock = catchClause.getBody();
						Block newCatchBlock = processBlock(catchBlock,"", 0, false);
						catchClause.setBody((Block) ASTNode.copySubtree(catchClause.getAST(), newCatchBlock));
					}
				}
			}

			Block finallyBlock = tryStatement.getFinally();
			if (finallyBlock != null) {
				Block newFinallyBlock = processBlock( finallyBlock, "", 0, false);
				tryStatement.setFinally((Block) ASTNode.copySubtree(tryStatement.getAST(), newFinallyBlock));
			}

			result.add(tryStatement);
		} else{
		
			Statement copy = (Statement) ASTNode.copySubtree(AST.newAST(AST.JLS8), statement);
		
			if (statement instanceof ConstructorInvocation) {
				result.add(copy);
		
			} else if (statement instanceof ContinueStatement || statement instanceof BreakStatement
					|| statement instanceof ReturnStatement || statement instanceof ThrowStatement
					|| statement instanceof AssertStatement || statement instanceof ExpressionStatement
					|| statement instanceof VariableDeclarationStatement) {
				
				result.add(copy);

			} else if (statement instanceof LabeledStatement) {
				result.add(copy);
			} else if (statement instanceof SynchronizedStatement) {
				result.add(copy);
			} else {
				result.add(copy);
			}
		}
		return result;
	}

	public List<Statement> processIf (IfStatement node) {
		// TODO Auto-generated method stub

		List<Statement> result = new ArrayList();
		String message =  _clazzName ;
		
		int lineNumber = _cu.getLineNumber(node.getExpression().getStartPosition());
			
	    if(methodCounterMap.containsKey(_methodNode)){
	    	methodCounterMap.get(_methodNode).add(lineNumber);
	    }else{
	    	List<Integer> lineList = new ArrayList<Integer>();
	    	lineList.add(lineNumber);
	    	methodCounterMap.put(_methodNode, lineList);
	    }
	    
		Statement thenBody = node.getThenStatement();
		
		if (thenBody != null){
			Block thenBlock = null;
			if (thenBody instanceof Block){
				thenBlock = (Block) thenBody;
			}else{
				AST ast = AST.newAST(AST.JLS8);
				thenBlock = ast.newBlock();
				thenBlock .statements().add(ASTNode.copySubtree(thenBlock.getAST(), thenBody));
			}
			message =  message + " thenblock ";
			
			Block newThenBlock = processBlock( thenBlock, message, lineNumber, true);
			node.setThenStatement((Statement)ASTNode.copySubtree(node.getAST(), newThenBlock));
		}
		
		Statement elseBody = node.getElseStatement();
		if (elseBody != null) {
			Block elseBlock = null;
			if (elseBody instanceof Block) {
				elseBlock = (Block) elseBody;
			} else {
				AST ast = AST.newAST(AST.JLS8);
				elseBlock = ast.newBlock();
				elseBlock.statements().add(ASTNode.copySubtree(elseBlock.getAST(), elseBody));
			}
			message =  _clazzName + " elseblock ";
			Block newElseBlock = processBlock(elseBlock, message, lineNumber, true);
			node.setElseStatement((Statement) ASTNode.copySubtree(node.getAST(), newElseBlock));
		}
		result.add(node);  
		return result;
	}


	private Block processBlock(Block block, String message, int lineNumber, boolean isIf) {
		Block newBlock = AST.newAST(AST.JLS8).newBlock();
		if (block == null) {
			return newBlock;
		}
		if(isIf){
			List<Statement> insert = GenStatement.genASTNode(message, lineNumber, this.instrumentWritefile);
			for(Statement stmt: insert){
				newBlock.statements().add(ASTNode.copySubtree(newBlock.getAST(), stmt));
			}
		}

		for (Object object : block.statements()) {
			if (object instanceof Statement) {
				Statement statement = (Statement) object;
				List<Statement> newStatements = processStatement( statement);
				for (Statement newStatement : newStatements) {
					newBlock.statements().add(ASTNode.copySubtree(newBlock.getAST(), newStatement));		
				}
			} else {
				System.out.println("processBlock error!");
			}
		}
		return newBlock;
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
				if ( (fileT.getName().contains(suffixs))) {
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

public static void main(String arg[]){

}


}

