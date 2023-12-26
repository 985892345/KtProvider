plugins {
  kotlin("jvm")
  kotlin("kapt")
  id("publish-maven-central")
}

version = properties["VERSION"].toString()
group = properties["GROUP"].toString()

publish {
  artifactId = "provider-compile-ksp"
}

dependencies {
  implementation(project(":provider-api"))
  
  // https://mvnrepository.com/artifact/com.google.auto.service/auto-service
  compileOnly("com.google.auto.service:auto-service:1.1.1")
  kapt("com.google.auto.service:auto-service:1.1.1")
  
  implementation("com.google.devtools.ksp:symbol-processing-api:1.9.20-1.0.14")
  
  // https://square.github.io/kotlinpoet/
  implementation("com.squareup:kotlinpoet:1.15.1")
  implementation("com.squareup:kotlinpoet-ksp:1.15.1")
}

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
      create<MavenPublication>("MavenCentral") {
        groupId = publish.groupId
        artifactId = projectArtifactId
        version = publish.version
        from(components["kotlin"])
        signing {
          sign(this@create)
        }

        pom {
          name.set(projectArtifactId)
          description.set(projectDescription)
          url.set("https://github.com/985892345/$projectGithubName")

          licenses {
            license {
              name.set("Apache-2.0 license")
              url.set("https://github.com/985892345/$projectGithubName/blob/$projectMainBranch/LICENSE")
            }
          }

          developers {
            developer {
              id.set("985892345")
              name.set("GuoXiangrui")
              email.set("guo985892345@formail.com")
            }
          }

          scm {
            connection.set("https://github.com/985892345/$projectGithubName.git")
            developerConnection.set("https://github.com/985892345/$projectGithubName.git")
            url.set("https://github.com/985892345/$projectGithubName")
          }
        }
      }
    }
  }
}

