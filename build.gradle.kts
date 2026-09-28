import com.github.spotbugs.snom.Confidence
import com.github.spotbugs.snom.Effort
import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
    application
    checkstyle
    jacoco
    alias(libs.plugins.spotbugs)
    alias(libs.plugins.spotless)
}

group = "fr.eseo"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

repositories {
    mavenCentral()
}

// Mockito's inline mock maker is a Java agent: attach it explicitly instead of
// letting it self-attach, which the JDK warns about and will eventually refuse.
val mockitoAgent = configurations.create("mockitoAgent")

dependencies {
    testImplementation(libs.junit)
    testImplementation(libs.mockito.core)
    mockitoAgent(libs.mockito.core) { isTransitive = false }
}

application {
    mainClass = "fr.eseo.controller.Game"
}

tasks.jar {
    archiveFileName = "monkey-island.jar"
    manifest {
        attributes(
            "Main-Class" to application.mainClass,
            "Implementation-Version" to project.version,
        )
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    // Every compiler warning is an error.
    options.compilerArgs.addAll(listOf("-Xlint:all", "-Werror"))
}

tasks.test {
    useJUnit()
    // The model uses javax.swing.Timer: no display is needed.
    systemProperty("java.awt.headless", "true")
    jvmArgs("-javaagent:${mockitoAgent.asPath}")
    testLogging {
        events("failed")
        exceptionFormat = TestExceptionFormat.FULL
    }
    finalizedBy(tasks.jacocoTestReport)
}

// Formatting: google-java-format. `./gradlew spotlessApply` fixes it.
spotless {
    java {
        googleJavaFormat(
            libs.versions.google.java.format
                .get(),
        )
        formatAnnotations()
    }
    kotlinGradle {
        target("*.gradle.kts")
        ktlint()
    }
}

// Coding rules the formatter does not cover.
checkstyle {
    toolVersion = libs.versions.checkstyle.get()
    maxWarnings = 0
}

// Bug patterns (successor of FindBugs, used by the original project).
spotbugs {
    toolVersion = libs.versions.spotbugs.get()
    effort = Effort.MAX
    reportLevel = Confidence.MEDIUM
    excludeFilter = file("config/spotbugs/exclude.xml")
}

tasks.spotbugsTest {
    enabled = false
}

tasks.withType<com.github.spotbugs.snom.SpotBugsTask>().configureEach {
    reports.create("html") { required = true }
}

// Coverage (successor of Emma, used by the original project).
jacoco {
    toolVersion = libs.versions.jacoco.get()
}

tasks.jacocoTestReport {
    reports {
        xml.required = true
        html.required = true
    }
}

tasks.jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                counter = "LINE"
                minimum = "0.70".toBigDecimal()
            }
        }
    }
}

// Documentation: the Javadoc must build without errors or warnings.
tasks.javadoc {
    (options as StandardJavadocDocletOptions).apply {
        encoding = "UTF-8"
        addBooleanOption("Xdoclint:all,-missing", true)
        addBooleanOption("Werror", true)
    }
}

tasks.check {
    dependsOn(tasks.jacocoTestCoverageVerification, tasks.javadoc)
}
