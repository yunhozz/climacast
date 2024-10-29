import java.net.URI
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	kotlin("jvm") version "2.0.0"
	kotlin("plugin.jpa") version "2.0.0"
	kotlin("plugin.spring") version "2.0.0"
	kotlin("kapt") version "1.8.22"
	id("org.springframework.boot") version "3.3.5"
	id("io.spring.dependency-management") version "1.1.6"
}

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
	}
}

allprojects {
	group = "com.climacast"
	version = "0.0.1-SNAPSHOT"

	repositories {
		mavenCentral()
	}
}

subprojects {
	apply(plugin = "java")
	apply(plugin = "kotlin")
	apply(plugin = "kotlin-spring")
	apply(plugin = "kotlin-allopen")
	apply(plugin = "kotlin-noarg")
	apply(plugin = "kotlin-kapt")
	apply(plugin = "org.springframework.boot")
	apply(plugin = "io.spring.dependency-management")

	repositories {
		maven {
			url = URI.create("https://plugins.gradle.org/m2/")
		}
		mavenCentral()
	}

	dependencyManagement {
		imports {
			mavenBom("org.springframework.boot:spring-boot-dependencies:3.3.5")
			mavenBom("org.springframework.cloud:spring-cloud-dependencies:2023.0.3")
		}
	}

	dependencies {
		implementation("org.springframework.boot:spring-boot-starter")
		implementation("org.jetbrains.kotlin:kotlin-reflect")
		implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
		implementation("org.springframework.boot:spring-boot-starter-actuator")
		implementation("org.danilopianini:khttp:1.3.1")
		implementation("org.springframework.boot:spring-boot-starter-log4j2")
		developmentOnly("org.springframework.boot:spring-boot-devtools")
		testImplementation("org.springframework.boot:spring-boot-starter-test")
		testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
		testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	}

	configurations.forEach {
		it.exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
		it.exclude(group = "org.apache.logging.log4j", module = "log4j-to-slf4j")
	}

	tasks.withType<KotlinCompile> {
		kotlinOptions {
			jvmTarget = "21"
		}
	}

	tasks.withType<Test> {
		useJUnitPlatform()
	}

	allOpen {
		annotation("jakarta.persistence.Entity")
		annotation("jakarta.persistence.MappedSuperclass")
		annotation("jakarta.persistence.Embeddable")
		annotation("org.springframework.data.elasticsearch.annotations.Document")
	}

	noArg {
		annotation("jakarta.persistence.Entity")
		annotation("jakarta.persistence.MappedSuperclass")
		annotation("jakarta.persistence.Embeddable")
		annotation("org.springframework.data.elasticsearch.annotations.Document")
	}
}

project(":common")
project(":config-server")
project(":eureka-server")