dependencies {
	implementation(project(":global"))
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-data-elasticsearch")
	implementation("org.springframework.boot:spring-boot-starter-mail")
	implementation("org.springframework.cloud:spring-cloud-starter-config")
	implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}