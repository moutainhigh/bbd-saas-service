package com.bbd.saas.controllers.system;

import com.bbd.poi.api.SitePoiApi;
import com.bbd.poi.api.vo.Result;
import com.bbd.saas.Services.AdminService;
import com.bbd.saas.api.mongo.SiteService;
import com.bbd.saas.api.mongo.UserService;
import com.bbd.saas.api.mongo.WayService;
import com.bbd.saas.api.mysql.PostcompanyService;
import com.bbd.saas.api.mysql.PostmanUserService;
import com.bbd.saas.api.mysql.SiteMySqlService;
import com.bbd.saas.constants.Constants;
import com.bbd.saas.constants.UserSession;
import com.bbd.saas.enums.*;
import com.bbd.saas.form.SiteForm;
import com.bbd.saas.models.Postcompany;
import com.bbd.saas.models.PostmanUser;
import com.bbd.saas.models.SiteMySql;
import com.bbd.saas.mongoModels.Site;
import com.bbd.saas.mongoModels.User;
import com.bbd.saas.utils.Numbers;
import com.bbd.saas.utils.PageModel;
import com.bbd.saas.utils.StringUtil;
import com.bbd.saas.vo.Option;
import org.apache.commons.lang3.StringUtils;
import org.apache.xmlbeans.impl.common.ConcurrentReaderHashMap;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Key;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 系统设置 -- 站点管理
 * 骆波涛
 */
@Controller
@RequestMapping("/siteManage")
public class SiteManageController {
	public static final Logger logger = LoggerFactory.getLogger(SiteManageController.class);
	@Autowired
	private PostmanUserService userMysqlService;
	@Autowired
	private AdminService adminService;
	@Autowired
	private UserService userService;
	@Autowired
	PostcompanyService postcompanyService;
	@Autowired
	SiteService siteService;
	@Autowired
	SitePoiApi sitePoiApi;
	@Autowired
	WayService wayService;
	@Autowired
	SiteMySqlService siteMySqlService;
	/**
	 * 获取某一公司下的站点列表信息
	 * @param
	 * @return
	 */
	@RequestMapping(method=RequestMethod.GET)
	public String siteManage(HttpServletRequest request, Model model, String keyword) {
		PageModel<Site> sitePage = getSitePage(request, null, null, null, 0, null, -1, -1, keyword);
		model.addAttribute("sitePage", sitePage);
		//当前登录的用户信息
		User currUser = adminService.get(UserSession.get(request));
		//查询登录用户的公司下的所有站点
		List<Option> optionList = siteService.findOptByCompanyIdAndAddress(currUser.getCompanyId(), null, null, null, null, null);
		model.addAttribute("siteList", optionList);
		model.addAttribute("companyId", currUser.getCompanyId());
		return "systemSet/siteManage";
	}

