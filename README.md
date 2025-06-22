# ESB LLM Demo - Advanced JSON Transformation Service with AI-Powered Mapping

This Spring Boot application provides a comprehensive JSON transformation service that maps between different JSON structures using **MapStruct** (annotation-based mapping library) and includes **AI-powered reverse engineering** capabilities using **Groq LLM** to generate JSON mapping rules from existing MapStruct annotations. The system can analyze complex mapper interfaces, helper classes, and business logic to generate comprehensive mapping documentation.

## 🚀 Key Features

- **AI-Powered Mapping Analysis**: Uses Groq LLM to analyze MapStruct mappers and extract complex business logic
- **JSON Transformation**: Transform complex nested JSON structures using MapStruct
- **Reverse Engineering**: Generate JSON mapping rules from existing MapStruct annotations with AI assistance
- **Helper Class Analysis**: Automatically detects and analyzes helper classes with custom business logic
- **Business Rule Extraction**: Identifies custom calculations, transformations, and validation rules
- **Collection Handling**: Supports complex collection mappings with item-level transformations
- **Nested Mapping Detection**: Automatically detects nested property mappings (e.g., `address.country`)
- **Post-Processing Rules**: Extracts `@AfterMapping` methods and their effects
- **REST API**: Comprehensive REST endpoints for all transformation operations
- **Web Interface**: User-friendly web interface with real-time testing capabilities

## 🏗️ Project Structure

```
src/main/java/com/esb/llm/ESBLlmDemo/
├── model/                          # Data models
│   ├── Employee.java              # Source employee model
│   ├── Address.java               # Address model
│   ├── OfficeDetails.java         # Office details model
│   ├── WorkExperience.java        # Work experience model
│   ├── TargetEmployee.java        # Target employee model
│   ├── TargetWorkExperience.java  # Target work experience model
│   ├── SourceDto.java             # Source DTO for testing
│   ├── TargetDto.java             # Target DTO for testing
│   ├── User.java                  # User model
│   ├── TargetUser.java            # Target user model
│   ├── Product.java               # Product model
│   ├── TargetProduct.java         # Target product model
│   ├── Order.java                 # Order model
│   └── TargetOrder.java           # Target order model
├── mapper/                        # MapStruct mappers
│   ├── EmployeeMapper.java        # Employee to TargetEmployee mapper
│   ├── SourceTargetMapper.java    # Source to Target DTO mapper with helper
│   ├── UserMapper.java            # User mapper
│   ├── ProductMapper.java         # Product mapper
│   ├── OrderMapper.java           # Order mapper
│   └── UserMapperHelp.java        # Helper class with business logic
├── config/                        # Configuration classes
│   └── MappingRules.java          # JSON mapping rules configuration
├── service/                       # Business logic services
│   ├── JsonTransformationService.java              # Main transformation service
│   ├── EnhancedMapStructJsonRuleGeneratorService.java  # AI-powered mapping analysis
│   ├── MapStructJsonRuleGeneratorService.java      # Basic mapping analysis
│   ├── MapStructBasedMappingRulesGeneratorService.java # MapStruct-based generator
│   ├── GenericMappingRulesGeneratorService.java    # Generic mapping rules generator
│   └── JsonTransformationServiceTest.java          # Test demonstration
├── controller/                    # REST API controllers
│   ├── JsonTransformationController.java           # Basic transformation endpoints
│   ├── EnhancedJsonRuleGeneratorController.java    # AI-powered mapping endpoints
│   ├── JsonRuleGeneratorController.java            # Mapping rule endpoints
│   └── SourceTargetController.java                 # Source/Target testing endpoints
└── EsbLlmDemoApplication.java     # Main Spring Boot application
```

## 🔧 Technology Stack

- **Spring Boot 3.5.0**: Main application framework
- **MapStruct**: Annotation-based object mapping
- **Groq LLM API**: AI-powered code analysis (llama3-8b-8192 model)
- **Jackson**: JSON processing
- **Gradle**: Build tool
- **Java 21**: Programming language

