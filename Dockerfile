# Stage 1: Build da aplicação
FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder
WORKDIR /app

# Copia dependências do Maven primeiro para cache eficiente
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copia o código-fonte e gera o pacote
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Imagem final enxuta para execução
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Cria usuário não-root por segurança
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Copia o artefato construído
COPY --from=builder /app/target/*.jar app.jar

# Expõe a porta configurável do Render
EXPOSE 8080

# Limites de memória e GC otimizados para o plano Free (512MB RAM) do Render
ENTRYPOINT ["java", "-Xmx300m", "-Xms128m", "-XX:+UseSerialGC", "-jar", "app.jar"]
