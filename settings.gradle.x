pluginManagement {
    repositories {
        ivy { // Use my own CS dept repo
            url 'https://www.cs.odu.edu/~zeil/ivyrepo'
        }
        gradlePluginPortal()
        mavenCentral()
    }
}
rootProject.name = 'code-grader'
