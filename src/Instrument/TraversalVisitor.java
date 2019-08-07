package Instrument;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;

public class TraversalVisitor extends ASTVisitor{

	public boolean traverse(CompilationUnit compilationUnit){
		compilationUnit.accept(this);
		return true;
	}

}
