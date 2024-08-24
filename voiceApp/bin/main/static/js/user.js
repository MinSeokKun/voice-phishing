$(document).ready(function() {
    function login(username, password) {
        return $.ajax({
            url: '/login',
            method: 'POST',
            contentType: 'application/json',
            data: JSON.stringify({ username, password })
        }).then(function() {
            return true;
        }).fail(function() {
            alert("아이디 또는 비밀번호가 일치하지 않습니다.");
            return false;
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

    function getAccessToken() {
        return $.ajax({
            url: '/checkLogin',
            method: 'GET',
            dataType: 'json'
        }).then(function(response) {
            return response.accessToken;
        }).catch(function(error) {
            console.error('로그인 상태 확인 중 오류 발생:', error);
            return null;
        });
    }

    function checkLoginStatus() {
        return $.ajax({
            url: '/checkLogin',
            method: 'GET',
            dataType: 'json'
        }).then(function(response) {
            return response.isLoggedIn;
        }).catch(function(error) {
            console.error('로그인 상태 확인 중 오류 발생:', error);
            return false;
        });
    }

    function updateMenu() {
        checkLoginStatus().then(function(isLoggedIn) {
            if (isLoggedIn) {
                $('#logged-in-menu').show();
                $('#logged-out-menu').hide();
            } else {
                $('#logged-in-menu').hide();
                $('#logged-out-menu').show();
            }
        }).catch(function(error) {
            console.error('메뉴 업데이트 중 오류 발생:', error);
            $('#logged-in-menu').hide();
            $('#logged-out-menu').show();
        });
    }

    // 페이지 로드 시 메뉴 업데이트
    updateMenu();

    $('.user-icon').click(function(e) {
        e.preventDefault();
        $('.dropdown-menu').toggleClass('show');
    });

    $(document).click(function(e) {
        if (!$(e.target).closest('.user-menu').length) {
            $('.dropdown-menu').removeClass('show');
        }
    });

    $('#logout-link').click(function(e) {
        e.preventDefault();
        logout();
        updateMenu();
    });

    $('#loginForm').submit(function(e) {
        e.preventDefault();
        var username = $('#username').val();
        var password = $('#password').val();
        
        login(username, password).done(function(loginSuccess) {
            if (loginSuccess) {
                window.location.href = "/";
            } else {
                alert('Login failed. Please try again.');
            }
        });
    });
});