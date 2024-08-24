document.addEventListener('DOMContentLoaded', function() {
	checkAccessToken();
});


async function login() {
    const id = document.getElementById('id').value;
    const password = document.getElementById('password').value;

    const data = {
        id: id,
        password: password
    };

    try {
		const response = await fetch('/members/login-api', {
		  method: 'POST',
		  headers: {
		    'Content-Type': 'application/json'
		  },
		  body: JSON.stringify(data),
		  credentials: 'include'
		});

        if (response.status === 401) {
            alert("아이디 또는 비밀번호가 잘못되었습니다.");
        } else if (response.status === 500) {
            alert("로그인 중 예상치 못한 오류가 발생했습니다.");
        } else if (response.ok) {
            const responseData = await response.json();
            console.log(responseData);
			alert("로그인에 성공하였습니다.")
			window.location.href = "/";
        } else {
            alert("서버로부터 예상치 못한 응답이 돌아왔습니다.");
        }
    } catch (error) {
        console.error('Error:', error);
        alert("로그인 시도 중 오류가 발생했습니다.");
    }
}

function checkAccessToken() {
    fetch('/members/check-access-token', {
        method: 'POST',
        credentials: 'include' // 쿠키를 포함하여 요청을 보냄
    })
    .then(response => {
        if (response.ok) {
            return response.text(); // 서버의 응답 텍스트를 반환
        } else if (response.status === 401) {
            throw new Error("Token is invalid or expired");
        } else if (response.status === 403) {
            throw new Error("Token is valid but user ID does not match");
        } else {
            throw new Error(response.statusText);
        }
    })
    .then(result => {
        console.log(result);  // 서버에서 반환된 메시지를 출력
        if (result.includes("Token is valid and user ID matches")) {
            console.log("Access token is valid and user ID matches");
            // 토큰이 유효하고 사용자 ID가 일치하는 경우 추가로 수행할 작업
        }
    })
    .catch(error => {
        console.error("Error during access token check:", error.message);
        // 에러 발생 시 추가로 수행할 작업
        if (error.message === "Token is invalid or expired") {
            console.log("Redirecting to login...");
            // 여기서 로그인 페이지로 리다이렉트 등의 작업을 수행할 수 있음
        } else if (error.message === "Token is valid but user ID does not match") {
            console.log("User ID does not match.");
            // 여기서 다른 작업을 수행할 수 있음
        }
    });
}
