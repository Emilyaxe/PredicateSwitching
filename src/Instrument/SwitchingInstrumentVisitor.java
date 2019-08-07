package Instrument;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AssertStatement;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression.Operator;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;



public class SwitchingInstrumentVisitor extends TraversalVisitor{
	
	private String _clazzName = "";
	private String _clazzFileName = "";
	private MethodDeclaration _methodNode;
	private String _ifStatement = "";
	private CompilationUnit _cu;
	private HashMap<MethodDeclaration, List<Integer>> methodCounterMap = new HashMap<MethodDeclaration, List<Integer>>();
	private boolean isSuccess = true;
	
	public SwitchingInstrumentVisitor(){
		
	}
	public SwitchingInstrumentVisitor(String ifStatement){		
		// line:org.jfree.chart.util.ResourceBundleWrapper#122:1@elseblock
		this._ifStatement = ifStatement;
		
	}
	
	@Override
	public boolean visit(CompilationUnit node) {
		// TODO Auto-generated method stub
		if(node.getPackage() == null){
			System.out.println("couldn't find package!");
			isSuccess = false;
			return false;
		}
		_clazzName = node.getPackage().getName().getFullyQualifiedName();
		_clazzFileName = _clazzName;
		_cu = node;
		return true;
	}
	

	public boolean isSuccessful(){
		return this.isSuccess;
	}
	@Override
	public boolean visit(TypeDeclaration node) {
		// TODO Auto-generated method stub
		if (_clazzName.equals(_clazzFileName)){
			_clazzName += '.' + node.getName().getFullyQualifiedName();
		}
		return true;
	}

	@Override
	public boolean visit(IfStatement node) {
		// TODO Auto-generated method stub
		if(!this.isProcessIf(node))
			return true;
		int lineNumber = _cu.getLineNumber(node.getExpression().getStartPosition());
		MethodDeclaration methodNode = findMethod(node);
		processIf(node);
		
		AST ast = AST.newAST(AST.JLS8);
		String realPath = this._ifStatement.split("@")[1].trim();
		Expression ifExpression = node.getExpression();
		
		InfixExpression expression = ast.newInfixExpression();
		
		String counterTime = this._ifStatement.split(":")[1].split("@")[0];
		
		InfixExpression infixExp = ast.newInfixExpression();
		infixExp.setLeftOperand(ast.newSimpleName("count" + lineNumber));
	//	System.out.println(counterTime);
		infixExp.setRightOperand(ast.newNumberLiteral(counterTime));
		infixExp.setOperator(org.eclipse.jdt.core.dom.InfixExpression.Operator.EQUALS);
		
		ParenthesizedExpression parentExp = ast.newParenthesizedExpression();
		parentExp.setExpression(infixExp);
		
		PrefixExpression prefixExp = ast.newPrefixExpression();
		prefixExp.setOperand(parentExp);
		prefixExp.setOperator(Operator.NOT);
		
		if(realPath.equals("thenblock") ){
		//	System.out.println(realPath);
			
			expression.setLeftOperand((Expression)ASTNode.copySubtree(expression.getAST(),ifExpression));
			expression.setOperator(InfixExpression.Operator.CONDITIONAL_AND);
			expression.setRightOperand((Expression)ASTNode.copySubtree(expression.getAST(),prefixExp));
			
		}else{
            // || true
			expression.setLeftOperand((Expression)ASTNode.copySubtree(expression.getAST(),ifExpression));
			expression.setOperator(InfixExpression.Operator.CONDITIONAL_OR);
			expression.setRightOperand((Expression)ASTNode.copySubtree(expression.getAST(),infixExp));
		}	
		node.setExpression((Expression)ASTNode.copySubtree(node.getAST(), expression));
	    		
		processMethod(methodNode, lineNumber);
		
		return true;
	}
	
	public void processIf (IfStatement node) {
		// TODO Auto-generated method stub

		List<Statement> result = new ArrayList();
		String message =  _clazzName ;
		
		int lineNumber = _cu.getLineNumber(node.getExpression().getStartPosition());
			
	 //   System.out.println(node.toString() + " " + lineNumber);
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
			
			Block newThenBlock = processBlock( thenBlock,  lineNumber, true);
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
			Block newElseBlock = processBlock(elseBlock,  lineNumber, true);
			node.setElseStatement((Statement) ASTNode.copySubtree(node.getAST(), newElseBlock));
		}
		
