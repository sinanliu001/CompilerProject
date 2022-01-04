// Copyright 2012- Bill Campbell, Swami Iyer and Bahar Akbal-Delibas

package jminusminus;

import static jminusminus.CLConstants.*;
import static jminusminus.JForStatement.*;
/**
 * An AST node for a continue-statement.
 */
public class JContinueStatement extends JStatement {
    /**
     * Constructs an AST node for a continue-statement.
     *
     * @param line line in which the continue-statement occurs in the source file.
     */

    private JStatement enclosingStatement;
    public JContinueStatement(int line) {
        super(line);
    }

    /**
     * {@inheritDoc}
     */
    public JStatement analyze(Context context) {
        enclosingStatement = JMember.enclosingStatement.peek();
        try {
            (((JForStatement) enclosingStatement)).hasContinue();
        } catch(ClassCastException e) {}
        try {
            (((JDoStatement) enclosingStatement)).hasContinue();
        } catch(ClassCastException e){}
        try {
            (((JWhileStatement) enclosingStatement)).hasContinue();
        } catch(ClassCastException e){}

        return this;
    }

    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output)
    {
        String continuelabel = null;
        try{
            continuelabel = ((JForStatement) enclosingStatement).continueLabel();
        } catch(ClassCastException e) {}
        try{
            continuelabel = ((JDoStatement) enclosingStatement).continueLabel();
        } catch(ClassCastException e) {}
        try{
            continuelabel = ((JWhileStatement) enclosingStatement).continueLabel();
        } catch(ClassCastException e) {}
        output.addBranchInstruction(GOTO, continuelabel);
    }

    /**
     * {@inheritDoc}
     */
    public void toJSON(JSONElement json) {
        JSONElement e = new JSONElement();
        json.addChild("JContinueStatement:" + line, e);
    }
}
