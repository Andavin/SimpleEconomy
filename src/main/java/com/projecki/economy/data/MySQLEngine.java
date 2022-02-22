package com.projecki.economy.data;

import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.jooq.Record;
import org.jooq.*;
import org.jooq.conf.MappedSchema;
import org.jooq.conf.RenderMapping;
import org.jooq.conf.Settings;
import org.jooq.impl.DSL;
import org.tinylog.Logger;

import javax.sql.DataSource;
import java.util.Properties;
import java.util.concurrent.*;
import java.util.function.Consumer;

import static org.jooq.impl.DSL.using;

/**
 * A simple engine built to provide simple methods for
 * interfacing with MySQL. This class provided methods
 * to interface with jOOQ in order to have a simple building
 * tool for SQL syntax and statements.
 * <p>
 * Several shortcut methods are provided:
 * <ul>
 *     <li>{@link #selectFrom(Table)}</li>
 *     <li>{@link #insertInto(Table)}</li>
 *     <li>{@link #update(Table)}</li>
 *     <li>{@link #deleteFrom(Table)}</li>
 * </ul>
 * These methods should cover the bases of basic SQL needs
 * for simple syntax. However, more complex methods are available
 * using the {@link #create()} method to get a new {@link DSLContext}
 * and furthermore the methods provided in {@link DSL} can be
 * used to make use of the full jOOQ API.
 */
public class MySQLEngine implements DataEngine {

    private static final ExecutorService EXECUTOR = new ThreadPoolExecutor(2, 10,
            60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(), new ThreadFactoryBuilder()
            .setNameFormat("MySQL - %d").setUncaughtExceptionHandler((thread, error) -> error.printStackTrace()).build());

    private final Properties properties;
    private final HikariDataSource dataSource;
    private final Settings settings = new Settings().withExecuteLogging(false);

    public MySQLEngine(Properties properties) {

        this.properties = properties;
        String property = properties.getProperty("dataSource.mappings");
        if (property != null) {

            RenderMapping renderMapping = new RenderMapping();
            String[] mappings = property.split(",");
            for (String mapping : mappings) {

                String[] entry = mapping.split(":");
                if (entry.length == 2) {
                    renderMapping.getSchemata().add(new MappedSchema()
                            .withInput(entry[0]).withOutput(entry[1]));
                } else {
                    Logger.warn("Invalid entry in datasource mappings in MySQL property file: {}", mapping);
                }
            }

            if (!renderMapping.getSchemata().isEmpty()) {
                this.settings.setRenderMapping(renderMapping);
            }
        }
        // Must initialize the jdbcUrl here to allow connection
        // Set it as a property because the config below then gets the
        // method "set" + "j".toUpperCase() + "dbcUrl" = "setJdbcUrl"
        // and passes in this value which we need to happen in the constructor
        properties.setProperty("driverClassName", "com.mysql.jdbc.Driver");
        properties.setProperty("jdbcUrl", String.format("jdbc:mysql://%s:%s/%s",
                properties.getProperty("dataSource.serverName"),
                properties.getProperty("dataSource.portNumber"),
                properties.getProperty("dataSource.databaseName")
        ));

        HikariConfig config = new HikariConfig(properties);
        this.dataSource = new HikariDataSource(config);
    }

    /**
     * Create a new {@link DSLContext} with a simple configuration using a
     * {@link DataSource data source}, {@link SQLDialect#MARIADB MariaDB}
     * and an asynchronous {@link Executor executor provider}.
     *
     * @return The newly created DSLContext instance.
     */
    public DSLContext create() {
        DSLContext context = using(dataSource, SQLDialect.MYSQL);
        context.configuration().set(EXECUTOR).set(settings);
        return context;
    }

    public <R extends Record> SelectWhereStep<R> selectFrom(Table<R> table) {
        return this.create().selectFrom(table);
    }

    /**
     * Create a new DSL select statement.
     * <p>
     * This creates an attached, renderable and executable <code>SELECT</code>
     * statement from this {@link DSLContext}. If you don't need to render or
     * execute this <code>SELECT</code> statement (e.g. because you want to
     * create a subselect), consider using the static
     * {@link DSL#select(SelectFieldOrAsterisk...)} instead.
     * <p>
     * Example: <code><pre>
     * DSLContext create = DSL.using(configuration);
     *
     * create.select(field1, field2)
     *       .from(table1)
     *       .join(table2).on(field1.equal(field2))
     *       .where(field1.greaterThan(100))
     *       .orderBy(field2)
     *       .execute();
     * </pre></code>
     * <p>
     * Note that passing an empty collection conveniently produces
     * <code>SELECT *</code> semantics, i.e. it:
     * <ul>
     * <li>Renders <code>SELECT tab1.col1, tab1.col2, ..., tabN.colN</code> if
     * all columns are known</li>
     * <li>Renders <code>SELECT *</code> if not all columns are known, e.g. when
     * using plain SQL</li>
     * </ul>
     *
     * @see DSL#select(SelectFieldOrAsterisk...)
     */
    public SelectSelectStep<Record> select(SelectFieldOrAsterisk... fields) {
        return this.create().select(fields);
    }

