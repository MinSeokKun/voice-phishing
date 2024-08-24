function login(username, password) {
    return $.ajax({
        url: '/login',
        method: 'POST',
        contentType: 'application/json',
        data: JSON.stringify({ username, password })
        }).then(function(data, textStatus, jqXHR) {
            // const accessToken = jqXHR.getResponseHeader('access');
            // localStorage.setItem('access', accessToken);
            return true;
        }).fail(function(jqXHR, textStatus, errorThrown) {
            console.error('Login failed');
            return false;
        });
}

function makeAuthenticatedRequest(url) {
    $.ajax({
        url: url,
        xhrFields: {
            withCredentials: true
        },
        success: function(html) {
            $('body').html(html);
            history.pushState(null, '', url);
        },
        error: function(jqXHR) {
            if (jqXHR.status === 401) {
                refreshToken().then(function() {
                    makeAuthenticatedRequest(url);
                });
            } else {
                throw new Error(`Access to ${url} failed`);
            }
        }
    }).fail(function(error) {
        console.error(error);
        alert('페이지 로드에 실패했습니다.');
    });
}

function refreshToken() {
    return $.ajax({
        url: '/reissue',
        method: 'POST',
        xhrFields: {
            withCredentials: true
        },
        error: function() {
            console.error('Error refreshing token');
            logout();
        }
    });
}

function logout() {
    $.ajax({
        url: '/logout',
        method: 'POST',
        xhrFields: {
            withCredentials: true
        },
        success: function() {
            window.location.href = '/member/login';
        },
        error: function(error) {
            console.error('Logout failed:', error);
        }
    });
}

function navigateTo(url) {
    makeAuthenticatedRequest(url);
}

function handleLoginSuccess() {
    makeAuthenticatedRequest('/');
}

// access 쿠키 가져오는 함수
function getAccessToken() {
    return $.ajax({
        url: '/checkLogin',
        method: 'GET',
        dataType: 'json'
    }).then(function(response) {
        // console.log(response.isLoggedIn)
        return response.accessToken;
    }).catch(function(error) {
        console.error('로그인 상태 확인 중 오류 발생:', error);
        return null;
    });
}

// 토큰 검증 함수
function validateToken(accessToken) {
    return fetch('/validate-token', {
        method: 'GET',
        headers: {
            'access': accessToken
        }
    }).then(response => {
        if (response.ok) {
            return true; // 토큰이 유효함
        } else if (response.status === 401) {
            return false; // 토큰이 만료되었거나 유효하지 않음
        } else {
            throw new Error('Token validation failed');
        }
    });
}

// 토큰 재발급 함수
function reissueToken() {
    return fetch('/reissue', {
        method: 'POST',
        credentials: 'include' // 쿠키를 포함시키기 위해 필요
    }).then(response => {
        if (response.ok) {
            // 새 토큰이 쿠키에 자동으로 설정됨
            return true;
        } else {
            // 에러 메시지 파싱
            return response.text().then(errorMsg => {
                throw new Error(errorMsg);
            });
        }
    });
}

// 사용 예시
function handleApiCall() {
    getAccessToken()
        .then(accessToken => {
            if (!accessToken) {
                throw new Error('No access token available');
            }
            return validateToken(accessToken);
        })
        .then(isValid => {
            if (isValid) {
                return performApiCall();
            } else {
                return reissueToken().then(() => getAccessToken());
            }
        })
        .then(newToken => {
            if (newToken) {
                return performApiCall();
            }
        })
        .catch(error => {
            console.error('Error:', error);
            // 에러 처리 (예: 로그인 페이지로 리다이렉트)
        });
}

// 실제 API 호출을 수행하는 함수 (예시)
function performApiCall() {
    // 여기에 실제 API 호출 로직 구현
    console.log('Performing API call with valid token');
}

$(document).ready(function() {
    $('#loginForm').submit(function(e) {
        e.preventDefault();
        var username = $('#username').val();
        var password = $('#password').val();
        
        login(username, password).done(function(loginSuccess) {
            if (loginSuccess) {
                // handleLoginSuccess();
                window.location.href = "/";
            } else {
                alert('Login failed. Please try again.');
            }
        });
    });
});