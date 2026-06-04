/*
 * metatron: a distributed virtual machine and language
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

package studio.phaseshift.metatron.isa.dcmnt.schema.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import studio.phaseshift.metatron.AbstractMetatronTest;
import studio.phaseshift.metatron.furi.fURI;
import studio.phaseshift.metatron.isa.m.type.Type;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static studio.phaseshift.metatron.furi.fURI.Singleton.f;
import static studio.phaseshift.metatron.isa.m.mInstSet.REC_TID;
import static studio.phaseshift.metatron.isa.m.type.impl.MRec.rec;

/**
 * Tests for {@link ExistingCollectionSchema} collection-type dereference
 * and {@link CollectionSchemaInstSet} wiring.
 *
 * <p>Validates that collection dereferences return instset-encoded Types
 * (single source of truth) instead of dcmnt-specific {@code COLLECTION_TID} URIs.
 *
 * <p>These tests do NOT require a MongoDB connection — they test the
 * pure-Java wiring layer ({@code setSchemaInstset}, {@code getCollectionType},
 * {@code getCollectionTypes}) using pre-built {@link Type} objects.
 *
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
@DisplayName("ExistingCollectionSchema — collection type dereference")
public class ExistingCollectionSchemaTest extends AbstractMetatronTest {

    // ── Test data helpers ────────────────────────────────────────────

    /** Build a minimal Type with a given collection-name VID. */
    private static Type collectionType(final String name) {
        return Type.Builder.build()
                .tid(REC_TID)
                .vid(f(name))
                .isaPredicate(rec())
                .create();
    }

    /** Build a {@link CollectionSchemaInstSet} from a list of Types. */
    private static CollectionSchemaInstSet instset(final List<Type> types,
                                                    final String vidPath) {
        return new CollectionSchemaInstSet(f(vidPath), types);
    }

    /** Create an unwired ExistingCollectionSchema (null space, sample=100). */
    private static ExistingCollectionSchema unwiredSchema() {
        return new ExistingCollectionSchema(null, 100);
    }

    // ── setSchemaInstset ─────────────────────────────────────────────

    @Nested
    @DisplayName("setSchemaInstset wiring")
    class SetSchemaInstsetTests {

        @Test
        @DisplayName("after wiring, getCollectionType returns matching Type")
        void afterWiringGetCollectionTypeWorks() {
            final var schema = unwiredSchema();
            final var usersType = collectionType("users");
            final var ordersType = collectionType("orders");
            final var is = instset(List.of(usersType, ordersType),
                    "/m/dcmnt/inst/schema/testdb");

            schema.setSchemaInstset(is);

            assertNotNull(schema.getCollectionType("users"));
            assertEquals(f("users"), schema.getCollectionType("users").vid());
            assertNotNull(schema.getCollectionType("orders"));
            assertEquals(f("orders"), schema.getCollectionType("orders").vid());
        }

        @Test
        @DisplayName("after wiring, getCollectionTypes returns all types")
        void afterWiringGetCollectionTypesReturnsAll() {
            final var schema = unwiredSchema();
            final var is = instset(List.of(
                    collectionType("a"), collectionType("b"), collectionType("c")),
                    "/m/dcmnt/inst/schema/testdb");

            schema.setSchemaInstset(is);

            final List<Type> types = schema.getCollectionTypes();
            assertEquals(3, types.size());
        }
    }

    // ── getCollectionType ────────────────────────────────────────────

    @Nested
    @DisplayName("getCollectionType")
    class GetCollectionTypeTests {

        @Test
        @DisplayName("returns null when schema instset is not wired")
        void returnsNullWhenNotWired() {
            final var schema = unwiredSchema();
            assertNull(schema.getCollectionType("users"));
        }

        @Test
        @DisplayName("returns null for unknown collection")
        void returnsNullForUnknownCollection() {
            final var schema = unwiredSchema();
            schema.setSchemaInstset(instset(
                    List.of(collectionType("users")),
                    "/m/dcmnt/inst/schema/testdb"));

            assertNull(schema.getCollectionType("nonexistent"));
        }

        @Test
        @DisplayName("lookup is case-insensitive")
        void lookupIsCaseInsensitive() {
            final var schema = unwiredSchema();
            schema.setSchemaInstset(instset(
                    List.of(collectionType("Users")),
                    "/m/dcmnt/inst/schema/testdb"));

            assertNotNull(schema.getCollectionType("users"));
            assertNotNull(schema.getCollectionType("USERS"));
            assertNotNull(schema.getCollectionType("Users"));
        }

        @Test
        @DisplayName("returns correct Type for each collection in multi-collection instset")
        void returnsCorrectTypePerCollection() {
            final var schema = unwiredSchema();
            final var productsType = collectionType("products");
            final var reviewsType = collectionType("reviews");
            schema.setSchemaInstset(instset(
                    List.of(productsType, reviewsType),
                    "/m/dcmnt/inst/schema/testdb"));

            final Type p = schema.getCollectionType("products");
            final Type r = schema.getCollectionType("reviews");

            assertNotNull(p);
            assertEquals(f("products"), p.vid());
            assertNotNull(r);
            assertEquals(f("reviews"), r.vid());
            assertNotSame(p, r, "different collections should have different Type objects");
        }

        @Test
        @DisplayName("empty instset returns null for any lookup")
        void emptyInstsetReturnsNull() {
            final var schema = unwiredSchema();
            schema.setSchemaInstset(instset(
                    Collections.emptyList(),
                    "/m/dcmnt/inst/schema/empty"));

            assertNull(schema.getCollectionType("anything"));
        }
    }

    // ── getCollectionTypes ───────────────────────────────────────────

    @Nested
    @DisplayName("getCollectionTypes")
    class GetCollectionTypesTests {

        @Test
        @DisplayName("returns empty list when schema instset is not wired")
        void returnsEmptyListWhenNotWired() {
            final var schema = unwiredSchema();
            final List<Type> types = schema.getCollectionTypes();
            assertNotNull(types);
            assertTrue(types.isEmpty());
        }

        @Test
        @DisplayName("returns empty list when instset has no types")
        void returnsEmptyListForEmptyInstset() {
            final var schema = unwiredSchema();
            schema.setSchemaInstset(instset(
                    Collections.emptyList(),
                    "/m/dcmnt/inst/schema/empty"));

            final List<Type> types = schema.getCollectionTypes();
            assertNotNull(types);
            assertTrue(types.isEmpty());
        }

        @Test
        @DisplayName("returns a defensive copy (mutation does not affect internal state)")
        void returnsDefensiveCopy() {
            final var schema = unwiredSchema();
            schema.setSchemaInstset(instset(
                    List.of(collectionType("users")),
                    "/m/dcmnt/inst/schema/testdb"));

            final List<Type> types1 = schema.getCollectionTypes();
            assertEquals(1, types1.size());

            // Mutate the returned list
            types1.clear();

            // Internal state should be unchanged
            final List<Type> types2 = schema.getCollectionTypes();
            assertEquals(1, types2.size(),
                    "internal state should not be affected by mutating returned list");
        }
    }

    // ── CollectionSchemaInstSet ──────────────────────────────────────

    @Nested
    @DisplayName("CollectionSchemaInstSet")
    class CollectionSchemaInstSetTests {

        @Test
        @DisplayName("correct VID is accessible")
        void correctVidIsAccessible() {
            final var is = instset(List.of(collectionType("users")),
                    "/m/dcmnt/inst/schema/mydb");
            assertEquals(f("/m/dcmnt/inst/schema/mydb"), is.vid());
        }

        @Test
        @DisplayName("types() returns all provided types")
        void typesReturnsAllProvidedTypes() {
            final var t1 = collectionType("users");
            final var t2 = collectionType("orders");
            final var is = instset(List.of(t1, t2), "/m/dcmnt/inst/schema/testdb");

            final var types = is.types();
            assertEquals(2, types.size());
            assertTrue(types.contains(t1));
            assertTrue(types.contains(t2));
        }

        @Test
        @DisplayName("empty types list is valid")
        void emptyTypesListIsValid() {
            final var is = instset(Collections.emptyList(),
                    "/m/dcmnt/inst/schema/empty");
            assertNotNull(is);
            assertTrue(is.types().isEmpty());
        }
    }

    // ── Combined: wiring + lookup ────────────────────────────────────

    @Test
    @DisplayName("single-collection instset round-trip: set → getCollectionType → getCollectionTypes")
    void singleCollectionRoundTrip() {
        final var schema = unwiredSchema();
        final var t = collectionType("inventory");
        schema.setSchemaInstset(instset(List.of(t), "/m/dcmnt/inst/schema/testdb"));

        // Exact lookup works
        final Type found = schema.getCollectionType("inventory");
        assertNotNull(found);
        assertEquals(t.vid(), found.vid());

        // Collection list has exactly one entry
        final List<Type> all = schema.getCollectionTypes();
        assertEquals(1, all.size());
        assertEquals(t.vid(), all.get(0).vid());
    }

    @Test
    @DisplayName("switching instset: set a second instset replaces the first")
    void switchingInstsetReplacesPrevious() {
        final var schema = unwiredSchema();
        schema.setSchemaInstset(instset(
                List.of(collectionType("old")),
                "/m/dcmnt/inst/schema/old"));

        // Replace with new instset
        schema.setSchemaInstset(instset(
                List.of(collectionType("new")),
                "/m/dcmnt/inst/schema/new"));

        assertNull(schema.getCollectionType("old"),
                "old collection should no longer be found");
        assertNotNull(schema.getCollectionType("new"));
        assertEquals(1, schema.getCollectionTypes().size());
    }

    @Test
    @DisplayName("many collections (100) are all retrievable")
    void manyCollectionsAreAllRetrievable() {
        final var schema = unwiredSchema();
        final List<Type> types = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            types.add(collectionType("col_" + i));
        }
        schema.setSchemaInstset(instset(types, "/m/dcmnt/inst/schema/big"));

        assertEquals(100, schema.getCollectionTypes().size());
        assertNotNull(schema.getCollectionType("col_0"));
        assertNotNull(schema.getCollectionType("col_50"));
        assertNotNull(schema.getCollectionType("col_99"));
        assertNull(schema.getCollectionType("col_100"));
    }
}
