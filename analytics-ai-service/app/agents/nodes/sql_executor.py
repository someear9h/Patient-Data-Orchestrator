from sqlalchemy import text
from sqlalchemy.exc import SQLAlchemyError
import decimal
from app.agents.graph import AgentState
from app.database.connection import engine

def execute_sql_node(state: AgentState) -> AgentState:
    """
    LangGraph Node: Takes the generated SQL from the state, executes it,
    and stores the raw results back into the state.
    """
    query = state.get("generated_sql")
    
    if not query:
        return {"error": "No SQL query found in state to execute."}
        
    print(f"Executing Query against database...")
    
    try:
        with engine.connect() as connection:
            result = connection.execute(text(query))
            
            rows = [dict(row._mapping) for row in result]

            for row in rows:
                for key, value in row.items():
                    if isinstance(value, decimal.Decimal):
                        row[key] = float(value)
            
            print(f"INFO: Successfully fetched {len(rows)} rows.")
            
            return {
                "raw_data": rows,
                "error": None
            }
            
    except SQLAlchemyError as e:
        print(f"ERROR: Database execution error: {str(e)}")

        return {"error": f"Database error: {str(e)}"}


# quick test
if __name__ == "__main__":
    
    test_state: AgentState = {
        "user_query": "Show me the total number of PATIENT CREATED events for patients aged 30-44, grouped by their email domain.",
        "database_schema": "", 
        "generated_sql": "SELECT email_domain, SUM(event_count) AS total_registration_events FROM patient_analytics WHERE age_group = '30-44' AND event_type = 'PATIENT CREATED' GROUP BY email_domain;",
        "raw_data": None,
        "visualization_json": None,
        "error": None
    }
    
    result_state = execute_sql_node(test_state)
    
    print("\n--- Extracted Data ---")
    print(result_state.get("raw_data"))