    /**
     * Create a new DSL select statement.
     * <p>
     * This is the same as {@link #select(SelectFieldOrAsterisk...)}, except that it
     * declares additional record-level typesafety, which is needed by
     * {@link Field#in(Select)}, {@link Field#equal(Select)} and other predicate
     * building methods taking subselect arguments.
     * <p>
     * This creates an attached, renderable and executable <code>SELECT</code>
     * statement from this {@link DSLContext}. If you don't need to render or
     * execute this <code>SELECT</code> statement (e.g. because you want to
     * create a subselect), consider using the static
     * {@link DSL#select(SelectField)} instead.
     * <p>
     * Example: <code><pre>
     * using(configuration)
     *       .select(field1)
     *       .from(table1)
     *       .join(table2).on(field1.equal(field2))
     *       .where(field1.greaterThan(100))
     *       .orderBy(field2);
     * </pre></code>
     *
     * @see DSL#selectDistinct(SelectFieldOrAsterisk...)
     */
    public <T1> SelectSelectStep<Record1<T1>> select(SelectField<T1> field1) {
        return this.create().select(field1);
    }

    /**
     * Create a new DSL select statement.
     * <p>
     * This is the same as {@link #select(SelectFieldOrAsterisk...)}, except that it
     * declares additional record-level typesafety, which is needed by
     * {@link Row2#in(Select)}, {@link Row2#equal(Select)} and other predicate
     * building methods taking subselect arguments.
     * <p>
     * This creates an attached, renderable and executable <code>SELECT</code>
     * statement from this {@link DSLContext}. If you don't need to render or
     * execute this <code>SELECT</code> statement (e.g. because you want to
     * create a subselect), consider using the static
     * {@link DSL#select(SelectField, SelectField)} instead.
     * <p>
     * Example: <code><pre>
     * using(configuration)
     *       .select(field1, field2)
     *       .from(table1)
     *       .join(table2).on(field1.equal(field2))
     *       .where(field1.greaterThan(100))
     *       .orderBy(field2);
     * </pre></code>
     *
     * @see DSL#selectDistinct(SelectFieldOrAsterisk...)
     */
    public <T1, T2> SelectSelectStep<Record2<T1, T2>> select(SelectField<T1> field1, SelectField<T2> field2) {
        return this.create().select(field1, field2);
    }

    /**
     * Create a new DSL select statement.
     * <p>
     * This is the same as {@link #select(SelectFieldOrAsterisk...)}, except that it
     * declares additional record-level typesafety, which is needed by
     * {@link Row3#in(Select)}, {@link Row3#equal(Select)} and other predicate
     * building methods taking subselect arguments.
     * <p>
     * This creates an attached, renderable and executable <code>SELECT</code>
     * statement from this {@link DSLContext}. If you don't need to render or
     * execute this <code>SELECT</code> statement (e.g. because you want to
     * create a subselect), consider using the static
     * {@link DSL#select(SelectField, SelectField, SelectField)} instead.
     * <p>
     * Example: <code><pre>
     * using(configuration)
     *       .select(field1, field2, field3)
     *       .from(table1)
     *       .join(table2).on(field1.equal(field2))
     *       .where(field1.greaterThan(100))
     *       .orderBy(field2);
     * </pre></code>
     *
     * @see DSL#selectDistinct(SelectFieldOrAsterisk...)
     */
    public <T1, T2, T3> SelectSelectStep<Record3<T1, T2, T3>> select(SelectField<T1> field1, SelectField<T2> field2, SelectField<T3> field3) {
        return this.create().select(field1, field2, field3);
    }

    /**
     * Create a new DSL select statement.
     * <p>
     * This is the same as {@link #select(SelectFieldOrAsterisk...)}, except that it
     * declares additional record-level typesafety, which is needed by
     * {@link Row4#in(Select)}, {@link Row4#equal(Select)} and other predicate
     * building methods taking subselect arguments.
     * <p>
     * This creates an attached, renderable and executable <code>SELECT</code>
     * statement from this {@link DSLContext}. If you don't need to render or
     * execute this <code>SELECT</code> statement (e.g. because you want to
     * create a subselect), consider using the static
     * {@link DSL#select(SelectField, SelectField, SelectField, SelectField)} instead.
     * <p>
     * Example: <code><pre>
     * using(configuration)
     *       .select(field1, field2, field3, field4)
     *       .from(table1)
     *       .join(table2).on(field1.equal(field2))
     *       .where(field1.greaterThan(100))
     *       .orderBy(field2);
     * </pre></code>
     *
     * @see DSL#selectDistinct(SelectFieldOrAsterisk...) {
    return this.create().select(field1);
    }
     */
    public <T1, T2, T3, T4> SelectSelectStep<Record4<T1, T2, T3, T4>> select(SelectField<T1> field1, SelectField<T2> field2, SelectField<T3> field3, SelectField<T4> field4) {
        return this.create().select(field1, field2, field3, field4);
    }

    /**
     * Create a new DSL select statement.
     * <p>
     * This is the same as {@link #select(SelectFieldOrAsterisk...)}, except that it
     * declares additional record-level typesafety, which is needed by
     * {@link Row5#in(Select)}, {@link Row5#equal(Select)} and other predicate
     * building methods taking subselect arguments.
     * <p>
     * This creates an attached, renderable and executable <code>SELECT</code>
     * statement from this {@link DSLContext}. If you don't need to render or
     * execute this <code>SELECT</code> statement (e.g. because you want to
     * create a subselect), consider using the static
     * {@link DSL#select(SelectField, SelectField, SelectField, SelectField, SelectField)} instead.
     * <p>
     * Example: <code><pre>
     * using(configuration)
     *       .select(field1, field2, field3, field4, field5)
     *       .from(table1)
     *       .join(table2).on(field1.equal(field2))
     *       .where(field1.greaterThan(100))
     *       .orderBy(field2);
     * </pre></code>
     *
     * @see DSL#selectDistinct(SelectFieldOrAsterisk...) {
    return this.create().select(field1);
    }
     */
    public <T1, T2, T3, T4, T5> SelectSelectStep<Record5<T1, T2, T3, T4, T5>> select(SelectField<T1> field1, SelectField<T2> field2, SelectField<T3> field3, SelectField<T4> field4, SelectField<T5> field5) {
        return this.create().select(field1, field2, field3, field4, field5);
    }

