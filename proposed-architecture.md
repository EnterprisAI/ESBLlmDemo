# Proposed Architecture: Hybrid Java + Python LLM Orchestration for MapStruct Analysis

## Executive Summary

This document outlines a proposed hybrid architecture that leverages Java's strengths for static code analysis and Python's ecosystem for LLM orchestration, specifically designed to reduce costs while maintaining accuracy in MapStruct mapper analysis.

## Problem Statement

### Current Challenges
1. **High API Costs**: Direct calls to remote LLMs (Groq) for every mapping analysis
2. **Limited Local LLM Usage**: Underutilization of local 7B models for routine tasks
3. **Complex Mapping Detection**: Difficulty in identifying custom mappings (expressions, qualifiedByName, @AfterMapping)
4. **Inconsistent Results**: LLM hallucinations and missing fields in output JSON
5. **Scalability Issues**: Single-threaded processing and no caching mechanisms

### Goals
- Reduce remote LLM API calls by 70-80%
- Leverage local 7B models for routine tasks
- Improve accuracy of custom mapping detection
- Implement intelligent fallback mechanisms
- Add vector database for similarity-based caching

## Proposed Architecture

### 1. System Overview

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   Java Service  │    │  Python Service  │    │   Vector DB     │
│  (Information   │◄──►│  (LLM Orchestrator)│◄──►│   (Chroma/     │
│   Extractor)    │    │                  │    │   Pinecone)     │
└─────────────────┘    └──────────────────┘    └─────────────────┘
         │                       │                       │
         │                       │                       │
         ▼                       ▼                       ▼
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│  JavaParser     │    │   LangGraph      │    │   Embeddings    │
│  Analysis       │    │   Workflows      │    │   & Similarity  │
└─────────────────┘    └──────────────────┘    └─────────────────┘
```

### 2. Component Breakdown

#### A. Java Service (Information Extractor)

**Purpose**: Static code analysis and information extraction from MapStruct mappers

**Key Components**:
- **JavaParser Engine**: Parse source code and extract @Mapping annotations
- **Custom Logic Detector**: Identify expressions, qualifiedByName, @AfterMapping methods
- **Dependency Analyzer**: Find helper classes and utility methods
- **Type System Analyzer**: Determine field types, arrays, nested objects

**Responsibilities**:
1. Parse MapStruct mapper interfaces using JavaParser
2. Extract explicit @Mapping annotations with source/target pairs
3. Identify custom mappings (expression, qualifiedByName)
4. Analyze @AfterMapping methods and their dependencies
5. Generate structured metadata for Python service

**Output Format**:
```json
{
  "mapperInfo": {
    "className": "SourceTargetMapper",
    "sourceClass": "SourceDto",
    "targetClass": "TargetDto"
  },
  "directMappings": [
    {
      "source": "id",
      "target": "userId",
      "type": "direct"
    }
  ],
  "customMappings": [
    {
      "source": "salary",
      "target": "salary",
      "type": "expression",
      "expression": "calculateSalaryWithAdjustment(sourceDto)",
      "dependencies": ["UserMapperHelp"]
    }
  ],
  "afterMappingMethods": [
    {
      "methodName": "setAdjustedSalary",
      "referencedFields": ["salary", "age", "doj"],
      "helperClass": "UserMapperHelp"
    }
  ]
}
```

#### B. Python Service (LLM Orchestrator)

**Purpose**: Intelligent LLM orchestration with cost optimization

**Key Components**:
- **LangGraph Workflows**: Complex decision trees and routing logic
- **Model Router**: Intelligent selection between local and remote LLMs
- **Vector Database Integration**: Similarity-based caching and retrieval
- **Fallback Manager**: Automatic escalation to more capable models

**Responsibilities**:
1. Receive structured data from Java service
2. Determine task complexity and select appropriate LLM
3. Generate prompts optimized for selected model
4. Handle fallbacks and retries
5. Cache results in vector database

#### C. Vector Database

**Purpose**: Similarity-based caching and retrieval

**Key Features**:
- **Embedding Storage**: Store embeddings of mapper structures
- **Similarity Search**: Find similar mappings for reuse
- **Cost Tracking**: Monitor API usage and costs
- **Performance Metrics**: Track accuracy and response times

## 3. Detailed Workflow

### Step 1: Java Information Extraction

```java
// Enhanced mapper parsing with JavaParser
public class MapperAnalyzer {
    public MapperAnalysisResult analyzeMapper(String mapperClassName) {
        // Parse source code with JavaParser
        CompilationUnit cu = StaticJavaParser.parse(sourceFile);
        
        // Extract @Mapping annotations
        List<MappingInfo> directMappings = extractDirectMappings(cu);
        
        // Identify custom mappings
        List<CustomMappingInfo> customMappings = extractCustomMappings(cu);
        
        // Analyze @AfterMapping methods
        List<AfterMappingInfo> afterMappings = extractAfterMappings(cu);
        
        return new MapperAnalysisResult(directMappings, customMappings, afterMappings);
    }
}
```

### Step 2: Python LLM Orchestration

```python
# LangGraph workflow for LLM orchestration
@graph
def mapper_analysis_workflow():
    # Node 1: Task Complexity Assessment
    complexity = assess_complexity()
    
    # Node 2: Vector Database Lookup
    similar_mappings = vector_db_lookup()
    
    # Node 3: LLM Selection
    llm_choice = select_llm(complexity, similar_mappings)
    
    # Node 4: Prompt Generation
    prompt = generate_prompt(llm_choice)
    
    # Node 5: LLM Execution
    result = execute_llm(llm_choice, prompt)
    
    # Node 6: Result Validation
    validated_result = validate_result(result)
    
    # Node 7: Fallback Logic
    final_result = handle_fallback(validated_result)
    
    # Node 8: Cache Results
    cache_results(final_result)
    
    return final_result
