package org.wzm.interceptor;

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLLimit;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.statement.SQLSelect;
import com.alibaba.druid.sql.ast.statement.SQLSelectQueryBlock;
import com.alibaba.druid.sql.ast.statement.SQLSelectStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlHintStatement;
import com.alibaba.druid.sql.dialect.mysql.parser.MySqlStatementParser;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlSchemaStatVisitor;
import com.alibaba.druid.stat.TableStat;
import com.alibaba.druid.util.JdbcUtils;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.jdbc.SQL;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * <p>
 * DeepPager 分页拦截器<br/>
 * 用途：将普通limit分页SQL重写为内连接LIMIT方式的SQL,利用覆盖索引提高性能,例如:
 * select id,hq_id,sn,status from table where hq_id=1 and status=2 order by id desc limit 0,100   ====>
 * select t1.id,t1.hq_id,t1.sn,t1.status from table as t1,(select id from table where hq_id=1 and status=2 order by id desc limit 0,100) as t2 where hq_id=1 and t1.id=t2.id
 * 特别注意：<br/>
 * 1.拦截器只对增加了分页前缀标签的SQL进行重写，未增加分页前缀标签的SQL不受影响；<br/>
 * 2.支持两种SQL前缀标签，分库分表:PREFIX_PAGER_SHARD,单表：PREFIX_PAGER_SINGLE (见代码中的常量)；<br/>
 * 3.该拦截器仅适应于简单查询，不支持join、group by、聚合函数；<br/>
 * 4.需要重写的SQL，必须保证：主键字段是id。如果重写的是分库分表的SQL,还必须保证分表字段是hq_id,且hq_id在SQL中只能精确匹配；<br/>
 * 5.在重写过程中如果出现异常，会中断对SQL的重写，继续保持原流程；<br/>
 * 6.SQL中的排序字段如果没有在索引中,则无法使用覆盖索引,会影响SQL优化的效果。因此建议尽量保证在排序字段上有索引。
 * @author wangzhiming
 */
@Component
@Intercepts(@Signature(type = Executor.class, method = "query", args = { MappedStatement.class, Object.class,
                                                                         RowBounds.class, ResultHandler.class}))
@Slf4j
public class DeepPagerInterceptor implements Interceptor {
    private static final String PREFIX_PAGER_SHARD = "/*deep-pager:shard*/";

    private static final String PREFIX_PAGER_SINGLE = "/*deep-pager:single*/";

    private static final int MAPPED_STATEMENT_INDEX = 0;

    private static final int PARAMETER_INDEX = 1;

    private static final String HQ_ID = "hqId";

    private static final String SELECT = "select";

    private static final String FROM = "from";

    @Override
    public Object intercept(Invocation invocation) throws Throwable {

        //如果需要则重写SQL
        reWriteSqlIfNeeded(invocation);

        //执行后续流程
        return invocation.proceed();
    }

    /**
     * 如果需要则重写SQL
     *
     * @param invocation
     */
    private void reWriteSqlIfNeeded(Invocation invocation) {
        try {
            //获取SQL
            Object[] queryArgs = invocation.getArgs();
            MappedStatement mappedStatement = (MappedStatement) queryArgs[MAPPED_STATEMENT_INDEX];
            Object parameter = queryArgs[PARAMETER_INDEX];
            BoundSql boundSql = mappedStatement.getBoundSql(parameter);
            String sql = boundSql.getSql();

            //如果不包含分页的前缀则返回
            if (!prefixWithPager(sql)) {
                return;
            }

            //过滤掉不支持的SQL语法
            if (notSupport(getSqlNoPrefix(sql))) {
                return;
            }

            //是否是单表查询(非分库分表)
            boolean singleTable = sql.contains(PREFIX_PAGER_SINGLE);

            //获取改写后的新SQL
            String newSql = getNewSql(getSqlNoPrefix(sql), singleTable);
            //验证新SQL合法性
            checkSql(newSql);

            //获取新的MappedStatement
            MappedStatement newMappedStatement = getNewMappedStatement(mappedStatement, boundSql, newSql, parameter,
                                                                       singleTable);
            //将新的MappedStatement作为查询参数
            queryArgs[MAPPED_STATEMENT_INDEX] = newMappedStatement;

        } catch (Exception e) {
            log.error("SQL重写异常,降级不做重写,不影响业务.invocation:{}, e:{}", invocation, e);
        }

    }

    /**
     * 获取一个新的MappedStatement
     *
     * @param mappedStatement
     * @param boundSql
     * @param newSql
     * @param parameter
     * @param singleTable
     * @return
     */
    private MappedStatement getNewMappedStatement(MappedStatement mappedStatement, BoundSql boundSql, String newSql,
                                                  Object parameter, boolean singleTable) {
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();

        //单表查询,复制出一个新的MappedStatement
        if (singleTable) {
            return copyFromNewSql(mappedStatement, boundSql, newSql, parameterMappings, parameter);
        } else {
            //由于增加了分表字段的查询条件，需要增加一个ParameterMapping
            List<ParameterMapping> newParameterMappings = Lists.newArrayList(parameterMappings);
            newParameterMappings.add(findHqIdParameterMapping(parameterMappings));
            return copyFromNewSql(mappedStatement, boundSql, newSql, newParameterMappings, parameter);
        }

    }

