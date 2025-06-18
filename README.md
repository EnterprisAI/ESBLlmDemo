# ESB LLM Demo - JSON Transformation Service

This Spring Boot application provides a comprehensive JSON transformation service that maps between different JSON structures using **MapStruct** (annotation-based mapping library) and includes **reverse engineering** capabilities to generate JSON mapping rules from existing MapStruct annotations.

## Project Structure

```
src/main/java/com/esb/llm/ESBLlmDemo/
├── model/                          # Data models
│   ├── Employee.java              # Source employee model
│   ├── Address.java               # Address model
│   ├── OfficeDetails.java         # Office details model
│   ├── WorkExperience.java        # Work experience model
│   ├── TargetEmployee.java        # Target employee model
│   └── TargetWorkExperience.java  # Target work experience model
├── mapper/                        # MapStruct mappers
│   └── EmployeeMapper.java        # Employee to TargetEmployee mapper
├── config/                        # Configuration classes
│   └── MappingRules.java          # JSON mapping rules configuration
├── service/                       # Business logic services
│   ├── JsonTransformationService.java          # Main transformation service
│   ├── MappingRulesGeneratorService.java       # Reverse engineering service
│   └── JsonTransformationServiceTest.java      # Test demonstration
├── controller/                    # REST API controllers
│   └── JsonTransformationController.java       # REST endpoints
└── EsbLlmDemoApplication.java     # Main Spring Boot application
```

## Features

- **JSON Transformation**: Transform complex nested JSON structures using MapStruct
- **Reverse Engineering**: Generate JSON mapping rules from existing MapStruct annotations
- **REST API**: Expose transformation functionality via REST endpoints
- **Mapping Rules**: Configurable mapping rules for different transformation scenarios
- **Validation**: Input validation and error handling
- **Test Suite**: Built-in test demonstration with sample data

## Mapping Rules

The application implements the following JSON transformation rules:

### Source to Target Mapping

| Source Field | Target Field | Description |
|--------------|--------------|-------------|
| `employeeId` | `employeeId` | Employee ID (unchanged) |
| `name` | `employeename` | Employee name |
| `age` | `age` | Employee age (unchanged) |
| `gender` | `gender` | Employee gender (unchanged) |
| `address.country` | `emplocation` | Employee location (from address) |
| `officeDetails.location` | `officelocation` | Office location |
| `workExperience[*].company` | `workExperience[*].company` | Work experience companies only |

### Example Transformation

**Source JSON:**
```json
[
  {
    "employeeId": "E001",
    "name": "John Doe",
    "gender": "Male",
    "age": 30,
    "address": {
      "street": "123 Main St",
      "city": "New York",
      "state": "NY",
      "zipcode": "10001",
      "country": "USA"
    },
    "officeDetails": {
      "department": "Engineering",
      "designation": "Software Developer",
      "location": "New York HQ",
      "employeeType": "Full-Time"
    },
    "workExperience": [
      {
        "company": "ABC Tech",
        "role": "Software Engineer",
        "startDate": "2018-01-01",
        "endDate": "2020-12-31",
        "location": "Chicago"
      }
    ]
  }
]
```

**Target JSON:**
```json
[
  {
    "employeeId": "E001",
    "employeename": "John Doe",
    "age": 30,
    "gender": "Male",
    "emplocation": "USA",
    "officelocation": "New York HQ",
    "workExperience": [
      {
        "company": "ABC Tech"
      }
    ]
  }
]
```

## Reverse Engineering

The application includes a **reverse engineering** feature that analyzes MapStruct annotations and generates JSON mapping rules automatically.

### Generated Mapping Rules

When you call the reverse engineering endpoint, it generates rules like this:

