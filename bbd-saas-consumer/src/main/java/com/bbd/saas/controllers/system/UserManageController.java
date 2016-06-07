package com.bbd.saas.controllers.system;

import com.bbd.saas.Services.AdminService;
import com.bbd.saas.api.mongo.SiteService;
import com.bbd.saas.api.mongo.UserService;
import com.bbd.saas.api.mysql.BalanceService;
import com.bbd.saas.api.mysql.OpenUserService;
import com.bbd.saas.api.mysql.PostmanUserService;
import com.bbd.saas.constants.UserSession;
import com.bbd.saas.enums.SiteStatus;
import com.bbd.saas.enums.UserRole;
import com.bbd.saas.enums.UserStatus;
import com.bbd.saas.form.UserForm;
import com.bbd.saas.models.Balance;
import com.bbd.saas.models.PostmanUser;
import com.bbd.saas.mongoModels.Site;
import com.bbd.saas.mongoModels.User;
import com.bbd.saas.utils.PageModel;
import com.bbd.saas.utils.SignUtil;
import com.bbd.saas.vo.UserQueryVO;
import flexjson.JSONDeserializer;
import org.apache.commons.lang3.StringUtils;
import org.mongodb.morphia.Key;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.*;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 版权：zuowenhai新石器时代<br/>
 * 作者：zuowenhai@neolix.cn <br/>
 * 生成日期：2016-04-11 <br/>
 * 描述：用户管理
 */
@Controller
@RequestMapping("/userManage")
public class UserManageController {
	public static final Logger logger = LoggerFactory.getLogger(UserManageController.class);
	@Autowired
	private UserService userService;
	@Autowired
	private AdminService adminService;
	@Autowired
	private PostmanUserService userMysqlService;
	@Autowired
	private OpenUserService openUserMysqlService;

	@Autowired
	BalanceService balanceService;

	@Autowired
	SiteService siteService;

	/**
     * 获取用户列表信息
     * @param 
     * @return
     */
	@RequestMapping(value="", method=RequestMethod.GET)
	public String index(HttpServletRequest request,Model model) {
		User userNow = adminService.get(UserSession.get(request));//当前登录用户
		if(userNow.getRole()== UserRole.COMPANY){
			List<Site> siteList = siteService.findSiteListByCompanyId(userNow.getCompanyId(), null);
			model.addAttribute("siteList", siteList);
		}
		PageModel<User> userPage = getUserPage(request,0,null,null,null,null);

		model.addAttribute("userNow", userNow);
		model.addAttribute("userPage", userPage);
		return "systemSet/userManage";
	}
	
	/**
     * 获取用户列表信息
     * @param 
     * @return
     */
	@ResponseBody
	@RequestMapping(value = "/getUserPage", method = RequestMethod.GET)
	public PageModel<User> getUserPage(HttpServletRequest request, Integer pageIndex, String siteId, String roleId , Integer status, String keyword) {
		User userNow = adminService.get(UserSession.get(request));//当前登录用户
		if (pageIndex==null) pageIndex =0 ;

		UserQueryVO userQueryVO = new UserQueryVO();
		userQueryVO.roleId=roleId;
		userQueryVO.status=status;
		userQueryVO.keyword=keyword;

		PageModel<User> pageModel = new PageModel<>();
		pageModel.setPageNo(pageIndex);
		PageModel<User> userPage = new PageModel<>();
		if(UserRole.COMPANY==userNow.getRole()){//公司用户
			userQueryVO.companyId=userNow.getCompanyId();
			Site site = null;
			if(StringUtils.isNotBlank(siteId) && !"-1".equals(siteId)){
				site = siteService.findSite(siteId);
			}
			userPage = userService.findUserList(pageModel,userQueryVO,site);
		}else{//站长
			userQueryVO.roleId= UserRole.SENDMEM.toString();
			userPage = userService.findUserList(pageModel,userQueryVO,userNow.getSite());
		}

		for(User user : userPage.getDatas()){
			user.setIdStr(user.getId().toString());
			user.setRoleMessage(user.getRole().getMessage());
			if(user.getUserStatus()!=null && !user.getUserStatus().getMessage().equals("")){
				user.setStatusMessage(user.getUserStatus().getMessage());
			}
		}
		
		return userPage;
	}
	
