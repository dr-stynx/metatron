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

package studio.phaseshift.metatron.furi.form;

import studio.phaseshift.metatron.furi.C;
import studio.phaseshift.metatron.furi.c.cInt;
import studio.phaseshift.metatron.furi.fURI;
import studio.phaseshift.metatron.util.MTronException;

import java.util.*;
import java.util.stream.Collectors;

/*
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public abstract class AbstractfURI implements fURI {

    @Override
    public fURI resolve() {
        final List<String> newSegments = new ArrayList<>();
        for (final String seg : this.path()) {
            if (seg.equals("."))
                continue;
            if (seg.equals("..") && !newSegments.isEmpty() && !newSegments.getLast().equals(".."))
                newSegments.removeLast();
            else
                newSegments.add(seg);
        }
        return this.path(newSegments);
    }

    @Override
    public boolean hasDom(final fURI dom) {
        return this.dom().test(dom);
    }

    @Override
    public boolean hasRng(final fURI rng) {
        return this.rng().test(rng);
    }

    @Override
    public boolean hasDom() {
        return this.hasQ("dom");
    }

    @Override
    public boolean hasRng() {
        return this.hasQ("rng");
    }

    @Override
    public boolean isGeneric() {
        if (this.path().isEmpty())
            return false;
        if (this.pathLength() == 1 && (this.path().getFirst().equals("#") || this.path().getFirst().equals("+")))
            return false;
        boolean hasCapitalGeneric = false;
        for (final String seg : this.c("1").path()) { // TODO: this is necessary because {} is appended to final segment (needs to be fixed ASAP!).
            if (!seg.isEmpty() && seg.chars().allMatch(Character::isUpperCase))
                hasCapitalGeneric = true;
            if (seg.chars().anyMatch(c -> c != '#' && c != '+' && !Character.isAlphabetic(c) || Character.isLowerCase(c)))
                return false;
        }
        return hasCapitalGeneric;
    }

    @Override
    public boolean isEmpty() {
        return !this.hasScheme() && !this.hasHost() && this.path().isEmpty() && this.qMap().isEmpty();
    }

    @Override
    public boolean hasQ() {
        return !this.qMap().isEmpty();
    }

    @Override
    public fURI noQ() {
        return fURI.of(this.scheme(), this.host(), this.port(), this.path(), this.c(), List.of(), Map.of());
    }

    @Override
    public boolean hasQ(final String key) {
        return key.equals("+") || key.equals("#") || this.qMap().containsKey(key);
    }

    @Override
    public fURI zero() {
        return NOOBJ.c(cInt.ZERO());
    }

    @Override
    public fURI one() {
        return this.c(cInt.ONE());
    }

    @Override
    public fURI maybe() {
        return this.c(cInt.MAYBE());
    }

    @Override
    public fURI maybeMaybe() {
        return this.c("??");
    }

    @Override
    public fURI maybeSome() {
        return this.c(cInt.MAYBESOME());
    }

    @Override
    public fURI some() {
        return this.c(cInt.SOME());
    }


    public fURI removePrefix(final fURI prefix) {
        final String newPath = this.toString();
        final String pre = prefix.toString();
        //return new fURI(newPath.startsWith(prefix.toString()) ? newPath.substring(prefix.send ? prefix.toString().length() +1 : prefix.toString().length()) : newPath);
        if (!newPath.startsWith(pre))
            return this;
        final fURI newURI = fURI.of(newPath.substring(pre.length() + (newPath.charAt(pre.length()) == '/' ? 1 : 0)));
        //   newURI.sstart = !prefix.send;
        return newURI;
    }

    @Override
    public fURI clone() {
        return this;
    }

    @Override
    public fURI asRelative() {
        if (!this.path().isEmpty() && this.path().getFirst().isEmpty())
            return fURI.of(this.scheme(), this.host(), this.port(), this.path().subList(1, this.path().size()), this.c(), List.of(), this.qMap());
        return this;
    }

    @Override
    public int compareTo(final fURI furi) {
        if (null == furi) return -1;
        if (this.equals(furi)) return 0;
        if (Objects.equals(this.host(), "#"))
            return 1;
        if (!Objects.equals(this.host(), furi.host()) && !Objects.equals(this.host(), "+"))
            return -1;
        for (int i = 0; i < this.path().size(); i++) {
            final String segment = this.path().get(i);
            if (segment.equals("#"))
                return 1;
            if (furi.pathLength() <= i)
                return -1;
            if (!segment.equals("+") && !segment.equals(furi.path().get(i)))
                return -1;
        }
        return (this.path().size() > furi.pathLength() || furi.pathLength() == this.path().size() && this.hasPattern()) ? 1 : -1;
    }

    @Override
    public fURI asNode() {
        if (this.pathLength() > 0 && this.path().getLast().isEmpty()) {
            final List<String> newPath = new ArrayList<>(this.path());
            newPath.removeLast();
            return fURI.of(this.scheme(), this.host(), this.port(), newPath, this.c(), List.of(), this.qMap());
        }
        return this;
    }

    @Override
    public fURI asBranch() {
        if (this.pathLength() > 0 && !this.path().getLast().isEmpty()) {
            final List<String> newPath = new ArrayList<>(this.path());
            newPath.addLast("");
            return fURI.of(this.scheme(), this.host(), this.port(), newPath, this.c(), List.of(), this.qMap());
        }
        return this;
    }

    @Override
    public fURI scheme(final String scheme) {
        return fURI.of(scheme, this.host(), this.port(), this.path(), this.c(), List.of(), this.qMap());
    }

    @Override
    public String scheme() {
        return "";
    }

    @Override
    public fURI host(final String host) {
        return fURI.of(this.scheme(), host, this.port(), this.path(), this.c(), List.of(), this.qMap());
    }

    @Override
    public String host() {
        return "";
    }


    @Override
    public int port() {
        return -1;
    }

    @Override
    public fURI port(final int port) {
        return fURI.of(this.scheme(), this.host(), port, this.path(), this.c(), List.of(), this.qMap());
    }


    @Override
    public List<String> path() {
        return List.of();
    }

    @Override
    public int pathLength() {
        return this.path().size();
    }

    @Override
    public fURI path(final List<String> path) {
        return fURI.of(this.scheme(), this.host(), this.port(), path, this.c(), List.of(), this.qMap());
    }

    @Override
    public fURI path(final String path) {
        return fURI.of(this.scheme(), this.host(), this.port(), List.of(path.split("/")), this.c(), List.of(), this.qMap());
    }

    @Override
    public fURI c(final C<?, ?> coefficient) {
        return fURI.of(this.scheme(), this.host(), this.port(), this.path(), null == coefficient ? cInt.ONE() : coefficient, List.of(), this.qMap());
    }

    @Override
    public fURI c(final String coefficient) {
        return fURI.of(this.scheme(), this.host(), this.port(), this.path(), null == coefficient ? cInt.ONE() : cInt.of(coefficient), List.of(), this.qMap());
    }

    @Override
    public fURI q(final Map<String, String> query) {
        return fURI.of(this.scheme(), this.host(), this.port(), this.path(), this.c(), List.of(), null == query ? Map.of() : query);
    }

    @Override
    public fURI dom(final fURI dom) {
        final Map<String, String> map = new HashMap<>(this.qMap());
        map.put("dom", dom.toString());
        return fURI.of(this.scheme(), this.host(), this.port(), this.path(), this.c(), List.of(), map);
    }

    @Override
    public fURI rng(final fURI rng) {
        final Map<String, String> map = new HashMap<>(this.qMap());
        map.put("rng", rng.toString());
        return fURI.of(this.scheme(), this.host(), this.port(), this.path(), this.c(), List.of(), map);
    }

    @Override
    public boolean hasPattern() {
        if (Objects.equals(this.scheme(), "#") || Objects.equals(this.scheme(), "+"))
            return true;
        if (Objects.equals(this.host(), "#") || Objects.equals(this.host(), "+"))
            return true;
        for (String segment : this.path()) {
            if (segment.equals("#") || segment.equals("+"))
                return true;
        }
        return this.qMap().entrySet().stream().anyMatch(kv -> {
            if (kv.getValue().equals("#") || kv.getValue().equals("+"))
                return true;
            if (kv.getKey().equals("#") || kv.getKey().equals("+"))
                return true;
            return false;
        });
    }

    @Override
    public boolean hasPattern(final char patternCharacter) {
        final String pattern = Character.toString(patternCharacter);
        if (Objects.equals(this.scheme(), pattern))
            return true;
        if (Objects.equals(this.host(), pattern))
            return true;
        for (String segment : this.path()) {
            if (Objects.equals(segment, pattern))
                return true;
        }
        return this.qMap().entrySet().stream().anyMatch(kv -> {
            if (Objects.equals(kv.getValue(), pattern))
                return true;
            if (Objects.equals(kv.getKey(), pattern))
                return true;
            return false;
        });
    }

    @Override
    public List<String> poly() {
        return List.of();
    }


    @Override
    public fURI basePath() {
        return fURI.of(this.scheme(), this.host(), this.port(), this.path(), cInt.ONE(), List.of(), Map.of());
    }

    @Override
    public boolean test(final fURI lhs) {
        final C c = this.c();
        final C d = lhs.c();
        //if (c.isZero() && d.isZero())
        //    return true;
        if (c.within(d)) { // no need to check path as its noobj
            if (c.isZero())
                return true;
        } else
            return false;
        if (!lhs.hasPattern() && !this.hasPattern()) {
            if (!this.name().equals(lhs.name()))
                return false;
        }
        /*if (!Objects.equals(this.poly(), lhs.poly())) {
            if (null != this.poly() && null != lhs.poly()) {
                for (int i = 0; i < lhs.poly().size(); i++) {
                    final ifURI rp = ifURI.of(lhs.poly().get(i));
                    if (rp.toString().equals("#"))
                        break;
                    if (i >= this.poly().size())
                        return false;
                    final ifURI lp = ifURI.of(this.poly().get(i));
                    if (!lp.test(rp))
                        return false;
                }
            }
        }*/
        if (lhs.toString().equals("#"))
            return true;
        if (lhs.hasScheme() && Objects.equals(lhs.scheme(), "#"))
            return true;
        if (!Objects.equals(this.scheme(), lhs.scheme()) && !Objects.equals(lhs.scheme(), "+"))
            return false;
        if (lhs.hasHost() && Objects.equals(lhs.host(), "#"))
            return true;
        if (!Objects.equals(this.host(), lhs.host()) && !Objects.equals(lhs.host(), "+"))
            return false;
        if (!(lhs.port() == -1) || !Objects.equals(lhs.host(), "+"))
            if (this.port() != -1 && (lhs.port() == -1 || (lhs.port() != 0 && this.port() != lhs.port())))
                return false;
        if (!lhs.hasPattern())
            return this.path().equals(lhs.path());
        if (this.isAbsolute() != lhs.isAbsolute())
            return false;
        // if (this.path().getFirst().isEmpty() != lhs.path().getFirst().isEmpty())
        //    return false;
        //if (!Objects.equals(this.host, other.host) && !Objects.equals(other.host, ONE_WILD_STRING))
        //    return false;
        //if (this.path.isEmpty() && other.toString().contains("#"))
        //   return true;
        for (int i = 0; i < lhs.path().size(); i++) {
            if (lhs.path().get(i).equals("#")) // #
                return true;
            if (!lhs.path().get(i).equals("+")) {
                if (this.pathLength() <= i) // a/b a/b/c
                    return false;
                else if (!this.path().get(i).equals(lhs.path().get(i))) // a a
                    return false;
            }  // +
        }
        return this.path().size() == lhs.path().size();// && this.path().getLast().isEmpty() == lhs.path().getLast().isEmpty();
    }

    @Override
    public fURI prepend(final String segment) {
        if (segment.isEmpty() && this.path().getFirst().isEmpty())
            return this;
        final List<String> newPath = new ArrayList<>();
        final List<String> prefix = Arrays.asList(segment.split("/"));
        if ((segment.startsWith("/") && !this.pathString().startsWith("/")) || this.hasHost())
            newPath.add("");
        newPath.addAll(prefix);
        //   if (segment.endsWith("/"))
        //  newPath.add("");
        newPath.addAll(this.path().getFirst().isEmpty() ? this.path().subList(1, this.path().size()) : this.path());
        return fURI.of(this.scheme(), this.host(), this.port(), newPath, this.c(), List.of(), this.qMap());
    }

    @Override
    public fURI extend(final fURI segment) {
        return this.extend(segment.toString());
    }

    @Override
    public fURI extend(final String segment) {
        if (segment.isEmpty() && !this.path().isEmpty() && this.path().getLast().isEmpty())
            return this;
        final List<String> newPath = new ArrayList<>(this.path());
        final List<String> prefix = Arrays.asList(segment.split("/"));
        if (!this.path().isEmpty() && this.path().getLast().isEmpty())
            newPath.removeLast();
        if (this.path().isEmpty() && segment.startsWith("/"))
            newPath.add("");
        newPath.addAll(prefix);
        if (segment.endsWith("/"))
            newPath.add("");
        return fURI.of(this.scheme(), this.host(), this.port(), newPath, this.c(), List.of(), this.qMap());
    }

    @Override
    public boolean hasPrefix(final fURI prefix) {
        if (prefix.hasScheme() && (!this.hasScheme() || !this.scheme().equals(prefix.scheme())))
            return false;
        if (prefix.hasAuthority() && (!this.hasAuthority() || !this.authority().test(prefix.authority())))
            return false;
        final List<String> prefixSegments = prefix.path();
        if (prefixSegments.size() > this.path().size())
            return false;
        for (int i = 0; i < prefixSegments.size(); i++) {
            final String prefixSegment = prefixSegments.get(i);
            if (prefixSegment.equals("#"))
                return true;
            if (prefixSegment.isEmpty() || prefixSegment.equals("+"))
                continue;
            final String pathSegment = this.path().get(i);
            if (!Objects.equals(prefixSegment, pathSegment))
                return false;
        }
        return true;
    }

    @Override
    public boolean hasPostfix(fURI postfix) {
        return this.toString().endsWith(postfix.toString());
    }

    @Override
    public fURI pretract(final String segment) {
        if (segment.isEmpty() && this.path().getFirst().isEmpty())
            return fURI.of(this.scheme(), this.host(), this.port(), this.path().subList(1, this.path().size()), this.c(), List.of(), this.qMap());
        if (this.hasPrefix(fURI.of(segment)))
            return fURI.of(this.scheme(), this.host(), this.port(), this.path().subList(segment.split("/").length, this.path().size()), this.c(), List.of(), this.qMap());
        return this;
    }

    private boolean hasBlankCap(final boolean prefix) {
        return !this.path().isEmpty() && (prefix ? this.path().getFirst().isEmpty() : this.path().getLast().isEmpty());
    }

    @Override
    public fURI pretract(final int steps) {
        if (steps == 0)
            return this;
        if (steps >= this.pathLength())
            return fURI.of(this.scheme(), this.host(), this.port(), List.of(), this.c(), List.of(), this.qMap());
        boolean hasBlank = this.hasBlankCap(true);
        List<String> newPath = new ArrayList<>(this.path());
        if (hasBlank) newPath.removeFirst();
        for (int i = steps; i < newPath.size(); i++) {
            newPath.removeFirst();
        }
        if (hasBlank) newPath.addFirst("");
        if (newPath.stream().allMatch(String::isEmpty)) {
            newPath.clear();
        }
        return fURI.of(this.scheme(), this.host(), this.port(), newPath, this.c(), List.of(), this.qMap());
    }


    @Override
    public fURI retract(int steps) {
        if (steps == 0)
            return this;
        if (steps >= this.pathLength())
            return fURI.of(this.scheme(), this.host(), this.port(), List.of(), this.c(), List.of(), this.qMap());
        boolean hasBlank = this.hasBlankCap(false);
        List<String> newPath = new ArrayList<>(this.path());
        if (hasBlank) newPath.removeLast();
        for (int i = 0; i < steps; i++) {
            newPath.removeLast();
        }
        if (hasBlank) newPath.addLast("");
        if (newPath.stream().allMatch(String::isEmpty)) {
            newPath.clear();
        }
        return fURI.of(this.scheme(), this.host(), this.port(), newPath, this.c(), List.of(), this.qMap());
    }

    @Override
    public fURI retractPattern() {
        final List<String> newPath = new ArrayList<>(this.path());
        while (!newPath.isEmpty() && (newPath.getLast().equals("#") || newPath.getLast().equals("+"))) {
            newPath.removeLast();
        }
        return fURI.of(this.scheme(), this.host(), this.port(), newPath, this.c(), List.of(), this.qMap());
    }

    @Override
    public fURI retract(final String segment) {


        //   if (segment.isEmpty() && this.path().getLast().isEmpty())
        //    return ifURI.of(this.scheme(), this.host(), this.port(), this.path().subList(0, this.path().size() - 1), this.c(), List.of(), this.qMap());
        if (this.hasPrefix(fURI.of(segment)))
            return fURI.of(this.scheme(), this.host(), this.port(), this.path().subList(0, this.path().size() - segment.split("/").length), this.c(), List.of(), this.qMap());
        return this;
    }


    @Override
    public C c() {
        return cInt.ONE();
    }

    @Override
    public fURI mult(final fURI rhs) {
        final List<String> segments = new ArrayList<>(this.path());
        segments.addAll(rhs.path());
        return fURI.of(this.scheme(), this.host(), this.port(), segments, this.c().mult(rhs.c()), List.of(), Map.of());
    }

    @Override
    public fURI plus(final fURI rhs) {
        final List<String> segments = new ArrayList<>(this.path());
        segments.addAll(rhs.path());
        return fURI.of(this.scheme(), this.host(), this.port(), segments, this.c().plus(rhs.c()), List.of(), Map.of());
    }


    @Override
    public fURI dom() {
        final String d = this.q("dom");
        if (null == d)
            return ALL;
        return fURI.of(d);
    }


    @Override
    public fURI rng() {
        final String r = this.q("rng");
        if (null == r)
            return ALL;
        return fURI.of(r);
    }


    @Override
    public String qString() {
        return String.join("&", this.qMap().entrySet().stream().map(e -> e.getKey() + (e.getValue().isEmpty() ? "" : ("=" + e.getValue()))).toList());
    }

    @Override
    public Map<String, String> qMap() {
        return Map.of();
    }

    @Override
    public fURI q(final String key, final Object value) {
        final Map<String, String> newQ = new HashMap<>(this.qMap());
        newQ.put(key, null == value ? "" : value.toString());
        return fURI.of(this.scheme(), this.host(), this.port(), this.path(), this.c(), List.of(), newQ);
    }

    @Override
    public <T> T qValue(final String key, final Class<T> valueClass) {
        final String value = this.q(key);
        if (null == value)
            return null;
        if (String.class.isAssignableFrom(valueClass))
            return (T) value;
        else if (fURI.class.isAssignableFrom(valueClass))
            return (T) fURI.of(value);
        else if (Integer.class.isAssignableFrom(valueClass))
            return (T) Integer.valueOf(value);
        else if (Long.class.isAssignableFrom(valueClass))
            return (T) Long.valueOf(value);
        else if (Double.class.isAssignableFrom(valueClass))
            return (T) Double.valueOf(value);
        else if (Boolean.class.isAssignableFrom(valueClass))
            return (T) Boolean.valueOf(value);
        else
            throw MTronException.of("no known conversion of %s to %s", value, valueClass);
    }


    @Override
    public String q(final String key) {
        if (null == key)
            return null;
        return this.qMap().get(key);
    }

    @Override
    public boolean isRelative() {
        return !this.path().isEmpty() && !this.path().getFirst().isEmpty();
    }

    @Override
    public boolean isBranch() {
        return this.path().isEmpty() || this.path().getLast().isEmpty();
    }

    @Override
    public String pathString() {
        return String.join("/", this.path());
    }


    @Override
    public fURI head(final int steps) {
        if (steps == 0)
            return fURI.of(this.scheme(), this.host(), this.port(), List.of(), this.c(), List.of(), this.qMap());
        if (steps >= this.pathLength())
            return this;
        boolean hasBlankRight = this.hasBlankCap(false);
        boolean hasBlankLeft = this.hasBlankCap(true);
        final List<String> newPath = new ArrayList<>(this.path().subList(0, steps + (hasBlankLeft ? 1 : 0)));
        if (hasBlankRight && !newPath.isEmpty() && !newPath.getLast().isEmpty())
            newPath.addLast("");
        return fURI.of(this.scheme(), this.host(), this.port(), newPath, this.c(), List.of(), this.qMap());
        // return ifURI.of(this.scheme(), this.host(), this.port(), this.path().subList(0, steps + (hasBlank ? 1 : 0)), this.c(), List.of(), this.qMap());
    }

    @Override
    public fURI tail(final int steps) {
        if (steps == 0)
            return fURI.of(this.scheme(), this.host(), this.port(), List.of(), this.c(), List.of(), this.qMap());
        if (steps >= this.pathLength())
            return this;
        boolean hasBlankRight = this.hasBlankCap(false);
        boolean hasBlankLeft = this.hasBlankCap(true);
        final List<String> newPath = new ArrayList<>(this.path().subList(((this.path().size() - steps) - (hasBlankRight ? 1 : 0)), this.path().size()));
        if (hasBlankLeft && !newPath.isEmpty() && !newPath.getFirst().isEmpty())
            newPath.addFirst("");
        return fURI.of(this.scheme(), this.host(), this.port(), newPath, this.c(), List.of(), this.qMap());
    }

    public int hashCode() {
        return Objects.hash(this.scheme(), this.host(), this.port(), this.path(), this.c(), this.qMap());
        //return this.toString().hashCode();
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        if (!scheme().isEmpty())
            sb.append(scheme()).append(":");
        if (!host().isEmpty()) {
            sb.append("//");
            sb.append(host());
            if (-1 != port())
                sb.append(":").append(port());
        }
        sb.append(path().stream().collect(Collectors.joining("/")));
        if (!c().isOne())
            sb.append("{").append(c().toString()).append("}");
        if (!qMap().isEmpty())
            sb.append("?").append(qString());
        return sb.toString();
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof fURI that))
            return false;
        return Objects.equals(this.scheme(), that.scheme())
                && Objects.equals(this.host(), that.host())
                && this.port() == that.port()
                && Objects.equals(this.path(), that.path())
                && Objects.equals(this.c(), that.c())
                && Objects.equals(this.qMap(), that.qMap());
    }

}
