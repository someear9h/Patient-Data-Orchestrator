import os
import re
from langchain_core.prompts import ChatPromptTemplate
from langchain_core.output_parsers import StrOutputParser
from langchain_groq import ChatGroq
from langchain_huggingface import HuggingFaceEmbeddings
from langchain_chroma import Chroma
from app.agents.graph import AgentState
from app.database.schema_tools import get_database_schema
from app.core.config import settings

embeddings = HuggingFaceEmbeddings(model_name="sentence-transformers/all-MiniLM-L6-v2")
vector_store = Chroma(persist_directory="./chroma_db", embedding_function=embeddings)

# initialise the LLM
llm = ChatGroq(
    temperature=0,
    groq_api_key=settings.GROQ_API_KEY,
    model_name="openai/gpt-oss-20b"
)

sql_prompt = ChatPromptTemplate.from_messages([
    ("system", """You are a PostgreSQL expert. Your only job is to translate the user's natural language request into a valid, optimized SQL query.
    
    CRITICAL RULES:
    1. Return ONLY the raw SQL query. 
    2. Do NOT wrap the query in markdown formatting (e.g., no ```sql).
    3. Do NOT include any explanations, greetings, or trailing text.
    4. Do NOT output your thinking process. NEVER use <think> tags.
    5. Only use the tables and columns provided in the schema below.
    6. Crucial: Consult the "SEMANTIC TRANSLATIONS" context below. If a user asks for a concept listed there, you MUST use the corresponding exact DB_VALUE in your SQL WHERE clause.
    
    {schema}
    
    SEMANTIC TRANSLATIONS FOR EXACT FILTER MATCHES:
    {semantic_context}
    """),
    ("human", "{query}")
])

sql_chain = sql_prompt | llm | StrOutputParser()

def clean_sql(raw_sql: str) -> str:
    """Removes think tags, markdown formatting, and trailing text from the LLM output."""
    cleaned = raw_sql.strip()
    
    cleaned = re.sub(r"<think>.*?</think>", "", cleaned, flags=re.DOTALL)
    
    
    cleaned = cleaned.strip()
    if cleaned.startswith("```sql"):
        cleaned = cleaned[6:]
    elif cleaned.startswith("```"):
        cleaned = cleaned[3:]
    if cleaned.endswith("```"):
        cleaned = cleaned[:-3]
        
    return cleaned.strip()

def generate_sql_node(state: AgentState) -> AgentState:
    """
    LangGraph Node: Takes the current state, generates SQL, and updates the state.
    """
    print("Agent: Generating SQL...")
    query = state["user_query"]
    
    try:
        retrieved_docs = vector_store.similarity_search(query, k=2)

        semantic_context = ""
        for doc in retrieved_docs:
            category = doc.metadata.get("category")
            db_value = doc.metadata.get("db_value")
            semantic_context += f"- If user mentions terms similar to '{doc.page_content}', the column '{category}' must equal '{db_value}' exactly.\n"
        
        print(f"Semantic Layer Context Injected:\n{semantic_context.strip()}")
        print("Agent: Generating SQL Query...")
        
        raw_sql = sql_chain.invoke({
            "schema": state["database_schema"],
            "semantic_context": semantic_context,
            "query": query
        })

    
        
        clean_query = clean_sql(raw_sql)
        print(f"INFO: Generated SQL: {clean_query}")
        
        return {"generated_sql": clean_query, "error": None}
        
    except Exception as e:
        print(f"Error generating SQL: {str(e)}")
        return {"error": f"Failed to generate SQL: {str(e)}"}


# for quick test
if __name__ == "__main__":
    test_state: AgentState = {
        "user_query": "Show me the total number of registration events for patients aged 30-44, grouped by their email domain.",
        "database_schema": get_database_schema(),
        "generated_sql": None,
        "raw_data": None,
        "visualization_json": None,
        "error": None
    }
    
    result_state = generate_sql_node(test_state)