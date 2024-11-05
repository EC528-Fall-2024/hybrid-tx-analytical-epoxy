#!/bin/bash

# Step 1: Identify the PostgreSQL container ID
echo "Identifying PostgreSQL Docker container ID..."
container_id=$(docker ps --filter "ancestor=postgres:14.5-bullseye" --format "{{.ID}}")

if [ -z "$container_id" ]; then
  echo "Error: No PostgreSQL container is running."
  exit 1
fi

echo "PostgreSQL container ID: $container_id"

# Step 2: Upload the CSV file to the Docker container
csv_file="$HOME/Downloads/htap/campaign_product_subcategory.csv"  # Change this to your actual CSV file path
echo "Uploading CSV file to the Docker container..."
docker cp "$csv_file" "$container_id:/campaign_product_subcategory.csv"

# Step 3: Connect to PostgreSQL and execute SQL commands
echo "Connecting to PostgreSQL and setting up the database and table..."

docker exec -i "$container_id" psql -U postgres << EOF
-- Create a new database for the dataset
CREATE DATABASE campaign_product_subcategory;

-- Switch to the new database
\c campaign_product_subcategory

-- Create a table for the CSV data
CREATE TABLE campaign_product_subcategory (
    campaign_product_subcategory_id INT PRIMARY KEY,
    campaign_id INT,
    subcategory_id INT,
    discount FLOAT
);

-- Import the CSV data into the table
COPY campaign_product_subcategory(campaign_product_subcategory_id, campaign_id, subcategory_id, discount)
FROM '/campaign_product_subcategory.csv'
DELIMITER ','
CSV HEADER;
EOF

echo "Data import completed successfully."