## 🚀 Quick Start

### Prerequisites

- Java 21 or higher
- Gradle 8.0 or higher
- Groq API key (for AI-powered features)

### Installation

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd ESBLlmDemo
   ```

2. **Configure Groq API Key**
   ```bash
   # The API key is already configured in the service
   # You can update it in EnhancedMapStructJsonRuleGeneratorService.java if needed
   ```

3. **Build the project**
   ```bash
   ./gradlew build
   ```

4. **Run the application**
   ```bash
   ./gradlew bootRun
   ```

5. **Access the application**
   - Web Interface: http://localhost:8183
   - API Base URL: http://localhost:8183/api

## 📡 REST API Endpoints

### 1. Basic JSON Transformation

#### Transform JSON using predefined rules
```http
POST /api/transform
Content-Type: application/json

{
  "sourceJson": "[{\"employeeId\":\"E001\",\"name\":\"John Doe\",...}]"
}
```

#### Transform JSON with custom rules
```http
POST /api/transform-with-rules
Content-Type: application/json

{
  "sourceJson": "[{\"employeeId\":\"E001\",\"name\":\"John Doe\",...}]",
  "mappingRules": {
    "conversionRules": [...]
  }
}
```

### 2. AI-Powered Mapping Analysis

#### Generate JSON rules from mapper class name (AI-powered)
```http
POST /api/enhanced/generate-rules
Content-Type: application/json

{
  "mapperName": "SourceTargetMapper"
}
```

#### Generate JSON rules for all available mappers
```http
GET /api/enhanced/generate-all-rules
```

#### Get available mappers list
```http
GET /api/enhanced/available-mappers
```

### 3. Basic Mapping Analysis

#### Generate JSON rules from mapper class name
```http
POST /api/generate-rules
Content-Type: application/json

{
  "mapperName": "EmployeeMapper"
}
```

#### Generate mapping rules from source and target JSON
```http
POST /api/generate-rules-from-json
Content-Type: application/json

{
  "sourceJson": "[{\"employeeId\":\"E001\",\"name\":\"John Doe\",...}]",
  "targetJson": "[{\"employeeId\":\"E001\",\"employeename\":\"John Doe\",...}]"
}
```

### 4. Source/Target Testing

#### Test SourceTargetMapper transformation
```http
POST /api/source-target/transform
Content-Type: application/json

{
  "sourceDto": {
    "id": "123",
    "name": "John Doe",
    "age": 30,
    "salary": 50000,
    "doj": "2020-01-01",
    "emailList": ["john@example.com"],
    "phoneNumbers": ["1234567890"]
  }
}
```

#### Get sample source data
```http
GET /api/source-target/sample-source
```

#### Get sample target data
```http
GET /api/source-target/sample-target
```

## 🧪 Testing the Endpoints

### 1. Using cURL

#### Test AI-powered mapping analysis
```bash
curl -X POST http://localhost:8183/api/enhanced/generate-rules \
  -H "Content-Type: application/json" \
  -d '{"mapperName": "SourceTargetMapper"}'
```

#### Test JSON transformation
```bash
curl -X POST http://localhost:8183/api/transform \
  -H "Content-Type: application/json" \
  -d '{
    "sourceJson": "[{\"employeeId\":\"E001\",\"name\":\"John Doe\",\"gender\":\"Male\",\"age\":30,\"address\":{\"country\":\"USA\"},\"officeDetails\":{\"location\":\"New York HQ\"},\"workExperience\":[{\"company\":\"ABC Tech\"}]}]"
  }'
```

#### Test SourceTargetMapper with helper class
```bash
curl -X POST http://localhost:8183/api/source-target/transform \
  -H "Content-Type: application/json" \
  -d '{
    "sourceDto": {
      "id": "123",
      "name": "John Doe",
      "age": 30,
      "salary": 50000,
      "doj": "2020-01-01",
      "emailList": ["john@example.com"],
      "phoneNumbers": ["1234567890"]
    }
  }'
