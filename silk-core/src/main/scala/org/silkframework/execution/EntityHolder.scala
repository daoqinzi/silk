package org.silkframework.execution

import org.silkframework.config.{Task, TaskSpec}
import org.silkframework.entity.{Entity, EntitySchema}
import org.silkframework.execution.local.{GenericEntityTable, LocalEntities}

/**
  * Holds entities that are exchanged between tasks.
  */
trait EntityHolder {

  /**
    * The schema of the entities
    */
  def entitySchema: EntitySchema

  /**
    * The entities in this table.
    */
  def entities: Traversable[Entity]

  /**
    * get head Entity
    */
  def headOption: Option[Entity]

  /**
    * The task that generated this table.
    * If the entity table has been generated by a workflow this is a copy of the actual task that has been executed.
    */
  def task: Task[TaskSpec]

  /**
    * Convenience method to get either the task label if it exists or the task ID.
    * @return
    */
  def taskLabel: String = task.metaData.formattedLabel(task.id.toString)

  def mapEntities(f: Entity => Entity): EntityHolder

  def filter(f: Entity => Boolean): EntityHolder
}

trait EmptyEntityHolder extends LocalEntities {

  final def entities: Traversable[Entity] = Seq.empty

  override def updateEntities(entities: Traversable[Entity]): LocalEntities = {
    if(entities.isEmpty) {
      this
    } else {
      new GenericEntityTable(entities, entitySchema, task)
    }
  }

}