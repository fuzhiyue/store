<%@ page language="java" 
	contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" 
	uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head lang="en">
    <meta charset="UTF-8">
    <title>收货地理管理 - 达内学子商城</title>
    <link href="../css/orders.css" rel="stylesheet"/>
    <link href="../css/header.css" rel="stylesheet"/>
    <link href="../css/footer.css" rel="stylesheet"/>
    <link href="../css/personage.css" rel="stylesheet" />
</head>
<body>
<!-- 页面顶部-->
<header id="top" class="fixed_nav">
    <div id="logo" class="lf">
        <img class="animated jello" src="../images/header/logo.png" alt="logo"/>
    </div>
    <div id="top_input" class="lf">
        <input id="input" type="text" placeholder="请输入您要搜索的内容"/>
        <a href="search.html" class="rt"><img id="search" src="../images/header/search.png" alt="搜索"/></a>
    </div>
    <div class="rt">
        <ul class="lf">
            <li><a href="favorites.html" title="我的收藏"><img class="care" src="../images/header/care.png" alt=""/></a><b>|</b></li>
            <li><a href="orders.html" title="我的订单"><img class="order" src="../images/header/order.png" alt=""/></a><b>|</b></li>
            <li><a href="cart.html" title="我的购物车"><img class="shopcar" src="../images/header/shop_car.png" alt=""/></a><b>|</b></li>
            <li><a href="help.html">帮助</a><b>|</b></li>
            <li><a href="login.html">登录</a></li>
        </ul>
    </div>
</header>
<!-- nav主导航-->
<nav id="nav">
    <ul>
        <li><a href="index.html" class="acti">首页</a></li>
        <li><a href="index.html#computer" >电脑办公</a></li>
        <li><a href="index.html#stationery" >办公文具</a></li>
    </ul>
</nav>
<!-- 我的订单导航栏-->
<div id="nav_order">
    <ul>
        <li><a href="">首页<span>&gt;</span>个人中心</a></li>
    </ul>
