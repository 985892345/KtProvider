import org.gradle.api.Project

/**
 * ...
 *
 * @author 985892345 (Guo Xiangrui)
 * @date 2022/10/10 16:57
 */
abstract class Publish(project: Project) {
  var artifactId: String = ""
  var githubName: String = "KtProvider"
  var mainBranch: String = "master"
  var description = "支持 KMM 的跨模块服务提供框架"
  var groupId = project.properties.getValue("GROUP").toString()
  var version = project.properties.getValue("VERSION").toString()
}