    /**
     * Create a new DSL select statement.
     * <p>
     * This is the same as {@link #select(SelectFieldOrAsterisk...)}, except that it
     * declares additional record-level typesafety, which is needed by
     * {@link Row6#in(Select)}, {@link Row6#equal(Select)} and other predicate
     * building methods taking subselect arguments.
     * <p>
     * This creates an attached, renderable and executable <code>SELECT</code>
     * statement from this {@link DSLContext}. If you don't need to render or
     * execute this <code>SELECT</code> statement (e.g. because you want to
     * create a subselect), consider using the static
     * {@link DSL#select(SelectField, SelectField, SelectField, SelectField, SelectField, SelectField)} instead.
     * <p>
     * Example: <code><pre>
     * using(configuration)
     *       .select(field1, field2, field3, .., field5, field6)
     *       .from(table1)
     *       .join(table2).on(field1.equal(field2))
     *       .where(field1.greaterThan(100))
     *       .orderBy(field2);
     * </pre></code>
     *
     * @see DSL#selectDistinct(SelectFieldOrAsterisk...) {
    return this.create().select(field1);
    }
     */
    public <T1, T2, T3, T4, T5, T6> SelectSelectStep<Record6<T1, T2, T3, T4, T5, T6>> select(SelectField<T1> field1, SelectField<T2> field2, SelectField<T3> field3, SelectField<T4> field4, SelectField<T5> field5, SelectField<T6> field6) {
        return this.create().select(field1, field2, field3, field4, field5, field6);
    }

    /**
     * Create a new DSL select statement.
     * <p>
     * This is the same as {@link #select(SelectFieldOrAsterisk...)}, except that it
     * declares additional record-level typesafety, which is needed by
     * {@link Row7#in(Select)}, {@link Row7#equal(Select)} and other predicate
     * building methods taking subselect arguments.
     * <p>
     * This creates an attached, renderable and executable <code>SELECT</code>
     * statement from this {@link DSLContext}. If you don't need to render or
     * execute this <code>SELECT</code> statement (e.g. because you want to
     * create a subselect), consider using the static
     * {@link DSL#select(SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField)} instead.
     * <p>
     * Example: <code><pre>
     * using(configuration)
     *       .select(field1, field2, field3, .., field6, field7)
     *       .from(table1)
     *       .join(table2).on(field1.equal(field2))
     *       .where(field1.greaterThan(100))
     *       .orderBy(field2);
     * </pre></code>
     *
     * @see DSL#selectDistinct(SelectFieldOrAsterisk...) {
    return this.create().select(field1);
    }
     */
    public <T1, T2, T3, T4, T5, T6, T7> SelectSelectStep<Record7<T1, T2, T3, T4, T5, T6, T7>> select(SelectField<T1> field1, SelectField<T2> field2, SelectField<T3> field3, SelectField<T4> field4, SelectField<T5> field5, SelectField<T6> field6, SelectField<T7> field7) {
        return this.create().select(field1, field2, field3, field4, field5, field6, field7);
    }

    /**
     * Create a new DSL select statement.
     * <p>
     * This is the same as {@link #select(SelectFieldOrAsterisk...)}, except that it
     * declares additional record-level typesafety, which is needed by
     * {@link Row8#in(Select)}, {@link Row8#equal(Select)} and other predicate
     * building methods taking subselect arguments.
     * <p>
     * This creates an attached, renderable and executable <code>SELECT</code>
     * statement from this {@link DSLContext}. If you don't need to render or
     * execute this <code>SELECT</code> statement (e.g. because you want to
     * create a subselect), consider using the static
     * {@link DSL#select(SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField)} instead.
     * <p>
     * Example: <code><pre>
     * using(configuration)
     *       .select(field1, field2, field3, .., field7, field8)
     *       .from(table1)
     *       .join(table2).on(field1.equal(field2))
     *       .where(field1.greaterThan(100))
     *       .orderBy(field2);
     * </pre></code>
     *
     * @see DSL#selectDistinct(SelectFieldOrAsterisk...) {
    return this.create().select(field1);
    }
     */
    public <T1, T2, T3, T4, T5, T6, T7, T8> SelectSelectStep<Record8<T1, T2, T3, T4, T5, T6, T7, T8>> select(SelectField<T1> field1, SelectField<T2> field2, SelectField<T3> field3, SelectField<T4> field4, SelectField<T5> field5, SelectField<T6> field6, SelectField<T7> field7, SelectField<T8> field8) {
        return this.create().select(field1, field2, field3, field4, field5, field6, field7, field8);
    }