	/**
	 * 站点分页查询
	 * @param request 请求
	 * @param pageIndex 当前页
	 * @param siteIdStr 站点编号集合areaCode1,areaCode2---
	 * @param status 站点状态
	 * @param areaFlag 配送区域状态
	 * @param keyword 查询关键字
	 * @return 每页的数据和分页信息
	 */
	@ResponseBody
	@RequestMapping(value = "/getSitePage", method = RequestMethod.GET)
	public PageModel<Site> getSitePage(HttpServletRequest request, String prov, String city, String area, Integer pageIndex, String siteIdStr, Integer status, Integer areaFlag, String keyword) {
		User currUser = adminService.get(UserSession.get(request));
		if (pageIndex==null) pageIndex = 0 ;
		PageModel<Site> pageModel = new PageModel<>();
		pageModel.setPageNo(pageIndex);
		List<ObjectId> siteIdList = null;
		if(StringUtils.isNotBlank(siteIdStr)){//部分站点
			String [] siteIds = siteIdStr.split(",");
			if(siteIds.length > 0){
				siteIdList = new ArrayList<ObjectId>();
				for(String siteId : siteIds){
					siteIdList.add(new ObjectId(siteId));
				}
			}
		}else {//全部
			if(StringUtils.isNotBlank(prov)){//不是公司下的全部站点，是某个省市区下的全部站点
				siteIdList = new ArrayList<ObjectId>();
				List<Option> optionList = siteService.findOptByCompanyIdAndAddress(currUser.getCompanyId(), prov, city, area, null, null);
				if(optionList != null && !optionList.isEmpty()){
					for(Option option : optionList){
						siteIdList.add(new ObjectId(option.getId()));
					}
				}
			}
		}
		PageModel<Site> sitePage = siteService.getSitePage(pageModel,currUser.getCompanyId(), siteIdList, status, areaFlag, keyword);
		for(Site site :sitePage.getDatas()){
			site.setTurnDownMessage(site.getTurnDownReasson() == null ? "" : site.getTurnDownReasson().getMessage());
		}
		return sitePage;
	}
	/**
	 * 根据区域码获取站点
	 * @param
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/getSiteByAreaCode", method = RequestMethod.GET)
	public Site getSiteByAreaCode(HttpServletRequest request, String areaCode) {
		return siteService.findSiteByAreaCode(areaCode);
	}

	/**
	 * 检查手机号是否被注册
	 * @param loginName 手机号
	 * @param areaCode 站点code
	 * @return true:手机号尚未被注册，可以用； false : 手机号已被注册，不可以用。
	 */
	@ResponseBody
	@RequestMapping(value="/checkSiteWithLoginName", method=RequestMethod.GET)
	public Map<String, Object> checkSiteWithUsername(@RequestParam(value = "loginName", required = true) String loginName, String areaCode) {
		Map<String, Object> map = new ConcurrentReaderHashMap();;
		try{
			User user = userService.findUserByLoginName(loginName);
			if(user == null){//新添加手机号
				PostmanUser postmanUser = userMysqlService.selectPostmanUserByPhone(loginName, 0);
				if(postmanUser == null){
					map.put("success", true);
				}else{
					map.put("success", false);
					map.put("msg", "手机号已注册派件员");
				}
			}else{//修改
				if(user.getSite() != null && user.getSite().getAreaCode().equals(areaCode)){//修改，可以用
					map.put("success", true);
				}else{
					map.put("success", false);
					map.put("msg", "手机号已存在");
				}
			}
		}catch (Exception e){
			e.printStackTrace();
			map.put("success", false);
		}
		return map;
	}
//	@RequestMapping(value="/updateSite", method=RequestMethod.GET)
//	public String updateSite(Model model, HttpServletRequest request) {
//		List<Postcompany> postcompanyList = postcompanyService.selectAll();
//		model.addAttribute("postcompanyList",postcompanyList);
//		Site site =siteService.findSite(request.getParameter("siteid"));
//		if("0".equals(site.getFlag())){
//			site.setFlag("1");
//			siteService.save(site);//更新审核状态并保存站点
//		}
//		model.addAttribute("site",site);
//		model.addAttribute("ossUrl",ossUrl);
//		return "site/updateSite";
//	}

