// Copyright 2012- Bill Campbell, Swami Iyer and Bahar Akbal-Delibas

package jminusminus;

import static jminusminus.CLConstants.*;

/**
 * An AST node for a break-statement.
 */
public class JBreakStatement extends JStatement {
    /**
     * Constructs an AST node for a break-statement.
     *
     * @param line line in which the break-statement occurs in the source file.
     */

    private JStatement enclosingStatement;
    public JBreakStatement(int line) {
        super(line);
    }

    /**
     * {@inheritDoc}
     */
    public JStatement analyze(Context context) {
        enclosingStatement = JMember.enclosingStatement.peek();
        try {
            (((JForStatement) enclosingStatement)).hasbreak();
        } catch(ClassCastException e) {}
        try {
            (((JSwitchStatement) enclosingStatement)).hasbreak();
        } catch(ClassCastException e){}
        try {
            (((JDoStatement) enclosingStatement)).hasbreak();
        } catch(ClassCastException e){}
        try {
            (((JWhileStatement) enclosingStatement)).hasbreak();
        } catch(ClassCastException e){}

        return this;
    }

    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output) {
        String breaklabel = null;
        try{
            breaklabel = ((JForStatement) enclosingStatement).breaklabeal();
        } catch(ClassCastException e) {}
        try{
            breaklabel = ((JSwitchStatement) enclosingStatement).breaklabeal();
        } catch(ClassCastException e) {}
        try{
            breaklabel = ((JDoStatement) enclosingStatement).breaklabeal();
        } catch(ClassCastException e) {}
        try{
            breaklabel = ((JWhileStatement) enclosingStatement).breaklabeal();
        } catch(ClassCastException e) {}
        output.addBranchInstruction(GOTO, breaklabel);
    }

    /**
     * {@inheritDoc}
     */
    public void toJSON(JSONElement json) {
        JSONElement e = new JSONElement();
        json.addChild("JBreakStatement:" + line, e);
    }
}
