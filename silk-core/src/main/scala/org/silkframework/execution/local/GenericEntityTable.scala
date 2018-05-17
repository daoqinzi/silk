package org.silkframework.execution.local

import org.silkframework.config.{Task, TaskSpec}
import org.silkframework.entity.{Entity, EntitySchema}

case class GenericEntityTable(entities: Traversable[Entity], entitySchema: EntitySchema, taskOption: Option[Task[TaskSpec]]) extends LocalEntities
