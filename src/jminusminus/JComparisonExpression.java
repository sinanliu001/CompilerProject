// Copyright 2012- Bill Campbell, Swami Iyer and Bahar Akbal-Delibas

package jminusminus;

import static jminusminus.CLConstants.*;

/**
 * This abstract base class is the AST node for a comparison expression.
 */
abstract class JComparisonExpression extends JBooleanBinaryExpression {
    /**
     * Constructs an AST node for a comparison expression.
     *
     * @param line     line in which the expression occurs in the source file.
     * @param operator the comparison operator.
     * @param lhs      the lhs operand.
     * @param rhs      the rhs operand.
     */
    protected JComparisonExpression(int line, String operator, JExpression lhs, JExpression rhs) {
        super(line, operator, lhs, rhs);
    }

    /**
     * {@inheritDoc}
     */
    public JExpression analyze(Context context) {
        lhs = (JExpression) lhs.analyze(context);
        rhs = (JExpression) rhs.analyze(context);
        rhs.type().mustMatchExpected(line(), lhs.type());
        type = Type.BOOLEAN;
        return this;
    }
}

/**
 * The AST node for a greater-than (&gt;) expression.
 */
class JGreaterThanOp extends JComparisonExpression {
    /**
     * Constructs an AST node for a greater-than expression.
     *
     * @param line line in which the greater-than expression occurs in the source file.
     * @param lhs  lhs operand.
     * @param rhs  rhs operand.
     */
    public JGreaterThanOp(int line, JExpression lhs, JExpression rhs) {
        super(line, ">", lhs, rhs);
    }

    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output, String targetLabel, boolean onTrue) {
        lhs.codegen(output);
        rhs.codegen(output);
        if (lhs.type()==Type.INT&&rhs.type()==Type.INT){
            output.addBranchInstruction(onTrue ? IF_ICMPGT : IF_ICMPLE, targetLabel);
        } else if (lhs.type().equals(Type.DOUBLE) && rhs.type().equals(Type.DOUBLE)) {
            output.addNoArgInstruction(DCMPL);
            output.addBranchInstruction(onTrue ? IFGT : IFLE, targetLabel);
        } else if (lhs.type().equals(Type.LONG) && rhs.type().equals(Type.LONG)) {
            output.addNoArgInstruction(LCMP);
            output.addBranchInstruction(onTrue ? IFGT : IFLE, targetLabel);
        }
    }
}

/**
 * The AST node for a less-than-or-equal-to (&lt;=) expression.
 */
class JLessEqualOp extends JComparisonExpression {

    /**
     * Constructs an AST node for a less-than-or-equal-to expression.
     *
     * @param line line in which the less-than-or-equal-to expression occurs in the source file.
     * @param lhs  lhs operand.
     * @param rhs  rhs operand.
     */
    public JLessEqualOp(int line, JExpression lhs, JExpression rhs) {
        super(line, "<=", lhs, rhs);
    }

    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output, String targetLabel, boolean onTrue) {
        lhs.codegen(output);
        rhs.codegen(output);
        if (lhs.type() == Type.INT && rhs.type() == Type.INT) {
            output.addBranchInstruction(onTrue ? IF_ICMPLE : IF_ICMPGT, targetLabel);
        } else if (lhs.type().equals(Type.DOUBLE) && rhs.type().equals(Type.DOUBLE)) {
            output.addNoArgInstruction(DCMPG);
            output.addBranchInstruction(onTrue ? IFLE : IFGT, targetLabel);
        } else if (lhs.type().equals(Type.LONG) && rhs.type().equals(Type.LONG)) {
            output.addNoArgInstruction(LCMP);
            output.addBranchInstruction(onTrue ? IFLE : IFGT, targetLabel);
        }
    }
}

/**
 * The AST node for a greater-than-or-equal-to (&gt;=) expression.
 */
class JGreaterEqualOp extends JComparisonExpression {

    /**
     * Constructs an AST node for a greater-than-or-equal-to expression.
     *
     * @param line line in which the greater-than-or-equal-to expression occurs in the source file.
     * @param lhs  lhs operand.
     * @param rhs  rhs operand.
     */
    public JGreaterEqualOp(int line, JExpression lhs, JExpression rhs) {
        super(line, ">=", lhs, rhs);
    }

    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output, String targetLabel, boolean onTrue) {
        lhs.codegen(output);
        rhs.codegen(output);
        if (lhs.type().equals(Type.INT) && rhs.type().equals(Type.INT)) {
            output.addBranchInstruction(onTrue ? IF_ICMPGE : IF_ICMPLT, targetLabel);
        } else if (lhs.type().equals(Type.DOUBLE) && rhs.type().equals(Type.DOUBLE)) {
            output.addNoArgInstruction(DCMPL);
            output.addBranchInstruction(onTrue ? IFGE : IFLT, targetLabel);
        } else if (lhs.type().equals(Type.LONG) && rhs.type().equals(Type.LONG)) {
            output.addNoArgInstruction(LCMP);
            output.addBranchInstruction(onTrue ? IFGE : IFLT, targetLabel);
        }
    }
}

/**
 * The AST node for a less-than (&lt;) expression.
 */
class JLessThanOp extends JComparisonExpression {
    /**
     * Constructs an AST node for a less-than expression.
     *
     * @param line line in which the less-than expression occurs in the source file.
     * @param lhs  lhs operand.
     * @param rhs  rhs operand.
     */
    public JLessThanOp(int line, JExpression lhs, JExpression rhs) {
        super(line, "<", lhs, rhs);
    }

    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output, String targetLabel, boolean onTrue) {
        lhs.codegen(output);
        rhs.codegen(output);
        if (lhs.type()==Type.INT&&rhs.type()==Type.INT){
            output.addBranchInstruction(onTrue ? IF_ICMPLT : IF_ICMPGE, targetLabel);
        }else if (lhs.type()==Type.DOUBLE&&rhs.type()==Type.DOUBLE){
            output.addNoArgInstruction(DCMPG);
            output.addBranchInstruction(onTrue ? IFLT : IFGE,targetLabel);
        }else if (lhs.type().equals(Type.LONG)&&rhs.type().equals(Type.LONG)){
            output.addNoArgInstruction(LCMP);
            output.addBranchInstruction(onTrue ? IFLT : IFGE,targetLabel);
        }

    }
}