// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/sqlrest
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2024/3/31
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.gitee.sqlrest.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gitee.sqlrest.common.dto.DateCount;
import com.gitee.sqlrest.common.dto.NameCount;
import com.gitee.sqlrest.persistence.entity.AccessRecordEntity;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface AccessRecordMapper extends BaseMapper<AccessRecordEntity> {

  @Select("<script>"
      + "<if test='_databaseId == \"mysql\" '>"
      + "SELECT "
      + "  (SELECT count(*) from SQLREST_API_ASSIGNMENT) as `totalCount`, "
      + "  (SELECT count(*) from SQLREST_API_ASSIGNMENT where `open`=1) as `openCount`, "
      + "  (SELECT count(*) from SQLREST_API_ASSIGNMENT where `status`=1) as `publishCount`, "
      + "  (SELECT count(*) from SQLREST_DATASOURCE) as `datasourceCount` "
      + "</if>"
      + "<if test='_databaseId == \"postgresql\" '>"
      + "SELECT "
      + "  (SELECT count(*) from SQLREST_API_ASSIGNMENT) as \"totalCount\", "
      + "  (SELECT count(*) from SQLREST_API_ASSIGNMENT where open=true) as \"openCount\", "
      + "  (SELECT count(*) from SQLREST_API_ASSIGNMENT where status=true) as \"publishCount\", "
      + "  (SELECT count(*) from SQLREST_DATASOURCE) as \"datasourceCount\" "
      + "</if>"
      + "</script>")
  Map<String, Integer> selectCount();

  @Select("<script>"
      + "<if test='_databaseId == \"mysql\" '>"
      + "SELECT path as name,count(1) as count from SQLREST_ACCESS_RECORD "
      + "WHERE DATE_SUB( CURDATE(), INTERVAL ${days} DAY ) <![CDATA[ <=  ]]> date(create_time) "
      + "GROUP BY path "
      + "ORDER BY count DESC "
      + "LIMIT ${limit} "
      + "</if>"
      + "<if test='_databaseId == \"postgresql\" '>"
      + "SELECT path as name,count(1) as count from SQLREST_ACCESS_RECORD "
      + "WHERE CURRENT_DATE - INTERVAL'${days} day' <![CDATA[ <=  ]]> create_time::date "
      + "GROUP BY path "
      + "ORDER BY count DESC "
      + "LIMIT ${limit} "
      + "</if>"
      + "</script>")
  List<NameCount> getTopPathAccess(@Param("days") Integer days, @Param("limit") Integer limit);

  @Select("<script>"
      + "<if test='_databaseId == \"mysql\" '>"
      + "SELECT ip_addr as name,count(1) as count from SQLREST_ACCESS_RECORD "
      + "WHERE DATE_SUB( CURDATE(), INTERVAL ${days} DAY ) <![CDATA[ <=  ]]> date(create_time) "
      + "GROUP BY ip_addr "
      + "ORDER BY count DESC "
      + "LIMIT ${limit} "
      + "</if>"
      + "<if test='_databaseId == \"postgresql\" '>"
      + "SELECT ip_addr as name,count(1) as count from SQLREST_ACCESS_RECORD "
      + "WHERE CURRENT_DATE - INTERVAL'${days} day' <![CDATA[ <=  ]]> create_time::date "
      + "GROUP BY ip_addr "
      + "ORDER BY count DESC "
      + "LIMIT ${limit} "
      + "</if>"
      + "</script>")
  List<NameCount> getTopIpAddrAccess(@Param("days") Integer days, @Param("limit") Integer limit);

  @Select("<script>"
      + "<if test='_databaseId == \"mysql\" '>"
      + "SELECT "
      + "  IFNULL((select name from SQLREST_APP_CLIENT t where app_key = client_key),'空') as name, "
      + "  count(1) as count "
      + "from SQLREST_ACCESS_RECORD  "
      + "WHERE DATE_SUB( CURDATE(), INTERVAL ${days} DAY ) &lt;= date(create_time) "
      + "GROUP BY client_key "
      + "ORDER BY count DESC "
      + "LIMIT ${limit} "
      + "</if>"
      + "<if test='_databaseId == \"postgresql\" '>"
      + "SELECT "
      + "  COALESCE((select name from SQLREST_APP_CLIENT t where app_key = client_key),'空') as name, "
      + "  count(1) as count "
      + "from SQLREST_ACCESS_RECORD  "
      + "WHERE CURRENT_DATE - INTERVAL'${days} day' <![CDATA[ <=  ]]> create_time::date "
      + "GROUP BY client_key "
      + "ORDER BY count DESC "
      + "LIMIT ${limit} "
      + "</if>"
      + "</script>")
  List<NameCount> getTopAppClientAccess(@Param("days") Integer days, @Param("limit") Integer limit);

  @Select("<script>"
      + "<if test='_databaseId == \"mysql\" '>"
      + "SELECT concat('HTTP(',status,')') as name,count(1) as count from SQLREST_ACCESS_RECORD  "
      + "WHERE DATE_SUB( CURDATE(), INTERVAL ${days} DAY ) <![CDATA[ <=  ]]> date(create_time) "
      + "GROUP BY status "
      + "ORDER BY count DESC "
      + "</if>"
      + "<if test='_databaseId == \"postgresql\" '>"
      + "SELECT concat('HTTP(',status,')') as name,count(1) as count from SQLREST_ACCESS_RECORD "
      + "WHERE CURRENT_DATE - INTERVAL'${days} day' <![CDATA[ <=  ]]> create_time::date "
      + "GROUP BY status "
      + "ORDER BY count DESC "
      + "</if>"
      + "</script>")
  List<NameCount> getHttpStatusCount(@Param("days") Integer days);

  @Select("<script>"
      + "<if test='_databaseId == \"mysql\" '>"
      + "SELECT "
      + " DATE_FORMAT(create_time,'%Y-%m-%d') as of_date , "
      + " count(*) as total, "
      + " sum(success) as success "
      + "FROM (  "
      + "  SELECT id,path,case when status=200 then 1 else 0 end as success, create_time "
      + "  FROM SQLREST_ACCESS_RECORD "
      + "  WHERE DATE_SUB( CURDATE(), INTERVAL ${days} DAY ) <![CDATA[ <=  ]]> date(create_time) "
      + " ) t  "
      + " GROUP BY of_date"
      + "</if>"
      + "<if test='_databaseId == \"postgresql\" '>"
      + "SELECT "
      + " to_char(create_time, 'YYYY-MM-DD') as of_date , "
      + " count(*) as total, "
      + " sum(success) as success "
      + "FROM (  "
      + "  SELECT id,path,case when status=200 then 1 else 0 end as success, create_time "
      + "  FROM SQLREST_ACCESS_RECORD "
      + "  WHERE CURRENT_DATE - INTERVAL'${days} day' <![CDATA[ <=  ]]> create_time::date "
      + " ) t  "
      + " GROUP BY of_date"
      + "</if>"
      + "</script>")
  List<DateCount> getDailyTrend(@Param("days") Integer days);

  @Delete("<script>"
      + "<if test='_databaseId == \"mysql\" '>"
      + "DELETE FROM SQLREST_ACCESS_RECORD "
      + "WHERE date(create_time) &lt;= DATE_SUB( CURDATE(), INTERVAL ${days} DAY ) "
      + "</if>"
      + "<if test='_databaseId == \"postgresql\" '>"
      + "DELETE FROM SQLREST_ACCESS_RECORD "
      + "WHERE CURRENT_DATE - INTERVAL'${days} day' <![CDATA[ <=  ]]> create_time::date "
      + "</if>"
      + "</script>")
  void deleteHistoryBeforeDays(@Param("days") Integer days);
}
