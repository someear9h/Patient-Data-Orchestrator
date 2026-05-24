import streamlit as st
import requests
import pandas as pd
import plotly.express as px

# Point this to your FastAPI backend
API_URL = "http://localhost:8000/api/analyze"

st.set_page_config(page_title="AI Analytics Engine", page_icon="📈", layout="wide")

st.title("🤖 Autonomous Patient Analytics")
st.markdown("Ask natural language questions to instantly query and visualize your database.")

# Helper text for the MVP constraints
st.info("💡 **MVP Test Query:** 'Show me the total number of PATIENT CREATED events for patients aged 30-44, grouped by their email domain.'")

# The user input bar
query = st.text_input("Enter your analytics query:", placeholder="e.g., Show me registration events...")

if st.button("Generate Insight", type="primary") and query:
    with st.spinner("AI Agents are generating SQL, executing queries, and building charts..."):
        try:
            # 1. Send the POST request to our FastAPI backend
            response = requests.post(API_URL, json={"query": query})
            
            if response.status_code == 200:
                data = response.json()
                
                st.success("Pipeline Execution Complete!")
                
                # 2. Render the AI's Text Summary
                st.subheader(data.get("title", "Analytics Result"))
                st.write(f"**Insight:** {data.get('summary', '')}")
                
                st.divider()
                
                # 3. Transform the JSON data into a Pandas DataFrame for Plotly
                x_col = data.get("x_axis_label", "Category")
                y_col = data.get("y_axis_label", "Value")
                
                df = pd.DataFrame({
                    x_col: data.get("labels", []),
                    y_col: data.get("data", [])
                })
                
                chart_type = data.get("chart_type", "bar").lower()
                
                # 4. Render the interactive chart natively
                if chart_type == "bar":
                    fig = px.bar(df, x=x_col, y=y_col, color=x_col)
                    st.plotly_chart(fig, use_container_width=True)
                elif chart_type == "pie":
                    fig = px.pie(df, names=x_col, values=y_col)
                    st.plotly_chart(fig, use_container_width=True)
                elif chart_type == "line":
                    fig = px.line(df, x=x_col, y=y_col, markers=True)
                    st.plotly_chart(fig, use_container_width=True)
                else:
                    st.warning("Chart type not supported yet.")
                    st.dataframe(df)
                    
            else:
                st.error(f"Backend Error [{response.status_code}]: {response.text}")
                
        except requests.exceptions.ConnectionError:
            st.error("Failed to connect to the backend. Is FastAPI running on port 8000?")