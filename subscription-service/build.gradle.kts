dependencies {
	implementation(project(":global"))
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.8.1")
	implementation("org.springframework.boot:spring-boot-starter-data-elasticsearch")
	implementation("org.springframework.boot:spring-boot-starter-mail")
	implementation("org.springframework.cloud:spring-cloud-starter-config")
	implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")
	implementation("org.springframework.boot:spring-boot-starter-data-redis")
	implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
	// redisson
	implementation("org.redisson:redisson-spring-boot-starter:3.27.0")
	// slack
	implementation("com.slack.api:slack-api-client:1.44.2")
	// selenium
	implementation("org.seleniumhq.selenium:selenium-java:4.27.0")
	// twilio
	implementation("com.twilio.sdk:twilio:10.6.4")

	runtimeOnly("com.mysql:mysql-connector-j")
}