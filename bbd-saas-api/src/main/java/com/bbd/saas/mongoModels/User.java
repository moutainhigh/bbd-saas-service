package com.bbd.saas.mongoModels;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.*;

import com.bbd.saas.enums.UserRole;
import com.bbd.saas.enums.UserStatus;

import java.io.Serializable;
import java.util.Date;

/**
 * User
 * 用户表
 * @author luobotao
 * Created by luobotao on 2016/4/10.
 */
@Entity("user")
@Indexes(
        @Index(value = "loginName", fields = @Field("loginName"))
)
public class User implements Serializable {

    private static final long serialVersionUID = 7757716632879758078L;
    @Id
    private ObjectId id;
    private String loginName;//登录名即手机号
    private String passWord;
    private String realName;
    private String email;
    private UserRole role;
    private String group;
    @Reference("site")
    private Site site;
    @Embedded
    private User operate;//用户创建者
    private String lastIp;
    private int loginCount;
    private UserStatus userStatus;//状态标识 1启用,0停用
    private Date dateAdd;//创建时间
    private Date dateUpdate;//修改时间
    private Date dateLogin;//最后一次登录时间
    private String staffid;//员工id
    private int postmanuserId;//postmanuser的主键id
    private String companyId;      //所属公司ID
    private Integer balanceId;      //资金账号ID(针对商户有用)
    private String pwdFlag;      //0修改过密码 1未修改过密码

    private String appKey;               //平台授权key
    private String sessionKey;           //平台授权sessionkey
    private String secret;               //平台加密月
    private String printCode;            //打印机编码
    private int dispatchPermsn; //是否有到站权限。0：无; 1: 有。
    private String solutionFlag;            //是否购买过解决方案 为ICP使用
    @Transient
    private String roleMessage;//存放角色信息
    @Transient
    private int roleStatus;//存放0或1
    @Transient
    private String statusMessage;//存放有效或无效
    @Transient
    private String idStr;//id.toString();

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public String getPassWord() {
        return passWord;
    }

    public void setPassWord(String passWord) {
        this.passWord = passWord;
    }

    public String getRealName() {
        return realName == null ? "" : realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public Site getSite() {
        return site;
    }

    public void setSite(Site site) {
        this.site = site;
    }

    public String getLastIp() {
        return lastIp;
    }

    public void setLastIp(String lastIp) {
        this.lastIp = lastIp;
    }

    public int getLoginCount() {
        return loginCount;
    }

    public void setLoginCount(int loginCount) {
        this.loginCount = loginCount;
    }


    public Date getDateAdd() {
        return dateAdd;
    }

    public void setDateAdd(Date dateAdd) {
        this.dateAdd = dateAdd;
    }

    public Date getDateLogin() {
        return dateLogin;
    }

    public void setDateLogin(Date dateLogin) {
        this.dateLogin = dateLogin;
    }

    public User getOperate() {
        return operate;
    }

    public void setOperate(User operate) {
        this.operate = operate;
    }

    public UserStatus getUserStatus() {
        return userStatus;
    }

    public void setUserStatus(UserStatus userStatus) {
        this.userStatus = userStatus;
    }

    public Date getDateUpdate() {
        return dateUpdate;
    }

    public void setDateUpdate(Date dateUpdate) {
        this.dateUpdate = dateUpdate;
    }

    public String getRoleMessage() {
        return roleMessage;
    }

    public void setRoleMessage(String roleMessage) {
        this.roleMessage = roleMessage;
    }

    public int getRoleStatus() {
        return roleStatus;
    }

    public void setRoleStatus(int roleStatus) {
        this.roleStatus = roleStatus;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public String getStaffid() {
        return staffid;
    }

    public void setStaffid(String staffid) {
        this.staffid = staffid;
    }

    public int getPostmanuserId() {
        return postmanuserId;
    }

    public void setPostmanuserId(int postmanuserId) {
        this.postmanuserId = postmanuserId;
    }

    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getIdStr() {
        return idStr;
    }

    public void setIdStr(String idStr) {
        this.idStr = idStr;
    }

    public Integer getBalanceId() {
        return balanceId;
    }

    public void setBalanceId(Integer balanceId) {
        this.balanceId = balanceId;
    }

    public String getPwdFlag() {
        return pwdFlag;
    }

    public void setPwdFlag(String pwdFlag) {
        this.pwdFlag = pwdFlag;
    }

    public String getAppKey() {
        return appKey;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }

    public String getSessionKey() {
        return sessionKey;
    }

    public void setSessionKey(String sessionKey) {
        this.sessionKey = sessionKey;
    }

    public String getPrintCode() {
        return printCode;
    }

    public void setPrintCode(String printCode) {
        this.printCode = printCode;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }
    public int getDispatchPermsn() {
        return dispatchPermsn;
    }

    public void setDispatchPermsn(int dispatchPermsn) {
        this.dispatchPermsn = dispatchPermsn;
    }

    public String getSolutionFlag() {
        return solutionFlag;
    }

    public void setSolutionFlag(String solutionFlag) {
        this.solutionFlag = solutionFlag;
    }
}