	/**
     * 获取用户列表信息
     * @param 
     * @return
     */
	@ResponseBody
	@RequestMapping(value = "/getUserPageFenYe", method = RequestMethod.GET)
	public PageModel<User> getUserPageFenYe(HttpServletRequest request, Integer pageIndex, String siteId, String roleId, Integer status, String keyword) {
		PageModel<User> userPage = getUserPage(request,pageIndex,siteId,roleId,status,keyword);
		
		return userPage;
	}
	
	/**
     * 保存新建用户（派件员）
     * @param userForm
     * @return
     */
	@ResponseBody
	@RequestMapping(value="saveUser", method=RequestMethod.POST)
	public Map<String, Object> saveUser(HttpServletRequest request,@Valid UserForm userForm) throws IOException {
		User currUser = adminService.get(UserSession.get(request));
		//验证该loginName在user表中是否已存在
		User oldUser = userService.findUserByLoginName(userForm.getLoginName());
		//验证在同一个站点下staffid是否已存在
		//查找在mysql的bbt数据库的postmanuser表中是否存在改userForm.getLoginName() 即手机号记录
		Map<String, Object> map = new HashMap<String, Object>();
		PostmanUser postmanUser = userMysqlService.selectPostmanUserByPhone(userForm.getLoginName());
		if((oldUser != null && !oldUser.getId().equals("")) || postmanUser != null && postmanUser.getId() != null){
			////loginName在user表中已存在
			map.put("success", false);
			map.put("msg", "手机号已存在，请重新输入11位手机号!");
			return map;
		}else{
			Date currDate = new Date();
			postmanUser = getPostManUser(currUser, userForm, currDate);
			//保存成功，把数据同步到mysql中的postmanUser表中
			int postmanuserId = userMysqlService.insertUser(postmanUser).getId();
			if(postmanuserId > 0){//保存成功
				User user = getUserByUserForm(currUser, userForm, currDate);
				user.setPostmanuserId(postmanuserId);
				Key<User> userKey = userService.save(user);
				if(userKey != null && userKey.getId() != null){
					//user = userService.findOne(kuser.getId().toString());
					insertToBalance(postmanuserId, postmanUser.getPhone());
					map.put("success", true);
					map.put("msg", "保存用户成功");
				}else {
					userMysqlService.deleteById(postmanuserId);
					map.put("success", false);
					map.put("msg", "保存用户失败");
				}
			}
		}
		return map;
	}

	/**
	 * 同步到mysql中balance表
	 * @param postmanuserId 派件员id (mysql表中的postmanUser ,id 字段值)
	 * @param phone 派件员手机号
     */
	private void insertToBalance(int postmanuserId, String phone){
		Balance balance=new Balance();
		balance.setuId(postmanuserId);
		balance.setPhone(phone);
		balance.setCanuse(0);
		balance.setBalance(0);
		balance.setWithdraw(0);
		balance.setRemark("");
		balance.setDateUpd(new Date());
		balance.setDateNew(new Date());
		balanceService.insertBalance(balance);
	}
	/**
	 * 根据表单信息生成一个用户对象 -- 插入的mongodb中
	 * @param currUser 当前登录用户
	 * @param userForm 表单
	 * @param currDate 当前时间
     * @return 用户对象（站长，派件员）
     */
	private User getUserByUserForm(User currUser, UserForm userForm, Date currDate){
		User user = new User();
		user.setCompanyId(currUser.getCompanyId());
		user.setRealName(userForm.getRealName().replaceAll(" ", ""));
		user.setLoginName(userForm.getLoginName().replaceAll(" ", ""));
		user.setPassWord(userForm.getLoginPass());
		user.setSite(currUser.getSite());
		user.setOperate(currUser);
		//staffid就是该用户的手机号
		user.setStaffid(userForm.getLoginName().replaceAll(" ", ""));
		user.setRole(UserRole.obj2UserRole(userForm.getRoleId()));
		user.setDateAdd(currDate);
		user.setDateUpdate(currDate);
		user.setUserStatus(UserStatus.status2Obj(1));
		return user;
	}