</div>
<!--我的订单内容区域 #container-->
<div id="container" class="clearfix">
    <!-- 左边栏-->
    <div id="leftsidebar_box" class="lf">
        <div class="line"></div>
        <dl class="my_order">
            <dt >我的订单
                <img src="../images/myOrder/myOrder2.png">
            </dt>
            <dd class="first_dd"><a href="orders.html">全部订单</a></dd>
            <dd>
                <a href="#">
                    待付款
                    <span><!--待付款数量--></span>
                </a>
            </dd>
            <dd>
                <a href="#">
                    待收货
                    <span><!--待收货数量-->1</span>
                </a>
            </dd>
            <dd>
                <a href="#">
                    待评价<span><!--待评价数量--></span>
                </a>
            </dd>
            <dd>
                <a href="#">退货退款</a>
            </dd>
        </dl>

        <dl class="footMark">
            <dt >我的优惠卷<img src="../images/myOrder/myOrder1.png"></dt>
        </dl>
        <dl class="address">
                <dt>收货地址<img src="../images/myOrder/myOrder1.png"></dt>
				<dd><a href="addressAdmin.html">地址管理</a></dd>
        </dl>
       <dl class="count_managment">
            <dt >帐号管理<img src="../images/myOrder/myOrder1.png"></dt>
            <dd class="first_dd"><a href="personage.html">我的信息</a></dd>
            <dd><a href="personal_icon.html">个人头像</a></dd>
            <dd><a href="personal_password.html">安全管理</a></dd>
        </dl>
    </div>
   <!-- 右边栏-->
    <div class="rightsidebar_box rt">	
        <!--标题栏-->
        <div class="rs_header">
            <span class="address_title">收获地址管理</span>
        </div>
        <!--收货人信息填写栏-->
        <div class="rs_content">
        	<form method="post" action="handle_add.do">
	            <!--收货人姓名-->
	            <div class="recipients">
	                <span class="red">*</span>
	                <span class="kuan">收货人：</span>
	                <input type="text" name="recvName" id="receiverName"/>
	            </div>
	            <!--收货人所在城市等信息-->
	            <div data-toggle="distpicker" class="address_content">
					 <span class="red">*</span><span class="kuan">省&nbsp;&nbsp;份：</span>
					 <select name="recvProvince" onchange="getCities()" id="recvProvince">
					 	<option value="0">---- 选择省 ----</option>
					 	<c:forEach var="province" items="${provinces }">
					 	<option value="${province.code }">${province.name }</option>
					 	</c:forEach>
					 </select>
					  城市：
					 <select name="recvCity" id="recvCity" onchange="getAreas()">
					 	<option value="0">---- 选择市 ----</option>
					 </select>
					  区/县：
					 <select name="recvArea" id="recvArea">
					 	<option value="0">---- 选择区 ----</option>
					 </select>
				</div> 
	            
	            <!--收货人详细地址-->
	            <div class="address_particular">
	                <span class="red">*</span>
	                <span class="kuan">详细地址：</span>
	                <textarea name="recvAddress" id="receiverAddress" cols="60" rows="3" placeholder="建议您如实填写详细收货地址"></textarea>
	            </div>
	            <!--收货人地址-->
	            <div class="address_tel">
	                <span class="red">*</span>
	                <span class="kuan">手机号码：</span>
	                <input type="tel" name="recvPhone" id="receiverMobile"/>
	                	固定电话：
	                <input type="text" name="recvTel" id="receiverPhone"/>
	            </div>
	            <!--邮政编码-->
	            <div class="address_postcode">
	                <span class="red">&nbsp;</span class="kuan">
	                <span>邮政编码：</span>&nbsp;
	                <input type="text" name="recvZip"/>
	            </div>
	            <!--地址名称-->
	            <div class="address_name">
	                <span class="red">&nbsp;</span class="kuan">
	                <span>地址名称：</span>&nbsp;
	                <input type="text" name="recvTag" id="addressName" />如：<span class="sp">家</span><span class="sp">公司</span><span class="sp">宿舍</span>
	            </div>
	            <!--保存收货人信息-->
	            <div>
	               <input type="submit" value="保存收货人信息" style="padding: 10px 20px;" />
	            </div>
    		</form>
    		
            <!--已有地址栏-->
            <div class="address_information_manage">
                <div class="aim_title">
                    <span class="dzmc dzmc_title">地址名称</span>
                    <span class="dzxm dzxm_title">姓名</span>
                    <span class="dzxq dzxq_title">地址详情</span>
                    <span class="lxdh lxdh_title">联系电话</span>
                    <span class="operation operation_title">操作</span>
                </div>
                
                <c:forEach var="address" items="${addresses }">
                	<div class="aim_content_three 
                		<c:if test="${address.isDefault == 1}">
                		aim_active
                		</c:if>
                	">
                    <span class="dzmc 
                    
                    <c:if test="${address.isDefault == 1}">
                    dzmc_active
                    </c:if>
                    
                    <c:if test="${address.isDefault != 1}">
                    dzmc_normal
                    </c:if>
                    
                    ">${address.recvTag }</span>
                    <span class="dzxm dzxm_normal">${address.recvName }</span>
                    <span class="dzxq dzxq_normal">${address.recvDistrict } ${address.recvAddress }</span>
                    <span class="lxdh lxdh_normal">${address.recvPhone }</span>
                    <span class="operation operation_normal">
                    	<span class="aco_change">修改</span>|<span class="aco_delete">删除</span>
                    </span>
                    <span class="swmr swmr_normal">
                    <c:if test="${address.isDefault != 1}">
                    	<a href="set_default.do?id=${address.id }">设为默认</a>
                    </c:if>
                    </span>
                </div>
                </c:forEach>
                
            </div>
        </div>
    </div>
</div>

<!-- 品质保障，私人定制等-->
<div id="foot_box">
    <div class="icon1 lf">
        <img src="../images/footer/icon1.png" alt=""/>

        <h3>品质保障</h3>
    </div>
    <div class="icon2 lf">
        <img src="../images/footer/icon2.png" alt=""/>

        <h3>私人定制</h3>
    </div>
    <div class="icon3 lf">
        <img src="../images/footer/icon3.png" alt=""/>

        <h3>学员特供</h3>
    </div>
    <div class="icon4 lf">
        <img src="../images/footer/icon4.png" alt=""/>

        <h3>专属特权</h3>
    </div>
