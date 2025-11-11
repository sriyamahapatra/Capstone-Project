# ============================================================ 
# 🤖 Forest Project - RAG API (Retrieval-Augmented Generation)
# ============================================================ 
# This FastAPI microservice receives a natural language query,
# retrieves relevant context from MongoDB (vector similarity search),
# and generates a natural-language answer using Vertex AI Gemini.
# 
# Workflow:
#   1. Embed user query → Vertex AI (TextEmbedding-004)
#   2. Perform vector similarity search in MongoDB
#   3. Construct context → Pass to Gemini model
#   4. Generate context-aware response
# ============================================================ 

import os
import google.auth
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from pymongo import MongoClient
from google.cloud import aiplatform
from vertexai.language_models import TextEmbeddingModel
from vertexai.generative_models import GenerativeModel
from dotenv import load_dotenv  # <-- ADDED: Import load_dotenv

# --- LOAD ENVIRONMENT VARIABLES ---
# This line loads the MONGO_URI, GCP_PROJECT_ID, and GOOGLE_APPLICATION_CREDENTIALS
# path from your local .env file.
load_dotenv() 


# ------------------------------------------------------------
# 🔧 CONFIGURATION (Loaded from environment variables)
# ------------------------------------------------------------

# MongoDB connection URI (⚠️ Keep only in .env)
MONGO_URI = os.getenv('MONGO_URI')
DB_NAME = os.getenv('DB_NAME', 'forest')
COLLECTION_NAME = os.getenv('VECTOR_COLLECTION_NAME', 'posts_and_comments_vectors')

# Name of the MongoDB Atlas vector index (must exist)
INDEX_NAME = os.getenv('VECTOR_INDEX_NAME', 'vector_index')

# Google Cloud Vertex AI config
GCP_PROJECT_ID = os.getenv('GCP_PROJECT_ID')
GCP_LOCATION = os.getenv('GCP_LOCATION', 'us-central1')

# ------------------------------------------------------------
# 🚀 INITIALIZE APPLICATION AND CLIENTS
# ------------------------------------------------------------

app = FastAPI(title='Forest RAG API', description='Retrieval-Augmented Generation microservice', version='1.0.0')

# Initialize MongoDB client and database
client = MongoClient(MONGO_URI)
db = client[DB_NAME]
collection = db[COLLECTION_NAME]

# Initialize Google Cloud credentials and Vertex AI API
credentials, _ = google.auth.default()

# --- CRITICAL FIX: Ensure the correct Project ID is used ---
if GCP_PROJECT_ID:
    # This explicitly tells the Google SDK the project ID loaded from the .env file,
    # preventing it from defaulting to the wrong project ID.
    os.environ['CLOUDSDK_CORE_PROJECT'] = GCP_PROJECT_ID
    print(f'Using Google Cloud Project ID: {GCP_PROJECT_ID}')
# ------------------------------------------------------------

# Initialize Vertex AI using the explicitly set project ID
aiplatform.init(project=GCP_PROJECT_ID, location=GCP_LOCATION, credentials=credentials)

# Load pre-trained models from Vertex AI
embedding_model = TextEmbeddingModel.from_pretrained('text-embedding-004')
generation_model = GenerativeModel('gemini-2.5-flash')

print('✅ Forest RAG API Service initialized successfully.')


# ------------------------------------------------------------
# 🧩 DATA MODELS (For request/response validation)
# ------------------------------------------------------------

class QueryRequest(BaseModel):
    """Incoming user query payload"""
    question: str
    user_id: str | None = None  # Optional personalization support


class SourceDocument(BaseModel):
    """Individual document retrieved from MongoDB"""
    content: str
    score: float
    source_id: str | None = None  # Original MongoDB ID


class QueryResponse(BaseModel):
    """Structured API response"""
    answer: str
    source_documents: list[SourceDocument]


# ------------------------------------------------------------
# 💬 API ENDPOINT - Query Endpoint
# ------------------------------------------------------------

@app.post('/query', response_model=QueryResponse)
async def ask_question(request: QueryRequest):
    """
    Handle user queries via RAG pipeline:
      - Generate embeddings for the question
      - Retrieve top relevant documents
      - Combine retrieved context and generate an answer via Gemini
    """

    try:
        # Step 1️⃣: Generate embedding for user query
        query_embedding = embedding_model.get_embeddings([request.question])[0].values

        # Step 2️⃣: Perform vector search in MongoDB (requires Atlas Vector Search index)
        documents = list(collection.aggregate([
            {
                '$vectorSearch': {
                    'index': INDEX_NAME,
                    'path': 'embedding',
                    'queryVector': query_embedding,
                    'numCandidates': 100,
                    'limit': 3  # Retrieve top 3 most relevant posts/comments
                }
            },
            {
                '$project': {
                    '_id': 0,
                    'text_content': 1,
                    'original_id': 1,
                    'score': {'$meta': 'vectorSearchScore'}
                }
            }
        ]))

        # If no relevant documents found, return a fallback response
        if not documents:
            return QueryResponse(
                answer='I couldn\'t find any relevant information in the existing posts and comments.',
                source_documents=[]
            )

        # Step 3️⃣: Build contextual prompt for Gemini
        context = ''
        source_docs_response = []

        for doc in documents:
            context += doc['text_content'] + '\n---\n'
            source_docs_response.append(SourceDocument(
                content=doc['text_content'],
                score=doc['score'],
                source_id=str(doc.get('original_id'))
            ))

        # Construct the final Gemini prompt
        prompt = f"""
        You are a helpful assistant for a community platform called Forest.
        Your task is to answer the following question using ONLY the context
        provided from user posts and comments.

        If the context does not contain enough information,
        respond with: "I'm sorry, but I couldn't find that information."

        Context from user posts/comments:
       {context}

        Question:
        {request.question}

        Answer:
        """

        # Step 4️⃣: Generate the AI response via Gemini
        response = generation_model.generate_content(prompt)

        # Step 5️⃣: Return structured API response
        return QueryResponse(
            answer=response.text,
            source_documents=source_docs_response
        )

    except Exception as e:
        print(f'❌ Error during query processing: {e}')
        raise HTTPException(status_code=500, detail='Internal Server Error')


# ------------------------------------------------------------
# 🏁 RUNNING THE SERVICE
# ------------------------------------------------------------
# You can run this service using:
#   uvicorn rag_api:app --host 0.0.0.0 --port 8000 --reload
# ------------------------------------------------------------
