package com.bbd.saas.controllers;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.mongodb.morphia.Key;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;

import com.bbd.saas.Services.AdminService;
import com.bbd.saas.api.mongo.OrderService;
import com.bbd.saas.api.mongo.UserService;
import com.bbd.saas.api.mysql.PostDeliveryService;
import com.bbd.saas.constants.UserSession;
import com.bbd.saas.enums.OrderStatus;
import com.bbd.saas.models.PostDelivery;
import com.bbd.saas.mongoModels.Order;
import com.bbd.saas.mongoModels.User;
import com.bbd.saas.utils.Dates;
import com.bbd.saas.utils.Numbers;
import com.bbd.saas.utils.PageModel;
import com.bbd.saas.utils.StringUtil;
import com.bbd.saas.vo.OrderQueryVO;
import com.bbd.saas.vo.UserVO;

@Controller
@RequestMapping("/packageDispatch")
@SessionAttributes("packageDispatch")
public class PackageDispatchController {
	
	public static final Logger logger = LoggerFactory.getLogger(PackageDispatchController.class);
	
	@Autowired
	OrderService orderService;
	@Autowired
	UserService userService;
	@Autowired
	AdminService adminService;
	@Autowired
	PostDeliveryService postDeliveryService;
	
	/**
	 * Description: 跳转到包裹分派页面
	 * @param pageIndex 当前页
	 * @param status 状态
	 * @param arriveBetween 到站时间
	 * @param courierId 派件员
	 * @param request
	 * @param model
	 * @return
	 * @author: liyanlei
	 * 2016年4月12日下午3:25:07
	 */
	@RequestMapping(value="", method=RequestMethod.GET)
	public String index(Integer pageIndex, Integer status, String arriveBetween, String courierId, final HttpServletRequest request, Model model) {
		//设置默认查询条件
		status = Numbers.defaultIfNull(status, OrderStatus.NOTDISPATCH.getStatus());//未分派
		//到站时间前天、昨天和今天
		arriveBetween = StringUtil.initStr(arriveBetween, Dates.getBetweenTime(new Date(), -2));
		//查询数据
		PageModel<Order> orderPage = getList(pageIndex, status, arriveBetween, courierId, request);
		logger.info("=====运单分派====" + orderPage);
		model.addAttribute("orderPage", orderPage);
		model.addAttribute("arriveBetween", arriveBetween);
		return "page/packageDispatch";
	}
	
	//分页Ajax更新
	@ResponseBody
	@RequestMapping(value="/getList", method=RequestMethod.GET)
	public PageModel<Order> getList(Integer pageIndex, Integer status, String arriveBetween, String courierId, final HttpServletRequest request) {
		//参数为空时，默认值设置
		pageIndex = Numbers.defaultIfNull(pageIndex, 0);
		status = Numbers.defaultIfNull(status, -1);
		//当前登录的用户信息
		User user = adminService.get(UserSession.get(request));
		//设置查询条件
		OrderQueryVO orderQueryVO = new OrderQueryVO();
		orderQueryVO.dispatchStatus = status;
		orderQueryVO.arriveBetween = arriveBetween;
		orderQueryVO.userId = courierId;
		orderQueryVO.areaCode = user.getSite().getAreaCode();
		//查询数据
		PageModel<Order> orderPage = orderService.findPageOrders(pageIndex, orderQueryVO);
		return orderPage;
	}
	
	/**
	 * Description: 运单分派--把到站的包裹分派给派件员
	 * @param mailNum 运单号
	 * @param courierId 派件员id
	 * @param request
	 * @return
	 * @author: liyanlei
	 * 2016年4月16日上午11:36:55
	 */
	@ResponseBody
	@RequestMapping(value="/dispatch", method=RequestMethod.GET)
	public Map<String, Object> dispatch(String mailNum, String courierId, final HttpServletRequest request) {
		Map<String, Object> map = new HashMap<String, Object>();
		//当前登录的用户信息
		User user = adminService.get(UserSession.get(request));
		//查询运单信息
		Order order = orderService.findOneByMailNum(user.getSite().getAreaCode(), mailNum);
		if(order == null){//运单不存在,与站点无关
			map.put("operFlag", 0);//0:运单号不存在
		}else{//运单存在
			//当运单到达站点(未分派)，首次分派;当运单状态处于滞留、拒收时，可以重新分派
			if(OrderStatus.NOTDISPATCH.equals(order.getOrderStatus())//未分派
				||OrderStatus.RETENTION.equals(order.getOrderStatus()) //滞留
				|| OrderStatus.REJECTION.equals(order.getOrderStatus())){//拒收
				saveOrderMail(order, courierId, user.getSite().getAreaCode(), map);//更新mysql
			}else if(order.getUser() != null){//重复扫描，此运单已分派过了
				map.put("operFlag", 2);//0:运单号不存在;1:分派成功;2:重复扫描，此运单已分派过了;3:分派失败;4:未知错误（不可预料的错误）。
			}else{
				map.put("operFlag", 4);//0:运单号不存在;1:分派成功;2:重复扫描，此运单已分派过了;3:分派失败;4:未知错误（不可预料的错误）。
			}
			//saveOrderMail(order, courierId, user.getSite().getAreaCode(), map);
		}
		return map;
	}
	
