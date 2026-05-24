from typing import TypedDict, Optional, List, Dict, Any

class AgentState(TypedDict):
    """
    This state object is passed between our LangGraph nodes.
    Each node reads from it and updates specific fields.
    """
    user_query: str
    database_schema: str
    generated_sql: Optional[str]
    raw_data: Optional[List[Dict[str, Any]]]
    visualization_json: Optional[str]
    error: Optional[str]