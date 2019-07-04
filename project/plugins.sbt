addSbtPlugin("com.lightbend.lagom" % "lagom-sbt-plugin" % "1.4.14")

addSbtPlugin("com.lightbend.cinnamon" % "sbt-cinnamon" % "2.12.0-M2")
credentials += Credentials(Path.userHome / ".lightbend" / "commercial.credentials")
resolvers += Resolver.url("lightbend-commercial", url("https://repo.lightbend.com/commercial-releases"))(Resolver.ivyStylePatterns)