    /**
     * SQL是否包含分页前缀
     *
     * @param sql
     * @return
     */
    private boolean prefixWithPager(String sql) {
        if (sql.contains(PREFIX_PAGER_SINGLE) && sql.contains(PREFIX_PAGER_SHARD)) {
            log.error("SQL中包含同时包含分库分表和单店的前缀,不予处理,sql:{}", sql);
        }

        return sql.contains(PREFIX_PAGER_SINGLE) || sql.contains(PREFIX_PAGER_SHARD);
    }

    /**
     * 不支持的SQL
     *
     * @param sql
     * @return
     */
    private boolean notSupport(String sql) {

        List<SQLStatement> sqlStatements = SQLUtils.parseStatements(sql, JdbcUtils.MYSQL);

        for (SQLStatement sqlStatement : sqlStatements) {

            //zebra的hint则跳过
            if (sqlStatement instanceof MySqlHintStatement) {
                continue;
            }

            //如果不是SELECT语句则不支持
            if (!(sqlStatement instanceof SQLSelectStatement)) {
                log.error("SQL不是SELECT语句,不予支持,sql:{}", sql);
                return true;
            }

            //如果不含LIMIT语句则不支持
            SQLSelectStatement selectStatement = (SQLSelectStatement) sqlStatement;
            SQLSelect sqlSelect = selectStatement.getSelect();
            SQLSelectQueryBlock sqlSelectQuery = (SQLSelectQueryBlock) sqlSelect.getQuery();
            SQLLimit limit = sqlSelectQuery.getLimit();
            if (Objects.isNull(limit)) {
                log.error("SQL中使用了分页前缀但不存在LIMIT语句,不予支持,sql:{}", sql);
                return true;
            }

            //使用访问器
            MySqlSchemaStatVisitor visitor = new MySqlSchemaStatVisitor();
            selectStatement.accept(visitor);

            //检查是否有多表关联关系
            if (CollectionUtils.isNotEmpty(visitor.getRelationships())) {
                log.error("SQL中包含多表关联,不予支持,sql:{}", sql);
                return true;
            }

            //检查是否有Group By语句
            if (CollectionUtils.isNotEmpty(visitor.getGroupByColumns())) {
                log.error("SQL中包含Group By,不予支持,sql:{}", sql);
                return true;
            }

            //检查是否有聚合函数
            if (CollectionUtils.isNotEmpty(visitor.getAggregateFunctions())) {
                log.error("SQL中包含聚合函数,不予支持,sql:{}", sql);
                return true;
            }
        }

        return false;
    }

    /**
     * 查询hqId的ParameterMapping
     *
     * @param parameterMappings
     * @return
     */
    private ParameterMapping findHqIdParameterMapping(List<ParameterMapping> parameterMappings) {

        List<ParameterMapping> parameterMappingsHqId = parameterMappings.stream()
                .filter(parameterMapping -> parameterMapping.getProperty().equals(HQ_ID))
                .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(parameterMappingsHqId)) {
            throw new RuntimeException();
        }

