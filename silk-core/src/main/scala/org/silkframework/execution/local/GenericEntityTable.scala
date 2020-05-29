package org.silkframework.execution.local

import org.silkframework.config.{Task, TaskSpec}
import org.silkframework.entity.{Entity, EntitySchema}
import org.silkframework.execution.{EntityHolder, InterruptibleTraversable}

class GenericEntityTable(genericEntities: Traversable[Entity],
                         override val entitySchema: EntitySchema,
                         override val task: Task[TaskSpec],
                         override val globalErrors: Seq[String] = Seq.empty) extends LocalEntities {

  override def entities: Traversable[Entity] = {
    new InterruptibleTraversable(genericEntities)
  }

  def mapEntities(f: Entity => Entity): EntityHolder = {
    updateEntities(entities.map(f), entitySchema)
  }

  def flatMapEntities(outputSchema: EntitySchema, updateTask: Task[TaskSpec] = task)(f: Entity => TraversableOnce[Entity]): EntityHolder = {
    updateEntities(entities.flatMap(f), outputSchema)
  }

  def filter(f: Entity => Boolean): EntityHolder = {
    updateEntities(entities.filter(f), entitySchema)
  }

  @inline
  private def updateEntities(newEntities: Traversable[Entity], newSchema: EntitySchema): GenericEntityTable = {
    new GenericEntityTable(newEntities, newSchema, task)
  }
}

object GenericEntityTable {
  def apply(entities: Traversable[Entity], entitySchema: EntitySchema, task: Task[TaskSpec], globalErrors: Seq[String] = Seq.empty): GenericEntityTable = {
    new GenericEntityTable(entities, entitySchema, task, globalErrors)
  }
}