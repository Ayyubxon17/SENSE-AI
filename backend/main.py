
import os
import base64

from fastapi import FastAPI, HTTPException
from pydantic import BaseModel, Field
from openai import AsyncOpenAI, APIError

app = FastAPI(
    title="SENSE AI",
    description="AI Education Platform",
    version="1.0.0"
)

MODEL = os.getenv("OPENAI_MODEL", "gpt-5.5")


class AskRequest(BaseModel):
    question: str = Field(min_length=1, max_length=2000)
    language: str = "uz"
    image_base64: str | None = None
    image_type: str = "image/jpeg"


@app.get("/")
async def home():
    return {
        "name": "SENSE AI",
        "status": "running"
    }


@app.get("/health")
async def health():
    return {
        "status": "ok",
        "ai_configured": bool(os.getenv("OPENAI_API_KEY"))
    }


@app.post("/ask")
async def ask_ai(data: AskRequest):
    api_key = os.getenv("OPENAI_API_KEY")

    if not api_key:
        raise HTTPException(
            status_code=503,
            detail="AI server sozlanmagan"
        )

    languages = {
        "uz": "O‘zbek tilida",
        "ru": "Rus tilida",
        "en": "Ingliz tilida"
    }

    language = languages.get(data.language, "O‘zbek tilida")

    content = [
        {
            "type": "input_text",
            "text": data.question
        }
    ]

    if data.image_base64:
        if len(data.image_base64) > 7_000_000:
            raise HTTPException(
                status_code=413,
                detail="Rasm hajmi juda katta"
            )

        if data.image_type not in [
            "image/jpeg",
            "image/png",
            "image/webp"
        ]:
            raise HTTPException(
                status_code=400,
                detail="Rasm formati qo‘llab-quvvatlanmaydi"
            )

        try:
            base64.b64decode(
                data.image_base64,
                validate=True
            )
        except Exception:
            raise HTTPException(
                status_code=400,
                detail="Rasm ma'lumoti noto‘g‘ri"
            )

        content.append({
            "type": "input_image",
            "image_url": (
                f"data:{data.image_type};base64,"
                f"{data.image_base64}"
            )
        })

    try:
        client = AsyncOpenAI(api_key=api_key)

        response = await client.responses.create(
            model=MODEL,
            instructions=(
                "Sen SENSE AI ta'lim yordamchisisan. "
                "Foydalanuvchiga mavzularni sodda, "
                "aniq va bosqichma-bosqich tushuntir. "
                f"Javobni {language} ber. "
                "Agar rasm yuborilgan bo‘lsa, uni tahlil qil. "
                "Bilmagan narsangni to‘qib chiqarmagin."
            ),
            input=[
                {
                    "role": "user",
                    "content": content
                }
            ],
            max_output_tokens=1200
        )

        return {
            "success": True,
            "answer": response.output_text
        }

    except APIError:
        raise HTTPException(
            status_code=502,
            detail="AI xizmatida vaqtinchalik xatolik"
        )
        
