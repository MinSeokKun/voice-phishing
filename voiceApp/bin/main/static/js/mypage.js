function getMyPageInfo() {
    fetch('/api/mypage', {
        method: 'GET',
        credentials: 'include',
        headers: {
            'Accept': 'application/json'
        }
    })
    .then(response => {
        if (response.ok) {
            return response.json();
        } else if (response.status === 401) {
            throw new Error('인증되지 않은 사용자입니다.');
        } else {
            throw new Error('서버 오류가 발생했습니다.');
        }
    })
    .then(data => {
        updateUserInfo(data);
    })
    .catch(error => {
        console.error('에러:', error.message);
        alert(error.message);
    });
}

function updateUserInfo(data) {
    document.getElementById('email').textContent = data.email || '정보 없음';
    document.getElementById('username').textContent = data.username || '정보 없음';
    document.getElementById('name').value = data.name || '';
}

function submitUserInfo(event) {
    event.preventDefault();
    
    const formData = new FormData(document.getElementById('userInfoForm'));
    const jsonData = Object.fromEntries(formData);
    
    fetch('/api/mypage/update', {
        method: 'POST',
        credentials: 'include',
        headers: {
            'Content-Type': 'application/json',
            'Accept': 'application/json'
        },
        body: JSON.stringify(jsonData)
    })
    .then(response => {
        if (response.ok) {
            return response.json();
        } else {
            throw new Error('정보 수정에 실패했습니다.');
        }
    })
    .then(data => {
        alert('정보가 성공적으로 수정되었습니다.');
        getMyPageInfo(); // 수정된 정보 다시 불러오기
    })
    .catch(error => {
        console.error('에러:', error.message);
        alert(error.message);
    });
}

// 페이지 로드 시 정보 가져오기
window.addEventListener('load', getMyPageInfo);

// 폼 제출 이벤트 리스너 추가
document.getElementById('userInfoForm').addEventListener('submit', submitUserInfo);