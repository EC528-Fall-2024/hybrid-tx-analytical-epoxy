# Use an official Python runtime as the base image
FROM python:3.11-slim

# Set the working directory in the container
WORKDIR /app

# Copy the current directory contents into the container at /app
COPY . /app

# Install any needed dependencies
RUN pip install --no-cache-dir -r requirements.txt

# Expose the port the app runs on
EXPOSE 5001

# Define environment variable for Flask
ENV FLASK_APP=simulate_traffic.py

# Run the application
CMD ["flask", "run", "--host=0.0.0.0", "--port=5001"]