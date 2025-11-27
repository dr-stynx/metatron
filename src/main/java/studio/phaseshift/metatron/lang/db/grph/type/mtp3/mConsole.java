package studio.phaseshift.metatron.lang.db.grph.type.mtp3;

import org.apache.commons.configuration2.MapConfiguration;
import org.apache.tinkerpop.gremlin.console.Console;
import org.apache.tinkerpop.gremlin.console.GremlinGroovysh;
import org.apache.tinkerpop.gremlin.console.Mediator;
import org.apache.tinkerpop.gremlin.groovy.jsr223.GremlinGroovyScriptEngine;
import org.apache.tinkerpop.gremlin.jsr223.GremlinLangScriptEngine;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.io.Io;
import org.apache.tinkerpop.gremlin.structure.io.IoRegistry;
import org.apache.tinkerpop.gremlin.structure.io.gryo.GryoIo;
import org.apache.tinkerpop.gremlin.util.Gremlin;
import org.codehaus.groovy.tools.shell.IO;
import studio.phaseshift.metatron.BootLoader;
import studio.phaseshift.metatron.lang.db.grph.inst.grphInstSet;
import studio.phaseshift.metatron.lang.db.kv.inst.kvInstSet;
import studio.phaseshift.metatron.lang.db.kv.kvSpace;
import studio.phaseshift.metatron.lang.sys.router.Router;

import javax.script.ScriptContext;
import javax.script.SimpleBindings;
import java.util.LinkedHashMap;
import java.util.List;
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
     se.setBindings(new SimpleBindings(Map.of("g",graph.traversal())), ScriptContext.GLOBAL_SCOPE);
     System.out.println(se.eval("g.V().toList()"));
       
    }

}
