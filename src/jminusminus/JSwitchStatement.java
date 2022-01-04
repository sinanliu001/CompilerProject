// Copyright 2012- Bill Campbell, Swami Iyer and Bahar Akbal-Delibas

package jminusminus;

import java.util.ArrayList;
import java.util.TreeMap;

import static jminusminus.CLConstants.*;


/**
 * The AST node for a switch-statement.
 */
public class JSwitchStatement extends JStatement {
    // Test expression.
    private JExpression condition;

    // List of switch-statement groups.
    private ArrayList<SwitchStatementGroup> stmtGroup;

    private ArrayList<Integer> list;

    private LocalContext lContext;

    private int hi, lo, nLabels;

    protected boolean hasbreak;
    protected String breaklabel;

    /**
     * Constructs an AST node for a switch-statement.
     *
     * @param line      line in which the switch-statement occurs in the source file.
     * @param condition test expression.
     * @param stmtGroup list of statement groups.
     */
    public JSwitchStatement(int line, JExpression condition,
                            ArrayList<SwitchStatementGroup> stmtGroup) {
        super(line);
        this.condition = condition;
        this.stmtGroup = stmtGroup;
        this.hasbreak = false;
    }

    public void hasbreak() {hasbreak = true;}
    public String  breaklabeal() {return breaklabel;}

    /**
     * {@inheritDoc}
     */
    public JStatement analyze(Context context) {
        lContext = new LocalContext(context);
        condition = condition.analyze(lContext);
        condition.type().mustMatchExpected(line(), Type.INT);
        JMember.enclosingStatement.push(this);
        list = new ArrayList<Integer>();
        nLabels = 0;
        ArrayList<Integer> list = new ArrayList<Integer>();
        for (SwitchStatementGroup statement : stmtGroup) {
            for (JExpression label: statement.switchLabels){
                if (label!=null){
                    lContext = new LocalContext(context);
                    label.analyze(lContext);
                    label.type().mustMatchExpected(line(), Type.INT);
                    list.add(((JLiteralInt) label).toInt());
                    nLabels++;
                }
            }
            for (JStatement blockmember : statement.block)
            {
                lContext = new LocalContext(context);
                blockmember.analyze(lContext);
            }
        }
        JMember.enclosingStatement.pop();
        for (int i = 0; i < list.size();i++){
            if (i == 0){
                lo = list.get(i);
            } else{
                if (list.get(i) < lo){
                    lo = list.get(i);
                }
            }
        }
        for (int i = 0; i < list.size();i++){
            if (i == 0){
                hi = list.get(i);
            } else{
                if (list.get(i) > hi){
                    hi = list.get(i);
                }
            }
        }
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output) {
        long tableSpaceCost = 5 + hi - lo;
        long tableTimeCost = 3;
        long lookupSpaceCost = 3 + 2 * nLabels;
        long lookupTimeCost = nLabels;
        int opcode = nLabels > 0 && (tableSpaceCost + 3 * tableTimeCost <= lookupSpaceCost + 3 * lookupTimeCost) ?
                TABLESWITCH : LOOKUPSWITCH;
        String out = output.createLabel();
        String def = output.createLabel();
        breaklabel = output.createLabel();
        condition.codegen(output);

        TreeMap<Integer, String> map = new TreeMap<Integer, String>();
        ArrayList<String> caselist = new ArrayList<String>();
        if (opcode == 171){
            int i = 0;
            for (SwitchStatementGroup member: stmtGroup){
                for (JStatement label: member.switchLabels){
                    if (label!=null){
                        String casenumber = output.createLabel();
                        caselist.add(casenumber);
                        int n = ((JLiteralInt)label).toInt();
                        map.put(n,caselist.get(i++));
                    }
                }
            }
            output.addLOOKUPSWITCHInstruction(def, i, map);
        } else {
            for (SwitchStatementGroup member: stmtGroup){
                for (JStatement label: member.switchLabels){
                    if (label!=null){
                        String casenumber = output.createLabel();
                        caselist.add(casenumber);
                    }
                }
            }
            output.addTABLESWITCHInstruction(def, lo, hi, caselist);
        }
        int j = 0;
        boolean check = true;
        for (SwitchStatementGroup member: stmtGroup){
            if (j < caselist.size()){
                output.addLabel(caselist.get(j));
                j++;
                for (JStatement blockmember: member.block){
                    blockmember.codegen(output);
                }
            } else {
                check = false;
                output.addLabel(def);
                for (JStatement blockmember: member.block){
                    blockmember.codegen(output);
                }
            }
        }
        if (check) {
            output.addLabel(def);
        }
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
        json.addChild("JSwitchStatement:" + line, e);
        JSONElement e1 = new JSONElement();
        e.addChild("Condition", e1);
        condition.toJSON(e1);
        for (SwitchStatementGroup group : stmtGroup) {
            group.toJSON(e);
        }
    }
}

/**
 * A switch statement group consists of case labels and a block of statements.
 */
class SwitchStatementGroup {
    // Case labels.
    protected ArrayList<JExpression> switchLabels;

    // Block of statements.
    protected ArrayList<JStatement> block;

    /**
     * Constructs a switch-statement group.
     *
     * @param switchLabels case labels.
     * @param block        block of statements.
     */
    public SwitchStatementGroup(ArrayList<JExpression> switchLabels, ArrayList<JStatement> block) {
        this.switchLabels = switchLabels;
        this.block = block;
    }

    /**
     * Stores information about this switch statement group in JSON format.
     *
     * @param json the JSON emitter.
     */
    public void toJSON(JSONElement json) {
        JSONElement e = new JSONElement();
        json.addChild("SwitchStatementGroup", e);
        for (JExpression label : switchLabels) {
            JSONElement e1 = new JSONElement();
            if (label != null) {
                e.addChild("Case", e1);
                label.toJSON(e1);
            } else {
                e.addChild("Default", e1);
            }
        }
        if (block != null) {
            for (JStatement stmt : block) {
                stmt.toJSON(e);
            }
        }
    }
}
