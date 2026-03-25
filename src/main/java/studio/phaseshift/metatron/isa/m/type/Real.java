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

import net.objecthunter.exp4j.ExpressionBuilder;
import studio.phaseshift.metatron.algebra.MultGroup;
import studio.phaseshift.metatron.algebra.Ring;
import studio.phaseshift.metatron.furi.c.cInt;
import studio.phaseshift.metatron.furi.fURI;
import studio.phaseshift.metatron.isa.mach.type.Router;
import studio.phaseshift.metatron.util.MathUtil;

import java.util.*;
import java.util.stream.Collectors;

import static studio.phaseshift.metatron.furi.fURI.Singleton.ALL;
import static studio.phaseshift.metatron.isa.m.mInstSet.*;
import static studio.phaseshift.metatron.isa.m.type.Algebras.*;
import static studio.phaseshift.metatron.isa.m.type.impl.MBool.bool;
import static studio.phaseshift.metatron.isa.m.type.impl.MInst.instC;
import static studio.phaseshift.metatron.isa.m.type.impl.MInt.jnt;
import static studio.phaseshift.metatron.isa.m.type.impl.MLst.lst;
import static studio.phaseshift.metatron.isa.m.type.impl.MReal.real;
import static studio.phaseshift.metatron.isa.m.type.impl.MStr.str;
import static studio.phaseshift.metatron.isa.m.type.impl.MType.T;

public interface Real extends Mono {

    Real ZERO = real(0.0d);
    Real ONE = real(1.0d);
    Type REAL_TYPE = Type.Builder.build().tid(REAL_TID).vid(REAL_TID).create();

    @Override
    Real clone(final Object jvm, final fURI tid, final fURI vid);

    @Override
    Double jvm();

    default Real jvm(final Double jvm) {
        return this.clone(jvm, this.tid(), this.vid());
    }

    default Real tid(final fURI tid) {
        return this.clone(this.jvm(), tid, this.vid());
    }

    default Real vid(final fURI vid) {
        return this.clone(this.jvm(), this.tid(), vid);
    }

    Real self(final Double jvm, final fURI tid, final fURI vid);

    @Override
    default Real c(final cInt c) {
        return (Real) Mono.super.c(c);
    }



    final class TypeObj {

        Type REAL_TYPE = T(REAL_TID);

