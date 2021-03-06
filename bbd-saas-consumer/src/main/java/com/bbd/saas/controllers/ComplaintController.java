package com.bbd.saas.controllers;

import com.bbd.saas.Services.AdminService;
import com.bbd.saas.api.mongo.ComplaintService;
import com.bbd.saas.api.mongo.UserService;
import com.bbd.saas.api.mysql.ComplaintDealService;
import com.bbd.saas.api.mysql.SiteMySqlService;
import com.bbd.saas.constants.UserSession;
import com.bbd.saas.enums.ComplaintStatus;
import com.bbd.saas.models.SiteMySql;
import com.bbd.saas.mongoModels.Complaint;
import com.bbd.saas.mongoModels.User;
import com.bbd.saas.utils.Dates;
import com.bbd.saas.utils.Numbers;
import com.bbd.saas.utils.PageModel;
import com.bbd.saas.utils.StringUtil;
import com.bbd.saas.vo.ComplaintQueryVO;
import com.bbd.saas.vo.entity.ComplaintVO;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/complaint")
public class ComplaintController {
    public static final Logger logger = LoggerFactory.getLogger(ComplaintController.class);
    @Autowired
    ComplaintService complaintService;
    @Autowired
    UserService userService;
    @Autowired
    AdminService adminService;
    @Autowired
    SiteMySqlService siteMySqlService;
    @Autowired
    ComplaintDealService complaintDealService;

    /**
     * 跳转到投诉管理页面
     * @param pageIndex 当前页
     * @param complaintStatus 投诉状态
     * @param appealStatus 申诉状态
     * @param between 投诉时间
     * @param mailNum 运单号
     * @param request 请求
     * @param model 携带响应数据
     * @return 请求页的数据+分页信息
     */
    @RequestMapping(value="", method=RequestMethod.GET)
    public String index(Integer pageIndex, Integer complaintStatus, Integer appealStatus, Integer reason, String between, String mailNum, final HttpServletRequest request, Model model) {
        try {
            if(mailNum != null){
                mailNum = mailNum.trim();
            }
            //往前推一个月的时间
            between = StringUtil.initStr(between, Dates.getBeforeMonthBetweenTime(-1));
            //查询数据
            PageModel<ComplaintVO> complaintPage = this.getList(pageIndex, complaintStatus,appealStatus, reason, between,mailNum,request);
            logger.info("=====投诉管理页面列表===" + complaintPage);
            //当前登录的用户信息
            User user = adminService.get(UserSession.get(request));
            SiteMySql siteMySql = siteMySqlService.selectIdBySiteId(user.getSite().getId().toString());
            model.addAttribute("dataPage", complaintPage);
            model.addAttribute("grade", siteMySql != null ? siteMySql.getSitescore() : 0);
            model.addAttribute("between", between);
            return "page/complaint";
        } catch (Exception e) {
            logger.error("===跳转到投诉管理页面==出错 :" + e.getMessage());
        }
        return "page/dataQuery";
    }

    /**
     * 设置处罚结果
     * @param complaintPage
     */
    private void setDealResult(PageModel<ComplaintVO> complaintPage){
        if(complaintPage != null && complaintPage.getDatas() != null && complaintPage.getDatas().size() > 0){
            List<String> mailNumList = Lists.newArrayList();
            for(ComplaintVO complaintVO : complaintPage.getDatas()){
                if(complaintVO.getComplaintStatus() == ComplaintStatus.COMPLAINT_SUCCESS){//投诉成立的才需要查询
                    mailNumList.add(complaintVO.getMailNum());
                }
            }
            if(mailNumList.isEmpty()){
                return;
            }
            Map<String, String> dealMap = this.complaintDealService.findListByMailNums(mailNumList);
            if(dealMap != null && !dealMap.isEmpty()){
                for(ComplaintVO complaintVO : complaintPage.getDatas()){
                    if(complaintVO.getComplaintStatus() == ComplaintStatus.COMPLAINT_SUCCESS){//投诉成立的才会有处理结果
                        complaintVO.setDealResult(dealMap.get(complaintVO.getMailNum()));
                    }
                }
            }
        }
    }
    /**
     * 分页查询，Ajax更新列表
     * @param pageIndex 当前页
     * @param complaintStatus 投诉状态
     * @param appealStatus 申诉状态
     * @param between 投诉时间
     * @param mailNum 运单号
     * @param request 请求
     * @return 请求页的数据+分页信息
     */
    @ResponseBody
    @RequestMapping(value="/getList", method=RequestMethod.GET)
    public PageModel<ComplaintVO> getList(Integer pageIndex, Integer complaintStatus, Integer appealStatus, Integer reason, String between, String mailNum, final HttpServletRequest request) {
        //查询数据
        PageModel<ComplaintVO> complaintPage = null;
        try {
            if(mailNum != null){
                mailNum = mailNum.trim();
            }
            //参数为空时，默认值设置
            pageIndex = Numbers.defaultIfNull(pageIndex, 0);
            //当前登录的用户信息
            User user = adminService.get(UserSession.get(request));
            //设置查询条件
            ComplaintQueryVO complaintQueryVO = new ComplaintQueryVO();
            complaintQueryVO.complaintStatus = complaintStatus;
            complaintQueryVO.appealStatus = appealStatus;
            complaintQueryVO.reason = reason;
            complaintQueryVO.between = between;
            complaintQueryVO.mailNum = mailNum;
            complaintQueryVO.areaCode = user.getSite().getAreaCode();
            PageModel<Complaint> pageModel = new PageModel<Complaint>();
            pageModel.setPageNo(pageIndex);
            //当前页的数据
            complaintPage = complaintService.findPageDatas(pageModel, complaintQueryVO);
            this.setDealResult(complaintPage);
        } catch (Exception e) {
            logger.error("===分页查询，Ajax查询列表数据===出错:" + e.getMessage());
        }
        return complaintPage;
    }

}