	/**
	 * 公司用户新建站点
	 * @param siteForm
	 * @param result
	 * @return
	 * @throws IOException
	 */
	@ResponseBody
	@RequestMapping(value="/saveSite",method=RequestMethod.POST)
	public boolean saveSite(HttpServletRequest request, @Valid SiteForm siteForm, BindingResult result) throws IOException {
		/*if(siteForm.getSitetype() == null || siteForm.getSitetype() != SiteType.EXPRESS_CABINET){
			siteForm.setSiteSrc(null);
		}*/
		User userNow = adminService.get(UserSession.get(request));
		if (result.hasErrors()) {
			return false;
		}
		Site site = new Site();
		User user = new User();
		PostmanUser postmanUser = new PostmanUser();
		String newPhone = siteForm.getPhone();
		String oldPhone = "";
		if(!Constants.BBD_COMPANYID.equals(userNow.getCompanyId())){//非棒棒达公司，保存时默认值下限单量和上限单量分别存为“50、300”
			siteForm.setLowerlimit(50);
			siteForm.setUpperlimit(300);
		}
		if (StringUtils.isNotBlank(siteForm.getAreaCode())) {//公司用户对站点进行修改
			site = siteService.findSiteByAreaCode(siteForm.getAreaCode());//更新操作
			oldPhone = site.getUsername();
			if(Constants.BBD_COMPANYID.equals(userNow.getCompanyId())){//棒棒达公司账号登录，不要修改站点所属的公司信息
				siteForm.setCompanyId(site.getCompanyId());
			}else{
				siteForm.setCompanyId(userNow.getCompanyId());
			}
			BeanUtils.copyProperties(siteForm, site);
			//当前登录公司用户的公司ID
			this.setSiteCompany(site, siteForm.getCompanyId());
			user = userService.findUserByLoginName(site.getUsername());
			if(user == null){//原始站长被删除了
				List<User> userList = userService.findUsersBySite(site,UserRole.SITEMASTER,null);
				if(userList!=null && userList.size()>0){
					user = userList.get(0);
				}else {
					return false;
				}
			}
			user.setLoginName(newPhone);
			postmanUser = userMysqlService.selectPostmanUserByPhone(site.getUsername(), 0);
			if (postmanUser == null) {
				postmanUser = new PostmanUser();
			}
			site.setUsername(newPhone);

			user.setPassWord(siteForm.getPassword());
			user.setUserStatus(UserStatus.VALID);//公司修改设置为有效
		} else {
			if ("1".equals(siteForm.getFrom())) {//外部注册 驳回时的修改
				Site siteTemp = siteService.findSiteByUserName(siteForm.getPhone());
				if (siteTemp != null) {//更新操作
					site = siteTemp;
				}
				oldPhone = site.getUsername();//newPhone == oldPhone
				BeanUtils.copyProperties(siteForm, site);
				setSiteCompany(site, siteForm.getCompanyId());
				postmanUser = userMysqlService.selectPostmanUserByPhone(siteForm.getPhone(), 0);
				if (postmanUser == null)
					postmanUser = new PostmanUser();
				site.setStatus(SiteStatus.WAIT);
				site.setMemo("您的棒棒达快递账号申请信息提交成功。我们将在1-3个工作日完成审核。");
				user = userService.findUserByLoginName(site.getUsername());
				user.setUserStatus(UserStatus.INVALID);//设置为无效
				postmanUser.setSta("3");//对应mongdb user表中的userStatus,默认3位无效
			} else {//公司用户创建
				Site siteTemp = siteService.findSiteByUserName(siteForm.getPhone());
				if(siteTemp != null){//更新操作
					site = siteTemp;
				}
				BeanUtils.copyProperties(siteForm, site);
				String areaCode = siteService.dealOrderWithGetAreaCode(site.getProvince() + site.getCity());
				site.setAreaCode(areaCode);
				setSiteCompany(site, userNow.getCompanyId());
				site.setStatus(SiteStatus.APPROVE);
				site.setMemo("您提交的信息已审核通过，您可访问http://www.bangbangda.cn登录。");
				user.setPassWord(siteForm.getPassword());
				user.setUserStatus(UserStatus.VALID);//公司创建的为有效
				postmanUser = userMysqlService.selectPostmanUserByPhone(siteForm.getPhone(), 0);
				if (postmanUser == null) {
					postmanUser = new PostmanUser();
				} else {//手机号码重复，覆盖
					oldPhone = siteForm.getPhone();
				}
			}
			site.setDateAdd(new Date());
			site.setUsername(siteForm.getPhone());
			user.setDateAdd(new Date());
			user.setLoginName(site.getUsername());
			//设置postmanUser默认值
			setPostmanUserDefaultValue(postmanUser, user.getLoginName());
		}
		site.setDateUpd(new Date());
		Key<Site> siteKey = siteService.save(site);//保存站点
		this.siteService.addSPOIAndSetLatAndLng(siteKey.getId().toString());//设置经纬度
		//setLatAndLng(siteKey.getId().toString());//设置经纬度
		site.setId(new ObjectId(siteKey.getId().toString()));
		//向用户表插入登录用户
		this.addUser(user, site, postmanUser, siteForm, newPhone, oldPhone);
		SiteMySql siteMySql = this.siteToSiteMySql(site);
		siteMySql.setSiteid(site.getId().toString());
		//更新mysql中的site表
		this.siteMySqlService.save(siteMySql);
		return true;
	}
	private SiteMySql siteToSiteMySql(Site site){
		SiteMySql siteMySql = new SiteMySql();
		BeanUtils.copyProperties(site, siteMySql);
		siteMySql.setSiteid(site.getId().toString());
		siteMySql.setDateadd(site.getDateAdd());
		siteMySql.setCompanyid(site.getCompanyId());
		siteMySql.setSitestatus(site.getStatus().toString());
		siteMySql.setSitetype(site.getSitetype() == null ? 0 : site.getSitetype().getStatus());
		siteMySql.setDaycnt(0);
		return siteMySql;
	}
	private void setSiteCompany(Site site, String companyId){
		Postcompany postcompany = postcompanyService.selectPostmancompanyById(Numbers.parseInt(companyId, 0));//当前登录公司用户的公司ID
		if (postcompany != null) {
			site.setCompanyId(companyId);
			site.setCompanyName(postcompany.getCompanyname());
			site.setCompanycode(postcompany.getCompanycode());
		}
	}

