import time
import psycopg2
import clickhouse_driver

def measure_query_time(database_type, query_func):
    start_time = time.time()
    result = query_func()
    end_time = time.time()
    execution_time = end_time - start_time
    print(f"{database_type} Query Time: {execution_time:.4f} seconds")
    return result

def run_postgres_query():
    conn = psycopg2.connect(
        dbname="social_media",
        user="postgres",
        password="dbos",
        host="localhost",
        port="5432"
    )
    cursor = conn.cursor()
    cursor.execute("""
                    SELECT 
                        u.username,
                        COUNT(DISTINCT p.post_id) as post_count,
                        COUNT(DISTINCT l.like_id) as like_count
                    FROM users u
                    LEFT JOIN posts p ON u.user_id = p.user_id
                    LEFT JOIN likes l ON p.post_id = l.post_id
                    GROUP BY u.username
                    ORDER BY post_count DESC, like_count DESC;
                   """)
    result = cursor.fetchall()
    cursor.close()
    conn.close()
    return result

def run_clickhouse_query():
    client = clickhouse_driver.Client(
        host='localhost',
        port=9000,
        user='default',
        password=''
    )
    result = client.execute("""
                            SELECT 
                                u.username,
                                COUNT(DISTINCT p.post_id) as post_count,
                                COUNT(DISTINCT l.like_id) as like_count
                            FROM social_media.users u
                            LEFT JOIN social_media.posts p ON u.user_id = p.user_id
                            LEFT JOIN social_media.likes l ON p.post_id = l.post_id
                            GROUP BY u.username
                            ORDER BY post_count DESC, like_count DESC;
    """)
    return result

def compare_analytics_performance():
    print("Running analytics comparison...")
    
    # Run OLTP Query
    print("\nExecuting PostgreSQL (OLTP) analytics...")
    oltp_result = measure_query_time("PostgreSQL", run_postgres_query)
    
    # Run OLAP Query
    print("\nExecuting ClickHouse (OLAP) analytics...")
    olap_result = measure_query_time("ClickHouse", run_clickhouse_query)
    
    # Print first few results for verification
    print("\nFirst 5 results from each database:")
    print("PostgreSQL:", oltp_result[:5])
    print("ClickHouse:", olap_result[:5])

if __name__ == "__main__":
    compare_analytics_performance()