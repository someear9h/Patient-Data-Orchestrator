from sqlalchemy import create_engine, text
from sqlalchemy.exc import SQLAlchemyError
from app.core.config import settings

# create the SQLAlchemy engine
# pool_pre_ping to check connection health before executing queries
engine = create_engine(
    settings.DATABASE_URL, 
    pool_pre_ping=True,
    echo=False # make it true -> to see the exact SQL generated in terminal
)

def test_db_connection():
    """Tests the connection to the PostgreSQL database."""
    try:
        with engine.connect() as connection:
            result = connection.execute(text("SELECT current_user;"))
            current_user = result.scalar()
            print(f"INFO: Successfully connected to database as: {current_user}")
            return True
    except SQLAlchemyError as e:
        print(f"ERROR: Database connection failed: {e}")
        return False