```json
{
  "sourceContentType": "JSON",
  "targetContentType": "JSON",
  "conversionRules": [
    {
      "propID": "EmployeeList",
      "sourceLocation": "$",
      "targetLocation": "$",
      "isArray": true,
      "items": [
        {
          "propID": "EMPLOYEE_ID",
          "sourceLocation": "employeeId",
          "targetLocation": "employeeId",
          "isArray": false,
          "items": null
        },
        {
          "propID": "EMPLOYEE_NAME",
          "sourceLocation": "name",
          "targetLocation": "employeename",
          "isArray": false,
          "items": null
        },
        {
          "propID": "AGE",
          "sourceLocation": "age",
          "targetLocation": "age",
          "isArray": false,
          "items": null
        },
        {
          "propID": "GENDER",
          "sourceLocation": "gender",
          "targetLocation": "gender",
          "isArray": false,
          "items": null
        },
        {
          "propID": "EMP_LOCATION",
          "sourceLocation": "address.country",
          "targetLocation": "emplocation",
          "isArray": false,
          "items": null
        },
        {
          "propID": "OFFICE_LOCATION",
          "sourceLocation": "officeDetails.location",
          "targetLocation": "officelocation",
          "isArray": false,
          "items": null
        },
        {
          "propID": "WORK_EXP",
          "sourceLocation": "workExperience",
          "targetLocation": "workExperience",
          "isArray": true,
          "items": [
            {
              "propID": "WORK_EXP_COMPANY",
              "sourceLocation": "company",
              "targetLocation": "company",
              "isArray": false,
              "items": null
            }
          ]
        }
      ]
    }
  ]
}
```

## API Endpoints

### 1. Transform JSON (MapStruct)
```
POST /api/transform/json
Content-Type: application/json

{
  "sourceJson": "your_source_json_string_here"
}
```

### 2. Generate Mapping Rules (Reverse Engineering)
```
GET /api/transform/rules/generate
```

### 3. Get Default Mapping Rules
```
GET /api/transform/rules/default
```

### 4. Health Check
```
GET /api/transform/health
```

## Running the Application

### Prerequisites
- Java 21 or higher
- Gradle 7.0 or higher

### Build and Run
```bash
# Build the project
./gradlew build

# Run the application
./gradlew bootRun
```

The application will start on `http://localhost:8082`

### Test the Transformation
The application includes a built-in test that runs automatically on startup, demonstrating the transformation with the provided sample data.

### Test Reverse Engineering
Use the web interface at `http://localhost:8082` and click "Generate Mapping Rules" to see the reverse engineering in action.

## Dependencies

- **Spring Boot 3.5.0**: Main framework
- **MapStruct 1.5.5.Final**: Annotation-based mapping library
- **Jackson**: JSON processing

## Configuration

The mapping rules are defined in `EmployeeMapper.java` using MapStruct annotations and can be reverse-engineered into JSON format using the `MappingRulesGeneratorService`.

## Error Handling

The application includes comprehensive error handling:
- Input validation for JSON format
- Transformation error handling
- REST API error responses
- Detailed error messages for debugging

## Extensibility

The architecture is designed to be easily extensible:
- Add new model classes for different data structures
- Create new MapStruct mappers for different transformation scenarios
- Extend the service layer with additional transformation logic
- Add new REST endpoints for specific use cases
- Reverse engineer mapping rules from any MapStruct mapper

## Testing

The application includes:
- Unit tests for the transformation service
- Unit tests for the reverse engineering service
- Integration tests for REST endpoints
- Built-in demonstration with sample data
- Validation tests for input data

Run tests with:
```bash
./gradlew test
```

## Reverse Engineering Workflow

1. **Define MapStruct Mapper**: Create mapper interfaces with `@Mapping` annotations
2. **Generate Implementation**: MapStruct generates implementation at compile time
3. **Reverse Engineer Rules**: Use the service to extract mapping rules from annotations
4. **Use Generated Rules**: Apply the generated rules for documentation or other systems

This approach provides a **bidirectional workflow** where you can:
- Start with MapStruct annotations and generate JSON rules
- Start with JSON rules and implement MapStruct mappers
- Maintain consistency between both representations 