	/**
	 * 设置postmanUser默认值
	 * @param postmanUser
	 * @param loginName
	 */
	private void setPostmanUserDefaultValue(PostmanUser postmanUser, String loginName){
		postmanUser.setHeadicon("");
		postmanUser.setCardidno("");
		postmanUser.setAlipayAccount("");
		postmanUser.setToken("");
		postmanUser.setBbttoken("");
		postmanUser.setLat(0d);
		postmanUser.setLon(0d);
		postmanUser.setHeight(0d);
		postmanUser.setAddr("");
		postmanUser.setAddrdes("");
		postmanUser.setShopurl("");
		postmanUser.setSpreadticket("");
		postmanUser.setDateNew(new Date());
		postmanUser.setPoststatus(1);//默认为1
		postmanUser.setPostrole(4);
		postmanUser.setSta("1");//对应mongdb user表中的userStatus,默认1位有效
		//staffid就是该用户的手机号
		postmanUser.setStaffid(loginName.replaceAll(" ", ""));
	}

	/**
	 * 向mysql同步用户信息
	 * @param postmanUser 派件员--mysql
	 * @param user 派件员--mongodb
	 * @param areaCode 站点编码
	 * @param newPhone 新输入手机号
	 * @param oldPhone 旧手机号
	 */
	private PostmanUser addOrUpdatePostmanUser(PostmanUser postmanUser, User user, String areaCode, String newPhone, String oldPhone){
		postmanUser.setNickname(user.getRealName().replaceAll(" ", ""));
		postmanUser.setCompanyname(user.getSite().getCompanyName() != null ? user.getSite().getCompanyName() : "");
		postmanUser.setCompanyid(user.getSite().getCompanyId() != null ? Integer.parseInt(user.getSite().getCompanyId()) : 0);
		postmanUser.setSubstation(user.getSite().getName());
		postmanUser.setAreaCode(user.getSite().getAreaCode());
		postmanUser.setPhone(user.getLoginName().replaceAll(" ", ""));
		postmanUser.setDateUpd(new Date());
		postmanUser.setSiteid(user.getSite().getId().toString());
		//删除mysql中与新手机号相同的手机号
		userMysqlService.deleteByPhoneAndId(newPhone, user.getPostmanuserId());
		if (StringUtils.isNotBlank(areaCode)) {//修改
			postmanUser.setPhone(newPhone);
			int count = 0;
			while(count < 3){//保存失败，最多尝试3次
				count++;
				int n = userMysqlService.updateByPhone(postmanUser, oldPhone);
				if(n > 0){//保存成功
					break;
				}
			}
			count = 0;
			while(count < 3){//保存失败，最多尝试3次
				count++;
				int m = userMysqlService.updateSitenameBySiteId(postmanUser.getSiteid(), postmanUser.getSubstation());
				if(m > 0){//保存成功
					break;
				}
			}
		} else {//新增
			int count = 0;
			while(count < 3){//保存失败，最多尝试3次
				count++;
				postmanUser = userMysqlService.insertUser(postmanUser);
				if(postmanUser.getId() != null && postmanUser.getId() > 0){//保存成功
					break;
				}
			}
		}
		return postmanUser;
	}
	private void addUser(User user, Site site, PostmanUser postmanUser, SiteForm siteForm, String newPhone, String oldPhone){
		user.setRealName(site.getResponser());
		user.setSite(site);
		user.setRole(UserRole.SITEMASTER);
		user.setCompanyId(site.getCompanyId());
		user.setDateUpdate(new Date());
		user.setEmail(siteForm.getEmail());
		//向mysql同步用户信息
		postmanUser = addOrUpdatePostmanUser(postmanUser, user, siteForm.getAreaCode(), newPhone, oldPhone);
		user.setPostmanuserId(postmanUser.getId());
		userService.save(user);//更新用户
	}

