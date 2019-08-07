package Instrument;


import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;


public class GenStatement {
	
	private static AST ast = AST.newAST(AST.JLS8);


	public static List<Statement> genASTNode(String message, int lineNumber, String writefile) {
		// TODO Auto-generated method stub
		List<Statement> statements = new ArrayList<Statement>();
		StringLiteral stringLiteral = ast.newStringLiteral();
		stringLiteral.setLiteralValue(message + "#" + lineNumber + ":");
		
		InfixExpression infixExp = ast.newInfixExpression();
		infixExp.setOperator(Operator.PLUS);
		infixExp.setLeftOperand(stringLiteral);
		infixExp.setRightOperand(ast.newName("count" + lineNumber));
	
		StringLiteral writefileLiteral = ast.newStringLiteral();
		writefileLiteral.setLiteralValue(writefile);

		InfixExpression rightInfix = ast.newInfixExpression();
		rightInfix.setOperator(Operator.PLUS);
		rightInfix.setLeftOperand(ast.newSimpleName("count" + lineNumber));
		rightInfix.setRightOperand(ast.newNumberLiteral("1"));
		
		Assignment assignment = ast.newAssignment();
        assignment.setLeftHandSide(ast.newSimpleName("count" + lineNumber));
        assignment.setOperator(org.eclipse.jdt.core.dom.Assignment.Operator.ASSIGN);
        assignment.setRightHandSide(rightInfix);

		
		ExpressionStatement expStatement = ast.newExpressionStatement(assignment);
		statements.add(expStatement);
		statements.add(genPrinter(infixExp,writefileLiteral)) ;
		return statements;
		
	}


	private static Statement genPrinter(Expression expression, Expression writefileExpression) {
		// TODO Auto-generated method stub
		//auxiliary.Dumper.write(expression);
		MethodInvocation methodInvocation = ast.newMethodInvocation();
		methodInvocation.setExpression(ast.newName("auxiliary.Dumper"));
		methodInvocation.setName(ast.newSimpleName("write"));
		methodInvocation.arguments().add(writefileExpression);
		methodInvocation.arguments().add(expression);
		ExpressionStatement expressionStatement = ast.newExpressionStatement(methodInvocation);
		return expressionStatement;
	}


	public static Statement genCounterASTNode( int lineNumber) {
		// TODO Auto-generated method stub
		// int countlingNumber = 0;

		VariableDeclarationFragment vdf = ast.newVariableDeclarationFragment();
		vdf.setName(ast.newSimpleName("count" + lineNumber));	
		NumberLiteral number = ast.newNumberLiteral("0");   // ifStatementInstrument.visitor = 0 ; SwitchingInstrumentVisitor = 1
		vdf.setInitializer(number);
		
		VariableDeclarationStatement vds = ast.newVariableDeclarationStatement(vdf);
		vds.setType(ast.newPrimitiveType(PrimitiveType.INT));
		return vds;
	}


	public static Statement genCounterASTNodeforSwitching(int lineNumber) {
		// TODO Auto-generated method stub
		VariableDeclarationFragment vdf = ast.newVariableDeclarationFragment();
		vdf.setName(ast.newSimpleName("count" + lineNumber));	
		NumberLiteral number = ast.newNumberLiteral("1");   // ifStatementInstrument.visitor = 0 ; SwitchingInstrumentVisitor = 1
		vdf.setInitializer(number);
		
		VariableDeclarationStatement vds = ast.newVariableDeclarationStatement(vdf);
		vds.setType(ast.newPrimitiveType(PrimitiveType.INT));
		return vds;
	}


}