	/**
	 * Description: 运单分派--mongodb库中跟新派件员和运单状态，添加一条数据到mysql库或者更新mysql库中快递员信息
	 * @param order 订单
	 * @param courierId 派件员Id
	 * @param areaCode 站点编码
	 * @param map
	 * @author: liyanlei
	 * 2016年4月16日上午11:36:08
	 */
	private void saveOrderMail(Order order, String courierId, String areaCode, Map<String, Object> map){
		//查询派件员信息
		User user = userService.findOne(courierId);
		//运单分派给派件员
		order.setUser(user);
		//更新运单状态--已分派
		order.setOrderStatus(OrderStatus.DISPATCHED);
		//order.setOrderStatus(OrderStatus.REJECTION);
		//order.setOrderStatus(OrderStatus.RETENTION);
		//更新运单
		Key<Order> r = orderService.save(order);
		if(r != null){
			saveOneOrUpdatePost(order, user);
			map.put("operFlag", 1);//1:分派成功
			//刷新列表
			OrderQueryVO orderQueryVO = new OrderQueryVO();
			orderQueryVO.dispatchStatus = OrderStatus.DISPATCHED.getStatus();
			//orderQueryVO.dispatchStatus = OrderStatus.REJECTION.getStatus();
			//orderQueryVO.dispatchStatus = OrderStatus.RETENTION.getStatus();
			orderQueryVO.userId = courierId;
			orderQueryVO.areaCode = areaCode;
			//查询数据
			PageModel<Order> orderPage = orderService.findPageOrders(0, orderQueryVO);
			map.put("orderPage", orderPage); 
		}else{
			map.put("operFlag", 3);//3:分派失败
		}
	}
	/**
	 * Description: 运单号不存在，则添加一条记录；存在，则更新派件员postManId和staffId
	 * @param order
	 * @param user
	 * @author: liyanlei
	 * 2016年4月21日上午10:37:30
	 */
	private void saveOneOrUpdatePost(Order order, User user){
		int row = postDeliveryService.findCountByMailNum(order.getMailNum());
		if(row == 0){ //保存--插入mysql数据库
			PostDelivery postDelivery = new PostDelivery();
			postDelivery.setCompany_code("BANGBANGDA");
			postDelivery.setDateNew(new Date());
			postDelivery.setDateUpd(new Date());
			postDelivery.setMail_num(order.getMailNum());
			postDelivery.setOut_trade_no(order.getOrderNo());
			postDelivery.setPostman_id(user.getPostmanuserId());
			postDelivery.setReceiver_address(order.getReciever().getAddress());
			postDelivery.setReceiver_city(order.getReciever().getCity());
			postDelivery.setReceiver_company_name("");
			postDelivery.setReceiver_district("");
			postDelivery.setReceiver_name(order.getReciever().getName());
			postDelivery.setReceiver_phone(order.getReciever().getPhone());
			postDelivery.setReceiver_province(order.getReciever().getProvince());
			postDelivery.setSender_address(order.getSender().getAddress());
			postDelivery.setSender_city(order.getSender().getCity());
			postDelivery.setSender_company_name(order.getSrc().getMessage());
			postDelivery.setSender_name(order.getSender().getName());
			postDelivery.setSender_phone(order.getSender().getPhone());
			postDelivery.setSender_province(order.getSender().getProvince());
			postDelivery.setStaffid(user.getStaffid());
			postDelivery.setGoods_number(order.getGoods().size());
			postDelivery.setPay_status("1");
			postDelivery.setPay_mode("4");
			postDelivery.setFlg("1");
			postDelivery.setSta("1");
			postDelivery.setTyp("4");
			postDelivery.setNeed_pay("0");
			postDelivery.setIslooked("0");
			postDelivery.setIscommont("0");
			postDeliveryService.insert(postDelivery);
			logger.info("运单分派成功，已更新到mysql的bbt数据库的postdelivery表，mailNum==="+order.getMailNum()+" staffId=="+user.getStaffid()+" postManId=="+user.getPostmanuserId());			
		}else{//已保存过了，更新快递员信息
			postDeliveryService.updatePostIdAndStaffId(order.getMailNum(), user.getPostmanuserId(), user.getStaffid());
			logger.info("运单重新分派成功，已更新到mysql的bbt数据库的postdelivery表，mailNum==="+order.getMailNum()+" staffId=="+user.getStaffid()+" postManId=="+user.getPostmanuserId());
		}
		
	}
	
	/**
	 * Description: 获取本站点下的所有状态为有效的派件员
	 * @param request
	 * @return
	 * @author: liyanlei
	 * 2016年4月15日上午11:06:19
	 */
	@ResponseBody
	@RequestMapping(value="/getAllUserList", method=RequestMethod.GET)
	public List<UserVO> getAllUserList(final HttpServletRequest request) {
		User user = adminService.get(UserSession.get(request));//当前登录的用户信息
		//查询
		List<UserVO> userVoList = userService.findUserListBySite(user.getSite());
		return userVoList;
	}
}
