// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/sqlrest
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2024/3/31
// Location: beijing , china
/////////////////////////////////////////////////////////////
package org.dromara.sqlrest.template;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;

public class TemplateTest {

  @Test
  public void testIf() {
    String content = "select * from user where <if test='id != null ' > id  = #{id} </if>";
    XmlSqlTemplate parser = new XmlSqlTemplate(content);

    System.out.println(parser.getParameterNames());

    HashMap<String, Object> map = new HashMap<>();
    map.put("id", "11");
    SqlMeta process = parser.process(map);
    System.out.println(process);
  }

  @Test
  public void testWhere() {
    String content = "select * from user <where> <if test='id != null ' > and id  = #{id} </if>  <if test=' name != null' >name =#{name}</if> </where>";
    XmlSqlTemplate parser = new XmlSqlTemplate(content);

    System.out.println(parser.getParameterNames());

    HashMap<String, Object> map = new HashMap<>();
    //map.put("name", "join");
    SqlMeta process = parser.process(map);
    System.out.println(process);
  }


  @Test
  public void testSet() {
    String content = "update user <set> <if test='id != null '> id = #{id} ,</if><if test='name != null '> name = #{name} ,</if> </set> ";
    XmlSqlTemplate parser = new XmlSqlTemplate(content);

    System.out.println(parser.getParameterNames());

    HashMap<String, Object> map = new HashMap<>();
    map.put("id", "123");
    map.put("name", "join");
    SqlMeta process = parser.process(map);
    System.out.println(process);
  }


  @Test
  public void testChoose() {
    String content = "select  * from user <where><choose><when test=' id!= null '> and id = #{id} </when><when test=' name!= null and name.length()>0 '> and name = #{name} </when></choose> </where>";
    XmlSqlTemplate parser = new XmlSqlTemplate(content);

    System.out.println(parser.getParameterNames());

    HashMap<String, Object> map = new HashMap<>();
    //map.put("id", "123");
    map.put("name", "join");
    SqlMeta process = parser.process(map);
    System.out.println(process);
  }

  @Test
  public void testForEach() {
    String content = "select  * from user <where> id in <foreach item=\"item\" index=\"index\" collection=\"list\"    open=\"(\" separator=\",\" close=\")\">   ${item}   ${index}  </foreach></where>";
    XmlSqlTemplate parser = new XmlSqlTemplate(content);

    System.out.println(parser.getParameterNames());

    HashMap<String, Object> map = new HashMap<>();
    map.put("id", "123");
    map.put("name", "join");
		/*
		List<String> arrayList = new ArrayList<>() ;
		arrayList.add("1") ;
		arrayList.add("2") ;
		arrayList.add("3") ;
		arrayList.add("4") ;
		*/
    HashMap<String, Object> map2 = new HashMap<>();
    map2.put("11", "111");
    map2.put("22", "222");
    map.put("list", map2);
    SqlMeta process = parser.process(map);
    System.out.println(process);
  }

  @Test
  public void testLT() {
    //String content = "select * from user where <if test='id != null ' > id  &lt; #{id} </if>";
    String content = "select * from user where <if test='id != null ' > id  <![CDATA[ < ]]> #{id} </if>";
    XmlSqlTemplate parser = new XmlSqlTemplate(content);

    System.out.println(parser.getParameterNames());

    HashMap<String, Object> map = new HashMap<>();
    map.put("id", "11");
    SqlMeta process = parser.process(map);
    System.out.println(process);
  }

  @Test
  public void testTrim() {
    String content = "SELECT * from employees.employees \n"
        + "<trim prefix =\"WHERE\" prefixOverrides=\"AND | OR\">\n"
        + "  <if test=\"empNo != null\">\n"
        + "    AND emp_no = #{empNo}\n"
        + "  </if>\n"
        + "  <if test=\"firstName != null\">\n"
        + "    AND first_name like concat('%', #{firstName}, '%')\n"
        + "  </if>\n"
        + "  <if test=\"lastName != null \">\n"
        + "    AND last_name like concat('%', #{lastName}, '%')\n"
        + "  </if>\n"
        + " </trim>";
    XmlSqlTemplate parser = new XmlSqlTemplate(content);
    System.out.println(parser.getParameterNames());
  }

