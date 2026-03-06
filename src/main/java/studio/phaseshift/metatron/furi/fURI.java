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

package studio.phaseshift.metatron.furi;

import studio.phaseshift.metatron.Tokens;
import studio.phaseshift.metatron.algebra.Ring;
import studio.phaseshift.metatron.furi.c.cInt;
import studio.phaseshift.metatron.furi.form.*;
import studio.phaseshift.metatron.isa.m.type.Uri;
import studio.phaseshift.metatron.isa.mach.type.Router;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static studio.phaseshift.metatron.isa.m.type.impl.MUri.uri;

/*
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public interface fURI extends Cloneable, Ring<fURI>, Comparable<fURI>, Predicate<fURI> {

    public static final fURI ALL = f("#");
    public static final fURI EMPTY = fURI.empty();
    public static final fURI fnull = null;
    public static final fURI DOM = f("dom");
    public static final fURI RNG = f("rng");
    public static final fURI NOOBJ = f("noobj");


    static fURI f(final String furi) {
        return fURI.of(furi);
    }

    default fURI big() {
        return Router.loaded() ? Router.global().rewrite(this, true) : this;
    }

    default fURI small() {
        return Router.loaded() ? Router.global().rewrite(this, false) : this;
    }

    fURI removePrefix(final fURI prefix);

    fURI resolve();

    String scheme();

    fURI scheme(final String scheme);

    default boolean hasScheme() {
        return null != this.scheme();
    }

    fURI mult(final fURI rhs);

    fURI plus(final fURI rhs);
    
   /* default String authority() {
        if (this.hasHost())
            return this.hasPort() ? this.host() + ":" + this.port() : this.host();
        return null;
    }*/

    String host();

    fURI host(final String host);

    default boolean hasHost() {
        return this.host() != null;
    }

    int port();

    default boolean hasPort() {
        return this.port() != -1;
    }

    fURI port(final int port);

    List<String> path();

    fURI path(final List<String> path);

    fURI path(final String path);

    default String path(final int index) {
        return this.path().get(index);
    }

    String pathString();

    default String name() {
        return this.path().isEmpty() ? Tokens.EMPTY : this.path().getLast();
    }

    boolean test(final fURI lhs);

    default boolean bimatches(final fURI rhs) {
        return this.test(rhs) || rhs.test(this);
    }

    boolean isEmpty();

    fURI noQ();

    fURI extend(final String segment);

    fURI extend(final fURI segment);

    fURI head(final int steps);

    fURI tail(final int steps);

    fURI retract(final int steps);

    fURI retractPattern();

    fURI retract(final String segment);

    fURI prepend(final String segment);

    fURI pretract(final String segment);

    fURI pretract(final int steps);

    boolean hasPrefix(final fURI prefix);

    boolean hasPostfix(final fURI postfix);

    default fURI authority() {
        if (this.hasHost())
            return fURI.of(this.host() + ":" + this.port());
        return null;
    }

    default boolean hasAuthority() {
        return this.hasHost();
    }

    boolean hasPattern();

    boolean hasPattern(final char patternCharacter);

    fURI basePath();

    List<String> poly();

    int pathLength();

    boolean isGeneric();

    default Uri toUri() {
        return uri(this);
    }

    C c();

    default fURI neg() {
        return this.c(this.c().neg());
    }

    fURI c(final C<?, ?> coefficient);

    fURI c(final String coefficient);

    default fURI poly(final List<String> poly) {
        return this;
    }

    fURI zero();

    fURI one();

    fURI maybe();

    fURI maybeSome();

    fURI maybeMaybe();

    fURI some();

    fURI asNode();

    fURI asBranch();

    //default ifURI dom() {
    //    return this.q(DOM);
    //}

    fURI dom();

    fURI dom(fURI dom);

    fURI rng();

    fURI rng(fURI rng);

    boolean hasDom();

    boolean hasRng();

    boolean hasDom(final fURI dom);

    boolean hasRng(final fURI rng);


    String qString();

    Map<String, String> qMap();

    fURI q(final Map<String, String> query);

    <T> T qValue(final String key, final Class<T> valueClass);

    String q(final String key);

    fURI q(final String key, final Object value);

    boolean hasQ(final String key);

    boolean hasQ();

    boolean isRelative();

    default boolean isAbsolute() {
        return !this.isRelative();
    }

    boolean isBranch();

    default boolean isNode() {
        return !this.isBranch();
    }

    fURI asRelative();

    public static fURI dotPath(final String uri) {
        return fURI.of(uri.replace('.', '/'));
    }

    default fURI removeSubpath(final fURI subpath) {
        String newPath = this.toString();
        return fURI.of(newPath.replace(subpath.asBranch().toString(), Tokens.EMPTY));
    }

    fURI clone();


    /// /////////////////////////////////////////////

    default boolean isZero() {
        return this.toString().equals(NOOBJ.toString()) || this.c().isZero();
    }

    default boolean isOne() {
        return this.c().isOne();
    }

    default boolean isAny() {
        return this.c().isAny();
    }

    default boolean isZeroable() {
        return this.c().isZeroable();
    }

    default boolean isSome() {
        return this.c().isSome();
    }

    default boolean isMaybe() {
        return this.c().isMaybe();
    }

    default boolean isMaybeSome() {
        return this.c().isMaybeSome();
    }

    /// ////////////////////////////////////////////////

    static fURI empty() {
        return XXXXXfURI.INSTANCE;
    }


    static fURI of(final String furi) {
        if (null == furi)
            return empty();
        String scheme = null;
        String host = null;
        int port = -1;
        String pathStr = null;
        cInt coefficient = cInt.ONE();
        Map<String, String> query = Map.of();


        Matcher matcher;
        try {
            matcher = Pattern.compile("((?<scheme>[^:/.]+):)?(//((?<host>[^:/]+)(:(?<port>\\d+))?))?(?<path>[^?{]+)?(\\{(?<coefficient>[^}]+)})?(\\?(?<query>[^#+]+))?").matcher(furi);
            if (!matcher.matches()) {
                throw new IllegalArgumentException("Invalid URI: " + furi);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid URI: " + furi, e);
        }

        scheme = matcher.group("scheme");
        host = matcher.group("host");
        port = matcher.group("port") == null ? -1 : Integer.parseInt(matcher.group("port"));
        pathStr = matcher.group("path");
        coefficient = matcher.group("coefficient") == null ? cInt.ONE() : cInt.of(matcher.group("coefficient").replace("{", "").replace("}", ""));
        String queryStr = matcher.group("query");
        query = queryStr == null ? Map.of() : parseQuery(queryStr);
        List<String> path = null == pathStr ? List.of() : new ArrayList<>(Arrays.asList(pathStr.split("/")));
        if (null != pathStr) {
            if (pathStr.endsWith("/"))
                path.add("");
            if (path.stream().allMatch(String::isEmpty)) {
                path.clear();
            }
        }
        return fURI.of(scheme, host, port, path, coefficient, List.of(), query);
    }

    static Map<String, String> parseQuery(final String query) {
        return query == null ? Map.of() : Arrays.stream(query.split("&")).map(s -> s.split("=")).collect(Collectors.toMap(a -> a[0], a -> a.length > 1 ? a[1] : ""));
    }


    static fURI of(final String scheme, final String host, final int port, final List<String> path, final C<?, ?> coefficient, final List<String> poly, final Map<String, String> query) {
        if (!coefficient.isOne()) {
            if (!query.isEmpty()) {
                return new SAPCQfURI(scheme, host, port, path, coefficient, query);
            } else {
                if (null == scheme && null == host)
                    return new XXPCXfURI(path, coefficient);
                else if (null == host)
                    return new SXPCXfURI(scheme, path, coefficient);
                else
                    return new SAPCXfURI(scheme, host, port, path, coefficient);
            }
        } else {
            if (query.isEmpty()) {
                if (null != scheme) {
                    if (null == host)
                        return new SXPXXfURI(scheme, path);
                    else
                        return new SAPXXfURI(scheme, host, port, path);
                } else {
                    return host == null ? new XXPXXfURI(path) : new SAPXXfURI(null, host, port, path);
                }
            } else {
                return new SAPCQfURI(scheme, host, port, path, coefficient, query);
            }
        }
    }
}
