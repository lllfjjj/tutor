const demo = document.querySelector("#fetch-demo");
const statusOutput = document.querySelector("#demo-status");
const buttons = demo.querySelectorAll("button[data-url]");

for (const button of buttons) {
    button.addEventListener("click", async () => {
        for (const control of buttons) control.disabled = true;
        statusOutput.textContent = "请求中…";
        try {
            const response = await fetch(button.dataset.url);
            if (!response.ok) {
                statusOutput.textContent = `请求失败：HTTP ${response.status}`;
                return;
            }
            const lesson = await response.json();
            statusOutput.textContent = `HTTP ${response.status}\n${JSON.stringify(lesson, null, 2)}`;
        } catch {
            statusOutput.textContent = "未能读取响应，请检查本地服务是否运行。";
        } finally {
            for (const control of buttons) control.disabled = false;
        }
    });
}
