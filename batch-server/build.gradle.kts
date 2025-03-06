dependencies {
	implementation(project(":global"))
	implementation("org.springframework.boot:spring-boot-starter-batch")
	implementation("org.springframework.cloud:spring-cloud-starter-config")
	implementation("org.springframework.boot:spring-boot-starter-webflux")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-data-redis")
	implementation("org.redisson:redisson-spring-boot-starter:3.27.0")
	testImplementation("org.springframework.batch:spring-batch-test")
	testImplementation("io.projectreactor:reactor-test")
	runtimeOnly("com.mysql:mysql-connector-j")
}