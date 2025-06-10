# VJUTest Backend

A Spring Boot application for managing and conducting tests at VJU (Vietnam Japan University).

## 🚀 Features

- RESTful API endpoints for test management
- JWT-based authentication and authorization
- Role-based access control (Admin, Teacher, Student)
- File upload and management with Cloudinary
- Email notifications
- Google Drive integration
- MySQL database integration
- Docker support for easy deployment

## 🛠️ Technologies

- Java 17
- Spring Boot 3.2.3
- Spring Security
- Spring Data JPA
- MySQL
- JWT Authentication
- Docker
- Maven
- Lombok
- Cloudinary
- Google Drive API

## 📋 Prerequisites

### For Local Development
- Java 17 or later
- Maven 3.6 or later
- MySQL 8.0 or later
- IDE (IntelliJ IDEA, Eclipse, or VS Code)

### For Docker Development
- Docker
- Docker Compose

## 🚀 Getting Started

### Option 1: Local Development Setup

#### 1. Install Java 17
```bash
# Ubuntu/Debian
sudo apt update
sudo apt install openjdk-17-jdk

# Windows
# Download and install from: https://adoptium.net/

# macOS
brew install openjdk@17
```

#### 2. Install Maven
```bash
# Ubuntu/Debian
sudo apt install maven

# Windows
# Download from: https://maven.apache.org/download.cgi
# Add to PATH

# macOS
brew install maven
```

#### 3. Install MySQL
```bash
# Ubuntu/Debian
sudo apt install mysql-server

# Windows
# Download from: https://dev.mysql.com/downloads/installer/

# macOS
brew install mysql
```

#### 4. Configure MySQL
```sql
CREATE DATABASE vjutest;
CREATE USER 'vjutest_user'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON vjutest.* TO 'vjutest_user'@'localhost';
FLUSH PRIVILEGES;
```

#### 5. Configure Environment Variables
Copy `.env.example` to `.env` and update the values:
```bash
cp .env.example .env
```

Edit `.env` with your configuration:
```env
DB_URL=jdbc:mysql://localhost:3306/vjutest
DB_USERNAME=vjutest_user
DB_PASSWORD=your_password
JWT_SECRET=your_jwt_secret
```

#### 6. Build and Run
```bash
# Build the project
mvn clean install

# Run the application
mvn spring-boot:run
```

### Option 2: Docker Setup

#### 1. Install Docker
```bash
# Ubuntu/Debian
sudo apt update
sudo apt install docker.io docker-compose

# Windows
# Download from: https://www.docker.com/products/docker-desktop

# macOS
brew install docker docker-compose
```

#### 2. Start Docker Service
```bash
# Ubuntu/Debian
sudo systemctl start docker
sudo systemctl enable docker

# Windows/macOS
# Docker Desktop should start automatically
```

#### 3. Build and Run with Docker
```bash
# Build and start containers
docker-compose up --build

# Run in detached mode
docker-compose up -d

# Stop containers
docker-compose down
```

## 📁 Project Structure

```
vjutest/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/vjutest/
│   │   │       ├── config/          # Configuration classes
│   │   │       ├── controller/      # REST controllers
│   │   │       ├── dto/             # Data Transfer Objects
│   │   │       ├── entity/          # JPA entities
│   │   │       ├── repository/      # JPA repositories
│   │   │       ├── service/         # Business logic
│   │   │       └── security/        # Security configuration
│   │   └── resources/
│   │       └── application.properties
│   └── test/                        # Test classes
├── Dockerfile
├── docker-compose.yml
└── pom.xml
```

## 🔧 Configuration

### Database Configuration
The application uses MySQL. Configure the connection in `application.properties`:
```properties
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
```

### JWT Configuration
Configure JWT settings in `application.properties`:
```properties
jwt.secret=${JWT_SECRET}
jwt.expiration=86400000
```

### Cloudinary Configuration
Configure Cloudinary in `application.properties`:
```properties
cloudinary.cloud-name=your_cloud_name
cloudinary.api-key=your_api_key
cloudinary.api-secret=your_api_secret
```

## 🐳 Docker Configuration

### Dockerfile
```dockerfile
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/*.jar app.jar
ENTRYPOINT ["java","-jar","app.jar"]
```

### docker-compose.yml
```yaml
version: '3.8'
services:
  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      - DB_URL=jdbc:mysql://db:3306/vjutest
      - DB_USERNAME=vjutest_user
      - DB_PASSWORD=your_password
      - JWT_SECRET=your_jwt_secret
    depends_on:
      - db

  db:
    image: mysql:8.0
    environment:
      - MYSQL_DATABASE=vjutest
      - MYSQL_USER=vjutest_user
      - MYSQL_PASSWORD=your_password
      - MYSQL_ROOT_PASSWORD=root_password
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql

volumes:
  mysql_data:
```

## 🔍 API Documentation

The API documentation is available at:
- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs

## 🧪 Testing

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=TestClassName

# Run specific test method
mvn test -Dtest=TestClassName#testMethodName
```

## 🔐 Security

The application uses Spring Security with JWT authentication. Key security features:
- Password encryption
- JWT token-based authentication
- Role-based authorization
- CORS configuration
- CSRF protection

## 📝 Logging

Logs are configured in `application.properties`:
```properties
logging.level.root=INFO
logging.level.com.example.vjutest=DEBUG
logging.file.name=logs/application.log
```

## 🚨 Troubleshooting

### Common Issues

1. **Port Conflicts**
```bash
# Check if port 8080 is in use
sudo lsof -i :8080
# Kill the process
sudo kill -9 <PID>
```

2. **Database Connection Issues**
- Verify MySQL is running
- Check database credentials
- Ensure database exists

3. **Docker Issues**
```bash
# Remove all containers and volumes
docker-compose down -v

# Rebuild and start
docker-compose up --build
```

4. **Maven Build Issues**
```bash
# Clean Maven cache
mvn clean

# Update dependencies
mvn dependency:purge-local-repository
```

## 🤝 Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details. 