
async function predict() {
    const text = document.getElementById('inputText').value;
    const apiKey = document.getElementById('apiKeyInput').value;
    apiKey = '808ea858-ff41-4a0c-a24b-a500585c993e';
    if (!apiKey) {
        alert('API 키를 입력해주세요.');
        return;
    }

    try {
        const response = await fetch('http://192.168.0.175:8574/api/predict', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-API-Key': apiKey
            },
            body: JSON.stringify({ text: text })
        });

        if (!response.ok) {
            throw new Error(`HTTP 오류! 상태: ${response.status}`);
        }

        const result = await response.json();
        displayResult(result);
    } catch (error) {
        console.error('예측 실패:', error);
        document.getElementById('result').textContent = '예측에 실패했습니다. 다시 시도해주세요.';
    }
}