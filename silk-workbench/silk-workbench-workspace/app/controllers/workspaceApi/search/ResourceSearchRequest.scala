package controllers.workspaceApi.search

import java.net.URLEncoder
import java.time.{Instant, ZoneId}
import java.time.format.DateTimeFormatter

import controllers.workspaceApi.search.SearchApiModel.SearchRequestTrait
import org.silkframework.runtime.activity.UserContext
import org.silkframework.runtime.resource.Resource
import org.silkframework.workspace.Project
import play.api.libs.json._

/**
  * Search request over project resources.
  */
case class ResourceSearchRequest(searchText: Option[String] = None,
                                 limit: Option[Int] = None,
                                 offset: Option[Int] = None) extends SearchRequestTrait {
  override val project: Option[String] = None // Unused, projectId is given in the apply() call.
  val workingOffset: Int =  offset.getOrElse(ResourceSearchRequest.DEFAULT_OFFSET)
  val workingLimit: Int = limit.getOrElse(ResourceSearchRequest.DEFAULT_LIMIT)

  def apply(project: Project)
           (implicit userContext: UserContext): JsValue = {
    val resources = project.resources.listRecursive
    val matchedSortedResources = filterByTextQuery(resources).sorted
    val pagedResources = matchedSortedResources.slice(workingOffset, workingOffset + workingLimit)
    val resultResources = pagedResources.map(resource => project.resources.get(resource))
    JsArray(resultResources.map(toJson))
  }

  private def toJson(resource: Resource): JsObject = {
    JsObject(
      Seq(
        ResourceSearchRequest.NAME_PARAM -> JsString(resource.name)
        /* FIXME: The full path should reflect the full path with the resource repository as root and only makes sense
                  as soon as hierarchical folder structures are supported. resource.path actually prints the file system absolute path. */
//        ResourceSearchRequest.FULL_PATH_PARAM -> JsString(resource.path)
      )
        ++ resource.modificationTime.map(instant => ResourceSearchRequest.MODIFIED_PARAM -> JsString(formatDate(instant))).toSeq
        ++ resource.size.map(size => ResourceSearchRequest.SIZE_PARAM -> JsNumber(size))
    )
  }

  private lazy val dateFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME.
      withZone(ZoneId.of("UTC"))
  private def formatDate(instant: Instant): String = {
    dateFormatter.format(instant)
  }

  private def filterByTextQuery(resources: List[String]): Seq[String] = {
    searchText.map(_.trim).filter(_.nonEmpty) match {
      case Some(text) =>
        val multiTerms = extractSearchTerms(text)
        resources.filter(r => matchesSearchTerm(multiTerms, r))
      case None =>
        resources
    }
  }

  def queryString: String = {
    Seq(
      searchText.map(t => s"searchText=${URLEncoder.encode(t, "UTF-8")}"),
      limit.map(l => s"limit=$l"),
      offset.map(o => s"offset=$o")
    ).flatten.mkString("&")
  }
}

object ResourceSearchRequest {
  final val DEFAULT_LIMIT = Int.MaxValue
  final val DEFAULT_OFFSET = 0

  final val NAME_PARAM = "name"
  final val FULL_PATH_PARAM = "fullPath"
  final val MODIFIED_PARAM = "modified"
  final val SIZE_PARAM = "size"

  implicit val resourceSearchRequestFormat: Format[ResourceSearchRequest] = Json.format[ResourceSearchRequest]
}
