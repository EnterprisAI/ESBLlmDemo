# ESB LLM Demo - JSON Transformation Service

This Spring Boot application provides a comprehensive JSON transformation service that maps between different JSON structures using **MapStruct** (annotation-based mapping library) and includes **reverse engineering** capabilities to generate JSON mapping rules from existing MapStruct annotations. Additionally, it features a **generic mapping rules generator** that can analyze any source and target JSON structures to automatically generate mapping rules.

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
│   ├── JsonTransformationService.java              # Main transformation service
│   ├── MappingRulesGeneratorService.java           # Reverse engineering service
│   ├── GenericMappingRulesGeneratorService.java    # Generic mapping rules generator
│   └── JsonTransformationServiceTest.java          # Test demonstration
├── controller/                    # REST API controllers
│   └── JsonTransformationController.java           # REST endpoints
└── EsbLlmDemoApplication.java     # Main Spring Boot application
```

## Features

- **JSON Transformation**: Transform complex nested JSON structures using MapStruct
- **Reverse Engineering**: Generate JSON mapping rules from existing MapStruct annotations
- **Generic Mapping Rules Generation**: Automatically generate mapping rules by comparing any source and target JSON structures
- **JSON Structure Analysis**: Analyze JSON structures and generate documentation mapping rules
- **REST API**: Expose transformation functionality via REST endpoints
- **Mapping Rules**: Configurable mapping rules for different transformation scenarios
- **Validation**: Input validation and error handling
- **Test Suite**: Built-in test demonstration with sample data
- **Web Interface**: User-friendly web interface with tabbed functionality

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

## Generic Mapping Rules Generation

The application includes a **generic mapping rules generator** that can analyze any source and target JSON structures to automatically generate mapping rules.

### Features

- **Automatic Field Matching**: Intelligently matches fields between source and target JSON
- **Nested Object Support**: Handles complex nested object structures
- **Array Mapping**: Supports array-to-array transformations
- **Smart Field Detection**: Uses direct, case-insensitive, and partial matching
- **Unmapped Field Detection**: Identifies fields that don't have clear mappings

### Example Generic Generation

**Source JSON:**
```json
{
  "id": "123",
  "title": "Sample Title",
  "description": "Sample Description",
  "metadata": {
    "created": "2023-01-01",
    "updated": "2023-01-02"
  }
}
```

**Target JSON:**
```json
{
  "documentId": "123",
  "documentTitle": "Sample Title",
  "documentDescription": "Sample Description",
  "documentMetadata": {
    "creationDate": "2023-01-01",
    "lastModified": "2023-01-02"
  }
}
```

**Generated Mapping Rules:**
```json
{
  "sourceContentType": "JSON",
  "targetContentType": "JSON",
  "conversionRules": [
    {
      "propID": "RootObject",
      "sourceLocation": "$",
      "targetLocation": "$",
      "isArray": false,
      "items": [
        {
          "propID": "ROOT_DOCUMENTID",
          "sourceLocation": "id",
          "targetLocation": "documentId",
          "isArray": false,
          "items": null
        },
        {
          "propID": "ROOT_DOCUMENTTITLE",
          "sourceLocation": "title",
          "targetLocation": "documentTitle",
          "isArray": false,
          "items": null
        },
        {
          "propID": "ROOT_DOCUMENTDESCRIPTION",
          "sourceLocation": "description",
          "targetLocation": "documentDescription",
          "isArray": false,
          "items": null
        },
        {
          "propID": "ROOT_DOCUMENTMETADATA",
          "sourceLocation": "metadata",
          "targetLocation": "documentMetadata",
          "isArray": false,
          "items": [
            {
              "propID": "ROOT_DOCUMENTMETADATA_CREATIONDATE",
              "sourceLocation": "created",
              "targetLocation": "creationDate",
              "isArray": false,
              "items": null
            },
            {
              "propID": "ROOT_DOCUMENTMETADATA_LASTMODIFIED",
              "sourceLocation": "updated",
              "targetLocation": "lastModified",
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

### 3. Generate Mapping Rules from JSON Comparison (Generic)
```
POST /api/transform/rules/generate-from-json
Content-Type: application/json

{
  "sourceJson": "your_source_json_string_here",
  "targetJson": "your_target_json_string_here"
}
```

### 4. Analyze JSON Structure
```
POST /api/transform/rules/analyze-structure
Content-Type: application/json

{
  "jsonStructure": "your_json_structure_here"
}
```

### 5. Get Default Mapping Rules
```
GET /api/transform/rules/default
```

### 6. Health Check
```
GET /api/transform/health
```

## Web Interface

The application provides a comprehensive web interface with three main tabs:

### 1. JSON Transformation Tab
- Transform source JSON using MapStruct
- Load sample data
- View transformation results

### 2. Mapping Rules Generation Tab
- Generate rules from MapStruct annotations
- Generate rules from JSON comparison
- Side-by-side JSON input for comparison

### 3. Structure Analysis Tab
- Analyze JSON structure for documentation
- Generate mapping rules for structure analysis
- Load sample complex structures

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

### Test Generic Mapping Rules Generation
Use the web interface at `http://localhost:8082` and navigate to the "Mapping Rules Generation" tab to test the generic functionality.

## Dependencies

- **Spring Boot 3.5.0**: Main framework
- **MapStruct 1.5.5.Final**: Annotation-based mapping library
- **Jackson**: JSON processing

## Configuration

The mapping rules are defined in `EmployeeMapper.java` using MapStruct annotations and can be reverse-engineered into JSON format using the `MappingRulesGeneratorService`. The generic mapping rules are generated dynamically using the `GenericMappingRulesGeneratorService`.

## Error Handling

The application includes comprehensive error handling:
- Input validation for JSON format
- Transformation error handling
- REST API error responses
- Detailed error messages for debugging
- Field mapping validation

## Extensibility

The architecture is designed to be easily extensible:
- Add new model classes for different data structures
- Create new MapStruct mappers for different transformation scenarios
- Extend the service layer with additional transformation logic
- Add new REST endpoints for specific use cases
- Reverse engineer mapping rules from any MapStruct mapper
- Use generic mapping rules generation for any JSON structure

## Testing

The application includes:
- Unit tests for the transformation service
- Unit tests for the reverse engineering service
- Unit tests for the generic mapping rules generator
- Integration tests for REST endpoints
- Built-in demonstration with sample data
- Validation tests for input data

Run tests with:
```bash
./gradlew test
```

## Generic Mapping Rules Generation Workflow

1. **Input Source and Target JSON**: Provide source and target JSON structures
2. **Automatic Analysis**: The service analyzes both structures
3. **Field Matching**: Intelligently matches fields using multiple strategies
4. **Rule Generation**: Creates comprehensive mapping rules
5. **Output**: Returns structured mapping rules in JSON format

### Field Matching Strategies

1. **Direct Match**: Exact field name match
2. **Case-Insensitive Match**: Ignores case differences
3. **Partial Match**: Matches fields that contain similar terms
4. **Unmapped Detection**: Identifies fields without clear mappings

## Reverse Engineering Workflow

1. **Define MapStruct Mapper**: Create mapper interfaces with `@Mapping` annotations
2. **Generate Implementation**: MapStruct generates implementation at compile time
3. **Reverse Engineer Rules**: Use the service to extract mapping rules from annotations
4. **Use Generated Rules**: Apply the generated rules for documentation or other systems

This approach provides a **bidirectional workflow** where you can:
- Start with MapStruct annotations and generate JSON rules
- Start with JSON rules and implement MapStruct mappers
- Use generic generation for any JSON structure
- Maintain consistency between both representations 