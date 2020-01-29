package controllers.workspaceApi

import controllers.core.{RequestUserContextAction, UserContextAction}
import controllers.core.util.ControllerUtilsTrait
import controllers.workspace.JsonSerializer
import controllers.workspaceApi.project.ProjectApiRestPayloads.{ItemMetaData, ProjectCreationData}
import javax.inject.Inject
import org.silkframework.config.{MetaData, Prefixes}
import org.silkframework.runtime.activity.UserContext
import org.silkframework.runtime.validation.BadUserInputException
import org.silkframework.util.Identifier
import org.silkframework.workspace.ProjectConfig
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, AnyContent, InjectedController}

/**
  * REST API for project artifacts.
  */
class ProjectApi @Inject()() extends InjectedController with ControllerUtilsTrait {
  /** Create a project given the meta data. Automatically generates an ID. */
  def createNewProject(): Action[JsValue] = RequestUserContextAction(parse.json) { implicit request => implicit userContext =>
      validateJson[ProjectCreationData] { projectCreationData =>
        val metaData = projectCreationData.metaData.asMetaData
        val label = metaData.label.trim
        if(label == "") {
          throw BadUserInputException("The label must not be empty!")
        }
        val generatedId = generateProjectId(label)
        val project = workspace.createProject(ProjectConfig(generatedId, metaData = cleanUpMetaData(metaData)))
        Created(JsonSerializer.projectJson(project)).
            withHeaders(LOCATION -> s"/api/workspace/projects/$generatedId")
      }
  }

  private def cleanUpMetaData(metaData: MetaData) = {
    MetaData(metaData.label.trim, metaData.description.filter(_.trim.nonEmpty))
  }

  /** Update the project meta data. */
  def updateProjectMetaData(projectId: String): Action[JsValue] = RequestUserContextAction(parse.json) { implicit request => implicit userContext =>
    validateJson[ItemMetaData] { newMetaData =>
      workspace.updateProjectMetaData(projectId, cleanUpMetaData(newMetaData.asMetaData))
      NoContent
    }
  }

  private def generateProjectId(label: String)
                               (implicit userContext: UserContext): Identifier = {
    val tempId = Identifier.fromAllowed(label, Some("project"))
    var counter = 2
    workspace.findProject(tempId) match {
      case Some(_) =>
        while (workspace.findProject(tempId + counter).isDefined) {
          counter += 1
        }
        tempId + counter
      case None =>
        tempId
    }
  }

  /** Returns all project prefixes */
  def fetchProjectPrefixes(projectId: String): Action[AnyContent] = UserContextAction { implicit userContext =>
    val project = getProject(projectId)
    Ok(Json.toJson(project.config.prefixes.prefixMap))
  }

  /** Add or update project prefix. */
  def addProjectPrefix(projectId: String, prefixName: String): Action[JsValue] = RequestUserContextAction(parse.json) { implicit request =>implicit userContext =>
    val project = getProject(projectId)
    validateJson[String] { prefixUri =>
      val newPrefixes = Prefixes(project.config.prefixes.prefixMap ++ Map(prefixName -> prefixUri))
      project.config = project.config.copy(prefixes = newPrefixes)
      Ok(Json.toJson(newPrefixes.prefixMap))
    }
  }

  /** Delete a project prefix */
  def deleteProjectPrefix(projectId: String, prefixName: String): Action[AnyContent] = UserContextAction { implicit userContext =>
    val project = getProject(projectId)
    val newPrefixes = Prefixes(project.config.prefixes.prefixMap - prefixName)
    project.config = project.config.copy(prefixes = newPrefixes)
    Ok(Json.toJson(newPrefixes.prefixMap))
  }
}