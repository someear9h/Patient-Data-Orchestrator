import os
from langchain_huggingface import HuggingFaceEmbeddings
from langchain_chroma import Chroma
from langchain_core.documents import Document

def seed_semantic_dictionary():
    print("Loading embedding model (all-MiniLM-L6-v2)...")
    embeddings = HuggingFaceEmbeddings(model_name="sentence-transformers/all-MiniLM-L6-v2")
    
    # Chroma will save its database files on disk
    persist_directory = "./chroma_db"

    semantic_mappings = [
        # Event Types 
        Document(page_content="signup, sign up, user registration, create account, new patient, onboarding, joined", 
                 metadata={"db_value": "PATIENT CREATED", "category": "event_type"}),
        Document(page_content="delete account, remove patient, account cancellation, user departure, purged", 
                 metadata={"db_value": "PATIENT DELETED", "category": "event_type"}),
        
        # Age Groups
        Document(page_content="middle aged, adults, thirties, forties, 30 to 45", 
                 metadata={"db_value": "30-44", "category": "age_group"}),
        Document(page_content="kids, children, teenagers, minors, youth, toddlers, under 18", 
                 metadata={"db_value": "0-17", "category": "age_group"}),
        Document(page_content="young adults, college students, twenties, 18 to 30", 
                 metadata={"db_value": "18-29", "category": "age_group"}),
        Document(page_content="older adults, seniors, retired, elderly, 60 plus", 
                 metadata={"db_value": "60+", "category": "age_group"})
    ]

    print("Embedding data dictionary and saving to disk...")
    vector_store = Chroma.from_documents(
        documents=semantic_mappings,
        embedding=embeddings,
        persist_directory=persist_directory
    )

    print(f"Semantic Vector Store initialized successfully! {vector_store._collection.count()} categories mapped.")

if __name__ == "__main__":
    seed_semantic_dictionary()