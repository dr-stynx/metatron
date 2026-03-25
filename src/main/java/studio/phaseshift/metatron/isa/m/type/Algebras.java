/*
 * Metatron: A Distributed Computing Language and Virtual Machine
 *  Copyright (C) 2025- PhaseShift Studio, LLC
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package studio.phaseshift.metatron.isa.m.type;

import studio.phaseshift.metatron.isa.mach.type.Router;

import static studio.phaseshift.metatron.isa.m.mInstSet.*;
import static studio.phaseshift.metatron.isa.m.parser.mFluent.StartLess.map_;
import static studio.phaseshift.metatron.isa.m.parser.mFluent.StartLess.start_;
import static studio.phaseshift.metatron.isa.m.type.impl.MInst.instB;
import static studio.phaseshift.metatron.isa.m.type.impl.MLst.lst;

/**
 * Static helper class for algebraic operations and property checks.
 * <p>
 * This class provides type-based (not Java interface-based) checking for algebraic
 * properties and utility methods for performing algebraic operations via instructions.
 *
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class Algebras {

    // Type-based algebraic property checks

    /**
     * Check if an object's type supports addition (has PLUS_INST_TID).
     */
    public static boolean isPlusMonoid(Obj obj) {
        if (obj == null || obj.isNoObj()) return false;
        try {
            return !studio.phaseshift.metatron.isa.mach.type.Router.global()
                    .read(PLUS_INST_TID.dom(obj.tid()).rng(obj.tid()))
                    .isNoObj();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if an object's type supports multiplication (has MULT_INST_TID).
     */
    public static boolean isMultMonoid(Obj obj) {
        if (obj == null || obj.isNoObj()) return false;
        try {
            return !studio.phaseshift.metatron.isa.mach.type.Router.global()
                    .read(MULT_INST_TID.dom(obj.tid()).rng(obj.tid()))
                    .isNoObj();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if an object's type forms a ring (has plus, mult, and neg).
     */
    public static boolean isRing(Obj obj) {
        return isPlusMonoid(obj) && isMultMonoid(obj) && hasNeg(obj);
    }

    /**
     * Check if an object's type supports negation (has NEG_INST_TID).
     */
    public static boolean hasNeg(Obj obj) {
        if (obj == null || obj.isNoObj()) return false;
        try {
            return !Router.global().read(NEG_INST_TID.dom(obj.tid()).rng(obj.tid())).isNoObj();
        } catch (Exception e) {
            return false;
        }
    }

    // Identity element checks via instructions

    /**
     * Check if an object is the additive identity (zero) for its type.
     */
    public static boolean isZero(Obj obj) {
        if (obj == null || obj.isNoObj()) return false;
        try {
            Obj zero = Algebras.zero(obj);
            return obj.equals(zero);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if an object is the multiplicative identity (one) for its type.
     */
    public static boolean isOne(Obj obj) {
        if (obj == null || obj.isNoObj()) return false;
        try {
            Obj one = Algebras.one(obj);
            return obj.equals(one);
        } catch (Exception e) {
            return false;
        }
    }

    // Algebraic operation utilities

    /**
     * Add two objects using the PLUS_INST_TID instruction.
     * Special case: For instructions (non-noobj Calls), the sum is a split instruction.
     */
    public static Obj plus(Obj a, Obj b) {
        if (a == null || a.isNoObj()) return b;
        if (b == null || b.isNoObj()) return a;
        // In the algebra of Inst, the sum of two Insts is a split of those insts
        if (a.isObjCall() && b.isObjCall()) {
            return instB(SPLIT_INST_TID, lst(a, b));
        }
        return map_(a).plus_(b).apply();
    }

    /**
     * Multiply two objects using the MULT_INST_TID instruction.
     */
    public static Obj mult(Obj a, Obj b) {
        if (a == null || a.isNoObj()) return a;
        if (b == null || b.isNoObj()) return b;
        return map_(a).mult_(b).apply();
    }

    /**
     * Negate an object using the NEG_INST_TID instruction.
     */
    public static Obj neg(Obj a) {
        if (a == null || a.isNoObj()) return a;
        return map_(a).neg_();
    }

    /**
     * Get the additive identity (zero) for an object's type.
     */
    public static Obj zero(Obj obj) {
        if (obj == null || obj.isNoObj()) return obj;
        return map_(obj).zero_().apply();
    }

    /**
     * Get the multiplicative identity (one) for an object's type.
     */
    public static Obj one(Obj obj) {
        if (obj == null || obj.isNoObj()) return obj;
        return map_(obj).one_().apply();
    }

    /**
     * Divide two objects using the DIV_INST_TID instruction.
     */
    public static Obj div(Obj a, Obj b) {
        if (a == null || a.isNoObj()) return a;
        if (b == null || b.isNoObj()) return b;
        return a.apply(instB(DIV_INST_TID, lst(b)));
    }

    /**
     * Get the multiplicative inverse using the INV_INST_TID instruction.
     */
    public static Obj inv(Obj a) {
        if (a == null || a.isNoObj()) return a;
        return a.apply(instB(INV_INST_TID, lst()));
    }
}
