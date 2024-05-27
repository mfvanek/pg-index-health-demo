import com.github.spotbugs.snom.Confidence
import com.github.spotbugs.snom.Effort
import com.github.spotbugs.snom.SpotBugsTask
import gradle.kotlin.dsl.accessors._53fcf84b5c55ae3c098e3bb78b94da8d.implementation
import net.ltgt.gradle.errorprone.errorprone
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.kotlin.dsl.withType
import org.sonarqube.gradle.SonarTask

plugins {
    id("java")
    id("jacoco")
    id("org.sonarqube")
    id("checkstyle")
    id("pmd")
    id("com.github.spotbugs")
    id("net.ltgt.errorprone")
    id("io.freefair.lombok")
}

configurations.configureEach {
    exclude("org.hamcrest")
}

dependencies {
    implementation(platform("io.github.mfvanek:pg-index-health-bom:0.11.1"))
    implementation(platform("org.testcontainers:testcontainers-bom:1.19.8"))

    implementation("com.google.code.findbugs:jsr305:3.0.2")
    implementation("org.postgresql:postgresql:42.7.3") {
        exclude(group = "ch.qos.logback", module = "logback-classic")
        because("to be compatible with Spring Boot 2.7.18")
    }

    testImplementation(platform("org.junit:junit-bom:5.10.2"))
    testImplementation(platform("org.assertj:assertj-bom:3.26.0"))

    checkstyle("com.thomasjensen.checkstyle.addons:checkstyle-addons:7.0.1")

    errorprone("com.google.errorprone:error_prone_core:2.27.1")
    errorprone("jp.skypencil.errorprone.slf4j:errorprone-slf4j:0.1.24")

    spotbugsSlf4j("org.slf4j:slf4j-simple:1.7.36") {
        because("to be compatible with Spring Boot 2.7.18")
    }
    spotbugsPlugins("jp.skypencil.findbugs.slf4j:bug-pattern:1.5.0")
    spotbugsPlugins("com.h3xstream.findsecbugs:findsecbugs-plugin:1.13.0")
    spotbugsPlugins("com.mebigfatguy.sb-contrib:sb-contrib:7.6.4")
}

tasks.withType<JavaCompile>().configureEach {
    options.errorprone {
        disableWarningsInGeneratedCode.set(true)
        disable("Slf4jLoggerShouldBeNonStatic")
    }
}

tasks {
    test {
        dependsOn(checkstyleMain, checkstyleTest, pmdMain, pmdTest, spotbugsMain, spotbugsTest)
    }

    withType<SpotBugsTask>().configureEach {
        reports {
            create("xml") { enabled = true }
            create("html") { enabled = true }
        }
    }

    withType<SonarTask>().configureEach {
        dependsOn(test, jacocoTestReport)
    }
}

lombok {
    version = "1.18.32"
}

checkstyle {
    toolVersion = "10.16.0"
    configFile = file("../config/checkstyle/checkstyle.xml")
    isIgnoreFailures = false
    maxWarnings = 0
    maxErrors = 0
}

pmd {
    toolVersion = "7.1.0"
    isConsoleOutput = true
    ruleSetFiles = files("../config/pmd/pmd.xml")
    ruleSets = listOf()
}

spotbugs {
    showProgress.set(true)
    effort.set(Effort.MAX)
    reportLevel.set(Confidence.LOW)
    excludeFilter.set(file("../config/spotbugs/exclude.xml"))
}