	/**
	 * 审核通过站点
	 * @param request
	 * @param phone
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/validSite", method = RequestMethod.GET)
	public boolean validSite(HttpServletRequest request, String phone) {
		Site site = siteService.findSiteByUserName(phone);
		logger.info("审核通过站点：" + site.getId().toHexString()+",IP地址："+ StringUtil.getIpAddr(request));
		siteService.validSite(site.getId().toHexString());//审核通过站点
		//setLatAndLng(site.getId().toHexString());//设置经纬度
		this.siteService.addSPOIAndSetLatAndLng(site.getId().toHexString());//设置经纬度
		//将用户设置为有效
		try{
			User user = userService.findUserByLoginName(phone);
			PostmanUser postmanUser = userMysqlService.selectPostmanUserByPhone(phone, 0);
			if(postmanUser==null) {
				postmanUser = new PostmanUser();
			}
			postmanUser.setSta("1");//对应mongdb user表中的userStatus,默认1位有效
			postmanUser.setHeadicon("");
			postmanUser.setCardidno("");
			postmanUser.setAlipayAccount("");
			postmanUser.setToken("");
			postmanUser.setBbttoken("");
			postmanUser.setLat(0d);
			postmanUser.setLon(0d);
			postmanUser.setHeight(0d);
			postmanUser.setAddr("");
			postmanUser.setAddrdes("");
			postmanUser.setShopurl("");
			postmanUser.setSpreadticket("");
			postmanUser.setDateNew(new Date());
			postmanUser.setPoststatus(1);//默认为1
			postmanUser.setPostrole(4);//站长角色
			//staffid就是该用户的手机号
			postmanUser.setStaffid(user.getLoginName().replaceAll(" ", ""));
			//向mysql同步用户信息
			postmanUser.setNickname(user.getRealName().replaceAll(" ", ""));
			postmanUser.setCompanyname(user.getSite().getCompanyName()!=null?user.getSite().getCompanyName():"");
			postmanUser.setCompanyid(user.getSite().getCompanyId()!=null?Integer.parseInt(user.getSite().getCompanyId()):0);
			postmanUser.setSubstation(user.getSite().getName());
			postmanUser.setAreaCode(user.getSite().getAreaCode());
			postmanUser.setPhone(user.getLoginName().replaceAll(" ", ""));
			postmanUser.setDateUpd(new Date());
			postmanUser.setSiteid(user.getSite().getId().toString());
			if(postmanUser.getId()!=null){//修改
				userMysqlService.updateByPhone(postmanUser, phone);
			}else{//新增
				int postmanuserId = userMysqlService.insertUser(postmanUser).getId();
				user.setPostmanuserId(postmanuserId);
			}
			user.setUserStatus(UserStatus.VALID);
			userService.save(user);//更新用户
		}catch (Exception e){
			e.printStackTrace();
		}

		return true;
	}

	/**
	 * 审核驳回站点
	 * @param
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/turnDownSite", method = RequestMethod.GET)
	public boolean turnDownSite(HttpServletRequest request, String phone,Integer turnDownReason,String otherMessage) {
		Site site = siteService.findSiteByUserName(phone);
		site.setMemo( "抱歉，您的棒棒达快递账号未审核通过。具体原因如下：");
		site.setStatus(SiteStatus.TURNDOWN);
		site.setTurnDownReasson(SiteTurnDownReasson.status2Obj(turnDownReason));
		site.setOtherMessage(otherMessage);
		site.setDateUpd(new Date());
		siteService.save(site);
		return true;
	}
	/**
	 * 停用站点
	 * @param areaCode
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/stopSite", method = RequestMethod.GET)
	public boolean stopSite(String areaCode,HttpServletRequest request) {
		Site site = siteService.findSiteByAreaCode(areaCode);
		site.setStatus(SiteStatus.INVALID);
		site.setDateUpd(new Date());
		siteService.save(site);
		List<User> userList = userService.findUsersBySite(site, UserRole.SITEMASTER, UserStatus.VALID);//所有有效站长
		for(User user:userList){//将站点下的所有站长置为无效
			userService.updateUserStatu(user.getLoginName(), UserStatus.INVALID);
			userMysqlService.updateById(UserStatus.INVALID.getStatus(),user.getPostmanuserId());
		}
		//停用配送区域,失败最多尝试3次
		Integer areaFlag = 0;
		int operCount = 0;
		while (areaFlag != 1 && operCount < 3){
			areaFlag = updateArea(areaCode,  0,request);
			operCount ++;
		}
		return true;
	}
	/**
	 * 启用站点
	 * @param areaCode
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/startSite", method = RequestMethod.GET)
	public boolean startSite(String areaCode,HttpServletRequest request) {
		Site site = siteService.findSiteByAreaCode(areaCode);
		logger.info("启用站点：" + site.getId().toHexString()+",IP地址："+ StringUtil.getIpAddr(request));
		site.setStatus(SiteStatus.APPROVE);
		site.setDateUpd(new Date());
		siteService.save(site);
		List<User> userList = userService.findUsersBySite(site, UserRole.SITEMASTER,null);//所有站长
		for(User user:userList){//将站点下的所有站长置为有效
			userService.updateUserStatu(site.getUsername(), UserStatus.VALID);
			userMysqlService.updateById(UserStatus.VALID.getStatus(),user.getPostmanuserId());
		}
		return true;
	}

	/**
	 * 删除站点
	 * @param areaCode
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/delSite", method = RequestMethod.GET)
	public boolean delSite(String areaCode,HttpServletRequest request) {
		Site site = siteService.findSiteByAreaCode(areaCode);
		logger.info("删除站点：" + site.getId().toHexString()+",IP地址："+ StringUtil.getIpAddr(request));
		userService.delUsersBySiteId(site.getId().toHexString());//删除此站点下的所有用户
		siteService.delSiteBySiteId(site.getId().toHexString());//删除站点信息
		return true;
	}

	/**
	 * 配送区域 == 启用 || 停用
	 * @param areaCode 站点编码
	 * @param areaFlag 配送区域启用停用标志（1：启用；0：停用）
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/updateArea", method = RequestMethod.POST)
	public Integer updateArea(String areaCode, Integer  areaFlag,HttpServletRequest request) {
		Site site = siteService.findSiteByAreaCode(areaCode);
		if(site != null){
			if(areaFlag == 1){//启用配送区域,查询是否有路线经过此站点
				long wayNum = wayService.findWayBySiteId(site.getId().toString());
				if(wayNum <= 0){//没有查询到路线
					logger.info("配送区域启用：此站点无司机线路");
					return -1;//此站点无司机线路，请设置
				}
			}
			site.setAreaFlag(areaFlag);
			site.setDateUpd(new Date());
			Key<Site> r = siteService.save(site);
			if(r != null && r.getId() != null){
				try {
					Result result = null;
					if(areaFlag == 1){//启用配送区域
						result =  sitePoiApi.enableSite(site.getId().toString());
						logger.info("配送区域启用：" + site.getId().toHexString()+",IP地址："+ StringUtil.getIpAddr(request));
					}else{//停用配送区域
						result = sitePoiApi.disableSite(site.getId().toString());
						logger.info("配送区域停用：" + site.getId().toHexString()+",IP地址："+ StringUtil.getIpAddr(request));
					}
					if(result != null && result.code == 0){
						return 1;
					}else{//-1：站点不存在
						//往POI表中增加站点记录,默认是启用状态
						this.siteService.addSPOIAndSetLatAndLng(site.getId().toString());//设置经纬度
						if(areaFlag == 0){//停用需要更新一下状态
							result = sitePoiApi.disableSite(site.getId().toString());
							logger.info("配送区域停用：" + site.getId().toHexString()+",IP地址："+ StringUtil.getIpAddr(request));
							if(result != null && result.code == 0){
								return 1;
							}else{
								return 0;
							}
						}
						return 1;
					}
				}catch (Exception e){
					e.printStackTrace();
				}
			}
		}
		return 0;
	}

	@ResponseBody
	@RequestMapping(value = "/updateLoaction", method = RequestMethod.GET)
	public void updateLoaction() throws Exception {
		List<Site> siteList = siteService.findAllSiteList();
		siteList.forEach(site -> {
			logger.info(site.getAreaCode());
			this.siteService.addSPOIAndSetLatAndLng(site.getId().toHexString());
		});
	}
}