```

### 2. Using Web Interface

1. Open http://localhost:8183 in your browser
2. Navigate to the "AI-Powered Mapping Analysis" tab
3. Select a mapper from the dropdown (e.g., "SourceTargetMapper")
4. Click "Generate Rules" to see AI-analyzed mapping rules
5. View the comprehensive JSON output with business logic extraction

### 3. Using Postman

Import these sample requests:

#### AI-Powered Mapping Analysis
```json
{
  "method": "POST",
  "url": "http://localhost:8183/api/enhanced/generate-rules",
  "headers": {
    "Content-Type": "application/json"
  },
  "body": {
    "mode": "raw",
    "raw": "{\"mapperName\": \"SourceTargetMapper\"}"
  }
}
```

#### JSON Transformation
```json
{
  "method": "POST",
  "url": "http://localhost:8183/api/transform",
  "headers": {
    "Content-Type": "application/json"
  },
  "body": {
    "mode": "raw",
    "raw": "{\"sourceJson\": \"[{\\\"employeeId\\\":\\\"E001\\\",\\\"name\\\":\\\"John Doe\\\",\\\"gender\\\":\\\"Male\\\",\\\"age\\\":30,\\\"address\\\":{\\\"country\\\":\\\"USA\\\"},\\\"officeDetails\\\":{\\\"location\\\":\\\"New York HQ\\\"},\\\"workExperience\\\":[{\\\"company\\\":\\\"ABC Tech\\\"}]}]\"}"
  }
}
```

## 🔍 AI-Powered Features

### 1. Enhanced Mapping Analysis

The AI-powered service can analyze complex MapStruct mappers and extract:

- **Mapping Annotations**: All `@Mapping` annotations with source/target properties
- **Nested Mappings**: Properties with dots (e.g., `address.country`)
- **Collection Mappings**: List, Set, and array processing
- **Helper Classes**: `@Mapper(uses = {...})` helper classes and their business logic
- **Business Rules**: Custom calculations, transformations, and validation rules
- **Post-Processing**: `@AfterMapping` methods and their effects
- **Dependencies**: Relationships between mapping rules

### 2. Example AI Analysis Output

For the `SourceTargetMapper` with `UserMapperHelp` helper class:

```json
{
  "sourceContentType": "JSON",
  "targetContentType": "JSON",
  "mapperInfo": {
    "mapperName": "SourceTargetMapper",
    "helperClasses": ["UserMapperHelp"],
    "afterMappingMethods": ["setAdjustedSalary"],
    "hasNestedMappings": false,
    "hasCollections": true
  },
  "conversionRules": [
    {
      "propID": "USER_ID",
      "sourceLocation": "id",
      "targetLocation": "userId",
      "isArray": false,
      "nestedMapping": false,
      "transformationType": "direct",
      "businessRule": "",
      "helperClass": "",
      "helperMethod": "",
      "collectionType": null,
      "itemTransformation": "",
      "validationRules": [],
      "defaultValue": "",
      "description": "Maps id from source to userId in target"
    },
    {
      "propID": "EMAILS",
      "sourceLocation": "emailList",
      "targetLocation": "emails",
      "isArray": true,
      "nestedMapping": false,
      "transformationType": "collection",
      "businessRule": "",
      "helperClass": "",
      "helperMethod": "",
      "collectionType": "List",
      "itemTransformation": "",
      "validationRules": [],
      "defaultValue": "",
      "description": "Maps emailList from source to emails in target"
    }
  ],
  "postProcessingRules": [
    {
      "methodName": "setAdjustedSalary",
      "description": "Calculates adjusted salary using UserMapperHelp helper class",
      "dependencies": ["SALARY", "AGE", "DOJ"],
      "businessLogic": "Uses UserMapperHelp.calculateAdjustedSalary() to apply experience bonus, performance multiplier, and market adjustment factors"
    }
  ],
  "businessRules": [
    {
      "ruleId": "SALARY_ADJUSTMENT",
      "description": "Salary adjustment based on experience and performance",
      "logic": "Calculates experience bonus (5% per year, capped at 50%), performance multiplier based on age and experience, and market adjustment factor",
      "appliesTo": ["SALARY"]
    }
  ]
}
```

### 3. Helper Class Analysis

The AI can analyze helper classes like `UserMapperHelp` and extract:

- **Custom Calculation Methods**: `calculateAdjustedSalary()`, `calculateExperienceBonus()`
- **Business Logic**: Performance multipliers, market adjustments
- **Validation Rules**: Null checks, boundary conditions
- **Mathematical Operations**: Complex salary calculations with multiple factors

## 📊 Available Mappers

The system includes several pre-configured mappers for testing:

1. **EmployeeMapper**: Basic employee transformation with nested mappings
2. **SourceTargetMapper**: Complex mapping with helper class and business logic
3. **UserMapper**: User data transformation
4. **ProductMapper**: Product catalog transformation
5. **OrderMapper**: Order processing transformation

## 🔧 Configuration

### Application Properties

```properties
# Server configuration
server.port=8183

