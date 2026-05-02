### 
    <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
    </dependency>

What it gives you

This starter automatically includes:
* Spring Data Redis
* Connection libraries (like Lettuce by default)
* Auto-configuration for Redis

When it's enough

This alone is sufficient if:
* You're using Spring Boot (with parent POM or dependency management)
* You don't need custom Redis configuration / You don’t need to manually specify versions

Things you still need to configure
1. Redis server running
    * Locally or remote
2. application.properties / application.yml
###
    spring.redis.host=localhost
    spring.redis.port=6379
3. (Optional) If using password:
###
    spring.redis.password=your_password

Optional additions
* If you're doing advanced stuff:
  * Cache support:
  ### 
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-cache</artifactId>
        </dependency>