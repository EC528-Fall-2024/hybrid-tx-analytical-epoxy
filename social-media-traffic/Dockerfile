# Use an official Python runtime as the base image
FROM amd64/python:3.11-slim

# Set the working directory in the container
WORKDIR /app

# Copy the current directory contents into the container at /app
COPY . /app

# Install any needed dependencies
RUN pip install --no-cache-dir -r requirements.txt

# Run the application
CMD ["python", "simulate_traffic.py"]