        return parameterMappingsHqId.get(0);
    }

    /**
     * 检查SQL合法性
     *
     * @param sql
     */
    private void checkSql(String sql) {
        MySqlStatementParser parser = new MySqlStatementParser(sql);
        try {
            parser.parseStatementList();
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

    /**
     * 获取新的SQL
     *
     * @param sql
     * @return
     */
    private String getNewSql(String sql, boolean singleTable) {
        List<SQLStatement> sqlStatements = SQLUtils.parseStatements(sql, JdbcUtils.MYSQL);

        String newColumns = StringUtils.EMPTY;
        String newTable = StringUtils.EMPTY;

        //获取列名、表名
        for (SQLStatement sqlStatement : sqlStatements) {

            if (sqlStatement instanceof MySqlHintStatement) {
                continue;
            }

            if (sqlStatement instanceof SQLSelectStatement) {
                MySqlSchemaStatVisitor visitor = new MySqlSchemaStatVisitor();
                sqlStatement.accept(visitor);

                List<String> columns = visitor.getColumns()
                        .stream()
                        .map(TableStat.Column::getName)
                        .distinct()
                        .collect(Collectors.toList());

                //列名上增加列表前缀，避免冲突
                newColumns = StringUtils.join(
                        columns.stream().map(c -> ("t1." + c)).distinct().collect(Collectors.toList()), ",");

                //获取表名
                newTable = visitor.getTables().entrySet().stream().findFirst().get().getKey().getName();

                //设置表名的别名
                newTable = newTable + " as t1";

                //只受理一个简单SQL

                break;
            }

        }

        if (StringUtils.isBlank(newColumns) || StringUtils.isBlank(newTable)) {
            throw new RuntimeException();
        }

        //解析出zebra原本的hint
        String hint = sql.substring(0, sql.toLowerCase().indexOf(SELECT));

        //截取from及之后的sql语句
        String fromMore = sql.substring(sql.toLowerCase().indexOf(FROM));

        //组装子查询：仅保留id，以便利用覆盖索引的特性
        String subSql = "(select id " + fromMore + ") as t2";

        //通过where条件进行表连接。如果是分库分表则增加hq_id字段作为条件，便于分库分表的路由
        String where = singleTable ? "t1.id = t2.id" : "t1.id = t2.id and hq_id = ?";

        //构建新SQL
        return buildNewSql(hint, newTable, newColumns, subSql, where);
    }

    /**
     * 构建单表查询SQL
     *
     * @param hint
     * @param table
     * @param columns
     * @param subSql
     * @param where
     * @return
     */
    private static String buildNewSql(String hint, String table, String columns, String subSql, String where) {
        String sql = hint + new SQL().SELECT(columns).FROM(table).FROM(subSql).WHERE(where).toString();

        return sql;

    }

    /**
     * 复制MappedStatement
     *
     * @param ms
     * @param boundSql
     * @param sql
     * @param parameterMappings
     * @param parameter
     * @return
     */
    private MappedStatement copyFromNewSql(MappedStatement ms, BoundSql boundSql, String sql,
                                           List<ParameterMapping> parameterMappings, Object parameter) {

        BoundSql newBoundSql = copyFromBoundSql(ms, boundSql, sql, parameterMappings, parameter);
        return copyFromMappedStatement(ms, new BoundSqlSqlSource(newBoundSql));
    }

    /**
     * 复制BoundSql
     *
     * @param ms
     * @param boundSql
     * @param sql
     * @param parameterMappings
     * @param parameter
     * @return
     */
    private BoundSql copyFromBoundSql(MappedStatement ms, BoundSql boundSql, String sql,
                                      List<ParameterMapping> parameterMappings, Object parameter) {

        BoundSql newBoundSql = new BoundSql(ms.getConfiguration(), sql, parameterMappings, parameter);
        for (ParameterMapping mapping : boundSql.getParameterMappings()) {
            String prop = mapping.getProperty();
            if (boundSql.hasAdditionalParameter(prop)) {
                newBoundSql.setAdditionalParameter(prop, boundSql.getAdditionalParameter(prop));
            }
        }
        return newBoundSql;
    }

    /**
     * 复制MappedStatement
     *
     * @param ms
     * @param newSqlSource
     * @return
     */
    private MappedStatement copyFromMappedStatement(MappedStatement ms, SqlSource newSqlSource) {
        MappedStatement.Builder builder = new MappedStatement.Builder(ms.getConfiguration(), ms.getId(), newSqlSource,
                                                                      ms.getSqlCommandType());

        builder.resource(ms.getResource());
        builder.fetchSize(ms.getFetchSize());
        builder.statementType(ms.getStatementType());
        builder.keyGenerator(ms.getKeyGenerator());
        if (ms.getKeyProperties() != null && ms.getKeyProperties().length != 0) {
            StringBuilder keyProperties = new StringBuilder();
            for (String keyProperty : ms.getKeyProperties()) {
                keyProperties.append(keyProperty).append(",");
            }
            keyProperties.delete(keyProperties.length() - 1, keyProperties.length());
            builder.keyProperty(keyProperties.toString());
        }

        //setStatementTimeout()
        builder.timeout(ms.getTimeout());

        //setStatementResultMap()
        builder.parameterMap(ms.getParameterMap());

        //setStatementResultMap()
        builder.resultMaps(ms.getResultMaps());
        builder.resultSetType(ms.getResultSetType());

        //setStatementCache()
        builder.cache(ms.getCache());
        builder.flushCacheRequired(ms.isFlushCacheRequired());
        builder.useCache(ms.isUseCache());

        return builder.build();
    }

    static class BoundSqlSqlSource implements SqlSource {
        private BoundSql boundSql;

        BoundSqlSqlSource(BoundSql boundSql) {
            this.boundSql = boundSql;
        }

        @Override
        public BoundSql getBoundSql(Object parameterObject) {
            return boundSql;
        }
    }

    @Override
    public Object plugin(Object o) {
        return Plugin.wrap(o, this);
    }

    @Override
    public void setProperties(Properties properties) {
    }

    /**
     * 获取去掉分页前缀的SQL
     *
     * @param sql
     * @return
     */
    private static String getSqlNoPrefix(String sql) {
        return sql.replace(PREFIX_PAGER_SINGLE, StringUtils.EMPTY).replace(PREFIX_PAGER_SHARD, StringUtils.EMPTY);
    }

}
