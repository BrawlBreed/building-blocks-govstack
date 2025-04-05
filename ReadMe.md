# Microservices Setup Guide: Config, Discovery, Gateway & Services A/B

This guide walks through setting up a basic Spring Boot microservices architecture with centralized configuration, service discovery, and API gateway. It introduces two dummy microservices (Service A and Service B) to simulate scaling and communication.

## 🛠️ Microservices Setup on Kubernetes with Spring Boot & Minikube

This tutorial walks you through building a simple microservices architecture using **Spring Boot**, **Eureka Discovery**, **Spring Cloud Config**, and **Spring Cloud Gateway**, all deployed in a **Minikube** Kubernetes cluster.

You’ll create:

-   A centralized **Config Server** to manage shared configs.
    
-   A **Discovery Server (Eureka)** to register and locate services.
    
-   An **API Gateway** for routing and external access.
    
-   Two sample microservices (**Service A** and **Service B**) that interact with each other.
    

> 💡 In the case of the repo this tutorial is based on, **Service A** and **Service B** represent the **school** and **student** services. However, the same setup and pipeline can be extended to include many more microservices—just follow the same Spring Boot configuration, Dockerization, and Kubernetes deployment flow.

You'll package each service into Docker images, load them into Minikube, and deploy using Kubernetes manifests. This setup simulates a production-like environment on your local machine and can scale to distributed nodes with minimal changes.

----------
### 🗂️ File Structure
```
project-root/
│
│
├── services/
│   ├── config/
│   │   ├── configurations/
│   │   │   ├── configserver.yaml
│   │   │   ├── discovery.yaml
│   │   │   ├── gateway.yaml
│   │   │   ├── service-a.yaml
│   │   │   └── service-b.yaml
│   │   ├── application.yaml
│   │   └── deployment.yaml
│   │
│   ├── discovery/
│   │   ├── application.yaml
│   │   └── deployment.yaml
│   │
│   ├── gateway/
│   │   ├── application.yaml
│   │   └── deployment.yaml
│   │
│   ├── service-a/
│   │   ├── application.yaml
│   │   └── deployment.yaml
│   │
│   └── service-b/
│       ├── application.yaml
│       └── deployment.yaml
├── deployments/                 # Kubernetes deployment YAMLs
│   ├── config-deployment.yaml
│   ├── discovery-deployment.yaml
│   ├── gateway-deployment.yaml
│   ├── service-a-deployment.yaml
│   └── service-b-deployment.yaml
```
----------
## 1. **Config Server** (Centralized Config Management)

### application.yaml

```yaml
server:
  port: 8888

spring:
  application:
    name: config-server
  profiles:
    active: native
  cloud:
    config:
      server:
        native:
          search-locations: classpath:/configurations

```

### Deployment YAML

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: config-service
spec:
  selector:
    matchLabels:
      app: config-service
  template:
    metadata:
      labels:
        app: config-service
    spec:
      containers:
        - name: config-service
          image: config-service
          ports:
            - containerPort: 8888
          env:
            - name: SPRING_PROFILES_ACTIVE
              value: native
---
apiVersion: v1
kind: Service
metadata:
  name: config-service
spec:
  selector:
    app: config-service
  ports:
    - port: 8888
      targetPort: 8888
  type: ClusterIP

```

### Role

-   Central config server serving properties for all microservices
    
-   `spring.config.import=optional:configserver:http://config-service:8888` in other services fetches config from here
    

----------

## 2. **Discovery Server** (Service Registration)

### config/discovery.yaml

```yaml
server:
  port: 8761

spring:
  application:
    name: discovery

eureka:
  instance:
    hostname: discovery-service
  client:
    register-with-eureka: false
    fetch-registry: false
    service-url:
      defaultZone: http://discovery-service:8761/eureka/

```

### Deployment YAML

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: discovery-service
spec:
  selector:
    matchLabels:
      app: discovery-service
  template:
    metadata:
      labels:
        app: discovery-service
    spec:
      containers:
        - name: discovery-service
          image: discovery
          ports:
            - containerPort: 8761
