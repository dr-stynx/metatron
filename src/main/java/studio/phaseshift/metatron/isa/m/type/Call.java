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

import studio.phaseshift.metatron.furi.c.cInt;
import studio.phaseshift.metatron.furi.fURI;
import studio.phaseshift.metatron.isa.m.type.impl.MCode;
import studio.phaseshift.metatron.isa.m.type.impl.MInst;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static studio.phaseshift.metatron.isa.m.mInstSet.*;
import static studio.phaseshift.metatron.isa.m.parser.mFluent.StartLess.split_;
import static studio.phaseshift.metatron.isa.m.type.impl.MLst.lst;
import static studio.phaseshift.metatron.isa.m.type.impl.MObjs.objs;

public interface Call extends Obj {

    default boolean hasDomOrRng() {
        return this.hasDom() || this.hasRng();
    }

    default boolean hasDomAndRng() {
        return this.hasDom() && this.hasRng();
    }

    default boolean hasDom() {
        return this.tid().hasDom();
    }

    default boolean hasRng() {
        return this.tid().hasRng();
    }

    static Call from(final List<Inst> insts) {
        if (insts.isEmpty())
            return NoObj.noobj();
        else if (insts.size() == 1)
            return insts.get(0);
        else
            return MCode.of(insts);
    }

    default Call tryToInst() {
        if (this.isCode()) {
            if (this.codeValue().isEmpty())
                return NoObj.noobj();
            else if (this.codeValue().size() == 1)
                return this.codeValue().getFirst();
        }
        return this;
    }

    default Code toCode() {
        if (this.isCode())
            return (Code) this;
        else
            return new MCode(List.of(this.as()), CODE_TID,null);
    }

    default boolean isAuto() {
        if (this.isNoObj())
            return false;
        final List<Inst> insts = this.insts();
        if (insts.isEmpty())
            return false;
        final fURI first = insts.getFirst().tid().basePath();
        return first.equals(AUTO_FROM_INST_TID) || first.equals(AUTO_INST_TID);
    }

    default List<Inst> insts() {
        return this.isCode() ? this.codeValue() : List.of(this.as());
    }

    @Override
    Call resolve(final Obj start);

    default <C extends Call> C dom(final Type domain) {
        return (C) this.tid(this.tid().dom(domain.tid()));
    }

    default <C extends Call> C rng(final Type range) {
        return (C) this.tid(this.tid().rng(range.tid()));
    }

    @Override
    default Call c(final Function<cInt, cInt> func) {
        return (Call) Obj.super.c(func);
    }

    @Override
    default Obj append(final Obj obj) {
        return obj.isCall() && !obj.tid().basePath().equals(AUTO_FROM_INST_TID) ? objs(List.of(this, obj)) : objs(List.of(this, obj));
    }
}
