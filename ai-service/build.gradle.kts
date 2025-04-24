import java.net.URI

dependencies {
	implementation(project(":global"))
	implementation("org.springframework.boot:spring-boot-starter-data-redis-reactive")
	implementation("org.springframework.boot:spring-boot-starter-webflux")
	implementation("org.springframework.ai:spring-ai-starter-model-openai")
	implementation("org.springframework.ai:spring-ai-starter-model-ollama")
	implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
	implementation(platform("org.springframework.ai:spring-ai-bom:1.0.0-SNAPSHOT"))
	implementation("org.springframework.cloud:spring-cloud-starter-config")
	implementation("org.redisson:redisson-spring-boot-starter:3.27.0")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-cache")
	implementation("io.projectreactor.addons:reactor-extra:3.3.8.RELEASE")
	implementation("org.redisson:redisson-spring-boot-starter:3.27.0")
	testImplementation("io.projectreactor:reactor-test")
	testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")
}

repositories {
	maven {
		url = URI.create("https://repo.spring.io/milestone")
	}
	maven {
		url = URI.create("https://repo.spring.io/snapshot")
	}
	maven {
		name = "Central Portal Snapshots"
		url = URI.create("https://central.sonatype.com/repository/maven-snapshots/")
	}
}