    /**
     * Create a new DSL select statement.
     * <p>
     * This is the same as {@link #select(SelectFieldOrAsterisk...)}, except that it
     * declares additional record-level typesafety, which is needed by
     * {@link Row9#in(Select)}, {@link Row9#equal(Select)} and other predicate
     * building methods taking subselect arguments.
     * <p>
     * This creates an attached, renderable and executable <code>SELECT</code>
     * statement from this {@link DSLContext}. If you don't need to render or
     * execute this <code>SELECT</code> statement (e.g. because you want to
     * create a subselect), consider using the static
     * {@link DSL#select(SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField)} instead.
     * <p>
     * Example: <code><pre>
     * using(configuration)
     *       .select(field1, field2, field3, .., field8, field9)
     *       .from(table1)
     *       .join(table2).on(field1.equal(field2))
     *       .where(field1.greaterThan(100))
     *       .orderBy(field2);
     * </pre></code>
     *
     * @see DSL#selectDistinct(SelectFieldOrAsterisk...) {
    return this.create().select(field1);
    }
     */
    public <T1, T2, T3, T4, T5, T6, T7, T8, T9> SelectSelectStep<Record9<T1, T2, T3, T4, T5, T6, T7, T8, T9>> select(SelectField<T1> field1, SelectField<T2> field2, SelectField<T3> field3, SelectField<T4> field4, SelectField<T5> field5, SelectField<T6> field6, SelectField<T7> field7, SelectField<T8> field8, SelectField<T9> field9) {
        return this.create().select(field1, field2, field3, field4, field5, field6, field7, field8, field9);
    }

    /**
     * Create a new DSL select statement.
     * <p>
     * This is the same as {@link #select(SelectFieldOrAsterisk...)}, except that it
     * declares additional record-level typesafety, which is needed by
     * {@link Row10#in(Select)}, {@link Row10#equal(Select)} and other predicate
     * building methods taking subselect arguments.
     * <p>
     * This creates an attached, renderable and executable <code>SELECT</code>
     * statement from this {@link DSLContext}. If you don't need to render or
     * execute this <code>SELECT</code> statement (e.g. because you want to
     * create a subselect), consider using the static
     * {@link DSL#select(SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField)} instead.
     * <p>
     * Example: <code><pre>
     * using(configuration)
     *       .select(field1, field2, field3, .., field9, field10)
     *       .from(table1)
     *       .join(table2).on(field1.equal(field2))
     *       .where(field1.greaterThan(100))
     *       .orderBy(field2);
     * </pre></code>
     *
     * @see DSL#selectDistinct(SelectFieldOrAsterisk...) {
    return this.create().select(field1);
    }
     */
    public <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> SelectSelectStep<Record10<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10>> select(SelectField<T1> field1, SelectField<T2> field2, SelectField<T3> field3, SelectField<T4> field4, SelectField<T5> field5, SelectField<T6> field6, SelectField<T7> field7, SelectField<T8> field8, SelectField<T9> field9, SelectField<T10> field10) {
        return this.create().select(field1, field2, field3, field4, field5, field6, field7, field8, field9, field10);
    }

    /**
     * Create a new DSL select statement.
     * <p>
     * This is the same as {@link #select(SelectFieldOrAsterisk...)}, except that it
     * declares additional record-level typesafety, which is needed by
     * {@link Row11#in(Select)}, {@link Row11#equal(Select)} and other predicate
     * building methods taking subselect arguments.
     * <p>
     * This creates an attached, renderable and executable <code>SELECT</code>
     * statement from this {@link DSLContext}. If you don't need to render or
     * execute this <code>SELECT</code> statement (e.g. because you want to
     * create a subselect), consider using the static
     * {@link DSL#select(SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField)} instead.
     * <p>
     * Example: <code><pre>
     * using(configuration)
     *       .select(field1, field2, field3, .., field10, field11)
     *       .from(table1)
     *       .join(table2).on(field1.equal(field2))
     *       .where(field1.greaterThan(100))
     *       .orderBy(field2);
     * </pre></code>
     *
     * @see DSL#selectDistinct(SelectFieldOrAsterisk...) {
    return this.create().select(field1);
    }
     */
    public <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11> SelectSelectStep<Record11<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11>> select(SelectField<T1> field1, SelectField<T2> field2, SelectField<T3> field3, SelectField<T4> field4, SelectField<T5> field5, SelectField<T6> field6, SelectField<T7> field7, SelectField<T8> field8, SelectField<T9> field9, SelectField<T10> field10, SelectField<T11> field11) {
        return this.create().select(field1, field2, field3, field4, field5, field6, field7, field8, field9, field10, field11);
    }

    /**
     * Create a new DSL select statement.
     * <p>
     * This is the same as {@link #select(SelectFieldOrAsterisk...)}, except that it
     * declares additional record-level typesafety, which is needed by
     * {@link Row12#in(Select)}, {@link Row12#equal(Select)} and other predicate
     * building methods taking subselect arguments.
     * <p>
     * This creates an attached, renderable and executable <code>SELECT</code>
     * statement from this {@link DSLContext}. If you don't need to render or
     * execute this <code>SELECT</code> statement (e.g. because you want to
     * create a subselect), consider using the static
     * {@link DSL#select(SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField)} instead.
     * <p>
     * Example: <code><pre>
     * using(configuration)
     *       .select(field1, field2, field3, .., field11, field12)
     *       .from(table1)
     *       .join(table2).on(field1.equal(field2))
     *       .where(field1.greaterThan(100))
     *       .orderBy(field2);
     * </pre></code>
     *
     * @see DSL#selectDistinct(SelectFieldOrAsterisk...) {
    return this.create().select(field1);
    }
     */
    public <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12> SelectSelectStep<Record12<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12>> select(SelectField<T1> field1, SelectField<T2> field2, SelectField<T3> field3, SelectField<T4> field4, SelectField<T5> field5, SelectField<T6> field6, SelectField<T7> field7, SelectField<T8> field8, SelectField<T9> field9, SelectField<T10> field10, SelectField<T11> field11, SelectField<T12> field12) {
        return this.create().select(field1, field2, field3, field4, field5, field6, field7, field8, field9, field10, field11, field12);
    }

