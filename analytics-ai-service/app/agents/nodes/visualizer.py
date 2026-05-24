import json
from langchain_core.prompts import ChatPromptTemplate
from langchain_core.output_parsers import JsonOutputParser
from langchain_groq import ChatGroq
from app.agents.graph import AgentState
from app.core.config import settings

llm = ChatGroq(
    temperature=0,
    groq_api_key=settings.GROQ_API_KEY,
    model_name="openai/gpt-oss-20b"
)

# JsonOutputParser to force the LLM to return valid JSON
json_parser = JsonOutputParser()

viz_prompt = ChatPromptTemplate.from_messages([
    ("system", """You are an expert Data Visualization Architect. Your job is to take raw data and output a JSON configuration for a modern, interactive chart (like Chart.js).
    
    CRITICAL RULES:
    1. Output strictly valid JSON.
    2. Do NOT wrap the output in markdown blocks (no ```json).
    3. Do NOT include <think> tags or explanations.
    4. Choose the best chart type (bar, pie, line) based on the user query.
    
    The JSON structure MUST follow this exact schema:
    {{
        "chart_type": "bar | pie | line",
        "title": "A descriptive title based on the query",
        "labels": ["Label 1", "Label 2"],
        "data": [10, 20],
        "x_axis_label": "Optional label for X axis",
        "y_axis_label": "Optional label for Y axis",
        "summary": "A short, one-sentence human-readable summary of the insight."
    }}
    """),
    ("human", "User Query: {query}\n\nRaw Data:\n{data}")
])

viz_chain = viz_prompt | llm | json_parser

def generate_visualization_node(state: AgentState) -> AgentState:
    """
    LangGraph Node: Takes the raw data and generates a charting JSON config.
    """
    raw_data = state.get("raw_data")
    query = state.get("user_query")
    
    if not raw_data:
        return {"error": "No raw data available to visualize."}
        
    print("Agent: Designing Visualization Configuration...")
    
    try:
        # We pass the data as a string to the LLM
        viz_config = viz_chain.invoke({
            "query": query,
            "data": json.dumps(raw_data)
        })
        
        print("** SUCCESS **: Visualization JSON generated successfully.")
        
        # We store it as a stringified JSON object in the state
        return {"visualization_json": json.dumps(viz_config, indent=2), "error": None}
        
    except Exception as e:
        print(f"Error generating visualization: {str(e)}")
        return {"error": f"Failed to generate visualization: {str(e)}"}



if __name__ == "__main__":
    test_state: AgentState = {
        "user_query": "Show me the total number of PATIENT CREATED events for patients aged 30-44, grouped by their email domain.",
        "database_schema": "",
        "generated_sql": "",
        "raw_data": [
            {'email_domain': 'apollohospitals.com', 'total_registration_events': 3}, 
            {'email_domain': 'github.com', 'total_registration_events': 2}, 
            {'email_domain': 'gmail.com', 'total_registration_events': 1}, 
            {'email_domain': 'outlook.com', 'total_registration_events': 1}
        ],
        "visualization_json": None,
        "error": None
    }
    
    result_state = generate_visualization_node(test_state)
    
    print("\n--- Generated Chart Configuration ---")
    print(result_state.get("visualization_json"))