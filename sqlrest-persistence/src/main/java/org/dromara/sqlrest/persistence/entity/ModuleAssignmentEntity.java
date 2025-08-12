package org.dromara.sqlrest.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ModuleAssignmentEntity {

  private Long moduleId;
  private String moduleName;
  private Long assigmentId;
  private String assigmentName;
  private Long groupId;
}
