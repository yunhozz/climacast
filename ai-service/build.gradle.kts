dependencies {
	implementation(project(":global"))
	implementation("org.springframework.boot:spring-boot-starter-data-redis-reactive")
	implementation("org.springframework.boot:spring-boot-starter-webflux")
	implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
	implementation("org.springframework.ai:spring-ai-starter-model-ollama")
	implementation(platform("org.springframework.ai:spring-ai-bom:1.0.0-SNAPSHOT"))
	implementation("org.springframework.cloud:spring-cloud-starter-config")
	implementation("org.redisson:redisson-spring-boot-starter:3.27.0")
	testImplementation("io.projectreactor:reactor-test")
	testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")
}