</div>
<!-- 页面底部-->
<div class="foot_bj">
    <div id="foot">
        <div class="lf">
             <p class="footer1"><img src="../images/footer/logo.png" alt="" class=" footLogo"/></p>
             <p class="footer2"><img src="../images/footer/footerFont.png" alt=""/></p>
        </div>
        <div class="foot_left lf">
            <ul>
                <li><a href="#"><h3>买家帮助</h3></a></li>
                <li><a href="#">新手指南</a></li>
                <li><a href="#">服务保障</a></li>
                <li><a href="#">常见问题</a></li>
            </ul>
            <ul>
                <li><a href="#"><h3>商家帮助</h3></a></li>
                <li><a href="#">商家入驻</a></li>
                <li><a href="#">商家后台</a></li>
            </ul>
            <ul>
                <li><a href="#"><h3>关于我们</h3></a></li>
                <li><a href="#">关于达内</a></li>
                <li><a href="#">联系我们</a></li>
                <li>
                    <img src="../images/footer/wechat.png" alt=""/>
                    <img src="../images/footer/sinablog.png" alt=""/>
                </li>
            </ul>
        </div>
        <div class="service">
            <p>学子商城客户端</p>
            <img src="../images/footer/ios.png" class="lf">
            <img src="../images/footer/android.png" alt="" class="lf"/>
        </div>
        <div class="download">
            <img src="../images/footer/erweima.png">
        </div>
		<!-- 页面底部-备案号 #footer -->
        <div class="record">
            &copy;2017 达内集团有限公司 版权所有 京ICP证xxxxxxxxxxx
        </div>
    </div>
</div>
</body>
<script type="text/javascript" src="../js/jquery-3.1.1.min.js"></script>
<script src="../js/jquery.page.js"></script>
<script type="text/javascript" src="../js/orders.js"></script>
<script type="text/javascript" src="../js/distpicker.data.js111"></script>
<script type="text/javascript" src="../js/distpicker.js111"></script>
<script type="text/javascript" src="../js/personal.js"></script>
<script type="text/javascript">
	$(".lxdh_normal").each(function(i,e) {
		var phone = $(e).html();
		$(e).html(changePhone(phone));
	});
</script>
<script type="text/javascript">
function getCities() {
	// 一旦选择的省发生变化，则"市"和"区"列表还原为初始状态
	$("#recvCity").empty();
	$("#recvArea").empty();
	$("#recvCity").append("<option value='0'>---- 选择市 ----</option>");
	$("#recvArea").append("<option value='0'>---- 选择区 ----</option>");
	
	// 从省的下拉列表中获取选中项的value，即选中的省的代号
	var provinceCode = $("#recvProvince").val();
	
	// 判断选中的是否是默认项，如果是默认项，则不作任何处理
	if (provinceCode != 0) {
		// 选中的是某个有效省份，则获取对应的城市列表，并显示在"市"的下拉列表中
		$.ajax({
			"url": "../city/list.do",
			"data": "provinceCode=" + provinceCode,
			"type": "GET",
			"dataType": "json",
			"success": function(obj) {
				for (var i = 0; i < obj.data.length; i++) {
					$("#recvCity").append(
						"<option value='" 
						+ obj.data[i].code 
						+ "'>" 
						+ obj.data[i].name + "</option>");
				}
			}
		});
	}
}

function getAreas() {
	// 一旦选择的市发生变化，则"区"列表还原为初始状态
	$("#recvArea").empty();
	$("#recvArea").append("<option value='0'>---- 选择区 ----</option>");
	
	// 从市的下拉列表中获取选中项的value，即选中的市的代号
	var cityCode = $("#recvCity").val();
	
	// 判断选中的是否是默认项，如果是默认项，则不作任何处理
	if (cityCode != 0) {
		// 选中的是某个有效市，则获取对应的区列表，并显示在"区"的下拉列表中
		$.ajax({
			"url": "../area/list.do",
			"data": "cityCode=" + cityCode,
			"type": "GET",
			"dataType": "json",
			"success": function(obj) {
				for (var i = 0; i < obj.data.length; i++) {
					$("#recvArea").append(
						"<option value='" 
						+ obj.data[i].code 
						+ "'>" 
						+ obj.data[i].name + "</option>");
				}
			}
		});
	}
}
</script>
</html>







