# How to Run the Animal Shelter Dashboard

## Prerequisites

- Python 3.8 or higher installed
- MongoDB installed and running
- MongoDB Compass (for easy data import)

## Quick Start

### 1. Install Required Packages

Open PowerShell or Terminal in your project folder:

```bash
python -m pip install --upgrade pip
python -m pip install dash==2.14.2
python -m pip install dash-leaflet==1.0.15
python -m pip install plotly==5.18.0
python -m pip install pymongo==4.6.1
python -m pip install pandas
```

### 2. Start MongoDB

**Windows:**
```cmd
net start MongoDB
```

### 3. Import Your Data to the Database

#### Option A: Using MongoDB Compass (Easiest)

1. Open MongoDB Compass
2. Connect to `mongodb://localhost:27017`
3. Create database: `AAC`
4. Create collection: `animals`
5. Click "ADD DATA" → "Import JSON or CSV file"
6. Select your CSV file
7. Make sure "Header Row" is checked
8. Click "Import"

#### Option B: Using Python Script

```bash
python import_data.py your_data.csv
```

### 4. Run the Dashboard

```bash
python dashboard.py
```

### 5. Open Your Browser
Navigate to: **http://localhost:8050**
