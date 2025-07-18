#!/usr/bin/env python3
import psycopg2
import pandas as pd
from tabulate import tabulate

# Database connection
try:
    conn = psycopg2.connect(
        host="localhost",
        database="hms",
        user="postgres",
        password="d433847adf9344c4a5e31f6fdb51be0a"
    )
    
    cursor = conn.cursor()
    
    # Get table structure first
    cursor.execute("""
        SELECT column_name, data_type 
        FROM information_schema.columns 
        WHERE table_name = 'account' 
        ORDER BY ordinal_position;
    """)
    
    columns = cursor.fetchall()
    print("Account table structure:")
    print(tabulate(columns, headers=["Column", "Type"], tablefmt="grid"))
    print()
    
    # Get all users
    cursor.execute("SELECT * FROM account LIMIT 10;")
    users = cursor.fetchall()
    
    # Get column names
    col_names = [desc[0] for desc in cursor.description]
    
    print("Users in the system:")
    print(tabulate(users, headers=col_names, tablefmt="grid"))
    
    # Count total users
    cursor.execute("SELECT COUNT(*) FROM account;")
    total_users = cursor.fetchone()[0]
    print(f"\nTotal users in system: {total_users}")
    
    # Get active users count
    cursor.execute("SELECT COUNT(*) FROM account WHERE active = true;")
    active_users = cursor.fetchone()[0]
    print(f"Active users: {active_users}")
    
    cursor.close()
    conn.close()
    
except Exception as e:
    print(f"Error: {e}")
