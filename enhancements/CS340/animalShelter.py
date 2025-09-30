from pymongo import MongoClient
from bson.objectid import ObjectId
import os

class AnimalShelter:

    """
    function __init__
    params: username (str), password (str), host (str), port (int)
    Description: Initialize MongoDB connection with flexible configuration
    """
    def __init__(self, username=None, password=None, host='localhost', port=27017):
        self.host = host
        self.port = port
        self.username = username
        self.password = password
        
        try:
            if username and password:
                connection_string = f'mongodb://{username}:{password}@{host}:{port}'
            else:
                connection_string = f'mongodb://{host}:{port}'
            
            self.client = MongoClient(connection_string)
            self.database = self.client['AAC']
            self.collection = self.database['animals']
            
            self.client.server_info()
            print(f"✓ Connected to MongoDB at {host}:{port}")
            
        except Exception as e:
            print(f"✗ Error connecting to MongoDB: {e}")
            raise

    """
    function create
    params: data (dict)
    Description: Insert a new document into the collection
    """
    def create(self, data):
        if data is not None:
            try:
                result = self.collection.insert_one(data)
                return result.inserted_id is not None
            except Exception as e:
                print(f"Error inserting: {e}")
                return False
        else:
            raise Exception("Data cannot be empty")

    """
    function read
    params: query (dict)
    Description: Read documents from collection based on query
    """
    def read(self, query=None):
        if query is None:
            query = {}
        try:
            return list(self.collection.find(query, {"_id": False}))
        except Exception as e:
            print(f"Error reading: {e}")
            return []

    """
    function update
    params: query (dict), update_data (dict)
    Description: Update documents matching query with new data
    """
    def update(self, query, update_data):
        if update_data is not None:
            try:
                result = self.collection.update_many(query, {"$set": update_data})
                return {
                    "matched": result.matched_count,
                    "modified": result.modified_count
                }
            except Exception as e:
                print(f"Error updating: {e}")
                return {}
        return {}

    """
    function delete
    params: query (dict)
    Description: Delete document matching query from collection
    """
    def delete(self, query):
        if query is not None:
            try:
                result = self.collection.delete_one(query)
                return result.deleted_count > 0
            except Exception as e:
                print(f"Error deleting: {e}")
                return False
        raise Exception("Query cannot be empty")

    """
    function close
    params: none
    Description: Close MongoDB connection
    """
    def close(self):
        if self.client:
            self.client.close()
            print("✓ MongoDB connection closed")