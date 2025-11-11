@echo off
echo Starting all Forest Backend and Frontend Services...
echo.

REM --- Start Spring Boot Backend (Main App) ---
REM Runs the main application from the root directory using Maven wrapper.
echo [1/4] Starting Spring Boot Backend (Maven)...
start "Spring Backend" cmd /k ".\mvnw spring-boot:run"

REM --- Start Python RAG API (FastAPI) ---
REM Runs the dedicated Python RAG API service (uvicorn) for RAG lookups.
echo [2/4] Starting Python RAG API (FastAPI) on port 8000...
start "Python RAG API" cmd /k "cd RAGpython && uvicorn rag_api:app --host 0.0.0.0 --port 8000"

REM --- Start Ingestion Worker (Python) ---
REM Runs the MongoDB worker in the RAGpython directory in a new window.
echo [3/4] Starting MongoDB Ingestion Worker...
start "Ingestion Worker" cmd /k "cd RAGpython && python ingestion_worker.py"

REM --- Start Frontend (NPM) ---
REM Runs 'npm start' in the Frontend directory in a new window.
echo [4/4] Starting Frontend Application...
start "Frontend App" cmd /k "cd Frontend && npm start"

echo.
echo All FOUR services launched successfully in separate windows.
echo Close the new windows to stop each service.