        public static Set<Inst> insts() {
            return new LinkedHashSet<>(List.of(
                    instC(AS_INST_TID.dom(REAL_TID).rng(INT_TID), lst(T(INT_TID)), (lhs, inst) -> jnt(lhs.realValue().longValue(), inst.arg(0).tid(), lhs.vid())),
                    instC(AS_INST_TID.dom(REAL_TID).rng(STR_TID), lst(T(STR_TID)), (lhs, inst) -> str(String.valueOf(lhs.realValue()), inst.arg(0).tid(), lhs.vid())),
                    instC(GT_INST_TID.dom(REAL_TID).rng(BOOL_TID), lst(T(REAL_TID)), (lhs, inst) -> bool(Inst.Helper.alignLHSType(lhs, inst.arg(0)).filter(l -> l.realValue() > inst.arg(0).realValue()).isPresent())),
                    instC(GTE_INST_TID.dom(REAL_TID).rng(BOOL_TID), lst(T(REAL_TID)), (lhs, inst) -> bool(Inst.Helper.alignLHSType(lhs, inst.arg(0)).filter(l -> l.realValue() >= inst.arg(0).realValue()).isPresent())),
                    instC(LT_INST_TID.dom(REAL_TID).rng(BOOL_TID), lst(T(REAL_TID)), (lhs, inst) -> bool(Inst.Helper.alignLHSType(lhs, inst.arg(0)).filter(l -> l.realValue() < inst.arg(0).realValue()).isPresent())),
                    instC(LTE_INST_TID.dom(REAL_TID).rng(BOOL_TID), lst(T(REAL_TID)), (lhs, inst) -> bool(Inst.Helper.alignLHSType(lhs, inst.arg(0)).filter(l -> l.realValue() <= inst.arg(0).realValue()).isPresent())),
                    instC(NEG_INST_TID.dom(REAL_TID).rng(REAL_TID), lst(), (lhs, inst) -> lhs.jvm(-1.0d * lhs.realValue())),
                    instC(PLUS_INST_TID.dom(REAL_TID).rng(REAL_TID), lst(T(REAL_TID)), (lhs, inst) -> lhs.jvm((lhs.realValue() * lhs.c().max()) + (Inst.Helper.alignRHSType(lhs,inst.arg(0)).realValue() * Inst.Helper.alignRHSType(lhs,inst.arg(0)).c().max())).c(cInt.ONE())),
                    instC(MULT_INST_TID.dom(REAL_TID).rng(REAL_TID), lst(T(REAL_TID)), (lhs, inst) -> lhs.jvm((lhs.realValue() * lhs.c().max()) * (Inst.Helper.alignRHSType(lhs,inst.arg(0)).realValue() * Inst.Helper.alignRHSType(lhs,inst.arg(0)).c().max())).c(cInt.ONE())),
                    instC(DIV_INST_TID.dom(REAL_TID).rng(REAL_TID), lst(T(REAL_TID)), (lhs, inst) -> lhs.jvm(lhs.realValue() / Inst.Helper.alignRHSType(lhs,inst.arg(0)).realValue())),
                    instC(ZERO_INST_TID.dom(REAL_TID).rng(REAL_TID), lst(), (lhs, inst) -> ZERO),
                    instC(ONE_INST_TID.dom(REAL_TID).rng(REAL_TID), lst(), (lhs, inst) -> ONE),
                    instC(INV_INST_TID.dom(REAL_TID).rng(REAL_TID), lst(), (lhs, inst) -> lhs.jvm(1.0d / lhs.realValue())),
                    instC(MINUS_INST_TID.dom(REAL_TID).rng(REAL_TID), lst(T(REAL_TID)), (lhs, inst) -> Algebras.plus(lhs, Algebras.neg(Inst.Helper.alignRHSType(lhs,inst.arg(0))))),
                    instC(SUM_INST_TID.dom(REAL_TID.maybeSome()).rng(REAL_TID), lst(), (lhs, inst) -> lhs.stream().reduce(Algebras.zero(lhs), Algebras::plus)),
                    instC(PROD_INST_TID.dom(REAL_TID.maybeSome()).rng(REAL_TID), lst(), (lhs, inst) -> lhs.stream().reduce(Algebras.one(lhs), (a, b) -> Algebras.mult(Algebras.mult(a, b), real((double)b.c().max())))),
                    instC(POW_INST_TID.dom(REAL_TID).rng(REAL_TID), lst(T(REAL_TID)), (lhs, inst) -> real(Math.pow(lhs.realValue(), inst.arg(0).realValue()))),
                    instC(MATH_INST_TID.dom(ALL.maybe()).rng(REAL_TID), lst(T(STR_TID)), (lhs, inst) -> {
                        final String equation = inst.arg(0).strValue();
                        final Set<String> variables = MathUtil.getVariables(equation);
                        final double result = new ExpressionBuilder(equation)
                                .variables(MathUtil.getVariables(equation))
                                .build()
                                .setVariables(variables.stream()
                                        .map(var -> List.of(var, Router.readFromSpace(var).<Number>jvm().doubleValue()))
                                        .collect(Collectors.toMap(
                                                a -> a.get(0).toString(),
                                                b -> (Double) b.get(1),
                                                (a, b) -> b,
                                                HashMap::new)))
                                .evaluate();
                        return real(result);
                    }),
                    instC(ORDER_INST_TID.dom(REAL_TID.maybeSome()).rng(LST_TID), lst(), (lhs, inst) -> lst(lhs.stream().sorted(Comparator.comparing(a -> a.asReal().realValue()))))));
        }
    }

}