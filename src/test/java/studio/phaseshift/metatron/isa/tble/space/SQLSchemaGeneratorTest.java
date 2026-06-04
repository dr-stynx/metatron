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

package studio.phaseshift.metatron.isa.tble.space;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import studio.phaseshift.metatron.AbstractMetatronTest;
import studio.phaseshift.metatron.furi.fURI;
import studio.phaseshift.metatron.isa.m.type.Type;

import java.sql.Types;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static studio.phaseshift.metatron.furi.fURI.Singleton.f;

/**
 * Tests for {@link SQLSchemaGenerator} — FK registration, type generation,
 * and schema instset creation.
 *
 * <p>These tests validate that the schema generator serves as the single
 * source of truth for FK relationships, replacing the old dual-representation
 * pattern ({@code ForeignKeyMetadata} + native schema rec).
 *
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
@DisplayName("SQLSchemaGenerator")
public class SQLSchemaGeneratorTest extends AbstractMetatronTest {

    // ── Test data helpers ────────────────────────────────────────────

    private static ExistingTableSchema.ColumnMetadata col(final String name,
                                                           final int sqlType,
                                                           final String typeName) {
        return new ExistingTableSchema.ColumnMetadata(name, sqlType, typeName);
    }

    private static ExistingTableSchema.TableMetadata table(final String name,
                                                            final List<ExistingTableSchema.ColumnMetadata> columns) {
        return new ExistingTableSchema.TableMetadata(null, name, columns, List.of("id"));
    }

    private static SQLSchemaGenerator generator(final List<ExistingTableSchema.TableMetadata> tables) {
        return new SQLSchemaGenerator(tables, f("/m/tble/inst/schema/testdb"), "testdb");
    }

    /** Shorthand: create a generator with one trivial table (for pure FK tests). */
    private static SQLSchemaGenerator emptyGenerator() {
        final List<ExistingTableSchema.ColumnMetadata> cols = List.of(
                col("id", Types.INTEGER, "INTEGER")
        );
        return generator(List.of(table("dummy", cols)));
    }

    // ── FKTarget record ──────────────────────────────────────────────

    @Nested
    @DisplayName("FKTarget record")
    class FKTargetTests {

        @Test
        @DisplayName("should store target path")
        void shouldStoreTargetPath() {
            final SQLSchemaGenerator.FKTarget fk =
                    new SQLSchemaGenerator.FKTarget("office/+/officeCode");
            assertEquals("office/+/officeCode", fk.targetPath());
        }

        @Test
        @DisplayName("should equal another FKTarget with same path")
        void shouldEqualSamePath() {
            final var a = new SQLSchemaGenerator.FKTarget("a/+/b");
            final var b = new SQLSchemaGenerator.FKTarget("a/+/b");
            assertEquals(a, b);
            assertEquals(a.hashCode(), b.hashCode());
        }

        @Test
        @DisplayName("should not equal FKTarget with different path")
        void shouldNotEqualDifferentPath() {
            final var a = new SQLSchemaGenerator.FKTarget("a/+/b");
            final var b = new SQLSchemaGenerator.FKTarget("x/+/y");
            assertNotEquals(a, b);
        }
    }

    // ── FK registration / lookup ─────────────────────────────────────

    @Nested
    @DisplayName("FK registration and lookup")
    class FKRegistrationTests {

        @Test
        @DisplayName("registerFK stores FK and getFKTarget retrieves it")
        void registerAndLookupSimple() {
            final var gen = emptyGenerator();
            gen.registerFK("employee", "org_id", "org", "id");

            final SQLSchemaGenerator.FKTarget fk = gen.getFKTarget("employee", "org_id");
            assertNotNull(fk, "FK should be registered");
            assertEquals("org/+/id", fk.targetPath());
        }

        @Test
        @DisplayName("getFKTarget returns null for non-FK column")
        void lookupMissReturnsNull() {
            final var gen = emptyGenerator();
            assertNull(gen.getFKTarget("employee", "name"));
        }

        @Test
        @DisplayName("getFKTarget returns null for unknown table")
        void lookupUnknownTableReturnsNull() {
            final var gen = emptyGenerator();
            assertNull(gen.getFKTarget("nonexistent", "col"));
        }

        @Test
        @DisplayName("FK lookup is case-insensitive for table and column names")
        void lookupIsCaseInsensitive() {
            final var gen = emptyGenerator();
            gen.registerFK("Employee", "Org_Id", "Org", "id");

            assertNotNull(gen.getFKTarget("employee", "org_id"));
            assertNotNull(gen.getFKTarget("EMPLOYEE", "ORG_ID"));
            assertNotNull(gen.getFKTarget("Employee", "Org_Id"));
        }

        @Test
        @DisplayName("last registerFK wins for duplicate key (overwrite)")
        void lastWriteWins() {
            final var gen = emptyGenerator();
            gen.registerFK("employee", "org_id", "org", "id");
            gen.registerFK("employee", "org_id", "company", "cid");

            final var fk = gen.getFKTarget("employee", "org_id");
            assertEquals("company/+/cid", fk.targetPath());
        }

        @Test
        @DisplayName("registerFK with cross-schema target (contains ':') produces correct path")
        void crossSchemaFK() {
            final var gen = emptyGenerator();
            gen.registerFK("employee", "location", "g:vertices", "vid");

            final var fk = gen.getFKTarget("employee", "location");
            assertNotNull(fk);
            assertEquals("g:vertices/+/vid", fk.targetPath());
        }
    }

    // ── getFKLookup ──────────────────────────────────────────────────

    @Nested
    @DisplayName("getFKLookup")
    class FKLookupMapTests {

        @Test
        @DisplayName("returns unmodifiable view of all registered FKs")
        void returnsUnmodifiableView() {
            final var gen = emptyGenerator();
            gen.registerFK("a", "x", "b", "y");
            gen.registerFK("c", "z", "d", "w");

            final Map<String, SQLSchemaGenerator.FKTarget> map = gen.getFKLookup();
            assertEquals(2, map.size());
            assertTrue(map.containsKey("a.x"));
            assertTrue(map.containsKey("c.z"));

            assertThrows(UnsupportedOperationException.class, () -> map.put("new", null));
        }
    }

    // ── Table type generation ────────────────────────────────────────

    @Nested
    @DisplayName("Table type generation")
    class TableTypeGenerationTests {

        @Test
        @DisplayName("getTableTypes lazily generates types on first call and caches")
        void lazyGenerationAndCaching() {
            final var columns = List.of(
                    col("id", Types.INTEGER, "INTEGER"),
                    col("name", Types.VARCHAR, "VARCHAR")
            );
            final var t = table("users", columns);
            final var gen = generator(List.of(t));

            // First call triggers generation
            final Collection<Type> types = gen.getTableTypes();
            assertEquals(1, types.size());
            final Type userType = types.iterator().next();
            assertNotNull(userType);
            assertTrue(userType.vid().toString().endsWith("/users"),
                    "VID should end with /users, got: " + userType.vid());

            // Second call returns same collection instance (cached)
            final Collection<Type> types2 = gen.getTableTypes();
            assertSame(types, types2, "second call should return same cached collection");
        }

        @Test
        @DisplayName("getTableType triggers lazy init and returns correct type")
        void getTableTypeTriggersLazyInit() {
            final var columns = List.of(
                    col("id", Types.INTEGER, "INTEGER"),
                    col("score", Types.REAL, "REAL")
            );
            final var t = table("scores", columns);
            final var gen = generator(List.of(t));

            final Type scoreType = gen.getTableType("scores");
            assertNotNull(scoreType);
            assertTrue(scoreType.vid().toString().endsWith("/scores"),
                    "VID should end with /scores, got: " + scoreType.vid());
        }

        @Test
        @DisplayName("getTableType returns null for unknown table")
        void getTableTypeUnknownReturnsNull() {
            final var gen = emptyGenerator();
            assertNull(gen.getTableType("nonexistent"));
        }

        @Test
        @DisplayName("getTableType is case-insensitive")
        void getTableTypeCaseInsensitive() {
            final var columns = List.of(col("id", Types.INTEGER, "INTEGER"));
            final var t = table("Users", columns);
            final var gen = generator(List.of(t));

            assertNotNull(gen.getTableType("users"));
            assertNotNull(gen.getTableType("USERS"));
            assertNotNull(gen.getTableType("Users"));
        }

        @Test
        @DisplayName("FK column has an FKTarget in the generator")
        void fkColumnIsTrackedInGenerator() {
            final var columns = List.of(
                    col("id", Types.INTEGER, "INTEGER"),
                    col("dept_id", Types.INTEGER, "INTEGER")
            );
            final var t = table("employee", columns);
            final var gen = generator(List.of(t));

            gen.registerFK("employee", "dept_id", "department", "id");

            // The FK should be queryable from the generator
            final var fk = gen.getFKTarget("employee", "dept_id");
            assertNotNull(fk, "FK should be registered in generator");
            assertEquals("department/+/id", fk.targetPath());

            // The non-FK column should have no FKTarget
            assertNull(gen.getFKTarget("employee", "id"),
                    "id column should not have an FK target");
        }

        @Test
        @DisplayName("table type exists for all registered tables")
        void tableTypeExistsForAllTables() {
            final var cols1 = List.of(col("id", Types.INTEGER, "INTEGER"));
            final var cols2 = List.of(col("id", Types.INTEGER, "INTEGER"),
                                       col("name", Types.VARCHAR, "VARCHAR"));
            final var gen = generator(List.of(
                    table("users", cols1),
                    table("orders", cols2)
            ));

            assertNotNull(gen.getTableType("users"));
            assertNotNull(gen.getTableType("orders"));
            assertEquals(2, gen.getTableTypes().size());
        }
    }

    // ── Schema instset generation ────────────────────────────────────

    @Nested
    @DisplayName("Schema instset generation")
    class SchemaInstSetTests {

        @Test
        @DisplayName("generateSchemaInstset creates instset with correct VID")
        void generatesInstsetWithCorrectVid() {
            final var columns = List.of(col("id", Types.INTEGER, "INTEGER"));
            final var gen = generator(List.of(table("users", columns)));

            final fURI schemaVid = f("/m/tble/inst/schema/test");
            final SQLSchemaInstSet instset = gen.generateSchemaInstset(schemaVid);

            assertNotNull(instset);
            assertEquals(schemaVid, instset.vid());
        }

        @Test
        @DisplayName("generateSchemaInstset contains all table types")
        void instsetContainsAllTableTypes() {
            final var cols1 = List.of(col("id", Types.INTEGER, "INTEGER"));
            final var cols2 = List.of(col("id", Types.INTEGER, "INTEGER"),
                                       col("name", Types.VARCHAR, "VARCHAR"));
            final var gen = generator(List.of(table("users", cols1), table("orders", cols2)));

            final SQLSchemaInstSet instset =
                    gen.generateSchemaInstset(f("/m/tble/inst/schema/test"));

            assertNotNull(instset);
            assertEquals(2, instset.types().size());
        }

        @Test
        @DisplayName("empty table list produces instset with no types")
        void emptyTableListProducesEmptyInstset() {
            final var gen = generator(Collections.emptyList());
            final SQLSchemaInstSet instset =
                    gen.generateSchemaInstset(f("/m/tble/inst/schema/empty"));

            assertNotNull(instset);
            assertTrue(instset.types().isEmpty());
        }
    }

    // ── getSchemaBasePath ────────────────────────────────────────────

    @Test
    @DisplayName("getSchemaBasePath returns the base path provided at construction")
    void schemaBasePathIsPreserved() {
        final var gen = new SQLSchemaGenerator(
                Collections.emptyList(),
                f("/custom/schema/path"),
                "customdb"
        );
        assertEquals(f("/custom/schema/path"), gen.getSchemaBasePath());
    }

    // ── Multiple FKs ─────────────────────────────────────────────────

    @Test
    @DisplayName("multiple FKs on same table are all registered")
    void multipleFKsOnSameTable() {
        final var gen = emptyGenerator();
        gen.registerFK("employee", "org_id", "org", "id");
        gen.registerFK("employee", "manager_id", "employee", "id");
        gen.registerFK("employee", "dept_id", "department", "id");

        assertNotNull(gen.getFKTarget("employee", "org_id"));
        assertNotNull(gen.getFKTarget("employee", "manager_id"));
        assertNotNull(gen.getFKTarget("employee", "dept_id"));
        assertNull(gen.getFKTarget("employee", "name"));

        assertEquals(3, gen.getFKLookup().size());
    }

    // ── Edge cases ───────────────────────────────────────────────────

    @Test
    @DisplayName("generator with empty table list still supports FK operations")
    void emptyTableListStillSupportsFK() {
        final var gen = new SQLSchemaGenerator(Collections.emptyList(), f("/schema/path"));

        // FK operations should work independently of tables
        gen.registerFK("a", "b", "c", "d");
        assertNotNull(gen.getFKTarget("a", "b"));
        assertEquals("c/+/d", gen.getFKTarget("a", "b").targetPath());

        // Type operations should return empty/null
        assertTrue(gen.getTableTypes().isEmpty());
        assertNull(gen.getTableType("anything"));
    }
}
