
const CORS = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Methods": "GET, POST, OPTIONS",
  "Access-Control-Allow-Headers": "Content-Type"
};

function json(data, status = 200) {
  return new Response(JSON.stringify(data), {
    status,
    headers: {
      ...CORS,
      "Content-Type": "application/json; charset=utf-8"
    }
  });
}

const MODELS = {
  text: "@cf/zai-org/glm-4.7-flash",
  vision: "@cf/moondream/moondream3.1-9B-A2B"
};

export default {
  async fetch(request, env) {
    const url = new URL(request.url);

    if (request.method === "OPTIONS") {
      return new Response(null, {
        status: 204,
        headers: CORS
      });
    }

    if (request.method === "GET" && url.pathname === "/") {
      return json({
        name: "SENSE AI",
        status: "running",
        version: "2.0"
      });
    }

    if (request.method === "GET" && url.pathname === "/health") {
      return json({
        status: "ok",
        ai_binding: Boolean(env.AI),
        models: MODELS
      });
    }

    if (request.method !== "POST" || url.pathname !== "/ask") {
      return json({ error: "Manzil topilmadi" }, 404);
    }

    if (!env.AI) {
      return json({ error: "AI xizmati ulanmagan" }, 503);
    }

    const contentType = request.headers.get("Content-Type") || "";

    if (!contentType.includes("application/json")) {
      return json({ error: "JSON formatida yuboring" }, 415);
    }

    const declaredLength = Number(
      request.headers.get("Content-Length") || 0
    );

    if (declaredLength > 5500000) {
      return json({ error: "So‘rov hajmi juda katta" }, 413);
    }

    let raw;
    let body;

    try {
      raw = await request.text();

      if (raw.length > 5500000) {
        return json({ error: "So‘rov hajmi juda katta" }, 413);
      }

      body = JSON.parse(raw);
    } catch {
      return json({ error: "JSON ma'lumoti noto‘g‘ri" }, 400);
    }

    if (!body || typeof body !== "object" || Array.isArray(body)) {
      return json({ error: "So‘rov ma'lumoti noto‘g‘ri" }, 400);
    }

    const question = String(body.question || "").trim();

    const language = ["uz", "ru", "en"].includes(body.language)
      ? body.language
      : "uz";

    if (!question || question.length > 2000) {
      return json({
        error: "Savol 1–2000 belgi bo‘lishi kerak"
      }, 400);
    }

    const languageNames = {
      uz: "o‘zbek tilida",
      ru: "rus tilida",
      en: "ingliz tilida"
    };

    const instruction =
      "Sen SENSE AI ta'lim yordamchisisan. " +
      "Foydalanuvchiga mavzularni sodda va tushunarli tushuntir. " +
      "Kerak bo‘lsa misollar keltir va bosqichma-bosqich izohla. " +
      "Javobni " + languageNames[language] + " ber. " +
      "Bilmagan narsangni to‘qib chiqarmagin. " +
      "Savolga bevosita javob ber.";

    try {
      let answer = "";

      if (body.image_base64) {
        const image = String(body.image_base64);

        const imageType = String(
          body.image_type || "image/jpeg"
        );

        const allowedTypes = [
          "image/jpeg",
          "image/png",
          "image/webp"
        ];

        if (!allowedTypes.includes(imageType)) {
          return json({ error: "Rasm formati qo‘llab-quvvatlanmaydi" }, 400);
        }

        if (
          image.length > 4500000 ||
          image.length < 20 ||
          !/^[A-Za-z0-9+/]+={0,2}$/.test(image)
        ) {
          return json({ error: "Rasm ma'lumoti noto‘g‘ri" }, 400);
        }

        const result = await env.AI.run(
          MODELS.vision,
          {
            task: "query",
            image: "data:" + imageType + ";base64," + image,
            question: instruction + "\nFoydalanuvchi savoli: " + question,
            max_tokens: 700,
            stream: false
          }
        );

        answer = result?.answer || result?.caption || "";

      } else {
        const result = await env.AI.run(
          MODELS.text,
          {
            messages: [
              {
                role: "system",
                content: instruction
              },
              {
                role: "user",
                content: question
              }
            ],
            max_tokens: 900,
            stream: false
          }
        );

        answer =
          result?.response ||
          result?.choices?.[0]?.message?.content ||
          "";
      }

      if (typeof answer !== "string" || !answer.trim()) {
        return json({
          error: "AI javob qaytarmadi. Qayta urinib ko‘ring."
        }, 502);
      }

      return json({
        success: true,
        answer: answer.trim(),
        language: language
      });

    } catch (error) {
      return json({
        success: false,
        error: "AI xizmati vaqtincha ishlamayapti. Keyinroq urinib ko‘ring."
      }, 503);
    }
  }
};
      