```

### Step 3: Intelligent LLM Selection

**Decision Matrix**:
```
Task Complexity | Local LLM (7B) | Remote LLM (Groq) | Fallback
----------------|----------------|-------------------|----------
Simple Mapping  | ✅ Primary     | ❌ Not needed     | None
Custom Logic    | ⚠️ Try first   | ✅ If fails       | Groq
Complex Nested  | ❌ Skip        | ✅ Primary        | None
Validation      | ⚠️ Try first   | ✅ If fails       | Groq
```

### Step 4: Vector Database Integration

```python
# Similarity-based caching
def vector_db_lookup(mapper_structure):
    # Generate embedding for current mapper
    embedding = generate_embedding(mapper_structure)
    
    # Search for similar mappings
    similar_results = vector_db.similarity_search(
        embedding, 
        k=5, 
        threshold=0.85
    )
    
    if similar_results:
        return {
            "cached_result": similar_results[0],
            "similarity_score": similar_results[0].score,
            "cost_saved": calculate_cost_savings()
        }
    
    return None
```

## 4. Implementation Steps

### Phase 1: Enhanced Java Service (Week 1-2)

1. **Upgrade JavaParser Integration**
   - Replace reflection-based analysis with JavaParser
   - Implement comprehensive @Mapping annotation extraction
   - Add custom mapping detection (expression, qualifiedByName)
   - Create @AfterMapping method analyzer

2. **Structured Output Generation**
   - Design standardized JSON output format
   - Include metadata for Python service
   - Add validation and error handling

3. **API Endpoint Enhancement**
   - Create new endpoint for structured data extraction
   - Add health checks and monitoring
   - Implement request/response logging

### Phase 2: Python LLM Orchestrator (Week 3-4)

1. **LangGraph Setup**
   - Install and configure LangGraph
   - Design workflow nodes and edges
   - Implement decision logic for LLM selection

2. **Model Integration**
   - Integrate local Ollama (7B models)
   - Configure Groq API with fallback logic
   - Implement prompt optimization for each model

3. **Workflow Implementation**
   - Create task complexity assessment
   - Implement intelligent routing
   - Add result validation and fallback logic

### Phase 3: Vector Database Integration (Week 5-6)

1. **Database Setup**
   - Choose between Chroma, Pinecone, or Weaviate
   - Set up embedding generation pipeline
   - Configure similarity search parameters

2. **Caching Logic**
   - Implement similarity-based retrieval
   - Add cost tracking and metrics
   - Create cache invalidation strategies

3. **Performance Optimization**
   - Add batch processing capabilities
   - Implement connection pooling
   - Optimize embedding generation

### Phase 4: Integration and Testing (Week 7-8)

1. **Service Integration**
   - Connect Java and Python services
   - Implement error handling and retries
   - Add monitoring and logging

2. **Testing and Validation**
   - Unit tests for each component
   - Integration tests for workflows
   - Performance testing and optimization

3. **Documentation and Deployment**
   - Create deployment guides
   - Document API specifications
   - Prepare monitoring dashboards

## 5. Cost Optimization Strategy

### Current Costs (Baseline)
- **Groq API**: ~$0.05 per request
- **1000 requests/day**: $50/day, $1,500/month

### Projected Costs (After Implementation)
- **Local LLM**: ~$0.001 per request (electricity)
- **Groq API**: ~$0.05 per request (fallback only)
- **Estimated 80% local usage**: $0.40/day, $12/month

### Cost Savings
- **Monthly savings**: ~$1,488 (99.2% reduction)
- **Annual savings**: ~$17,856

## 6. Technical Specifications

### Java Service Requirements
- **Java 21+**
- **Spring Boot 3.5+**
- **JavaParser 3.25+**
- **MapStruct 1.5+**

### Python Service Requirements
- **Python 3.11+**
- **LangGraph 0.2+**
- **LangChain 0.3+**
- **Ollama (local)**
- **Chroma/Pinecone**

### Infrastructure Requirements
- **Memory**: 16GB+ (for local LLM)
- **Storage**: 100GB+ (for vector database)
- **CPU**: 8+ cores (for parallel processing)
- **Network**: Stable internet (for fallback APIs)

## 7. Monitoring and Observability

### Key Metrics
- **API Response Times**: Track performance across different LLMs
- **Cost per Request**: Monitor cost optimization effectiveness
- **Accuracy Rates**: Measure output quality and consistency
- **Cache Hit Rates**: Track vector database effectiveness
- **Fallback Frequency**: Monitor when remote LLMs are needed

### Alerting
- **High Cost Alerts**: When monthly costs exceed thresholds
- **Performance Degradation**: When response times increase
- **Service Availability**: When services become unavailable
- **Accuracy Drops**: When output quality decreases

## 8. Future Enhancements

### Short Term (3-6 months)
- **Multi-language Support**: Extend beyond Java MapStruct
- **Advanced Caching**: Implement more sophisticated caching strategies
- **Batch Processing**: Handle multiple mappers simultaneously

### Long Term (6-12 months)
- **Machine Learning**: Train custom models for specific domains
- **Auto-scaling**: Dynamic resource allocation based on demand
- **Advanced Analytics**: Deep insights into mapping patterns

## 9. Risk Mitigation

### Technical Risks
- **Local LLM Reliability**: Implement robust fallback mechanisms
- **Vector Database Performance**: Monitor and optimize as needed
- **Service Integration**: Comprehensive testing and error handling

### Business Risks
- **Cost Overruns**: Set strict budgets and monitoring
- **Performance Issues**: Regular performance testing and optimization
- **Security Concerns**: Implement proper authentication and authorization

## 10. Conclusion

This hybrid architecture provides a robust, cost-effective solution for MapStruct mapper analysis while maintaining high accuracy and reliability. By leveraging Java's strengths for static analysis and Python's ecosystem for LLM orchestration, we can achieve significant cost savings while improving the overall system performance and reliability.

The phased implementation approach ensures manageable development cycles while delivering incremental value. The integration of vector databases and intelligent LLM selection creates a scalable foundation for future enhancements and broader adoption across different programming languages and frameworks. 