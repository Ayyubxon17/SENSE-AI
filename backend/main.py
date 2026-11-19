
from fastapi import FastAPI

app = FastAPI(
    title="SENSE AI Server",
    description="SENSE AI ta'lim platformasi serveri",
    version="1.0.0"
)


@app.get("/")
async def home():
    return {
        "name": "SENSE AI",
        "status": "running",
        "message": "SENSE AI serveriga xush kelibsiz"
    }


@app.get("/health")
async def health():
    return {
        "status": "ok",
        "server": "SENSE AI",
        "ai_connected": False
    }
  
