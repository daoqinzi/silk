package de.fuberlin.wiwiss.silk.plugins.datasource

import de.fuberlin.wiwiss.silk.datasource.DataSource
import de.fuberlin.wiwiss.silk.entity._
import scala.io.Source
import de.fuberlin.wiwiss.silk.runtime.plugin.Plugin
import de.fuberlin.wiwiss.silk.runtime.resource.Resource

@Plugin(
  id = "csv",
  label = "CSV Source.",
  description =
    "DataSource which retrieves all entities from a csv file.\n" +
    "Parameters: \n" +
    "  file:  File name inside the resources directory. In the Workbench, this is the '(projectDir)/resources' directory.\n" +
    "  properties: Comma-separated list of properties.\n" +
    "  separator: The character that is used to separate values. " +
                  "If not provided, defaults to ',', i.e., comma-separated values. " +
                  "Regexes, such as '\\t' for specifying tab-separated values, are also supported.\n" +
    "  prefix: The prefix that is used to generate URIs for each line.\n"
)
case class CsvDataSource(file: Resource, properties: String, separator: String = ",", prefix: String = "") extends DataSource {

  private val propertyList: Seq[String] = properties.split(',')

  override def retrievePaths(restriction: SparqlRestriction, depth: Int, limit: Option[Int]): Traversable[(Path, Double)] = {
    for(property <- propertyList) yield {
      (Path.parse("?" + restriction.variable + "/<" + prefix + property + ">"), 1.0)
    }
  }

  override def retrieve(entityDesc: EntityDescription, entities: Seq[String] = Seq.empty): Traversable[Entity] = {
    // Retrieve the indices of the request paths
    val indices =
      for(path <- entityDesc.paths) yield {
        val property = path.operators.head.asInstanceOf[ForwardOperator].property.uri.stripPrefix(prefix)
        val propertyIndex = propertyList.indexOf(property)
        propertyIndex
      }

    // Return new Traversable that generates an entity for each line
    new Traversable[Entity] {
      def foreach[U](f: Entity => U) {
        val inputStream = file.load
        val source = Source.fromInputStream(inputStream)
        try {
          // Iterate through all lines of the source file.
          for ((line, number) <- source.getLines.zipWithIndex) {
            //Split the line into values
            val allValues = line.split(separator)
            assert(propertyList.size == allValues.size, "Invalid line '" + line + "' with " + allValues.size + " elements. Expected numer of elements " + propertyList.size + ".")
            //Extract requested values
            val values = indices.map(allValues(_))
            //Build entity
            f(new Entity(
              uri = prefix + number,
              values = values.map(Set(_)),
              desc = entityDesc
            ))
          }
        } finally {
          source.close()
          inputStream.close()
        }
      }
    }
  }
}