		//result.add(node);  
		//return result;
	}


	private Block processBlock(Block block,  int lineNumber, boolean isIf) {
		Block newBlock = AST.newAST(AST.JLS8).newBlock();
		if (block == null) {
			return newBlock;
		}
		if(isIf){
			AST ast = AST.newAST(AST.JLS8);
			InfixExpression rightInfix = ast.newInfixExpression();
			rightInfix.setOperator(org.eclipse.jdt.core.dom.InfixExpression.Operator.PLUS);
			rightInfix.setLeftOperand(ast.newSimpleName("count" + lineNumber));
			rightInfix.setRightOperand(ast.newNumberLiteral("1"));
			
			Assignment assignment = ast.newAssignment();
	        assignment.setLeftHandSide(ast.newSimpleName("count" + lineNumber));
	        assignment.setOperator(org.eclipse.jdt.core.dom.Assignment.Operator.ASSIGN);
	        assignment.setRightHandSide(rightInfix);
		
			ExpressionStatement expStatement = ast.newExpressionStatement(assignment);
			newBlock.statements().add(ASTNode.copySubtree(newBlock.getAST(), expStatement));
		}

		for (Object object : block.statements()) {
			if (object instanceof Statement) {
				Statement statement = (Statement) object;
				newBlock.statements().add(ASTNode.copySubtree(newBlock.getAST(), statement));		
				
			} else {
				System.out.println("processBlock error!");
			}
		}
		return newBlock;
	}
	private boolean processMethod(MethodDeclaration methodNode,int lineNumber) {
		// TODO Auto-generated method stub
		_methodNode = methodNode;
	//	System.out.println(methodNode.toString());
		Block methodBody = methodNode.getBody();

		if (methodBody == null) {
			return false;
		}

		List<Statement> blockStatement = new ArrayList<>();

		for (Object object : methodBody.statements()) {
			if (object instanceof Statement) {
				Statement statement = (Statement) object;
				blockStatement.add(statement);
			} 
		}
		methodBody.statements().clear();
		
		if (methodNode.isConstructor()){
			if(blockStatement.size() > 0 && ( blockStatement.get(0) instanceof SuperConstructorInvocation || 
					blockStatement.get(0) instanceof ConstructorInvocation)){
				methodBody.statements().add(ASTNode.copySubtree(methodBody.getAST(), blockStatement.get(0)));
				blockStatement.remove(0);
			}
		}

		Statement counterStatement = GenStatement.genCounterASTNodeforSwitching(lineNumber);

		methodBody.statements().add(ASTNode.copySubtree(methodBody.getAST(), counterStatement));
		
		for (Statement statement : blockStatement) {
			methodBody.statements().add(ASTNode.copySubtree(methodBody.getAST(), statement));
		}

		return true;
	
	}

	private MethodDeclaration findMethod(IfStatement node) {
		// TODO Auto-generated method stub
		//System.out.println(node.toString());
		ASTNode parent = node.getParent();
		MethodDeclaration tmp = null ;
		while(parent != null){
			//System.out.println(parent.toString());
			if(parent instanceof MethodDeclaration){
				//System.out.println(parent.toString());
				 tmp = (MethodDeclaration) parent;
				 break;
			}
			parent = parent.getParent();
		}
		return tmp;
	}
	private boolean isProcessIf(IfStatement node){
		boolean result = false;
		if(node == null)
			return result;
		
		// line:org.jfree.chart.util.ResourceBundleWrapper#122:1@elseblock
		
		int lineNumber = _cu.getLineNumber(node.getExpression().getStartPosition());
		String realIf = this._clazzName + "#" + lineNumber;
		String ifStmt = this._ifStatement.split(":")[0];
		//System.out.println(realIf+ " -- " + ifStmt);
		if(realIf.equals(ifStmt) ){
			result = true;
		}
		return result;
	}



}
