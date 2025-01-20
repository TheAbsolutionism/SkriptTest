plugins {
    id("java")
}

group = "com.github.SkriptLang"
version = "2.9.5-nightly-7b5a8a623"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")

	implementation("com.google.guava:guava:32.1.2-jre")
	implementation("org.jetbrains:annotations:26.0.1")

	// This dependency is aimed to disappear in the future.
	// The directions will be completely opposite - the plugin itself should depend on this library.
//	implementation(rootProject)
}

java {
}

tasks.compileJava {
	options.encoding = "UTF-8"
}

tasks.compileTestJava {
	options.encoding = "UTF-8"
}

tasks.test {
    useJUnitPlatform()
}
