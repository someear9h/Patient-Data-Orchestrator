from fastapi import APIRouter, HTTPException
from pydantic import BaseModel
from app.agents.graph import create_workflow
from app.database.schema_tools import get_database_schema

router = APIRouter()

workflow_app = create_workflow()

class AnalyzeRequest(BaseModel):
    query: str

@router.post("/analyze")
async def analyze_data(request: AnalyzeRequest):
    """
    Takes a natural language query, runs it through the LangGraph AI agents,
    and returns a visualization configuration.
    """
    print(f"\nReceived API Request: {request.query}")
    
    initial_state = {
        "user_query": request.query,
        "database_schema": get_database_schema(),
        "generated_sql": None,
        "raw_data": None,
        "visualization_json": None,
        "error": None
    }
    
    try:
        final_state = workflow_app.invoke(initial_state)
        
        if final_state.get("error"):
            raise HTTPException(status_code=400, detail=final_state["error"])
            
        viz_json = final_state.get("visualization_json")
        if not viz_json:
            raise HTTPException(status_code=500, detail="Failed to generate visualization config.")
            
        import json
        return json.loads(viz_json)
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))