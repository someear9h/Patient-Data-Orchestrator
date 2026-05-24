from typing import TypedDict, Optional, List, Dict, Any
from langgraph.graph import StateGraph, START, END
from app.database.schema_tools import get_database_schema

class AgentState(TypedDict):
    user_query: str
    database_schema: str
    generated_sql: Optional[str]
    raw_data: Optional[List[Dict[str, Any]]]
    visualization_json: Optional[str]
    error: Optional[str]

def create_workflow():
    # Inline imports to avoid circular dependency crashes
    from app.agents.nodes.sql_generator import generate_sql_node
    from app.agents.nodes.sql_executor import execute_sql_node
    from app.agents.nodes.visualizer import generate_visualization_node
    
    workflow = StateGraph(AgentState)
    
    workflow.add_node("sql_generator", generate_sql_node)
    workflow.add_node("sql_executor", execute_sql_node)
    workflow.add_node("visualizer", generate_visualization_node)
    

    def route_execution(state: AgentState):
        if state.get("error"):
            print(f"**FAILURE** Halting graph execution due to error: {state.get('error')}")
            return END
        return "visualizer"
        
    workflow.add_edge(START, "sql_generator")
    workflow.add_edge("sql_generator", "sql_executor")
    workflow.add_conditional_edges(
        "sql_executor", 
        route_execution
    )
    workflow.add_edge("visualizer", END)
    
    return workflow.compile()



if __name__ == "__main__":
    print("INFO: Initializing full AI Analytics pipeline...")
    app = create_workflow()
    
    initial_state = {
        "user_query": "Show me the total number of PATIENT CREATED events for patients aged 30-44, grouped by their email domain.",
        "database_schema": get_database_schema(),
        "generated_sql": None,
        "raw_data": None,
        "visualization_json": None,
        "error": None
    }
    
    print("\n" + "="*50)
    print(f"USER QUERY: {initial_state['user_query']}")
    print("="*50 + "\n")
    
    # Execute the entire graph
    final_state = app.invoke(initial_state)
    
    print("\n" + "="*50)
    print("FINAL SYSTEM OUTPUT:")
    print("="*50)
    if final_state.get("visualization_json"):
        print(final_state["visualization_json"])
    else:
        print("Pipeline failed to generate output.")