    /**
     * Create a new DSL select statement.
     * <p>
     * This is the same as {@link #select(SelectFieldOrAsterisk...)}, except that it
     * declares additional record-level typesafety, which is needed by
     * {@link Row13#in(Select)}, {@link Row13#equal(Select)} and other predicate
     * building methods taking subselect arguments.
     * <p>
     * This creates an attached, renderable and executable <code>SELECT</code>
     * statement from this {@link DSLContext}. If you don't need to render or
     * execute this <code>SELECT</code> statement (e.g. because you want to
     * create a subselect), consider using the static
     * {@link DSL#select(SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField)} instead.
     * <p>
     * Example: <code><pre>
     * using(configuration)
     *       .select(field1, field2, field3, .., field12, field13)
     *       .from(table1)
     *       .join(table2).on(field1.equal(field2))
     *       .where(field1.greaterThan(100))
     *       .orderBy(field2);
     * </pre></code>
     *
     * @see DSL#selectDistinct(SelectFieldOrAsterisk...) {
    return this.create().select(field1);
    }
     */
    public <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13> SelectSelectStep<Record13<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13>> select(SelectField<T1> field1, SelectField<T2> field2, SelectField<T3> field3, SelectField<T4> field4, SelectField<T5> field5, SelectField<T6> field6, SelectField<T7> field7, SelectField<T8> field8, SelectField<T9> field9, SelectField<T10> field10, SelectField<T11> field11, SelectField<T12> field12, SelectField<T13> field13) {
        return this.create().select(field1, field2, field3, field4, field5, field6, field7, field8, field9, field10, field11, field12, field13);
    }

    /**
     * Create a new DSL select statement.
     * <p>
     * This is the same as {@link #select(SelectFieldOrAsterisk...)}, except that it
     * declares additional record-level typesafety, which is needed by
     * {@link Row14#in(Select)}, {@link Row14#equal(Select)} and other predicate
     * building methods taking subselect arguments.
     * <p>
     * This creates an attached, renderable and executable <code>SELECT</code>
     * statement from this {@link DSLContext}. If you don't need to render or
     * execute this <code>SELECT</code> statement (e.g. because you want to
     * create a subselect), consider using the static
     * {@link DSL#select(SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField)} instead.
     * <p>
     * Example: <code><pre>
     * using(configuration)
     *       .select(field1, field2, field3, .., field13, field14)
     *       .from(table1)
     *       .join(table2).on(field1.equal(field2))
     *       .where(field1.greaterThan(100))
     *       .orderBy(field2);
     * </pre></code>
     *
     * @see DSL#selectDistinct(SelectFieldOrAsterisk...) {
    return this.create().select(field1);
    }
     */
    public <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14> SelectSelectStep<Record14<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14>> select(SelectField<T1> field1, SelectField<T2> field2, SelectField<T3> field3, SelectField<T4> field4, SelectField<T5> field5, SelectField<T6> field6, SelectField<T7> field7, SelectField<T8> field8, SelectField<T9> field9, SelectField<T10> field10, SelectField<T11> field11, SelectField<T12> field12, SelectField<T13> field13, SelectField<T14> field14) {
        return this.create().select(field1, field2, field3, field4, field5, field6, field7, field8, field9, field10, field11, field12, field13, field14);
    }

    /**
     * Create a new DSL select statement.
     * <p>
     * This is the same as {@link #select(SelectFieldOrAsterisk...)}, except that it
     * declares additional record-level typesafety, which is needed by
     * {@link Row15#in(Select)}, {@link Row15#equal(Select)} and other predicate
     * building methods taking subselect arguments.
     * <p>
     * This creates an attached, renderable and executable <code>SELECT</code>
     * statement from this {@link DSLContext}. If you don't need to render or
     * execute this <code>SELECT</code> statement (e.g. because you want to
     * create a subselect), consider using the static
     * {@link DSL#select(SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField)} instead.
     * <p>
     * Example: <code><pre>
     * using(configuration)
     *       .select(field1, field2, field3, .., field14, field15)
     *       .from(table1)
     *       .join(table2).on(field1.equal(field2))
     *       .where(field1.greaterThan(100))
     *       .orderBy(field2);
     * </pre></code>
     *
     * @see DSL#selectDistinct(SelectFieldOrAsterisk...) {
    return this.create().select(field1);
    }
     */
    public <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15> SelectSelectStep<Record15<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15>> select(SelectField<T1> field1, SelectField<T2> field2, SelectField<T3> field3, SelectField<T4> field4, SelectField<T5> field5, SelectField<T6> field6, SelectField<T7> field7, SelectField<T8> field8, SelectField<T9> field9, SelectField<T10> field10, SelectField<T11> field11, SelectField<T12> field12, SelectField<T13> field13, SelectField<T14> field14, SelectField<T15> field15) {
        return this.create().select(field1, field2, field3, field4, field5, field6, field7, field8, field9, field10, field11, field12, field13, field14, field15);
    }

