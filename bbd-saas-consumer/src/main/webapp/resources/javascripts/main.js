$(function () {

    // S sidebar\
    var inithei = $(window).height();
    var dochei = $(document).height();

    function counthei() {
        var winhei = $(window).height();
        var winhei = $(window).height();
        $(".b-branch").css({minHeight: winhei - 60});
        $(".b-detail").css({minHeight: winhei});
        $(".b-sidebar").css({height: winhei - 60,overflow:"auto"});
        $(".i-hei").css({height:winhei-146});
    };
    counthei();
    $(".b-sidebar .lv1").click(function () {
        if ($(this).hasClass('side-cur')) {//h3有curr
            if ($(this).next('ul.menu').css("display") == "block") {//menu有dn
                $(this).next('ul.menu').slideUp();
            } else {//menu没有dn
                $(this).next('ul.menu').slideDown();
            }
        } else {//h3没有curr
            $(this).siblings('.lv1').removeClass('side-cur');
            $(this).siblings('.menu').slideUp();
            $(this).addClass('side-cur').next('ul.menu').slideDown();
        }
    });

    $(window).resize(function () {
        counthei();
    });
    // E sidebar

// S 站点管理


    // 修改
    shP(".j-siteM-rvs",".j-siteM-pop");
    // 通过
    shP(".j-pass",".j-pass-pop");
    // 驳回
    shP(".j-reject",".j-reject-pop");
    // 查看驳回原因
    shP(".j-rejectR",".j-rejectR-pop");
    // 修改密码
    shP(".j-pwd",".j-pwd-pop");
    // E 站点管理
    // 重新分派
    shP(".j-sel", ".j-sel-pop");
    // 转其他快递
    shP(".j-turn", ".j-turn-pop");
    // 退货
    shP(".j-th", ".j-th-pop");
    // 注册
    shP(".j-site", ".j-site-pop");
    // 新建用户
    shP(".j-user", ".j-user-pop");
    // 新建角色
    shP(".j-role", ".j-role-pop");
    function shP(clickW, showW) {
        $(clickW).on("click", function () {
            $(showW).modal('show');
        });
    }

// S 注册站点
    $("#mask").css({height: dochei + 120});
    // 点击注册
    $(".j-login").on("click", function (e) {
        e.stopPropagation();
        $(".j-login-type").addClass("in").show();
        $("#mask").show();
        $(document.body).addClass("modal-open").css({paddingRight:"17px"});
    });
    // 注册站点
    $(".j-site-re").on("click", function () {
        $(this).parents(".modal").hide();
        $("#mask").show();
        $(".j-site-re-pop").addClass("in").show();
        $(document.body).addClass("modal-open").css({paddingRight:"17px"});
    });
    $(".j-scroll-dislog").on("click", function (e) {
        e.stopPropagation();
    })
//    $(document).on("click", function () {
//        $(".j-login-type,#mask").hide()
//    })
    $(".j-close").on("click",function(){
        $(this).parent().parent().parent().parent().hide();
        $("#mask").hide();
        $(".j-guide-pop").hide().removeClass("in");
        $(document.body).removeClass("modal-open").css({paddingRight:"0"});
    })
    // E 注册站点

    //全选
    $(".j-sel-all").on("click", function () {
        if (!$("input[name=inputA]").is(':checked')) {
            $("input[name=inputC]").prop("checked", false);
        } else {
            $("input[name=inputC]").prop("checked", true);
        }
    })
    // S 浮层固定定位
    $(parent.window).scroll(function(){
	  $('.j-pl-pop').css({top:$(parent.window).scrollTop()});
	})
	// E 浮层固定定位
	
	// S iframe嵌进去的页面的左导航
	var parentD=$('#psrE',window.parent.document);  
	var ulhtml=parentD.find(".b-sidebar").html();
	$(".sub-sidebar").html(ulhtml);
	// E iframe嵌进去的页面的左导航

    //S new 注册
    //点击保存
    nstep=0;
    var innerwid=$(".n-progress-inner").width();
    $('.j-next').click(function(){
        nstep+=1
        // 3的倍数
        if(nstep==3){
            nstep=2;
            return;
        }else if(nstep<3){
            $(".n-progress-inner").css({width:innerwid+432*nstep})
            $(".n-progress-txt span").eq(nstep).addClass("n-fcur");
        }

        showStep(nstep)
    });
    //点击上一步
    $('.j-prev').click(function(){
        nstep-=1
        if(nstep<=0){
            nstep=0;
            $(".n-progress-txt span").eq(nstep+1).removeClass("n-fcur");
        }else if(nstep>=0){
            $(".n-progress-txt span").eq(nstep+1).removeClass("n-fcur");
            $(".n-progress-inner").css({width:innerwid+432*nstep})
        }
        showStep(nstep);
    });


    //点击按钮执行的函数
    function showStep(nstep){
        var viewwid=$('.n-form-infoA').width();
        $(".n-form-info").css({"margin-left": -viewwid*nstep+'px'});
    };

    // 站点
    $(".j-next-site").click(function(){
        var viewwid=$('.n-form-infoA').width();
        $(".n-form-info").css({"margin-left": -viewwid*3+'px'});
        $(".n-progress-inner").css({width:innerwid+432*3});
        $(".n-progress-txt span").eq(2).addClass("n-fcur");
    });

    $(".j-prev-site").click(function(){
        var viewwid=$('.n-form-infoA').width();
        $(".n-form-info").css({"margin-left": -viewwid*1+'px'});
        $(".n-progress-inner").css({width:innerwid+432*1})
        $(".n-progress-txt span").eq(2).removeClass("n-fcur");
    });

    // 配送公司 提交
    $(".j-company").click(function(){
        $(".n-form-infoA,.n-progress-area").hide();
        $(".j-company-result").show();
    });
    $(".j-site-s").click(function(){
        $(".n-form-infoA,.n-progress-area").hide();
        $(".j-site-result").show();
    });

    //E new 注册

    // S 站点管理
    $(".j-siteM").click(function(){
        var rvstxt=$(this).find("em").html();
        $(".j-cg-txt").html(rvstxt);
    });
    $(".j-sel-val").click(function(){
        if($(this).val() == 5){
            $(".j-dn").show();
        }else{
            $(".j-dn").hide();
        }
    })
    // E 站点管理

    // S 密码
    $(".j-n-pwd").on("blur",function(){
        var pwdval=$(this).val();
        if(!pwdreg.test(pwdval)){
            $(".b-prompt").addClass("mov");
            $(".b-prompt i").html("请输入6-12位数字和字母结合的密码")
            outDiv();
        }
    })
    //确认密码
    $(".j-c-pwd").on("blur",function(){
        if($(this).val()!="" && $(this).val() != $(".j-n-pwd").val()){
            $(".b-prompt").addClass("mov");
            $(".b-prompt i").html("两次密码不一致")
            outDiv();
        }
    })
    // E 密码

    // S iframe密码

    $(".j-nf-pwd").on("blur",function(){
        var pwdval2=$(this).val();
        if(!pwdreg.test(pwdval2)){
            ioutDiv("请输入6-12位数字和字母结合的密码");
        }
    })
//确认密码
    $(".j-cf-pwd").on("blur",function(){
        if($(this).val()!="" && $(this).val() != $(".j-nf-pwd").val()){
            ioutDiv("两次密码不一致");
        }
    })
// E iframe密码

    // S 站点管理新建
    // 新建

    //$(".j-siteM").on("click", function (e) {
    //    e.stopPropagation();
    //    $(".j-siteM-pop").addClass("in").show();
    //    $("#mask").show();
    //    $("#mask").css({left:"16%"})
    //    $('.j-siteM-pop').css({top:$(parent.window).scrollTop()});
    //    parentD.addClass("modal-open").css({paddingRight:"17px"});
    //})

    $(".j-f-close").on("click",function(){
        $(this).parent().parent().parent().parent().parent().hide();
        $("#mask").hide();
        $(".j-siteM-pop").hide().removeClass("in");
        parentD.removeClass("modal-open").css({paddingRight:"0"});
    })

    $(".n-re-con").css({minHeight:inithei-152});


    yscroll();
    // S 最后一个元素去掉间距
    tap0(".navbar-nav > li:last-child a");
    // E 最后一个元素去掉间距

    // S 判断是 ie8/9 的话 就给 input 设置 value
    //if(navigator.appName == "Microsoft Internet Explorer" && navigator.appVersion.match(/8./i)=="8." || navigator.appName == "Microsoft Internet Explorer" && navigator.appVersion.match(/9./i)=="9.") {
    //    $("input").each(function(){
    //        var plh=$(this).attr("placeholder");
    //        if($(this).val() == "" || $(this).val() == undefined ){
    //            if(plh != "" &&  plh != undefined ){
    //                $(this).attr("value",plh)
    //            }
    //
    //        }
    //
    //    })
    //}
    // E 判断是 ie8/9 的话 就给 input 设置 value

    $(".all-area,.y-scroll").scrollUnique();
})
var pwdreg=/^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{6,12}$/;
var mailNumreg=/^[0-9A-Za-z]{10,20}$/;
//iframe 自适应高度
//function iFrameHeight() {
//
//    var ifm = document.getElementById("iframe1");
//
//    var subWeb = document.frames ? document.frames["iframe1"].document : ifm.contentDocument;
//
//    //console.log("ht===000="+ifm.height);
//    if (ifm != null && subWeb != null) {
//        /*if(ifm.height){
//            console.log("ht===000="+ifm.height);
//        }*/
//        ifm.height = subWeb.body.scrollHeight;
//        if(ifm.height < 1580){
//            ifm.height = subWeb.body.scrollHeight + 150;
//        }
//    }
//    //console.log("ht===="+ifm.height);
//}
// S 自己的alert 提示
function alert_mine(titile,content){
    $(".j-alert-pop").modal();
    $(".j-c-tie").html(titile)
    $(".j-alert-con").html(content)
}
// E 自己的alert 提示

