from sqlalchemy import inspect
from app.database.connection import engine, test_db_connection

def get_database_schema() -> str:
    """
    Extracts the database schema dynamically and formats it as a string
    to be injected into the LLM's system prompt.
    """
    inspector = inspect(engine)
    schema_text = "Database Schema:\n\n"
    
    table_names = inspector.get_table_names(schema="public")
    
    if not table_names:
        return "No tables found in the 'public' schema."
        
    for table_name in table_names:
        schema_text += f"Table: {table_name}\n"
        columns = inspector.get_columns(table_name, schema="public")
        for col in columns:
            col_name = col['name']
            col_type = str(col['type'])
            schema_text += f"  - {col_name} ({col_type})\n"
        schema_text += "\n"
        
    return schema_text

# --- Quick Test Block ---
if __name__ == "__main__":
    # 1. Test the connection
    print("Testing connection...")
    if test_db_connection():
        # 2. Dump the schema
        print("\nExtracting Schema for the LLM...")
        schema = get_database_schema()
        print("-" * 40)
        print(schema)
        print("-" * 40)