    /**
     * Create a new DSL select statement.
     * <p>
     * This is the same as {@link #select(SelectFieldOrAsterisk...)}, except that it
     * declares additional record-level typesafety, which is needed by
     * {@link Row16#in(Select)}, {@link Row16#equal(Select)} and other predicate
     * building methods taking subselect arguments.
     * <p>
     * This creates an attached, renderable and executable <code>SELECT</code>
     * statement from this {@link DSLContext}. If you don't need to render or
     * execute this <code>SELECT</code> statement (e.g. because you want to
     * create a subselect), consider using the static
     * {@link DSL#select(SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField)} instead.
     * <p>
     * Example: <code><pre>
     * using(configuration)
     *       .select(field1, field2, field3, .., field15, field16)
     *       .from(table1)
     *       .join(table2).on(field1.equal(field2))
     *       .where(field1.greaterThan(100))
     *       .orderBy(field2);
     * </pre></code>
     *
     * @see DSL#selectDistinct(SelectFieldOrAsterisk...) {
    return this.create().select(field1);
    }
     */
    public <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16> SelectSelectStep<Record16<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16>> select(SelectField<T1> field1, SelectField<T2> field2, SelectField<T3> field3, SelectField<T4> field4, SelectField<T5> field5, SelectField<T6> field6, SelectField<T7> field7, SelectField<T8> field8, SelectField<T9> field9, SelectField<T10> field10, SelectField<T11> field11, SelectField<T12> field12, SelectField<T13> field13, SelectField<T14> field14, SelectField<T15> field15, SelectField<T16> field16) {
        return this.create().select(field1, field2, field3, field4, field5, field6, field7, field8, field9, field10, field11, field12, field13, field14, field15, field16);
    }

    /**
     * Create a new DSL select statement.
     * <p>
     * This is the same as {@link #select(SelectFieldOrAsterisk...)}, except that it
     * declares additional record-level typesafety, which is needed by
     * {@link Row17#in(Select)}, {@link Row17#equal(Select)} and other predicate
     * building methods taking subselect arguments.
     * <p>
     * This creates an attached, renderable and executable <code>SELECT</code>
     * statement from this {@link DSLContext}. If you don't need to render or
     * execute this <code>SELECT</code> statement (e.g. because you want to
     * create a subselect), consider using the static
     * {@link DSL#select(SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField)} instead.
     * <p>
     * Example: <code><pre>
     * using(configuration)
     *       .select(field1, field2, field3, .., field16, field17)
     *       .from(table1)
     *       .join(table2).on(field1.equal(field2))
     *       .where(field1.greaterThan(100))
     *       .orderBy(field2);
     * </pre></code>
     *
     * @see DSL#selectDistinct(SelectFieldOrAsterisk...) {
    return this.create().select(field1);
    }
     */
    public <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17> SelectSelectStep<Record17<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17>> select(SelectField<T1> field1, SelectField<T2> field2, SelectField<T3> field3, SelectField<T4> field4, SelectField<T5> field5, SelectField<T6> field6, SelectField<T7> field7, SelectField<T8> field8, SelectField<T9> field9, SelectField<T10> field10, SelectField<T11> field11, SelectField<T12> field12, SelectField<T13> field13, SelectField<T14> field14, SelectField<T15> field15, SelectField<T16> field16, SelectField<T17> field17) {
        return this.create().select(field1, field2, field3, field4, field5, field6, field7, field8, field9, field10, field11, field12, field13, field14, field15, field16, field17);
    }

    /**
     * Create a new DSL select statement.
     * <p>
     * This is the same as {@link #select(SelectFieldOrAsterisk...)}, except that it
     * declares additional record-level typesafety, which is needed by
     * {@link Row18#in(Select)}, {@link Row18#equal(Select)} and other predicate
     * building methods taking subselect arguments.
     * <p>
     * This creates an attached, renderable and executable <code>SELECT</code>
     * statement from this {@link DSLContext}. If you don't need to render or
     * execute this <code>SELECT</code> statement (e.g. because you want to
     * create a subselect), consider using the static
     * {@link DSL#select(SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField)} instead.
     * <p>
     * Example: <code><pre>
     * using(configuration)
     *       .select(field1, field2, field3, .., field17, field18)
     *       .from(table1)
     *       .join(table2).on(field1.equal(field2))
     *       .where(field1.greaterThan(100))
     *       .orderBy(field2);
     * </pre></code>
     *
     * @see DSL#selectDistinct(SelectFieldOrAsterisk...) {
    return this.create().select(field1);
    }
     */
    public <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18> SelectSelectStep<Record18<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18>> select(SelectField<T1> field1, SelectField<T2> field2, SelectField<T3> field3, SelectField<T4> field4, SelectField<T5> field5, SelectField<T6> field6, SelectField<T7> field7, SelectField<T8> field8, SelectField<T9> field9, SelectField<T10> field10, SelectField<T11> field11, SelectField<T12> field12, SelectField<T13> field13, SelectField<T14> field14, SelectField<T15> field15, SelectField<T16> field16, SelectField<T17> field17, SelectField<T18> field18) {
        return this.create().select(field1, field2, field3, field4, field5, field6, field7, field8, field9, field10, field11, field12, field13, field14, field15, field16, field17, field18);
    }

