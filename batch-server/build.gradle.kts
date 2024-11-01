dependencies {
	implementation(project(":common"))
	implementation("org.springframework.boot:spring-boot-starter-batch")
	implementation("org.springframework.boot:spring-boot-starter-data-elasticsearch")
	implementation("org.springframework.cloud:spring-cloud-starter-config")
	implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")
	implementation("org.springframework.boot:spring-boot-starter-webflux")
	testImplementation("org.springframework.batch:spring-batch-test")
	testImplementation("io.projectreactor:reactor-test")
	runtimeOnly("com.mysql:mysql-connector-j")
}