	/**
	 * 根据表单信息生成一个派件员对象 -- 插入到mysql中
	 * @param currUser 当前登录用户
	 * @param userForm 用户表单
	 * @param currDate 当前时间
     * @return 派件员对象
     */
	private PostmanUser getPostManUser(User currUser, UserForm userForm, Date currDate){
		PostmanUser postmanUser = new PostmanUser();
		postmanUser.setNickname(userForm.getRealName().replaceAll(" ", ""));
		postmanUser.setHeadicon("");
		postmanUser.setCardidno("");
		postmanUser.setCompanyname(currUser.getSite().getCompanyName() != null ? currUser.getSite().getCompanyName():"");
		postmanUser.setCompanyid(currUser.getSite().getCompanyId()!=null?Integer.parseInt(currUser.getSite().getCompanyId()):0);

		postmanUser.setSubstation(currUser.getSite().getName());
		postmanUser.setAlipayAccount("");
		postmanUser.setToken("");
		postmanUser.setBbttoken("");
		postmanUser.setLat(0d);
		postmanUser.setLon(0d);
		postmanUser.setHeight(0d);
		postmanUser.setAddr("");
		postmanUser.setAddrdes("");
		postmanUser.setShopurl("");
		postmanUser.setSta("1");//对应mongdb user表中的userStatus,默认1位有效
		postmanUser.setSpreadticket("");
		postmanUser.setPhone(userForm.getLoginName().replaceAll(" ", ""));
		//staffid就是该用户的手机号
		postmanUser.setStaffid(userForm.getLoginName().replaceAll(" ", ""));
		postmanUser.setDateNew(currDate);
		postmanUser.setDateUpd(currDate);
		postmanUser.setPoststatus(1);//默认为1
		//快递员
		postmanUser.setPostrole(0);
		return postmanUser;
	}
	/**
     * 保存修改用户
     * @param userForm
     * @return 
     */
	@ResponseBody
	@RequestMapping(value="editUser", method=RequestMethod.POST)
	public Map<String, Object> editUser(HttpServletRequest request,@Valid UserForm userForm) throws IOException {
		Map<String, Object> map = new HashMap<String, Object>();
		//验证该loginName在user表中是否已存在
		User newUser = userService.findUserByLoginName(userForm.getLoginName());
		//查找在mysql的bbt数据库的postmanuser表中是否存在该userForm.getLoginName() 即手机号记录
		PostmanUser postmanUser = userMysqlService.selectPostmanUserByPhone(userForm.getLoginName());
		if((newUser != null && !newUser.getId().equals("")) || postmanUser != null && postmanUser.getId() != null){
			////loginName在user表中已存在
			map.put("success", false);
			map.put("msg", "手机号已存在，请重新输入11位手机号!");
			return map;
		}else{
			User currentUser = adminService.get(UserSession.get(request));
			User user = userService.findOne(userForm.getUserId());
			Date dateUpdate = new Date();
			user.setRealName(userForm.getRealName().replaceAll(" ", ""));
			user.setPassWord(userForm.getLoginPass());
			user.setLoginName(userForm.getLoginName());
			if(!user.getId().equals(currentUser.getId())){
				//如果修改的用户为站长且修改的用户就是当前登录用户的话，不执行olduser.setOperate(getuser);
				user.setOperate(currentUser);
			}
			user.setRole(UserRole.obj2UserRole(userForm.getRoleId()));
			user.setDateUpdate(dateUpdate);
			Key<User> userKey = userService.save(user);
			if(userKey.getId() != null){
				//原有手机号
				postmanUser = userMysqlService.selectPostmanUserByPhone(userForm.getOldLoginName());
				if (postmanUser != null) {
					postmanUser.setDateUpd(new Date());
					postmanUser.setNickname(userForm.getRealName().replaceAll(" ", ""));
					if(user.getRole() == UserRole.SITEMASTER){
						postmanUser.setPostrole(4);
					}else  if(user.getRole()== UserRole.SENDMEM){
						postmanUser.setPostrole(0);
					}
					//修改手机号
					postmanUser.setStaffid(userForm.getLoginName());
					postmanUser.setPhone(userForm.getOldLoginName());
					int i = userMysqlService.updateByPhone(postmanUser);
					if(i > 0){
						map.put("success", true);
						map.put("msg", "修改用户成功");
					}else{
						map.put("success", false);
						map.put("msg", "修改用户失败");
					}
				}else{
					map.put("success", false);
					map.put("msg", "修改用户失败");
				}
			}else {
				map.put("success", false);
				map.put("msg", "修改用户失败");
			}
		}

		return map;
	}
	