---
apiVersion: v1
kind: Service
metadata:
  name: discovery-service
spec:
  selector:
    app: discovery-service
  ports:
    - port: 8761
      targetPort: 8761

```

### Role

-   Enables service registry and discovery
    
-   Other services point here to register: `eureka.client.service-url.defaultZone=http://discovery-service:8761/eureka`
    

----------

## 3. **Gateway Service** (Routing Layer)

### config/gateway.yaml

```yaml
server:
  port: 8222

spring:
  application:
    name: gateway
  cloud:
    gateway:
      discovery:
        locator:
          enabled: true
      routes:
        - id: service-a
          uri: lb://service-a
          predicates:
            - Path=/api/v1/service-a/**
        - id: service-b
          uri: lb://service-b
          predicates:
            - Path=/api/v1/service-b/**

eureka:
  client:
    service-url:
      defaultZone: http://discovery-service:8761/eureka

management:
  tracing:
    sampling:
      probability: 1.0

```

### Deployment YAML

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: gateway-service
spec:
  selector:
    matchLabels:
      app: gateway-service
  template:
    metadata:
      labels:
        app: gateway-service
    spec:
      containers:
        - name: gateway-service
          image: gateway
          ports:
            - containerPort: 8222
---
apiVersion: v1
kind: Service
metadata:
  name: gateway-service
spec:
  selector:
    app: gateway-service
  ports:
    - port: 8222
      targetPort: 8222
  type: ClusterIP

```

### Role

-   Acts as the main API entrypoint
    
-   Forwards requests to services via load-balancer (lb://service-name)
    

----------

## 4. **Service A / Service B** (Core Microservices)

### Common Config (service-a.yaml / service-b.yaml)

```yaml
spring:
  application:
    name: service-a # or service-b
  config:
    import: optional:configserver:http://config-service:8888
  datasource:
    driver-class-name: org.postgresql.Driver
    url: jdbc:postgresql://postgresql:5432/service_a
    username: admin
    password: admin
  jpa:
    hibernate:
      ddl-auto: create
    database: postgresql
    database-platform: org.hibernate.dialect.PostgreSQLDialect

eureka:
  client:
    service-url:
      defaultZone: http://discovery-service:8761/eureka

management:
  tracing:
    sampling:
      probability: 1.0

springdoc:
  api-docs.enabled: true
  swagger-ui.path: /swagger-ui.html

```

### Deployment YAML Template

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: service-a
spec:
  selector:
    matchLabels:
      app: service-a
  template:
    metadata:
      labels:
        app: service-a
    spec:
      containers:
        - name: service-a
          image: service-a
          ports:
            - containerPort: 8090
          env:
            - name: SPRING_DATASOURCE_URL
              value: jdbc:postgresql://postgresql:5432/service_a
            - name: SPRING_DATASOURCE_USERNAME
              value: admin
            - name: SPRING_DATASOURCE_PASSWORD
              value: admin
---
apiVersion: v1
kind: Service
metadata:
  name: service-a
spec:
  selector:
    app: service-a
  ports:
    - port: 8090
      targetPort: 8090
  type: ClusterIP

```

### Role

-   Individual microservices, performing isolated logic
    
-   Configurable through centralized config
    
-   Auto-register with Eureka, exposed via Gateway
    

----------
### **Why Split Services into Separate Pods**

Separating services into individual pods in Kubernetes allows:

-   **Independent scaling**: Each service can scale based on its own load.
    
-   **Fault isolation**: Failures in one service don’t crash the others.
    
-   **Tech isolation**: Services can use different stacks/configs without interfering.
    
-   **Easier updates**: You can redeploy a single service without restarting the whole system.
    
-   **Cleaner logs/metrics**: Each pod outputs logs/metrics separately — easier to debug.

### ✅ Config Service

1.  **Maven Build**
    
    ```bash
    cd config
    ./mvnw clean package -DskipTests
    
    ```
    
2.  **Docker Build**
    
    ```bash
    docker build -t config-service:latest .
    
    ```
    
