
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
        ai: "Cloudflare Workers AI"
      });
    }

    if (request.method === "GET" && url.pathname === "/health") {
      return json({
        status: "ok",
        ai_binding: Boolean(env.AI)
      });
    }

    if (request.method !== "POST" || url.pathname !== "/ask") {
      return json({ error: "Manzil topilmadi" }, 404);
    }

    const length = Number(
      request.headers.get("Content-Length") || 0
    );

    if (length > 5500000) {
      return json({ error: "So‘rov juda katta" }, 413);
    }

    let body;

    try {
      body = await request.json();
    } catch {
      return json({ error: "JSON noto‘g‘ri" }, 400);
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
      "Mavzularni sodda va bosqichma-bosqich tushuntir. " +
      "Javobni " + languageNames[language] + " ber. " +
      "Bilmagan narsangni to‘qib chiqarmagin.";

    try {
      let answer = "";

      if (body.image_base64) {
        const image = String(body.image_base64);
        const imageType = String(
          body.image_type || "image/jpeg"
        );

        if (![
          "image/jpeg",
          "image/png",
          "image/webp"
        ].includes(imageType)) {
          return json({ error: "Rasm formati noto‘g‘ri" }, 400);
        }

        if (
          image.length > 4500000 ||
          !/^[A-Za-z0-9+/=]+$/.test(image)
        ) {
          return json({ error: "Rasm hajmi yoki ma'lumoti noto‘g‘ri" }, 400);
        }

        const result = await env.AI.run(
          "@cf/moondream/moondream3.1-9B-A2B",
          {
            task: "query",
            image: "data:" + imageType + ";base64," + image,
            question: instruction + "\nSavol: " + question,
            max_tokens: 700,
            stream: false
          }
        );

        answer = result.answer || result.caption || "";

      } else {
        const result = await env.AI.run(
          "@cf/zai-org/glm-4.7-flash",
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
            max_tokens: 900
          }
        );

        answer = result.response || "";
      }

      if (!answer) {
        return json({
          error: "AI javob qaytarmadi"
        }, 502);
      }

      return json({
        success: true,
        answer: answer
      });

    } catch (error) {
      return json({
        error: "AI xizmati vaqtincha ishlamayapti"
      }, 503);
    }
  }
};
            
