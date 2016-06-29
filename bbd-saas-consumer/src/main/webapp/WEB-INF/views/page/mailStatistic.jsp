<%@ page language="java" pageEncoding="UTF-8"%>
<%@ page import="com.bbd.saas.models.ExpressStatStation" %>
<%@ page import="com.bbd.saas.utils.PageModel" %>
<%@ page import="com.bbd.saas.enums.UserRole" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<html>
<head>
	<jsp:include page="../main.jsp" flush="true" />
</head>
<%
	String proPath = request.getContextPath();
	String path = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+proPath;
%>
<style type="text/css">
.font-bg-color { background-color: #FFED97; }
</style>
<body class="fbg">

<!-- S content -->
<div class="clearfix b-branch">
	<div class="container-fluid">
		<div class="row">
			<!-- S sidebar -->
			<div class="col-xs-12 col-sm-12 bbd-md-3" style="visibility: hidden;">
				<ul class="sub-sidebar">
				</ul>
			</div>
			<!-- E sidebar -->
			<!-- S detail -->
			<div class="b-detail col-xs-12 col-sm-12 bbd-md-9">
				<!-- S 搜索区域 -->
				<form class="form-inline form-inline-n">
					<div class="search-area">
	  					<div class="row pb20">

							<c:if test="${role == UserRole.COMPANY}">
								<div class="form-group col-xs-12 col-sm-6 col-md-4 col-lg-4">
									<label>站点：</label>
									<select id="areaCode" name="areaCode" class="form-control form-con-new">
										<option value="">全部</option>
										<c:if test="${not empty siteList}">
											<c:forEach var="site" items="${siteList}">
												<option value="${site.areaCode}">${site.name}</option>
											</c:forEach>
										</c:if>
									</select>
								</div>
							</c:if>
	  						<div class="form-group col-xs-12 col-sm-6 col-md-4 col-lg-4 pad0">
	  							<label>时间：</label>
	  							<input id="time" name="time" value="${time}" type="text" placeholder="请选择时间" class="form-control c-disable"  data-date-format="yyyy-mm-dd"/>
	  						</div>
	  					</div>
	  					<div class="row pb20">
	  						<div class="form-group col-xs-12 col-sm-12 col-md-12 col-lg-12">
	  							<a href="javascript:void(0)" class="ser-btn l" onclick="gotoPage(0);"><i class="b-icon p-query p-ser"></i>查询</a>
	  							<a href="javascript:void(0)" class="ser-btn d ml16" onclick="exportData();"><i class="glyphicon glyphicon-off f16 mr10"></i>导出</a>
	  						</div>
	  					</div>
	  				</div>
				</form>
				<!-- 用于导出 -->
				<form action="<c:url value="/mailStatistic/exportToExcel" />" method="get" id="exptForm">
					<c:if test="${role == UserRole.COMPANY}">
						<input id="areaCode_expt" name="areaCode" type="hidden" />
					</c:if>
					<input id="time_expt" name="time" type="hidden" />
				</form>
				<!-- E 搜索区域 -->
				<div class="tab-bod mt20">
					<!-- S table -->
					<div class="table-responsive">
  					<table class="table">
  						<thead>
  							<tr>
								<c:if test="${role == UserRole.COMPANY}">
									<th>站点</th>
								</c:if>
  								<th>未到站订单数</th>
								<th>已到站订单数</th>
								<th>已分派</th>
								<th>签收</th>
								<th>滞留</th>
								<th>拒收</th>
								<th>转站</th>
								<th>转其他快递</th>
  							</tr>
  						</thead>
  						<tbody id="dataList">
  							<%
								PageModel<ExpressStatStation> pageModel = (PageModel<ExpressStatStation>)request.getAttribute("pageModel");
								if(pageModel==null || pageModel.getDatas() == null || pageModel.getDatas().size() == 0){
							%>
								<tr>
									<td colspan="9">没有站点，请建立站点。</td>
								</tr>
							<%
								}else{
									for(ExpressStatStation ess : pageModel.getDatas()){
							%>
								<tr>
									<c:if test="${role == UserRole.COMPANY}">
										<td><%=ess.getSitename()%></td>
									</c:if>
									<td><%=ess.getNostationcnt()%></td>
									<td><%=ess.getStationcnt()%></td>
									<td><%=ess.getDeliverycnt()%></td>
									<td><%=ess.getSuccesscnt()%></td>
									<td><%=ess.getDailycnt()%></td>
									<td><%=ess.getRefusecnt()%></td>
									<td><%=ess.getChangestationcnt()%></td>
									<td><%=ess.getChangeexpresscnt()%></td>
								</tr>
							<%
								}//for
							}//else
							%>
  						</tbody>
  					</table>
  				</div>
  				<!-- E table -->
  				<!-- S tableBot -->
				<div class="clearfix pad20" id="pagin"></div>
				<!-- E tableBot -->
				</div>
  				
			</div>
			<!-- E detail -->
		</div>
	</div>
</div>
<!-- E content -->


<!-- S footer -->
<footer class="pos-footer tc">
    <em class="b-copy">京ICP备 465789765 号 版权所有 &copy; 2016-2020 棒棒达       北京棒棒达科技有限公司</em>
</footer>
<!-- E footer -->
<script type="text/javascript">

$(document).ready(function() {
	//显示分页条
	var pageStr = paginNav(<%=pageModel.getPageNo()%>, <%=pageModel.getTotalPages()%>, <%=pageModel.getTotalCount()%>);
	$("#pagin").html(pageStr);
	
	//初始化到站时间框
	//$("#time").datetimepicker();
	$("#time").datetimepicker({
		minView: "month", //选择日期后，不会再跳转去选择时分秒
		format: "yyyy-mm-dd", //选择日期后，文本框显示的日期格式
		language: 'zh-CN', //汉化
		autoclose:true //选择日期后自动关闭
	});
});

//加载带有查询条件的指定页的数据
function gotoPage(pageIndex) {
	var areaCode = null;
	<c:if test="${role == UserRole.COMPANY}">
		areaCode = $("#areaCode").val();
	</c:if>
	//查询所有派件员
	$.ajax({
		type : "GET",  //提交方式  
        url : "<c:url value="/mailStatistic/getList" />",//路径
        data : {  
            "pageIndex" : pageIndex,
			"areaCode" : areaCode,
            "time" : $("#time").val(),
        },//数据，这里使用的是Json格式进行传输
        success : function(dataObject) {//返回数据根据结果进行相应的处理 
            var tbody = $("#dataList");
            var dataList = dataObject.datas;
			var datastr = "";
			if(dataList != null && dataList.length > 0){
				for(var i = 0; i < dataList.length; i++){
					datastr += getRowHtml(dataList[i]);
				}
			}
			tbody.html(datastr);
			//更新分页条
			var pageStr = paginNav(pageIndex, dataObject.totalPages, dataObject.totalCount);
			$("#pagin").html(pageStr);
		},
        error : function() {  
           	if(window.top==window.self){//不存在父页面
				window.location.href="<c:url value="/login" />"
			}else{
				window.top.location.href="<c:url value="/login" />"
			}
      	}    
    });	
}	


//封装一行的数据
function getRowHtml(data){
	var row = "<tr>";
	<c:if test="${role == UserRole.COMPANY}">
		row += "<td>" + data.sitename + "</td>";
	</c:if>
	row += "<td>" + data.nostationcnt + "</td>";
	row += "<td>" + data.stationcnt + "</td>";
	row += "<td>" + data.deliverycnt + "</td>";
	row += "<td>" + data.successcnt + "</td>";
	row += "<td>" + data.dailycnt + "</td>";
	row += "<td>" + data.refusecnt + "</td>";
	row += "<td>" + data.changestationcnt + "</td>";
	row += "<td>" + data.changeexpresscnt + "</td>";
	row += "</tr>";
	return row;
}


//导出数据
function exportData() {
	<c:if test="${role == UserRole.COMPANY}">
		$("#areaCode_expt").val($("#areaCode").val());
	</c:if>
	$("#time_expt").val($("#time").val());
	$("#exptForm").submit();
}

</script>
</body>
</html>