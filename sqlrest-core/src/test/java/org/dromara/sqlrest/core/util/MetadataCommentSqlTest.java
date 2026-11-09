// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/sqlrest
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2024/3/31
// Location: beijing , china
/////////////////////////////////////////////////////////////
package org.dromara.sqlrest.core.util;

import java.util.List;
import org.dromara.sqlrest.common.enums.ProductTypeEnum;
import org.junit.Assert;
import org.junit.Test;

public class MetadataCommentSqlTest {

  @Test
  public void testDamengTableCommentsCandidates() {
    List<MetadataCommentSql.Query> queries =
        MetadataCommentSql.tableComments(ProductTypeEnum.DM);
    Assert.assertEquals(3, queries.size());
    Assert.assertTrue(queries.get(0).getSql().contains("ALL_TAB_COMMENTS"));
    Assert.assertArrayEquals(new int[]{0}, queries.get(0).getParamIndexes());
    Assert.assertTrue(queries.get(1).getSql().contains("USER_TAB_COMMENTS"));
    Assert.assertEquals(0, queries.get(1).getParamIndexes().length);
    Assert.assertTrue(queries.get(2).getSql().contains("SYSTABLECOMMENTS"));
    Assert.assertArrayEquals(new int[]{0}, queries.get(2).getParamIndexes());
  }

  @Test
  public void testDamengColumnCommentsCandidates() {
    List<MetadataCommentSql.Query> queries =
        MetadataCommentSql.columnComments(ProductTypeEnum.DM);
    Assert.assertEquals(3, queries.size());
    Assert.assertArrayEquals(new int[]{0, 1}, queries.get(0).getParamIndexes());
    Assert.assertTrue(queries.get(1).getSql().contains("USER_COL_COMMENTS"));
    Assert.assertArrayEquals(new int[]{1}, queries.get(1).getParamIndexes());
    Assert.assertTrue(queries.get(2).getSql().contains("SYSCOLUMNCOMMENTS"));
    Assert.assertArrayEquals(new int[]{0, 1}, queries.get(2).getParamIndexes());
  }

  @Test
  public void testDamengObjectListFallback() {
    Assert.assertEquals(3, MetadataCommentSql.tableNames(ProductTypeEnum.DM).size());
    Assert.assertTrue(MetadataCommentSql.tableNames(ProductTypeEnum.DM).get(0)
        .getSql().contains("ALL_TABLES"));
    Assert.assertEquals(3, MetadataCommentSql.viewNames(ProductTypeEnum.DM).size());
    Assert.assertEquals(2, MetadataCommentSql.columns(ProductTypeEnum.DM).size());
    Assert.assertEquals(2, MetadataCommentSql.schemaNames(ProductTypeEnum.DM).size());
    Assert.assertTrue(MetadataCommentSql.tableNames(ProductTypeEnum.MYSQL).isEmpty());
    Assert.assertTrue(MetadataCommentSql.viewNames(ProductTypeEnum.ORACLE).isEmpty());
    Assert.assertTrue(MetadataCommentSql.columns(ProductTypeEnum.MYSQL).isEmpty());
  }

  @Test
  public void testOtherDatabasesKeepOriginalBehavior() {
    Assert.assertEquals(1, MetadataCommentSql.tableComments(ProductTypeEnum.ORACLE).size());
    Assert.assertTrue(MetadataCommentSql.tableComments(ProductTypeEnum.ORACLE)
        .get(0).getSql().contains("ALL_TAB_COMMENTS"));
    Assert.assertEquals(1, MetadataCommentSql.tableComments(ProductTypeEnum.MYSQL).size());
    Assert.assertArrayEquals(new int[]{0, 1},
        MetadataCommentSql.columnComments(ProductTypeEnum.MYSQL).get(0).getParamIndexes());
    Assert.assertTrue(MetadataCommentSql.tableComments(ProductTypeEnum.SQLITE3).isEmpty());
  }

  @Test
  public void testNullType() {
    Assert.assertTrue(MetadataCommentSql.tableComments(null).isEmpty());
    Assert.assertTrue(MetadataCommentSql.columnComments(null).isEmpty());
    Assert.assertTrue(MetadataCommentSql.tableNames(null).isEmpty());
  }

  @Test
  public void testQueryParameterIndexesAreDefensiveCopied() {
    MetadataCommentSql.Query query =
        MetadataCommentSql.tableComments(ProductTypeEnum.DM).get(0);
    int[] indexes = query.getParamIndexes();
    indexes[0] = 99;
    Assert.assertArrayEquals(new int[]{0}, query.getParamIndexes());
  }
}
