// Copyright 2012- Bill Campbell, Swami Iyer and Bahar Akbal-Delibas

package jminusminus;

import java.util.ArrayList;

import static jminusminus.CLConstants.*;

/**
 * The AST node for a for-statement.
 */
class JForStatement extends JStatement {
    // Initialization.
    private ArrayList<JStatement> init;

    // Test expression
    private JExpression condition;

    // Update.
    private ArrayList<JStatement> update;

    // The body.
    private JStatement body;

    private LocalContext localContext;

    private boolean hasContinue;
    private String continueLabel = "c";

    private boolean hasbreak;
    private String breaklabel;

    /**
     * Constructs an AST node for a for-statement.
     *
     * @param line      line in which the for-statement occurs in the source file.
     * @param init      the initialization.
     * @param condition the test expression.
     * @param update    the update.
     * @param body      the body.
     */
    public JForStatement(int line, ArrayList<JStatement> init, JExpression condition,
                         ArrayList<JStatement> update, JStatement body) {
        super(line);
        this.init = init;
        this.condition = condition;
        this.update = update;
        this.body = body;
        this.hasContinue = false;
        this.hasbreak = false;
    }

    /**
     * {@inheritDoc}
     */
    public void hasbreak() {this.hasbreak = true;}
    public String  breaklabeal() {return breaklabel;}

    public void hasContinue(){this.hasContinue = true;}
    public String continueLabel() {return continueLabel;}

    public JForStatement analyze(Context context) {
        localContext = new LocalContext(context);
        JMember.enclosingStatement.push(this);
        if (init != null) {
            for (JStatement i:init){
                i.analyze(localContext);
            }
        }
        if (condition!=null){
            condition.analyze(localContext);
            condition.type().mustMatchExpected(line(), Type.BOOLEAN);
        }
        if (update!=null){
            for (JStatement updateway: update){
                updateway.analyze(localContext);
            }
        }
        body.analyze(localContext);
        JMember.enclosingStatement.pop();
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output) {
        String test = output.createLabel();
        String out = output.createLabel();
        breaklabel = output.createLabel();
        continueLabel = output.createLabel();
        if (init!=null){
            for (JStatement i: init){
                i.codegen(output);
            }
        }
        output.addLabel(test);
        if (condition!=null){
            condition.codegen(output, out, false);
        }
        body.codegen(output);
        if(hasContinue){
            output.addLabel(continueLabel);
        }
        if (update!=null){
            for (JStatement updateway: update){
                updateway.codegen(output);
            }
        }
        output.addBranchInstruction(GOTO, test);
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
        json.addChild("JForStatement:" + line, e);
        if (init != null) {
            JSONElement e1 = new JSONElement();
            e.addChild("Init", e1);
            for (JStatement stmt : init) {
                stmt.toJSON(e1);
            }
        }
        if (condition != null) {
            JSONElement e1 = new JSONElement();
            e.addChild("Condition", e1);
            condition.toJSON(e1);
        }
        if (update != null) {
            JSONElement e1 = new JSONElement();
            e.addChild("Update", e1);
            for (JStatement stmt : update) {
                stmt.toJSON(e1);
            }
        }
        if (body != null) {
            JSONElement e1 = new JSONElement();
            e.addChild("Body", e1);
            body.toJSON(e1);
        }
    }
}