  @Test
  public void parseParams() {
    String content = "SELECT count(DISTINCT datasource_uuid,database_name,table_name) \n"
        + "FROM t_test_record \n"
        + "WHERE is_sensitive=#{status} AND is_confirmed='1' AND is_deleted='0' \n"
        + "<choose>\n"
        + "  <when test=\"dsUuids!=null and dsUuids.size ==1 \">\n"
        + "AND datasource_uuid =#{dsUuids[0]} \n"
        + "  </when>\n"
        + "  <when test=\"dsUuids!=null and dsUuids.size > 1 \">\n"
        + "AND datasource_uuid in \n"
        + "    <foreach item='item' collection='dsUuids' separator=',' open='(' close=')' index=''> \n"
        + "     #{item} \n"
        + "    </foreach> \n"
        + "  </when>\n"
        + "</choose>";
    XmlSqlTemplate parser = new XmlSqlTemplate(content);
    System.out.println(parser.getParameterNames());
  }

  @Test
  public void testObject() {
    String xmlSql = " insert into `employees` "
        + " (emp_no,birth_date,first_name,last_name,gender,hire_date)"
        + " values "
        + "<foreach collection='records' item='record' separator=','>"
        + " ( #{record.empNo}, #{record.birthDate}, #{record.firstName}, #{record.lastName}, #{record.gender}, #{record.hireDate})"
        + "</foreach>";

    XmlSqlTemplate sqlParser = new XmlSqlTemplate(xmlSql);
    Map<String, Boolean> paramNames = sqlParser.getParameterNames();
    System.out.println(paramNames);

    Map<String, Object> record1 = new HashMap<>();
    record1.put("empNo", 11);
    record1.put("birthDate", new Date());
    record1.put("firstName", "tom1");
    record1.put("lastName", "cat1");
    record1.put("gender", "F");
    record1.put("hireDate", new Date());
    Map<String, Object> record2 = new HashMap<>();
    record2.put("empNo", 22);
    record2.put("birthDate", new Date());
    record2.put("firstName", "tom2");
    record2.put("lastName", "cat2");
    record2.put("gender", "F");
    record2.put("hireDate", new Date());
    List<Map<String, Object>> employees = Arrays.asList(record1, record2);

    Map<String, Object> params = new HashMap<>();
    params.put("records", employees);
    SqlMeta sqlData = sqlParser.process(params);
    System.out.println(sqlData.getSql());
    System.out.println(sqlData.getParameter());
  }

  @Test
  public void testObjFiled() {
    String sql = "SELECT * from employees.departments WHERE dept_no=#{obj.deptNo}";
    XmlSqlTemplate template = new XmlSqlTemplate(sql);
    System.out.println(template.getParameterNames());

    Map<String, Object> obj = new HashMap<>();
    obj.put("deptNo", "d001");

    Map<String, Object> params = new HashMap<>();
    params.put("obj", obj);
    SqlMeta sqlMeta = template.process(params);
    System.out.println(sqlMeta);
  }

  @Test
  public void testObjArrayField() {
    String sql = "SELECT * from employees.departments WHERE dept_no in \n"
        + "<foreach open=\"(\" close=\")\" collection=\"obj.deptNo\" separator=\",\" item=\"item\" index=\"index\">"
        + "#{item}"
        + "</foreach>";
    XmlSqlTemplate template = new XmlSqlTemplate(sql);
    System.out.println(template.getParameterNames());

    Map<String, Object> obj = new HashMap<>();
    obj.put("deptNo", Arrays.asList("d001","d002"));

    Map<String, Object> params = new HashMap<>();
    params.put("obj", obj);
    SqlMeta sqlMeta = template.process(params);
    System.out.println(sqlMeta);
  }

  @Test
  public void testObjNotRequiredField() {
    String sql = "SELECT * from employees.departments "
        + "<if test='obj.deptNo != null and obj.deptNo.size() > 0'> "
        + " WHERE dept_no in "
        + " <foreach open=\"(\" close=\")\" collection=\"obj.deptNo\" separator=\",\" item=\"item\" index=\"index\"> "
        + " #{item} "
        + " </foreach> "
        + "</if>";
    XmlSqlTemplate template = new XmlSqlTemplate(sql);
    System.out.println(template.getParameterNames());

    Map<String, Object> obj = new HashMap<>();
    Map<String, Object> params = new HashMap<>();
    params.put("obj", obj);
    SqlMeta sqlMeta = template.process(params);
    System.out.println(sqlMeta);
  }
}
