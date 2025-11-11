# ============================================================ 
# 🌲 Forest Project - Ingestion Worker 
# ============================================================ 
# This background worker listens to MongoDB collections 
# ("posts" and "comments") for new documents. 
# When a new post or comment is inserted, it: 
#   1. Extracts and cleans the text content 
#   2. Generates a semantic embedding using Google Vertex AI 
#   3. Stores the resulting vector in a dedicated MongoDB collection 
# ============================================================ 

import os
import time
from dotenv import load_dotenv
from pymongo import MongoClient
from google.cloud import aiplatform
import google.auth
from vertexai.language_models import TextEmbeddingModel
from bs4 import BeautifulSoup  # Used to clean HTML tags from post descriptions


# --- LOAD ENVIRONMENT VARIABLES ---
# This line will load the variables from the local .env file
load_dotenv() 


# ------------------------------------------------------------ 
# 🔧 CONFIGURATION (All loaded from environment variables) 
# ------------------------------------------------------------ 

# MongoDB connection string (keep only in .env — never commit real URIs)
MONGO_URI = os.getenv("MONGO_URI")
DB_NAME = os.getenv("DB_NAME", "forest")

# Collections to watch for new data insertions
SOURCE_COLLECTIONS = os.getenv("SOURCE_COLLECTIONS", "posts,comments").split(",")

# Collection to store generated embeddings
VECTOR_COLLECTION_NAME = os.getenv(
    "VECTOR_COLLECTION_NAME", "posts_and_comments_vectors"
)

# Google Cloud Vertex AI project and location
GCP_PROJECT_ID = os.getenv("GCP_PROJECT_ID")
GCP_LOCATION = os.getenv("GCP_LOCATION", "us-central1")

# Worker tuning parameters
WORKER_POLL_INTERVAL = float(os.getenv("WORKER_POLL_INTERVAL", "1"))
LOG_LEVEL = os.getenv("LOG_LEVEL", "INFO")


# ------------------------------------------------------------ 
# 🚀 INITIALIZATION 
# ------------------------------------------------------------ 

# Initialize MongoDB client and select database/collection
client = MongoClient(MONGO_URI)
db = client[DB_NAME]
vector_collection = db[VECTOR_COLLECTION_NAME]

# Authenticate and initialize Vertex AI API
credentials, _ = google.auth.default()
aiplatform.init(project=GCP_PROJECT_ID, location=GCP_LOCATION, credentials=credentials)

# Load Vertex AI’s embedding model
embedding_model = TextEmbeddingModel.from_pretrained("text-embedding-004")


# ------------------------------------------------------------ 
# 🧠 MAIN FUNCTION - Stream Processor 
# ------------------------------------------------------------ 
def process_change_stream():
    """
    Continuously listens for new documents in MongoDB.
    For each new post or comment:
      - Cleans text content
      - Generates embeddings
      - Stores vectors in MongoDB
    """

    # Trigger only on new insertions
    pipeline = [{"$match": {"operationType": "insert"}}]

    # Create change streams for each source collection
    change_streams = [db[coll].watch(pipeline) for coll in SOURCE_COLLECTIONS]

    print("--- 🟢 Ingestion Worker Started ---")
    print(f"Listening for new documents in {SOURCE_COLLECTIONS} collections...")

    # Main listening loop
    while True:
        for i, stream in enumerate(change_streams):
            change = stream.try_next()
            if change is None:
                continue  # No new data yet; check again next cycle

            collection_name = SOURCE_COLLECTIONS[i]
            full_document = change["fullDocument"]

            print(f"📥 New document detected in '{collection_name}' collection.")

            text_to_embed = ""

            # ------------------------------------------------------------ 
            # 📝 Extract and clean text content by schema 
            # ------------------------------------------------------------ 
            if collection_name == "posts":
                post_name = full_document.get("postName", "")
                description_html = full_document.get("description", "")

                # Strip HTML tags using BeautifulSoup
                soup = BeautifulSoup(description_html, "html.parser")
                clean_description = soup.get_text(separator=" ", strip=True)

                # Combine title and description for better embeddings
                text_to_embed = f"Title: {post_name}. Content: {clean_description}"

            elif collection_name == "comments":
                comment_text = full_document.get("text", "")
                text_to_embed = f"Comment: {comment_text}"

            else:
                # Skip any unrecognized collection
                print(f"⚠️  Unknown collection '{collection_name}', skipping...")
                continue

            # Skip empty text
            if not text_to_embed.strip():
                print(
                    f"⚠️  Empty content detected for ID: {full_document['_id']}, skipping..."
                )
                continue

            # ------------------------------------------------------------ 
            # 🧩 Generate Embedding Vector via Vertex AI 
            # ------------------------------------------------------------ 
            try:
                # Create vector embedding
                embedding = embedding_model.get_embeddings([text_to_embed])[0].values

                # Prepare vector document to store
                vector_doc = {
                    "original_id": full_document[
                        "_id"
                    ],  # Reference to original document
                    "source_collection": collection_name,  # e.g., 'posts' or 'comments'
                    "text_content": text_to_embed,  # Cleaned text
                    "embedding": embedding,  # Vector representation
                }

                # Insert the vector into MongoDB
                vector_collection.insert_one(vector_doc)
                print(f"✅ Stored embedding for document ID: {full_document['_id']}")

            except Exception as e:
                print(f"❌ Error embedding document {full_document['_id']}: {e}")

        # Pause briefly to avoid high CPU usage
        time.sleep(WORKER_POLL_INTERVAL)


# ------------------------------------------------------------ 
# 🏁 SCRIPT ENTRY POINT 
# ------------------------------------------------------------ 
if __name__ == "__main__":
    try:
        process_change_stream()
    except KeyboardInterrupt:
        print("\n🛑 Ingestion worker stopped manually.")
    except Exception as e:
        print(f"❌ Fatal error: {e}")