// S 气泡 提示
function outDiv(content){
    $(".b-prompt").addClass("mov");
    $(".b-prompt i").html(content);
    var txtwid=$(".b-prompt .b-prompt-txt").outerWidth();
    $(".b-prompt-txt").css({marginLeft:-txtwid/2})
    setTimeout(function(){
        $(".b-prompt").removeClass("mov");
    },2000)
}
// E 气泡 提示
// S iframe气泡 提示
var parentF=$('#psrE',window.parent.document);
function ioutDiv(content, time){
    parentF.find(".b-prompt").addClass("mov");
    parentF.find(".b-prompt i").html(content);
    var txtwid=parentF.find(".b-prompt .b-prompt-txt").outerWidth();
    parentF.find(".b-prompt-txt").css({marginLeft:-txtwid/2})
    if(time == null){
        time = 3000;
    }
    setTimeout(function(){
        parentF.find(".b-prompt").removeClass("mov")
    },time);

}
parentF.find(".b-prompt").removeClass("mov")
// E iframe气泡 提示




// S 图片上传
function setImagePreviews(avalue) {
    var docObj = document.getElementById("licensePic");
    var dd = document.getElementById("lcs-img");
    dd.innerHTML = "";
    var fileList = docObj.files;
    for (var i = 0; i < fileList.length; i++) {
        dd.innerHTML += "<div style='float:left' > <img id='img" + i + "' class='img0'  /> </div>";
        var imgObjPreview = document.getElementById("img" + i);
        if (docObj.files && docObj.files[i]) {
            //火狐下，直接设img属性
            imgObjPreview.style.display = 'block';
            imgObjPreview.style.width = '180px';
            //火狐7以上版本不能用上面的getAsDataURL()方式获取，需要一下方式
            imgObjPreview.src = window.URL.createObjectURL(docObj.files[i]);
        }
        else {
            //IE下，使用滤镜
            docObj.select();
            var imgSrc = document.selection.createRange().text;
            var localImagId = document.getElementById("img" + i);
            //必须设置初始大小

            localImagId.style.width = "150px";

            //图片异常的捕捉，防止用户修改后缀来伪造图片

            try {

                localImagId.style.filter = "progid:DXImageTransform.Microsoft.AlphaImageLoader(sizingMethod=scale)";

                localImagId.filters.item("DXImageTransform.Microsoft.AlphaImageLoader").src = imgSrc;

            }

            catch (e) {

                alert("您上传的图片格式不正确，请重新选择!");

                return false;

            }

            imgObjPreview.style.display = 'none';

            document.selection.empty();

        }

    }


    return true;

}
// E 图片上传

