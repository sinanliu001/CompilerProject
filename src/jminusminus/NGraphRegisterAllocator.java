// Copyright 2012- Bill Campbell, Swami Iyer and Bahar Akbal-Delibas

package jminusminus;

import java.awt.*;
import java.lang.reflect.Array;
import java.util.*;

import static jminusminus.NPhysicalRegister.*;

/**
 * Implements register allocation using graph coloring algorithm.
 */
public class NGraphRegisterAllocator extends NRegisterAllocator {
    /**
     * Constructs an NGraphRegisterAllocator object.
     *
     * @param cfg an instance of a control flow graph.
     */
    public NGraphRegisterAllocator(NControlFlowGraph cfg) {
        super(cfg);
    }

    /**
     * {@inheritDoc}
     */
    public void allocation() {
        buildIntervals();
        ArrayList<NInterval> livenessIntervals = new ArrayList<NInterval> ();
        HashMap<Integer, ArrayList<Integer>> edge = new HashMap<Integer, ArrayList<Integer>>();
        for (NInterval interval:cfg.intervals){
            if (cfg.registers.get(interval.vRegId) != null){
                livenessIntervals.add(interval);
            }
        }
        for (NInterval interval:livenessIntervals){
            edge.put(interval.vRegId, new ArrayList<Integer>());
            for(int key:edge.keySet()){
                if (!edge.get(key).isEmpty()){
                    if(edge.get(key).contains(interval.vRegId)){
                        edge.get(interval.vRegId).add(key);}
                }
            }
            for (NRange r1 : interval.ranges) {
                for (NInterval edgeto:livenessIntervals){
                    for (NRange r : edgeto.ranges) {
                        if(interval!=edgeto){
                            if (r1.start >= r.start && r1.start < r.stop){
                                if (!edge.get(interval.vRegId).contains(edgeto.vRegId)){
                                    edge.get(interval.vRegId).add(edgeto.vRegId);
                                }
                            } else if (r1.stop > r.start && r1.stop <= r.stop){
                                if (!edge.get(interval.vRegId).contains(edgeto.vRegId)){
                                    edge.get(interval.vRegId).add(edgeto.vRegId);
                                }
                            } else if (r.start >= r1.start && r.start < r1.stop){
                                if (!edge.get(interval.vRegId).contains(edgeto.vRegId)){
                                    edge.get(interval.vRegId).add(edgeto.vRegId);
                                }
                            } else if (r.stop > r1.start && r.stop <= r1.stop){
                                if (!edge.get(interval.vRegId).contains(edgeto.vRegId)){
                                    edge.get(interval.vRegId).add(edgeto.vRegId);
                                }
                            }
                            break;
                        }
                    }
                }
            }
        }

        int R = 1;
        int r = 0;
        int count = 0;
        int loopcheck = 0;

        Stack<Integer> stack;

        while (true){
            stack = new Stack<Integer>();
            HashMap<Integer, ArrayList<Integer>> colorgraph = new HashMap<Integer, ArrayList<Integer>>();
            colorgraph=copy(edge);
            while(count<cfg.intervals.size())
            {
                if (colorgraph.containsKey(count)){
                    if (colorgraph.get(count).size()<R){
                        colorgraph.remove(count);
                        if (count > 31){
                            stack.push(count);
                        }
                        for(int i: colorgraph.keySet()){
                            if (colorgraph.get(i).contains(count)){
                                colorgraph.get(i).remove((Integer) count);
                            }
                        }
                        count = 0;
                    } else {
                        count++;
                    }
                } else {
                    count++;
                }
            }
            if (colorgraph.isEmpty()){
                break;
            } else {
                loopcheck++;
                R++;
                count = 0;
            }
        }
        for (NInterval interval : cfg.intervals) {
            NBasicBlock lastBlock = cfg.basicBlocks.get(cfg.basicBlocks.size() - 1);
            NLIRInstruction lastLir = lastBlock.lir.get(lastBlock.lir.size() - 1);
            interval.ranges.add(new NRange(0, lastLir.id));
        }

        preprocess();
        System.out.println(edge);
        System.out.println(stack);
        System.out.println(R);
        if (R > MAX_COUNT){
            Queue<NInterval> assigned = new LinkedList<NInterval>();
            for (int i = 32, j = 0; i < cfg.intervals.size(); i++) {
                NInterval interval = cfg.intervals.get(i);
                if (interval.pRegister == null) {
                    if (j >= MAX_COUNT) {
                        // Pull out (from a queue) a register that's already assigned to another
                        // interval and re-assign it to this interval. But then we have a spill
                        // situation, so create an offset for the spill.
                        NInterval spilled = assigned.remove();
                        spilled.spill = true;
                        if (spilled.offset == -1) {
                            spilled.offset = cfg.offset++;
                            spilled.offsetFrom = OffsetFrom.SP;
                        }
                        interval.pRegister = spilled.pRegister;
                        interval.spill = true;
                        if (interval.offset == -1) {
                            interval.offset = cfg.offset++;
                            interval.offsetFrom = OffsetFrom.SP;
                        }
                    } else {
                        // Allocate free register to interval.
                        NPhysicalRegister pRegister = regInfo[T0 + j++];
                        interval.pRegister = pRegister;
                        cfg.pRegisters.add(pRegister);
                    }
                    assigned.add(interval);
                }
            }
        }else {
            for (int i = 0; i < R; i++){
                NPhysicalRegister pRegister = regInfo[T0 + i];
                cfg.pRegisters.add(pRegister);
            }
            int z = 0;
            while (!stack.isEmpty()){
                int x = stack.pop();
                if (!stack.isEmpty()){
                    int y = stack.peek();
                    if (cfg.intervals.get(x).pRegister == null){
                        if (edge.get(x).contains(y)){
                            cfg.intervals.get(x).pRegister = cfg.pRegisters.get(z);
                            z++;
                            if (z == R){
                                z = 0;
                            }
                        }
                        else {
                            cfg.intervals.get(x).pRegister = cfg.pRegisters.get(z);
                        }
                    }
                } else {
                    if (cfg.intervals.get(x).pRegister == null){
                        cfg.intervals.get(x).pRegister = cfg.pRegisters.get(z);
                    }
                }
            }
        }

        for (int i = 1; i < cfg.basicBlocks.size(); i++) {
            // We ignore block B0.
            NBasicBlock block = cfg.basicBlocks.get(i);
            ArrayList<NLIRInstruction> newLir = new ArrayList<NLIRInstruction>();
            for (NLIRInstruction lir : block.lir) {
                newLir.add(lir);
            }
            for (NLIRInstruction lir : block.lir) {
                int id = lir.id;
                if (lir.reads.size() == 2) {
                    NInterval input1 = cfg.intervals.get(lir.reads.get(0).number()).childAt(id);
                    NInterval input2 = cfg.intervals.get(lir.reads.get(1).number()).childAt(id);
                    if (input1.pRegister == input2.pRegister) {
                        input2.pRegister =
                                regInfo[T0 + (input2.pRegister.number() + 1) % MAX_COUNT];
                    }
                }

                // Loads.
                for (int j = 0; j < lir.reads.size(); j++) {
                    NInterval input = cfg.intervals.get(lir.reads.get(j).number()).childAt(id);
                    if (input.spill) {
                        NLIRLoad load = new NLIRLoad(block, id - lir.reads.size() + j, input.offset,
                                input.offsetFrom, input.pRegister);
                        newLir.add(newLir.indexOf(lir), load);
                    }
                }

                // Stores.
                if (lir.write != null) {
                    NInterval output = cfg.intervals.get(lir.write.number());
                    if (output.spill) {
                        NLIRStore store = new NLIRStore(block, id + 1, output.offset,
                                output.offsetFrom, lir.write);
                        newLir.add(newLir.indexOf(lir) + 1, store);
                    }
                }
            }
            block.lir = newLir;

        }
        /*

        if (NPhysicalRegister.MAX_COUNT >= R){
*/
    }
    public static HashMap<Integer, ArrayList<Integer>> copy(HashMap<Integer, ArrayList<Integer>> a){
        HashMap<Integer, ArrayList<Integer>> b = new HashMap<Integer, ArrayList<Integer>>();
        for (int x: a.keySet()){
            b.put(x,new ArrayList<Integer>(a.get(x)));
        }
        return b;
    }
}
