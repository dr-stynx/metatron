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

package studio.phaseshift.metatron.lang.db.grph.type.mtp3;

import org.apache.commons.configuration2.MapConfiguration;
import org.apache.tinkerpop.gremlin.jsr223.GremlinLangScriptEngine;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.io.IoRegistry;
import studio.phaseshift.metatron.BootLoader;
import studio.phaseshift.metatron.lang.db.grph.inst.grphInstSet;
import studio.phaseshift.metatron.lang.db.kv.inst.kvInstSet;
import studio.phaseshift.metatron.lang.db.kv.kvSpace;
import studio.phaseshift.metatron.lang.sys.router.Router;

import javax.script.ScriptContext;
import javax.script.SimpleBindings;
import java.util.LinkedHashMap;
import java.util.Map;

import static studio.phaseshift.metatron.Tokens.*;
import static studio.phaseshift.metatron.furi.fURI.f;
import static studio.phaseshift.metatron.lang.core.m.type.impl.MRec.rec;

/*
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class mConsole {

    public static void main(final String[] args) throws Exception {
        BootLoader.load(rec());
        kvInstSet.create();
        grphInstSet.create();
        Router.global().addSpace(kvSpace.of(f("/mnt/#"), f("/sys/router/space/kv")));
        final Map<String, Object> config = new LinkedHashMap<>();
        config.put(Graph.GRAPH, f(mGraph.class.getCanonicalName()));
        config.put(STORE, f("/mnt/test/mtp3"));
        config.put(PATTERN, f("/g/#"));
        config.put(NAME, f("/g/"));
        config.put("guice.injector-source", f("studio.phaseshift.metatron.lang.db.grph.mtp3.mGraphFeatureTest$WorldInjectorSource"));
        config.put(IoRegistry.IO_REGISTRY, f(mIoRegistry.class.getCanonicalName()));
        final mGraph graph = new mGraph(new MapConfiguration(config));
        GremlinLangScriptEngine se = new GremlinLangScriptEngine();
        se.setBindings(new SimpleBindings(Map.of("g", graph.traversal())), ScriptContext.GLOBAL_SCOPE);
        System.out.println(se.eval("g.V().toList()"));

    }

}
