from pymongo import MongoClient
from bson.objectid import ObjectId

class AnimalShelter(object):

    def __init__(self, username, password):
        # Initializing the MongoClient. This helps to 
        # access the MongoDB databases and collections.
        # This is hard-wired to use the aac database, the 
        # animals collection, and the aac user.
        # Definitions of the connection string variables are
        # unique to the individual Apporto environment.
        #
        # You must edit the connection variables below to reflect
        # your own instance of MongoDB!
        #
        # Connection Variables
        #
        USER = username
        PASS = password
        HOST = 'nv-desktop-services.apporto.com'
        PORT = 30501
        DB = 'AAC'
        COL = 'animals'
        #
        # Initialize Connection
        #
        self.client = MongoClient('mongodb://%s:%s@%s:%d' % (USER,PASS,HOST,PORT))
        self.database = self.client['%s' % (DB)]
        self.collection = self.database['%s' % (COL)]

# Complete this create method to implement the C in CRUD.
    def create(self, data):
        if data is not None:
            succ_insert = self.database.animals.insert_one(data)
            if succ_insert != 0:
                return True
            else:
                return False
        else:
            raise Exception("Nothing to save, because data parameter is empty")

# Create method to implement the R in CRUD.
    def read(self, sData):
        if sData:
            data = self.database.animals.find(sData, {"_id":False})
        else:
            data=self.database.animals.find({}, {"id":False})
            
        return data
    
# Create method to implement U in CRUD
    def update(self, sData, uData):
        if uData is not None:
            if uData:
                result = self.database.animals.update_many(sData, {"$set":uData})
        else:
            return "{}"
        return result.raw_result

# Create method to implement D in CRUD
    def delete(self, dData):
        if dData is not None:
            if dData:
                result = self.database.animals.delete_one(dData)
        else:
            raise Exception("Nothing to update, rData is empty")
