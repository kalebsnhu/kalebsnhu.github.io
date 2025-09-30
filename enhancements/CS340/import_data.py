"""
Import CSV data into MongoDB for Animal Shelter Dashboard
"""

from animalShelter import AnimalShelter
import pandas as pd
import sys

"""
function import_csv_to_mongodb
params: csv_file (str)
Description: Import CSV file into MongoDB AAC database
"""
def import_csv_to_mongodb(csv_file):
    print(f"\n{'='*50}")
    print("Animal Shelter Data Import Tool")
    print(f"{'='*50}\n")
    
    print(f"📂 Reading CSV file: {csv_file}")
    try:
        df = pd.read_csv(csv_file)
        print(f"✓ Found {len(df)} records with {len(df.columns)} columns")
        print(f"  Columns: {', '.join(df.columns.tolist()[:5])}...")
    except Exception as e:
        print(f"✗ Error reading CSV: {e}")
        return
    
    print("\n🔌 Connecting to MongoDB...")
    try:
        db = AnimalShelter()
        print("✓ Connected to MongoDB")
    except Exception as e:
        print(f"✗ Error connecting to MongoDB: {e}")
        print("\nMake sure MongoDB is running:")
        print("  Windows: net start MongoDB")
        print("  Mac: brew services start mongodb-community")
        print("  Linux: sudo systemctl start mongodb")
        return
    
    print("\n📥 Importing data...")
    success_count = 0
    error_count = 0
    
    for idx, row in df.iterrows():
        try:
            record = row.to_dict()
            if db.create(record):
                success_count += 1
            else:
                error_count += 1
        except Exception as e:
            error_count += 1
            if error_count <= 5:
                print(f"  Warning: Error on row {idx}: {e}")
        
        if (idx + 1) % 100 == 0:
            print(f"  Processed {idx + 1}/{len(df)} records...")
    
    print(f"\n{'='*50}")
    print("Import Complete!")
    print(f"{'='*50}")
    print(f"✓ Successfully imported: {success_count} records")
    if error_count > 0:
        print(f"✗ Errors: {error_count} records")
    print(f"\nYou can now run: python dashboard.py")
    print(f"{'='*50}\n")

"""
function main
params: none
Description: Main entry point for CSV import script
"""
def main():
    if len(sys.argv) < 2:
        print("\nUsage: python import_data.py <csv_file>")
        print("\nExample:")
        print("  python import_data.py animals.csv")
        print()
        return
    
    csv_file = sys.argv[1]
    import_csv_to_mongodb(csv_file)

if __name__ == "__main__":
    main()