    /**
     * Create a new DSL select statement.
     * <p>
     * This is the same as {@link #select(SelectFieldOrAsterisk...)}, except that it
     * declares additional record-level typesafety, which is needed by
     * {@link Row19#in(Select)}, {@link Row19#equal(Select)} and other predicate
     * building methods taking subselect arguments.
     * <p>
     * This creates an attached, renderable and executable <code>SELECT</code>
     * statement from this {@link DSLContext}. If you don't need to render or
     * execute this <code>SELECT</code> statement (e.g. because you want to
     * create a subselect), consider using the static
     * {@link DSL#select(SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField)} instead.
     * <p>
     * Example: <code><pre>
     * using(configuration)
     *       .select(field1, field2, field3, .., field18, field19)
     *       .from(table1)
     *       .join(table2).on(field1.equal(field2))
     *       .where(field1.greaterThan(100))
     *       .orderBy(field2);
     * </pre></code>
     *
     * @see DSL#selectDistinct(SelectFieldOrAsterisk...) {
    return this.create().select(field1);
    }
     */
    public <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19> SelectSelectStep<Record19<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19>> select(SelectField<T1> field1, SelectField<T2> field2, SelectField<T3> field3, SelectField<T4> field4, SelectField<T5> field5, SelectField<T6> field6, SelectField<T7> field7, SelectField<T8> field8, SelectField<T9> field9, SelectField<T10> field10, SelectField<T11> field11, SelectField<T12> field12, SelectField<T13> field13, SelectField<T14> field14, SelectField<T15> field15, SelectField<T16> field16, SelectField<T17> field17, SelectField<T18> field18, SelectField<T19> field19) {
        return this.create().select(field1, field2, field3, field4, field5, field6, field7, field8, field9, field10, field11, field12, field13, field14, field15, field16, field17, field18, field19);
    }

    /**
     * Create a new DSL select statement.
     * <p>
     * This is the same as {@link #select(SelectFieldOrAsterisk...)}, except that it
     * declares additional record-level typesafety, which is needed by
     * {@link Row20#in(Select)}, {@link Row20#equal(Select)} and other predicate
     * building methods taking subselect arguments.
     * <p>
     * This creates an attached, renderable and executable <code>SELECT</code>
     * statement from this {@link DSLContext}. If you don't need to render or
     * execute this <code>SELECT</code> statement (e.g. because you want to
     * create a subselect), consider using the static
     * {@link DSL#select(SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField)} instead.
     * <p>
     * Example: <code><pre>
     * using(configuration)
     *       .select(field1, field2, field3, .., field19, field20)
     *       .from(table1)
     *       .join(table2).on(field1.equal(field2))
     *       .where(field1.greaterThan(100))
     *       .orderBy(field2);
     * </pre></code>
     *
     * @see DSL#selectDistinct(SelectFieldOrAsterisk...) {
    return this.create().select(field1);
    }
     */
    public <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20> SelectSelectStep<Record20<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20>> select(SelectField<T1> field1, SelectField<T2> field2, SelectField<T3> field3, SelectField<T4> field4, SelectField<T5> field5, SelectField<T6> field6, SelectField<T7> field7, SelectField<T8> field8, SelectField<T9> field9, SelectField<T10> field10, SelectField<T11> field11, SelectField<T12> field12, SelectField<T13> field13, SelectField<T14> field14, SelectField<T15> field15, SelectField<T16> field16, SelectField<T17> field17, SelectField<T18> field18, SelectField<T19> field19, SelectField<T20> field20) {
        return this.create().select(field1, field2, field3, field4, field5, field6, field7, field8, field9, field10, field11, field12, field13, field14, field15, field16, field17, field18, field19, field20);
    }

    /**
     * Create a new DSL select statement.
     * <p>
     * This is the same as {@link #select(SelectFieldOrAsterisk...)}, except that it
     * declares additional record-level typesafety, which is needed by
     * {@link Row21#in(Select)}, {@link Row21#equal(Select)} and other predicate
     * building methods taking subselect arguments.
     * <p>
     * This creates an attached, renderable and executable <code>SELECT</code>
     * statement from this {@link DSLContext}. If you don't need to render or
     * execute this <code>SELECT</code> statement (e.g. because you want to
     * create a subselect), consider using the static
     * {@link DSL#select(SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField)} instead.
     * <p>
     * Example: <code><pre>
     * using(configuration)
     *       .select(field1, field2, field3, .., field20, field21)
     *       .from(table1)
     *       .join(table2).on(field1.equal(field2))
     *       .where(field1.greaterThan(100))
     *       .orderBy(field2);
     * </pre></code>
     *
     * @see DSL#selectDistinct(SelectFieldOrAsterisk...) {
    return this.create().select(field1);
    }
     */
    public <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21> SelectSelectStep<Record21<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21>> select(SelectField<T1> field1, SelectField<T2> field2, SelectField<T3> field3, SelectField<T4> field4, SelectField<T5> field5, SelectField<T6> field6, SelectField<T7> field7, SelectField<T8> field8, SelectField<T9> field9, SelectField<T10> field10, SelectField<T11> field11, SelectField<T12> field12, SelectField<T13> field13, SelectField<T14> field14, SelectField<T15> field15, SelectField<T16> field16, SelectField<T17> field17, SelectField<T18> field18, SelectField<T19> field19, SelectField<T20> field20, SelectField<T21> field21) {
        return this.create().select(field1, field2, field3, field4, field5, field6, field7, field8, field9, field10, field11, field12, field13, field14, field15, field16, field17, field18, field19, field20, field21);
    }