	/**
	 * ajax异步调用
     * 根据realname验证在user表中是否已存在 
     * @param realname
     * @return "true"/"false"
     */
	@ResponseBody
	@RequestMapping(value="/checkUser", method=RequestMethod.GET)
	public String checkUser(Model model,@RequestParam(value = "realname", required = true) String realname,HttpServletResponse response) {
		String checkRealName = "";
		try {
			checkRealName=new String(realname.getBytes("iso-8859-1"),"utf-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		User user = userService.findUserByRealName(checkRealName);
		logger.info("realname"+realname);
		if(user!=null && !user.getId().equals("")){
			return "true";
			
		}else{ 
			return "false";
			
		}
		
	}
	@ResponseBody
	@RequestMapping(value="/editPass", method=RequestMethod.POST)
	public boolean editPass(HttpServletRequest request,@RequestParam(value = "passwordOld", required = true) String passwordOld,@RequestParam(value = "password", required = true) String password,HttpServletResponse response) {
		User currentUser = adminService.get(UserSession.get(request));
		if(passwordOld.equals(currentUser.getPassWord())){
			currentUser.setPassWord(password);
			userService.save(currentUser);
			adminService.put(currentUser);//set user to redis
			return true;
		}else
			return false;

	}

	/**
	 * ajax异步调用
     * 根据user的主键id或者loginName修改user的状态     
     * @param id、loginName、status
     * @return "true"/"false"
     */
	@ResponseBody
	@RequestMapping(value="/changestatus", method=RequestMethod.GET)
	public String changestatus(Model model,@RequestParam(value = "id", required = true) String id,
			@RequestParam(value = "status", required = true) String status,
			@RequestParam(value = "loginName", required = true) String loginName,HttpServletResponse response) {
		if(loginName!=null && !loginName.equals("")){
			User user = userService.findUserByLoginName(loginName);
			if(user!=null && !user.getId().equals("")){
				if(user.getRole()== UserRole.SITEMASTER && user.getSite().getStatus()!= SiteStatus.APPROVE){//站点无效 且为站长用户
					return "false";
				}
				userService.updateUserStatu(loginName, UserStatus.status2Obj(Integer.parseInt(status)));
				int ret = userMysqlService.updateById(Integer.parseInt(status),user.getPostmanuserId());
				return "true";
			}else{
				return "false";
			}

			
		}else{
			return "false";
		}

		
	}
	
	
	/**
	 * ajax异步调用
     * 根据loginName修改user的状态     
     * @return "true"/"false"
     */
	//@ResponseBody
	@RequestMapping(value="/changestatusOuter", method=RequestMethod.POST)
	public void changestatusOuter(HttpServletRequest request,HttpServletResponse response) {
		User user = null;
		System.out.println();
		StringBuilder sb = null;
		BufferedReader in;
		try {
			in = new BufferedReader(new InputStreamReader(request.getInputStream()));
			sb = new StringBuilder();   
		    String line = null;  
		    while ((line = in.readLine()) != null) {   
		         sb.append(line);   
		    }
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		  
		Map<String,Object> map = null;
		//String data = request.getParameter("data");
		JSONDeserializer deserializer = new JSONDeserializer();
		try{
			map = (Map<String,Object>)deserializer.deserialize(sb.toString());
			//System.out.println(map.get("atcode"));
		}catch(Exception e){
			System.out.println(e.getMessage());
		}
		
		
		String sign = map.get("sign")!=null?(String) map.get("sign"):"";
		String code = map.get("code")!=null?(String) map.get("code"):"";
		String timestamp = map.get("timestamp")!=null?(String) map.get("timestamp"):"";
		String loginName = map.get("loginName")!=null?(String) map.get("loginName"):"";
		String newStatus = map.get("newStatus")!=null?(String) map.get("newStatus"):"";
		String oldStatus = map.get("oldStatus")!=null?(String) map.get("oldStatus"):"";
		//根据code查询对应的token
		String token = openUserMysqlService.selectTokenByCode(code);
		String retflag = "";
		
		Map<Object, Object> m = new HashMap<Object, Object>();
		
		m.put("newStatus", newStatus);
		m.put("oldStatus", oldStatus);
		m.put("loginName", loginName);
		m.put("timestamp", timestamp);

		String getSign = SignUtil.makeOpenSign(m,token);
		
		response.setCharacterEncoding("UTF-8");  
	    response.setContentType("application/json; charset=utf-8");  
	    PrintWriter out = null;  
	    try {
			out = response.getWriter();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
		if(getSign.equals(sign)){
			//判断签名是否正确
			if(loginName!=null && !loginName.equals("")){
				user = userService.findUserByLoginName(loginName);
				
				if(user!=null && !user.getId().equals("")){ 
					userService.updateUserStatu(loginName, UserStatus.status2Obj(Integer.parseInt(newStatus)));
					//int ret = userMysqlService.updateById(Integer.parseInt(newStatus),user.getPostmanuserId());
					retflag = "{\"result\":\"ok\"}";
					out.append(retflag);
				}else{
					retflag = "{\"result\":\"error\",\"code\":51002,\"msg\":\"参数错误\"}";
					out.append(retflag);
				}

				
			}else{
				retflag = "{\"result\":\"error\",\"code\":51002,\"msg\":\"参数错误\"}";
				out.append(retflag);
			}
		}else{
			retflag = "{\"result\":\"error\",\"code\":51002,\"msg\":\"签名错误\"}";
			out.append(retflag);
		}

		
		
		
	}
	
	
	/**
	 * ajax异步调用
     * 根据loginName删除user表中的记录
     * @param loginName
     * @return "true"/"false"
     */
	@ResponseBody
	@RequestMapping(value="/delUser", method=RequestMethod.GET)
	public String delUser(Model model,@RequestParam(value = "loginName", required = true) String loginName,HttpServletResponse response) {
		User user = userService.findUserByLoginName(loginName);
		logger.info("loginName"+loginName);
		userService.delUser(user);
		userMysqlService.deleteById(user.getPostmanuserId());
		return "true";
		
	}
	
	/**
	 * ajax异步调用
     * 验证user表下是否已存在loginName记录、以及在mysql bbt 中的postmanuser表中是否存在
     * @param loginName
     * @return "true"/"false"
     */
	@ResponseBody
	@RequestMapping(value="/checkLognName", method=RequestMethod.GET)
	public String checkLognName(Model model,@RequestParam(value = "loginName", required = true) String loginName) {
		User user = userService.findUserByLoginName(loginName);
		PostmanUser postmanUser = userMysqlService.selectPostmanUserByPhone(loginName);
		logger.info("loginName"+loginName);
		if((user!=null && !user.getId().equals("") || (postmanUser!=null && postmanUser.getId()!=null))){
			return "true";
			
		}else{ 
			return "false";
			
		}
	}
	
	/**
	 * ajax异步调用
     * 根据user对象的site和staffid参数查找在相同的site下是否有该staffid
     * @param staffid
     * @return "true"/"false"
     */
	@ResponseBody
	@RequestMapping(value="/checkStaffIdBySiteByStaffid", method=RequestMethod.GET)
	public String checkStaffIdBySiteByStaffid(HttpServletRequest request,Model model,@RequestParam(value = "staffid", required = true) String staffid) {
		User getuser = adminService.get(UserSession.get(request));
		User user = userService.findOneBySiteByStaffid(getuser.getSite(), staffid);
		logger.info("staffid"+staffid);
		if(user!=null && !user.getId().equals("")){
			return "true";
			
		}else{ 
			return "false";
			
		}
	}
	
	/**
	 * ajax异步调用
     * 根据user对象的id获取user对象
     * @param id
     * @return user
     */
	@ResponseBody
	@RequestMapping(value="/getOneUser", method=RequestMethod.GET)
	public User getOneUser(Model model, @RequestParam(value = "id", required = true) String id,
						   @RequestParam(value = "loginName", required = true) String loginName) {
		User user = null;

		if(StringUtils.isNotBlank(id)){
			user = userService.findOne(id);
		}else if(loginName!=null && !loginName.equals("")){
			user = userService.findUserByLoginName(loginName);
		}
		user.setRoleStatus(user.getRole().getStatus());
		return user;
	}
	
}
