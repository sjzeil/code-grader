plugins {
    id 'java'
    id 'application'
	id 'project-report'
	id 'checkstyle'
	id "com.github.spotbugs" version "5.0.8"
	id 'org.jbake.site' version "5.3.0"
	id 'edu.odu.cs.report_accumulator' version '1.4'
	id 'org.hidetake.ssh' version '2.9.0'
    id 'org.unbroken-dome.test-sets' version '4.0.0' 
}

version = '1.1.4'

java {
	sourceCompatibility = JavaVersion.toVersion(11)
	targetCompatibility = JavaVersion.toVersion(11)
}

application {
    mainModule = 'odu.edu.cs.zeil.codegrader' // name defined in module-info.java
    mainClass = 'odu.edu.cs.zeil.codegrader.cli.RunTests'
}

repositories {
        ivy { // Use my own CS dept repo
            url 'https://www.cs.odu.edu/~zeil/ivyrepo'
        }
		mavenCentral()
}


dependencies {
    implementation 'org.slf4j:slf4j-api:1.7.36'
    implementation 'org.apache.logging.log4j:log4j-slf4j-impl:2.13.3'
    implementation 'org.apache.logging.log4j:log4j-api:2.13.3'
    implementation 'org.apache.logging.log4j:log4j-core:2.13.3'
    implementation 'com.opencsv:opencsv:5.6'
    implementation 'com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.13.3'
	implementation 'com.fasterxml.jackson.datatype:jackson-datatype-jdk8:2.13.3'
    implementation 'commons-cli:commons-cli:1.5.0'
    testImplementation 'org.junit.jupiter:junit-jupiter:5.8.1'
    testImplementation 'org.hamcrest:hamcrest-library:2.2'
}

application {
    mainClassName = 'odu.edu.cs.zeil.codegrader.cli.RunTests'
}

testSets { systest }

tasks.withType(Test) {
	ignoreFailures = true
    useJUnitPlatform()
	testLogging {
		events "passed", "skipped", "failed"
	}
}

check.dependsOn(systest)
systest.dependsOn(jar)


jar {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
    manifest {
        attributes 'Implementation-Title': 'code-grader',
           'Implementation-Version': '1.0',
            'Main-Class': 'edu.odu.cs.zeil.codegrader.cli.RunTests'
    }
//    from { configurations.runtimeClasspath.collect {
//         it.isDirectory() ? it : zipTree(it) } }
    from {
        configurations.runtimeClasspath.findAll {
             it.name.endsWith('jar') /*&& !it.name.contains("poi-ooxml-5") */}
             .collect { zipTree(it) }
             /*
        configurations.runtimeClasspath.findAll {
             it.name.endsWith('jar') && it.name.contains("poi-ooxml-5")}
             .collect { zipTree(it) }
             */
    }
}


// Could this be useful in making IDEs work
// when module-info is in place?
task buildLib (type: Copy) {
    from configurations.testRuntimeClasspath
    into 'lib'
}



// Reporting


checkstyle {
    ignoreFailures = true
    showViolations = false
}

tasks.withType(Checkstyle) {
  reports {
    html.destination project.file("build/reports/checkstyle/main.html")
  }
}


checkstyleTest.enabled = false
checkstyleSystest.enabled = false
check.dependsOn htmlDependencyReport

task reports (dependsOn: ['htmlDependencyReport', 'javadoc', 'check']) {
    description 'Generate all reports for this project'
}

javadoc.failOnError=false

spotbugsMain {
    ignoreFailures = true
    effort = 'max'
    reportLevel = 'medium'
    reports {
       xml.enabled = false
       html.enabled = true
    }
}

spotbugsTest.enabled = false
spotbugsSystest.enabled = false



task copyJDocs (type: Copy) {
    from 'build/docs'
    into 'build/reports'
    dependsOn 'javadoc'
}


jbake {
	 srcDirName = "src/main/jbake/"
}

task copyBake (type: Copy) {
    from 'build/jbake'
    into 'build/reports'
    dependsOn 'bake'
}

/*
task copyJar (type: Copy) {
    from 'build/libs'
    into 'build/reports/lib'
    rename { String fileName ->
        fileName.replace("-$project.version", "")
    }
    dependsOn 'jar'
}
*/


import edu.odu.cs.zeil.report_accumulator.ReportStats


task collectStats (type: ReportStats, dependsOn: ['build','htmlDependencyReport', 'check', 'copyJDocs', 'copyBake']) {
    description "Collect statistics from various reports & analysis tools"
    reportsURL = 'https://sjzeil.github.io/code-grader/'
    htmlSourceDir = file("build/jbake")
}


task site (dependsOn: ['copyJDocs', 'collectStats' , 'copyBake']){
    description "Build the project website (in build/reports)"
    group "reporting"
}


if (!project.hasProperty("ivyRepoUser")) {
    ext.ivyRepoUser = "bogus"
}
if (!project.hasProperty("ivyRepoPass")) {
    ext.ivyRepoPass = "bogus"
}


remotes {
  webServer {
    host = 'linux.cs.odu.edu'
    user = "$ivyRepoUser"
    password = "$ivyRepoPass"
  }
}

task copyJar2 (type: Copy) {
    from 'build/libs'
    into 'build/distrib'
    rename { String fileName ->
        fileName.replace("-$project.version", "")
    }
    dependsOn 'jar'
}

task dailyJar (dependsOn: 'copyJar2') {
    description "Deploy the 'daily' build to a fixed URL for downloads."
    group "Distribution"
  doLast {
	def websitePath="/home/zeil/secure_html/gitlab/code-grader/"
    ssh.run {
      session(remotes.webServer) {
       put from: "$buildDir/distrib/code-grader.jar", into: websitePath
      }
    }
  }
}

////////  Website publication on GitHub pages ///////////////////

def pagesDir = file("${project.rootDir}/../website-temp").absolutePath

task clearPages(type: Delete) {
    delete pagesDir
}

task workTree(dependsOn: ['clearPages']) { 
    doLast {
        exec {
            workingDir = '.'
            commandLine = ['git', 'worktree', 'add', '-f', pagesDir, 'gh-pages']
        }
        exec {
            workingDir = pagesDir
            commandLine = ['git', 'checkout', 'gh-pages']
        }
        exec {
            workingDir = pagesDir
            commandLine = ['git', 'pull']
        }
    }
}

task copyWebsite (type: Copy, dependsOn: ['site', 'workTree']) {
    from 'build/reports'
    into pagesDir
}


task commitWebsite (dependsOn: 'copyWebsite') {
    doLast {
        Date now = new Date()

        exec {
            workingDir = pagesDir
            commandLine = ['git', 'add', pagesDir]
            ignoreExitValue = true
        }
        exec {
            workingDir = pagesDir
            commandLine = ['git', 'commit', '-m', 'Updating webpages at ' + now.toString()]
            ignoreExitValue = true
        }
    }
}

task pushWebsite (type: Exec, dependsOn: 'commitWebsite') {
    workingDir = pagesDir
    commandLine = ['git', 'push']
    ignoreExitValue = true
}

task deploySite (type: Delete, dependsOn: 'pushWebsite') {
    group = "Reporting"
    description  'Commit and push website changes to GitHub'
    delete pagesDir
}