3.  **Load Docker Image to Minikube**
    
    ```bash
    minikube image load config-service:latest
    
    ```
    
4.  **Set `imagePullPolicy` to Never** in `config-deployment.yaml`:
    
    ```yaml
    imagePullPolicy: Never
    
    ```
    
5.  **Apply with kubectl**
    
    ```bash
    kubectl apply -f deployments/config-deployment.yaml
    
    ```
    

----------

### ✅ Discovery Service

1.  **Maven Build**
    
    ```bash
    cd discovery
    ./mvnw clean package -DskipTests
    
    ```
    
2.  **Docker Build**
    
    ```bash
    docker build -t discovery:latest .
    
    ```
    
3.  **Load Docker Image**
    
    ```bash
    minikube image load discovery:latest
    
    ```
    
4.  **Update `discovery-deployment.yaml`**
    
    ```yaml
    imagePullPolicy: Never
    
    ```
    
5.  **Apply**
    
    ```bash
    kubectl apply -f deployments/discovery-deployment.yaml
    
    ```
    

----------

### ✅ Gateway Service

1.  **Maven Build**
    
    ```bash
    cd gateway
    ./mvnw clean package -DskipTests
    
    ```
    
2.  **Docker Build**
    
    ```bash
    docker build -t gateway:latest .
    
    ```
    
3.  **Load Image**
    
    ```bash
    minikube image load gateway:latest
    
    ```
    
4.  **Update Deployment**
    
    ```yaml
    imagePullPolicy: Never
    
    ```
    
5.  **Apply**
    
    ```bash
    kubectl apply -f deployments/gateway-deployment.yaml
    
    ```
    

----------

### ✅ Service A (Microservice A)

1.  **Maven Build**
    
    ```bash
    cd service-a
    ./mvnw clean package -DskipTests
    
    ```
    
2.  **Docker Build**
    
    ```bash
    docker build -t service-a:latest .
    
    ```
    
3.  **Load Image**
    
    ```bash
    minikube image load service-a:latest
    
    ```
    
4.  **Update Deployment**
    
    ```yaml
    imagePullPolicy: Never
    
    ```
    
5.  **Apply**
    
    ```bash
    kubectl apply -f deployments/service-a-deployment.yaml
    
    ```
    

----------

### ✅ Service B (Microservice B)

1.  **Maven Build**
    
    ```bash
    cd service-b
    ./mvnw clean package -DskipTests
    
    ```
    
2.  **Docker Build**
    
    ```bash
    docker build -t service-b:latest .
    
    ```
    
3.  **Load Image**
    
    ```bash
    minikube image load service-b:latest
    
    ```
    
4.  **Update Deployment**
    
    ```yaml
    imagePullPolicy: Never
    
    ```
    
5.  **Apply**
    
    ```bash
    kubectl apply -f deployments/service-b-deployment.yaml
    
    ```
    

----------

### 🚀 **Deployment Steps Recap**

For **each service**:

1.  `./mvnw clean package -DskipTests` → builds the `.jar`
    
2.  `docker build -t <service-name>:latest .` → creates a Docker image
    
3.  `minikube image load <service-name>:latest` → loads image into Minikube
    
4.  Add `imagePullPolicy: Never` to the deployment file
    
5.  `kubectl apply -f services/<service-name>-deployment.yaml` → deploys to the cluster
    

----------

### 🌐 **Networking Process in the Background**

-   Each **Service** object in Kubernetes exposes the corresponding **Pod** to the cluster using a stable DNS name like `http://config-service:8888`.
    
-   All services resolve each other **by name**, based on the `metadata.name` of the Kubernetes Service.
    
-   Spring Boot apps fetch their config via:

## Final Notes

-   **Service names** in `spring.application.name` must match the names used in Gateway routes and Eureka registration
    
-   Use `config/configurations/service-a.yaml` for external configuration files
    
-   Image names and container ports in YAML must match your built Docker images
    

Ready for multi-node and horizontal scaling when deployed with tools like Helm or k8s node pools.
