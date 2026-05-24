import os
from pathlib import Path

def create_app_structure():

    base_dir = Path(".") 
    
    files = [
        "app/__init__.py",
        "app/main.py",
        "app/core/__init__.py",
        "app/core/config.py",
        "app/database/__init__.py",
        "app/database/connection.py",
        "app/database/schema_tools.py",
        "app/agents/__init__.py",
        "app/agents/graph.py",
        "app/agents/nodes/__init__.py",
        "app/agents/nodes/sql_generator.py",
        "app/agents/nodes/sql_executor.py",
        "app/agents/nodes/visualizer.py",
        "app/api/__init__.py",
        "app/api/routes.py"
    ]

    print("INFO: Initializing 'app' structure in current directory...\n")

    for file_path in files:
        full_path = base_dir / file_path
        
        full_path.parent.mkdir(parents=True, exist_ok=True)
        
        if not full_path.exists():
            full_path.touch()
            print(f"Created: {full_path}")
        else:
            print(f"Skipped (already exists): {full_path}")

    env_path = base_dir / ".env"
    if not env_path.exists():
        env_content = """# Database Connection (Read-Only AI User)
DATABASE_URL=postgresql://ai_reader:secure_read_only_password@localhost:5002/analyticsdb

# LLM API Keys
GROQ_API_KEY=your_groq_api_key_here
"""
        env_path.write_text(env_content)
        print("Created: .env")

    gitignore_path = base_dir / ".gitignore"
    ignore_entries = "\n# Environment Variables\n.env\n"
    if gitignore_path.exists():
        current_content = gitignore_path.read_text()
        if ".env" not in current_content:
            with open(gitignore_path, "a") as f:
                f.write(ignore_entries)
            print("Updated: .gitignore (added .env)")
    else:
        gitignore_path.write_text(ignore_entries)
        print("Created: .gitignore")

    print("\nINFO: Internal app structure generated successfully!")

if __name__ == "__main__":
    create_app_structure()