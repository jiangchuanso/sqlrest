// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/sqlrest
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2024/3/31
// Location: beijing , china
/////////////////////////////////////////////////////////////
package org.dromara.sqlrest.core.exec.module;

import javax.sql.DataSource;
import org.dromara.sqlrest.common.service.VarModuleInterface;
import org.dromara.sqlrest.core.exec.annotation.Comment;
import org.dromara.sqlrest.core.exec.annotation.Module;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

@Module(TxVarModule.VAR_NAME)
public class TxVarModule implements VarModuleInterface {

  protected static final String VAR_NAME = "tx";

  private final TransactionDefinition transactionDefinition;
  private final PlatformTransactionManager transactionManager;
  private final TransactionStatus transactionStatus;

  public TxVarModule(DataSource dataSource) {
    this.transactionDefinition = getDefaultTransactionDefinition();
    this.transactionManager = new DataSourceTransactionManager(dataSource);
    this.transactionStatus = this.transactionManager.getTransaction(this.transactionDefinition);
  }

  private TransactionDefinition getDefaultTransactionDefinition() {
    DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
    definition.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
    definition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
    return definition;
  }

  @Override
  public String getVarModuleName() {
    return VAR_NAME;
  }

  @Comment("comment.tx.commit")
  public void commit() {
    this.transactionManager.commit(this.transactionStatus);
  }

  @Comment("comment.tx.rollback")
  public void rollback() {
    this.transactionManager.rollback(this.transactionStatus);
  }
}