# Logging configuration
logging.level.com.esb.llm=DEBUG
logging.level.org.springframework.web=DEBUG

# Groq API configuration (configured in service)
groq.api.key=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
groq.model=llama3-8b-8192
```

### Customizing Mappers

To add new mappers:

1. Create mapper interface in `src/main/java/com/esb/llm/ESBLlmDemo/mapper/`
2. Add `@Mapper` annotation with helper classes if needed
3. Define mapping methods with `@Mapping` annotations
4. Add `@AfterMapping` methods for post-processing if required
5. Update `getAvailableMappers()` method in `EnhancedMapStructJsonRuleGeneratorService`

## 🧪 Testing Scenarios

### 1. Basic Transformation Test

```bash
# Test employee transformation
curl -X POST http://localhost:8183/api/transform \
  -H "Content-Type: application/json" \
  -d @test-data/employee-source.json
```

### 2. AI Analysis Test

```bash
# Test AI-powered analysis of SourceTargetMapper
curl -X POST http://localhost:8183/api/enhanced/generate-rules \
  -H "Content-Type: application/json" \
  -d '{"mapperName": "SourceTargetMapper"}'
```

### 3. Helper Class Test

```bash
# Test transformation with helper class business logic
curl -X POST http://localhost:8183/api/source-target/transform \
  -H "Content-Type: application/json" \
  -d @test-data/source-dto.json
```

## 🐛 Troubleshooting

### Common Issues

1. **Port Already in Use**
   ```bash
   # Kill process using port 8183
   netstat -ano | findstr :8183
   taskkill /PID <PID> /F
   ```

2. **Groq API Errors**
   - Check API key configuration
   - Verify network connectivity
   - Check API rate limits

3. **Build Errors**
   ```bash
   # Clean and rebuild
   ./gradlew clean build
   ```

### Debug Mode

Enable debug logging:

```properties
logging.level.com.esb.llm=DEBUG
logging.level.org.springframework.web=DEBUG
```

## 📈 Performance Considerations

- **AI Analysis**: Groq API calls may take 1-3 seconds
- **Large JSON**: For large JSON structures, consider chunking
- **Caching**: Consider implementing caching for frequently used mappers
- **Rate Limiting**: Groq API has rate limits; implement retry logic for production

## 🔒 Security Considerations

- **API Key**: Store Groq API key securely (use environment variables in production)
- **Input Validation**: All inputs are validated before processing
- **Error Handling**: Comprehensive error handling prevents information leakage

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Add tests for new functionality
4. Ensure all tests pass
5. Submit a pull request

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🆘 Support

For issues and questions:
1. Check the troubleshooting section
2. Review the API documentation
3. Create an issue in the repository
4. Contact the development team

---

**Note**: This application demonstrates advanced JSON transformation capabilities with AI-powered analysis. The Groq LLM integration provides intelligent mapping rule generation that can understand complex business logic and helper classes. 