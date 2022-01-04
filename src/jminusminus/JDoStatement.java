// Copyright 2012- Bill Campbell, Swami Iyer and Bahar Akbal-Delibas

package jminusminus;

import static jminusminus.CLConstants.*;

/**
 * The AST node for a do-statement.
 */
public class JDoStatement extends JStatement {
    // Body.
    private JStatement body;

    // Test expression.
    private JExpression condition;
    // break statement use
    private boolean hasbreak;
    private String breaklabel;

    private boolean hasContinue;
    private String continueLabel;



    /**
     * Constructs an AST node for a do-statement.
     *
     * @param line      line in which the do-statement occurs in the source file.
     * @param body      the body.
     * @param condition test expression.
     */
    public JDoStatement(int line, JStatement body, JExpression condition) {
        super(line);
        this.body = body;
        this.condition = condition;
    }

    public void hasbreak() {hasbreak = true;}
    public String  breaklabeal() {return breaklabel;}

    public void hasContinue(){this.hasContinue = true;}
    public String continueLabel() {return continueLabel;}
    /**
     * {@inheritDoc}
     */
    public JStatement analyze(Context context) {
        JMember.enclosingStatement.push(this);
        condition = condition.analyze(context);
        condition.type().mustMatchExpected(line(), Type.BOOLEAN);
        body = (JStatement) body.analyze(context);
        JMember.enclosingStatement.pop();
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output) {
        String test = output.createLabel();
        String main = output.createLabel();
        String out = output.createLabel();
        breaklabel = output.createLabel();
        continueLabel = output.createLabel();
        output.addLabel(main);
        if (hasContinue){
            output.addLabel(continueLabel);
        }
        body.codegen(output);
        output.addLabel(test);
        condition.codegen(output, out, false);
        output.addBranchInstruction(GOTO, main);
        if (hasbreak){
            output.addLabel(breaklabel);
        }
        output.addLabel(out);
    }

    /**
     * {@inheritDoc}
     */
    public void toJSON(JSONElement json) {
        JSONElement e = new JSONElement();
        json.addChild("JDoStatement:" + line, e);
        JSONElement e1 = new JSONElement();
        e.addChild("Body", e1);
        body.toJSON(e1);
        JSONElement e2 = new JSONElement();
        e.addChild("Condition", e2);
        condition.toJSON(e2);
    }
}
