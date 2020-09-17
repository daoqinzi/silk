package org.silkframework.plugins.dataset.xml

import org.silkframework.config.CustomTask
import org.silkframework.dataset.DatasetResourceEntitySchema
import org.silkframework.entity.EntitySchema
import org.silkframework.runtime.plugin.annotations.{Param, Plugin}
import org.silkframework.runtime.resource.Resource
import org.silkframework.workspace.resources.ResourceAutoCompletionProvider

@Plugin(
  id = "xsltOperator",
  label = "XSLT",
  description =
      """A task that converts an XML resource via an XSLT script and writes the transformed output into a file resource.
      """
)
case class XSLTOperator(@Param(value = "The XSLT file to be used for transforming XML.",
                               autoCompletionProvider = classOf[ResourceAutoCompletionProvider], allowOnlyAutoCompletedValues = true)
                        file: Resource) extends CustomTask {
  override def inputSchemataOpt: Option[Seq[EntitySchema]] = {
    Some(Seq(DatasetResourceEntitySchema.schema))
  }

  override def referencedResources: Seq[Resource] = Seq(file)

  /** Outputs the resulting XML file as a [[org.silkframework.runtime.resource.Resource]] which overwrites the
    * target dataset's resource. */
  override lazy val outputSchemaOpt: Option[EntitySchema] = {
    Some(DatasetResourceEntitySchema.schema)
  }
}
