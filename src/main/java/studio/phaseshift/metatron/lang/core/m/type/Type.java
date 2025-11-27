/*
 * Metatron: A Distributed Computing Language and Virtual Machine
 * Copyright (C) 2025- PhaseShift Studio, LLC
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

package studio.phaseshift.metatron.lang.core.m.type;

import studio.phaseshift.metatron.algebra.PlusMonoid;
import studio.phaseshift.metatron.furi.fURI;
import studio.phaseshift.metatron.lang.core.m.inst.mInstSet;
import studio.phaseshift.metatron.lang.core.m.obj.NoObj;
import studio.phaseshift.metatron.lang.sys.router.Router;
import studio.phaseshift.metatron.util.MTronException;
import studio.phaseshift.metatron.util.Tuple;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static studio.phaseshift.metatron.lang.core.m.inst.mInstSet.INT_TID;
import static studio.phaseshift.metatron.lang.core.m.obj.NoObj.noobj;
import static studio.phaseshift.metatron.lang.core.m.type.impl.MLst.lst;

public interface Type extends Obj, PlusMonoid<Type> {

    @Override
    Type clone(final Object jvm, final fURI tid, final fURI vid);

    @Override
    Tuple.Pair<Call, List<Inst>> jvm();

    default List<Inst> insts() {
        return null == this.jvm().get1() ? List.of() : this.jvm().get1();
    }

    default Type addInst(final Inst inst) {
        if (inst.hasDom() && !inst.dom().tid().isGeneric() && !inst.dom().tid().basePath().equals(this.tid().basePath()))
            throw MTronException.of("type inst must have a domain that can resolve to type %s: %s", this.tid(), inst);

        final List<Inst> insts = this.jvm().get1();
        final List<Inst> clone = null == insts ? new ArrayList<>() : new ArrayList<>(insts);
        clone.add(inst);
        return this.clone(Tuple.Pair.with(this.jvm().get0(), clone), this.tid(), this.vid());
    }

    default boolean isBaseType() {
        return mInstSet.BASE_TYPES.contains(this.tid().basePath());
    }

    default Inst constructor() {
        return this.jvm().get1() == null ? null : this.jvm().get1().get(0);
    }

    default Call predicate() {
        return this.jvm().get0();
    }

    default boolean hasPredicate() {
        return null != this.jvm().get0();
    }

    default boolean hasConstructor() {
        return null != this.jvm().get1() && !this.jvm().get1().isEmpty();
    }

    @Override
    default Obj apply(final Obj obj) {
        // if (!obj.rng().tid().matches(this.tid()))
        //     return NoObj.single();
        if (!this.isBaseType()) {
            Obj subType = Router.readFromSpace(this.tid());
            if (!subType.equals(this))
                if (subType.apply(obj).isNoObj())
                    return noobj();
        }
        return !this.hasPredicate() || !this.predicate().apply(obj).isNoObj() ?
                obj :
                noobj();
    }

    @Override
    default Type plus(final Type other) {
        if (this.isNoObj())
            return other;
        if (other.isNoObj())
            return this;
        final fURI tidPlus = this.tid().plus(other.tid());
        final Call constructor = null == this.constructor() ? other.constructor() : null == other.constructor() ? this.constructor() : this.constructor().plus(other.constructor());
        final Call predicate = null == this.predicate() ? other.predicate() : null == other.predicate() ? this.predicate() : this.predicate().plus(other.predicate());
        return this.clone(Tuple.Pair.with(constructor, predicate), tidPlus, tidPlus);
    }

    @Override
    default Type zero() {
        return this.tid(this.tid().zero()).jvm(Tuple.Pair.with(null, null));
    }
}
