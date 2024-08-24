$(document).ready(function() {
    /* 토글 사이드 바 버튼 관련 */
    $('.toggle-side-bar-btn').click(function(e){
        e.stopPropagation(); // 이벤트 버블링 방지
        
        var $clicked = $(this);
        var nowAnimating = $clicked.attr('data-ico-now-animating');
        
        if ( nowAnimating == "Y" ){
            return;
        }
        
        $clicked.attr('data-ico-now-animating', 'Y');
        
        // nav의 토글 버튼인 경우
        if ($clicked.closest('#navbar').length) {
            hideLeftSideBar();
        } 
        // 헤더의 토글 버튼인 경우
        else if ($clicked.closest('#header').length) {
            $clicked.toggleClass('active');
            if ($clicked.hasClass('active')) {
                showLeftSideBar();
            } else {
                hideLeftSideBar();
            }
        }
        
        setTimeout(function(){
            $clicked.attr('data-ico-now-animating', 'N');
        }, 400);
    });

    /* 왼쪽 사이드바 함수 */
    function showLeftSideBar(){
        $('.left-side-bar > .menu-1 ul > li.active').removeClass('active');
        $('.left-side-bar-box').addClass('active');
        // $('.logo').fadeOut('fast');
    }
    
    function hideLeftSideBar(){
        $('.left-side-bar-box').removeClass('active');
        // $('.logo').fadeIn('slow');
        $('#header .toggle-side-bar-btn').removeClass('active');
    }
	
	/* 메뉴 접히고 펼치기 */
	$('.left-side-bar > .menu-1 ul > li').click(function(e){
	    //console.log("메뉴 클릭됨");
	    
	    /* 만약 클릭된 메뉴에 엑티브 클래스가 있으면 */
	    if ( $(this).hasClass('active') ){
	        /* 클릭된 메뉴의 엑티브를 없앤다 */
	        $(this).removeClass('active');
	    }
	    else {
	        /* 클릭된 메뉴의 형제의 엑티브를 없앤다 */
	        $(this).siblings('.active').removeClass('active');
	        
	        /* 클릭된 메뉴(지역)의 엑티브를 없앤다 */
	        $(this).find('.active').removeClass('active');
	        
	        /* 클릭된 메뉴의 엑티브를 만든다 */
	        $(this).addClass('active');
	    }
	    
	    /* 클릭된 메뉴 안에 다른 메뉴를 클릭하면 위에있는 메뉴가 같이 클릭되는데 그것을 막아준다 */
	    e.stopPropagation();
	});
	
	/* 좌측 사이드바 배경을 클릭했을때 */
	$('.left-side-bar-box').click(function(){
	    //console.log('배경클릭');
	    
	    /* 토글 사이드바 버튼을 클릭한 효과를 만듬 */
	    $('.toggle-side-bar-btn').click();
	});
	
	/* 사이드바를 클릭할때 상위요소인 배경이 같이 클릭되어서 사이드바가 들어가버리기 때문에 그것을 막음 */
	$('.left-side-bar').click(function(e){
	    e.stopPropagation();
	});
});

//ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ
$(document).ready(function() {
    $('.list-title').click(function() {
        var $item = $(this).parent();
        if($item.hasClass('active')) {
            $item.removeClass('active');
            $item.find('.list-content').slideUp();
        } else {
            $('.list-item.active .list-content').slideUp();
            $('.list-item.active').removeClass('active');
            $item.addClass('active');
            $item.find('.list-content').slideDown();
        }
         // 색상 변경을 위한 클래스 토글
        $(this).toggleClass('active');
    });
});
function searchList() {
    var input, filter, listItems, title, content, i, txtValue;
    input = document.getElementById('searchInput');
    filter = input.value.toUpperCase();
    listItems = document.getElementsByClassName('list-item');

    for (i = 0; i < listItems.length; i++) {
        title = listItems[i].getElementsByClassName('list-title')[0];
        content = listItems[i].getElementsByClassName('list-content')[0];
        txtValue = title.textContent || title.innerText;
        txtValue += content.textContent || content.innerText;

        if (txtValue.toUpperCase().indexOf(filter) > -1) {
            listItems[i].style.display = "";
        } else {
            listItems[i].style.display = "none";
        }
    }
}
function deleteVoice(element) {
    if (confirm('정말로 삭제하시겠습니까?')) {
        var url = element.getAttribute('data-uri');
        var token = $("meta[name='_csrf']").attr("content");
        var header = $("meta[name='_csrf_header']").attr("content");
        
        fetch(url, {
            method: 'DELETE',
            headers: {
                'Content-Type': 'application/json',
                [header]: token
            },
        }).then(response => {
            if (response.ok) {
                alert('삭제되었습니다.');
                location.reload();
            } else {
                alert('삭제에 실패했습니다.');
            }
        }).catch(error => {
            console.error('Error:', error);
            alert('오류가 발생했습니다.');
        });
    }
}