    /**
     * Create a new DSL select statement.
     * <p>
     * This is the same as {@link #select(SelectFieldOrAsterisk...)}, except that it
     * declares additional record-level typesafety, which is needed by
     * {@link Row22#in(Select)}, {@link Row22#equal(Select)} and other predicate
     * building methods taking subselect arguments.
     * <p>
     * This creates an attached, renderable and executable <code>SELECT</code>
     * statement from this {@link DSLContext}. If you don't need to render or
     * execute this <code>SELECT</code> statement (e.g. because you want to
     * create a subselect), consider using the static
     * {@link DSL#select(SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField, SelectField)} instead.
     * <p>
     * Example: <code><pre>
     * using(configuration)
     *       .select(field1, field2, field3, .., field21, field22)
     *       .from(table1)
     *       .join(table2).on(field1.equal(field2))
     *       .where(field1.greaterThan(100))
     *       .orderBy(field2);
     * </pre></code>
     *
     * @see DSL#selectDistinct(SelectFieldOrAsterisk...) {
    return this.create().select(field1);
    }
     */
    public <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, T22> SelectSelectStep<Record22<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, T22>> select(SelectField<T1> field1, SelectField<T2> field2, SelectField<T3> field3, SelectField<T4> field4, SelectField<T5> field5, SelectField<T6> field6, SelectField<T7> field7, SelectField<T8> field8, SelectField<T9> field9, SelectField<T10> field10, SelectField<T11> field11, SelectField<T12> field12, SelectField<T13> field13, SelectField<T14> field14, SelectField<T15> field15, SelectField<T16> field16, SelectField<T17> field17, SelectField<T18> field18, SelectField<T19> field19, SelectField<T20> field20, SelectField<T21> field21, SelectField<T22> field22) {
        return this.create().select(field1, field2, field3, field4, field5, field6, field7, field8, field9, field10, field11, field12, field13, field14, field15, field16, field17, field18, field19, field20, field21, field22);
    }

    /**
     * Create a new DSL insert statement.
     * <p>
     * This type of insert may feel more convenient to some users, as it uses
     * the <code>UPDATE</code> statement's <code>SET a = b</code> syntax.
     * <p>
     * Example: <code><pre>
     * DSLContext create = DSL.using(configuration);
     *
     * create.insertInto(table)
     *       .set(field1, value1)
     *       .set(field2, value2)
     *       .newRecord()
     *       .set(field1, value3)
     *       .set(field2, value4)
     *       .onDuplicateKeyUpdate()
     *       .set(field1, value1)
     *       .set(field2, value2)
     *       .execute();
     * </pre></code>
     */
    public <R extends Record> InsertSetStep<R> insertInto(Table<R> table) {
        return this.create().insertInto(table);
    }

    /**
     * Create a new DSL update statement.
     * <p>
     * Example: <code><pre>
     * DSLContext create = DSL.using(configuration);
     *
     * create.update(table)
     *       .set(field1, value1)
     *       .set(field2, value2)
     *       .where(field1.greaterThan(100))
     *       .execute();
     * </pre></code>
     * <p>
     * Note that some databases support table expressions more complex than
     * simple table references. In CUBRID and MySQL, for instance, you can write
     * <code><pre>
     * create.update(t1.join(t2).on(t1.id.eq(t2.id)))
     *       .set(t1.value, value1)
     *       .set(t2.value, value2)
     *       .where(t1.id.eq(10))
     *       .execute();
     * </pre></code>
     */
    public <R extends Record> UpdateSetFirstStep<R> update(Table<R> table) {
        return this.create().update(table);
    }

    /**
     * Create a new DSL delete statement.
     * <p>
     * Example: <code><pre>
     * DSLContext create = DSL.using(configuration);
     *
     * create.deleteFrom(table)
     *       .where(field1.greaterThan(100))
     *       .execute();
     * </pre></code>
     * <p>
     * Some but not all databases support aliased tables in delete statements.
     */
    public <R extends Record> DeleteWhereStep<R> deleteFrom(Table<R> table) {
        return this.create().deleteFrom(table);
    }

    /**
     * Execute a jOOQ task asynchronously using the dedicated MySQL
     * thread pool. This is used primarily for operations where jOOQ
     * does not provide an {@link Query#executeAsync()} method such
     * as with {@link Batch} or when {@link ResultQuery#fetchOne()}.
     * <p>
     * Note that this method should not be used for operations other
     * than those pertaining to jOOQ and database interactions.
     * <br>
     * Also, note that this should <i>not</i> be used for jOOQ operations
     * that will be performed asynchronously via {@link ResultQuery#fetchAsync()}
     * or {@link Query#executeAsync()} as this would be a pointless
     * waste of resources in duplicating the asynchronous processes.
     *
     * @param runnable The operation to perform on the MySQL dedicated thread.
     */
    public void executeAsync(Runnable runnable) {
        EXECUTOR.execute(() -> {

            try {
                runnable.run();
            } catch (Throwable e) {
                Logger.error(e);
            }
        });
    }

    /**
     * Execute a jOOQ task asynchronously using the dedicated MySQL
     * thread pool. This is used primarily for operations where jOOQ
     * does not provide an {@link Query#executeAsync()} method such
     * as with {@link Batch} or when {@link ResultQuery#fetchOne()}.
     * <p>
     * Note that this method should not be used for operations other
     * than those pertaining to jOOQ and database interactions.
     * <br>
     * Also, note that this should <i>not</i> be used for jOOQ operations
     * that will be performed asynchronously via {@link ResultQuery#fetchAsync()}
     * or {@link Query#executeAsync()} as this would be a pointless
     * waste of resources in duplicating the asynchronous processes.
     *
     * @param consumer The operation to perform on the MySQL dedicated thread.
     */
    public void executeAsync(Consumer<DSLContext> consumer) {
        EXECUTOR.execute(() -> {

            try {
                consumer.accept(this.create());
            } catch (Throwable e) {
                Logger.error(e);
            }
        });
    }

    @Override
    public void close() {
        System.out.println("Finishing up MySQL tasks. This could take a bit...");
        //noinspection UnstableApiUsage
        MoreExecutors.shutdownAndAwaitTermination(EXECUTOR, 5, TimeUnit.MINUTES);
        this.dataSource.close();
    }

    @Override
    public Properties getProperties() {
        return properties;
    }
}
