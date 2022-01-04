// Copyright 2012- Bill Campbell, Swami Iyer and Bahar Akbal-Delibas

package jminusminus;

import com.sun.jdi.LocalVariable;

import java.util.ArrayList;

import static jminusminus.CLConstants.*;

/**
 * The AST node for a try-catch-finally statement.
 */
class JTryStatement extends JStatement {
    // The try block.
    private JBlock tryBlock;

    // The catch parameters.
    private ArrayList<JFormalParameter> parameters;

    // The catch blocks.
    private ArrayList<JBlock> catchBlocks;

    // The finally block.
    private JBlock finallyBlock;

    private LocalContext context;

    private ArrayList<JVariable> vars;

    private LocalContext Context;
    /**
     * Constructs an AST node for a try-statement.
     *
     * @param line         line in which the while-statement occurs in the source file.
     * @param tryBlock     the try block.
     * @param parameters   the catch parameters.
     * @param catchBlocks  the catch blocks.
     * @param finallyBlock the finally block.
     */
    public JTryStatement(int line, JBlock tryBlock, ArrayList<JFormalParameter> parameters,
                         ArrayList<JBlock> catchBlocks, JBlock finallyBlock) {
        super(line);
        this.tryBlock = tryBlock;
        this.parameters = parameters;
        this.catchBlocks = catchBlocks;
        this.finallyBlock = finallyBlock;
    }

    /**
     * {@inheritDoc}
     */
    public JTryStatement analyze(Context context) {
        Context = new LocalContext(context);
        tryBlock.analyze(Context);
        vars = new ArrayList<JVariable>();
        for (int i = 0; i < parameters.size();i++){
            JFormalParameter par = parameters.get(i);
            Context = new LocalContext(context);
            LocalVariableDefn defn = new LocalVariableDefn(par.type().resolve(Context), Context.nextOffset());
            defn.initialize();
            Context.addEntry(par.line(),par.name(), defn);
            par.analyze(Context);
            if (catchBlocks != null) {
                catchBlocks.get(i).analyze(Context);
            }
        }

        if (finallyBlock!=null){
            Context = new LocalContext(context);
            finallyBlock.analyze(Context);
        }
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output) {
        String starttry = output.createLabel();
        String endTry = output.createLabel();
        String endFinally = output.createLabel();
        String startFinally =output.createLabel();
        String StartFinallyPlusOne = output.createLabel();
        output.addLabel(starttry);
        tryBlock.codegen(output);
        output.addLabel(endTry);
        if (finallyBlock!=null){
            finallyBlock.codegen(output);
            output.addBranchInstruction(GOTO, endFinally);
        }
        int catchsize = catchBlocks.size();
        ArrayList<String> catchlabels = new ArrayList<String>();
        ArrayList<String> endlabels = new ArrayList<String>();
        for (int i = 0;i<catchsize;i++){
            String catchlaber = output.createLabel();
            String endlaber = output.createLabel();
            catchlabels.add(catchlaber);
            endlabels.add(endlaber);
            output.addLabel(catchlaber);
            output.addNoArgInstruction(ASTORE_1);
            //vars.get(i).codegen(output);
            /*for (JStatement element : catchBlocks.get(i).statements()){
                element.codegen(output);
            }*/
            catchBlocks.get(i).codegen(output);
            output.addLabel(endlaber);
            if (finallyBlock!=null){
                finallyBlock.codegen(output);
                output.addBranchInstruction(GOTO, endFinally);
            }

        }
        if (finallyBlock!=null){
            output.addLabel(startFinally);
            output.addOneArgInstruction(ASTORE, 5);
            output.addLabel(StartFinallyPlusOne);
            finallyBlock.codegen(output);
            output.addOneArgInstruction(ALOAD, 5);
            output.addNoArgInstruction(ATHROW);
            output.addLabel(endFinally);
        }
        for (int j = 0; j < catchsize;j++){
            String par = String.format("java/lang/%s",parameters.get(j).type().toString());
            output.addExceptionHandler(starttry,endTry,catchlabels.get(j),par);
        }
        if (finallyBlock!=null){
            output.addExceptionHandler(starttry,endTry,startFinally,null);
            int i = 0;
            while (i<(catchsize-1)){
                output.addExceptionHandler(catchlabels.get(i),endlabels.get(i),startFinally,null);
                i++;
            }
            output.addExceptionHandler(catchlabels.get(i),startFinally,startFinally,null);
            output.addExceptionHandler(startFinally,StartFinallyPlusOne,startFinally,null);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void toJSON(JSONElement json) {
        JSONElement e = new JSONElement();
        json.addChild("JTryStatement:" + line, e);
        JSONElement e1 = new JSONElement();
        e.addChild("TryBlock", e1);
        tryBlock.toJSON(e1);
        if (catchBlocks != null) {
            for (int i = 0; i < catchBlocks.size(); i++) {
                JFormalParameter param = parameters.get(i);
                JBlock catchBlock = catchBlocks.get(i);
                JSONElement e2 = new JSONElement();
                e.addChild("CatchBlock", e2);
                String s = String.format("[\"%s\", \"%s\"]", param.name(), param.type() == null ?
                        "" : param.type().toString());
                e2.addAttribute("parameter", s);
                catchBlock.toJSON(e2);
            }
        }
        if (finallyBlock != null) {
            JSONElement e2 = new JSONElement();
            e.addChild("FinallyBlock", e2);
            finallyBlock.toJSON(e2);
        }
    }
}
