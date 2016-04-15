<%@ page contentType="text/html; charset=UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="s" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@ page session="false" %>
<!DOCTYPE html>
<html>
<head>
	<meta charset="UTF-8">
	<title>棒棒达快递</title>
	<link href="<c:url value="/resources/bootstrap/css/bootstrap.min.css" />" rel="stylesheet"  type="text/css" />
	<link href="<c:url value="/resources/stylesheets/main.css" />" rel="stylesheet"  type="text/css" /><!--自定义css-->
	<script src="<c:url value="/resources/adminLTE/plugins/jQuery/jQuery-2.1.3.min.js" />"> </script>
	<script src="<c:url value="/resources/bootstrap/js/bootstrap.min.js" />" type="text/javascript"></script>
	<script src="<c:url value="/resources/javascripts/main.js" />"> </script>
</head>
<body>
<!-- S nav -->
<nav class="navbar navbar-default b-navbar">
	<div class="container">
		<!-- Brand and toggle get grouped for better mobile display -->
		<div class="navbar-header">
			<button type="button" class="navbar-toggle collapsed" data-toggle="collapse" data-target="#bs-example-navbar-collapse-1" aria-expanded="false">
				<span class="sr-only">Toggle navigation</span>
				<span class="icon-bar"></span>
				<span class="icon-bar"></span>
				<span class="icon-bar"></span>
			</button>
			<a class="navbar-brand" href="<c:url value="/" />"><img src="<c:url value="/resources/images/logo.png" />" alt="logo" /></a>
		</div>

		<!-- Collect the nav links, forms, and other content for toggling -->
		<div class="collapse navbar-collapse" id="bs-example-navbar-collapse-1">
			<ul class="nav navbar-nav navbar-right f16">
				<li><a href="<c:url value="/" />">首页</a></li>
				<li><a href="javascript:void(0);"><i class="glyphicon glyphicon-user orange">${user.realName}</i></a></li>
				<li><a href="/logout">退出登录</a></li>
			</ul>
		</div><!-- /.navbar-collapse -->
	</div><!-- /.container-fluid -->
</nav>
<!-- E nav -->
<!-- S content -->
<div class="clearfix b-branch">
	<div class="container">
		<div class="row">
			<!-- S sidebar -->
			<div class="col-xs-12 col-sm-12 col-md-3 col-lg-3">
				<ul class="b-sidebar">
					<li class="lv1 side-cur"><a href="<c:url value="/packageToSite" />" target="iframe1" ><i class="b-icon p-package"></i>包裹到站</a></li>
					<li class="lv1"><a href="<c:url value="/packageDispatch" />" target="iframe1" ><i class="b-icon p-aign"></i>运单分派</a></li>
					<li class="lv1"><a href="<c:url value="/handleAbnormal" />" target="iframe1" ><i class="b-icon p-error"></i>异常件处理</a></li>
					<li class="lv1"><a href="<c:url value="/dataQuery" />" target="iframe1" ><i class="b-icon p-query"></i>数据查询</a></li>
					<li class="lv1"><a href="#"><i class="b-icon p-set"></i>系统设置</a></li>
					<ul class="menu dn">
						<li><a href="<c:url value="/deliverRegion/map/1" />" target="iframe1">配送区域</a></li>
						<li><a href="<c:url value="/userManage/userList" />" target="iframe1">用户管理</a></li>
						<li><a href="system-role.html">角色管理</a></li>
					</ul>
				</ul>
			</div>
			<!-- E sidebar -->

		</div>
	</div>
</div>
<!-- E content -->
<!-- S detail -->
<div class="b-branch-hei">
	<iframe id="iframe1" class="i-hei" name="iframe1" src="<c:url value="/packageToSite" />" frameborder="0" marginheight="0" marginwidth="0" width="100%" height="100%" onLoad="iFrameHeight()"></iframe>
</div><!-- /.content-wrapper -->
<!-- E detail -->

<script>

	$("li").click(function(){
		$(this).addClass("side-cur").siblings().removeClass("side-cur");
	});
</script>
</body>
</html>