// S 子元素scroll父元素容器不跟随滚动JS实现
$.fn.scrollUnique = function() {
    return $(this).each(function() {
        var eventType = 'mousewheel';
        if (document.mozHidden !== undefined) {
            eventType = 'DOMMouseScroll';
        }
        $(this).on(eventType, function(event) {
            // 一些数据
            var scrollTop = this.scrollTop,
                scrollHeight = this.scrollHeight,
                height = this.clientHeight;

            var delta = (event.originalEvent.wheelDelta) ? event.originalEvent.wheelDelta : -(event.originalEvent.detail || 0);

            if ((delta > 0 && scrollTop <= delta) || (delta < 0 && scrollHeight - height - scrollTop <= -1 * delta)) {
                // IE浏览器下滚动会跨越边界直接影响父级滚动，因此，临界时候手动边界滚动定位
                this.scrollTop = delta > 0? 0: scrollHeight;
                // 向上滚 || 向下滚
                event.preventDefault();
            }
        });
    });
};
// E 子元素scroll父元素容器不跟随滚动JS实现

// S 最后一个元素去掉间距
function tap0(tapEle){
    $(tapEle).css({paddingRight:0})
}
// E 最后一个元素去掉间距

// S 超出显示滚动条
function yscroll(){
    var yhei=$(".y-scroll").height();
    if(window.screen.availHeight<800){
        if(yhei>=200){
            $(".y-scroll").css({height:"200px",overflowY:"scroll"})
        }
    }else{
        if(yhei>=420){
            $(".y-scroll").css({height:"420px",overflowY:"scroll"})
        }
    }
}
// E 超出显示滚动条


//S 解决 ie8 不支持foreach
if ( !Array.prototype.forEach ) {

    Array.prototype.forEach = function forEach( callback, thisArg ) {

        var T, k;

        if ( this == null ) {
            throw new TypeError( "this is null or not defined" );
        }
        var O = Object(this);
        var len = O.length >>> 0;
        if ( typeof callback !== "function" ) {
            throw new TypeError( callback + " is not a function" );
        }
        if ( arguments.length > 1 ) {
            T = thisArg;
        }
        k = 0;

        while( k < len ) {

            var kValue;
            if ( k in O ) {

                kValue = O[ k ];
                callback.call( T, kValue, k, O );
            }
            k++;
        }
    };
}
//E 解决 ie8 不支持foreach

// S 选中文本框的值
function selectInputTxt(id){
    //console.log("id==="+id);
    $("#"+id).focus();
    $("#"+id).select();
}