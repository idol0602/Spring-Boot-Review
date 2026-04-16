FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline -B
COPY src ./src
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/springbootreview-1.0.0-gateway.jar gateway.jar
COPY --from=build /app/target/springbootreview-1.0.0-user.jar user.jar
COPY --from=build /app/target/springbootreview-1.0.0-order.jar order.jar
COPY --from=build /app/target/springbootreview-1.0.0-product.jar product.jar
COPY --from=build /app/target/springbootreview-1.0.0-payment.jar payment.jar

# Define an entrypoint script or let docker-compose pass the command
CMD ["java", "-jar", "gateway.jar"]
