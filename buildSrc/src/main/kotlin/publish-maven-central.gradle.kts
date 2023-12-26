import org.gradle.kotlin.dsl.`maven-publish`
import org.gradle.kotlin.dsl.signing

plugins {
  `maven-publish`
  signing
}

val publish = extensions.create("publish", Publish::class.java, project)

// 使用 afterEvaluate 确保 publish 已被设置
afterEvaluate {
  publishing {
    publications {
      val projectArtifactId = publish.artifactId
      if (projectArtifactId.isEmpty()) {
        throw IllegalArgumentException(
          "artifactId 不能未空，请在 build.gradle.kts 中设置 plush.artifactId = \"...\""
        )
      }
      val projectGithubName = publish.githubName
      val projectDescription = publish.description
      val projectMainBranch = publish.mainBranch
      // create<MavenPublication>("MavenCentral") {
      //   groupId = publish.groupId
      //   artifactId = projectArtifactId
      //   version = publish.version
      //   from(components["kotlin"])
      //   signing {
      //     sign(this@create)
      //   }
      //
      //   pom {
      //     name.set(projectArtifactId)
      //     description.set(projectDescription)
      //     url.set("https://github.com/985892345/$projectGithubName")
      //
      //     licenses {
      //       license {
      //         name.set("Apache-2.0 license")
      //         url.set("https://github.com/985892345/$projectGithubName/blob/$projectMainBranch/LICENSE")
      //       }
      //     }
      //
      //     developers {
      //       developer {
      //         id.set("985892345")
      //         name.set("GuoXiangrui")
      //         email.set("guo985892345@formail.com")
      //       }
      //     }
      //
      //     scm {
      //       connection.set("https://github.com/985892345/$projectGithubName.git")
      //       developerConnection.set("https://github.com/985892345/$projectGithubName.git")
      //       url.set("https://github.com/985892345/$projectGithubName")
      //     }
      //   }
      // }
      repositories {
        maven {
          // https://s01.oss.sonatype.org/
          name = "mavenCentral"
          val releasesRepoUrl = "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
          val snapshotsRepoUrl = "https://s01.oss.sonatype.org/content/repositories/snapshots/"
          val isSnapshot = publish.version.endsWith("SNAPSHOT")
          setUrl(if (isSnapshot) snapshotsRepoUrl else releasesRepoUrl)
          credentials {
            username = project.properties["mavenCentralUsername"].toString()
            password = project.properties["mavenCentralPassword"].toString()
          }
        }
      }
    }
  }
  
  tasks.create("publishToMavenCentral") {
    group = "publishing"
    dependsOn(tasks.getByName("publishAllPublicationsToMavenCentralRepository"))
  }
}
